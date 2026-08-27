package com.example.ui.screens.sensors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.woodworking.ResultBadge

@Composable
fun FractionalCalculatorScreen(
    viewModel: FractionalCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val inputA by viewModel.inputA.collectAsState()
    val inputB by viewModel.inputB.collectAsState()
    val calcResult by viewModel.calcResult.collectAsState()

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
            // Header
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FRACTIONAL INCH TAPE MEASURE CALCULATOR",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Woodworking Tape Arithmetic Engine",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Parses Imperial fractional measurements (e.g., '15 3/64', '3 1/8') and performs exact arithmetic with live metric conversions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            // Big Result Display Badge
            ResultBadge(
                title = "FRACTIONAL RESULT",
                value = calcResult.formattedFraction,
                unit = "${String.format("%.4f", calcResult.decimalInches)}\" = ${String.format("%.2f", calcResult.millimeters)} mm",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            // Input Fields
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tape Measurements",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = inputA,
                        onValueChange = { viewModel.updateInputA(it) },
                        label = { Text("Measurement A (e.g. 15 3/64)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_fraction_a")
                    )

                    OutlinedTextField(
                        value = inputB,
                        onValueChange = { viewModel.updateInputB(it) },
                        label = { Text("Measurement B (e.g. 3 1/8)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("input_fraction_b")
                    )

                    // Arithmetic Operator Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.executeOperation("+") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("+ Add") }

                        Button(
                            onClick = { viewModel.executeOperation("-") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("- Subtract") }

                        Button(
                            onClick = { viewModel.executeOperation("*") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("× Multiply") }

                        Button(
                            onClick = { viewModel.executeOperation("/") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) { Text("÷ Divide") }
                    }
                }
            }

            // Live Unit Conversion Table Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Converted Dimensions",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(text = "• Fractional Inches: ${calcResult.formattedFraction}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "• Decimal Inches: ${String.format("%.4f", calcResult.decimalInches)} in", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "• Millimeters: ${String.format("%.2f", calcResult.millimeters)} mm", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "• Centimeters: ${String.format("%.2f", calcResult.centimeters)} cm", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
