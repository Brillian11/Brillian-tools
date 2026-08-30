package com.example.domain.agent

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PendingAiSession(
    val title: String,
    val initialPrompt: String,
    val autoSend: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

object AiSessionBridge {
    private val _pendingSession = MutableStateFlow<PendingAiSession?>(null)
    val pendingSession: StateFlow<PendingAiSession?> = _pendingSession.asStateFlow()

    fun startFreshSessionWithPrompt(title: String, prompt: String, autoSend: Boolean = true) {
        _pendingSession.value = PendingAiSession(
            title = title,
            initialPrompt = prompt,
            autoSend = autoSend
        )
    }

    fun consumePendingSession(): PendingAiSession? {
        val current = _pendingSession.value
        if (current != null) {
            _pendingSession.value = null
            return current
        }
        return null
    }

    fun hasPendingSession(): Boolean = _pendingSession.value != null
}
