package com.example.domain.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

data class DynamicToolState(
    val toolId: String,
    val title: String,
    val parameterValues: Map<String, Double>
) : ToolState

class DynamicToolModule(
    val definition: DynamicToolDefinition
) : ToolModule {

    override val metadata = ToolMetadata(
        id = definition.id,
        displayName = definition.title,
        category = definition.category,
        semanticDescription = definition.description,
        keywords = emptyList(),
        supportsInlineCanvas = true
    )

    override val schema = ToolSchema(
        name = definition.id.removePrefix("widget_").replace("[^a-zA-Z0-9_]".toRegex(), "_"),
        description = "Calculate values for ${definition.title}. Description: ${definition.description}.",
        parameters = definition.parameters.map { param ->
            ToolParameter(
                name = param.key,
                type = ParameterType.NUMBER,
                description = param.label + " (${param.unit})",
                isRequired = true,
                defaultValue = param.defaultValue
            )
        }
    )

    override fun createInitialState(args: Map<String, Any?>): ToolState {
        // Initialize parameter values from passed AI arguments, falling back to default values
        val initialValues = mutableMapOf<String, Double>()
        for (param in definition.parameters) {
            val aiVal = args[param.key]
            val doubleVal = when (aiVal) {
                is Number -> aiVal.toDouble()
                is String -> aiVal.toDoubleOrNull() ?: param.defaultValue
                else -> param.defaultValue
            }
            // Clamp value within bounds
            initialValues[param.key] = doubleVal.coerceIn(param.min, param.max)
        }
        return DynamicToolState(
            toolId = definition.id,
            title = definition.title,
            parameterValues = initialValues
        )
    }

    override fun getComposableView(
        state: ToolState,
        onStateChange: (ToolState) -> Unit
    ): @Composable () -> Unit = {
        val toolState = state as DynamicToolState
        
        // Local calculation results based on current sliding values
        val currentResults = remember(toolState.parameterValues) {
            definition.calculate(toolState.parameterValues)
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header
                Text(
                    text = toolState.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = definition.description,
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Divider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(bottom = 12.dp))

                // Interactive Sliders Section
                Text(
                    text = "ADJUST PARAMETERS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                toolState.parameterValues.forEach { (key, value) ->
                    val spec = definition.parameters.firstOrNull { it.key == key }
                    if (spec != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = spec.label,
                                    fontSize = 11.sp,
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${if (spec.step >= 1.0) value.roundToInt() else String.format("%.2f", value)} ${spec.unit}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Slider(
                                value = value.toFloat(),
                                onValueChange = { newVal ->
                                    val roundedVal = (newVal / spec.step).roundToInt() * spec.step
                                    val clampedVal = roundedVal.toDouble().coerceIn(spec.min, spec.max)
                                    val newParams = toolState.parameterValues.toMutableMap().apply {
                                        this[key] = clampedVal
                                    }
                                    onStateChange(toolState.copy(parameterValues = newParams))
                                },
                                valueRange = spec.min.toFloat()..spec.max.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    inactiveTrackColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Results output
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE2E8F0), shape = RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "LIVE CALCULATED OUTCOMES",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                        currentResults.forEach { res ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = res.label,
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.Normal
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = res.value,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    if (res.unit.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = res.unit,
                                            fontSize = 9.sp,
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
