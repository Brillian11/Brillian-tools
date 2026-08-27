package com.example.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

data class PaintColorItem(
    val brand: String,            // "Nippon Paint", "Mowilex", "Avitex", "Propan", "Dulux"
    val code: String,             // "NP OW 1081 P", "WS-500", etc.
    val name: String,             // "Off White", "Walnut Woodstain", etc.
    val hex: String,              // "#F4F3ED"
    val finishType: String,       // "Interior Wall", "Exterior Facade", "Woodstain", "Metal Gloss", "Stone Care"
    val tradeCategory: String,    // "Woodworker", "House Work", "Building Work", "Technical Work"
    val description: String,
    val lrv: Int = 80             // Light Reflectance Value 0-100
)

data class SurfaceObject(
    val id: String,
    val name: String,             // e.g. "Main Wall", "Accent Feature Wall", "Door Trim", "Ceiling"
    val role: SurfaceRole,
    val widthMeters: Double = 6.0,
    val heightMeters: Double = 3.0,
    val quantity: Int = 1
) {
    val areaSqM: Double get() = widthMeters * heightMeters * quantity
}

enum class SurfaceRole(val title: String, val targetPropText: String, val description: String) {
    MAIN_SURFACE("Main Surface (Dominant)", "50% - 70% Area", "Largest area needing comfortable high-reflection background tones"),
    ACCENT_SURFACE("Accent / Feature Wall", "20% - 35% Area", "Medium area providing focal point, rich or contrasting color"),
    TRIM_FRAME("Trim, Doors & Molding", "5% - 15% Area", "Small structural framing elements needing crisp or dark accent tones"),
    CEILING_SOFFIT("Ceiling / Roof Soffit", "5% - 15% Area", "Top overhead plane requiring high-opacity bright clean paint")
}

enum class PaletteStyle(val title: String, val description: String) {
    WARM_NEUTRAL_VILLA("Modern Warm Neutral Villa", "Soft off-whites, warm greys, and terracotta or gold accents"),
    JATI_TIMBER_STEEL("Indonesian Jati Timber & Dark Steel", "Authentic Teak woodstain, matte black, and brass or cream trim"),
    NORDIC_CALM_PASTEL("Nordic Calm & Tropical Pastels", "Crisp morning dew white, wheat tan, and deep ocean blue feature wall"),
    STRUCTURAL_FACADE_GREY("Civil Architectural Facade Grey", "Weatherproof slate grey, exposed concrete, and safety accent"),
    INDUSTRIAL_SAFETY_HIGHVIS("Industrial Workshop & OSHA Safety", "Graphite steel, high-vis safety yellow, and red conduit markings")
}

enum class SiteLightingTime(
    val title: String,
    val timeRange: String,
    val tempK: Int,
    val redShift: Float,
    val greenShift: Float,
    val blueShift: Float,
    val description: String
) {
    MORNING_DAWN("Morning Dawn", "06:00 - 09:00", 4500, 1.02f, 1.00f, 0.96f, "Soft cool sunlight with mild blue sky ambient"),
    MIDDAY_SUN("Midday Direct Sun", "11:00 - 14:00", 6500, 1.00f, 1.00f, 1.00f, "True white direct sunlight, neutral color rendering"),
    GOLDEN_HOUR("Golden Hour Sunset", "16:00 - 18:00", 3000, 1.25f, 0.90f, 0.70f, "Strong amber/warm orange sunset glow"),
    DUSK_SHADE("Dusk & Shaded Site", "18:00 - 19:00", 7500, 0.85f, 0.95f, 1.18f, "Deep shade or sky shadow with cool blue bias"),
    WARM_INDOOR_LED("Warm Indoor Lamp", "20:00+ Indoor", 2700, 1.20f, 0.88f, 0.72f, "Warm yellow incandescent or 2700K room lighting"),
    COOL_OFFICE_LED("Cool White Work LED", "20:00+ Worksite", 4000, 0.95f, 1.02f, 1.08f, "Standard worksite cool white LED illumination")
}

data class ColorMatchResult(
    val paint: PaintColorItem,
    val matchPercentage: Double,
    val deltaE: Double
)

data class GeneratedObjectColor(
    val surfaceObject: SurfaceObject,
    val areaPercentage: Double,
    val recommendedPaint: PaintColorItem,
    val paintLitresNeeded: Double,
    val designReasoning: String
)

data class PaletteCandidates(
    val dominant: PaintColorItem,
    val accent: PaintColorItem,
    val trim: PaintColorItem,
    val ceiling: PaintColorItem
)

object PaintCatalogRepository {

    val FULL_PAINT_CATALOG = listOf(
        // Nippon Paint
        PaintColorItem("Nippon Paint", "NP OW 1081 P", "Off White", "#F4F3ED", "Interior Wall", "House Work", "Clean, warm off-white for residential living spaces", lrv = 88),
        PaintColorItem("Nippon Paint", "NP YO 1188 D", "Golden Yellow Safety", "#F2B828", "Safety Coating", "Technical Work", "High-visibility OSHA safety yellow for machinery & steps", lrv = 62),
        PaintColorItem("Nippon Paint", "NP BGG 1601 T", "Ocean Breeze", "#78A0A8", "Interior Wall", "House Work", "Calming soft teal for modern bedroom & bathroom walls", lrv = 54),
        PaintColorItem("Nippon Paint", "NP N 1989 P", "Slate Charcoal", "#42494D", "Exterior Weatherbond", "Building Work", "Durable dark charcoal for modern building exterior facades", lrv = 22),
        PaintColorItem("Nippon Paint", "NP R 1269 D", "Signal Red Emergency", "#C8232B", "Industrial Gloss", "Technical Work", "Emergency fire fighting equipment & hazard markings", lrv = 28),
        PaintColorItem("Nippon Paint", "NP N 2004 T", "Executive Grey", "#959B9E", "Exterior Concrete", "Building Work", "Neutral balanced grey for structural columns & lintels", lrv = 48),
        PaintColorItem("Nippon Paint", "NP YO 1092 T", "Harvest Gold Trim", "#D4A853", "Wood Satin Finish", "Woodworker", "Warm gold timber accent for door frames & moldings", lrv = 55),
        PaintColorItem("Nippon Paint", "NP N 3042 P", "Warm Sand Beige", "#E6DEC8", "Interior Silk", "House Work", "Cozy beige tone for interior living and dining walls", lrv = 76),

        // Mowilex
        PaintColorItem("Mowilex", "E-2000", "Snow White Emulsion", "#FAFAFA", "Interior Wall", "House Work", "Pure high-opacity white for interior ceilings and walls", lrv = 92),
        PaintColorItem("Mowilex", "E-1000", "Cream Elegance", "#F5EBE1", "Interior Wall", "House Work", "Warm ivory cream for residential interiors", lrv = 85),
        PaintColorItem("Mowilex", "E-500", "Sunny Gold Accent", "#FFD000", "Weathercoat Facade", "House Work", "Vibrant exterior wall accent color", lrv = 70),
        PaintColorItem("Mowilex", "E-400", "Executive Slate", "#5A6266", "Weathercoat Facade", "Building Work", "Weatherproof dark grey for perimeter walls & facades", lrv = 30),
        PaintColorItem("Mowilex", "WS-500", "Woodstain Walnut", "#4B2F1D", "Woodstain Outdoor", "Woodworker", "Rich dark walnut finish highlighting natural wood grain", lrv = 18),
        PaintColorItem("Mowilex", "WS-600", "Woodstain Teak / Jati", "#7A4B29", "Woodstain Classic", "Woodworker", "Authentic Indonesian Jati / Teak warm amber woodstain", lrv = 28),
        PaintColorItem("Mowilex", "WS-700", "Woodstain Mahogany", "#5C1D18", "Woodstain Deep", "Woodworker", "Deep reddish-brown mahogany stain for fine furniture", lrv = 15),
        PaintColorItem("Mowilex", "WS-300", "Woodstain Clear Oak / Pine", "#C89D66", "Woodstain Light", "Woodworker", "Natural golden oak tone for light timber & plywood", lrv = 50),

        // Avitex / Avian Brands
        PaintColorItem("Avitex", "Avitex 050", "Super White", "#FFFFFF", "Interior Wall", "House Work", "Popular bright white interior wall paint", lrv = 95),
        PaintColorItem("Avitex", "Avitex 042", "Executive Grey", "#8A929A", "Exterior Facade", "Building Work", "Standard civil construction facade grey", lrv = 45),
        PaintColorItem("Avitex", "Avitex 093", "Cream Warmth", "#F2E6CE", "Interior Wall", "House Work", "Soft warm cream wall finish", lrv = 81),
        PaintColorItem("Avian Brands", "Avian Kayu 650", "Traffic Yellow", "#F5BE00", "Metal & Wood Enamel", "Technical Work", "High-gloss metal enamel for electrical boxes & structural steel", lrv = 68),
        PaintColorItem("Avian Brands", "Avian Kayu 600", "Signal Safety Red", "#D0121B", "Metal Enamel", "Technical Work", "Emergency valve & piping safety enamel", lrv = 25),
        PaintColorItem("Avian Brands", "Avian Kayu 630", "Bright Electrical Blue", "#0055A5", "Metal Enamel", "Technical Work", "Electrical conduit & fresh water line indicator", lrv = 32),
        PaintColorItem("Avian Brands", "Avian Kayu 670", "Industrial Gas Green", "#008248", "Metal Enamel", "Technical Work", "Gas pipeline & safety conduit enamel", lrv = 30),
        PaintColorItem("Avian Brands", "Avian Clear 110", "Gloss Varnish Timber", "#D9AD73", "Wood Varnish", "Woodworker", "High gloss protective lacquer for woodwork", lrv = 58),

        // Propan
        PaintColorItem("Propan", "DC-300", "Decorcoat Desert Sand", "#E2D5C3", "Interior Wall", "House Work", "Earthy warm sand tone for interior decor", lrv = 72),
        PaintColorItem("Propan", "UP-960", "Ultraproof Roof Terracotta", "#B84328", "Roofing Waterproofing", "Building Work", "Heavy-duty elastomer roof tile & gutter sealant red", lrv = 26),
        PaintColorItem("Propan", "EE-4000", "Eco Emulsion Silk White", "#FAF8F2", "Interior Wall", "House Work", "Low VOC silk off-white interior emulsion", lrv = 90),
        PaintColorItem("Propan", "PU-91 Walnut", "Aqua Polyurethane Walnut", "#3D2415", "PU Woodstain", "Woodworker", "Water-based polyurethane finish for high-wear flooring & tables", lrv = 14),
        PaintColorItem("Propan", "PU-91 Teak", "Aqua Polyurethane Natural Teak", "#8C572B", "PU Woodstain", "Woodworker", "UV-resistant outdoor timber deck finish", lrv = 32),
        PaintColorItem("Propan", "SC-80 Stone", "Propan Stone Care Natural Grey", "#787D82", "Stone & Masonry", "Building Work", "Protective coat for natural stone & exposed concrete", lrv = 40),
        PaintColorItem("Propan", "Epoxy Floor", "Industrial Floor Safety Yellow", "#FFD600", "Epoxy Floor", "Technical Work", "Heavy impact epoxy coating for workshop floors", lrv = 70),

        // Dulux / Catylac
        PaintColorItem("Dulux", "40YY 83/150", "Morning Dew White", "#F4F0E6", "Interior Emulsion", "House Work", "Luminous off-white with subtle warm undertone", lrv = 87),
        PaintColorItem("Dulux", "30BB 11/151", "Slate Facade Grey", "#3D464D", "Exterior Facade", "Building Work", "Modern architectural facade dark grey", lrv = 20),
        PaintColorItem("Dulux", "10YY 35/196", "Wheatfield Natural", "#C4A47C", "Interior Wall", "House Work", "Natural organic wheat tone", lrv = 52),
        PaintColorItem("Dulux", "90BG 17/120", "Deep Ocean Accent", "#2C505E", "Feature Wall", "House Work", "High-contrast feature wall dark teal blue", lrv = 24),
        PaintColorItem("Dulux", "10YR 13/383", "Tuscan Clay Terracotta", "#964B38", "Exterior Trim", "Building Work", "Rustic terracotta tile & brick accent", lrv = 28)
    )

    fun hexToRgb(hex: String): Triple<Int, Int, Int> {
        val clean = if (hex.startsWith("#")) hex.substring(1) else hex
        return try {
            val num = clean.toLong(16)
            Triple(
                ((num shr 16) and 0xFF).toInt(),
                ((num shr 8) and 0xFF).toInt(),
                (num and 0xFF).toInt()
            )
        } catch (_: Exception) { Triple(255, 255, 255) }
    }

    fun rgbToHex(r: Int, g: Int, b: Int): String {
        val rC = r.coerceIn(0, 255)
        val gC = g.coerceIn(0, 255)
        val bC = b.coerceIn(0, 255)
        return String.format("#%02X%02X%02X", rC, gC, bC)
    }

    /**
     * Calculates weighted Euclidean distance between two hex colors.
     */
    fun colorDistance(hex1: String, hex2: String): Double {
        val (r1, g1, b1) = hexToRgb(hex1)
        val (r2, g2, b2) = hexToRgb(hex2)
        val rMean = (r1 + r2) / 2.0
        val dr = (r1 - r2).toDouble()
        val dg = (g1 - g2).toDouble()
        val db = (b1 - b2).toDouble()

        val weightR = 2.0 + rMean / 256.0
        val weightG = 4.0
        val weightB = 2.0 + (255.0 - rMean) / 256.0

        return sqrt(weightR * dr * dr + weightG * dg * dg + weightB * db * db)
    }

    /**
     * Finds nearest paint codes from full catalog.
     */
    fun findNearestPaints(targetHex: String, topCount: Int = 3): List<ColorMatchResult> {
        val maxDist = 765.0 // Max possible weighted distance approx
        return FULL_PAINT_CATALOG.map { paint ->
            val dist = colorDistance(targetHex, paint.hex)
            val matchPct = (1.0 - (dist / maxDist)).coerceIn(0.0, 1.0) * 100.0
            ColorMatchResult(paint = paint, matchPercentage = matchPct, deltaE = dist)
        }.sortedBy { it.deltaE }.take(topCount)
    }

    /**
     * De-biases site raw photo color based on site lighting time condition.
     */
    fun debiasSiteColor(rawHex: String, siteLighting: SiteLightingTime): String {
        val (rRaw, gRaw, bRaw) = hexToRgb(rawHex)
        val rTrue = (rRaw / siteLighting.redShift).toInt().coerceIn(0, 255)
        val gTrue = (gRaw / siteLighting.greenShift).toInt().coerceIn(0, 255)
        val bTrue = (bRaw / siteLighting.blueShift).toInt().coerceIn(0, 255)
        return rgbToHex(rTrue, gTrue, bTrue)
    }

    /**
     * Generates a multi-surface paint palette for a set of objects based on surface areas & style.
     */
    fun generateMultiObjectPalette(
        objects: List<SurfaceObject>,
        style: PaletteStyle
    ): List<GeneratedObjectColor> {
        if (objects.isEmpty()) return emptyList()

        val totalArea = objects.sumOf { it.areaSqM }.coerceAtLeast(1.0)

        // Select paint candidate pools based on style
        val candidates = when (style) {
            PaletteStyle.WARM_NEUTRAL_VILLA -> PaletteCandidates(
                FULL_PAINT_CATALOG.first { it.code == "NP OW 1081 P" }, // Off White
                FULL_PAINT_CATALOG.first { it.code == "NP N 3042 P" },  // Warm Sand Beige
                FULL_PAINT_CATALOG.first { it.code == "NP YO 1092 T" }, // Harvest Gold Trim
                FULL_PAINT_CATALOG.first { it.code == "E-2000" }         // Snow White
            )
            PaletteStyle.JATI_TIMBER_STEEL -> PaletteCandidates(
                FULL_PAINT_CATALOG.first { it.code == "WS-600" },       // Mowilex Jati Teak
                FULL_PAINT_CATALOG.first { it.code == "NP N 1989 P" },  // Slate Charcoal
                FULL_PAINT_CATALOG.first { it.code == "Avian Clear 110" },// Timber Varnish
                FULL_PAINT_CATALOG.first { it.code == "E-1000" }         // Cream Elegance
            )
            PaletteStyle.NORDIC_CALM_PASTEL -> PaletteCandidates(
                FULL_PAINT_CATALOG.first { it.code == "40YY 83/150" },  // Morning Dew
                FULL_PAINT_CATALOG.first { it.code == "90BG 17/120" },  // Deep Ocean Accent
                FULL_PAINT_CATALOG.first { it.code == "10YY 35/196" },  // Wheatfield
                FULL_PAINT_CATALOG.first { it.code == "Avitex 050" }     // Super White
            )
            PaletteStyle.STRUCTURAL_FACADE_GREY -> PaletteCandidates(
                FULL_PAINT_CATALOG.first { it.code == "SC-80 Stone" },   // Stone Concrete Grey
                FULL_PAINT_CATALOG.first { it.code == "30BB 11/151" },  // Slate Facade Grey
                FULL_PAINT_CATALOG.first { it.code == "10YR 13/383" },  // Tuscan Clay Terracotta
                FULL_PAINT_CATALOG.first { it.code == "NP N 2004 T" }    // Executive Grey
            )
            PaletteStyle.INDUSTRIAL_SAFETY_HIGHVIS -> PaletteCandidates(
                FULL_PAINT_CATALOG.first { it.code == "NP N 1989 P" },  // Charcoal Steel
                FULL_PAINT_CATALOG.first { it.code == "Epoxy Floor" },   // Safety Yellow Epoxy
                FULL_PAINT_CATALOG.first { it.code == "Avian Kayu 600" },// Signal Safety Red
                FULL_PAINT_CATALOG.first { it.code == "E-2000" }         // White Ceiling
            )
        }

        return objects.map { obj ->
            val areaPct = (obj.areaSqM / totalArea) * 100.0

            // Estimate coverage: standard emulsion / paint is ~10-12 sq meters per litre per coat (2 coats)
            val coverageSqMPerLitre = 5.5 // net 2-coat coverage per litre
            val litresNeeded = obj.areaSqM / coverageSqMPerLitre

            val (paint, reason) = when (obj.role) {
                SurfaceRole.MAIN_SURFACE -> Pair(
                    candidates.dominant,
                    "Dominant surface (${String.format("%.1f", areaPct)}% area). Selected high-LRV comfortable base tone for visual balance."
                )
                SurfaceRole.ACCENT_SURFACE -> Pair(
                    candidates.accent,
                    "Accent focal surface (${String.format("%.1f", areaPct)}% area). Selected contrasting tone to create architectural depth."
                )
                SurfaceRole.TRIM_FRAME -> Pair(
                    candidates.trim,
                    "Structural frame / trim (${String.format("%.1f", areaPct)}% area). Selected durable protective accent finish."
                )
                SurfaceRole.CEILING_SOFFIT -> Pair(
                    candidates.ceiling,
                    "Ceiling plane (${String.format("%.1f", areaPct)}% area). Selected high-opacity bright white overhead finish."
                )
            }

            GeneratedObjectColor(
                surfaceObject = obj,
                areaPercentage = areaPct,
                recommendedPaint = paint,
                paintLitresNeeded = litresNeeded,
                designReasoning = reason
            )
        }
    }
}
