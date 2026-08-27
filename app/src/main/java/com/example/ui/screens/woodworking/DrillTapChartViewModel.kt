package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ThreadStandard(val label: String) {
    UNC_UNF("Imperial UNC / UNF"),
    METRIC_ISO("Metric ISO (mm)"),
    NUMBER_LETTER("Number (#) & Letter Bits"),
    NPT_PIPE("NPT Pipe Taps")
}

data class TapDrillEntry(
    val threadSize: String,
    val majorDiameterInches: Double,
    val pitchOrTpi: String,
    val standard: ThreadStandard,
    val tapDrillImperial: String,
    val tapDrillDecimalInches: Double,
    val tapDrillMetricMm: Double,
    val closeClearanceDrill: String,
    val freeClearanceDrill: String,
    val notes: String = "75% Thread Depth"
)

class DrillTapChartViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    val allEntries = listOf(
        // UNC / UNF
        TapDrillEntry("#0-80 UNF", 0.0600, "80 TPI", ThreadStandard.UNC_UNF, "#56 (3/64\")", 0.0465, 1.18, "#52 (0.0635\")", "#50 (0.0700\")"),
        TapDrillEntry("#2-56 UNC", 0.0860, "56 TPI", ThreadStandard.UNC_UNF, "#50", 0.0700, 1.78, "#43 (0.0890\")", "#41 (0.0960\")"),
        TapDrillEntry("#4-40 UNC", 0.1120, "40 TPI", ThreadStandard.UNC_UNF, "#43", 0.0890, 2.26, "#32 (0.1160\")", "#30 (0.1285\")"),
        TapDrillEntry("#6-32 UNC", 0.1380, "32 TPI", ThreadStandard.UNC_UNF, "#36", 0.1065, 2.70, "#27 (0.1440\")", "#25 (0.1495\")"),
        TapDrillEntry("#8-32 UNC", 0.1640, "32 TPI", ThreadStandard.UNC_UNF, "#29", 0.1360, 3.45, "#18 (0.1695\")", "#16 (0.1770\")"),
        TapDrillEntry("#10-24 UNC", 0.1900, "24 TPI", ThreadStandard.UNC_UNF, "#25", 0.1495, 3.80, "#9 (0.1960\")", "#7 (0.2010\")"),
        TapDrillEntry("#10-32 UNF", 0.1900, "32 TPI", ThreadStandard.UNC_UNF, "#21", 0.1590, 4.04, "#9 (0.1960\")", "#7 (0.2010\")"),
        TapDrillEntry("1/4\"-20 UNC", 0.2500, "20 TPI", ThreadStandard.UNC_UNF, "#7 (13/64\")", 0.2010, 5.10, "F (0.2570\")", "H (0.2660\")"),
        TapDrillEntry("1/4\"-28 UNF", 0.2500, "28 TPI", ThreadStandard.UNC_UNF, "#3 (7/32\")", 0.2130, 5.40, "F (0.2570\")", "H (0.2660\")"),
        TapDrillEntry("5/16\"-18 UNC", 0.3125, "18 TPI", ThreadStandard.UNC_UNF, "F (17/64\")", 0.2570, 6.53, "P (0.3230\")", "Q (0.3320\")"),
        TapDrillEntry("5/16\"-24 UNF", 0.3125, "24 TPI", ThreadStandard.UNC_UNF, "I", 0.2720, 6.90, "P (0.3230\")", "Q (0.3320\")"),
        TapDrillEntry("3/8\"-16 UNC", 0.3750, "16 TPI", ThreadStandard.UNC_UNF, "5/16\"", 0.3125, 7.94, "W (0.3860\")", "X (0.3970\")"),
        TapDrillEntry("3/8\"-24 UNF", 0.3750, "24 TPI", ThreadStandard.UNC_UNF, "Q (21/64\")", 0.3320, 8.43, "W (0.3860\")", "X (0.3970\")"),
        TapDrillEntry("7/16\"-14 UNC", 0.4375, "14 TPI", ThreadStandard.UNC_UNF, "U (23/64\")", 0.3680, 9.35, "29/64\"", "15/32\""),
        TapDrillEntry("1/2\"-13 UNC", 0.5000, "13 TPI", ThreadStandard.UNC_UNF, "27/64\"", 0.4219, 10.72, "33/64\"", "17/32\""),
        TapDrillEntry("1/2\"-20 UNF", 0.5000, "20 TPI", ThreadStandard.UNC_UNF, "29/64\"", 0.4531, 11.51, "33/64\"", "17/32\""),
        TapDrillEntry("5/8\"-11 UNC", 0.6250, "11 TPI", ThreadStandard.UNC_UNF, "17/32\"", 0.5312, 13.49, "41/64\"", "21/32\""),
        TapDrillEntry("3/4\"-10 UNC", 0.7500, "10 TPI", ThreadStandard.UNC_UNF, "21/32\"", 0.6562, 16.67, "49/64\"", "25/32\""),
        TapDrillEntry("1\"-8 UNC", 1.0000, "8 TPI", ThreadStandard.UNC_UNF, "7/8\"", 0.8750, 22.23, "1-1/64\"", "1-1/16\""),

        // Metric ISO
        TapDrillEntry("M2 × 0.4", 0.0787, "0.4 mm", ThreadStandard.METRIC_ISO, "1.60 mm (#52)", 0.0630, 1.60, "2.20 mm", "2.40 mm"),
        TapDrillEntry("M2.5 × 0.45", 0.0984, "0.45 mm", ThreadStandard.METRIC_ISO, "2.05 mm (#46)", 0.0807, 2.05, "2.70 mm", "2.90 mm"),
        TapDrillEntry("M3 × 0.5", 0.1181, "0.5 mm", ThreadStandard.METRIC_ISO, "2.50 mm (#39)", 0.0984, 2.50, "3.20 mm", "3.40 mm"),
        TapDrillEntry("M4 × 0.7", 0.1575, "0.7 mm", ThreadStandard.METRIC_ISO, "3.30 mm (#30)", 0.1299, 3.30, "4.30 mm", "4.50 mm"),
        TapDrillEntry("M5 × 0.8", 0.1969, "0.8 mm", ThreadStandard.METRIC_ISO, "4.20 mm (#19)", 0.1654, 4.20, "5.30 mm", "5.50 mm"),
        TapDrillEntry("M6 × 1.0", 0.2362, "1.0 mm", ThreadStandard.METRIC_ISO, "5.00 mm (#9)", 0.1969, 5.00, "6.40 mm", "6.60 mm"),
        TapDrillEntry("M8 × 1.25", 0.3150, "1.25 mm", ThreadStandard.METRIC_ISO, "6.80 mm (H)", 0.2677, 6.80, "8.40 mm", "9.00 mm"),
        TapDrillEntry("M10 × 1.5", 0.3937, "1.5 mm", ThreadStandard.METRIC_ISO, "8.50 mm (Q)", 0.3346, 8.50, "10.50 mm", "11.00 mm"),
        TapDrillEntry("M12 × 1.75", 0.4724, "1.75 mm", ThreadStandard.METRIC_ISO, "10.20 mm (13/32\")", 0.4016, 10.20, "13.00 mm", "13.50 mm"),
        TapDrillEntry("M14 × 2.0", 0.5512, "2.0 mm", ThreadStandard.METRIC_ISO, "12.00 mm (15/32\")", 0.4724, 12.00, "15.00 mm", "15.50 mm"),
        TapDrillEntry("M16 × 2.0", 0.6299, "2.0 mm", ThreadStandard.METRIC_ISO, "14.00 mm (35/64\")", 0.5512, 14.00, "17.00 mm", "17.50 mm"),
        TapDrillEntry("M20 × 2.5", 0.7874, "2.5 mm", ThreadStandard.METRIC_ISO, "17.50 mm (11/16\")", 0.6890, 17.50, "21.00 mm", "22.00 mm"),

        // Number & Letter Drill Gauges
        TapDrillEntry("#1 Drill Bit", 0.2280, "Wire Gauge", ThreadStandard.NUMBER_LETTER, "#1 (0.2280\")", 0.2280, 5.79, "-", "-"),
        TapDrillEntry("#7 Drill Bit", 0.2010, "Wire Gauge", ThreadStandard.NUMBER_LETTER, "#7 (0.2010\")", 0.2010, 5.11, "-", "-", "Standard 1/4\"-20 Tap Drill"),
        TapDrillEntry("#21 Drill Bit", 0.1590, "Wire Gauge", ThreadStandard.NUMBER_LETTER, "#21 (0.1590\")", 0.1590, 4.04, "-", "-", "Standard #10-32 Tap Drill"),
        TapDrillEntry("#29 Drill Bit", 0.1360, "Wire Gauge", ThreadStandard.NUMBER_LETTER, "#29 (0.1360\")", 0.1360, 3.45, "-", "-", "Standard #8-32 Tap Drill"),
        TapDrillEntry("#36 Drill Bit", 0.1065, "Wire Gauge", ThreadStandard.NUMBER_LETTER, "#36 (0.1065\")", 0.1065, 2.70, "-", "-", "Standard #6-32 Tap Drill"),
        TapDrillEntry("#43 Drill Bit", 0.0890, "Wire Gauge", ThreadStandard.NUMBER_LETTER, "#43 (0.0890\")", 0.0890, 2.26, "-", "-", "Standard #4-40 Tap Drill"),
        TapDrillEntry("Letter A Bit", 0.2340, "Letter Gauge", ThreadStandard.NUMBER_LETTER, "A (0.2340\")", 0.2340, 5.94, "-", "-"),
        TapDrillEntry("Letter F Bit", 0.2570, "Letter Gauge", ThreadStandard.NUMBER_LETTER, "F (0.2570\")", 0.2570, 6.53, "-", "-", "5/16\"-18 Tap Drill / 1/4\" Clearance"),
        TapDrillEntry("Letter Q Bit", 0.3320, "Letter Gauge", ThreadStandard.NUMBER_LETTER, "Q (0.3320\")", 0.3320, 8.43, "-", "-", "3/8\"-24 Tap Drill"),
        TapDrillEntry("Letter Z Bit", 0.4130, "Letter Gauge", ThreadStandard.NUMBER_LETTER, "Z (0.4130\")", 0.4130, 10.49, "-", "-"),

        // NPT Pipe Taps
        TapDrillEntry("1/8\"-27 NPT", 0.4050, "27 TPI", ThreadStandard.NPT_PIPE, "R (11/32\" without reamer)", 0.3390, 8.61, "7/16\"", "15/32\"", "Tapered Pipe Thread"),
        TapDrillEntry("1/4\"-18 NPT", 0.5400, "18 TPI", ThreadStandard.NPT_PIPE, "7/16\" (0.4375\")", 0.4375, 11.11, "9/16\"", "19/32\"", "Tapered Pipe Thread"),
        TapDrillEntry("3/8\"-18 NPT", 0.6750, "18 TPI", ThreadStandard.NPT_PIPE, "37/64\" (0.5781\")", 0.5781, 14.68, "11/16\"", "23/32\"", "Tapered Pipe Thread"),
        TapDrillEntry("1/2\"-14 NPT", 0.8400, "14 TPI", ThreadStandard.NPT_PIPE, "23/32\" (0.7188\")", 0.7188, 18.26, "7/8\"", "29/32\"", "Tapered Pipe Thread"),
        TapDrillEntry("3/4\"-14 NPT", 1.0500, "14 TPI", ThreadStandard.NPT_PIPE, "15/16\" (0.9375\")", 0.9375, 23.81, "1-3/32\"", "1-1/8\"", "Tapered Pipe Thread"),
        TapDrillEntry("1\"-11.5 NPT", 1.3150, "11.5 TPI", ThreadStandard.NPT_PIPE, "1-5/32\" (1.1562\")", 1.1562, 29.37, "1-3/8\"", "1-13/32\"", "Tapered Pipe Thread")
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStandard = MutableStateFlow<ThreadStandard?>(null) // null = all
    val selectedStandard: StateFlow<ThreadStandard?> = _selectedStandard.asStateFlow()

    private val _filteredEntries = MutableStateFlow(allEntries)
    val filteredEntries: StateFlow<List<TapDrillEntry>> = _filteredEntries.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
        filterList()
    }

    fun setStandardFilter(std: ThreadStandard?) {
        _selectedStandard.value = std
        filterList()
    }

    private fun filterList() {
        val q = _searchQuery.value.trim().lowercase()
        val std = _selectedStandard.value

        _filteredEntries.value = allEntries.filter { entry ->
            val matchStd = (std == null || entry.standard == std)
            val matchQuery = q.isEmpty() ||
                    entry.threadSize.lowercase().contains(q) ||
                    entry.tapDrillImperial.lowercase().contains(q) ||
                    entry.pitchOrTpi.lowercase().contains(q) ||
                    String.format("%.4f", entry.tapDrillDecimalInches).contains(q) ||
                    String.format("%.2f", entry.tapDrillMetricMm).contains(q)
            matchStd && matchQuery
        }
    }

    fun saveReferenceLookupLog(entry: TapDrillEntry) {
        viewModelScope.launch {
            toolLogRepository?.logToolActivity(
                toolType = "widget_drill_tap_chart",
                title = "Drill & Tap Lookup: ${entry.threadSize}",
                summary = "Tap Drill: ${entry.tapDrillImperial} (${String.format("%.4f\"", entry.tapDrillDecimalInches)} / ${String.format("%.2f mm", entry.tapDrillMetricMm)}), Clearance Close: ${entry.closeClearanceDrill}, Free: ${entry.freeClearanceDrill}",
                value = entry.tapDrillDecimalInches
            )
            _lastLogSaved.value = true
        }
    }
}
