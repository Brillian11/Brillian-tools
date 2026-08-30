with open("app/src/main/java/com/example/ui/screens/ai/BrillianAiAssistant.kt", "r") as f:
    content = f.read()

# Fix Icons
content = content.replace(
    "androidx.compose.material.icons.filled.KeyboardArrowUp",
    "androidx.compose.material.icons.Icons.Default.KeyboardArrowUp"
).replace(
    "androidx.compose.material.icons.filled.KeyboardArrowDown",
    "androidx.compose.material.icons.Icons.Default.KeyboardArrowDown"
)

# Move currentOnlineModel
declaration = r'                                    var currentOnlineModel by remember { mutableStateOf\(prefs\.getString\("online_model_\$aiProvider", if \(aiProvider == "DeepSeek"\) "deepseek-chat" else "gemini-2\.0-flash"\) \?: "gemini-2\.0-flash"\) \}\n'

if declaration in content:
    content = content.replace(declaration, "")
    
    target = "    val lazyListState = rememberLazyListState()\n"
    replacement = target + """
    var currentOnlineModel by remember { mutableStateOf(prefs.getString("online_model_$aiProvider", if (aiProvider == "DeepSeek") "deepseek-chat" else "gemini-2.0-flash") ?: "gemini-2.0-flash") }
"""
    content = content.replace(target, replacement)

# Add imports for KeyboardArrowUp and KeyboardArrowDown
imports_target = "import androidx.compose.material.icons.Icons\n"
imports_replacement = imports_target + """import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
"""
if "import androidx.compose.material.icons.filled.KeyboardArrowUp" not in content:
    content = content.replace(imports_target, imports_replacement)

# Also fix the onlineModelList to have gemini-2.0-flash instead of gemini-3.5-flash as default, or whatever it is, wait, let's leave it as is. Oh, actually let's fix it because they said "remove placeholder ai model".
content = content.replace('listOf("gemini-3.5-flash", "gemini-1.5-pro", "gemini-2.0-flash")', 'listOf("gemini-2.0-flash", "gemini-1.5-pro", "gemini-1.5-flash")')

with open("app/src/main/java/com/example/ui/screens/ai/BrillianAiAssistant.kt", "w") as f:
    f.write(content)
