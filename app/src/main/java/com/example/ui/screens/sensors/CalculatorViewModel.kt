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

    private val _isScientificMode = MutableStateFlow(false)
    val isScientificMode: StateFlow<Boolean> = _isScientificMode.asStateFlow()

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

    fun evaluate() {
        val expr = _input.value
        if (expr.isBlank()) return
        try {
            val evalResult = parseAndEvaluate(expr)
            val formattedResult = if (evalResult.isNaN()) {
                "Error: Not a Number"
            } else if (evalResult.isInfinite()) {
                "Error: Infinite"
            } else if (evalResult % 1.0 == 0.0) {
                evalResult.toLong().toString()
            } else {
                String.format("%.6f", evalResult).trimEnd('0').trimEnd('.')
            }
            _result.value = formattedResult
            _history.value = listOf("$expr = $formattedResult") + _history.value

            // Log this calculation inside Quick Notes automatically!
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
        return ExpressionParser(str).parse()
    }

    private class ExpressionParser(val str: String) {
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
            if (pos < str.length) throw RuntimeException("Unexpected character near position $pos: ${ch.toChar()}")
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
                if (eat('*'.code)) x *= parseFactor()
                else if (eat('/'.code)) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor
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
            } else if (ch >= 'a'.code && ch <= 'z'.code) {
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = str.substring(startPos, pos)
                if (func == "pi") {
                    x = Math.PI
                } else if (func == "e") {
                    x = Math.E
                } else {
                    x = parseFactor()
                    x = when (func) {
                        "sqrt" -> sqrt(x)
                        "sin" -> sin(Math.toRadians(x))
                        "cos" -> cos(Math.toRadians(x))
                        "tan" -> tan(Math.toRadians(x))
                        "log" -> log10(x)
                        "ln" -> ln(x)
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                }
            } else {
                throw RuntimeException("Unexpected character: ${ch.toChar()}")
            }

            if (eat('^'.code)) x = x.pow(parseFactor())

            return x
        }
    }
}
