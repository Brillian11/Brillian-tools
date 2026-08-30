package com.example.ui.screens.tools

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import com.example.domain.model.ColorMatchResult
import com.example.domain.model.GeneratedObjectColor
import com.example.domain.model.PaintCatalogRepository
import com.example.domain.model.PaintColorItem
import com.example.domain.model.PaletteStyle
import com.example.domain.model.SiteLightingTime
import com.example.domain.model.SurfaceObject
import com.example.domain.model.SurfaceRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.pow

enum class LightingCondition(
    val title: String,
    val tempK: String,
    val redFactor: Float,
    val greenFactor: Float,
    val blueFactor: Float,
    val description: String
) {
    DIRECT_SUNLIGHT("Direct Daylight", "6500K", 1.0f, 1.0f, 1.0f, "Bright natural outdoor daylight"),
    WARM_INDOR("Warm Indoor", "2700K", 1.15f, 0.92f, 0.78f, "Incandescent / warm LED room lamp"),
    COOL_OFFICE("Cool White LED", "4000K", 0.95f, 1.02f, 1.10f, "Standard office white LED light"),
    SHADOW_NIGHT("Shaded / Night", "Dusk", 0.82f, 0.86f, 0.95f, "Outdoor shade or dim porch lighting")
}

enum class DisplayCalibrationProfile(
    val title: String,
    val gamma: Float,
    val luminance: Float,
    val description: String
) {
    STANDARD_SRGB("Standard sRGB Screen", 1.0f, 1.0f, "Default uncalibrated device display"),
    D65_STUDIO_SWATCH("D65 Paper Swatch Match", 1.08f, 0.95f, "Calibrated to match physical paint color sample cards"),
    MATTE_TIMBER_SHEEN("Matte Wood Satin Finish", 0.95f, 0.90f, "Simulates low-glare satin oil/polyurethane wood finish"),
    HIGH_CONTRAST_OUTDOOR("High-Vis Outdoor Swatch", 1.18f, 1.12f, "High luminance for outdoor sunlight on site")
}

data class TradePaletteCombination(
    val title: String,
    val tradeCategory: String,     // "Woodworker", "House Work", "Building Work", "Technical Work"
    val dominantColorHex: String,   // 60%
    val dominantName: String,
    val secondaryColorHex: String,  // 30%
    val secondaryName: String,
    val accentColorHex: String,     // 10%
    val accentName: String,
    val combinationRule: String,    // "60-30-10 Rule"
    val recommendation: String
)

data class ColorDevToolsUiState(
    val selectedTab: Int = 0, // 0: Catalog & Calibration, 1: Multi-Surface Area Palette Generator, 2: Site Photo & Time Analyzer
    
    // Catalog & Calibration State
    val selectedBrandFilter: String = "ALL", // "ALL", "Nippon Paint", "Mowilex", "Avitex", "Propan", "Dulux"
    val selectedTradeFilter: String = "ALL", // "ALL", "Woodworker", "House Work", "Building Work", "Technical Work"
    val searchQuery: String = "",
    val selectedPaint: PaintColorItem = PaintCatalogRepository.FULL_PAINT_CATALOG.first(),
    
    // Real Life Appearance Simulation
    val selectedLighting: LightingCondition = LightingCondition.DIRECT_SUNLIGHT,
    val selectedSurface: String = "Wall Plaster", // "Wall Plaster", "Timber Grain", "Concrete Surface", "Gloss Metal"
    
    // Screen Calibration
    val screenBrightness: Float = 0.85f,
    val gammaCorrection: Float = 1.0f,
    val luminanceScale: Float = 1.0f,
    val selectedCalibrationProfile: DisplayCalibrationProfile = DisplayCalibrationProfile.D65_STUDIO_SWATCH,

    // Contrast Check
    val contrastRatioWhite: String = "1.1:1",
    val contrastRatioBlack: String = "18.5:1",

    // Color Code Outputs
    val rgbCode: String = "RGB(244, 243, 237)",
    val cmykCode: String = "CMYK(0%, 0%, 3%, 4%)",

    // Feature 1: Multi-Surface Area Palette Generator
    val surfaces: List<SurfaceObject> = listOf(
        SurfaceObject("1", "Main Living Room Wall", SurfaceRole.MAIN_SURFACE, widthMeters = 8.0, heightMeters = 3.0),
        SurfaceObject("2", "TV Feature Accent Wall", SurfaceRole.ACCENT_SURFACE, widthMeters = 3.5, heightMeters = 3.0),
        SurfaceObject("3", "Door Trim & Molding", SurfaceRole.TRIM_FRAME, widthMeters = 1.5, heightMeters = 2.2),
        SurfaceObject("4", "Ceiling Soffit", SurfaceRole.CEILING_SOFFIT, widthMeters = 8.0, heightMeters = 1.0)
    ),
    val selectedPaletteStyle: PaletteStyle = PaletteStyle.WARM_NEUTRAL_VILLA,
    val generatedMultiPalette: List<GeneratedObjectColor> = emptyList(),

    // Feature 2: Site Photo & Time Analyzer
    val siteLightingTime: SiteLightingTime = SiteLightingTime.MIDDAY_SUN,
    val samplePhotoPreset: String = "Plaster Wall Living Room",
    val rawSampledHex: String = "#7A4B29",
    val debiasedTrueHex: String = "#7A4B29",
    val topMatches: List<ColorMatchResult> = emptyList()
)

class ColorDevToolsViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ColorDevToolsUiState())
    val uiState: StateFlow<ColorDevToolsUiState> = _uiState.asStateFlow()

    val paintCatalog: List<PaintColorItem> get() = PaintCatalogRepository.FULL_PAINT_CATALOG

    val tradeCombinations = listOf(
        TradePaletteCombination(
            title = "Jati Teak Wood & Dark Metal Joinery",
            tradeCategory = "Woodworker",
            dominantColorHex = "#7A4B29",
            dominantName = "Mowilex WS-600 Teak (60%)",
            secondaryColorHex = "#2B2D2F",
            secondaryName = "Matte Black Steel (30%)",
            accentColorHex = "#D4AF37",
            accentName = "Brass Fastener Gold (10%)",
            combinationRule = "60-30-10 Dominant Organic Wood Balance",
            recommendation = "Ideal for custom timber furniture, ironwood metal legs, and brass hardware accents."
        ),
        TradePaletteCombination(
            title = "Mahogany & Warm Cream Studio",
            tradeCategory = "Woodworker",
            dominantColorHex = "#5C1D18",
            dominantName = "Mowilex WS-700 Mahogany (60%)",
            secondaryColorHex = "#F5EBE1",
            secondaryName = "Cream Wall Trim (30%)",
            accentColorHex = "#8B5A2B",
            accentName = "Antique Bronze Hardware (10%)",
            combinationRule = "Classic Heritage Cabinetry Palette",
            recommendation = "Combines rich reddish mahogany woodstain with warm cream backdrop for luxury interiors."
        ),
        TradePaletteCombination(
            title = "Modern Tropical Villa Living",
            tradeCategory = "House Work",
            dominantColorHex = "#FAF8F2",
            dominantName = "Propan EE-4000 Silk White (60%)",
            secondaryColorHex = "#8A929A",
            secondaryName = "Avitex 042 Executive Grey (30%)",
            accentColorHex = "#B84328",
            accentName = "Ultraproof Roof Terracotta (10%)",
            combinationRule = "60-30-10 Residential Design Standard",
            recommendation = "Off-white living room walls with slate grey window frames and terracotta decorative pots."
        ),
        TradePaletteCombination(
            title = "Nordic Calm Interior",
            tradeCategory = "House Work",
            dominantColorHex = "#F4F0E6",
            dominantName = "Dulux Morning Dew (60%)",
            secondaryColorHex = "#C4A47C",
            secondaryName = "Dulux Wheatfield Tan (30%)",
            accentColorHex = "#2C505E",
            accentName = "Deep Ocean Accent (10%)",
            combinationRule = "Warm Neutral Monochromatic Harmony",
            recommendation = "Soft warm background with organic wheat wood tones and a single teal feature wall."
        ),
        TradePaletteCombination(
            title = "Exposed Structural Concrete & Facade",
            tradeCategory = "Building Work",
            dominantColorHex = "#787D82",
            dominantName = "Propan Stone Care Concrete Grey (60%)",
            secondaryColorHex = "#42494D",
            secondaryName = "Nippon Weatherbond Charcoal (30%)",
            accentColorHex = "#F2B828",
            accentName = "Nippon Golden Safety Edge (10%)",
            combinationRule = "Civil Structural Safety Standard",
            recommendation = "Exposed concrete columns, dark grey aluminum composite panels, and safety yellow stairs."
        ),
        TradePaletteCombination(
            title = "OSHA Workshop & Field Safety",
            tradeCategory = "Technical Work",
            dominantColorHex = "#23272A",
            dominantName = "Industrial Steel Graphite (60%)",
            secondaryColorHex = "#FFD600",
            secondaryName = "Propan Epoxy Safety Yellow (30%)",
            accentColorHex = "#D0121B",
            accentName = "Avian Emergency Red Conduit (10%)",
            combinationRule = "ANSI Z535 / OSHA Hazard Identification",
            recommendation = "Dark steel machine bodies, high-visibility walkway lines, and emergency stop valves."
        ),
        TradePaletteCombination(
            title = "TACO HPL Royal Oak & Charcoal Duco Luxury",
            tradeCategory = "Woodworker",
            dominantColorHex = "#B89063",
            dominantName = "TACO HPL Royal Oak (60%)",
            secondaryColorHex = "#1A1A1A",
            secondaryName = "Danapaint Matte Black Duco (30%)",
            accentColorHex = "#D4C5A1",
            accentName = "Kertasive Champagne Gold (10%)",
            combinationRule = "Modern Luxury Custom Cabinetry Combo",
            recommendation = "Beautiful matte oak HPL drawers framed by black lacquered duco trims and champagne gold accent pulls."
        ),
        TradePaletteCombination(
            title = "Minimalist Kertasive Wood & Steel Wrap",
            tradeCategory = "Woodworker",
            dominantColorHex = "#C99B66",
            dominantName = "Kertasive Natural Oak Film (60%)",
            secondaryColorHex = "#A1A4A6",
            secondaryName = "Kertasive Brushed Steel Metallic (30%)",
            accentColorHex = "#FFFFFF",
            accentName = "Glossy Solid White (10%)",
            combinationRule = "Thermoformed Interior Wrap Harmony",
            recommendation = "Warm timber-wrapped wall panels, industrial brushed steel trims, and pure gloss white desks."
        ),
        TradePaletteCombination(
            title = "TACO Walnut Flooring & Warm Cream Living",
            tradeCategory = "House Work",
            dominantColorHex = "#F0E8D9",
            dominantName = "Decosheet Warm Cream Wall (60%)",
            secondaryColorHex = "#5C4033",
            secondaryName = "TACO Walnut Flooring Vinyl (30%)",
            accentColorHex = "#964B38",
            accentName = "Dulux Tuscan Clay Terracotta (10%)",
            combinationRule = "60-30-10 Cozy Living Space Scheme",
            recommendation = "Bright cream wrapped wall stickers, luxury rich walnut floor planks, and warm terracotta accent cushions."
        )
    )

    init {
        selectPaint(paintCatalog.first())
        recalculateMultiSurfacePalette()
        analyzeSiteColor(_uiState.value.rawSampledHex)
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    // --- Tab 0: Catalog & Calibration ---

    fun selectPaint(paint: PaintColorItem) {
        val colorHex = paint.hex
        val rgb = hexToRgb(colorHex)
        val cmyk = rgbToCmyk(rgb.first, rgb.second, rgb.third)
        
        val parsedColor = try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (_: Exception) { Color.White }

        val whiteRatio = calculateContrastRatio(parsedColor, Color.White)
        val blackRatio = calculateContrastRatio(parsedColor, Color.Black)

        _uiState.value = _uiState.value.copy(
            selectedPaint = paint,
            contrastRatioWhite = String.format("%.1f:1", whiteRatio),
            contrastRatioBlack = String.format("%.1f:1", blackRatio),
            rgbCode = "RGB(${rgb.first}, ${rgb.second}, ${rgb.third})",
            cmykCode = "CMYK(${cmyk.first}%, ${cmyk.second}%, ${cmyk.third}%, ${cmyk.fourth}%)"
        )

        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "COLOR",
                title = "Selected Paint ${paint.brand}",
                summary = "${paint.code} (${paint.name}) - ${paint.hex}",
                value = 1.0
            )
        }
    }

    fun setBrandFilter(brand: String) {
        _uiState.value = _uiState.value.copy(selectedBrandFilter = brand)
    }

    fun setTradeFilter(trade: String) {
        _uiState.value = _uiState.value.copy(selectedTradeFilter = trade)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setLightingCondition(lighting: LightingCondition) {
        _uiState.value = _uiState.value.copy(selectedLighting = lighting)
    }

    fun setSurface(surface: String) {
        _uiState.value = _uiState.value.copy(selectedSurface = surface)
    }

    fun setScreenBrightness(brightness: Float) {
        _uiState.value = _uiState.value.copy(screenBrightness = brightness.coerceIn(0.1f, 1.0f))
    }

    fun setGammaCorrection(gamma: Float) {
        _uiState.value = _uiState.value.copy(gammaCorrection = gamma.coerceIn(0.7f, 1.5f))
    }

    fun setLuminanceScale(scale: Float) {
        _uiState.value = _uiState.value.copy(luminanceScale = scale.coerceIn(0.7f, 1.4f))
    }

    fun setCalibrationProfile(profile: DisplayCalibrationProfile) {
        _uiState.value = _uiState.value.copy(
            selectedCalibrationProfile = profile,
            gammaCorrection = profile.gamma,
            luminanceScale = profile.luminance
        )
    }

    // --- Tab 1: Multi-Surface Area Palette Generator ---

    fun setPaletteStyle(style: PaletteStyle) {
        _uiState.value = _uiState.value.copy(selectedPaletteStyle = style)
        recalculateMultiSurfacePalette()
    }

    fun addSurfaceObject(name: String, role: SurfaceRole, width: Double, height: Double) {
        val newObj = SurfaceObject(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "New Surface Object" },
            role = role,
            widthMeters = width.coerceAtLeast(0.5),
            heightMeters = height.coerceAtLeast(0.5)
        )
        _uiState.value = _uiState.value.copy(surfaces = _uiState.value.surfaces + newObj)
        recalculateMultiSurfacePalette()
    }

    fun removeSurfaceObject(id: String) {
        if (_uiState.value.surfaces.size <= 1) return // Keep at least one
        _uiState.value = _uiState.value.copy(surfaces = _uiState.value.surfaces.filterNot { it.id == id })
        recalculateMultiSurfacePalette()
    }

    fun updateSurfaceDimensions(id: String, width: Double, height: Double) {
        val updated = _uiState.value.surfaces.map { s ->
            if (s.id == id) s.copy(widthMeters = width.coerceAtLeast(0.5), heightMeters = height.coerceAtLeast(0.5))
            else s
        }
        _uiState.value = _uiState.value.copy(surfaces = updated)
        recalculateMultiSurfacePalette()
    }

    fun updateSurfaceRole(id: String, role: SurfaceRole) {
        val updated = _uiState.value.surfaces.map { s ->
            if (s.id == id) s.copy(role = role) else s
        }
        _uiState.value = _uiState.value.copy(surfaces = updated)
        recalculateMultiSurfacePalette()
    }

    private fun recalculateMultiSurfacePalette() {
        val generated = PaintCatalogRepository.generateMultiObjectPalette(
            objects = _uiState.value.surfaces,
            style = _uiState.value.selectedPaletteStyle
        )
        _uiState.value = _uiState.value.copy(generatedMultiPalette = generated)
    }

    // --- Tab 2: Site Photo & Time Analyzer ---

    fun setSiteLightingTime(time: SiteLightingTime) {
        _uiState.value = _uiState.value.copy(siteLightingTime = time)
        analyzeSiteColor(_uiState.value.rawSampledHex)
    }

    fun setPhotoPreset(presetName: String) {
        val rawHex = when (presetName) {
            "Plaster Wall Living Room" -> "#E6DEC8"
            "Indonesian Jati Wood Board" -> "#7A4B29"
            "Civil Concrete Facade" -> "#787D82"
            "Workshop Safety Steel" -> "#F2B828"
            else -> "#7A4B29"
        }
        _uiState.value = _uiState.value.copy(samplePhotoPreset = presetName, rawSampledHex = rawHex)
        analyzeSiteColor(rawHex)
    }

    fun sampleRawColor(rawHex: String) {
        _uiState.value = _uiState.value.copy(rawSampledHex = rawHex)
        analyzeSiteColor(rawHex)
    }

    private fun analyzeSiteColor(rawHex: String) {
        val debiasedHex = PaintCatalogRepository.debiasSiteColor(rawHex, _uiState.value.siteLightingTime)
        val matches = PaintCatalogRepository.findNearestPaints(debiasedHex, topCount = 3)
        _uiState.value = _uiState.value.copy(
            debiasedTrueHex = debiasedHex,
            topMatches = matches
        )
    }

    // --- Utility Methods ---

    private fun hexToRgb(hex: String): Triple<Int, Int, Int> = PaintCatalogRepository.hexToRgb(hex)

    private fun rgbToCmyk(r: Int, g: Int, b: Int): Quadruple<Int, Int, Int, Int> {
        val rF = r / 255.0
        val gF = g / 255.0
        val bF = b / 255.0

        val k = 1.0 - maxOf(rF, maxOf(gF, bF))
        if (k >= 0.99) return Quadruple(0, 0, 0, 100)

        val c = (1.0 - rF - k) / (1.0 - k)
        val m = (1.0 - gF - k) / (1.0 - k)
        val y = (1.0 - bF - k) / (1.0 - k)

        return Quadruple(
            (c * 100).toInt().coerceIn(0, 100),
            (m * 100).toInt().coerceIn(0, 100),
            (y * 100).toInt().coerceIn(0, 100),
            (k * 100).toInt().coerceIn(0, 100)
        )
    }

    private fun calculateContrastRatio(c1: Color, c2: Color): Double {
        val l1 = getLuminance(c1)
        val l2 = getLuminance(c2)
        val brighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        return (brighter + 0.05) / (darker + 0.05)
    }

    private fun getLuminance(color: Color): Double {
        val argb = color.toArgb()
        val r = (argb shr 16 and 0xFF) / 255.0
        val g = (argb shr 8 and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0

        val rL = if (r <= 0.03928) r / 12.92 else ((r + 0.055) / 1.055).pow(2.4)
        val gL = if (g <= 0.03928) g / 12.92 else ((g + 0.055) / 1.055).pow(2.4)
        val bL = if (b <= 0.03928) b / 12.92 else ((b + 0.055) / 1.055).pow(2.4)

        return 0.2126 * rL + 0.7152 * gL + 0.0722 * bL
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
