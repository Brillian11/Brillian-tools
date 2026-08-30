sed -i 's/suspend fun generateOnlineGeminiInference(/suspend fun generateOnlineGeminiInference(\n    modelName: String,/g' app/src/main/java/com/example/ui/screens/ai/BrillianAiAssistant.kt
sed -i 's/suspend fun generateOnlineDeepSeekInference(/suspend fun generateOnlineDeepSeekInference(\n    modelName: String,/g' app/src/main/java/com/example/ui/screens/ai/BrillianAiAssistant.kt
