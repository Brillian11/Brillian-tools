package com.example.ui.screens.sensors

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val input by viewModel.input.collectAsState()
    val result by viewModel.result.collectAsState()
    val isScientific by viewModel.isScientificMode.collectAsState()
    val is2nd by viewModel.is2ndMode.collectAsState()
    val isDeg by viewModel.isDegMode.collectAsState()
    val isFE by viewModel.isFEFormat.collectAsState()
    val memoryVal by viewModel.memoryValue.collectAsState()
    val showTrig by viewModel.showTrigMenu.collectAsState()
    val showFunc by viewModel.showFuncMenu.collectAsState()
    val history by viewModel.history.collectAsState()

    var showHistoryDialog by remember { mutableStateOf(false) }

    // History Dialog
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Text(
                    text = "Calculation History Logs",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                if (history.isEmpty()) {
                    Text(
                        text = "No previous calculations logged in this session.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(history) { record ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val parts = record.split("=")
                                        if (parts.isNotEmpty()) {
                                            viewModel.append(parts[0].trim())
                                        }
                                        showHistoryDialog = false
                                    }
                            ) {
                                Text(
                                    text = record,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // Top Toolbar: Mode Switch, DEG/RAD, F-E, History Logs Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // DEG / RAD Toggle
                Surface(
                    onClick = { viewModel.toggleDegRad() },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDeg) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("deg_rad_toggle")
                ) {
                    Text(
                        text = if (isDeg) "DEG" else "RAD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDeg) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // F-E Toggle
                Surface(
                    onClick = { viewModel.toggleFE() },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isFE) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("fe_toggle")
                ) {
                    Text(
                        text = "F-E",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isFE) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // History Button
                IconButton(
                    onClick = { showHistoryDialog = true },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Calculation History",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Mode Toggle Button (Scientific vs Basic)
                Surface(
                    onClick = { viewModel.toggleMode() },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isScientific) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.testTag("calculator_mode_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isScientific) Icons.Default.Functions else Icons.Default.Calculate,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isScientific) "Scientific" else "Basic",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Formula Input & Converted Result Display Box
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = input.ifEmpty { "0" },
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calculator_formula")
                )

                Text(
                    text = result.ifEmpty { "0" },
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calculator_result")
                )
            }
        }

        if (isScientific) {
            // Memory Row: MC, MR, M+, M-, MS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val memActive = memoryVal != null
                MemoryBtn("MC", memActive, { viewModel.memoryClear() }, Modifier.weight(1f))
                MemoryBtn("MR", memActive, { viewModel.memoryRecall() }, Modifier.weight(1f))
                MemoryBtn("M+", true, { viewModel.memoryAdd() }, Modifier.weight(1f))
                MemoryBtn("M-", true, { viewModel.memorySubtract() }, Modifier.weight(1f))
                MemoryBtn("MS", true, { viewModel.memoryStore() }, Modifier.weight(1f))
            }

            // Trigonometry & Function Menus Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Trigonometry Menu Button
                Surface(
                    onClick = { viewModel.toggleTrigMenu() },
                    shape = RoundedCornerShape(6.dp),
                    color = if (showTrig) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "⊿ Trigonometry",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (showTrig) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Function Menu Button
                Surface(
                    onClick = { viewModel.toggleFuncMenu() },
                    shape = RoundedCornerShape(6.dp),
                    color = if (showFunc) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ƒ Function",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (showFunc) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Expanded Trigonometry Options Panel
            AnimatedVisibility(visible = showTrig) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val trigRow1 = listOf("sin", "cos", "tan", "asin", "acos", "atan")
                        val trigRow2 = listOf("sinh", "cosh", "tanh", "sec", "csc", "cot")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(trigRow1) { func ->
                                SciMenuChip(func) {
                                    viewModel.append("$func(")
                                    viewModel.toggleTrigMenu()
                                }
                            }
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(trigRow2) { func ->
                                SciMenuChip(func) {
                                    viewModel.append("$func(")
                                    viewModel.toggleTrigMenu()
                                }
                            }
                        }
                    }
                }
            }

            // Expanded Function Options Panel
            AnimatedVisibility(visible = showFunc) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SciMenuChip("|x| (abs)") {
                            viewModel.append("abs(")
                            viewModel.toggleFuncMenu()
                        }
                        SciMenuChip("floor") {
                            viewModel.append("floor(")
                            viewModel.toggleFuncMenu()
                        }
                        SciMenuChip("ceil") {
                            viewModel.append("ceil(")
                            viewModel.toggleFuncMenu()
                        }
                        SciMenuChip("rand") {
                            viewModel.append("rand")
                            viewModel.toggleFuncMenu()
                        }
                    }
                }
            }
        }

        // FULL HEIGHT WEIGHTED SCIENTIFIC KEYPAD GRID (Fills all remaining space to touch navbar!)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            if (isScientific) {
                // 5-Column Windows Scientific Keypad Grid (7 Rows)

                // Row 1: 2nd, π, e, C, ⌫
                KeypadRow(modifier = Modifier.weight(1f)) {
                    GridButton("2nd", isHighlight = is2nd, onClick = { viewModel.toggle2nd() }, modifier = Modifier.weight(1f))
                    GridButton("π", onClick = { viewModel.append("π") }, modifier = Modifier.weight(1f))
                    GridButton("e", onClick = { viewModel.append("e") }, modifier = Modifier.weight(1f))
                    GridButton("C", isClear = true, onClick = { viewModel.clear() }, modifier = Modifier.weight(1f))
                    GridIconButton(Icons.AutoMirrored.Filled.Backspace, onClick = { viewModel.backspace() }, modifier = Modifier.weight(1f))
                }

                // Row 2: x² (or x³), 1/x, |x|, exp, mod
                KeypadRow(modifier = Modifier.weight(1f)) {
                    GridButton(if (is2nd) "x³" else "x²", onClick = { viewModel.append(if (is2nd) "^3" else "^2") }, modifier = Modifier.weight(1f))
                    GridButton("1/x", onClick = { viewModel.append("1/(") }, modifier = Modifier.weight(1f))
                    GridButton("|x|", onClick = { viewModel.append("abs(") }, modifier = Modifier.weight(1f))
                    GridButton("exp", onClick = { viewModel.append("*10^(") }, modifier = Modifier.weight(1f))
                    GridButton("mod", isOperator = true, onClick = { viewModel.append("%") }, modifier = Modifier.weight(1f))
                }

                // Row 3: ²√x (or ³√x), (, ), n!, ÷
                KeypadRow(modifier = Modifier.weight(1f)) {
                    GridButton(if (is2nd) "³√x" else "²√x", onClick = { viewModel.append(if (is2nd) "cbrt(" else "sqrt(") }, modifier = Modifier.weight(1f))
                    GridButton("(", onClick = { viewModel.append("(") }, modifier = Modifier.weight(1f))
                    GridButton(")", onClick = { viewModel.append(")") }, modifier = Modifier.weight(1f))
                    GridButton("n!", onClick = { viewModel.append("!") }, modifier = Modifier.weight(1f))
                    GridButton("÷", isOperator = true, onClick = { viewModel.append("÷") }, modifier = Modifier.weight(1f))
                }

                // Row 4: x^y (or y√x), 7, 8, 9, ×
                KeypadRow(modifier = Modifier.weight(1f)) {
                    GridButton(if (is2nd) "y√x" else "x^y", onClick = { viewModel.append(if (is2nd) "^(1/" else "^") }, modifier = Modifier.weight(1f))
                    GridButton("7", isNumber = true, onClick = { viewModel.append("7") }, modifier = Modifier.weight(1f))
                    GridButton("8", isNumber = true, onClick = { viewModel.append("8") }, modifier = Modifier.weight(1f))
                    GridButton("9", isNumber = true, onClick = { viewModel.append("9") }, modifier = Modifier.weight(1f))
                    GridButton("×", isOperator = true, onClick = { viewModel.append("×") }, modifier = Modifier.weight(1f))
                }

                // Row 5: 10^x (or 2^x), 4, 5, 6, −
                KeypadRow(modifier = Modifier.weight(1f)) {
                    GridButton(if (is2nd) "2^x" else "10^x", onClick = { viewModel.append(if (is2nd) "2^(" else "10^(") }, modifier = Modifier.weight(1f))
                    GridButton("4", isNumber = true, onClick = { viewModel.append("4") }, modifier = Modifier.weight(1f))
                    GridButton("5", isNumber = true, onClick = { viewModel.append("5") }, modifier = Modifier.weight(1f))
                    GridButton("6", isNumber = true, onClick = { viewModel.append("6") }, modifier = Modifier.weight(1f))
                    GridButton("−", isOperator = true, onClick = { viewModel.append("-") }, modifier = Modifier.weight(1f))
                }

                // Row 6: log (or log_y), 1, 2, 3, +
                KeypadRow(modifier = Modifier.weight(1f)) {
                    GridButton(if (is2nd) "log_y" else "log", onClick = { viewModel.append("log(") }, modifier = Modifier.weight(1f))
                    GridButton("1", isNumber = true, onClick = { viewModel.append("1") }, modifier = Modifier.weight(1f))
                    GridButton("2", isNumber = true, onClick = { viewModel.append("2") }, modifier = Modifier.weight(1f))
                    GridButton("3", isNumber = true, onClick = { viewModel.append("3") }, modifier = Modifier.weight(1f))
                    GridButton("+", isOperator = true, onClick = { viewModel.append("+") }, modifier = Modifier.weight(1f))
                }

                // Row 7: ln (or e^x), +/-, 0, ., =
                KeypadRow(modifier = Modifier.weight(1f)) {
                    GridButton(if (is2nd) "e^x" else "ln", onClick = { viewModel.append(if (is2nd) "e^(" else "ln(") }, modifier = Modifier.weight(1f))
                    GridButton("+/-", onClick = { viewModel.togglePlusMinus() }, modifier = Modifier.weight(1f))
                    GridButton("0", isNumber = true, onClick = { viewModel.append("0") }, modifier = Modifier.weight(1f))
                    GridButton(".", isNumber = true, onClick = { viewModel.append(".") }, modifier = Modifier.weight(1f))
                    GridButton("=", isPrimaryEqual = true, onClick = { viewModel.evaluate() }, modifier = Modifier.weight(1f))
                }
            } else {
                // 4-Column Basic Keypad (5 Rows)
                KeypadRow(modifier = Modifier.weight(1f)) {
                    GridButton("C", isClear = true, onClick = { viewModel.clear() }, modifier = Modifier.weight(1f))
                    GridButton("(", onClick = { viewModel.append("(") }, modifier = Modifier.weight(1f))
                    GridButton(")", onClick = { viewModel.append(")") }, modifier = Modifier.weight(1f))
                    GridButton("÷", isOperator = true, onClick = { viewModel.append("÷") }, modifier = Modifier.weight(1f))
                }
                KeypadRow(modifier = Modifier.weight(1f)) {
                    GridButton("7", isNumber = true, onClick = { viewModel.append("7") }, modifier = Modifier.weight(1f))
                    GridButton("8", isNumber = true, onClick = { viewModel.append("8") }, modifier = Modifier.weight(1f))
                    GridButton("9", isNumber = true, onClick = { viewModel.append("9") }, modifier = Modifier.weight(1f))
                    GridButton("×", isOperator = true, onClick = { viewModel.append("×") }, modifier = Modifier.weight(1f))
                }
                KeypadRow(modifier = Modifier.weight(1f)) {
                    GridButton("4", isNumber = true, onClick = { viewModel.append("4") }, modifier = Modifier.weight(1f))
                    GridButton("5", isNumber = true, onClick = { viewModel.append("5") }, modifier = Modifier.weight(1f))
                    GridButton("6", isNumber = true, onClick = { viewModel.append("6") }, modifier = Modifier.weight(1f))
                    GridButton("−", isOperator = true, onClick = { viewModel.append("-") }, modifier = Modifier.weight(1f))
                }
                KeypadRow(modifier = Modifier.weight(1f)) {
                    GridButton("1", isNumber = true, onClick = { viewModel.append("1") }, modifier = Modifier.weight(1f))
                    GridButton("2", isNumber = true, onClick = { viewModel.append("2") }, modifier = Modifier.weight(1f))
                    GridButton("3", isNumber = true, onClick = { viewModel.append("3") }, modifier = Modifier.weight(1f))
                    GridButton("+", isOperator = true, onClick = { viewModel.append("+") }, modifier = Modifier.weight(1f))
                }
                KeypadRow(modifier = Modifier.weight(1f)) {
                    GridButton("+/-", onClick = { viewModel.togglePlusMinus() }, modifier = Modifier.weight(1f))
                    GridButton("0", isNumber = true, onClick = { viewModel.append("0") }, modifier = Modifier.weight(1f))
                    GridButton(".", isNumber = true, onClick = { viewModel.append(".") }, modifier = Modifier.weight(1f))
                    GridButton("=", isPrimaryEqual = true, onClick = { viewModel.evaluate() }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun KeypadRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        content = content
    )
}

@Composable
private fun GridButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isNumber: Boolean = false,
    isOperator: Boolean = false,
    isClear: Boolean = false,
    isHighlight: Boolean = false,
    isPrimaryEqual: Boolean = false
) {
    val bgColor = when {
        isPrimaryEqual -> MaterialTheme.colorScheme.primary
        isHighlight -> MaterialTheme.colorScheme.primaryContainer
        isClear -> MaterialTheme.colorScheme.errorContainer
        isOperator -> MaterialTheme.colorScheme.secondaryContainer
        isNumber -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    }

    val textColor = when {
        isPrimaryEqual -> MaterialTheme.colorScheme.onPrimary
        isHighlight -> MaterialTheme.colorScheme.onPrimaryContainer
        isClear -> MaterialTheme.colorScheme.onErrorContainer
        isOperator -> MaterialTheme.colorScheme.onSecondaryContainer
        isNumber -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() }
            .testTag("btn_calc_$label")
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = if (label.length > 3) 11.sp else 15.sp,
                fontWeight = if (isNumber || isPrimaryEqual) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GridIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() }
            .testTag("btn_calc_backspace")
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Backspace",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MemoryBtn(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun SciMenuChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

