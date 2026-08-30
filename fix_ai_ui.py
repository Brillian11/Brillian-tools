import re

with open("app/src/main/java/com/example/ui/screens/ai/BrillianAiAssistant.kt", "r") as f:
    content = f.read()

# 1. Add var showScreenContext
content = content.replace("                        // Chat Copilot Tab\n                        Column(modifier = Modifier.fillMaxSize()) {", "                        // Chat Copilot Tab\n                        var showScreenContext by remember { mutableStateOf(false) }\n                        Column(modifier = Modifier.fillMaxSize()) {")

# 2. Extract Context Banner and Model Selection Bar
pattern_context_banner = r"(                            // Context Notification Banner\n                            Box\(\n                                modifier = Modifier\n                                    \.fillMaxWidth\(\)\n                                    \.background\(MaterialTheme\.colorScheme\.secondaryContainer\.copy\(alpha = 0\.6f\)\)\n                                    \.padding\(horizontal = 16\.dp, vertical = 8\.dp\)\n                            \) \{.*?\n                            \})\n"
pattern_model_bar = r"(                            // Model & Provider Selection Bar\n                            Surface\(\n                                modifier = Modifier\.fillMaxWidth\(\),\n                                color = MaterialTheme\.colorScheme\.surfaceVariant\.copy\(alpha = 0\.5f\),\n                                tonalElevation = 1\.dp\n                            \) \{.*?\n                            \})\n"

context_banner_match = re.search(pattern_context_banner, content, flags=re.DOTALL)
model_bar_match = re.search(pattern_model_bar, content, flags=re.DOTALL)

if context_banner_match and model_bar_match:
    context_banner_str = context_banner_match.group(1)
    
    # We will remove the original Context Banner
    content = content.replace(context_banner_str + "\n", "")
    
    # We will modify the Model Selection Bar to include the dropdown arrow and toggle
    # Replace the Row(verticalAlignment = Alignment.CenterVertically) { Icon(...) Spacer(...) Text("Active LLM:") }
    
    search_row = r"(Row\(verticalAlignment = Alignment\.CenterVertically\) \{\n\s*Icon\(\n\s*imageVector = Icons\.Default\.Memory,.*?\n\s*\)\n\s*Spacer\(modifier = Modifier\.width\(6\.dp\)\)\n\s*Text\(\n\s*text = \"Active LLM:\",\n\s*style = MaterialTheme\.typography\.labelMedium\.copy\(fontWeight = FontWeight\.Bold\),\n\s*color = MaterialTheme\.colorScheme\.onSurface\n\s*\)\n\s*\})"
    
    replacement_row = r"""Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { showScreenContext = !showScreenContext }.padding(end = 4.dp, top = 4.dp, bottom = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Memory,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Active LLM:",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = if (showScreenContext) androidx.compose.material.icons.filled.KeyboardArrowUp else androidx.compose.material.icons.filled.KeyboardArrowDown,
                                            contentDescription = "Toggle Context",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }"""
    
    content = re.sub(search_row, replacement_row, content, flags=re.DOTALL)
    
    # Put the Context Banner below the Model Selection Bar, wrapped in AnimatedVisibility
    wrapped_context_banner = f"""
                            androidx.compose.animation.AnimatedVisibility(visible = showScreenContext) {{
{context_banner_str}
                            }}
"""
    
    # We need to insert wrapped_context_banner right after the Surface closing bracket of Model Selection Bar.
    # The Surface ends where we matched it, so we can just replace the model_bar matched string with (modified_model_bar + wrapped_context_banner)
    
    # Wait, re.sub already modified content with replacement_row. We just need to insert wrapped_context_banner after the Surface closing brace.
    # Let's find the end of Model & Provider Selection Bar.
    # Since we can't reliably find it with regex after modification, we can find the exact text:
    
    # Actually, we can just find `// Chat History List` which comes right after.
    content = content.replace("                            // Chat History List", wrapped_context_banner + "                            // Chat History List")

with open("app/src/main/java/com/example/ui/screens/ai/BrillianAiAssistant.kt", "w") as f:
    f.write(content)
