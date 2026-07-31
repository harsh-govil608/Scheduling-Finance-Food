package com.lifeos.expensecapture.ui.calculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.ui.theme.AmountHero

/**
 * Built-in Calculator (real user review: "built-in calculator") - a plain four-function
 * calculator, standalone from Profile. Deliberately NOT wired into ManualEntryDialog's amount
 * field or any other existing flow - this is purely additive scope, not a change to how amounts
 * get entered anywhere else in the app.
 *
 * Grid layout (found via a real user report, 2026-08 - "calculator ka ui should be fixed, it is
 * not proper"): an earlier version mixed rows of 4/4/4/3/2 buttons and gave "=" a different
 * aspectRatio than its row siblings - each row independently divided its own width among however
 * many buttons it held, so buttons were different sizes across rows, and "=" was a different
 * height than 4/5/6 next to it. Every row here now holds exactly 4 equal-weight slots (spare
 * ones filled with an invisible Spacer so widths stay identical down every column), and "=" is
 * its own full-width row below the grid instead of sharing a row's height with digit buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    var display by remember { mutableStateOf("0") }
    var stored by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<Char?>(null) }
    var justEvaluated by remember { mutableStateOf(false) }

    fun inputDigit(d: String) {
        display = if (display == "0" || justEvaluated) {
            justEvaluated = false
            d
        } else {
            display + d
        }
    }

    fun inputDot() {
        if (justEvaluated) {
            display = "0."
            justEvaluated = false
            return
        }
        if (!display.contains(".")) display += "."
    }

    fun applyOp(op: Char) {
        val current = display.toDoubleOrNull() ?: 0.0
        stored = if (stored != null && pendingOp != null) {
            compute(stored!!, current, pendingOp!!)
        } else {
            current
        }
        display = formatResult(stored!!)
        pendingOp = op
        justEvaluated = true
    }

    fun evaluate() {
        val current = display.toDoubleOrNull() ?: 0.0
        if (stored != null && pendingOp != null) {
            display = formatResult(compute(stored!!, current, pendingOp!!))
            stored = null
            pendingOp = null
        }
        justEvaluated = true
    }

    fun clear() {
        display = "0"
        stored = null
        pendingOp = null
        justEvaluated = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    display,
                    style = AmountHero,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }

            // Every slot list below sums to weight 4 (a plain digit/op = 1f, "0" = 2f, an
            // invisible filler = 1f) - see the kdoc above for why that invariant is what keeps
            // every row the same width per unit, unlike the earlier broken version.
            val grid = listOf(
                listOf(CalcKey.Op("C", Action.CLEAR), CalcKey.Op("÷", Action.OP), CalcKey.Op("×", Action.OP), CalcKey.Op("-", Action.OP)),
                listOf(CalcKey.Digit("7"), CalcKey.Digit("8"), CalcKey.Digit("9"), CalcKey.Op("+", Action.OP)),
                listOf(CalcKey.Digit("4"), CalcKey.Digit("5"), CalcKey.Digit("6"), CalcKey.Blank),
                listOf(CalcKey.Digit("1"), CalcKey.Digit("2"), CalcKey.Digit("3"), CalcKey.Blank),
                listOf(CalcKey.Digit("0", weight = 2f), CalcKey.Dot, CalcKey.Blank)
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                grid.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { key ->
                            when (key) {
                                is CalcKey.Blank -> {
                                    Spacer(Modifier.weight(1f))
                                }
                                is CalcKey.Digit -> CalculatorButton(
                                    label = key.label,
                                    weight = key.weight,
                                    style = KeyStyle.DIGIT,
                                    onClick = { inputDigit(key.label) }
                                )
                                is CalcKey.Dot -> CalculatorButton(
                                    label = ".",
                                    weight = 1f,
                                    style = KeyStyle.DIGIT,
                                    onClick = { inputDot() }
                                )
                                is CalcKey.Op -> CalculatorButton(
                                    label = key.label,
                                    weight = 1f,
                                    style = if (key.action == Action.CLEAR) KeyStyle.CLEAR else KeyStyle.OP,
                                    onClick = {
                                        if (key.action == Action.CLEAR) clear() else applyOp(symbolToOp(key.label))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer12()

            Button(
                onClick = { evaluate() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("=", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun Spacer12() {
    Spacer(Modifier.height(12.dp))
}

private enum class KeyStyle { DIGIT, OP, CLEAR }

private sealed class CalcKey {
    data class Digit(val label: String, val weight: Float = 1f) : CalcKey()
    data object Dot : CalcKey()
    data class Op(val label: String, val action: Action) : CalcKey()
    data object Blank : CalcKey()
}

private enum class Action { CLEAR, OP }

@Composable
private fun RowScope.CalculatorButton(
    label: String,
    weight: Float,
    style: KeyStyle,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        // aspectRatio = width/height. A weight-2 button (just "0") is twice as WIDE as a
        // weight-1 sibling in the same row (Row divides width by weight share) but should stay
        // the same HEIGHT as those siblings - so its ratio must be `weight` itself (2/1), not
        // `1/weight` (an earlier version of this had that inverted, making "0" taller than wide
        // instead of a normal-height double-width key).
        modifier = Modifier.weight(weight).aspectRatio(weight),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = when (style) {
                KeyStyle.CLEAR -> MaterialTheme.colorScheme.errorContainer
                KeyStyle.OP -> MaterialTheme.colorScheme.secondaryContainer
                KeyStyle.DIGIT -> MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = when (style) {
                KeyStyle.CLEAR -> MaterialTheme.colorScheme.onErrorContainer
                KeyStyle.OP -> MaterialTheme.colorScheme.onSecondaryContainer
                KeyStyle.DIGIT -> MaterialTheme.colorScheme.onSurface
            }
        )
    ) {
        Text(label, style = MaterialTheme.typography.titleLarge)
    }
}

private fun symbolToOp(symbol: String): Char = when (symbol) {
    "÷" -> '/'
    "×" -> '*'
    else -> symbol[0]
}

private fun compute(a: Double, b: Double, op: Char): Double = when (op) {
    '+' -> a + b
    '-' -> a - b
    '*' -> a * b
    '/' -> if (b != 0.0) a / b else Double.NaN
    else -> b
}

private fun formatResult(value: Double): String {
    if (value.isNaN()) return "Error"
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        "%.4f".format(value).trimEnd('0').trimEnd('.')
    }
}
