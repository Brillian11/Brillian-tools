package com.example.domain.math

enum class MaterialType {
    TIMBER_BOARD, // 1D linear stock board
    PLYWOOD_SHEET // 2D sheet panel
}

data class CutPiece(
    val id: String,
    val label: String,
    val lengthMm: Double,
    val widthMm: Double = 89.0, // Default width (e.g. 2x4 nominal / 89mm)
    val quantity: Int = 1,
    val colorHex: Long = 0xFF6750A4
)

data class StockBoard(
    val id: Long = 0,
    val name: String,
    val type: MaterialType = MaterialType.TIMBER_BOARD,
    val lengthMm: Double = 2438.4, // 8 ft
    val widthMm: Double = 1219.2,   // 4 ft (for plywood sheet)
    val costPerUnit: Double = 0.0,
    val quantityAvailable: Int = 999
)

data class PlacedPiece(
    val pieceLabel: String,
    val lengthMm: Double,
    val widthMm: Double = 0.0,
    val startPositionMm: Double, // 1D start OR 2D X start
    val endPositionMm: Double,   // 1D end OR 2D X end
    val startYMm: Double = 0.0,  // 2D Y start
    val endYMm: Double = 0.0,    // 2D Y end
    val colorHex: Long = 0xFF6750A4,
    val isRotated: Boolean = false
)

data class UsedBoardLayout(
    val boardName: String,
    val materialType: MaterialType = MaterialType.TIMBER_BOARD,
    val totalBoardLengthMm: Double,
    val totalBoardWidthMm: Double = 0.0,
    val placedPieces: List<PlacedPiece>,
    val remainingScrapMm: Double, // 1D linear scrap mm OR 2D scrap area mm²
    val kerfLossMm: Double
)

data class CutlistOptimizationResult(
    val usedBoards: List<UsedBoardLayout>,
    val totalStockLengthMm: Double,
    val totalCutLengthMm: Double,
    val totalKerfLossMm: Double,
    val totalScrapLengthMm: Double,
    val yieldPercentage: Double,
    val unplacedPieces: List<CutPiece>
)

object CutlistOptimizerEngine {

    val DEFAULT_CUT_COLORS = listOf(
        0xFF6750A4, // Purple
        0xFF006A6A, // Teal
        0xFF7D5260, // Rose
        0xFF984E00, // Amber
        0xFF1D6F42, // Green
        0xFF005AC1, // Blue
        0xFF8F4C38, // Terracotta
        0xFF6E5D00  // Gold
    )

    fun optimize(
        stockBoards: List<StockBoard>,
        requestedCuts: List<CutPiece>,
        bladeKerfMm: Double,
        trimMarginMm: Double = 0.0
    ): CutlistOptimizationResult {
        if (stockBoards.isEmpty() || requestedCuts.isEmpty()) {
            return CutlistOptimizationResult(
                usedBoards = emptyList(),
                totalStockLengthMm = 0.0,
                totalCutLengthMm = 0.0,
                totalKerfLossMm = 0.0,
                totalScrapLengthMm = 0.0,
                yieldPercentage = 0.0,
                unplacedPieces = emptyList()
            )
        }

        val primaryStock = stockBoards.first()
        val isPlywood = primaryStock.type == MaterialType.PLYWOOD_SHEET

        return if (isPlywood) {
            optimizePlywood2D(primaryStock, requestedCuts, bladeKerfMm, trimMarginMm)
        } else {
            optimizeTimber1D(stockBoards, requestedCuts, bladeKerfMm, trimMarginMm)
        }
    }

    private fun optimizeTimber1D(
        stockBoards: List<StockBoard>,
        requestedCuts: List<CutPiece>,
        bladeKerfMm: Double,
        trimMarginMm: Double
    ): CutlistOptimizationResult {
        val individualPieces = mutableListOf<Triple<String, Double, Long>>()
        requestedCuts.forEachIndexed { typeIdx, cut ->
            val color = cut.colorHex
            repeat(cut.quantity) { idx ->
                val label = if (cut.quantity > 1) "${cut.label} (#${idx + 1})" else cut.label
                individualPieces.add(Triple(label, cut.lengthMm, color))
            }
        }

        // Sort descending (First Fit Decreasing)
        individualPieces.sortByDescending { it.second }

        val primaryStock = stockBoards.first()
        val usedLayouts = mutableListOf<UsedBoardLayout>()
        val unplaced = mutableListOf<CutPiece>()
        val remainingPieces = individualPieces.toMutableList()

        while (remainingPieces.isNotEmpty()) {
            val stock = primaryStock
            val usableLength = stock.lengthMm - (trimMarginMm * 2)

            if (usableLength <= 0) break

            var currentPos = trimMarginMm
            val placedInThisBoard = mutableListOf<PlacedPiece>()
            var kerfLoss = 0.0

            val it = remainingPieces.iterator()
            while (it.hasNext()) {
                val piece = it.next()
                val pieceLen = piece.second
                val pieceColor = piece.third

                val kerf = if (placedInThisBoard.isNotEmpty()) bladeKerfMm else 0.0
                val spaceNeeded = pieceLen + kerf

                if (currentPos + spaceNeeded <= stock.lengthMm - trimMarginMm) {
                    currentPos += kerf
                    kerfLoss += kerf

                    val start = currentPos
                    val end = currentPos + pieceLen
                    placedInThisBoard.add(
                        PlacedPiece(
                            pieceLabel = piece.first,
                            lengthMm = pieceLen,
                            widthMm = stock.widthMm,
                            startPositionMm = start,
                            endPositionMm = end,
                            colorHex = pieceColor
                        )
                    )
                    currentPos = end
                    it.remove()
                }
            }

            if (placedInThisBoard.isNotEmpty()) {
                val scrap = (stock.lengthMm - trimMarginMm) - currentPos
                usedLayouts.add(
                    UsedBoardLayout(
                        boardName = stock.name,
                        materialType = MaterialType.TIMBER_BOARD,
                        totalBoardLengthMm = stock.lengthMm,
                        totalBoardWidthMm = stock.widthMm,
                        placedPieces = placedInThisBoard,
                        remainingScrapMm = scrap.coerceAtLeast(0.0),
                        kerfLossMm = kerfLoss
                    )
                )
            } else {
                val failedPiece = remainingPieces.removeAt(0)
                unplaced.add(CutPiece(id = "unplaced", label = failedPiece.first, lengthMm = failedPiece.second, quantity = 1, colorHex = failedPiece.third))
            }
        }

        val totalStock = usedLayouts.sumOf { it.totalBoardLengthMm }
        val totalCuts = usedLayouts.sumOf { board -> board.placedPieces.sumOf { it.lengthMm } }
        val totalKerf = usedLayouts.sumOf { it.kerfLossMm }
        val totalScrap = usedLayouts.sumOf { it.remainingScrapMm }
        val yield = if (totalStock > 0) (totalCuts / totalStock) * 100.0 else 0.0

        return CutlistOptimizationResult(
            usedBoards = usedLayouts,
            totalStockLengthMm = totalStock,
            totalCutLengthMm = totalCuts,
            totalKerfLossMm = totalKerf,
            totalScrapLengthMm = totalScrap,
            yieldPercentage = yield,
            unplacedPieces = unplaced
        )
    }

    private fun optimizePlywood2D(
        stockSheet: StockBoard,
        requestedCuts: List<CutPiece>,
        bladeKerfMm: Double,
        trimMarginMm: Double
    ): CutlistOptimizationResult {
        data class PanelItem(
            val label: String,
            val lengthMm: Double,
            val widthMm: Double,
            val colorHex: Long
        )

        val individualPanels = mutableListOf<PanelItem>()
        requestedCuts.forEach { cut ->
            repeat(cut.quantity) { idx ->
                val label = if (cut.quantity > 1) "${cut.label} (#${idx + 1})" else cut.label
                individualPanels.add(PanelItem(label, cut.lengthMm, cut.widthMm, cut.colorHex))
            }
        }

        // Sort by area descending
        individualPanels.sortByDescending { it.lengthMm * it.widthMm }

        val sheetL = stockSheet.lengthMm - (trimMarginMm * 2)
        val sheetW = stockSheet.widthMm - (trimMarginMm * 2)

        val usedLayouts = mutableListOf<UsedBoardLayout>()
        val unplaced = mutableListOf<CutPiece>()
        val remaining = individualPanels.toMutableList()

        while (remaining.isNotEmpty()) {
            val placedInSheet = mutableListOf<PlacedPiece>()
            var currentY = trimMarginMm
            var currentShelfH = 0.0
            var currentX = trimMarginMm

            val it = remaining.iterator()
            while (it.hasNext()) {
                val panel = it.next()
                var pLen = panel.lengthMm
                var pWid = panel.widthMm
                var rotated = false

                // Orient panel long side along sheet length if beneficial
                if (pLen > sheetL && pWid <= sheetL) {
                    pLen = panel.widthMm
                    pWid = panel.lengthMm
                    rotated = true
                }

                val kerfX = if (currentX > trimMarginMm) bladeKerfMm else 0.0
                val kerfY = if (currentY > trimMarginMm) bladeKerfMm else 0.0

                if (currentX + kerfX + pLen <= stockSheet.lengthMm - trimMarginMm) {
                    if (currentY + kerfY + pWid <= stockSheet.widthMm - trimMarginMm) {
                        val startX = currentX + kerfX
                        val startY = currentY + kerfY
                        val endX = startX + pLen
                        val endY = startY + pWid

                        placedInSheet.add(
                            PlacedPiece(
                                pieceLabel = panel.label,
                                lengthMm = pLen,
                                widthMm = pWid,
                                startPositionMm = startX,
                                endPositionMm = endX,
                                startYMm = startY,
                                endYMm = endY,
                                colorHex = panel.colorHex,
                                isRotated = rotated
                            )
                        )

                        currentX = endX
                        if (pWid > currentShelfH) {
                            currentShelfH = pWid
                        }
                        it.remove()
                    }
                } else {
                    // Move to next shelf
                    currentY += currentShelfH + bladeKerfMm
                    currentX = trimMarginMm
                    currentShelfH = 0.0

                    if (currentY + pWid <= stockSheet.widthMm - trimMarginMm && currentX + pLen <= stockSheet.lengthMm - trimMarginMm) {
                        val startX = currentX
                        val startY = currentY
                        val endX = startX + pLen
                        val endY = startY + pWid

                        placedInSheet.add(
                            PlacedPiece(
                                pieceLabel = panel.label,
                                lengthMm = pLen,
                                widthMm = pWid,
                                startPositionMm = startX,
                                endPositionMm = endX,
                                startYMm = startY,
                                endYMm = endY,
                                colorHex = panel.colorHex,
                                isRotated = rotated
                            )
                        )

                        currentX = endX
                        currentShelfH = pWid
                        it.remove()
                    }
                }
            }

            if (placedInSheet.isNotEmpty()) {
                val sheetTotalArea = stockSheet.lengthMm * stockSheet.widthMm
                val cutTotalArea = placedInSheet.sumOf { it.lengthMm * it.widthMm }
                val scrapArea = (sheetTotalArea - cutTotalArea).coerceAtLeast(0.0)

                usedLayouts.add(
                    UsedBoardLayout(
                        boardName = stockSheet.name,
                        materialType = MaterialType.PLYWOOD_SHEET,
                        totalBoardLengthMm = stockSheet.lengthMm,
                        totalBoardWidthMm = stockSheet.widthMm,
                        placedPieces = placedInSheet,
                        remainingScrapMm = scrapArea,
                        kerfLossMm = bladeKerfMm * placedInSheet.size
                    )
                )
            } else {
                val failed = remaining.removeAt(0)
                unplaced.add(CutPiece(id = "unplaced", label = failed.label, lengthMm = failed.lengthMm, widthMm = failed.widthMm, quantity = 1, colorHex = failed.colorHex))
            }
        }

        val totalSheetArea = usedLayouts.sumOf { it.totalBoardLengthMm * it.totalBoardWidthMm }
        val totalCutArea = usedLayouts.sumOf { sheet -> sheet.placedPieces.sumOf { it.lengthMm * it.widthMm } }
        val totalScrap = usedLayouts.sumOf { it.remainingScrapMm }
        val yield = if (totalSheetArea > 0) (totalCutArea / totalSheetArea) * 100.0 else 0.0

        return CutlistOptimizationResult(
            usedBoards = usedLayouts,
            totalStockLengthMm = usedLayouts.sumOf { it.totalBoardLengthMm },
            totalCutLengthMm = usedLayouts.sumOf { b -> b.placedPieces.sumOf { it.lengthMm } },
            totalKerfLossMm = usedLayouts.sumOf { it.kerfLossMm },
            totalScrapLengthMm = totalScrap,
            yieldPercentage = yield,
            unplacedPieces = unplaced
        )
    }
}

