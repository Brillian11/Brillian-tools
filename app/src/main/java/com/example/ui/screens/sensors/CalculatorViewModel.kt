package com.example.ui.screens.sensors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.*

class CalculatorViewModel(private val toolLogRepository: ToolLogRepository) : ViewModel() {

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _isScientificMode = MutableStateFlow(true)
    val isScientificMode: StateFlow<Boolean> = _isScientificMode.asStateFlow()

    private val _is2ndMode = MutableStateFlow(false)
    val is2ndMode: StateFlow<Boolean> = _is2ndMode.asStateFlow()

    private val _isDegMode = MutableStateFlow(true)
    val isDegMode: StateFlow<Boolean> = _isDegMode.asStateFlow()

    private val _isFEFormat = MutableStateFlow(false)
    val isFEFormat: StateFlow<Boolean> = _isFEFormat.asStateFlow()

    private val _memoryValue = MutableStateFlow<Double?>(null)
    val memoryValue: StateFlow<Double?> = _memoryValue.asStateFlow()

    private val _showTrigMenu = MutableStateFlow(false)
    val showTrigMenu: StateFlow<Boolean> = _showTrigMenu.asStateFlow()

    private val _showFuncMenu = MutableStateFlow(false)
    val showFuncMenu: StateFlow<Boolean> = _showFuncMenu.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    fun append(value: String) {
        _input.value += value
    }

    fun clear() {
        _input.value = ""
        _result.value = ""
    }

    fun backspace() {
        val current = _input.value
        if (current.isNotEmpty()) {
            _input.value = current.substring(0, current.length - 1)
        }
    }

    fun toggleMode() {
        _isScientificMode.value = !_isScientificMode.value
    }

    fun toggle2nd() {
        _is2ndMode.value = !_is2ndMode.value
    }

    fun toggleDegRad() {
        _isDegMode.value = !_isDegMode.value
    }

    fun toggleFE() {
        _isFEFormat.value = !_isFEFormat.value
        if (_result.value.isNotEmpty()) {
            evaluate()
        }
    }

    fun toggleTrigMenu() {
        _showTrigMenu.value = !_showTrigMenu.value
        if (_showTrigMenu.value) _showFuncMenu.value = false
    }

    fun toggleFuncMenu() {
        _showFuncMenu.value = !_showFuncMenu.value
        if (_showFuncMenu.value) _showTrigMenu.value = false
    }

    fun togglePlusMinus() {
        val current = _input.value
        if (current.isEmpty()) {
            _input.value = "-"
        } else if (current.startsWith("-")) {
            _input.value = current.substring(1)
        } else {
            _input.value = "-$current"
        }
    }

    // Memory operations
    fun memoryClear() {
        _memoryValue.value = null
    }

    fun memoryRecall() {
        _memoryValue.value?.let { mem ->
            val formatted = if (mem % 1.0 == 0.0) mem.toLong().toString() else mem.toString()
            _input.value += formatted
        }
    }

    fun memoryAdd() {
        val currentVal = getCurrentVal()
        _memoryValue.value = (_memoryValue.value ?: 0.0) + currentVal
    }

    fun memorySubtract() {
        val currentVal = getCurrentVal()
        _memoryValue.value = (_memoryValue.value ?: 0.0) - currentVal
    }

    fun memoryStore() {
        _memoryValue.value = getCurrentVal()
    }

    private fun getCurrentVal(): Double {
        val res = _result.value
        if (res.isNotEmpty() && !res.startsWith("Error")) {
            return res.toDoubleOrNull() ?: 0.0
        }
        val expr = _input.value
        if (expr.isNotEmpty()) {
            return try {
                parseAndEvaluate(expr)
            } catch (e: Exception) {
                0.0
            }
        }
        return 0.0
    }

    fun evaluate() {
        val expr = _input.value
        if (expr.isBlank()) return
        try {
            val evalResult = parseAndEvaluate(expr)
            val formattedResult = if (evalResult.isNaN()) {
                "Error: NaN"
            } else if (evalResult.isInfinite()) {
                "Error: Infinite"
            } else if (_isFEFormat.value) {
                String.format("%.6e", evalResult)
            } else if (evalResult % 1.0 == 0.0) {
                evalResult.toLong().toString()
            } else {
                String.format("%.8f", evalResult).trimEnd('0').trimEnd('.')
            }
            _result.value = formattedResult
            _history.value = listOf("$expr = $formattedResult") + _history.value

            // Log calculation into Quick Notes
            viewModelScope.launch {
                toolLogRepository.logToolActivity(
                    toolType = "CALCULATOR",
                    title = "Calculation Log",
                    summary = "$expr = $formattedResult",
                    value = evalResult
                )
            }
        } catch (e: Exception) {
            _result.value = "Error: ${e.message}"
        }
    }

    private fun parseAndEvaluate(str: String): Double {
        return ExpressionParser(str, _isDegMode.value).parse()
    }

    private class ExpressionParser(val str: String, val isDegMode: Boolean) {
        var pos = -1
        var ch = 0

        fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val resultValue = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected near pos $pos: ${ch.toChar()}")
            return resultValue
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+'.code)) x += parseTerm()
                else if (eat('-'.code)) x -= parseTerm()
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*'.code) || eat('×'.code)) x *= parseFactor()
                else if (eat('/'.code) || eat('÷'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Div by 0")
                    x /= divisor
                } else if (eat('%'.code)) {
                    val divisor = parseFactor()
                    x %= divisor
                } else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code || ch == 'π'.code || ch == 'e'.code) {
                while (ch >= 'a'.code && ch <= 'z'.code || ch == 'π'.code || ch == 'e'.code) nextChar()
                val func = str.substring(startPos, pos)
                if (func == "pi" || func == "π") {
                    x = Math.PI
                } else if (func == "e") {
                    x = Math.E
                } else if (func == "rand") {
                    x = Math.random()
                } else {
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> sqrt(x)
                        "cbrt" -> cbrt(x)
                        "abs" -> abs(x)
                        "sin" -> sin(if (isDegMode) Math.toRadians(x) else x)
                        "cos" -> cos(if (isDegMode) Math.toRadians(x) else x)
                        "tan" -> tan(if (isDegMode) Math.toRadians(x) else x)
                        "asin" -> if (isDegMode) Math.toDegrees(asin(x)) else asin(x)
                        "acos" -> if (isDegMode) Math.toDegrees(acos(x)) else acos(x)
                        "atan" -> if (isDegMode) Math.toDegrees(atan(x)) else atan(x)
                        "sinh" -> sinh(x)
                        "cosh" -> cosh(x)
                        "tanh" -> tanh(x)
                        "sec" -> 1.0 / cos(if (isDegMode) Math.toRadians(x) else x)
                        "csc" -> 1.0 / sin(if (isDegMode) Math.toRadians(x) else x)
                        "cot" -> 1.0 / tan(if (isDegMode) Math.toRadians(x) else x)
                        "log" -> log10(x)
                        "ln" -> ln(x)
                        "floor" -> floor(x)
                        "ceil" -> ceil(x)
                        "fact" -> factorial(x)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                }
            } else {
                throw RuntimeException("Unexpected character: ${ch.toChar()}")
            }

            // Power operations & Factorial postfix
            while (true) {
                if (eat('^'.code)) {
                    x = x.pow(parseFactor())
                } else if (eat('!'.code)) {
                    x = factorial(x)
                } else {
                    break
                }
            }

            return x
        }

        private fun factorial(n: Double): Double {
            if (n < 0) throw ArithmeticException("Negative factorial")
            val intN = n.toInt()
            var res = 1.0
            for (i in 2..intN) {
                res *= i
            }
            return res
        }
    }
}

