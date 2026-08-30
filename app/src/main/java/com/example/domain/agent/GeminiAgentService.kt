package com.example.domain.agent

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class AgentResponse {
    data class TextMessage(val text: String) : AgentResponse()
    
    data class ToolExecution(
        val tool: ToolModule,
        val arguments: Map<String, Any?>,
        val aiExplanation: String
    ) : AgentResponse()
}

data class AgentHistoryMessage(
    val role: String, // "user" or "model"
    val text: String
)

class GeminiAgentService(
    val toolRegistry: ToolRegistry
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    suspend fun processUserMessage(
        userMessage: String,
        apiKey: String,
        modelId: String,
        isIndonesian: Boolean,
        history: List<AgentHistoryMessage> = emptyList(),
        imageBytes: ByteArray? = null,
        imageMimeType: String? = null,
        context: android.content.Context? = null
    ): AgentResponse = withContext(Dispatchers.IO) {
        val effectiveApiKey = apiKey.trim().ifEmpty {
            val buildKey = BuildConfig.GEMINI_API_KEY.trim()
            if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
        }

        // 1. Semantic search for candidate tools
        val lowercaseQuery = userMessage.trim().lowercase(java.util.Locale.ROOT)
        val hasNumbersOrDimensions = userMessage.any { it.isDigit() } ||
            userMessage.contains("inch", ignoreCase = true) ||
            userMessage.contains("feet", ignoreCase = true) ||
            userMessage.contains("foot", ignoreCase = true) ||
            userMessage.contains("meter", ignoreCase = true) ||
            userMessage.contains("cm", ignoreCase = true) ||
            userMessage.contains("mm", ignoreCase = true)

        val isDefinitionQuery = !hasNumbersOrDimensions && (
            lowercaseQuery.startsWith("what is") ||
            lowercaseQuery.startsWith("explain ") ||
            lowercaseQuery.startsWith("describe ") ||
            lowercaseQuery.startsWith("define ") ||
            lowercaseQuery.startsWith("what are ") ||
            lowercaseQuery.startsWith("tell me about ") ||
            lowercaseQuery.startsWith("apa itu") ||
            lowercaseQuery.startsWith("jelaskan ") ||
            lowercaseQuery.startsWith("apa yang dimaksud") ||
            lowercaseQuery.contains(" meaning") ||
            lowercaseQuery.contains(" definition") ||
            (lowercaseQuery.endsWith("?") && (
                lowercaseQuery.startsWith("what ") ||
                lowercaseQuery.startsWith("why ") ||
                lowercaseQuery.startsWith("how ") ||
                lowercaseQuery.startsWith("apa ") ||
                lowercaseQuery.startsWith("bagaimana ")
            ))
        )

        val hasImage = imageBytes != null
        val candidateTools = if (isDefinitionQuery || hasImage) {
            emptyList()
        } else {
            toolRegistry.findRelevantTools(userMessage, topK = 4)
        }
        
        // If no API key or built-in offline mode, generate fallback text response
        if (effectiveApiKey.isBlank() || modelId == "built_in_engine") {
            val toolMatch = candidateTools.firstOrNull()
            return@withContext if (toolMatch != null) {
                val args = extractOfflineParameters(userMessage, toolMatch)
                val isIndo = isIndonesian
                val exp = if (isIndo) {
                    "Saya mendeteksi Anda membutuhkan alat **${toolMatch.metadata.displayName}**. Saya telah menyiapkan kalkulator interaktif untuk Anda di bawah ini:"
                } else {
                    "I detected that you need the **${toolMatch.metadata.displayName}** tool. I have set up the interactive tool canvas below:"
                }
                AgentResponse.ToolExecution(
                    tool = toolMatch,
                    arguments = args,
                    aiExplanation = exp
                )
            } else {
                val text = if (isIndonesian) {
                    "Saya tidak menemukan kalkulator khusus untuk pertanyaan Anda. Silakan coba tanyakan tentang brick wall, concrete slab, voltage drops, atau stair stringer layout!"
                } else {
                    "I couldn't locate a specialized calculator for your query. Try asking about brick walls, concrete slabs, voltage drops, or stair stringers!"
                }
                AgentResponse.TextMessage(text)
            }
        }

        try {
            // 2. Format candidate tools into Gemini Function Declarations JSON
            val functionDeclarations = JSONArray()
            for (tool in candidateTools) {
                functionDeclarations.put(tool.toGeminiFunctionJson())
            }

            val cutlistDbSummary = if (context != null) com.example.domain.math.CutlistDraftStore.getSavedProjectsSummary(context) else "No saved cutlist projects context available."

            val systemPrompt = if (isIndonesian) {
                "Anda adalah Brillian Copilot, asisten AI teknik dan pertukangan profesional. APLIKASI INI MEMILIKI SUITE LENGKAP SEBANYAK PERSIS 109 ALAT TEKNIK/PERTUKANGAN (termasuk Carpentry & Multi-Stock Cut List Optimizer, Electrical, Plumbing, HVAC, Masonry, Civil Engineering, AR Sizing, dan Color Matching/Painting Coating Studio). Katakan kepada pengguna dengan percaya diri bahwa aplikasi ini memiliki 109 alat jika mereka bertanya.\n\n" +
                "DATABASE PROYEK CUTLIST DALAM APLIKASI:\n" +
                "$cutlistDbSummary\n\n" +
                "ATURAN MENTION & REVISI PROYEK FURNITUR / WOODWORKING:\n" +
                "- Pengguna dapat menyebutkan proyek dengan nama (misal 'Regarding project \"NamaProyek\":' atau 'Meja Kopi') tanpa perlu menyalin teks panjang.\n" +
                "- Ketika pengguna menyebut nama proyek atau meminta 'revisi plan' / 'ubah ukuran' / 'sesuaikan sesuai selera', cari proyek tersebut dari DATABASE PROYEK CUTLIST di atas atau riwayat chat.\n" +
                "- Hitung ulang detail ukuran potongan kayu dan hardware BOM yang direvisi, lalu WAJIB sertakan tag [WOODWORK_DRAFT: ...] dan [PROJECT_NOTES: ...] di akhir jawaban Anda agar kartu pratinjau 3D interaktif diperbarui secara otomatis!\n" +
                "- Jika pengguna membalas/merevisi draft pesan sebelumnya (misal [REVISE DRAFT PLAN ...]), gunakan draft acuan tersebut sebagai baseline dan buat rancangan baru yang dimodifikasi sesuai permintaan pengguna.\n\n" +
                "FITUR BARU CUT LIST OPTIMIZER (1D & 2D MULTI-STOCK STUDIO):\n" +
                "- Tabbed Raw Stock Inventory: Mendukung multi-inventori bahan baku kayu/lembaran yang berbeda (misal Tab Plywood Sheet 2440x1220x18mm, Tab Solid Timber 2000x200x20mm, Tab Balok Kaki 2000x40x60mm). Pengguna dapat beralih antar tab, mengatur kapasitas/pengali stok (multiplier), dan membuat Preset Tipe Material Kustom yang dapat disimpan.\n" +
                "- Manajemen Potongan 2-Baris & Validasi Dimensi: Tiap potongan terorganisir rapi dalam 2 baris (dimensi & kuantitas), otomatis disesuaikan dengan tab stok yang memiliki ketebalan cocok, serta memiliki peringatan threshold jika ukuran potongan melebihi bahan mentah.\n" +
                "- Hardware, Aksesoris & Bill of Materials (BOM) Notes: Bagian catatan non-potong yang dapat dibuka/tutup (collapsible) untuk mencatat perlengkapan seperti lampu LED strip, engsel pintu soft-close, rel laci, sekrup, lem kayu, dan pelapis vernis.\n" +
                "- 3D Isometric Assembly Studio: Visualisasi 3D interaktif potongan rakitan dengan rotasi orbit 360°, hand tool pan, zoom, wireframe toggle, dan exploded view.\n\n" +
                "Saat pengguna mendeskripsikan masalah praktis, pilih alat yang paling sesuai dari daftar fungsi dan panggil fungsi tersebut. JIKA PENGGUNA TIDAK MEMBERIKAN INFORMASI ATAU PARAMETER YANG CUKUP, Anda harus BERPIKIR SENDIRI UNTUK MEREKOMENDASIKAN OPSI TERBAIK & IDEAL (standar industri) dan panggil fungsi tersebut dengan nilai rekomendasi ideal tersebut.\n\n" +
                "JIKA USER MENANYAKAN DEFINISI ATAU INFORMASI TEORITIS (seperti 'Apa itu mahoni?' atau 'Jelaskan hukum ohm'), JANGAN SEKALI-KALI MEMANGGIL FUNGSI ATAU MEMINTA PARAMETER/UKURAN ANGKANNYA. Jawab langsung secara informatif berupa penjelasan teks.\n\n" +
                "JIKA USER MEMINTA DRAFT/DESAIN FURNITUR (seperti meja kopi, meja tulis, rak buku, lemari, kitchen set), Anda harus menghitung detail ukuran potongan kayu yang dibutuhkan, mendukung ketebalan papan yang berbeda (misalnya papan tebal 2cm / 20mm untuk permukaan atas meja, dan kayu balok 2x4 tebal 4cm / 40mm untuk kaki meja). Kemudian, tambahkan tag format [WOODWORK_DRAFT: ...] di akhir jawaban Anda agar pengguna dapat mengimpornya dengan satu klik:\n" +
                "[WOODWORK_DRAFT: Nama Proyek | Dimensi | LabelBagian1,PanjangMm,LebarMm,TebalMm,Qty; LabelBagian2,PanjangMm,LebarMm,TebalMm,Qty; ...]\n" +
                "Contoh untuk Meja Kopi 100x60x40 cm:\n" +
                "[WOODWORK_DRAFT: Meja Kopi | 100x60x40 cm | Table Top Planks,1000,600,20,1; Table Legs (2x4 Rafters),400,0,40,4]\n\n" +
                "KAPANPUN ANDA MEMBUAT DRAFT DESAIN FURNITUR, Anda juga WAJIB melampirkan tag [PROJECT_NOTES: ...] untuk mencantumkan semua kebutuhan bahan non-potong/aksesoris perakitan seperti engsel pintu, rel laci, gagang pintu, sekrup, paku, lem kayu, plitur, pernis, atau cat pelapis:\n" +
                "[PROJECT_NOTES: BahanNonPotong1; BahanNonPotong2; ...]\n" +
                "Contoh:\n" +
                "[PROJECT_NOTES: 4x Engsel Sendok Soft-Close; 2x Gagang Laci Minimalis Hitam; 1 Kotak Sekrup Kayu 30mm; 1 Kaleng Plitur Kayu Melamin Coklat; 1 Liter Vernis Clear Coat]\n\n" +
                "JIKA USER MEMINTA ATAU MENUNJUKKAN KEBUTUHAN AKAN BEBERAPA ALAT PERHITUNGAN ATAU MASALAH MEREKA BISA DIBANTU OLEH BEBERAPA ALAT DI APLIKASI INI, Anda harus merekomendasikan dan menyebutkan beberapa alat tersebut dalam penjelasan Anda. Selain itu, lampirkan tag format berikut di akhir jawaban Anda untuk menampilkan daftar pintasan tombol alat interaktif yang dapat langsung dibuka oleh pengguna:\n" +
                "[RECOMMEND_TOOLS: id_alat_1|Nama Tampilan 1, id_alat_2|Nama Tampilan 2, ...]\n" +
                "Contoh:\n" +
                "[RECOMMEND_TOOLS: widget_cutlist_optimizer|Cut List Optimizer, widget_board_footage|Board Footage Calculator, widget_kerf_bending|Kerf Bending Studio, widget_wood_moisture|Wood Moisture Meter]\n\n" +
                "JIKA USER MENGUNGGAH FOTO/GAMBAR (seperti furnitur, beton, atau logam), analisis dan kenali gambar tersebut secara detail. Tentukan jenis furnitur/konstruksi, estimasi dimensi ideal, material kayu/logam/beton yang dibutuhkan, serta aksesoris hardware penting (seperti engsel/hinge, gagang pintu/door knob, laci, rel laci, sekrup, bracket, dll). Berikan rincian kebutuhan material tersebut, dan lampirkan tag [WOODWORK_DRAFT: ...], [PROJECT_NOTES: ...] serta tag [RECOMMEND_TOOLS: ...] yang sesuai sehingga user bisa mengimpor rancangan tersebut dan melihat model 3D-nya dengan sekali klik!\n\n" +
                "JIKA USER MEMINTA WARNA NYAMAN (cozy colors) ATAU REKOMENDASI WARNA, rekomendasikan palet warna komersial dari brand ternama (seperti Sherwin-Williams, Benjamin Moore, Jotun, Nippon Paint) dan gunakan format tag [COLOR: ...] di akhir jawaban Anda untuk menampilkan sampel swatch warna langsung di chat:\n" +
                "[COLOR: Nama Palette | Nama Warna Base (Code) | #BaseHex | Nama Warna Accent (Code) | #AccentHex | Match Score (0-100) | Brand Name | Ambience Category | Cozy Description]\n" +
                "Contoh:\n" +
                "[COLOR: Warm Hug Cozy | Alabaster (SW 7008) | #F2F0EB | Urbane Bronze (SW 7048) | #534F4A | 98 | Sherwin-Williams | Warm Hygge | Kombinasi Alabaster krem dan Urbane Bronze yang sangat nyaman.]"
            } else {
                "You are Brillian Copilot, an expert trade and engineering assistant. THE APP CONTAINS A COMPREHENSIVE SUITE OF EXACTLY 109 ADVANCED TRADE & ENGINEERING TOOLS (covering Carpentry & Multi-Stock Cut List Optimizer, Electrical, Plumbing, HVAC, Masonry, Civil, AR Sizing, and Color Match Studio). Confidently state that the app has exactly 109 tools if the user asks.\n\n" +
                "SAVED CUTLIST PROJECTS IN APP DATABASE:\n" +
                "$cutlistDbSummary\n\n" +
                "PROJECT MENTIONS & REVISION RULES:\n" +
                "- Users can mention projects by name (e.g. 'Regarding project \"Project X\":' or 'Coffee Table') without needing to copy raw text dumps.\n" +
                "- When the user mentions a project by name or asks to 'revise the plan' / 'modify dimensions' / 'rewrite according to flavor', locate the project in the SAVED CUTLIST PROJECTS DATABASE above or previous chat history.\n" +
                "- Calculate updated cut piece dimensions and hardware BOM, and YOU MUST OUTPUT updated [WOODWORK_DRAFT: ...] and [PROJECT_NOTES: ...] tags so the interactive 3D preview card is dynamically updated!\n" +
                "- If the user replies to or revises a previous draft card (e.g. [REVISE DRAFT PLAN ...]), use that referenced draft as the baseline and generate a brand new modified woodwork plan.\n\n" +
                "NEW CUT LIST OPTIMIZER (1D & 2D MULTI-STOCK STUDIO) CAPABILITIES:\n" +
                "- Tabbed Raw Stock Inventory: Supports managing multiple raw stock inventories in separate horizontal scrollable tabs (e.g. Tab 1: Plywood sheet 2440x1220x18mm, Tab 2: Solid Timber board 2000x200x20mm, Tab 3: Nominal 2x4 framing studs 2000x40x60mm). Users can adjust stock capacity/multipliers and create custom saved material profiles.\n" +
                "- 2-Row Cut Piece Cards & Dimension Thresholds: Each cut piece is organized cleanly in a 2-row layout (dimensions & quantity), matched automatically to stock tabs by thickness, with threshold warnings if pieces exceed raw material bounds.\n" +
                "- Hardware & BOM Notes Dropdown: A collapsible dropdown section for non-cut accessories (LED strips, soft-close hinges, drawer slides, fasteners, polyurethane finishes) with an interactive checklist and quick-add input.\n" +
                "- 3D Isometric Assembly Studio: Full 3D interactive preview of drafted furniture parts with 360° orbit rotation, hand tool pan, zoom, wireframe toggle, and exploded view.\n\n" +
                "When a user describes a practical calculation, select the most relevant tool from the supplied functions list and execute it. CRITICAL: IF THE USER DOES NOT PROVIDE ENOUGH INFO OR PARAMETERS, YOU MUST THINK BY YOURSELF TO RECOMMEND THE IDEAL & TYPICAL INDUSTRY-STANDARD OPTIONS AND CALL THE FUNCTION ANYWAY. Explain your choices in your text.\n\n" +
                "IF THE USER IS ASKING FOR GENERAL THEORETICAL OR INFORMATIONAL DEFINITIONS (such as 'What is mahogany?' or 'Explain carpentry basics'), DO NOT CALL ANY FUNCTIONS OR ASK FOR NUMBERS/LENGTHS. Answer directly as a helpful guide using text response.\n\n" +
                "IF THE USER REQUESTS A FURNITURE DESIGN DRAFT (like a coffee table, desk, bookshelf, cabinet), calculate the exact required wooden cuts, supporting multiple thicknesses where applicable (e.g. 2cm / 20mm thick boards for the tabletop, and 2x4 nominal timber 4cm / 40mm thick for the legs). Then, append a [WOODWORK_DRAFT: ...] tag block at the end of your response text to trigger our single-click drafting importer:\n" +
                "[WOODWORK_DRAFT: Project Name | Dimensions | Piece1Label,LengthMm,WidthMm,ThicknessMm,Qty; Piece2Label,LengthMm,WidthMm,ThicknessMm,Qty; ...]\n" +
                "Example for Coffee Table 100x60x40 cm:\n" +
                "[WOODWORK_DRAFT: Coffee Table | 100x60x40 cm | Table Top Planks,1000,600,20,1; Table Legs (2x4 Rafters),400,0,40,4]\n\n" +
                "WHENEVER YOU GENERATE A DESIGN DRAFT, you MUST also append a [PROJECT_NOTES: ...] tag block to list all required non-cut assembly hardware, materials, or coatings (such as door hinges, drawer slides, pulls, door knobs, paint, stain, glue, or screws) to automatically populate in the project's cutting list notes:\n" +
                "[PROJECT_NOTES: NonCutMaterial1; NonCutMaterial2; ...]\n" +
                "Example:\n" +
                "[PROJECT_NOTES: 4x Soft-close cabinet door hinges; 2x Matte black drawer handles; 1 Box wood screws 30mm; 1 Liter Walnut wood stain; 1 Can clear satin polyurethane]\n\n" +
                "IF THE USER'S QUERY SUGGESTS A NEED FOR MULTIPLE CALCULATIONS OR IF THEIR PROBLEM CAN BE SOLVED BY SEVERAL OF THE 109 TOOLS, you must recommend and mention multiple tools in your explanation. Additionally, append the following format tag block at the end of your text response to render an interactive clickable tools list card in the chat:\n" +
                "[RECOMMEND_TOOLS: tool_id_1|Display Name 1, tool_id_2|Display Name 2, ...]\n" +
                "Example:\n" +
                "[RECOMMEND_TOOLS: widget_cutlist_optimizer|Cut List Optimizer, widget_board_footage|Board Footage Calculator, widget_kerf_bending|Kerf Bending Studio, widget_wood_moisture|Wood Moisture Meter]\n\n" +
                "IF THE USER UPLOADS AN IMAGE/PHOTO (such as furniture, concrete structures, or metal works), analyze and recognize the image in detail. Identify the furniture/construction type, estimate ideal real-world dimensions, calculate exact material requirements (timber, metal, concrete), and list necessary structural hardware accessories (such as hinges, door knobs, drawer slides, shelf pins, brackets, or screws). Provide a complete material checklist, and append the appropriate [WOODWORK_DRAFT: ...], [PROJECT_NOTES: ...] and [RECOMMEND_TOOLS: ...] tags so the user can import the drafted layout and view its 3D model with a single click!\n\n" +
                "IF THE USER ASKS FOR COZY COLORS OR COLOR RECOMMENDATIONS, recommend real commercial colors from brands like Sherwin-Williams, Benjamin Moore, Jotun, or Nippon Paint, and append the following [COLOR: ...] format block to render the visual samples directly in the chat feed:\n" +
                "[COLOR: Palette Name | Base Color Name (Code) | #BaseHex | Accent Color Name (Code) | #AccentHex | Match Score (0-100) | Brand Name | Ambience Category | Cozy Description]\n" +
                "Example:\n" +
                "[COLOR: Warm Hug Cozy | Alabaster (SW 7008) | #F2F0EB | Urbane Bronze (SW 7048) | #534F4A | 98 | Sherwin-Williams | Warm Hygge | A comforting combination of Alabaster cream and Urbane Bronze, generating maximum cozy warmth.]"
            }

            // Assemble Gemini POST Request Body
            val requestJson = JSONObject()
            
            // Add system instruction
            requestJson.put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                })
            })
            
            // Add user message contents with history
            val contentsArray = JSONArray()
            
            // 1. Add all history turns
            for (msg in history) {
                contentsArray.put(JSONObject().apply {
                    put("role", msg.role)
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", msg.text))
                    })
                })
            }
            
            // 2. Add current user message and any inline image data
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", userMessage))
                    if (imageBytes != null && imageMimeType != null) {
                        val base64Data = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
                        put(JSONObject().apply {
                            put("inline_data", JSONObject().apply {
                                put("mime_type", imageMimeType)
                                put("data", base64Data)
                            })
                        })
                    }
                })
            })
            
            requestJson.put("contents", contentsArray)

            // Inject Tools if we have candidates
            if (candidateTools.isNotEmpty()) {
                requestJson.put("tools", JSONArray().apply {
                    put(JSONObject().apply {
                        put("function_declarations", functionDeclarations)
                    })
                })
            }

            // Target Model Selection
            val actualModel = if (modelId.isNotBlank() && modelId != "built_in_engine") modelId else "gemini-2.0-flash"
            
            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$actualModel:generateContent?key=$effectiveApiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val respBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val respJson = JSONObject(respBody)
                val candidates = respJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val partsArr = contentObj?.optJSONArray("parts")
                    
                    if (partsArr != null && partsArr.length() > 0) {
                        var functionCallObj: JSONObject? = null
                        var textResponse: String? = null
                        
                        for (p in 0 until partsArr.length()) {
                            val part = partsArr.getJSONObject(p)
                            if (part.has("functionCall")) {
                                functionCallObj = part.getJSONObject("functionCall")
                            }
                            if (part.has("text")) {
                                textResponse = part.optString("text")
                            }
                        }

                        // Handle Function Call Response
                        if (functionCallObj != null) {
                            val callName = functionCallObj.getString("name")
                            val argsObj = functionCallObj.optJSONObject("args") ?: JSONObject()
                            
                            val rawArgs = mutableMapOf<String, Any?>()
                            val keys = argsObj.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                rawArgs[key] = argsObj.get(key)
                            }

                            // Match back to registered tool
                            val matchedTool = candidateTools.find { it.schema.name == callName }
                                ?: toolRegistry.getToolById(callName)
                                ?: toolRegistry.getToolById("widget_$callName")

                            if (matchedTool != null) {
                                val explanation = textResponse ?: (if (isIndonesian) {
                                    "Saya telah menyiapkan alat **${matchedTool.metadata.displayName}** berdasarkan pertanyaan Anda:"
                                } else {
                                    "I have configured the **${matchedTool.metadata.displayName}** tool based on your request:"
                                })
                                return@withContext AgentResponse.ToolExecution(
                                    tool = matchedTool,
                                    arguments = rawArgs,
                                    aiExplanation = explanation
                                )
                            }
                        }

                        // Fallback to text response
                        if (!textResponse.isNullOrBlank()) {
                            return@withContext AgentResponse.TextMessage(textResponse)
                        }
                    }
                }
                
                val fallbackText = if (isIndonesian) "Saya telah menganalisis pertanyaan Anda tetapi tidak menemukan parameter kalkulasi yang cocok." else "I analyzed your query but could not extract active tool parameters."
                return@withContext AgentResponse.TextMessage(fallbackText)
            } else {
                val errMessage = try {
                    JSONObject(respBody).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}: ${response.message}"
                }
                return@withContext AgentResponse.TextMessage("⚠️ AI Server Error ($actualModel): $errMessage")
            }
        } catch (e: Exception) {
            Log.e("GeminiAgent", "Error communicating with Gemini", e)
            return@withContext AgentResponse.TextMessage("⚠️ Network Connection Error: ${e.localizedMessage ?: e.message}")
        }
    }

    private fun ToolModule.toGeminiFunctionJson(): JSONObject {
        val schemaObj = JSONObject()
        schemaObj.put("name", schema.name)
        schemaObj.put("description", schema.description)
        
        val paramsObj = JSONObject()
        paramsObj.put("type", "OBJECT")
        
        val propsObj = JSONObject()
        val requiredArr = JSONArray()
        
        for (param in schema.parameters) {
            val prop = JSONObject()
            val jsonType = when (param.type) {
                ParameterType.STRING -> "STRING"
                ParameterType.NUMBER -> "NUMBER"
                ParameterType.BOOLEAN -> "BOOLEAN"
                ParameterType.ENUM -> "STRING"
            }
            prop.put("type", jsonType)
            prop.put("description", param.description)
            propsObj.put(param.name, prop)
            if (param.isRequired) {
                requiredArr.put(param.name)
            }
        }
        
        paramsObj.put("properties", propsObj)
        if (requiredArr.length() > 0) {
            paramsObj.put("required", requiredArr)
        }
        
        schemaObj.put("parameters", paramsObj)
        return schemaObj
    }

    private fun extractOfflineParameters(query: String, tool: ToolModule): Map<String, Any?> {
        val args = mutableMapOf<String, Any?>()
        val numbers = "\\d+(?:\\.\\d+)?".toRegex().findAll(query).map { it.value.toDouble() }.toList()
        
        var numberIdx = 0
        for (param in tool.schema.parameters) {
            if (param.type == ParameterType.NUMBER) {
                val nextNum = numbers.getOrNull(numberIdx++)
                if (nextNum != null) {
                    args[param.name] = nextNum
                } else {
                    args[param.name] = param.defaultValue
                }
            }
        }
        return args
    }
}
