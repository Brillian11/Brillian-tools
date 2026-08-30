package com.example.domain.agent

import androidx.compose.runtime.Composable

interface ToolState

enum class ParameterType {
    STRING, NUMBER, BOOLEAN, ENUM
}

data class ToolParameter(
    val name: String,
    val type: ParameterType,
    val description: String,
    val isRequired: Boolean,
    val defaultValue: Any? = null
)

data class ToolSchema(
    val name: String,
    val description: String,
    val parameters: List<ToolParameter>
)

data class ToolMetadata(
    val id: String,
    val displayName: String,
    val category: String,
    val semanticDescription: String,
    val keywords: List<String>,
    val supportsInlineCanvas: Boolean
)

interface ToolModule {
    val metadata: ToolMetadata
    val schema: ToolSchema
    fun createInitialState(args: Map<String, Any?>): ToolState
    fun getComposableView(state: ToolState, onStateChange: (ToolState) -> Unit): @Composable () -> Unit
}
