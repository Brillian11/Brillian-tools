package com.example.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Carpenter
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChangeHistory
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Foundation
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    viewModel: UnitConverterViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header & Category Filter Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Unit Category",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                )

                if (state.category == ConversionCategory.CURRENCY) {
                    Surface(
                        color = if (state.isCurrencyLive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { viewModel.fetchLatestCurrencyRates() }
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Currency",
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (state.isCurrencyLive) "Live Rates" else "Fetch Rates",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Category Chips Horizontal Scrollable Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(ConversionCategory.entries.toTypedArray()) { cat ->
                    val isSelected = state.category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(cat) },
                        leadingIcon = {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                        },
                        label = {
                            Text(
                                text = cat.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("converter_cat_${cat.name}")
                    )
                }
            }

            // Input Value & Unit Selection Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Input Display
                    OutlinedTextField(
                        value = state.inputValue,
                        onValueChange = { viewModel.setInputValue(it) },
                        label = { Text("Quantity / Value", fontSize = 10.sp) },
                        textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("converter_input_field")
                    )

                    // From / To Dropdowns & Swap Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        var fromExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = fromExpanded,
                            onExpandedChange = { fromExpanded = !fromExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = state.category.units.getOrElse(state.fromUnitIndex) { "" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("From Unit", fontSize = 9.sp) },
                                textStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .testTag("from_unit_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = fromExpanded,
                                onDismissRequest = { fromExpanded = false }
                            ) {
                                state.category.units.forEachIndexed { index, unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit, fontSize = 11.sp) },
                                        onClick = {
                                            viewModel.setFromUnitIndex(index)
                                            fromExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.swapUnits() },
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .testTag("swap_units_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Swap Units",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        var toExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = toExpanded,
                            onExpandedChange = { toExpanded = !toExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = state.category.units.getOrElse(state.toUnitIndex) { "" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("To Unit", fontSize = 9.sp) },
                                textStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .testTag("to_unit_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = toExpanded,
                                onDismissRequest = { toExpanded = false }
                            ) {
                                state.category.units.forEachIndexed { index, unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit, fontSize = 11.sp) },
                                        onClick = {
                                            viewModel.setToUnitIndex(index)
                                            toExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Converted Result Display Hero Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CONVERTED RESULT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = state.result,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1
                        )
                        if (state.extraDescription.isNotEmpty()) {
                            Text(
                                text = state.extraDescription,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                maxLines = 1
                            )
                        }
                    }

                    Text(
                        text = state.category.units.getOrElse(state.toUnitIndex) { "" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // FULL HEIGHT WEIGHTED NUMERIC KEYPAD GRID (Fills remaining height flush to touch navbar)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val numpadKeys = listOf(
                    listOf("7", "8", "9", "C"),
                    listOf("4", "5", "6", "⌫"),
                    listOf("1", "2", "3", "±"),
                    listOf("0", ".", "00", "⇄")
                )

                numpadKeys.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        row.forEach { key ->
                            val isClear = key == "C"
                            val isAction = key in listOf("⌫", "±", "⇄")
                            val isNumber = key in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "00")

                            val bgColor = when {
                                isClear -> MaterialTheme.colorScheme.errorContainer
                                isAction -> MaterialTheme.colorScheme.secondaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val textColor = when {
                                isClear -> MaterialTheme.colorScheme.onErrorContainer
                                isAction -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = bgColor),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        if (key == "⇄") {
                                            viewModel.swapUnits()
                                        } else {
                                            viewModel.onNumpadPress(key)
                                        }
                                    }
                                    .testTag("numpad_btn_$key")
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "⌫") {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = textColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else if (key == "⇄") {
                                        Icon(
                                            imageVector = Icons.Default.SwapHoriz,
                                            contentDescription = "Swap",
                                            tint = textColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            fontSize = 15.sp,
                                            fontWeight = if (isNumber) FontWeight.Bold else FontWeight.SemiBold,
                                            color = textColor
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

private fun getCategoryIcon(category: ConversionCategory): ImageVector {
    return when (category) {
        ConversionCategory.CURRENCY -> Icons.Default.AttachMoney
        ConversionCategory.VOLUME -> Icons.Default.ViewInAr
        ConversionCategory.LENGTH -> Icons.Default.Straighten
        ConversionCategory.MASS -> Icons.Default.Scale
        ConversionCategory.TEMPERATURE -> Icons.Default.Thermostat
        ConversionCategory.ENERGY -> Icons.Default.LocalFireDepartment
        ConversionCategory.AREA -> Icons.Default.GridOn
        ConversionCategory.SPEED -> Icons.Default.DirectionsRun
        ConversionCategory.TIME -> Icons.Default.Schedule
        ConversionCategory.POWER -> Icons.Default.ElectricBolt
        ConversionCategory.DATA_NETWORK -> Icons.Default.Dns
        ConversionCategory.PRESSURE -> Icons.Default.Speed
        ConversionCategory.ANGLE -> Icons.Default.ChangeHistory
        ConversionCategory.ELECTRICIAN -> Icons.Default.Bolt
        ConversionCategory.WOODWORKER -> Icons.Default.Carpenter
        ConversionCategory.ARCHITECT_ENGINEER -> Icons.Default.Architecture
        ConversionCategory.STONE_MASONRY -> Icons.Default.Foundation
        ConversionCategory.GROUND_EARTHWORK -> Icons.Default.Terrain
        ConversionCategory.SATELLITE_RF -> Icons.Default.SatelliteAlt
    }
}
