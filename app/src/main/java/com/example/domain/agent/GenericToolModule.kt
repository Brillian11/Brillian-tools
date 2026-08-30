package com.example.domain.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ToolDefinition

data class GenericToolState(
    val id: String,
    val title: String,
    val arguments: Map<String, Any?> = emptyMap()
) : ToolState

class GenericToolModule(private val definition: ToolDefinition) : ToolModule {
    
    override val metadata = ToolMetadata(
        id = definition.id,
        displayName = definition.title,
        category = definition.category,
        semanticDescription = definition.description,
        keywords = definition.keywords,
        supportsInlineCanvas = false
    )

    override val schema = ToolSchema(
        name = definition.id.removePrefix("widget_").replace("[^a-zA-Z0-9_]".toRegex(), "_"),
        description = "Launch the full-screen interactive ${definition.title}: ${definition.description}.",
        parameters = listOf(
            ToolParameter("notes", ParameterType.STRING, "Relevant context, text input, or pre-configured values to pass directly to the tool", false, "")
        )
    )

    override fun createInitialState(args: Map<String, Any?>): ToolState {
        return GenericToolState(
            id = definition.id,
            title = definition.title,
            arguments = args
        )
    }

    override fun getComposableView(
        state: ToolState,
        onStateChange: (ToolState) -> Unit
    ): @Composable () -> Unit = {
        val genericState = state as GenericToolState
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF232323)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Launch,
                    contentDescription = "Full-Screen Mode Required",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${genericState.title} Launcher",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "This premium module runs high-performance rendering, specialized hardware sensors, or advanced civil/structural lookups. Tap below to launch full-screen with pre-filled inputs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                if (genericState.arguments.isNotEmpty() && genericState.arguments["notes"]?.toString()?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Pre-filled: \"${genericState.arguments["notes"]}\"",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
