                        // Chat Copilot Tab
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Context Notification Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Active Screen Context: ${toolContext.title}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Trade RAG Active",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Model & Provider Selection Bar
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                tonalElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    }

                                    // Parse downloaded models list with fallback
                                    val downloadedListString = prefs.getString("downloaded_models_list", "") ?: ""
                                    val downloadedIds = if (downloadedListString.isBlank()) {
                                        val modelId = if (!downloadedModelId.isNullOrEmpty()) downloadedModelId!! else "smollm2_360m"
                                        prefs.edit().putString("downloaded_model", modelId).putString("downloaded_models_list", modelId).apply()
                                        if (downloadedModelId.isNullOrEmpty()) downloadedModelId = modelId
                                        listOf(modelId)
                                    } else {
                                        downloadedListString.split(",").filter { it.isNotBlank() }
                                    }
                                    val allModels = getModelsList()
                                    var downloadedModels = allModels.filter { downloadedIds.contains(it.id) || it.id == downloadedModelId }

                                    if (downloadedModels.isEmpty()) {
                                        prefs.edit().putString("downloaded_model", "smollm2_360m").putString("downloaded_models_list", "smollm2_360m").apply()
                                        downloadedModelId = "smollm2_360m"
                                        downloadedModels = allModels.filter { it.id == "smollm2_360m" }
                                    }

                                    var showModelDropdown by remember { mutableStateOf(false) }
                                    var currentOnlineModel by remember { mutableStateOf(prefs.getString("online_model_$aiProvider", if (aiProvider == "DeepSeek") "deepseek-chat" else "gemini-2.0-flash") ?: "gemini-2.0-flash") }

                                    val activeModelName = if (isOnline) {
                                        currentOnlineModel
                                    } else {
                                        val activeModel = allModels.find { it.id == downloadedModelId } ?: downloadedModels.first()
                                        activeModel.name.split(" ")[0] + " " + (if (activeModel.id == "qwen25_15b") "1.5B" else if (activeModel.id == "smollm2_360m") "360M" else "2B")
                                    }

                                    Box {
                                        OutlinedButton(
                                            onClick = { showModelDropdown = true },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp).testTag("select_model_dropdown_button")
