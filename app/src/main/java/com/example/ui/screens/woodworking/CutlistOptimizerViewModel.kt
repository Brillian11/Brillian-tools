package com.example.ui.screens.woodworking

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.MaterialEntity
import com.example.data.repository.MaterialRepository
import com.example.domain.math.CutPiece
import com.example.domain.math.CutlistExportHelper
import com.example.domain.math.CutlistOptimizationResult
import com.example.domain.math.CutlistOptimizerEngine
import com.example.domain.math.DimensionUnit
import com.example.domain.math.MaterialType
import com.example.domain.math.StockBoard
import com.example.domain.math.StockProfilePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

data class CutlistProject(
    val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val unit: DimensionUnit = DimensionUnit.CM,
    val materialType: MaterialType = MaterialType.TIMBER_BOARD,
    val rawStocks: List<StockBoard> = emptyList(),
    val stockLength: String = "200.0", // fallback
    val stockWidth: String = "20.0",
    val stockThickness: String = "2.0",
    val bladeKerf: String = "0.32", // cm
    val trimMargin: String = "1.0", // cm
    val allowRipCuts: Boolean = true,
    val requestedCuts: List<CutPiece> = emptyList(),
    val projectNotes: String = ""
)

class CutlistOptimizerViewModel(
    private val materialRepository: MaterialRepository
) : ViewModel() {

    val availableMaterials: StateFlow<List<MaterialEntity>> = materialRepository.allMaterials
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeProjectId = MutableStateFlow<String>(UUID.randomUUID().toString())
    val activeProjectId: StateFlow<String> = _activeProjectId.asStateFlow()

    private val _activeProjectName = MutableStateFlow("Woodwork Project 1")
    val activeProjectName: StateFlow<String> = _activeProjectName.asStateFlow()

    private val _autoSaveStatus = MutableStateFlow("Saved ✓")
    val autoSaveStatus: StateFlow<String> = _autoSaveStatus.asStateFlow()

    private val _selectedStockIndex = MutableStateFlow(0)
    val selectedStockIndex: StateFlow<Int> = _selectedStockIndex.asStateFlow()

    private val _autoScaleStockEnabled = MutableStateFlow(true)
    val autoScaleStockEnabled: StateFlow<Boolean> = _autoScaleStockEnabled.asStateFlow()

    private val _dimensionUnit = MutableStateFlow(DimensionUnit.CM)
    val dimensionUnit: StateFlow<DimensionUnit> = _dimensionUnit.asStateFlow()

    private var cachedContext: Context? = null

    private val _savedProjects = MutableStateFlow<List<CutlistProject>>(emptyList())
    val savedProjects: StateFlow<List<CutlistProject>> = _savedProjects.asStateFlow()

    private val _materialType = MutableStateFlow(MaterialType.TIMBER_BOARD)
    val materialType: StateFlow<MaterialType> = _materialType.asStateFlow()

    private val _projectNotes = MutableStateFlow("")
    val projectNotes: StateFlow<String> = _projectNotes.asStateFlow()

    private val _customPresets = MutableStateFlow<List<StockProfilePreset>>(CutlistOptimizerEngine.STOCK_PRESETS)
    val customPresets: StateFlow<List<StockProfilePreset>> = _customPresets.asStateFlow()

    private val _rawStocks = MutableStateFlow<List<StockBoard>>(
        listOf(
            StockBoard(
                id = "stock_1",
                name = "Solid Mahogany Board",
                type = MaterialType.TIMBER_BOARD,
                lengthMm = 2000.0, // 200 cm
                widthMm = 200.0,   // 20 cm
                thicknessMm = 20.0, // 2 cm
                quantityAvailable = 2,
                costPerUnit = 25.0
            ),
            StockBoard(
                id = "stock_2",
                name = "Framing Timber (200×4×6 cm)",
                type = MaterialType.STUD_JOIST,
                lengthMm = 2000.0, // 200 cm
                widthMm = 60.0,    // 6 cm
                thicknessMm = 40.0, // 4 cm
                quantityAvailable = 4,
                costPerUnit = 12.0
            )
        )
    )
    val rawStocks: StateFlow<List<StockBoard>> = _rawStocks.asStateFlow()

    private val _stockBoardLength = MutableStateFlow("200.0") // in active unit (cm)
    val stockBoardLength: StateFlow<String> = _stockBoardLength.asStateFlow()

    private val _stockBoardWidth = MutableStateFlow("20.0")
    val stockBoardWidth: StateFlow<String> = _stockBoardWidth.asStateFlow()

    private val _stockBoardThickness = MutableStateFlow("2.0")
    val stockBoardThickness: StateFlow<String> = _stockBoardThickness.asStateFlow()

    private val _allowRipCuts = MutableStateFlow(true)
    val allowRipCuts: StateFlow<Boolean> = _allowRipCuts.asStateFlow()

    private val _bladeKerf = MutableStateFlow("0.32") // 0.32 cm (~3.2mm)
    val bladeKerf: StateFlow<String> = _bladeKerf.asStateFlow()

    private val _trimMargin = MutableStateFlow("1.0") // 1.0 cm (10mm)
    val trimMargin: StateFlow<String> = _trimMargin.asStateFlow()

    private val colors = CutlistOptimizerEngine.DEFAULT_CUT_COLORS

    private val _requestedCuts = MutableStateFlow(
        listOf(
            CutPiece("1", "Table Top Rails", 1000.0, 100.0, 4, colors[0], thicknessMm = 20.0),
            CutPiece("2", "Table Stretcher", 950.0, 100.0, 1, colors[1], thicknessMm = 20.0),
            CutPiece("3", "Leg Aprons", 450.0, 90.0, 4, colors[2], thicknessMm = 20.0),
            CutPiece("4", "Leg Stud Posts", 750.0, 60.0, 4, colors[3], thicknessMm = 40.0)
        )
    )
    val requestedCuts: StateFlow<List<CutPiece>> = _requestedCuts.asStateFlow()

    private val _optimizationResult = MutableStateFlow(calculateOptimization())
    val optimizationResult: StateFlow<CutlistOptimizationResult> = _optimizationResult.asStateFlow()

    fun setDimensionUnit(newUnit: DimensionUnit) {
        if (_dimensionUnit.value == newUnit) return
        val oldUnit = _dimensionUnit.value
        _dimensionUnit.value = newUnit

        // Convert string inputs smoothly
        val kerfMm = oldUnit.toMm(_bladeKerf.value.toDoubleOrNull() ?: 3.2)
        _bladeKerf.value = if (newUnit == DimensionUnit.CM) String.format(Locale.US, "%.2f", kerfMm / 10.0) else String.format(Locale.US, "%.1f", kerfMm)

        val trimMm = oldUnit.toMm(_trimMargin.value.toDoubleOrNull() ?: 10.0)
        _trimMargin.value = if (newUnit == DimensionUnit.CM) String.format(Locale.US, "%.1f", trimMm / 10.0) else String.format(Locale.US, "%.0f", trimMm)

        triggerAutoSave()
    }

    fun initProjects(context: Context) {
        cachedContext = context.applicationContext
        val prefs = context.getSharedPreferences("brillian_cutlist_projects_pref", Context.MODE_PRIVATE)
        val jsonStr = prefs.getString("saved_projects_v3", null)
            ?: prefs.getString("saved_projects_v2", null)

        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<CutlistProject>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val cutsArray = obj.optJSONArray("cuts")
                    val cutsList = mutableListOf<CutPiece>()
                    if (cutsArray != null) {
                        for (j in 0 until cutsArray.length()) {
                            val cObj = cutsArray.getJSONObject(j)
                            cutsList.add(
                                CutPiece(
                                    id = cObj.optString("id", UUID.randomUUID().toString()),
                                    label = cObj.optString("label", "Cut Piece"),
                                    lengthMm = cObj.optDouble("lengthMm", 600.0),
                                    widthMm = cObj.optDouble("widthMm", 150.0),
                                    quantity = cObj.optInt("quantity", 1),
                                    colorHex = cObj.optLong("colorHex", colors[j % colors.size]),
                                    thicknessMm = cObj.optDouble("thicknessMm", 20.0)
                                )
                            )
                        }
                    }

                    val stocksArray = obj.optJSONArray("rawStocks")
                    val stocksList = mutableListOf<StockBoard>()
                    if (stocksArray != null && stocksArray.length() > 0) {
                        for (k in 0 until stocksArray.length()) {
                            val sObj = stocksArray.getJSONObject(k)
                            val sType = try { MaterialType.valueOf(sObj.optString("type", "TIMBER_BOARD")) } catch (e: Exception) { MaterialType.TIMBER_BOARD }
                            stocksList.add(
                                StockBoard(
                                    id = sObj.optString("id", UUID.randomUUID().toString()),
                                    name = sObj.optString("name", "Stock Board"),
                                    type = sType,
                                    lengthMm = sObj.optDouble("lengthMm", 2000.0),
                                    widthMm = sObj.optDouble("widthMm", 200.0),
                                    thicknessMm = sObj.optDouble("thicknessMm", 20.0),
                                    quantityAvailable = sObj.optInt("quantityAvailable", 2),
                                    costPerUnit = sObj.optDouble("costPerUnit", 0.0)
                                )
                            )
                        }
                    } else {
                        // Create default raw stock from legacy values
                        val legLen = obj.optDouble("stockLengthVal", 2000.0)
                        val legWid = obj.optDouble("stockWidthVal", 200.0)
                        val legThick = obj.optDouble("stockThicknessVal", 20.0)
                        val matType = try { MaterialType.valueOf(obj.optString("materialType", "TIMBER_BOARD")) } catch (e: Exception) { MaterialType.TIMBER_BOARD }
                        stocksList.add(
                            StockBoard(
                                id = UUID.randomUUID().toString(),
                                name = matType.displayName,
                                type = matType,
                                lengthMm = if (legLen > 0) legLen else 2000.0,
                                widthMm = if (legWid > 0) legWid else 200.0,
                                thicknessMm = if (legThick > 0) legThick else 20.0,
                                quantityAvailable = 4
                            )
                        )
                    }

                    val unitStr = obj.optString("unit", "CM")
                    val resolvedUnit = try { DimensionUnit.valueOf(unitStr) } catch (e: Exception) { DimensionUnit.CM }
                    val matTypeName = obj.optString("materialType", "TIMBER_BOARD")
                    val resolvedMatType = try {
                        MaterialType.valueOf(matTypeName)
                    } catch (e: Exception) {
                        MaterialType.TIMBER_BOARD
                    }

                    list.add(
                        CutlistProject(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            name = obj.optString("name", "Project ${i + 1}"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            unit = resolvedUnit,
                            materialType = resolvedMatType,
                            rawStocks = stocksList,
                            stockLength = obj.optString("stockLength", "200.0"),
                            stockWidth = obj.optString("stockWidth", "20.0"),
                            stockThickness = obj.optString("stockThickness", "2.0"),
                            bladeKerf = obj.optString("bladeKerf", "0.32"),
                            trimMargin = obj.optString("trimMargin", "1.0"),
                            allowRipCuts = obj.optBoolean("allowRipCuts", true),
                            requestedCuts = cutsList,
                            projectNotes = obj.optString("projectNotes", "")
                        )
                    )
                }
                _savedProjects.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun persistProjects(context: Context) {
        val prefs = context.getSharedPreferences("brillian_cutlist_projects_pref", Context.MODE_PRIVATE)
        val array = JSONArray()
        for (proj in _savedProjects.value) {
            val obj = JSONObject().apply {
                put("id", proj.id)
                put("name", proj.name)
                put("createdAt", proj.createdAt)
                put("unit", proj.unit.name)
                put("materialType", proj.materialType.name)
                put("stockLength", proj.stockLength)
                put("stockWidth", proj.stockWidth)
                put("stockThickness", proj.stockThickness)
                put("bladeKerf", proj.bladeKerf)
                put("trimMargin", proj.trimMargin)
                put("allowRipCuts", proj.allowRipCuts)
                put("projectNotes", proj.projectNotes)

                val stocksArr = JSONArray()
                for (st in proj.rawStocks) {
                    stocksArr.put(JSONObject().apply {
                        put("id", st.id)
                        put("name", st.name)
                        put("type", st.type.name)
                        put("lengthMm", st.lengthMm)
                        put("widthMm", st.widthMm)
                        put("thicknessMm", st.thicknessMm)
                        put("quantityAvailable", st.quantityAvailable)
                        put("costPerUnit", st.costPerUnit)
                    })
                }
                put("rawStocks", stocksArr)

                val cutsArr = JSONArray()
                for (cut in proj.requestedCuts) {
                    cutsArr.put(JSONObject().apply {
                        put("id", cut.id)
                        put("label", cut.label)
                        put("lengthMm", cut.lengthMm)
                        put("widthMm", cut.widthMm)
                        put("quantity", cut.quantity)
                        put("colorHex", cut.colorHex)
                        put("thicknessMm", cut.thicknessMm)
                    })
                }
                put("cuts", cutsArr)
            }
            array.put(obj)
        }
        prefs.edit().putString("saved_projects_v3", array.toString()).apply()
        _autoSaveStatus.value = "Auto-saved ✓"
    }

    private fun triggerAutoSave() {
        val ctx = cachedContext ?: return
        val currentProject = CutlistProject(
            id = _activeProjectId.value,
            name = _activeProjectName.value,
            createdAt = System.currentTimeMillis(),
            unit = _dimensionUnit.value,
            materialType = _materialType.value,
            rawStocks = _rawStocks.value,
            stockLength = _stockBoardLength.value,
            stockWidth = _stockBoardWidth.value,
            stockThickness = _stockBoardThickness.value,
            bladeKerf = _bladeKerf.value,
            trimMargin = _trimMargin.value,
            allowRipCuts = _allowRipCuts.value,
            requestedCuts = _requestedCuts.value,
            projectNotes = _projectNotes.value
        )
        val existingList = _savedProjects.value.toMutableList()
        val existingIndex = existingList.indexOfFirst { it.id == currentProject.id }
        if (existingIndex >= 0) {
            existingList[existingIndex] = currentProject
        } else {
            existingList.add(0, currentProject)
        }
        _savedProjects.value = existingList
        persistProjects(ctx)
    }

    fun saveCurrentProject(context: Context, customName: String? = null) {
        val nameToUse = (customName ?: _activeProjectName.value).trim().ifEmpty { "Woodwork Project" }
        _activeProjectName.value = nameToUse

        val currentProject = CutlistProject(
            id = _activeProjectId.value,
            name = nameToUse,
            createdAt = System.currentTimeMillis(),
            unit = _dimensionUnit.value,
            materialType = _materialType.value,
            rawStocks = _rawStocks.value,
            stockLength = _stockBoardLength.value,
            stockWidth = _stockBoardWidth.value,
            stockThickness = _stockBoardThickness.value,
            bladeKerf = _bladeKerf.value,
            trimMargin = _trimMargin.value,
            allowRipCuts = _allowRipCuts.value,
            requestedCuts = _requestedCuts.value,
            projectNotes = _projectNotes.value
        )

        val existingList = _savedProjects.value.toMutableList()
        val existingIndex = existingList.indexOfFirst { it.id == currentProject.id }
        if (existingIndex >= 0) {
            existingList[existingIndex] = currentProject
        } else {
            existingList.add(0, currentProject)
        }

        _savedProjects.value = existingList
        persistProjects(context)
    }

    fun saveAsNewProject(context: Context, name: String) {
        val newId = UUID.randomUUID().toString()
        _activeProjectId.value = newId
        _activeProjectName.value = name.ifEmpty { "New Project" }
        saveCurrentProject(context, name)
    }

    fun loadProject(project: CutlistProject) {
        _activeProjectId.value = project.id
        _activeProjectName.value = project.name
        _dimensionUnit.value = project.unit
        _materialType.value = project.materialType
        _rawStocks.value = if (project.rawStocks.isNotEmpty()) project.rawStocks else listOf(
            StockBoard(
                name = project.materialType.displayName,
                type = project.materialType,
                lengthMm = 2000.0,
                widthMm = 200.0,
                thicknessMm = 20.0,
                quantityAvailable = 4
            )
        )
        _stockBoardLength.value = project.stockLength
        _stockBoardWidth.value = project.stockWidth
        _stockBoardThickness.value = project.stockThickness
        _bladeKerf.value = project.bladeKerf
        _trimMargin.value = project.trimMargin
        _allowRipCuts.value = project.allowRipCuts
        _requestedCuts.value = project.requestedCuts
        _projectNotes.value = project.projectNotes
        recalculate()
    }

    fun createNewBlankProject(context: Context, name: String = "New Woodwork Project") {
        val newId = UUID.randomUUID().toString()
        _activeProjectId.value = newId
        _activeProjectName.value = name
        _dimensionUnit.value = DimensionUnit.CM
        _materialType.value = MaterialType.TIMBER_BOARD
        _rawStocks.value = listOf(
            StockBoard(
                id = UUID.randomUUID().toString(),
                name = "Solid Mahogany Board (200×20×2 cm)",
                type = MaterialType.TIMBER_BOARD,
                lengthMm = 2000.0,
                widthMm = 200.0,
                thicknessMm = 20.0,
                quantityAvailable = 2,
                costPerUnit = 25.0
            ),
            StockBoard(
                id = UUID.randomUUID().toString(),
                name = "Framing Timber (200×4×6 cm)",
                type = MaterialType.STUD_JOIST,
                lengthMm = 2000.0,
                widthMm = 60.0,
                thicknessMm = 40.0,
                quantityAvailable = 4,
                costPerUnit = 12.0
            )
        )
        _stockBoardLength.value = "200.0"
        _stockBoardWidth.value = "20.0"
        _stockBoardThickness.value = "2.0"
        _bladeKerf.value = "0.32"
        _trimMargin.value = "1.0"
        _allowRipCuts.value = true
        _projectNotes.value = ""
        _requestedCuts.value = emptyList()
        recalculate()
        saveCurrentProject(context, name)
    }

    fun deleteProject(context: Context, projectId: String) {
        _savedProjects.value = _savedProjects.value.filterNot { it.id == projectId }
        persistProjects(context)
        if (_activeProjectId.value == projectId) {
            if (_savedProjects.value.isNotEmpty()) {
                loadProject(_savedProjects.value.first())
            } else {
                createNewBlankProject(context, "New Woodwork Project")
            }
        }
    }

    fun renameActiveProject(context: Context, newName: String) {
        val trimmed = newName.trim().ifEmpty { "Woodwork Project" }
        _activeProjectName.value = trimmed
        saveCurrentProject(context, trimmed)
    }

    // --- RAW STOCK INVENTORY MANAGEMENT ---

    fun addRawStock(
        name: String,
        type: MaterialType,
        lengthMm: Double,
        widthMm: Double,
        thicknessMm: Double,
        quantity: Int,
        cost: Double = 0.0
    ) {
        val newStock = StockBoard(
            id = UUID.randomUUID().toString(),
            name = name.ifEmpty { "${type.displayName} (${(lengthMm/10).toInt()}×${(widthMm/10).toInt()}×${(thicknessMm/10).toInt()} cm)" },
            type = type,
            lengthMm = lengthMm,
            widthMm = widthMm,
            thicknessMm = thicknessMm,
            quantityAvailable = quantity.coerceAtLeast(1),
            costPerUnit = cost
        )
        _rawStocks.value = _rawStocks.value + newStock
        recalculate()
    }

    fun updateRawStock(stock: StockBoard) {
        _rawStocks.value = _rawStocks.value.map { if (it.id == stock.id) stock else it }
        recalculate()
    }

    fun removeRawStock(stockId: String) {
        _rawStocks.value = _rawStocks.value.filterNot { it.id == stockId }
        recalculate()
    }

    fun duplicateRawStock(stock: StockBoard) {
        val copy = stock.copy(
            id = UUID.randomUUID().toString(),
            name = "${stock.name} (Copy)"
        )
        _rawStocks.value = _rawStocks.value + copy
        recalculate()
    }

    fun selectStock(index: Int) {
        val safeIndex = index.coerceIn(0, (_rawStocks.value.size - 1).coerceAtLeast(0))
        _selectedStockIndex.value = safeIndex
    }

    fun toggleAutoScaleStock(enabled: Boolean) {
        _autoScaleStockEnabled.value = enabled
        if (enabled) {
            autoMultiplyStockForRequiredCuts()
        }
    }

    fun setStockQuantity(stockId: String, quantity: Int) {
        _rawStocks.value = _rawStocks.value.map {
            if (it.id == stockId) it.copy(quantityAvailable = quantity.coerceAtLeast(1)) else it
        }
        recalculate()
    }

    fun autoMultiplyStockForRequiredCuts(stockId: String? = null) {
        val res = _optimizationResult.value
        val stocks = _rawStocks.value.toMutableList()
        var changed = false

        stocks.forEachIndexed { idx, stock ->
            if (stockId == null || stock.id == stockId) {
                // Count how many used boards in optimization belong to this thickness / stock profile
                val usedForThisStock = res.usedBoards.count { 
                    Math.abs(it.totalBoardThicknessMm - stock.thicknessMm) < 0.1 ||
                    it.boardName.contains(stock.name, ignoreCase = true)
                }
                val targetQty = usedForThisStock.coerceAtLeast(1)
                if (stock.quantityAvailable < targetQty) {
                    stocks[idx] = stock.copy(quantityAvailable = targetQty)
                    changed = true
                }
            }
        }

        if (changed) {
            _rawStocks.value = stocks
            recalculate()
        }
    }

    fun updateRawStockQuantity(stockId: String, delta: Int) {
        _rawStocks.value = _rawStocks.value.map {
            if (it.id == stockId) {
                val newQty = (it.quantityAvailable + delta).coerceAtLeast(1)
                it.copy(quantityAvailable = newQty)
            } else it
        }
        recalculate()
    }

    fun createCustomMaterialPreset(
        name: String,
        type: MaterialType,
        lengthMm: Double,
        widthMm: Double,
        thicknessMm: Double,
        description: String = ""
    ): StockProfilePreset {
        val preset = StockProfilePreset(
            name = name.trim().ifEmpty { "Custom Material" },
            type = type,
            lengthMm = lengthMm,
            widthMm = widthMm,
            thicknessMm = thicknessMm,
            description = description.ifEmpty { "Custom workshop profile" }
        )
        _customPresets.value = _customPresets.value + preset
        return preset
    }

    fun addPresetRawStock(preset: StockProfilePreset) {
        val newStock = StockBoard(
            id = UUID.randomUUID().toString(),
            name = preset.name,
            type = preset.type,
            lengthMm = preset.lengthMm,
            widthMm = preset.widthMm,
            thicknessMm = preset.thicknessMm,
            quantityAvailable = 2,
            costPerUnit = 0.0
        )
        _rawStocks.value = _rawStocks.value + newStock
        recalculate()
    }

    fun updateMaterialType(type: MaterialType) {
        _materialType.value = type
        recalculate()
    }

    fun applyStockPreset(preset: StockProfilePreset) {
        addPresetRawStock(preset)
    }

    fun updateStockLength(value: String) {
        _stockBoardLength.value = value
        recalculate()
    }

    fun updateStockWidth(value: String) {
        _stockBoardWidth.value = value
        recalculate()
    }

    fun updateStockThickness(value: String) {
        _stockBoardThickness.value = value
        recalculate()
    }

    fun updateAllowRipCuts(allowed: Boolean) {
        _allowRipCuts.value = allowed
        recalculate()
    }

    fun updateBladeKerf(value: String) {
        _bladeKerf.value = value
        recalculate()
    }

    fun updateTrimMargin(value: String) {
        _trimMargin.value = value
        recalculate()
    }

    fun addCutPiece(label: String, lengthMm: Double, widthMm: Double, quantity: Int, thicknessMm: Double = 20.0) {
        val colorIdx = _requestedCuts.value.size % colors.size
        val newPiece = CutPiece(
            id = System.currentTimeMillis().toString(),
            label = label.ifEmpty { "Cut Piece" },
            lengthMm = lengthMm,
            widthMm = widthMm,
            quantity = quantity,
            colorHex = colors[colorIdx],
            thicknessMm = thicknessMm
        )
        _requestedCuts.value = _requestedCuts.value + newPiece
        recalculate()
    }

    fun updateCutPiece(id: String, label: String, lengthMm: Double, widthMm: Double, quantity: Int, thicknessMm: Double) {
        val list = _requestedCuts.value.map { p ->
            if (p.id == id) {
                p.copy(
                    label = label,
                    lengthMm = lengthMm,
                    widthMm = widthMm,
                    quantity = quantity,
                    thicknessMm = thicknessMm
                )
            } else p
        }
        _requestedCuts.value = list
        recalculate()
    }

    /**
     * Load AI Draft as a BRAND NEW project so it never overwrites existing saved projects!
     */
    fun loadDraft(context: Context, projectName: String, pieces: List<com.example.domain.math.DraftPiece>, notes: String = "") {
        val newId = UUID.randomUUID().toString()
        val finalProjectName = projectName.ifEmpty { "AI Drafted Project" }
        _activeProjectId.value = newId
        _activeProjectName.value = finalProjectName
        _projectNotes.value = notes

        val newCuts = pieces.mapIndexed { index, p ->
            val colorIdx = index % colors.size
            CutPiece(
                id = "draft_${System.currentTimeMillis()}_$index",
                label = p.label,
                lengthMm = p.lengthMm,
                widthMm = p.widthMm,
                quantity = p.quantity,
                colorHex = colors[colorIdx],
                thicknessMm = p.thicknessMm
            )
        }
        _requestedCuts.value = newCuts

        // Create compatible raw stocks for the draft based on thicknesses and dimensions
        val generatedStocks = mutableListOf<StockBoard>()
        val distinctThicknesses = pieces.map { it.thicknessMm }.distinct()
        distinctThicknesses.forEach { th ->
            val maxLen = pieces.filter { it.thicknessMm == th }.maxOfOrNull { it.lengthMm } ?: 2000.0
            val maxWid = pieces.filter { it.thicknessMm == th }.maxOfOrNull { it.widthMm } ?: 200.0
            val isSheet = maxWid > 300.0
            val stockL = if (isSheet) 2440.0 else maxLen.coerceAtLeast(2000.0)
            val stockW = if (isSheet) 1220.0 else maxWid.coerceAtLeast(200.0)
            val matType = if (isSheet) MaterialType.PLYWOOD_SHEET else MaterialType.TIMBER_BOARD

            generatedStocks.add(
                StockBoard(
                    id = UUID.randomUUID().toString(),
                    name = if (isSheet) "Plywood Sheet ${(stockL/10).toInt()}×${(stockW/10).toInt()}×${(th/10).toInt()} cm" else "Raw Stock Timber ${(stockL/10).toInt()}×${(stockW/10).toInt()}×${(th/10).toInt()} cm",
                    type = matType,
                    lengthMm = stockL,
                    widthMm = stockW,
                    thicknessMm = th,
                    quantityAvailable = 4
                )
            )
        }

        if (generatedStocks.isNotEmpty()) {
            _rawStocks.value = generatedStocks
        }

        recalculate()
        saveCurrentProject(context, finalProjectName)
    }

    fun updateProjectNotes(notes: String) {
        _projectNotes.value = notes
        triggerAutoSave()
    }

    fun removeCutPiece(id: String) {
        _requestedCuts.value = _requestedCuts.value.filterNot { it.id == id }
        recalculate()
    }

    private fun recalculate() {
        _optimizationResult.value = calculateOptimization()
        triggerAutoSave()
    }

    fun generateAiMentionPrompt(): String {
        val projName = _activeProjectName.value
        val unit = _dimensionUnit.value
        val stocks = _rawStocks.value
        val cuts = _requestedCuts.value
        val optResult = _optimizationResult.value
        val notes = _projectNotes.value

        val stocksSummary = if (stocks.isEmpty()) "No raw stock boards specified." else stocks.joinToString("\n") {
            "- ${it.name} [${it.type.displayName}] (Stock Qty: ${it.quantityAvailable}): ${unit.format(it.lengthMm)} × ${unit.format(it.widthMm)} × ${unit.format(it.thicknessMm)}"
        }

        val cutsSummary = if (cuts.isEmpty()) "No cut pieces defined yet." else cuts.joinToString("\n") {
            "- ${it.quantity}x ${it.label}: ${unit.format(it.lengthMm)} × ${unit.format(it.widthMm)} × ${unit.format(it.thicknessMm)}"
        }

        val nonCutSummary = if (notes.isNotBlank()) {
            notes.split(";").map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n") { "- $it" }
        } else {
            "No non-cut hardware/accessories listed."
        }

        return """
I would like an expert workshop consultation for my active woodworking project:
**Project Name:** $projName
**Measurement Unit:** ${unit.label}
**Raw Stocks Inventory (Tabbed Multi-Stock Profiles):**
$stocksSummary

**Cut Pieces (${cuts.sumOf { it.quantity }} items total):**
$cutsSummary

**Hardware & Non-Cut BOM Materials Checklist:**
$nonCutSummary

**Current Cut Optimization:**
- Stock Yield / Efficiency: ${String.format(Locale.US, "%.1f", optResult.yieldPercentage)}%
- Required Stock Count: ${optResult.usedBoards.size} board/sheet unit(s)
${if (optResult.unplacedPieces.isNotEmpty()) "- Warning: ${optResult.unplacedPieces.sumOf { it.quantity }} piece(s) exceed available stock dimensions or thickness profile.\n" else ""}
Please provide your recommendations for:
1. Recommended assembly sequence & joinery techniques (pocket holes, mortise & tenon, dowels, or dominoes).
2. Hardware fittings installation advice (hinge clearance, drawer slide alignment, bracket load capacity).
3. Structural rigidity, grain orientation, and load bearing support.
4. Edge finishing, sanding progression, and surface coating/staining advice.
5. Workshop cutting sequence tips for safety and precision.
""".trimIndent()
    }

    private fun calculateOptimization(): CutlistOptimizationResult {
        val unit = _dimensionUnit.value
        val kerfMm = unit.toMm(_bladeKerf.value.toDoubleOrNull() ?: if (unit == DimensionUnit.CM) 0.32 else 3.2)
        val trimMm = unit.toMm(_trimMargin.value.toDoubleOrNull() ?: if (unit == DimensionUnit.CM) 1.0 else 10.0)
        val allowRips = _allowRipCuts.value

        var stocks = _rawStocks.value
        if (stocks.isEmpty()) {
            stocks = listOf(
                StockBoard(
                    id = "default_stock",
                    name = "Default Stock Board (200×20×2 cm)",
                    type = MaterialType.TIMBER_BOARD,
                    lengthMm = 2000.0,
                    widthMm = 200.0,
                    thicknessMm = 20.0,
                    quantityAvailable = 4
                )
            )
        }

        return CutlistOptimizerEngine.optimize(
            stockBoards = stocks,
            requestedCuts = _requestedCuts.value,
            bladeKerfMm = kerfMm,
            trimMarginMm = trimMm,
            allowRipCuts = allowRips
        )
    }

    fun exportPdf(context: Context): Uri? {
        val unit = _dimensionUnit.value
        val kerfMm = unit.toMm(_bladeKerf.value.toDoubleOrNull() ?: 3.175)
        val trimMm = unit.toMm(_trimMargin.value.toDoubleOrNull() ?: 12.7)

        val primaryStock = _rawStocks.value.firstOrNull() ?: StockBoard(name = "Stock", lengthMm = 2000.0, widthMm = 200.0, thicknessMm = 20.0)

        return CutlistExportHelper.exportToPdf(
            context = context,
            projectName = _activeProjectName.value,
            materialType = primaryStock.type,
            stockLengthMm = primaryStock.lengthMm,
            stockWidthMm = primaryStock.widthMm,
            stockThicknessMm = primaryStock.thicknessMm,
            bladeKerfMm = kerfMm,
            trimMarginMm = trimMm,
            requestedCuts = _requestedCuts.value,
            optimizationResult = _optimizationResult.value,
            projectNotes = _projectNotes.value
        )
    }

    fun exportExcel(context: Context): Uri? {
        val unit = _dimensionUnit.value
        val kerfMm = unit.toMm(_bladeKerf.value.toDoubleOrNull() ?: 3.175)
        val trimMm = unit.toMm(_trimMargin.value.toDoubleOrNull() ?: 12.7)

        val primaryStock = _rawStocks.value.firstOrNull() ?: StockBoard(name = "Stock", lengthMm = 2000.0, widthMm = 200.0, thicknessMm = 20.0)

        return CutlistExportHelper.exportToExcelCsv(
            context = context,
            projectName = _activeProjectName.value,
            materialType = primaryStock.type,
            stockLengthMm = primaryStock.lengthMm,
            stockWidthMm = primaryStock.widthMm,
            stockThicknessMm = primaryStock.thicknessMm,
            bladeKerfMm = kerfMm,
            trimMarginMm = trimMm,
            requestedCuts = _requestedCuts.value,
            optimizationResult = _optimizationResult.value,
            projectNotes = _projectNotes.value
        )
    }

    fun exportMarkdownNoteToDB(context: Context) {
        val unit = _dimensionUnit.value
        val kerfMm = unit.toMm(_bladeKerf.value.toDoubleOrNull() ?: 3.175)
        val trimMm = unit.toMm(_trimMargin.value.toDoubleOrNull() ?: 12.7)
        val primaryStock = _rawStocks.value.firstOrNull() ?: StockBoard(name = "Stock", lengthMm = 2000.0, widthMm = 200.0, thicknessMm = 20.0)

        val markdown = CutlistExportHelper.exportToMarkdown(
            projectName = _activeProjectName.value,
            materialType = primaryStock.type,
            stockLengthMm = primaryStock.lengthMm,
            stockWidthMm = primaryStock.widthMm,
            stockThicknessMm = primaryStock.thicknessMm,
            bladeKerfMm = kerfMm,
            trimMarginMm = trimMm,
            requestedCuts = _requestedCuts.value,
            optimizationResult = _optimizationResult.value,
            projectNotes = _projectNotes.value
        )

        val pdfUri = exportPdf(context)
        val pdfPathStr = pdfUri?.path ?: ""

        val db = com.example.data.database.AppDatabase.getInstance(context)
        val noteRepo = com.example.data.repository.NoteRepository(db.quickNoteDao(), db.syncQueueDao())

        viewModelScope.launch {
            noteRepo.addNote(
                title = "Cutlist Plan: ${_activeProjectName.value}",
                content = markdown,
                tag = "Cutlist",
                colorHex = "#2E7D32",
                imagePaths = "",
                pdfPaths = pdfPathStr,
                isMarkdown = true
            )
        }
    }
}
