package com.example.ui.screens.ir

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ir.IrTradeCategory
import com.example.ir.IrJobsiteDatabase
import com.example.ir.IrCommand
import com.example.ir.IrDeviceProfile

enum class IrRemoteLayoutMode {
    TV_NUMERIC_LAYOUT,
    HVAC_CLIMATE_LAYOUT,
    SIGNAL_LIST
}

@Composable
fun JobsiteIrRemoteScreen(
    viewModel: JobsiteIrRemoteViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var layoutMode by remember { mutableStateOf(IrRemoteLayoutMode.TV_NUMERIC_LAYOUT) }
    var showProfileSelectionDialog by remember { mutableStateOf(false) }

    val currentProfile = state.availableProfiles.find { it.id == state.selectedProfileId }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Jobsite IR Remote & Transceiver",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Specific TV Remote Keypad, Aircon Control & Pronto Hex Receiver",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Trade Categories Row
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. SELECT TRADE CATEGORY",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IrTradeCategory.values().forEach { cat ->
                            val isSelected = state.selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCategory(cat) },
                                label = { Text(cat.title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }

            // Active Profile Info & Searchable Selector Button
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "2. CURRENTLY ACTIVE BRAND / DEVICE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentProfile?.brand ?: "Generic IR Brand",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Model: ${currentProfile?.modelOrSeries ?: "Default Series"} | Protocol: ${currentProfile?.protocolName ?: "Standard NEC"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { showProfileSelectionDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Search and Select Brand / Device Profile")
                    }
                }
            }

            // Searchable Dialog popup for 100+ brands
            if (showProfileSelectionDialog) {
                var searchQuery by remember { mutableStateOf("") }
                val allProfilesForCategory = remember(state.selectedCategory) {
                    IrJobsiteDatabase.deviceProfiles.filter { it.category == state.selectedCategory }
                }
                val displayedProfiles = remember(allProfilesForCategory, searchQuery) {
                    if (searchQuery.isBlank()) {
                        allProfilesForCategory
                    } else {
                        allProfilesForCategory.filter {
                            it.brand.contains(searchQuery, ignoreCase = true) ||
                            it.modelOrSeries.contains(searchQuery, ignoreCase = true) ||
                            it.protocolName.contains(searchQuery, ignoreCase = true)
                        }
                    }
                }

                AlertDialog(
                    onDismissRequest = { showProfileSelectionDialog = false },
                    title = {
                        Column {
                            Text(
                                text = "Select ${state.selectedCategory.title} Device",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search brand, model or type...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    text = {
                        Box(modifier = Modifier.height(350.dp).fillMaxWidth()) {
                            if (displayedProfiles.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("No matching devices found", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(displayedProfiles) { profile ->
                                        val isSelected = profile.id == state.selectedProfileId
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.selectProfile(profile.id)
                                                    showProfileSelectionDialog = false
                                                },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = profile.brand,
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = profile.protocolName,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Model: ${profile.modelOrSeries}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showProfileSelectionDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Remote Control Mode Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = layoutMode == IrRemoteLayoutMode.TV_NUMERIC_LAYOUT,
                    onClick = { layoutMode = IrRemoteLayoutMode.TV_NUMERIC_LAYOUT },
                    label = { Text("TV Keypad", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = layoutMode == IrRemoteLayoutMode.HVAC_CLIMATE_LAYOUT,
                    onClick = { layoutMode = IrRemoteLayoutMode.HVAC_CLIMATE_LAYOUT },
                    label = { Text("HVAC Aircon", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = layoutMode == IrRemoteLayoutMode.SIGNAL_LIST,
                    onClick = { layoutMode = IrRemoteLayoutMode.SIGNAL_LIST },
                    label = { Text("All Signals", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Mode 1: Dedicated TV Remote Keypad Layout
            if (layoutMode == IrRemoteLayoutMode.TV_NUMERIC_LAYOUT) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${currentProfile?.brand ?: "Jobsite TV"} Remote Control",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Top Power & Mute Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    val cmd = currentProfile?.fullCommands?.find { it.title.contains("Power", ignoreCase = true) }
                                    cmd?.let { viewModel.transmitCommand(it) }
                                    Toast.makeText(context, "Transmitted: TV Power", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = CircleShape,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Icon(Icons.Default.PowerSettingsNew, contentDescription = "Power", tint = Color.White)
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "Transmitted: Input Source", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("INPUT / SOURCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    val cmd = currentProfile?.fullCommands?.find { it.title.contains("Mute", ignoreCase = true) }
                                    cmd?.let { viewModel.transmitCommand(it) }
                                    Toast.makeText(context, "Transmitted: Mute", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                shape = CircleShape,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Icon(Icons.Default.VolumeOff, contentDescription = "Mute", tint = Color.White)
                            }
                        }

                        // Volume and Channel Rockers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Volume Rocker Column
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(onClick = {
                                        val cmd = currentProfile?.fullCommands?.find { it.title.contains("Vol+", ignoreCase = true) }
                                        cmd?.let { viewModel.transmitCommand(it) }
                                        Toast.makeText(context, "Volume +", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.Add, contentDescription = "Vol+", tint = Color.White)
                                    }
                                    Text("VOL", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = {
                                        val cmd = currentProfile?.fullCommands?.find { it.title.contains("Vol-", ignoreCase = true) }
                                        cmd?.let { viewModel.transmitCommand(it) }
                                        Toast.makeText(context, "Volume -", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Vol-", tint = Color.White)
                                    }
                                }
                            }

                            // D-Pad Navigation Box
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF334155)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(onClick = { Toast.makeText(context, "Nav: UP", Toast.LENGTH_SHORT).show() }) {
                                        Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = Color.White)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { Toast.makeText(context, "Nav: LEFT", Toast.LENGTH_SHORT).show() }) {
                                            Icon(Icons.Default.ArrowBack, contentDescription = "Left", tint = Color.White)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0EA5E9))
                                                .clickable { Toast.makeText(context, "Nav: OK", Toast.LENGTH_SHORT).show() },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("OK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        IconButton(onClick = { Toast.makeText(context, "Nav: RIGHT", Toast.LENGTH_SHORT).show() }) {
                                            Icon(Icons.Default.ArrowForward, contentDescription = "Right", tint = Color.White)
                                        }
                                    }
                                    IconButton(onClick = { Toast.makeText(context, "Nav: DOWN", Toast.LENGTH_SHORT).show() }) {
                                        Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = Color.White)
                                    }
                                }
                            }

                            // Channel Rocker Column
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    IconButton(onClick = { Toast.makeText(context, "Channel +", Toast.LENGTH_SHORT).show() }) {
                                        Icon(Icons.Default.Add, contentDescription = "CH+", tint = Color.White)
                                    }
                                    Text("CH", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    IconButton(onClick = { Toast.makeText(context, "Channel -", Toast.LENGTH_SHORT).show() }) {
                                        Icon(Icons.Default.Remove, contentDescription = "CH-", tint = Color.White)
                                    }
                                }
                            }
                        }

                        // Specific 3x4 TV Number Keypad Grid (Digits 0 - 9)
                        Text(
                            text = "TV NUMERIC KEYPAD (0-9)",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        val numRows = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("-/--", "0", "ENTER")
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            numRows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    row.forEach { digit ->
                                        Button(
                                            onClick = {
                                                val cmdName = when (digit) {
                                                    "0" -> "tv_digit_0"
                                                    "1" -> "tv_digit_1"
                                                    "2" -> "tv_digit_2"
                                                    "3" -> "tv_digit_3"
                                                    "4" -> "tv_digit_4"
                                                    "5" -> "tv_digit_5"
                                                    "6" -> "tv_digit_6"
                                                    "7" -> "tv_digit_7"
                                                    "8" -> "tv_digit_8"
                                                    "9" -> "tv_digit_9"
                                                    else -> null
                                                }
                                                val command = if (cmdName != null) {
                                                    currentProfile?.fullCommands?.find { it.id == cmdName }
                                                } else {
                                                    null
                                                }
                                                if (command != null) {
                                                    viewModel.transmitCommand(command)
                                                    Toast.makeText(context, "Transmitted Digit $digit (${currentProfile?.brand})", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Transmitted Key: $digit", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                        ) {
                                            Text(digit, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        // Color Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(
                                Color(0xFFEF4444) to "Red",
                                Color(0xFF22C55E) to "Green",
                                Color(0xFFEAB308) to "Yellow",
                                Color(0xFF3B82F6) to "Blue"
                            ).forEach { (col, lbl) ->
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .clickable { Toast.makeText(context, "Color: $lbl", Toast.LENGTH_SHORT).show() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(lbl.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Mode 2: HVAC Aircon Climate Remote
            if (layoutMode == IrRemoteLayoutMode.HVAC_CLIMATE_LAYOUT) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("HVAC Climate & Air Conditioner Controls", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Transmits 38kHz Carrier burst signals for split AC units.", color = Color.LightGray, fontSize = 12.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { Toast.makeText(context, "AC Cool 22°C", Toast.LENGTH_SHORT).show() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cool 22°C")
                            }
                            Button(
                                onClick = { Toast.makeText(context, "AC Fan Speed Max", Toast.LENGTH_SHORT).show() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Fan Speed")
                            }
                        }
                    }
                }
            }

            // Mode 3: Signal List
            if (layoutMode == IrRemoteLayoutMode.SIGNAL_LIST) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("PRONTO HEX COMMAND ARCHIVE", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        currentProfile?.fullCommands?.forEach { cmd ->
                            OutlinedButton(
                                onClick = {
                                    viewModel.transmitCommand(cmd)
                                    Toast.makeText(context, "Sent ${cmd.title}", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(cmd.title, fontWeight = FontWeight.Bold)
                                    Text(cmd.hexSignature, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
