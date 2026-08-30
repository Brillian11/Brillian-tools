package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File

@Composable
fun MarkdownRenderer(
    markdownText: String,
    modifier: Modifier = Modifier,
    onContentChange: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val lines = markdownText.lines()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        var inCodeBlock = false
        val codeBlockLines = mutableListOf<String>()
        var inTable = false
        val tableLines = mutableListOf<String>()

        var lineIndex = 0
        while (lineIndex < lines.size) {
            val line = lines[lineIndex]
            val trimmed = line.trim()

            // Code block parsing
            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    // Render accumulated code block
                    val codeContent = codeBlockLines.joinToString("\n")
                    CodeBlockView(codeContent = codeContent, context = context)
                    codeBlockLines.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                lineIndex++
                continue
            }

            if (inCodeBlock) {
                codeBlockLines.add(line)
                lineIndex++
                continue
            }

            // Table parsing
            if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
                inTable = true
                tableLines.add(trimmed)
                lineIndex++
                continue
            } else if (inTable) {
                // Render table
                MarkdownTableView(tableLines = tableLines)
                tableLines.clear()
                inTable = false
            }

            // Images ![alt](src)
            if (trimmed.startsWith("![") && trimmed.contains("](") && trimmed.endsWith(")")) {
                val altText = trimmed.substringAfter("![").substringBefore("](")
                val src = trimmed.substringAfter("](").substringBeforeLast(")")
                MarkdownImageView(src = src, alt = altText)
                lineIndex++
                continue
            }

            // Headers
            if (trimmed.startsWith("# ")) {
                Text(
                    text = parseInlineMarkdown(trimmed.substring(2)),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            } else if (trimmed.startsWith("## ")) {
                Text(
                    text = parseInlineMarkdown(trimmed.substring(3)),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            } else if (trimmed.startsWith("### ")) {
                Text(
                    text = parseInlineMarkdown(trimmed.substring(4)),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            } else if (trimmed.startsWith("#### ")) {
                Text(
                    text = parseInlineMarkdown(trimmed.substring(5)),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            } else if (trimmed.startsWith("> ")) {
                // Blockquote
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(trimmed.substring(2)),
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
                // Horizontal divider
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            } else if (trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ")) {
                // Interactive Checkbox task list
                val isChecked = trimmed.startsWith("- [x] ", ignoreCase = true)
                val taskText = trimmed.substring(6)
                val currentLineIdx = lineIndex

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            if (onContentChange != null) {
                                val updatedLines = lines.toMutableList()
                                val newPrefix = if (checked) "- [x] " else "- [ ] "
                                updatedLines[currentLineIdx] = newPrefix + taskText
                                onContentChange(updatedLines.joinToString("\n"))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = parseInlineMarkdown(taskText),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textDecoration = if (isChecked) TextDecoration.LineThrough else null
                        ),
                        color = if (isChecked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )
                }
            } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                // Bullet point
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = parseInlineMarkdown(trimmed.substring(2)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else if (Regex("^\\d+\\.\\s.*").matches(trimmed)) {
                // Numbered list
                val numStr = trimmed.substringBefore(".")
                val restStr = trimmed.substringAfter(". ")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("$numStr. ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = parseInlineMarkdown(restStr),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else if (trimmed.isNotBlank()) {
                // Standard Paragraph Text
                Text(
                    text = parseInlineMarkdown(line),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            lineIndex++
        }

        // Flush remaining table or code block if file ends inside one
        if (inCodeBlock) {
            CodeBlockView(codeContent = codeBlockLines.joinToString("\n"), context = context)
        }
        if (inTable) {
            MarkdownTableView(tableLines = tableLines)
        }
    }
}

@Composable
private fun CodeBlockView(codeContent: String, context: Context) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CODE / SCRIPT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Code", codeContent))
                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = MaterialTheme.colorScheme.inverseOnSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = codeContent,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = Color(0xFF80CBC4),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
private fun MarkdownTableView(tableLines: List<String>) {
    if (tableLines.isEmpty()) return

    val rows = tableLines.filterNot { line ->
        val clean = line.replace("|", "").trim()
        clean.all { it == '-' || it == ':' || it == ' ' }
    }.map { line ->
        line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }

    if (rows.isEmpty()) return

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            rows.forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier
                        .background(
                            if (rowIndex == 0) MaterialTheme.colorScheme.primaryContainer
                            else if (rowIndex % 2 == 1) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { cell ->
                        Text(
                            text = parseInlineMarkdown(cell),
                            style = if (rowIndex == 0) MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            else MaterialTheme.typography.bodySmall,
                            color = if (rowIndex == 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .width(130.dp)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
                if (rowIndex < rows.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun MarkdownImageView(src: String, alt: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(6.dp)
        ) {
            val file = File(src)
            val model = if (file.exists()) file else src
            AsyncImage(
                model = model,
                contentDescription = alt,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            )
            if (alt.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alt,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Parses inline Markdown formatting: **bold**, *italic*, ~~strikethrough~~, `code`
 */
fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // **bold**
                text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // *italic*
                text.startsWith("*", i) && !text.startsWith("**", i) -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // ~~strikethrough~~
                text.startsWith("~~", i) -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end != -1) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // `code`
                text.startsWith("`", i) && !text.startsWith("```", i) -> {
                    val end = text.indexOf("`", i + 1)
                    if (end != -1) {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = Color(0x33808080)
                            )
                        ) {
                            append(" ${text.substring(i + 1, end)} ")
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}
