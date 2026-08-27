package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class GrainPattern(val label: String, val description: String) {
    CATHEDRAL_FLAME("Cathedral Flame (Flat Sawn)", "Arching flame grain with dramatic center crown"),
    QUARTERSAWN_LINEAR("Straight Linear (Quarter Sawn)", "Tight parallel grain with medullary ray flecks"),
    RIFT_COMB("Rift Sawn (Straight Comb)", "Uniform 45-degree angled straight growth lines"),
    CURLY_FIGURE("Curly / Tiger Figure", "Wavy horizontal chatoyance waves"),
    BURL_SWIRL("Burl / Wild Swirl", "Chaotic swirling knots and bird's eye eyes")
}

enum class MatchMode(val label: String) {
    BOOK_MATCH("Book Match (Mirrored Flames)"),
    SLIP_MATCH("Slip Match (Sequential Alignment)"),
    ALTERNATING_RINGS("Anti-Cup Alternating Rings (⌒ ⌣ ⌒ ⌣)"),
    CUSTOM_FREE("Custom Manual Arrangement")
}

enum class RingOrientation(val label: String, val symbol: String) {
    BARK_UP("Bark Side Up (Crown Up)", "⌒"),
    BARK_DOWN("Heart Side Up (Crown Down)", "⌣")
}

data class TimberBoard(
    val id: String,
    val label: String,
    val widthInches: Double,
    val lengthInches: Double,
    val grainPattern: GrainPattern,
    val ringOrientation: RingOrientation,
    val isFlippedHorizontal: Boolean = false,
    val isRotated180: Boolean = false,
    val woodToneHex: Long = 0xFFB45309 // Walnut default
)

class GrainMatchingViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    private val _boards = MutableStateFlow<List<TimberBoard>>(
        listOf(
            TimberBoard("b1", "Plank 1 (Left Flitch)", 6.5, 48.0, GrainPattern.CATHEDRAL_FLAME, RingOrientation.BARK_UP, false, false, 0xFF92400E),
            TimberBoard("b2", "Plank 2 (Book Center-L)", 7.0, 48.0, GrainPattern.CATHEDRAL_FLAME, RingOrientation.BARK_DOWN, false, false, 0xFFB45309),
            TimberBoard("b3", "Plank 3 (Book Center-R)", 7.0, 48.0, GrainPattern.CATHEDRAL_FLAME, RingOrientation.BARK_UP, true, false, 0xFFB45309),
            TimberBoard("b4", "Plank 4 (Right Flitch)", 6.5, 48.0, GrainPattern.QUARTERSAWN_LINEAR, RingOrientation.BARK_DOWN, false, false, 0xFF92400E)
        )
    )
    val boards: StateFlow<List<TimberBoard>> = _boards.asStateFlow()

    private val _matchMode = MutableStateFlow(MatchMode.BOOK_MATCH)
    val matchMode: StateFlow<MatchMode> = _matchMode.asStateFlow()

    private val _selectedBoardId = MutableStateFlow("b2")
    val selectedBoardId: StateFlow<String> = _selectedBoardId.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    fun selectBoard(id: String) {
        _selectedBoardId.value = id
    }

    fun applyPresetMatchMode(mode: MatchMode) {
        _matchMode.value = mode
        val list = _boards.value.toMutableList()
        when (mode) {
            MatchMode.BOOK_MATCH -> {
                if (list.size >= 2) {
                    list[0] = list[0].copy(isFlippedHorizontal = false, ringOrientation = RingOrientation.BARK_UP)
                    list[1] = list[1].copy(isFlippedHorizontal = true, ringOrientation = RingOrientation.BARK_UP)
                    if (list.size >= 4) {
                        list[2] = list[2].copy(isFlippedHorizontal = false, ringOrientation = RingOrientation.BARK_UP)
                        list[3] = list[3].copy(isFlippedHorizontal = true, ringOrientation = RingOrientation.BARK_UP)
                    }
                }
            }
            MatchMode.SLIP_MATCH -> {
                list.forEachIndexed { i, b ->
                    list[i] = b.copy(isFlippedHorizontal = false, isRotated180 = false)
                }
            }
            MatchMode.ALTERNATING_RINGS -> {
                list.forEachIndexed { i, b ->
                    list[i] = b.copy(ringOrientation = if (i % 2 == 0) RingOrientation.BARK_UP else RingOrientation.BARK_DOWN)
                }
            }
            MatchMode.CUSTOM_FREE -> {}
        }
        _boards.value = list
        _lastLogSaved.value = false
    }

    fun flipHorizontal(boardId: String) {
        _boards.value = _boards.value.map {
            if (it.id == boardId) it.copy(isFlippedHorizontal = !it.isFlippedHorizontal) else it
        }
        _lastLogSaved.value = false
    }

    fun rotate180(boardId: String) {
        _boards.value = _boards.value.map {
            if (it.id == boardId) it.copy(isRotated180 = !it.isRotated180) else it
        }
        _lastLogSaved.value = false
    }

    fun toggleRingOrientation(boardId: String) {
        _boards.value = _boards.value.map {
            if (it.id == boardId) {
                val next = if (it.ringOrientation == RingOrientation.BARK_UP) RingOrientation.BARK_DOWN else RingOrientation.BARK_UP
                it.copy(ringOrientation = next)
            } else it
        }
        _lastLogSaved.value = false
    }

    fun moveBoardLeft(index: Int) {
        if (index > 0 && index < _boards.value.size) {
            val list = _boards.value.toMutableList()
            val item = list.removeAt(index)
            list.add(index - 1, item)
            _boards.value = list
            _lastLogSaved.value = false
        }
    }

    fun moveBoardRight(index: Int) {
        if (index >= 0 && index < _boards.value.size - 1) {
            val list = _boards.value.toMutableList()
            val item = list.removeAt(index)
            list.add(index + 1, item)
            _boards.value = list
            _lastLogSaved.value = false
        }
    }

    fun addPlank() {
        val list = _boards.value.toMutableList()
        val nextNum = list.size + 1
        val newBoard = TimberBoard(
            id = "b_${System.currentTimeMillis()}",
            label = "Plank $nextNum",
            widthInches = 6.0,
            lengthInches = 48.0,
            grainPattern = GrainPattern.CATHEDRAL_FLAME,
            ringOrientation = if (nextNum % 2 == 0) RingOrientation.BARK_DOWN else RingOrientation.BARK_UP,
            woodToneHex = 0xFFB45309
        )
        list.add(newBoard)
        _boards.value = list
        _lastLogSaved.value = false
    }

    fun removePlank(id: String) {
        if (_boards.value.size > 1) {
            _boards.value = _boards.value.filter { it.id != id }
            _lastLogSaved.value = false
        }
    }

    fun updateSelectedBoard(
        width: Double,
        pattern: GrainPattern
    ) {
        _boards.value = _boards.value.map {
            if (it.id == _selectedBoardId.value) {
                it.copy(widthInches = width.coerceAtLeast(1.0), grainPattern = pattern)
            } else it
        }
        _lastLogSaved.value = false
    }

    fun getTotalPanelWidth(): Double = _boards.value.sumOf { it.widthInches }
    fun getPanelLength(): Double = _boards.value.maxOfOrNull { it.lengthInches } ?: 48.0
    fun getPanelAreaSqFt(): Double = (getTotalPanelWidth() * getPanelLength()) / 144.0
    fun getBoardFootage(thicknessInches: Double = 1.0): Double = (getTotalPanelWidth() * getPanelLength() * thicknessInches) / 144.0

    fun saveGrainLayoutLog(projectTitle: String = "Walnut Dining Table Top") {
        viewModelScope.launch {
            val totalW = getTotalPanelWidth()
            val len = getPanelLength()
            val count = _boards.value.size
            val area = getPanelAreaSqFt()

            toolLogRepository?.logToolActivity(
                toolType = "widget_grain_matching",
                title = "Grain Layout: $projectTitle ($count Planks, ${String.format("%.1f\"", totalW)} Wide)",
                summary = "Dimensions: ${String.format("%.1f\" x %.1f\"", totalW, len)}, Area: ${String.format("%.2f sq ft", area)}, Mode: ${_matchMode.value.label}, Planks: ${_boards.value.joinToString { it.ringOrientation.symbol }}",
                value = totalW
            )
            _lastLogSaved.value = true
        }
    }
}
