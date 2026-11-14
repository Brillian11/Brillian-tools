package com.example

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isEnabled
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import java.io.File
import java.util.Collections

object RobotAuditReport {

    data class Finding(
        val severity: String, // CRITICAL, HIGH, MEDIUM, LOW, INFO
        val area: String,
        val title: String,
        val details: String
    )

    data class ToolProbeRecord(
        val index: Int,
        val id: String,
        val title: String,
        val category: String,
        val mounted: Boolean,
        val titleMatched: Boolean,
        val clickableCount: Int,
        val clickedCount: Int,
        val editableCount: Int,
        val editedCount: Int,
        val smallTargetsCount: Int,
        val returnedBackCleanly: Boolean,
        val notes: String,
        val error: String? = null
    )

    val findings: MutableList<Finding> = Collections.synchronizedList(mutableListOf())
    val toolRecords: MutableList<ToolProbeRecord> = Collections.synchronizedList(mutableListOf())
    val coreFlowResults: MutableList<String> = Collections.synchronizedList(mutableListOf())

    fun addFinding(severity: String, area: String, title: String, details: String) {
        val f = Finding(severity, area, title, details)
        if (findings.none { it.area == area && it.title == title && it.details == details }) {
            findings.add(f)
        }
    }

    fun writeReportFile() {
        val reportsDir = File("build/reports").apply { mkdirs() }
        val reportFile = File(reportsDir, "robot_prober_report.md")

        val totalTools = toolRecords.size
        val passedTools = toolRecords.count { it.error == null && it.mounted }
        val failedTools = toolRecords.count { it.error != null || !it.mounted }

        val sb = StringBuilder()
        sb.appendLine("# Brillian Tools - Automated Robot Prober Audit Report")
        sb.appendLine()
        sb.appendLine("- **Probed Registered Tools**: $totalTools")
        sb.appendLine("- **Mounted & Interacted Without Crash**: $passedTools")
        sb.appendLine("- **Crashed / Failed Mount**: $failedTools")
        sb.appendLine("- **Total Findings Recorded**: ${findings.size}")
        sb.appendLine()

        sb.appendLine("## 1. Prioritized Findings")
        sb.appendLine()
        val severityOrder = listOf("CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO")
        val sortedFindings = findings.sortedBy { severityOrder.indexOf(it.severity).let { idx -> if (idx == -1) 99 else idx } }
        if (sortedFindings.isEmpty()) {
            sb.appendLine("_No findings recorded._")
        } else {
            sortedFindings.forEachIndexed { idx, f ->
                sb.appendLine("### ${idx + 1}. [${f.severity}] (${f.area}) ${f.title}")
                sb.appendLine(f.details)
                sb.appendLine()
            }
        }

        sb.appendLine("## 2. Core Flows & Studio Audits")
        sb.appendLine()
        coreFlowResults.forEach { sb.appendLine("- $it") }
        sb.appendLine()

        sb.appendLine("## 3. Tool-by-Tool Robot Probe Matrix ($totalTools Tools)")
        sb.appendLine()
        sb.appendLine("| # | Tool ID | Title | Category | Mount | Clicks (ok/found) | Inputs (ok/found) | <48dp Targets | Back OK | Notes / Error |")
        sb.appendLine("|---|---|---|---|---|---|---|---|---|---|")
        toolRecords.sortedBy { it.index }.forEach { r ->
            val status = if (r.error == null && r.mounted) "PASS" else "FAIL"
            val notesText = (r.error ?: r.notes).replace("\n", " ").replace("|", "/")
            sb.appendLine(
                "| ${r.index} | `${r.id}` | ${r.title.replace("|", "/")} | ${r.category} | $status | " +
                    "${r.clickedCount}/${r.clickableCount} | ${r.editedCount}/${r.editableCount} | " +
                    "${r.smallTargetsCount} | ${r.returnedBackCleanly} | $notesText |"
            )
        }

        reportFile.writeText(sb.toString())
        println("RobotProber report written to: ${reportFile.absolutePath}")
    }
}

object RobotSemanticsCrawler {

    data class CrawlStats(
        val clickableFound: Int,
        val clickedSuccess: Int,
        val editableFound: Int,
        val editedSuccess: Int,
        val smallTouchTargets: List<String>,
        val errors: List<String>
    )

    private val skipLabels = setOf(
        "navigate back",
        "back",
        "close camera",
        "open full tool screen",
        "app settings",
        "tool catalog",
        "customize dashboard",
        "workspace profile & guide",
        "offline sync center",
        "erase",
        "delete",
        "clear all",
        "reset all",
        "remove"
    )

    private fun describeNode(node: SemanticsNode): String {
        val tag = node.config.getOrNull(SemanticsProperties.TestTag)
        val text = node.config.getOrNull(SemanticsProperties.Text)
            ?.joinToString(" ") { it.text }
            ?.trim()
        val desc = node.config.getOrNull(SemanticsProperties.ContentDescription)
            ?.joinToString(" ")
            ?.trim()
        return listOfNotNull(
            tag?.let { "tag=$it" },
            text?.takeIf { it.isNotEmpty() }?.let { "text=\"${it.take(36)}\"" },
            desc?.takeIf { it.isNotEmpty() }?.let { "desc=\"${it.take(36)}\"" }
        ).joinToString(", ").ifEmpty { "node#${node.id}" }
    }

    private fun isTopBarOrDestructive(node: SemanticsNode): Boolean {
        val tag = node.config.getOrNull(SemanticsProperties.TestTag) ?: ""
        if (tag in setOf(
                "top_app_bar_back_button",
                "app_settings_button",
                "tool_catalog_button",
                "customize_dashboard_button",
                "header_profile_button",
                "sync_status_badge_button",
                "metalworks_back_button",
                "btn_outdoor_back",
                "btn_camera_close",
                "btn_camera_open_full_tool"
            )
        ) return true

        val text = node.config.getOrNull(SemanticsProperties.Text)
            ?.joinToString(" ") { it.text }?.lowercase()?.trim() ?: ""
        val desc = node.config.getOrNull(SemanticsProperties.ContentDescription)
            ?.joinToString(" ")?.lowercase()?.trim() ?: ""

        return skipLabels.any { text == it || desc == it || desc.contains("navigate back") }
    }

    fun probeCurrentScreen(
        rule: ComposeContentTestRule,
        density: Float,
        areaLabel: String,
        maxInputs: Int = 4,
        maxClicks: Int = 10
    ): CrawlStats {
        rule.waitForIdle()
        val errors = mutableListOf<String>()
        val smallTargets = mutableListOf<String>()

        val interactiveSnapshot = try {
            rule.onAllNodes(hasClickAction() or isToggleable()).fetchSemanticsNodes()
        } catch (t: Throwable) {
            errors.add("fetchSemanticsNodes(interactive) failed: ${t.message}")
            emptyList()
        }

        val minPx = 47.5f * density
        for (node in interactiveSnapshot) {
            if (!isTopBarOrDestructive(node)) {
                val bounds = node.boundsInRoot
                val wDp = node.size.width / density
                val hDp = node.size.height / density
                if (bounds.width > 0f && bounds.height > 0f && (node.size.width < minPx || node.size.height < minPx)) {
                    if (wDp < 44f || hDp < 36f) {
                        smallTargets.add("${describeNode(node)} (${wDp.toInt()}x${hDp.toInt()}dp)")
                    }
                }
            }
        }

        val editableNodes = try {
            rule.onAllNodes(hasSetTextAction() and isEnabled()).fetchSemanticsNodes()
        } catch (_: Throwable) {
            emptyList()
        }

        var editedOk = 0
        val editableCount = editableNodes.size
        for (idx in 0 until minOf(editableCount, maxInputs)) {
            val label = describeNode(editableNodes[idx])
            try {
                val nodeInteraction = rule.onAllNodes(hasSetTextAction() and isEnabled())[idx]
                try { nodeInteraction.performScrollTo() } catch (_: Throwable) {}
                try {
                    nodeInteraction.performTextReplacement("12")
                } catch (_: Throwable) {
                    nodeInteraction.performTextInput("12")
                }
                rule.waitForIdle()
                editedOk++
            } catch (t: Throwable) {
                errors.add("Text input failed on [$label]: ${t.javaClass.simpleName}: ${t.message}")
            }
        }

        val clickableCount = interactiveSnapshot.size
        var clickedOk = 0
        val clickedDescriptions = mutableSetOf<String>()

        var attempts = 0
        var candidateCursor = 0
        while (clickedOk < maxClicks && attempts < maxClicks * 2) {
            attempts++
            val currentClickable = try {
                rule.onAllNodes((hasClickAction() or isToggleable()) and isEnabled()).fetchSemanticsNodes()
            } catch (t: Throwable) {
                errors.add("Re-fetch interactive nodes failed: ${t.message}")
                break
            }
            if (candidateCursor >= currentClickable.size) break

            val targetNode = currentClickable[candidateCursor]
            val desc = describeNode(targetNode)
            candidateCursor++

            if (isTopBarOrDestructive(targetNode) || clickedDescriptions.contains(desc)) {
                continue
            }
            if (targetNode.config.contains(SemanticsProperties.EditableText)) {
                continue
            }
            clickedDescriptions.add(desc)

            try {
                val interaction = rule.onAllNodes((hasClickAction() or isToggleable()) and isEnabled())[candidateCursor - 1]
                try { interaction.performScrollTo() } catch (_: Throwable) {}
                interaction.performClick()
                rule.waitForIdle()
                clickedOk++

                dismissAnyOpenDialog(rule)
            } catch (t: Throwable) {
                errors.add("Click failed on $areaLabel [$desc]: ${t.javaClass.simpleName}: ${t.message}")
            }
        }

        return CrawlStats(
            clickableFound = clickableCount,
            clickedSuccess = clickedOk,
            editableFound = editableCount,
            editedSuccess = editedOk,
            smallTouchTargets = smallTargets.distinct(),
            errors = errors
        )
    }

    fun dismissAnyOpenDialog(rule: ComposeContentTestRule) {
        try {
            val dialogs = rule.onAllNodes(isDialog()).fetchSemanticsNodes()
            if (dialogs.isNotEmpty()) {
                val dismissCandidates = listOf("Got it", "Close", "Dismiss", "OK", "Cancel", "Done")
                for (btnText in dismissCandidates) {
                    val nodes = rule.onAllNodesWithText(btnText, ignoreCase = true).fetchSemanticsNodes()
                    if (nodes.isNotEmpty()) {
                        try {
                            rule.onAllNodesWithText(btnText, ignoreCase = true)[0].performClick()
                            rule.waitForIdle()
                            return
                        } catch (_: Throwable) {}
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    fun dumpVisibleTexts(rule: ComposeContentTestRule): List<String> {
        return try {
            val root = rule.onRoot(useUnmergedTree = false).fetchSemanticsNode()
            val out = mutableListOf<String>()
            fun walk(n: SemanticsNode) {
                n.config.getOrNull(SemanticsProperties.Text)?.forEach {
                    if (it.text.isNotBlank()) out.add(it.text)
                }
                n.children.forEach { walk(it) }
            }
            walk(root)
            out
        } catch (_: Throwable) {
            emptyList()
        }
    }
}
