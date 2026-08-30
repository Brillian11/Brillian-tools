package com.example.domain.math

import java.util.Locale
import java.util.UUID

enum class DimensionUnit(val symbol: String, val label: String, val toMmMultiplier: Double) {
    CM("cm", "Centimeters (cm)", 10.0),
    MM("mm", "Millimeters (mm)", 1.0);

    fun format(valueMm: Double, includeUnit: Boolean = true): String {
        val converted = valueMm / toMmMultiplier
        val formatted = if (converted % 1.0 == 0.0) {
            "${converted.toInt()}"
        } else {
            String.format(Locale.US, "%.1f", converted)
        }
        return if (includeUnit) "$formatted $symbol" else formatted
    }

    fun fromMm(valueMm: Double): Double = valueMm / toMmMultiplier
    fun toMm(valueInUnit: Double): Double = valueInUnit * toMmMultiplier
}

enum class MaterialType(
    val displayName: String,
    val isSheet: Boolean,
    val defaultLengthMm: Double = 2000.0,
    val defaultWidthMm: Double = 200.0,
    val defaultThicknessMm: Double = 20.0
) {
    TIMBER_BOARD("Solid Board", false, 2000.0, 200.0, 20.0),
    PLYWOOD_SHEET("Plywood / Sheet", true, 2440.0, 1220.0, 18.0),
    BATTEN_FURRING("Batten / Furring", false, 2400.0, 50.0, 25.0),
    STUD_JOIST("Framing Stud 2x4", false, 2438.4, 89.0, 38.0),
    RAFTER_BEAM("Rafter / Beam", false, 3000.0, 150.0, 50.0),
    CUSTOM_PROFILE("Custom Profile", false, 2000.0, 100.0, 20.0)
}

data class StockProfilePreset(
    val name: String,
    val type: MaterialType,
    val lengthMm: Double,
    val widthMm: Double,
    val thicknessMm: Double,
    val description: String
)

data class CutPiece(
    val id: String,
    val label: String,
    val lengthMm: Double,
    val widthMm: Double = 89.0, // Default width (e.g. 2x4 nominal / 89mm)
    val quantity: Int = 1,
    val colorHex: Long = 0xFF6750A4,
    val thicknessMm: Double = 20.0
)

data class StockBoard(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Raw Stock Board",
    val type: MaterialType = MaterialType.TIMBER_BOARD,
    val lengthMm: Double = 2000.0, // 200 cm
    val widthMm: Double = 200.0,   // 20 cm
    val thicknessMm: Double = 20.0, // 2 cm
    val quantityAvailable: Int = 2, // Quantity of this raw board available
    val costPerUnit: Double = 0.0
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
    val isRotated: Boolean = false,
    val thicknessMm: Double = 20.0
)

data class UsedBoardLayout(
    val boardName: String,
    val materialType: MaterialType = MaterialType.TIMBER_BOARD,
    val totalBoardLengthMm: Double,
    val totalBoardWidthMm: Double = 0.0,
    val totalBoardThicknessMm: Double = 20.0,
    val placedPieces: List<PlacedPiece>,
    val remainingScrapMm: Double, // 1D linear scrap mm OR 2D scrap area mm²
    val kerfLossMm: Double,
    val isRippedAcrossWidth: Boolean = false
)

data class CutlistOptimizationResult(
    val usedBoards: List<UsedBoardLayout>,
    val totalStockLengthMm: Double,
    val totalCutLengthMm: Double,
    val totalKerfLossMm: Double,
    val totalScrapLengthMm: Double,
    val yieldPercentage: Double,
    val unplacedPieces: List<CutPiece>,
    val thicknessGroupsCount: Int = 1
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

    val STOCK_PRESETS = listOf(
        StockProfilePreset(
            name = "Solid Mahogany Board",
            type = MaterialType.TIMBER_BOARD,
            lengthMm = 2000.0,
            widthMm = 200.0,
            thicknessMm = 20.0,
            description = "Solid hardwood plank. Can be ripped longitudinally into narrower widths."
        ),
        StockProfilePreset(
            name = "Framing Timber (4×6 cm)",
            type = MaterialType.STUD_JOIST,
            lengthMm = 2000.0,
            widthMm = 60.0,
            thicknessMm = 40.0,
            description = "Solid structural timber batten / stud."
        ),
        StockProfilePreset(
            name = "Framing Stud 2x4",
            type = MaterialType.STUD_JOIST,
            lengthMm = 2438.4,
            widthMm = 89.0,
            thicknessMm = 38.0,
            description = "Standard construction dimensional lumber."
        ),
        StockProfilePreset(
            name = "Plywood Sheet 18mm",
            type = MaterialType.PLYWOOD_SHEET,
            lengthMm = 2440.0,
            widthMm = 1220.0,
            thicknessMm = 18.0,
            description = "Standard 3/4\" cabinet carcass panel sheet."
        ),
        StockProfilePreset(
            name = "Plywood Sheet 12mm",
            type = MaterialType.PLYWOOD_SHEET,
            lengthMm = 2440.0,
            widthMm = 1220.0,
            thicknessMm = 12.0,
            description = "1/2\" backing & drawer panel sheet."
        ),
        StockProfilePreset(
            name = "Roof Rafter 2x8",
            type = MaterialType.RAFTER_BEAM,
            lengthMm = 3657.6,
            widthMm = 184.0,
            thicknessMm = 38.0,
            description = "Structural roof purlin, rafter, or floor joist."
        ),
        StockProfilePreset(
            name = "Ceiling Batten / Furring",
            type = MaterialType.BATTEN_FURRING,
            lengthMm = 2400.0,
            widthMm = 50.0,
            thicknessMm = 25.0,
            description = "Lightweight ceiling strapping, lattice, or tile battens."
        ),
        StockProfilePreset(
            name = "Heavy Timber Post / Beam",
            type = MaterialType.RAFTER_BEAM,
            lengthMm = 3000.0,
            widthMm = 100.0,
            thicknessMm = 100.0,
            description = "Pergola pillar, structural ridge beam, or table leg."
        )
    )

    fun optimize(
        stockBoards: List<StockBoard>,
        requestedCuts: List<CutPiece>,
        bladeKerfMm: Double,
        trimMarginMm: Double = 0.0,
        allowRipCuts: Boolean = true
    ): CutlistOptimizationResult {
        if (stockBoards.isEmpty() || requestedCuts.isEmpty()) {
            return CutlistOptimizationResult(
                usedBoards = emptyList(),
                totalStockLengthMm = 0.0,
                totalCutLengthMm = 0.0,
                totalKerfLossMm = 0.0,
                totalScrapLengthMm = 0.0,
                yieldPercentage = 0.0,
                unplacedPieces = emptyList(),
                thicknessGroupsCount = 0
            )
        }

        // Expand raw stocks into physical boards according to quantity available
        val physicalStocks = mutableListOf<StockBoard>()
        stockBoards.forEach { stock ->
            val qty = if (stock.quantityAvailable > 0) stock.quantityAvailable else 1
            repeat(qty) { idx ->
                physicalStocks.add(stock.copy(name = if (qty > 1) "${stock.name} #${idx + 1}" else stock.name))
            }
        }

        val distinctThicknesses = requestedCuts.map { it.thicknessMm.toInt() }.distinct()
        val allUsedBoards = mutableListOf<UsedBoardLayout>()
        val allUnplaced = mutableListOf<CutPiece>()

        val cutsByThickness = requestedCuts.groupBy { it.thicknessMm }

        for ((thick, cutsInGroup) in cutsByThickness) {
            // Find all matching physical stocks for this thickness (or best matching)
            val matchingStocks = physicalStocks.filter { it.thicknessMm == thick }.toMutableList()
            if (matchingStocks.isEmpty()) {
                // Fallback: match stocks that have thickness >= cut thickness or any stock
                val fallbackStocks = physicalStocks.filter { it.thicknessMm >= thick }.ifEmpty { physicalStocks }
                matchingStocks.addAll(fallbackStocks)
            }

            if (matchingStocks.isEmpty()) {
                // Use a generated stock matching primary
                matchingStocks.add(stockBoards.first().copy(thicknessMm = thick))
            }

            var cutsToPlace = cutsInGroup.toMutableList()

            // Allocate across available matching physical stocks
            val stockIterator = matchingStocks.iterator()
            while (cutsToPlace.isNotEmpty() && stockIterator.hasNext()) {
                val stock = stockIterator.next()
                physicalStocks.remove(stock) // remove from global pool once used

                val is2D = stock.type == MaterialType.PLYWOOD_SHEET ||
                        (allowRipCuts && cutsToPlace.any { it.widthMm < stock.widthMm })

                val result = if (is2D) {
                    optimize2DSingleBoard(stock, cutsToPlace, bladeKerfMm, trimMarginMm)
                } else {
                    optimize1DSingleBoard(stock, cutsToPlace, bladeKerfMm, trimMarginMm)
                }

                if (result.usedBoards.isNotEmpty()) {
                    allUsedBoards.addAll(result.usedBoards)
                    cutsToPlace = result.unplacedPieces.toMutableList()
                }
            }

            // If cuts still remain and no more stock boards available, keep generating instances of primary matching stock or mark as unplaced
            if (cutsToPlace.isNotEmpty()) {
                val templateStock = stockBoards.firstOrNull { it.thicknessMm == thick } ?: stockBoards.first().copy(thicknessMm = thick)
                val is2D = templateStock.type == MaterialType.PLYWOOD_SHEET ||
                        (allowRipCuts && cutsToPlace.any { it.widthMm < templateStock.widthMm })

                var safetyCount = 0
                while (cutsToPlace.isNotEmpty() && safetyCount < 50) {
                    safetyCount++
                    val extraStock = templateStock.copy(name = "${templateStock.name} (Extra #${safetyCount})")
                    val result = if (is2D) {
                        optimize2DSingleBoard(extraStock, cutsToPlace, bladeKerfMm, trimMarginMm)
                    } else {
                        optimize1DSingleBoard(extraStock, cutsToPlace, bladeKerfMm, trimMarginMm)
                    }

                    if (result.usedBoards.isNotEmpty()) {
                        allUsedBoards.addAll(result.usedBoards)
                        if (result.unplacedPieces.size == cutsToPlace.size) {
                            // Cannot fit even in fresh board, mark as unplaced
                            allUnplaced.addAll(cutsToPlace)
                            break
                        }
                        cutsToPlace = result.unplacedPieces.toMutableList()
                    } else {
                        allUnplaced.addAll(cutsToPlace)
                        break
                    }
                }
            }
        }

        val totalStock = allUsedBoards.sumOf { it.totalBoardLengthMm }
        val totalCuts = allUsedBoards.sumOf { board -> board.placedPieces.sumOf { it.lengthMm } }
        val totalKerf = allUsedBoards.sumOf { it.kerfLossMm }
        val totalScrap = allUsedBoards.sumOf { it.remainingScrapMm }

        val totalStockArea = allUsedBoards.sumOf { it.totalBoardLengthMm * if (it.totalBoardWidthMm > 0) it.totalBoardWidthMm else 1.0 }
        val totalCutArea = allUsedBoards.sumOf { b -> b.placedPieces.sumOf { it.lengthMm * if (it.widthMm > 0) it.widthMm else 1.0 } }
        val yield = if (totalStockArea > 0) (totalCutArea / totalStockArea) * 100.0 else 0.0

        return CutlistOptimizationResult(
            usedBoards = allUsedBoards,
            totalStockLengthMm = totalStock,
            totalCutLengthMm = totalCuts,
            totalKerfLossMm = totalKerf,
            totalScrapLengthMm = totalScrap,
            yieldPercentage = yield.coerceIn(0.0, 100.0),
            unplacedPieces = allUnplaced,
            thicknessGroupsCount = distinctThicknesses.size
        )
    }

    private fun optimize1DSingleBoard(
        stock: StockBoard,
        cuts: List<CutPiece>,
        bladeKerfMm: Double,
        trimMarginMm: Double
    ): CutlistOptimizationResult {
        data class Item(val id: String, val label: String, val lengthMm: Double, val colorHex: Long, val thicknessMm: Double)

        val individualPieces = mutableListOf<Item>()
        cuts.forEach { cut ->
            repeat(cut.quantity) { idx ->
                val label = if (cut.quantity > 1) "${cut.label} (#${idx + 1})" else cut.label
                individualPieces.add(Item(cut.id, label, cut.lengthMm, cut.colorHex, cut.thicknessMm))
            }
        }

        // Sort descending
        individualPieces.sortByDescending { it.lengthMm }

        var currentPos = trimMarginMm
        val placedInThisBoard = mutableListOf<PlacedPiece>()
        var kerfLoss = 0.0

        val remainingPieces = individualPieces.toMutableList()
        val it = remainingPieces.iterator()
        while (it.hasNext()) {
            val piece = it.next()
            val pieceLen = piece.lengthMm
            val kerf = if (placedInThisBoard.isNotEmpty()) bladeKerfMm else 0.0
            val spaceNeeded = pieceLen + kerf

            if (currentPos + spaceNeeded <= stock.lengthMm - trimMarginMm) {
                currentPos += kerf
                kerfLoss += kerf

                val start = currentPos
                val end = currentPos + pieceLen
                placedInThisBoard.add(
                    PlacedPiece(
                        pieceLabel = piece.label,
                        lengthMm = pieceLen,
                        widthMm = stock.widthMm,
                        startPositionMm = start,
                        endPositionMm = end,
                        colorHex = piece.colorHex,
                        thicknessMm = piece.thicknessMm
                    )
                )
                currentPos = end
                it.remove()
            }
        }

        val usedLayouts = mutableListOf<UsedBoardLayout>()
        if (placedInThisBoard.isNotEmpty()) {
            val scrap = (stock.lengthMm - trimMarginMm) - currentPos
            usedLayouts.add(
                UsedBoardLayout(
                    boardName = stock.name,
                    materialType = stock.type,
                    totalBoardLengthMm = stock.lengthMm,
                    totalBoardWidthMm = stock.widthMm,
                    totalBoardThicknessMm = stock.thicknessMm,
                    placedPieces = placedInThisBoard,
                    remainingScrapMm = scrap.coerceAtLeast(0.0),
                    kerfLossMm = kerfLoss,
                    isRippedAcrossWidth = false
                )
            )
        }

        // Reconstruct remaining cut pieces by grouping
        val unplacedGrouped = remainingPieces.groupBy { it.id }.map { (id, items) ->
            val first = items.first()
            val orig = cuts.firstOrNull { it.id == id }
            CutPiece(
                id = id,
                label = orig?.label ?: first.label,
                lengthMm = first.lengthMm,
                widthMm = orig?.widthMm ?: stock.widthMm,
                quantity = items.size,
                colorHex = first.colorHex,
                thicknessMm = first.thicknessMm
            )
        }

        return CutlistOptimizationResult(
            usedBoards = usedLayouts,
            totalStockLengthMm = stock.lengthMm,
            totalCutLengthMm = placedInThisBoard.sumOf { it.lengthMm },
            totalKerfLossMm = kerfLoss,
            totalScrapLengthMm = usedLayouts.sumOf { it.remainingScrapMm },
            yieldPercentage = 0.0,
            unplacedPieces = unplacedGrouped
        )
    }

    private fun optimize2DSingleBoard(
        stockSheet: StockBoard,
        cuts: List<CutPiece>,
        bladeKerfMm: Double,
        trimMarginMm: Double
    ): CutlistOptimizationResult {
        data class PanelItem(
            val id: String,
            val label: String,
            val lengthMm: Double,
            val widthMm: Double,
            val colorHex: Long,
            val thicknessMm: Double
        )

        val individualPanels = mutableListOf<PanelItem>()
        cuts.forEach { cut ->
            repeat(cut.quantity) { idx ->
                val label = if (cut.quantity > 1) "${cut.label} (#${idx + 1})" else cut.label
                individualPanels.add(PanelItem(cut.id, label, cut.lengthMm, cut.widthMm, cut.colorHex, cut.thicknessMm))
            }
        }

        individualPanels.sortByDescending { it.lengthMm * it.widthMm }

        val sheetL = stockSheet.lengthMm - (trimMarginMm * 2)

        val placedInSheet = mutableListOf<PlacedPiece>()
        var currentY = trimMarginMm
        var currentShelfH = 0.0
        var currentX = trimMarginMm

        val remaining = individualPanels.toMutableList()
        val it = remaining.iterator()
        while (it.hasNext()) {
            val panel = it.next()
            var pLen = panel.lengthMm
            var pWid = panel.widthMm
            var rotated = false

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
                            isRotated = rotated,
                            thicknessMm = panel.thicknessMm
                        )
                    )

                    currentX = endX
                    if (pWid > currentShelfH) {
                        currentShelfH = pWid
                    }
                    it.remove()
                }
            } else {
                // Next shelf / rip row
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
                            isRotated = rotated,
                            thicknessMm = panel.thicknessMm
                        )
                    )

                    currentX = endX
                    currentShelfH = pWid
                    it.remove()
                }
            }
        }

        val usedLayouts = mutableListOf<UsedBoardLayout>()
        if (placedInSheet.isNotEmpty()) {
            val sheetTotalArea = stockSheet.lengthMm * stockSheet.widthMm
            val cutTotalArea = placedInSheet.sumOf { it.lengthMm * it.widthMm }
            val scrapArea = (sheetTotalArea - cutTotalArea).coerceAtLeast(0.0)

            val hasRipping = placedInSheet.groupBy { it.startYMm }.size > 1

            usedLayouts.add(
                UsedBoardLayout(
                    boardName = stockSheet.name,
                    materialType = stockSheet.type,
                    totalBoardLengthMm = stockSheet.lengthMm,
                    totalBoardWidthMm = stockSheet.widthMm,
                    totalBoardThicknessMm = stockSheet.thicknessMm,
                    placedPieces = placedInSheet,
                    remainingScrapMm = scrapArea,
                    kerfLossMm = bladeKerfMm * placedInSheet.size,
                    isRippedAcrossWidth = hasRipping
                )
            )
        }

        val unplacedGrouped = remaining.groupBy { it.id }.map { (id, items) ->
            val first = items.first()
            val orig = cuts.firstOrNull { it.id == id }
            CutPiece(
                id = id,
                label = orig?.label ?: first.label,
                lengthMm = first.lengthMm,
                widthMm = first.widthMm,
                quantity = items.size,
                colorHex = first.colorHex,
                thicknessMm = first.thicknessMm
            )
        }

        return CutlistOptimizationResult(
            usedBoards = usedLayouts,
            totalStockLengthMm = stockSheet.lengthMm,
            totalCutLengthMm = placedInSheet.sumOf { it.lengthMm },
            totalKerfLossMm = bladeKerfMm * placedInSheet.size,
            totalScrapLengthMm = usedLayouts.sumOf { it.remainingScrapMm },
            yieldPercentage = 0.0,
            unplacedPieces = unplacedGrouped
        )
    }
}



