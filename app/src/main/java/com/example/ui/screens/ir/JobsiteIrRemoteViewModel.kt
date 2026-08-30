package com.example.ui.screens.ir

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import com.example.ir.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JobsiteIrUiState(
    val isHardwareSupported: Boolean = false,
    val carrierRanges: List<Pair<Int, Int>> = emptyList(),
    val selectedCategory: IrTradeCategory = IrTradeCategory.HVAC,
    val selectedProfileId: String = "hvac_daikin",
    val availableProfiles: List<IrDeviceProfile> = emptyList(),

    // Live Transmission Feedback
    val lastTransmission: IrTransmissionEvent? = null,
    val transmissionHistory: List<IrTransmissionEvent> = emptyList(),
    val isTransmittingFlash: Boolean = false,

    // Active Macro Execution
    val runningMacroId: String? = null,
    val macroStepIndex: Int = 0,
    val macroStepTitle: String = "",
    val isMacroRunning: Boolean = false,

    // Manual / Custom Signal Terminal
    val customCarrierFreqHz: Int = 38000,
    val customHexInput: String = "0000 006D 0022 0002 0155 00AA 0015 0015 0015 0040 0015 0015",
    val customRawPatternInput: String = "9000, 4500, 560, 560, 560, 1690, 560, 560, 560, 1690",
    val customTerminalMode: String = "HEX", // "HEX" or "RAW"
    val terminalStatusMessage: String = ""
)

class JobsiteIrRemoteViewModel(
    application: Application,
    private val toolLogRepository: ToolLogRepository
) : AndroidViewModel(application) {

    private val irController = JobsiteIrController(application.applicationContext)
    private var macroJob: Job? = null

    private val _uiState = MutableStateFlow(
        JobsiteIrUiState(
            isHardwareSupported = irController.isHardwareSupported(),
            carrierRanges = irController.getCarrierFrequencyRanges(),
            availableProfiles = IrJobsiteDatabase.deviceProfiles
        )
    )
    val uiState: StateFlow<JobsiteIrUiState> = _uiState.asStateFlow()

    init {
        val defaultProfile = IrJobsiteDatabase.deviceProfiles.firstOrNull { it.category == IrTradeCategory.HVAC }
        if (defaultProfile != null) {
            _uiState.value = _uiState.value.copy(selectedProfileId = defaultProfile.id)
        }
    }

    fun selectCategory(category: IrTradeCategory) {
        val firstMatching = IrJobsiteDatabase.deviceProfiles.firstOrNull { it.category == category }
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            selectedProfileId = firstMatching?.id ?: _uiState.value.selectedProfileId
        )
    }

    fun selectProfile(profileId: String) {
        val profile = IrJobsiteDatabase.deviceProfiles.firstOrNull { it.id == profileId }
        if (profile != null) {
            _uiState.value = _uiState.value.copy(
                selectedProfileId = profileId,
                selectedCategory = profile.category
            )
        }
    }

    fun transmitCommand(command: IrCommand) {
        val currentProfile = getCurrentProfile() ?: return
        val event = irController.transmit(
            title = command.title,
            brand = currentProfile.brand,
            category = currentProfile.category.title,
            frequencyHz = command.frequencyHz,
            patternUs = command.timingPattern,
            hexSignature = command.hexSignature
        )

        recordTransmission(event)
    }

    fun runMacro(macro: IrMacroDefinition) {
        if (_uiState.value.isMacroRunning) return
        val currentProfile = getCurrentProfile() ?: return

        macroJob?.cancel()
        macroJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isMacroRunning = true,
                runningMacroId = macro.id,
                macroStepIndex = 0,
                macroStepTitle = "Starting ${macro.title}..."
            )

            irController.executeMacro(
                macroName = macro.title,
                brand = currentProfile.brand,
                category = currentProfile.category.title,
                steps = macro.steps
            ) { stepIndex, stepTitle, event ->
                _uiState.value = _uiState.value.copy(
                    macroStepIndex = stepIndex + 1,
                    macroStepTitle = "Step ${stepIndex + 1}/${macro.steps.size}: $stepTitle"
                )
                recordTransmission(event)
            }

            _uiState.value = _uiState.value.copy(
                isMacroRunning = false,
                runningMacroId = null,
                macroStepTitle = "Completed ${macro.title}"
            )
        }
    }

    fun stopMacro() {
        macroJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isMacroRunning = false,
            runningMacroId = null,
            macroStepTitle = "Macro cancelled"
        )
    }

    fun setCustomCarrierFreq(freq: Int) {
        _uiState.value = _uiState.value.copy(customCarrierFreqHz = freq)
    }

    fun setCustomHexInput(hex: String) {
        _uiState.value = _uiState.value.copy(customHexInput = hex)
    }

    fun setCustomRawPatternInput(raw: String) {
        _uiState.value = _uiState.value.copy(customRawPatternInput = raw)
    }

    fun setCustomTerminalMode(mode: String) {
        _uiState.value = _uiState.value.copy(customTerminalMode = mode)
    }

    fun transmitCustomTerminalSignal() {
        val state = _uiState.value
        if (state.customTerminalMode == "HEX") {
            val parsed = IrProtocolEncoder.parseProntoHex(state.customHexInput)
            if (parsed != null) {
                val (freq, pattern) = parsed
                val event = irController.transmit(
                    title = "Custom Pronto HEX Signal",
                    brand = "Custom Manual",
                    category = "Direct Terminal",
                    frequencyHz = freq,
                    patternUs = pattern,
                    hexSignature = "PRONTO-${pattern.size}p"
                )
                recordTransmission(event)
                _uiState.value = _uiState.value.copy(
                    terminalStatusMessage = "Transmitted Pronto HEX (${pattern.size} pulses @ ${freq}Hz)"
                )
            } else {
                // Try 32-bit hex (e.g. 0x20DF10EF)
                val cleanHex = state.customHexInput.trim().removePrefix("0x")
                try {
                    val hexVal = cleanHex.toLong(16).toInt()
                    val addr = (hexVal shr 16) and 0xFFFF
                    val cmd = hexVal and 0xFF
                    val pattern = IrProtocolEncoder.encodeNec(addr, cmd)
                    val event = irController.transmit(
                        title = "Custom NEC Hex (0x${cleanHex.uppercase()})",
                        brand = "Custom Manual",
                        category = "Direct Terminal",
                        frequencyHz = state.customCarrierFreqHz,
                        patternUs = pattern,
                        hexSignature = "0x${cleanHex.uppercase()}"
                    )
                    recordTransmission(event)
                    _uiState.value = _uiState.value.copy(
                        terminalStatusMessage = "Transmitted NEC 32-bit Code (Addr 0x${addr.toString(16).uppercase()}, Cmd 0x${cmd.toString(16).uppercase()})"
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        terminalStatusMessage = "Invalid HEX format. Use Pronto format ('0000 006D...') or 32-bit hex ('0x00FF12ED')"
                    )
                }
            }
        } else {
            // RAW Microseconds array: "9000, 4500, 560, 560, 560, 1690"
            try {
                val pattern = state.customRawPatternInput
                    .split("[,\\s]+".toRegex())
                    .filter { it.isNotBlank() }
                    .mapNotNull { it.trim().toIntOrNull() }
                    .toIntArray()

                if (pattern.isNotEmpty()) {
                    val event = irController.transmit(
                        title = "Custom Raw Pulse Train",
                        brand = "Custom Manual",
                        category = "Direct Terminal",
                        frequencyHz = state.customCarrierFreqHz,
                        patternUs = pattern,
                        hexSignature = "RAW-${pattern.size}p"
                    )
                    recordTransmission(event)
                    _uiState.value = _uiState.value.copy(
                        terminalStatusMessage = "Transmitted Raw Pattern (${pattern.size} timing intervals, ${(pattern.sum() / 1000.0).toInt()}ms)"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        terminalStatusMessage = "Pattern is empty. Enter comma-separated microsecond values."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    terminalStatusMessage = "Invalid pattern numbers. Example: 9000, 4500, 560, 1690"
                )
            }
        }
    }

    private fun recordTransmission(event: IrTransmissionEvent) {
        val history = (_uiState.value.transmissionHistory.take(19) + event)
        _uiState.value = _uiState.value.copy(
            lastTransmission = event,
            transmissionHistory = history,
            isTransmittingFlash = true
        )

        // Log to repository
        viewModelScope.launch {
            val status = if (event.isHardwareTransmitted) "IR Hardware Transmitted" else "Optical Pulse Simulated"
            toolLogRepository.logToolActivity(
                toolType = "widget_jobsite_ir_remote",
                title = "Jobsite IR: ${event.title}",
                summary = "${event.brand} [${event.category}] @ ${event.frequencyHz / 1000}kHz (${event.pulseCount} pulses, ${event.totalDurationUs / 1000}ms) -> $status",
                value = event.pulseCount.toDouble()
            )
        }
    }

    fun getCurrentProfile(): IrDeviceProfile? {
        val s = _uiState.value
        return s.availableProfiles.firstOrNull { it.id == s.selectedProfileId }
            ?: s.availableProfiles.firstOrNull { it.category == s.selectedCategory }
    }
}
