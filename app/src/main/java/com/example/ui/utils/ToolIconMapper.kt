package com.example.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Foundation
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ToolVisuals(
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
)

object ToolIconMapper {

    fun getVisualsForTool(toolIdOrIconName: String): ToolVisuals {
        return when (toolIdOrIconName) {
            // Woodworking Suite
            "widget_kerf_bending", "Architecture" -> ToolVisuals(
                icon = Icons.Default.Architecture,
                containerColor = Color(0xFFFEF3C7),
                contentColor = Color(0xFFB45309)
            )
            "widget_cutlist_optimizer", "ContentCut" -> ToolVisuals(
                icon = Icons.Default.ContentCut,
                containerColor = Color(0xFFFFEDD5),
                contentColor = Color(0xFFC2410C)
            )
            "widget_dado_step_over", "TableChart" -> ToolVisuals(
                icon = Icons.Default.TableChart,
                containerColor = Color(0xFFFEF9C3),
                contentColor = Color(0xFFA16207)
            )
            "widget_board_footage" -> ToolVisuals(
                icon = Icons.Default.Calculate,
                containerColor = Color(0xFFFEF3C7),
                contentColor = Color(0xFF92400E)
            )
            "widget_stair_layout" -> ToolVisuals(
                icon = Icons.Default.Architecture,
                containerColor = Color(0xFFFFEDD5),
                contentColor = Color(0xFFC2410C)
            )
            "widget_rafter_calculator" -> ToolVisuals(
                icon = Icons.Default.HomeWork,
                containerColor = Color(0xFFECFDF5),
                contentColor = Color(0xFF065F46)
            )
            "widget_compound_miter" -> ToolVisuals(
                icon = Icons.Default.ContentCut,
                containerColor = Color(0xFFF3E8FF),
                contentColor = Color(0xFF6B21A8)
            )
            "widget_wood_moisture" -> ToolVisuals(
                icon = Icons.Default.WbSunny,
                containerColor = Color(0xFFE0F2FE),
                contentColor = Color(0xFF0369A1)
            )
            "widget_joinery_spacing" -> ToolVisuals(
                icon = Icons.Default.TableChart,
                containerColor = Color(0xFFDCFCE7),
                contentColor = Color(0xFF15803D)
            )

            // Electrical & Electronics Suite
            "widget_ohms_law", "Bolt" -> ToolVisuals(
                icon = Icons.Default.Bolt,
                containerColor = Color(0xFFFEF3C7),
                contentColor = Color(0xFFD97706)
            )
            "widget_voltage_drop", "ElectricMeter" -> ToolVisuals(
                icon = Icons.Default.Bolt,
                containerColor = Color(0xFFFFEDD5),
                contentColor = Color(0xFFEA580C)
            )
            "widget_conduit_fill", "Cable" -> ToolVisuals(
                icon = Icons.Default.Straighten,
                containerColor = Color(0xFFE0E7FF),
                contentColor = Color(0xFF4338CA)
            )
            "widget_conduit_bender", "Straighten" -> ToolVisuals(
                icon = Icons.Default.Architecture,
                containerColor = Color(0xFFCCFBF1),
                contentColor = Color(0xFF0F766E)
            )
            "widget_resistor_color_code" -> ToolVisuals(
                icon = Icons.Default.Palette,
                containerColor = Color(0xFFF3E8FF),
                contentColor = Color(0xFF7E22CE)
            )
            "widget_breaker_panel" -> ToolVisuals(
                icon = Icons.Default.FlashOn,
                containerColor = Color(0xFFFEE2E2),
                contentColor = Color(0xFFB91C1C)
            )
            "widget_led_driver", "LightMode" -> ToolVisuals(
                icon = Icons.Default.LightMode,
                containerColor = Color(0xFFFEF9C3),
                contentColor = Color(0xFFA16207)
            )
            "widget_box_fill_capacity", "Inbox" -> ToolVisuals(
                icon = Icons.Default.Category,
                containerColor = Color(0xFFE2E8F0),
                contentColor = Color(0xFF334155)
            )
            "widget_industrial_motor_fla" -> ToolVisuals(
                icon = Icons.Default.PrecisionManufacturing,
                containerColor = Color(0xFFFEF3C7),
                contentColor = Color(0xFFD97706)
            )
            "widget_solar_battery_sizer" -> ToolVisuals(
                icon = Icons.Default.WbSunny,
                containerColor = Color(0xFFFEF08A),
                contentColor = Color(0xFFCA8A04)
            )
            "widget_current_loop_scaling", "Tune" -> ToolVisuals(
                icon = Icons.Default.GraphicEq,
                containerColor = Color(0xFFCCFBF1),
                contentColor = Color(0xFF0F766E)
            )
            "widget_power_factor_correction", "TrendingDown" -> ToolVisuals(
                icon = Icons.Default.Bolt,
                containerColor = Color(0xFFDBEAFE),
                contentColor = Color(0xFF1D4ED8)
            )

            // Maintenance, Plumbing & Housework Suite
            "widget_pipe_sizing" -> ToolVisuals(
                icon = Icons.Default.Water,
                containerColor = Color(0xFFE0F2FE),
                contentColor = Color(0xFF0369A1)
            )
            "widget_hvac_load" -> ToolVisuals(
                icon = Icons.Default.WbSunny,
                containerColor = Color(0xFFFEF3C7),
                contentColor = Color(0xFFD97706)
            )
            "widget_tile_grout" -> ToolVisuals(
                icon = Icons.Default.GridOn,
                containerColor = Color(0xFFDCFCE7),
                contentColor = Color(0xFF15803D)
            )
            "widget_paint_coverage" -> ToolVisuals(
                icon = Icons.Default.Palette,
                containerColor = Color(0xFFFCE7F3),
                contentColor = Color(0xFFBE185D)
            )
            "widget_drywall_stud" -> ToolVisuals(
                icon = Icons.Default.HomeWork,
                containerColor = Color(0xFFFEF3C7),
                contentColor = Color(0xFFB45309)
            )

            // Safety & Compliance Suite
            "widget_safety_checklist", "HealthAndSafety" -> ToolVisuals(
                icon = Icons.Default.HealthAndSafety,
                containerColor = Color(0xFFDCFCE7),
                contentColor = Color(0xFF15803D)
            )
            "widget_chemical_msds", "Science" -> ToolVisuals(
                icon = Icons.Default.Science,
                containerColor = Color(0xFFEFF6FF),
                contentColor = Color(0xFF1D4ED8)
            )

            // Civil Engineering, Earthwork & Masonry Suite
            "widget_concrete_volume", "Layers" -> ToolVisuals(
                icon = Icons.Default.Calculate,
                containerColor = Color(0xFFE2E8F0),
                contentColor = Color(0xFF334155)
            )
            "widget_rebar_estimator", "GridOn" -> ToolVisuals(
                icon = Icons.Default.GridOn,
                containerColor = Color(0xFFE0E7FF),
                contentColor = Color(0xFF3730A3)
            )
            "widget_cut_fill_earthwork", "Terrain" -> ToolVisuals(
                icon = Icons.Default.Terrain,
                containerColor = Color(0xFFFEF3C7),
                contentColor = Color(0xFFB45309)
            )
            "widget_masonry_mortar", "HomeWork" -> ToolVisuals(
                icon = Icons.Default.HomeWork,
                containerColor = Color(0xFFFFEDD5),
                contentColor = Color(0xFFC2410C)
            )
            "widget_slope_drainage", "Water" -> ToolVisuals(
                icon = Icons.Default.Water,
                containerColor = Color(0xFFCFFAFE),
                contentColor = Color(0xFF0E7490)
            )
            "widget_soil_asphalt", "PrecisionManufacturing" -> ToolVisuals(
                icon = Icons.Default.PrecisionManufacturing,
                containerColor = Color(0xFFF1F5F9),
                contentColor = Color(0xFF334155)
            )
            "widget_beam_deflection", "Engineering" -> ToolVisuals(
                icon = Icons.Default.Engineering,
                containerColor = Color(0xFFDBEAFE),
                contentColor = Color(0xFF1D4ED8)
            )
            "widget_earthwork_grade" -> ToolVisuals(
                icon = Icons.Default.Terrain,
                containerColor = Color(0xFFEFE4D6),
                contentColor = Color(0xFF78350F)
            )
            "widget_framing_roofing" -> ToolVisuals(
                icon = Icons.Default.HomeWork,
                containerColor = Color(0xFFE0E7FF),
                contentColor = Color(0xFF4338CA)
            )
            "widget_stationing_cogo", "Navigation" -> ToolVisuals(
                icon = Icons.Default.Navigation,
                containerColor = Color(0xFFE0F2FE),
                contentColor = Color(0xFF0369A1)
            )
            "widget_retaining_wall_sizer", "Foundation" -> ToolVisuals(
                icon = Icons.Default.Foundation,
                containerColor = Color(0xFFFEF3C7),
                contentColor = Color(0xFFB45309)
            )
            "widget_aggregate_sieve", "Grain" -> ToolVisuals(
                icon = Icons.Default.Grain,
                containerColor = Color(0xFFF1F5F9),
                contentColor = Color(0xFF334155)
            )
            "widget_equipment_hauling", "LocalShipping" -> ToolVisuals(
                icon = Icons.Default.LocalShipping,
                containerColor = Color(0xFFFFEDD5),
                contentColor = Color(0xFFC2410C)
            )
            "widget_stormwater_rational", "WaterDrop" -> ToolVisuals(
                icon = Icons.Default.WaterDrop,
                containerColor = Color(0xFFCFFAFE),
                contentColor = Color(0xFF0284C7)
            )

            // Sensors & Hardware Suite
            "widget_digital_level", "ScreenRotation" -> ToolVisuals(
                icon = Icons.Default.ScreenRotation,
                containerColor = Color(0xFFD1FAE5),
                contentColor = Color(0xFF047857)
            )
            "widget_compass", "Explore" -> ToolVisuals(
                icon = Icons.Default.Explore,
                containerColor = Color(0xFFECFDF5),
                contentColor = Color(0xFF059669)
            )
            "widget_decibel_meter", "GraphicEq" -> ToolVisuals(
                icon = Icons.Default.GraphicEq,
                containerColor = Color(0xFFFEE2E2),
                contentColor = Color(0xFFB91C1C)
            )
            "widget_strobe_tachometer", "FlashOn" -> ToolVisuals(
                icon = Icons.Default.FlashOn,
                containerColor = Color(0xFFFEF08A),
                contentColor = Color(0xFFA16207)
            )
            "widget_thermal_camera", "DeviceThermostat" -> ToolVisuals(
                icon = Icons.Default.DeviceThermostat,
                containerColor = Color(0xFFFEE2E2),
                contentColor = Color(0xFFDC2626)
            )
            "widget_usb_endoscope", "Videocam" -> ToolVisuals(
                icon = Icons.Default.Videocam,
                containerColor = Color(0xFFE0F2FE),
                contentColor = Color(0xFF0284C7)
            )
            "widget_vibration_analyzer" -> ToolVisuals(
                icon = Icons.Default.GraphicEq,
                containerColor = Color(0xFFEDE9FE),
                contentColor = Color(0xFF7C3AED)
            )
            "widget_ble_multimeter" -> ToolVisuals(
                icon = Icons.Default.ElectricMeter,
                containerColor = Color(0xFFFEF3C7),
                contentColor = Color(0xFFD97706)
            )
            "widget_sun_path_tracker" -> ToolVisuals(
                icon = Icons.Default.WbSunny,
                containerColor = Color(0xFFFEF9C3),
                contentColor = Color(0xFFCA8A04)
            )
            "widget_barometric_altimeter", "Landscape" -> ToolVisuals(
                icon = Icons.Default.Landscape,
                containerColor = Color(0xFFE0F2FE),
                contentColor = Color(0xFF0369A1)
            )
            "widget_surface_lux_meter", "Lightbulb" -> ToolVisuals(
                icon = Icons.Default.Lightbulb,
                containerColor = Color(0xFFFEF08A),
                contentColor = Color(0xFFA16207)
            )
            "widget_fractional_calc" -> ToolVisuals(
                icon = Icons.Default.Calculate,
                containerColor = Color(0xFFCCFBF1),
                contentColor = Color(0xFF0F766E)
            )
            "widget_ar_measurement", "Camera" -> ToolVisuals(
                icon = Icons.Default.Camera,
                containerColor = Color(0xFFE0F2FE),
                contentColor = Color(0xFF0284C7)
            )
            "widget_ar_area_calculator", "SquareFoot" -> ToolVisuals(
                icon = Icons.Default.SquareFoot,
                containerColor = Color(0xFFD1FAE5),
                contentColor = Color(0xFF059669)
            )
            "widget_jobsite_ir_remote", "SettingsRemote" -> ToolVisuals(
                icon = Icons.Default.ElectricMeter,
                containerColor = Color(0xFFDCFCE7),
                contentColor = Color(0xFF166534)
            )

            // Field Engineering & Meteorology Suite
            "widget_meteorology", "WbSunny" -> ToolVisuals(
                icon = Icons.Default.WbSunny,
                containerColor = Color(0xFFFEF9C3),
                contentColor = Color(0xFFCA8A04)
            )
            "widget_parabolic_focus", "CellTower" -> ToolVisuals(
                icon = Icons.Default.CellTower,
                containerColor = Color(0xFFF1F5F9),
                contentColor = Color(0xFF475569)
            )

            // Material Inventory
            "widget_material_inventory", "Category" -> ToolVisuals(
                icon = Icons.Default.Category,
                containerColor = Color(0xFFE0E7FF),
                contentColor = Color(0xFF3730A3)
            )

            // Utilities & Notes
            "widget_tasks", "CheckCircle" -> ToolVisuals(
                icon = Icons.Default.CheckCircle,
                containerColor = Color(0xFFDCFCE7),
                contentColor = Color(0xFF15803D)
            )
            "widget_timer", "Timer" -> ToolVisuals(
                icon = Icons.Default.Timer,
                containerColor = Color(0xFFF3E8FF),
                contentColor = Color(0xFF7E22CE)
            )
            "widget_unit_converter", "SwapHoriz" -> ToolVisuals(
                icon = Icons.Default.SwapHoriz,
                containerColor = Color(0xFFCFFAFE),
                contentColor = Color(0xFF0E7490)
            )
            "widget_notes", "StickyNote2" -> ToolVisuals(
                icon = Icons.Default.StickyNote2,
                containerColor = Color(0xFFFEF3C7),
                contentColor = Color(0xFFD97706)
            )
            "widget_color_tools", "Palette" -> ToolVisuals(
                icon = Icons.Default.Palette,
                containerColor = Color(0xFFFCE7F3),
                contentColor = Color(0xFFBE185D)
            )
            "widget_expense", "AccountBalanceWallet" -> ToolVisuals(
                icon = Icons.Default.AccountBalanceWallet,
                containerColor = Color(0xFFD1FAE5),
                contentColor = Color(0xFF047857)
            )
            "widget_settings", "Settings" -> ToolVisuals(
                icon = Icons.Default.Settings,
                containerColor = Color(0xFFF3F4F6),
                contentColor = Color(0xFF374151)
            )
            "widget_psychrometric" -> ToolVisuals(
                icon = Icons.Default.DeviceThermostat,
                containerColor = Color(0xFFE0F2FE),
                contentColor = Color(0xFF0369A1)
            )
            "widget_refrigerant" -> ToolVisuals(
                icon = Icons.Default.DeviceThermostat,
                containerColor = Color(0xFFE0F2FE),
                contentColor = Color(0xFF0891B2)
            )
            "widget_duct_sizer" -> ToolVisuals(
                icon = Icons.Default.Grain,
                containerColor = Color(0xFFECFDF5),
                contentColor = Color(0xFF047857)
            )
            "widget_expansion_tank" -> ToolVisuals(
                icon = Icons.Default.WaterDrop,
                containerColor = Color(0xFFEFF6FF),
                contentColor = Color(0xFF1D4ED8)
            )
            "widget_sling_angle" -> ToolVisuals(
                icon = Icons.Default.Architecture,
                containerColor = Color(0xFFFFF7ED),
                contentColor = Color(0xFFC2410C)
            )

            else -> ToolVisuals(
                icon = Icons.Default.CheckCircle,
                containerColor = Color(0xFFE2E8F0),
                contentColor = Color(0xFF475569)
            )
        }
    }

    fun getIconByName(iconName: String): ImageVector {
        return when (iconName) {
            "Architecture" -> Icons.Default.Architecture
            "ContentCut" -> Icons.Default.ContentCut
            "TableChart" -> Icons.Default.TableChart
            "Engineering" -> Icons.Default.Engineering
            "Layers" -> Icons.Default.Layers
            "Terrain" -> Icons.Default.Terrain
            "HomeWork" -> Icons.Default.HomeWork
            "GridOn" -> Icons.Default.GridOn
            "Water" -> Icons.Default.Water
            "PrecisionManufacturing" -> Icons.Default.PrecisionManufacturing
            "ScreenRotation" -> Icons.Default.ScreenRotation
            "Explore" -> Icons.Default.Explore
            "GraphicEq" -> Icons.Default.GraphicEq
            "FlashOn" -> Icons.Default.FlashOn
            "Calculate" -> Icons.Default.Calculate
            "Camera" -> Icons.Default.Camera
            "SquareFoot" -> Icons.Default.SquareFoot
            "WbSunny" -> Icons.Default.WbSunny
            "CellTower" -> Icons.Default.CellTower
            "Category" -> Icons.Default.Category
            "CheckCircle" -> Icons.Default.CheckCircle
            "Timer" -> Icons.Default.Timer
            "SwapHoriz" -> Icons.Default.SwapHoriz
            "StickyNote2" -> Icons.Default.StickyNote2
            "Palette" -> Icons.Default.Palette
            "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
            "DeviceThermostat" -> Icons.Default.DeviceThermostat
            "Videocam" -> Icons.Default.Videocam
            "ElectricMeter" -> Icons.Default.ElectricMeter
            "Landscape" -> Icons.Default.Landscape
            "Lightbulb" -> Icons.Default.Lightbulb
            "HealthAndSafety" -> Icons.Default.HealthAndSafety
            "Science" -> Icons.Default.Science
            "Settings" -> Icons.Default.Settings
            "Navigation" -> Icons.Default.Navigation
            "Foundation" -> Icons.Default.Foundation
            "Grain" -> Icons.Default.Grain
            "LocalShipping" -> Icons.Default.LocalShipping
            "WaterDrop" -> Icons.Default.WaterDrop
            else -> Icons.Default.CheckCircle
        }
    }

    val AVAILABLE_ICONS = listOf(
        "Architecture", "ContentCut", "TableChart", "Engineering",
        "Layers", "Terrain", "HomeWork", "GridOn", "Water", "PrecisionManufacturing",
        "ScreenRotation", "Explore", "GraphicEq", "FlashOn", "Calculate",
        "Camera", "SquareFoot", "WbSunny", "CellTower", "HealthAndSafety", "Science",
        "DeviceThermostat", "Videocam", "ElectricMeter", "Landscape", "Lightbulb",
        "Category", "CheckCircle", "Timer", "SwapHoriz",
        "StickyNote2", "Palette", "AccountBalanceWallet", "Settings",
        "Navigation", "Foundation", "Grain", "LocalShipping", "WaterDrop"
    )

    val PRESET_BACKGROUND_COLORS = listOf(
        "" to "Default Theme",
        "#F1F5F9" to "Slate Light",
        "#FEF3C7" to "Warm Amber",
        "#ECFDF5" to "Emerald Mist",
        "#E0F2FE" to "Sky Blue",
        "#EEF2FF" to "Indigo Glow",
        "#F3E8FF" to "Purple Dusk",
        "#FCE7F3" to "Rose Quartz",
        "#FFEDD5" to "Sunset Coral",
        "#1E293B" to "Dark Slate",
        "#0F172A" to "Midnight Black"
    )

    val PRESET_STROKE_COLORS = listOf(
        "" to "Default Outline",
        "#CBD5E1" to "Slate Border",
        "#3B82F6" to "Blue Accent",
        "#10B981" to "Emerald Accent",
        "#F59E0B" to "Amber Accent",
        "#EF4444" to "Red Accent",
        "#8B5CF6" to "Violet Accent",
        "#EC4899" to "Pink Accent",
        "#000000" to "Solid Black"
    )

    val THUMBNAIL_PATTERNS = listOf(
        "none" to "Minimalist (None)",
        "gradient" to "Linear Gradient Header",
        "accent_banner" to "Bold Stripe Banner",
        "dots" to "Geometric Dot Matrix",
        "glow" to "Soft Glow Lens"
    )
}
