package com.example.ui.screens.sensors

import androidx.lifecycle.ViewModel
import com.example.domain.math.FractionalNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FractionalCalcResult(
    val formattedFraction: String,
    val decimalInches: Double,
    val millimeters: Double,
    val centimeters: Double
)

class FractionalCalculatorViewModel : ViewModel() {

    private val _inputA = MutableStateFlow("15 3/64")
    val inputA: StateFlow<String> = _inputA.asStateFlow()

    private val _inputB = MutableStateFlow("3 1/8")
    val inputB: StateFlow<String> = _inputB.asStateFlow()

    private val _calcResult = MutableStateFlow(calculate("15 3/64", "3 1/8", "+"))
    val calcResult: StateFlow<FractionalCalcResult> = _calcResult.asStateFlow()

    fun updateInputA(value: String) {
        _inputA.value = value
        recalculate("+")
    }

    fun updateInputB(value: String) {
        _inputB.value = value
        recalculate("+")
    }

    fun executeOperation(op: String) {
        _calcResult.value = calculate(_inputA.value, _inputB.value, op)
    }

    private fun recalculate(op: String) {
        _calcResult.value = calculate(_inputA.value, _inputB.value, op)
    }

    private fun calculate(aStr: String, bStr: String, op: String): FractionalCalcResult {
        val numA = FractionalNumber.parse(aStr)
        val numB = FractionalNumber.parse(bStr)

        val resultNum = when (op) {
            "+" -> numA + numB
            "-" -> numA - numB
            "*" -> numA * numB
            "/" -> if (numB.numerator == 0L) numA else numA / numB
            else -> numA + numB
        }

        return FractionalCalcResult(
            formattedFraction = resultNum.toFractionString(),
            decimalInches = resultNum.decimalValue,
            millimeters = resultNum.mmValue,
            centimeters = resultNum.mmValue / 10.0
        )
    }
}
