package com.example.domain.agent

import com.example.domain.model.ToolDefinition

class ToolRegistry(
    private val vectorSearchEngine: LocalVectorSearchEngine = LocalVectorSearchEngine()
) {
    private val allToolsMap = mutableMapOf<String, ToolModule>()

    init {
        // Build the active tool registry by wrapping every single tool in the 109-tool suite
        for (def in ToolDefinition.ALL_TOOLS) {
            val dynamicDef = DynamicToolEngine.getOrFallback(def.id, def)
            allToolsMap[def.id] = DynamicToolModule(dynamicDef)
        }
        // Register the new interactive matrix solver
        val matrixDef = DynamicToolEngine.DYNAMIC_TOOLS["widget_matrix_solver"]!!
        allToolsMap[matrixDef.id] = DynamicToolModule(matrixDef)
    }

    suspend fun indexAllTools() {
        val documents = allToolsMap.values.map { tool ->
            VectorDocument(
                id = tool.metadata.id,
                text = "${tool.metadata.displayName}. Category: ${tool.metadata.category}. ${tool.metadata.semanticDescription}. Keywords: ${tool.metadata.keywords.joinToString()}"
            )
        }
        vectorSearchEngine.indexDocuments(documents)
    }

    suspend fun findRelevantTools(userQuery: String, topK: Int = 4): List<ToolModule> {
        val matchingIds = vectorSearchEngine.search(userQuery, limit = topK)
        
        // Return matching tools, maintaining search ranking order
        return matchingIds.mapNotNull { id -> allToolsMap[id] }
    }

    fun getToolById(id: String): ToolModule? {
        return allToolsMap[id]
    }

    fun getAllIndexedTools(): List<ToolModule> {
        return allToolsMap.values.toList()
    }
}
