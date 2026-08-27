package com.example.ui.screens.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerMode(val title: String, val durationSeconds: Int) {
    POMODORO("Focus Work", 25 * 60),
    SHORT_BREAK("Short Break", 5 * 60),
    LONG_BREAK("Long Break", 15 * 60)
}

data class TimerUiState(
    val mode: TimerMode = TimerMode.POMODORO,
    val timeLeftSeconds: Int = TimerMode.POMODORO.durationSeconds,
    val isRunning: Boolean = false,
    val completedSessions: Int = 0
)

class FocusTimerViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun selectMode(mode: TimerMode) {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            mode = mode,
            timeLeftSeconds = mode.durationSeconds,
            isRunning = false
        )
    }

    fun toggleStartPause() {
        if (_uiState.value.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _uiState.value = _uiState.value.copy(isRunning = true)
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeftSeconds > 0 && _uiState.value.isRunning) {
                delay(1000L)
                val current = _uiState.value.timeLeftSeconds - 1
                _uiState.value = _uiState.value.copy(timeLeftSeconds = current)
            }

            if (_uiState.value.timeLeftSeconds <= 0) {
                onTimerCompleted()
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun resetTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            timeLeftSeconds = _uiState.value.mode.durationSeconds,
            isRunning = false
        )
    }

    private fun onTimerCompleted() {
        timerJob?.cancel()
        val sessions = _uiState.value.completedSessions + 1
        _uiState.value = _uiState.value.copy(
            isRunning = false,
            completedSessions = sessions,
            timeLeftSeconds = _uiState.value.mode.durationSeconds
        )

        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "TIMER",
                title = "Focus Session Completed",
                summary = "Completed ${_uiState.value.mode.title} (${_uiState.value.mode.durationSeconds / 60}m)",
                value = sessions.toDouble()
            )
        }
    }
}
