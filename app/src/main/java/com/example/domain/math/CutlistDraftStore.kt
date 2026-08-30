package com.example.domain.math

data class DraftPiece(
    val label: String,
    val lengthMm: Double,
    val widthMm: Double,
    val thicknessMm: Double,
    val quantity: Int
)

object CutlistDraftStore {
    var projectName: String = ""
    var dimensions: String = ""
    var pendingDraft: List<DraftPiece>? = null
    var notes: String = ""
    var hasNewDraft: Boolean = false

    fun setDraft(project: String, dims: String, pieces: List<DraftPiece>, projectNotes: String = "") {
        projectName = project
        dimensions = dims
        pendingDraft = pieces
        notes = projectNotes
        hasNewDraft = true
    }

    fun clearDraft() {
        pendingDraft = null
        notes = ""
        hasNewDraft = false
    }

    fun getSavedProjectsSummary(context: android.content.Context): String {
        val prefs = context.getSharedPreferences("brillian_cutlist_projects_pref", android.content.Context.MODE_PRIVATE)
        val jsonStr = prefs.getString("saved_projects_v3", null) ?: prefs.getString("saved_projects_v2", null)
        val sb = StringBuilder()

        if (!jsonStr.isNullOrBlank()) {
            try {
                val array = org.json.JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val name = obj.optString("name", "Project ${i + 1}")
                    val cutsArray = obj.optJSONArray("cuts")
                    val cutsList = mutableListOf<String>()
                    if (cutsArray != null) {
                        for (j in 0 until cutsArray.length()) {
                            val c = cutsArray.getJSONObject(j)
                            val label = c.optString("label", "Piece")
                            val len = c.optDouble("lengthMm", 0.0)
                            val wid = c.optDouble("widthMm", 0.0)
                            val thick = c.optDouble("thicknessMm", 20.0)
                            val qty = c.optInt("quantity", 1)
                            cutsList.add("${qty}x $label (${len.toInt()}x${wid.toInt()}x${thick.toInt()} mm)")
                        }
                    }
                    val notesStr = obj.optString("projectNotes", "")
                    sb.append("- Project \"$name\": Cuts = [${cutsList.joinToString("; ")}], Non-cut Notes = [$notesStr]\n")
                }
            } catch (e: Exception) {
                // Ignore parse error
            }
        }

        if (projectName.isNotBlank() && pendingDraft != null) {
            val draftCuts = pendingDraft?.joinToString("; ") { "${it.quantity}x ${it.label} (${it.lengthMm.toInt()}x${it.widthMm.toInt()}x${it.thicknessMm.toInt()} mm)" } ?: ""
            sb.append("- Active Draft \"$projectName\" ($dimensions): Cuts = [$draftCuts], Notes = [$notes]\n")
        }

        return if (sb.isNotEmpty()) sb.toString() else "No saved cutlist projects currently."
    }
}
