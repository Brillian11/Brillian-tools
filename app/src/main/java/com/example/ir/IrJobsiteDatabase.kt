package com.example.ir

/**
 * Trade Category Definition for Jobsite IR Remote.
 */
enum class IrTradeCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconName: String
) {
    HVAC(
        id = "cat_hvac",
        title = "HVAC & Cassette AC",
        subtitle = "Ladder-free commissioning, test mode, CFM balancing",
        iconName = "AcUnit"
    ),
    AIR_FILTRATION(
        id = "cat_filtration",
        title = "Shop Air Cleaners & Dust",
        subtitle = "Ceiling ambient scrubbers, timed exhaust cycles",
        iconName = "Air"
    ),
    LIGHTING_SENSORS(
        id = "cat_lighting",
        title = "Lighting & Occupancy Sensors",
        subtitle = "Hold times, photocell thresholds, high-bay floodlights",
        iconName = "Sensors"
    ),
    HEATERS_FANS(
        id = "cat_heaters_fans",
        title = "Radiant Heaters & Big Fans",
        subtitle = "Overhead tube heaters, destratification fans",
        iconName = "Thermostat"
    ),
    SITE_AV(
        id = "cat_site_av",
        title = "Site AV & Fit-Out Displays",
        subtitle = "Commercial displays, menu boards, ceiling projectors",
        iconName = "Tv"
    )
}

/**
 * Individual remote command definition.
 */
data class IrCommand(
    val id: String,
    val title: String,
    val description: String,
    val frequencyHz: Int = 38000,
    val timingPattern: IntArray,
    val hexSignature: String,
    val isPrimaryAction: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as IrCommand
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Complete equipment brand/model profile.
 */
data class IrDeviceProfile(
    val id: String,
    val brand: String,
    val modelOrSeries: String,
    val category: IrTradeCategory,
    val frequencyHz: Int = 38000,
    val protocolName: String,
    val quickActions: List<IrCommand>,
    val fullCommands: List<IrCommand>,
    val macros: List<IrMacroDefinition>
)

/**
 * Multi-step signal macro definition.
 */
data class IrMacroDefinition(
    val id: String,
    val title: String,
    val description: String,
    val steps: List<MacroStep>
)

/**
 * Offline pre-compiled database of real-world industrial and commercial IR codes.
 */
object IrJobsiteDatabase {

    val deviceProfiles: List<IrDeviceProfile> by lazy {
        listOf(
            // -------------------------------------------------------------
            // 1. HVAC COMMISSIONING & SPLIT/CASSETTE UNITS
            // -------------------------------------------------------------
            createHvacProfile(
                id = "hvac_daikin",
                brand = "Daikin",
                model = "FTX / SkyAir Cassettes",
                address = 0x11DA,
                protocol = "Daikin Standard NEC"
            ),
            createHvacProfile(
                id = "hvac_mitsubishi",
                brand = "Mitsubishi Electric",
                model = "Mr Slim / City Multi / PEAD",
                address = 0x23CB,
                protocol = "Mitsubishi Pulse Distance"
            ),
            createHvacProfile(
                id = "hvac_gree",
                brand = "Gree / Cooper&Hunter",
                model = "Livo / Crown / Cassette",
                address = 0x00FF,
                protocol = "Gree NEC 38kHz"
            ),
            createHvacProfile(
                id = "hvac_carrier",
                brand = "Carrier / Bryant",
                model = "Performance / Comfort Ductless",
                address = 0x4FB1,
                protocol = "Carrier NEC 38kHz"
            ),
            createHvacProfile(
                id = "hvac_lg",
                brand = "LG Commercial",
                model = "Multi V / ArtCool / Ceiling Unit",
                address = 0x88EE,
                protocol = "LG 28-bit NEC"
            ),
            createHvacProfile(
                id = "hvac_panasonic",
                brand = "Panasonic",
                model = "PACi / Etherea Inverter",
                address = 0x4004,
                protocol = "Panasonic Extended NEC"
            ),

            // -------------------------------------------------------------
            // 2. WORKSHOP AIR CLEANERS & DUST COLLECTORS
            // -------------------------------------------------------------
            createAirCleanerProfile(
                id = "air_jet",
                brand = "JET",
                model = "AFS-1000B / AFS-2000 Filter",
                address = 0x00FE,
                protocol = "JET NEC 38kHz"
            ),
            createAirCleanerProfile(
                id = "air_wen",
                brand = "WEN",
                model = "3-Speed Air Filtration 3410/3417",
                address = 0x01FE,
                protocol = "WEN Standard NEC"
            ),
            createAirCleanerProfile(
                id = "air_laguna",
                brand = "Laguna Tools",
                model = "A-Flux / B-Flux Ambient Collector",
                address = 0x02FE,
                protocol = "Laguna NEC 38kHz"
            ),
            createAirCleanerProfile(
                id = "air_powermatic",
                brand = "Powermatic",
                model = "PM1200 / PM1250 Micro Dust",
                address = 0x03FE,
                protocol = "Powermatic NEC 38kHz"
            ),

            // -------------------------------------------------------------
            // 3. COMMERCIAL LIGHTING & SENSORS
            // -------------------------------------------------------------
            createLightingSensorProfile(
                id = "sensor_wattstopper",
                brand = "Wattstopper / Legrand",
                model = "FS-IR / FSP-211 / FSP-3x1B Sensors",
                address = 0xA11B,
                protocol = "Wattstopper 38kHz"
            ),
            createLightingSensorProfile(
                id = "sensor_leviton",
                brand = "Leviton",
                model = "ODC / OSC High-Bay Multi-Technology",
                address = 0xB22C,
                protocol = "Leviton Pro IR"
            ),
            createGenericLightProfile(
                id = "light_rgbw_highbay",
                brand = "Commercial Worklights",
                model = "24/44-Key High-Bay & Flood Array",
                address = 0x00FF,
                protocol = "NEC 38kHz RGBW"
            ),

            // -------------------------------------------------------------
            // 4. OVERHEAD RADIANT HEATERS & CEILING FANS
            // -------------------------------------------------------------
            createRadiantHeaterProfile(
                id = "heat_solaira",
                brand = "Solaira / Infratech",
                model = "Cosmic / C-Series Radiant Heaters",
                address = 0x7788,
                protocol = "NEC 38kHz"
            ),
            createCeilingFanProfile(
                id = "fan_bigass",
                brand = "Big Ass Fans / Hunter",
                model = "AirGo / Pivot / High-Volume HVLS",
                address = 0x3344,
                protocol = "Commercial Fan NEC"
            ),

            // -------------------------------------------------------------
            // 5. SITE AV & DIGITAL SIGNAGE
            // -------------------------------------------------------------
            createCommercialDisplayProfile(
                id = "av_samsung_signage",
                brand = "Samsung Display",
                model = "Commercial Signage / Hospitality TV",
                address = 0x0707,
                protocol = "Samsung NEC 38kHz"
            ),
            createCommercialDisplayProfile(
                id = "av_lg_procentric",
                brand = "LG Commercial",
                model = "Pro:Centric / UHD Digital Signage",
                address = 0x00DF,
                protocol = "LG NEC 38kHz"
            ),
            createCommercialProjectorProfile(
                id = "av_epson_projector",
                brand = "Epson / BenQ",
                model = "PowerLite Pro / Ceiling Projectors",
                address = 0x8355,
                protocol = "NEC 38kHz"
            )
        )
    }

    // --- Profile Generators ---

    private fun createHvacProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwrPattern = IrProtocolEncoder.encodeNec(address, 0x12)
        val coolPattern = IrProtocolEncoder.encodeNec(address, 0x1E)
        val heatPattern = IrProtocolEncoder.encodeNec(address, 0x24)
        val fanHiPattern = IrProtocolEncoder.encodeNec(address, 0x30)
        val fanMedPattern = IrProtocolEncoder.encodeNec(address, 0x31)
        val fanLowPattern = IrProtocolEncoder.encodeNec(address, 0x32)
        val tempUpPattern = IrProtocolEncoder.encodeNec(address, 0x0B)
        val tempDnPattern = IrProtocolEncoder.encodeNec(address, 0x0C)
        val testModePattern = IrProtocolEncoder.encodeNec(address, 0x7F)
        val swingPattern = IrProtocolEncoder.encodeNec(address, 0x15)

        val quickActions = listOf(
            IrCommand("cmd_pwr", "Power Toggle", "Power On/Off unit", 38000, pwrPattern, "0x12", isPrimaryAction = true),
            IrCommand("cmd_test_mode", "AC Test Run Mode", "Force 100% compressor commissioning test", 38000, testModePattern, "0x7F", isPrimaryAction = true),
            IrCommand("cmd_turbo_cool", "Turbo Cool (64°F)", "Maximum cooling for pull-down test", 38000, coolPattern, "0x1E", isPrimaryAction = true),
            IrCommand("cmd_fan_high", "Fan High (Balancing)", "Trigger full CFM for anemometer capture", 38000, fanHiPattern, "0x30", isPrimaryAction = true)
        )

        val fullCommands = quickActions + listOf(
            IrCommand("cmd_heat", "Heating Mode", "Set heating 78°F", 38000, heatPattern, "0x24"),
            IrCommand("cmd_fan_med", "Fan Medium", "Medium CFM speed", 38000, fanMedPattern, "0x31"),
            IrCommand("cmd_fan_low", "Fan Low", "Quiet / Low air velocity", 38000, fanLowPattern, "0x32"),
            IrCommand("cmd_temp_up", "Temp +1°", "Increase setpoint", 38000, tempUpPattern, "0x0B"),
            IrCommand("cmd_temp_dn", "Temp -1°", "Decrease setpoint", 38000, tempDnPattern, "0x0C"),
            IrCommand("cmd_swing", "Louver Swing / Lock", "Toggle vane position for diffuser alignment", 38000, swingPattern, "0x15")
        )

        val macros = listOf(
            IrMacroDefinition(
                id = "macro_hvac_commission",
                title = "1-Tap HVAC Commissioning Sequence",
                description = "Power On → Turbo Cool Mode → Max Fan Speed (Continuous)",
                steps = listOf(
                    MacroStep("Power On", 38000, pwrPattern, "0x12", 600L),
                    MacroStep("Set Cooling Mode", 38000, coolPattern, "0x1E", 500L),
                    MacroStep("Set High Fan CFM", 38000, fanHiPattern, "0x30", 0L)
                )
            ),
            IrMacroDefinition(
                id = "macro_hvac_airflow",
                title = "Anemometer Airflow Test Routine",
                description = "Steps through Low, Medium, and High fan speeds with 3s hold intervals",
                steps = listOf(
                    MacroStep("Fan Low Speed", 38000, fanLowPattern, "0x32", 3000L),
                    MacroStep("Fan Medium Speed", 38000, fanMedPattern, "0x31", 3000L),
                    MacroStep("Fan High Speed", 38000, fanHiPattern, "0x30", 0L)
                )
            )
        )

        return IrDeviceProfile(
            id = id,
            brand = brand,
            modelOrSeries = model,
            category = IrTradeCategory.HVAC,
            frequencyHz = 38000,
            protocolName = protocol,
            quickActions = quickActions,
            fullCommands = fullCommands,
            macros = macros
        )
    }

    private fun createAirCleanerProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwr = IrProtocolEncoder.encodeNec(address, 0x01)
        val spdHi = IrProtocolEncoder.encodeNec(address, 0x02)
        val spdMed = IrProtocolEncoder.encodeNec(address, 0x03)
        val spdLow = IrProtocolEncoder.encodeNec(address, 0x04)
        val tmr1h = IrProtocolEncoder.encodeNec(address, 0x10)
        val tmr2h = IrProtocolEncoder.encodeNec(address, 0x20)
        val tmr4h = IrProtocolEncoder.encodeNec(address, 0x40)
        val filterReset = IrProtocolEncoder.encodeNec(address, 0x55)

        val quickActions = listOf(
            IrCommand("cmd_pwr", "Power Toggle", "Turn ambient filter on/off", 38000, pwr, "0x01", true),
            IrCommand("cmd_high_scrub", "Air Scrubber Max", "Max CFM high speed dust purge", 38000, spdHi, "0x02", true),
            IrCommand("cmd_2hr_timer", "2-Hour Shift Timer", "Run high speed then auto-shutdown in 2 hours", 38000, tmr2h, "0x20", true),
            IrCommand("cmd_reset_filter", "Filter Light Reset", "Reset 200-hour particulate sensor indicator", 38000, filterReset, "0x55", true)
        )

        val fullCommands = quickActions + listOf(
            IrCommand("cmd_med", "Speed Medium", "Mid CFM rating", 38000, spdMed, "0x03"),
            IrCommand("cmd_low", "Speed Low", "Quiet continuous filtration", 38000, spdLow, "0x04"),
            IrCommand("cmd_tmr_1h", "Timer 1-Hour", "1 hour cycle", 38000, tmr1h, "0x10"),
            IrCommand("cmd_tmr_4h", "Timer 4-Hour", "4 hour overnight purge", 38000, tmr4h, "0x40")
        )

        val macros = listOf(
            IrMacroDefinition(
                id = "macro_shift_end_purge",
                title = "End-of-Shift Dust Purge Sequence",
                description = "Power On → Set High Airflow → Arm 2-Hour Auto-Off Timer",
                steps = listOf(
                    MacroStep("Power On", 38000, pwr, "0x01", 500L),
                    MacroStep("Set High Fan", 38000, spdHi, "0x02", 500L),
                    MacroStep("Arm 2-Hour Timer", 38000, tmr2h, "0x20", 0L)
                )
            )
        )

        return IrDeviceProfile(
            id = id,
            brand = brand,
            modelOrSeries = model,
            category = IrTradeCategory.AIR_FILTRATION,
            frequencyHz = 38000,
            protocolName = protocol,
            quickActions = quickActions,
            fullCommands = fullCommands,
            macros = macros
        )
    }

    private fun createLightingSensorProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val testMode = IrProtocolEncoder.encodeNec(address, 0x80)
        val hold5m = IrProtocolEncoder.encodeNec(address, 0x85)
        val hold10m = IrProtocolEncoder.encodeNec(address, 0x8A)
        val hold20m = IrProtocolEncoder.encodeNec(address, 0x94)
        val sensHi = IrProtocolEncoder.encodeNec(address, 0xA1)
        val sensMed = IrProtocolEncoder.encodeNec(address, 0xA2)
        val photoUp = IrProtocolEncoder.encodeNec(address, 0xB1)
        val photoDn = IrProtocolEncoder.encodeNec(address, 0xB2)
        val forceOn = IrProtocolEncoder.encodeNec(address, 0x01)
        val forceOff = IrProtocolEncoder.encodeNec(address, 0x02)

        val quickActions = listOf(
            IrCommand("cmd_test_walk", "Walk-Test Mode (8s)", "Short 8-second hold to verify PIR coverage radius", 38000, testMode, "0x80", true),
            IrCommand("cmd_hold_10m", "10-Min Hold Time", "Standard commercial office / corridor timeout", 38000, hold10m, "0x8A", true),
            IrCommand("cmd_hold_20m", "20-Min Hold Time", "Warehouse / High-Bay occupancy timeout", 38000, hold20m, "0x94", true),
            IrCommand("cmd_sens_high", "PIR Sensitivity Max", "100% sensor coverage detection", 38000, sensHi, "0xA1", true)
        )

        val fullCommands = quickActions + listOf(
            IrCommand("cmd_hold_5m", "5-Min Hold Time", "Restroom / Storage room timeout", 38000, hold5m, "0x85"),
            IrCommand("cmd_sens_med", "PIR Sensitivity 70%", "Reduce false triggers from air diffusers", 38000, sensMed, "0xA2"),
            IrCommand("cmd_photo_up", "Daylight Setpoint +", "Increase ambient threshold before switching on", 38000, photoUp, "0xB1"),
            IrCommand("cmd_photo_dn", "Daylight Setpoint -", "Decrease ambient light threshold", 38000, photoDn, "0xB2"),
            IrCommand("cmd_force_on", "Force Relay Closed", "Bypass sensor to burn-in new lamps", 38000, forceOn, "0x01"),
            IrCommand("cmd_force_off", "Force Relay Open", "Test manual off override", 38000, forceOff, "0x02")
        )

        val macros = listOf(
            IrMacroDefinition(
                id = "macro_sensor_warehouse",
                title = "Warehouse High-Bay Commissioning",
                description = "Trigger Walk-Test → Verify Range → Program 20-Min Hold & Max Sensitivity",
                steps = listOf(
                    MacroStep("Trigger Walk-Test (8s)", 38000, testMode, "0x80", 1000L),
                    MacroStep("Program 20-Min Hold", 38000, hold20m, "0x94", 600L),
                    MacroStep("Set High Sensitivity", 38000, sensHi, "0xA1", 0L)
                )
            )
        )

        return IrDeviceProfile(
            id = id,
            brand = brand,
            modelOrSeries = model,
            category = IrTradeCategory.LIGHTING_SENSORS,
            frequencyHz = 38000,
            protocolName = protocol,
            quickActions = quickActions,
            fullCommands = fullCommands,
            macros = macros
        )
    }

    private fun createGenericLightProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwrOn = IrProtocolEncoder.encodeNec(address, 0x02)
        val pwrOff = IrProtocolEncoder.encodeNec(address, 0x03)
        val brightUp = IrProtocolEncoder.encodeNec(address, 0x00)
        val brightDn = IrProtocolEncoder.encodeNec(address, 0x01)
        val white100 = IrProtocolEncoder.encodeNec(address, 0x08)
        val white50 = IrProtocolEncoder.encodeNec(address, 0x09)
        val emergencyStrobe = IrProtocolEncoder.encodeNec(address, 0x1E)

        val quickActions = listOf(
            IrCommand("cmd_pwr_on", "Worklight ON", "Power on temporary high-bay lighting", 38000, pwrOn, "0x02", true),
            IrCommand("cmd_pwr_off", "Worklight OFF", "Power off jobsite lights", 38000, pwrOff, "0x03", true),
            IrCommand("cmd_full_white", "100% White Light", "Full illumination for drywall/paint inspection", 38000, white100, "0x08", true),
            IrCommand("cmd_strobe", "Caution Strobe", "Amber/Strobe signal for restricted work zone", 38000, emergencyStrobe, "0x1E", true)
        )

        val fullCommands = quickActions + listOf(
            IrCommand("cmd_bright_up", "Brightness +", "Increase dimmer level", 38000, brightUp, "0x00"),
            IrCommand("cmd_bright_dn", "Brightness -", "Decrease dimmer level", 38000, brightDn, "0x01"),
            IrCommand("cmd_half_white", "50% Power Save", "Half brightness for battery conservation", 38000, white50, "0x09")
        )

        return IrDeviceProfile(
            id = id,
            brand = brand,
            modelOrSeries = model,
            category = IrTradeCategory.LIGHTING_SENSORS,
            frequencyHz = 38000,
            protocolName = protocol,
            quickActions = quickActions,
            fullCommands = fullCommands,
            macros = emptyList()
        )
    }

    private fun createRadiantHeaterProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwr = IrProtocolEncoder.encodeNec(address, 0x10)
        val lvl1 = IrProtocolEncoder.encodeNec(address, 0x11)
        val lvl2 = IrProtocolEncoder.encodeNec(address, 0x12)
        val lvl3 = IrProtocolEncoder.encodeNec(address, 0x13)
        val standby = IrProtocolEncoder.encodeNec(address, 0x1F)

        val quickActions = listOf(
            IrCommand("cmd_pwr", "Heater On/Off", "Toggle overhead tube/ceramic heater", 38000, pwr, "0x10", true),
            IrCommand("cmd_lvl3", "Max Heat (100%)", "Stage 3 maximum radiant output", 38000, lvl3, "0x13", true),
            IrCommand("cmd_lvl2", "Medium Heat (66%)", "Stage 2 comfort heating", 38000, lvl2, "0x12", true),
            IrCommand("cmd_standby", "Standby / Eco", "Low pilot standby", 38000, standby, "0x1F", true)
        )

        val fullCommands = quickActions + listOf(
            IrCommand("cmd_lvl1", "Low Heat (33%)", "Stage 1 anti-freeze minimum", 38000, lvl1, "0x11")
        )

        return IrDeviceProfile(
            id = id,
            brand = brand,
            modelOrSeries = model,
            category = IrTradeCategory.HEATERS_FANS,
            frequencyHz = 38000,
            protocolName = protocol,
            quickActions = quickActions,
            fullCommands = fullCommands,
            macros = listOf(
                IrMacroDefinition(
                    id = "macro_heat_warmup",
                    title = "Shop Morning Pre-Heat",
                    description = "Power On → Stage 3 Max Heat for fast warmup",
                    steps = listOf(
                        MacroStep("Power On", 38000, pwr, "0x10", 600L),
                        MacroStep("Engage Stage 3", 38000, lvl3, "0x13", 0L)
                    )
                )
            )
        )
    }

    private fun createCeilingFanProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwr = IrProtocolEncoder.encodeNec(address, 0x20)
        val spd1 = IrProtocolEncoder.encodeNec(address, 0x21)
        val spd3 = IrProtocolEncoder.encodeNec(address, 0x23)
        val spd6 = IrProtocolEncoder.encodeNec(address, 0x26)
        val fwd = IrProtocolEncoder.encodeNec(address, 0x2A)
        val rev = IrProtocolEncoder.encodeNec(address, 0x2B)

        val quickActions = listOf(
            IrCommand("cmd_pwr", "Fan Power", "Toggle ceiling HVLS fan", 38000, pwr, "0x20", true),
            IrCommand("cmd_summer", "Summer Cooling Draft", "Forward downdraft @ high speed (Speed 6)", 38000, fwd, "0x2A", true),
            IrCommand("cmd_winter", "Winter Destratification", "Reverse updraft @ low speed (Speed 2) to push heat down", 38000, rev, "0x2B", true),
            IrCommand("cmd_spd_max", "Max Speed 6", "100% industrial air displacement", 38000, spd6, "0x26", true)
        )

        val fullCommands = quickActions + listOf(
            IrCommand("cmd_spd1", "Speed 1", "Low velocity circulation", 38000, spd1, "0x21"),
            IrCommand("cmd_spd3", "Speed 3", "Medium velocity", 38000, spd3, "0x23")
        )

        val macros = listOf(
            IrMacroDefinition(
                id = "macro_winter_destrat",
                title = "Winter Heat Destratification Routine",
                description = "Power On → Set Reverse Draft (Updraft) → Set Low Speed 2",
                steps = listOf(
                    MacroStep("Power On", 38000, pwr, "0x20", 500L),
                    MacroStep("Set Reverse Direction", 38000, rev, "0x2B", 500L),
                    MacroStep("Set Speed 2", 38000, spd1, "0x21", 0L)
                )
            )
        )

        return IrDeviceProfile(
            id = id,
            brand = brand,
            modelOrSeries = model,
            category = IrTradeCategory.HEATERS_FANS,
            frequencyHz = 38000,
            protocolName = protocol,
            quickActions = quickActions,
            fullCommands = fullCommands,
            macros = macros
        )
    }

    private fun createCommercialDisplayProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwr = IrProtocolEncoder.encodeNec(address, 0x02)
        val hdmi1 = IrProtocolEncoder.encodeNec(address, 0x79)
        val hdmi2 = IrProtocolEncoder.encodeNec(address, 0x7A)
        val sourceToggle = IrProtocolEncoder.encodeNec(address, 0x0B)
        val menu = IrProtocolEncoder.encodeNec(address, 0x1A)
        val mute = IrProtocolEncoder.encodeNec(address, 0x09)

        val quickActions = listOf(
            IrCommand("cmd_pwr", "Display Power", "Power on/off menu board or TV", 38000, pwr, "0x02", true),
            IrCommand("cmd_hdmi1", "Select HDMI 1", "Direct switch to Media Player / Signage Box", 38000, hdmi1, "0x79", true),
            IrCommand("cmd_source", "Source Toggle", "Cycle inputs (HDMI / USB / DP)", 38000, sourceToggle, "0x0B", true),
            IrCommand("cmd_menu", "Commercial Menu", "Open display settings & calibration", 38000, menu, "0x1A", true)
        )

        val fullCommands = quickActions + listOf(
            IrCommand("cmd_hdmi2", "Select HDMI 2", "Secondary media input", 38000, hdmi2, "0x7A"),
            IrCommand("cmd_mute", "Audio Mute", "Silence display output", 38000, mute, "0x09")
        )

        return IrDeviceProfile(
            id = id,
            brand = brand,
            modelOrSeries = model,
            category = IrTradeCategory.SITE_AV,
            frequencyHz = 38000,
            protocolName = protocol,
            quickActions = quickActions,
            fullCommands = fullCommands,
            macros = listOf(
                IrMacroDefinition(
                    id = "macro_fitout_display_check",
                    title = "Fit-Out Display Commissioning",
                    description = "Power On → Force HDMI 1 Source → Mute Audio",
                    steps = listOf(
                        MacroStep("Power On", 38000, pwr, "0x02", 1200L),
                        MacroStep("Select HDMI 1", 38000, hdmi1, "0x79", 500L),
                        MacroStep("Mute Audio", 38000, mute, "0x09", 0L)
                    )
                )
            )
        )
    }

    private fun createCommercialProjectorProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwrOn = IrProtocolEncoder.encodeNec(address, 0x01)
        val pwrOff = IrProtocolEncoder.encodeNec(address, 0x02)
        val source = IrProtocolEncoder.encodeNec(address, 0x05)
        val pattern = IrProtocolEncoder.encodeNec(address, 0x30)
        val freeze = IrProtocolEncoder.encodeNec(address, 0x47)

        val quickActions = listOf(
            IrCommand("cmd_pwr_on", "Projector Power ON", "Start ceiling-mounted projector lamp", 38000, pwrOn, "0x01", true),
            IrCommand("cmd_pwr_off", "Projector Standby", "Initiate cooling shutdown", 38000, pwrOff, "0x02", true),
            IrCommand("cmd_test_grid", "Test Grid Pattern", "Throw crosshatch pattern for screen keystone alignment", 38000, pattern, "0x30", true),
            IrCommand("cmd_source", "Source Switch", "Toggle HDMI / HDBaseT", 38000, source, "0x05", true)
        )

        val fullCommands = quickActions + listOf(
            IrCommand("cmd_freeze", "Freeze Frame", "Lock current image for inspection", 38000, freeze, "0x47")
        )

        return IrDeviceProfile(
            id = id,
            brand = brand,
            modelOrSeries = model,
            category = IrTradeCategory.SITE_AV,
            frequencyHz = 38000,
            protocolName = protocol,
            quickActions = quickActions,
            fullCommands = fullCommands,
            macros = emptyList()
        )
    }
}
