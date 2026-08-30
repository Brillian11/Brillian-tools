package com.example.ui.screens.tools

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.database.entity.QuickNoteEntity
import com.example.ui.components.MarkdownRenderer
import com.example.ui.utils.NoteAttachmentHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

@Composable
fun QuickNotesScreen(
    viewModel: QuickNotesViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val notes by viewModel.notes.collectAsState()

    if (state.isEditingMode) {
        // FULL SCREEN NOTE EDITOR
        FullScreenNoteEditor(
            viewModel = viewModel,
            state = state
        )
    } else {
        // MAIN NOTES & LOGS DASHBOARD
        MainNotesDashboard(
            viewModel = viewModel,
            state = state,
            notes = notes,
            modifier = modifier
        )
    }
}

@Composable
private fun MainNotesDashboard(
    viewModel: QuickNotesViewModel,
    state: QuickNotesUiState,
    notes: List<QuickNoteEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTabIdx by remember { mutableStateOf(0) } // 0 = Field Notes, 1 = System Logs
    var isGridView by remember { mutableStateOf(true) } // Google Keep View Toggle
    val availableTags = listOf("ALL", "Notes", "Work", "Personal", "Cutlist")

    // Filter list by selected tab first, then search query and sub-tag
    val filteredList = remember(notes, selectedTabIdx, state.selectedTag, state.searchQuery) {
        notes.filter { note ->
            val isLog = note.tag.equals("Logs", ignoreCase = true) || note.tag.equals("Log", ignoreCase = true)
            val matchesQuery = note.title.contains(state.searchQuery, ignoreCase = true) ||
                    note.content.contains(state.searchQuery, ignoreCase = true)

            if (selectedTabIdx == 0) {
                val matchesSubTag = state.selectedTag == "ALL" || note.tag.equals(state.selectedTag, ignoreCase = true)
                !isLog && matchesQuery && matchesSubTag
            } else {
                isLog && matchesQuery
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTabIdx == 0) {
                FloatingActionButton(
                    onClick = { viewModel.openNewNoteEditor() },
                    modifier = Modifier.testTag("add_note_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note")
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tab Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTabIdx == 0,
                    onClick = {
                        selectedTabIdx = 0
                        viewModel.setSelectedTag("ALL")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = { Text("Field Notes & Markdown", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = selectedTabIdx == 1,
                    onClick = {
                        selectedTabIdx = 1
                        viewModel.setSelectedTag("Logs")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = { Text("System Logs", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar & Grid/List Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text(if (selectedTabIdx == 0) "Search notes..." else "Search logs...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("notes_search_input")
                )

                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewAgenda else Icons.Default.GridView,
                        contentDescription = "Toggle Grid or List Layout",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Sub-tag Filter Chips
            if (selectedTabIdx == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableTags.forEach { tag ->
                        val tagIcon = when (tag.lowercase()) {
                            "cutlist" -> Icons.Default.ContentCut
                            "work" -> Icons.Default.Work
                            "personal" -> Icons.Default.Person
                            "notes" -> Icons.Default.StickyNote2
                            else -> Icons.Default.Description
                        }
                        FilterChip(
                            selected = state.selectedTag == tag,
                            onClick = { viewModel.setSelectedTag(tag) },
                            leadingIcon = if (tag != "ALL") {
                                { Icon(tagIcon, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            label = { Text(tag) }
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Automated audit logs of your calculations, formula updates, and transmissions captured for compliance history.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (selectedTabIdx == 0) Icons.Default.NoteAdd else Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTabIdx == 0) "No field notes found. Tap '+' to write a markdown note!" else "No system activity logs found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (isGridView) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalItemSpacing = 10.dp,
                        contentPadding = PaddingValues(bottom = 96.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredList, key = { it.id }) { note ->
                            GoogleKeepNoteCard(
                                note = note,
                                onClick = { viewModel.openEditNoteEditor(note) },
                                onDelete = { viewModel.deleteNote(note.id) },
                                onToggleCheckItem = { updatedContent ->
                                    viewModel.updateNoteContent(note.id, updatedContent)
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 96.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredList, key = { it.id }) { note ->
                            GoogleKeepNoteCard(
                                note = note,
                                onClick = { viewModel.openEditNoteEditor(note) },
                                onDelete = { viewModel.deleteNote(note.id) },
                                onToggleCheckItem = { updatedContent ->
                                    viewModel.updateNoteContent(note.id, updatedContent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoogleKeepNoteCard(
    note: QuickNoteEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleCheckItem: (updatedContent: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val cardColor = remember(note.colorHex, note.id, note.tag, isDark) {
        getKeepCardColor(note.colorHex, note.id, note.tag, isDark)
    }
    val isLog = note.tag.equals("Logs", ignoreCase = true) || note.tag.equals("Log", ignoreCase = true)
    val textColor = if (isDark) Color.White.copy(alpha = 0.95f) else Color(0xFF1F2128)
    val subTextColor = if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF5F6368)

    val formattedDate = remember(note.updatedAt) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(note.updatedAt))
    }

    val imagesList = NoteAttachmentHelper.parsePaths(note.imagePaths)
    val pdfsList = NoteAttachmentHelper.parsePaths(note.pdfPaths)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (isLog) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
            else if (cardColor == Color.White || cardColor == MaterialTheme.colorScheme.surface) Color(0xFFE0E0E0)
            else Color.Transparent
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("note_card_${note.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Title & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.title.ifBlank { "Untitled Note" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Note",
                        tint = subTextColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body preview: Checkboxes or text preview
            if (note.content.isNotBlank()) {
                val lines = note.content.lines()
                val isChecklist = lines.any {
                    val trimmed = it.trimStart()
                    trimmed.startsWith("- [ ]") || trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]")
                }

                if (isChecklist) {
                    val checkItems = remember(note.content) {
                        lines.filter {
                            val trimmed = it.trimStart()
                            trimmed.startsWith("- [ ]") || trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]")
                        }.take(5)
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        checkItems.forEach { line ->
                            val trimmed = line.trimStart()
                            val isChecked = trimmed.startsWith("- [x]") || trimmed.startsWith("- [X]")
                            val itemText = trimmed.removePrefix("- [ ] ").removePrefix("- [x] ").removePrefix("- [X] ")

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.clickable {
                                    val updatedLine = if (isChecked) {
                                        line.replace("- [x]", "- [ ]").replace("- [X]", "- [ ]")
                                    } else {
                                        line.replace("- [ ]", "- [x]")
                                    }
                                    val newContent = note.content.replace(line, updatedLine)
                                    onToggleCheckItem(newContent)
                                }
                            ) {
                                Icon(
                                    imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (isChecked) textColor.copy(alpha = 0.45f) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = itemText,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        fontSize = 13.sp
                                    ),
                                    color = if (isChecked) textColor.copy(alpha = 0.45f) else textColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                } else {
                    // Clean text preview
                    val cleanedText = remember(note.content) {
                        note.content
                            .replace(Regex("""^#+\s+""", RegexOption.MULTILINE), "")
                            .replace(Regex("""\*\*(.+?)\*\*"""), "$1")
                            .replace(Regex("""\*(.+?)\*"""), "$1")
                            .replace(Regex("""`(.+?)`"""), "$1")
                            .trim()
                    }

                    Text(
                        text = cleanedText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = textColor.copy(alpha = 0.85f),
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Card Footer: Attachments, Tag Chip & Timestamp
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (imagesList.isNotEmpty() || pdfsList.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (imagesList.isNotEmpty()) {
                            Surface(
                                color = subTextColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = subTextColor, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("${imagesList.size}", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                                }
                            }
                        }
                        if (pdfsList.isNotEmpty()) {
                            Surface(
                                color = subTextColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = subTextColor, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("${pdfsList.size}", style = MaterialTheme.typography.labelSmall, color = subTextColor)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tag chip pill
                    Surface(
                        color = subTextColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = note.tag,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                            color = textColor.copy(alpha = 0.9f),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Timestamp
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = subTextColor
                    )
                }
            }
        }
    }
}

private fun getKeepCardColor(colorHex: String, noteId: Long, tag: String, isDark: Boolean): Color {
    if (colorHex.startsWith("#") && colorHex != "#3F51B5" && colorHex != "#2E7D32") {
        try {
            return Color(android.graphics.Color.parseColor(colorHex))
        } catch (_: Exception) {}
    }

    val paletteIndex = (noteId.toInt() + tag.hashCode()).absoluteValue % 5

    return if (isDark) {
        when (paletteIndex) {
            0 -> Color(0xFF1E2A38)
            1 -> Color(0xFF38351E)
            2 -> Color(0xFF1E382A)
            3 -> Color(0xFF381E2A)
            4 -> Color(0xFF2E1E38)
            else -> Color(0xFF252525)
        }
    } else {
        when (paletteIndex) {
            0 -> Color(0xFFD7E3FF) // Keep Pastel Blue
            1 -> Color(0xFFFEF7DA) // Keep Pastel Yellow
            2 -> Color(0xFFC4F0E5) // Keep Pastel Mint
            3 -> Color(0xFFFFD8E4) // Keep Pastel Pink
            4 -> Color(0xFFE8DEF8) // Keep Pastel Lavender
            else -> Color(0xFFF5F5F5)
        }
    }
}

private class MarkdownVisualTransformation(
    private val primaryColor: Color,
    private val secondaryColor: Color,
    private val codeBgColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val annotated = buildAnnotatedString {
            append(raw)
            val dimSyntaxStyle = SpanStyle(color = primaryColor.copy(alpha = 0.45f), fontWeight = FontWeight.Normal)

            // Line headers (#, ##, ###, >, - [ ], - [x])
            val lines = raw.split("\n")
            var lineStart = 0
            for (line in lines) {
                val lineLen = line.length
                val trimmed = line.trimStart()
                val indent = lineLen - trimmed.length
                val contentStart = lineStart + indent

                if (trimmed.startsWith("# ")) {
                    if (contentStart + 2 <= raw.length) {
                        addStyle(dimSyntaxStyle, contentStart, contentStart + 2)
                        addStyle(SpanStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = primaryColor), contentStart + 2, lineStart + lineLen)
                    }
                } else if (trimmed.startsWith("## ")) {
                    if (contentStart + 3 <= raw.length) {
                        addStyle(dimSyntaxStyle, contentStart, contentStart + 3)
                        addStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = secondaryColor), contentStart + 3, lineStart + lineLen)
                    }
                } else if (trimmed.startsWith("### ")) {
                    if (contentStart + 4 <= raw.length) {
                        addStyle(dimSyntaxStyle, contentStart, contentStart + 4)
                        addStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), contentStart + 4, lineStart + lineLen)
                    }
                } else if (trimmed.startsWith("> ")) {
                    if (contentStart + 2 <= raw.length) {
                        addStyle(dimSyntaxStyle, contentStart, contentStart + 2)
                        addStyle(SpanStyle(fontStyle = FontStyle.Italic, color = primaryColor), contentStart + 2, lineStart + lineLen)
                    }
                } else if (trimmed.startsWith("- [ ] ") || trimmed.startsWith("- [x] ") || trimmed.startsWith("- [X] ")) {
                    if (contentStart + 6 <= raw.length) {
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor), contentStart, contentStart + 6)
                    }
                }

                lineStart += lineLen + 1
            }

            // Bold matching **text**
            val boldRegex = Regex("""\*\*(.+?)\*\*""")
            boldRegex.findAll(raw).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                if (start + 2 <= end - 2 && end <= raw.length) {
                    addStyle(dimSyntaxStyle, start, start + 2)
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), start + 2, end - 2)
                    addStyle(dimSyntaxStyle, end - 2, end)
                }
            }

            // Italic matching *text*
            val italicRegex = Regex("""(?<!\*)\*(?!\*)(.+?)(?<!\*)\*(?!\*)""")
            italicRegex.findAll(raw).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                if (start + 1 <= end - 1 && end <= raw.length) {
                    addStyle(dimSyntaxStyle, start, start + 1)
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), start + 1, end - 1)
                    addStyle(dimSyntaxStyle, end - 1, end)
                }
            }

            // Inline code matching `code`
            val codeRegex = Regex("""`(.+?)`""")
            codeRegex.findAll(raw).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                if (start + 1 <= end - 1 && end <= raw.length) {
                    addStyle(dimSyntaxStyle, start, start + 1)
                    addStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = codeBgColor), start + 1, end - 1)
                    addStyle(dimSyntaxStyle, end - 1, end)
                }
            }
        }

        return TransformedText(annotated, OffsetMapping.Identity)
    }
}

@Composable
private fun FullScreenNoteEditor(
    viewModel: QuickNotesViewModel,
    state: QuickNotesUiState
) {
    val context = LocalContext.current
    var previewImageModalPath by remember { mutableStateOf<String?>(null) }
    var tagDropdownExpanded by remember { mutableStateOf(false) }

    var contentTextFieldValue by remember(state.editingNoteId) {
        mutableStateOf(TextFieldValue(text = state.editorContent))
    }

    LaunchedEffect(state.editorContent) {
        if (contentTextFieldValue.text != state.editorContent) {
            contentTextFieldValue = contentTextFieldValue.copy(text = state.editorContent)
        }
    }

    fun applyFormatting(prefix: String, suffix: String = "") {
        val text = contentTextFieldValue.text
        val selection = contentTextFieldValue.selection
        val start = minOf(selection.start, selection.end).coerceIn(0, text.length)
        val end = maxOf(selection.start, selection.end).coerceIn(0, text.length)

        val newTFV = if (start != end) {
            val selectedText = text.substring(start, end)
            if (suffix.isNotEmpty()) {
                if (selectedText.startsWith(prefix) && selectedText.endsWith(suffix) && selectedText.length >= prefix.length + suffix.length) {
                    val unwrapped = selectedText.substring(prefix.length, selectedText.length - suffix.length)
                    val newText = text.substring(0, start) + unwrapped + text.substring(end)
                    TextFieldValue(
                        text = newText,
                        selection = TextRange(start, start + unwrapped.length)
                    )
                } else {
                    val wrapped = "$prefix$selectedText$suffix"
                    val newText = text.substring(0, start) + wrapped + text.substring(end)
                    TextFieldValue(
                        text = newText,
                        selection = TextRange(start, start + wrapped.length)
                    )
                }
            } else {
                val inserted = "$prefix$selectedText"
                val newText = text.substring(0, start) + inserted + text.substring(end)
                TextFieldValue(
                    text = newText,
                    selection = TextRange(start, start + inserted.length)
                )
            }
        } else {
            if (suffix.isNotEmpty()) {
                val placeholder = when (prefix) {
                    "**" -> "bold text"
                    "*" -> "italic text"
                    "```\n" -> "code"
                    else -> "text"
                }
                val inserted = "$prefix$placeholder$suffix"
                val newText = text.substring(0, start) + inserted + text.substring(end)
                val selStart = start + prefix.length
                val selEnd = selStart + placeholder.length
                TextFieldValue(
                    text = newText,
                    selection = TextRange(selStart, selEnd)
                )
            } else {
                val lineStart = if (text.isEmpty()) 0 else text.lastIndexOf('\n', (start - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
                val lineEnd = text.indexOf('\n', start).let { if (it == -1) text.length else it }
                val currentLine = text.substring(lineStart, lineEnd)

                val (updatedLine, newCursor) = if (currentLine.startsWith(prefix)) {
                    currentLine.removePrefix(prefix) to (start - prefix.length).coerceAtLeast(lineStart)
                } else {
                    prefix + currentLine to start + prefix.length
                }

                val newText = text.substring(0, lineStart) + updatedLine + text.substring(lineEnd)
                TextFieldValue(
                    text = newText,
                    selection = TextRange(newCursor)
                )
            }
        }

        contentTextFieldValue = newTFV
        viewModel.updateEditorContent(newTFV.text)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val codeBgColor = MaterialTheme.colorScheme.surfaceVariant
    val markdownVisualTransformation = remember(primaryColor, secondaryColor, codeBgColor) {
        MarkdownVisualTransformation(primaryColor, secondaryColor, codeBgColor)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { inputUri ->
            val savedFile = NoteAttachmentHelper.saveUriToInternalStorage(context, inputUri, "img")
            if (savedFile != null) {
                viewModel.addImageAttachment(savedFile.absolutePath)
                Toast.makeText(context, "Image Attached!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { inputUri ->
            val savedFile = NoteAttachmentHelper.saveUriToInternalStorage(context, inputUri, "pdf")
            if (savedFile != null) {
                viewModel.addPdfAttachment(savedFile.absolutePath)
                Toast.makeText(context, "PDF File Attached!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    var showPalettePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        // TOP NAVIGATION & ACTION BAR
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(onClick = {
                            viewModel.saveCurrentNote()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back & Save")
                        }

                        // Tag selector menu
                        Box {
                            FilterChip(
                                selected = true,
                                onClick = { tagDropdownExpanded = true },
                                leadingIcon = {
                                    Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                label = { Text(state.editorTag) }
                            )
                            DropdownMenu(
                                expanded = tagDropdownExpanded,
                                onDismissRequest = { tagDropdownExpanded = false }
                            ) {
                                listOf("Notes", "Work", "Personal", "Cutlist", "Logs").forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category) },
                                        leadingIcon = {
                                            val catIcon = when (category.lowercase()) {
                                                "cutlist" -> Icons.Default.ContentCut
                                                "work" -> Icons.Default.Work
                                                "personal" -> Icons.Default.Person
                                                "logs" -> Icons.Default.Terminal
                                                else -> Icons.Default.StickyNote2
                                            }
                                            Icon(catIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                        onClick = {
                                            viewModel.updateEditorTag(category)
                                            tagDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(onClick = { showPalettePicker = !showPalettePicker }) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Note Color Palette",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = { viewModel.togglePreviewMode() }) {
                            Icon(
                                imageVector = if (state.isRichTextPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                                contentDescription = if (state.isRichTextPreviewMode) "Edit Mode" else "Preview Mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(onClick = {
                            viewModel.saveCurrentNote()
                            Toast.makeText(context, "Note Saved!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Save, contentDescription = "Save Note", tint = MaterialTheme.colorScheme.primary)
                        }

                        IconButton(onClick = { viewModel.deleteCurrentNote() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Note", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Customizable Color Palette Row
                AnimatedVisibility(visible = showPalettePicker) {
                    val paletteOptions = remember {
                        listOf(
                            "Default Blue" to "#D7E3FF",
                            "Pastel Yellow" to "#FEF7DA",
                            "Pastel Mint" to "#C4F0E5",
                            "Pastel Pink" to "#FFD8E4",
                            "Pastel Lavender" to "#E8DEF8",
                            "Soft Peach" to "#FFE0B2",
                            "Slate Teal" to "#B2DFDB",
                            "Soft Gray" to "#F5F5F5",
                            "Classic Indigo" to "#3F51B5",
                            "Timber Green" to "#2E7D32"
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(paletteOptions) { (name, hex) ->
                            val parsedColor = remember(hex) {
                                try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.LightGray }
                            }
                            val isSelected = state.editorColorHex.equals(hex, ignoreCase = true)

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(parsedColor)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.updateEditorColorHex(hex)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = name,
                                        tint = if (hex == "#FEF7DA" || hex == "#D7E3FF" || hex == "#C4F0E5" || hex == "#FFD8E4" || hex == "#E8DEF8" || hex == "#FFE0B2" || hex == "#F5F5F5") Color.Black else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // DECREASED TITLE INPUT FIELD
        TextField(
            value = state.editorTitle,
            onValueChange = { viewModel.updateEditorTitle(it) },
            placeholder = { Text("Note Title...", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
            textStyle = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 0.dp)
                .testTag("full_note_title_input")
        )

        // TOOLBAR PLACED DIRECTLY UNDER NOTES TITLE
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { applyFormatting("# ") }, modifier = Modifier.size(36.dp)) {
                    Text("H1", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { applyFormatting("## ") }, modifier = Modifier.size(36.dp)) {
                    Text("H2", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = { applyFormatting("**", "**") }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.FormatBold, contentDescription = "Bold", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { applyFormatting("*", "*") }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { applyFormatting("- ") }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet List", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { applyFormatting("- [ ] ") }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.CheckBox, contentDescription = "Task Checkbox", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { applyFormatting("```\n", "\n```") }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Code, contentDescription = "Code", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { applyFormatting("> ") }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.FormatQuote, contentDescription = "Quote", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = {
                    applyFormatting("| Header 1 | Header 2 |\n|---|---|\n| Item 1 | Value 1 |")
                }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.TableChart, contentDescription = "Table", modifier = Modifier.size(18.dp))
                }

                VerticalDivider(
                    modifier = Modifier
                        .height(20.dp)
                        .padding(horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                IconButton(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Image, contentDescription = "Attach Image", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = { pdfPickerLauncher.launch("application/pdf") }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Attach PDF", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.tertiary)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        // MAIN SCROLLABLE EDITOR CONTAINER (Body + Attachments)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (state.isRichTextPreviewMode) {
                // RENDERED RICH MARKDOWN VIEW
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        MarkdownRenderer(
                            markdownText = state.editorContent.ifBlank { "*No content yet. Switch to Edit mode to write.*" },
                            modifier = Modifier.fillMaxWidth(),
                            onContentChange = { updated ->
                                viewModel.updateEditorContent(updated)
                            }
                        )
                    }
                }
            } else {
                // RICH FORMATTED EDITABLE BODY TEXT FIELD
                TextField(
                    value = contentTextFieldValue,
                    onValueChange = { newTFV ->
                        contentTextFieldValue = newTFV
                        viewModel.updateEditorContent(newTFV.text)
                    },
                    placeholder = { Text("Write markdown notes here...\n\n# Heading 1\n- Bullet item\n- [ ] Task checkbox\n**Bold text**") },
                    minLines = 8,
                    visualTransformation = markdownVisualTransformation,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("full_note_content_input")
                )
            }

            // ATTACHMENTS SECTION (IMAGES)
            if (state.editorImagePaths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text("Attached Images (${state.editorImagePaths.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.editorImagePaths) { imgPath ->
                        val file = File(imgPath)
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .width(140.dp)
                                .height(160.dp)
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { previewImageModalPath = imgPath }
                                ) {
                                    AsyncImage(
                                        model = if (file.exists()) file else imgPath,
                                        contentDescription = "Attachment",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            viewModel.insertMarkdownFormatting("![Image]($imgPath)")
                                            Toast.makeText(context, "Inserted image link to Markdown!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Insert into MD", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeImageAttachment(imgPath) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ATTACHMENTS SECTION (PDFs)
            if (state.editorPdfPaths.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                    Text("Attached PDF Documents (${state.editorPdfPaths.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.editorPdfPaths.forEach { pdfPath ->
                        val fileName = NoteAttachmentHelper.getFileName(pdfPath)
                        val fileSize = NoteAttachmentHelper.getFileSizeFormatted(pdfPath)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.InsertDriveFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = fileName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = fileSize,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        NoteAttachmentHelper.openPdfFile(context, pdfPath)
                                    }) {
                                        Icon(Icons.Default.OpenInNew, contentDescription = "Open PDF", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.removePdfAttachment(pdfPath) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete PDF", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Image Preview Modal Dialog
    if (previewImageModalPath != null) {
        val path = previewImageModalPath!!
        val file = File(path)
        Dialog(onDismissRequest = { previewImageModalPath = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model = if (file.exists()) file else path,
                        contentDescription = "Image Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { previewImageModalPath = null }) {
                        Text("Close Preview")
                    }
                }
            }
        }
    }
}
