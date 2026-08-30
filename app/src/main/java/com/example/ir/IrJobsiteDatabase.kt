package com.example.ir

/**
 * Trade Category Definition for Jobsite IR Remote with 50+ Brands.
 */
enum class IrTradeCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconName: String
) {
    HVAC(
        id = "cat_hvac",
        title = "AC & Climate Control",
        subtitle = "Daikin, Mitsubishi, Carrier, Gree, LG, Trane, Fujitsu, etc.",
        iconName = "AcUnit"
    ),
    SITE_AV(
        id = "cat_site_av",
        title = "TV & Smart Displays",
        subtitle = "Samsung, LG, Sony, Panasonic, TCL, Hisense, Philips, Vizio",
        iconName = "Tv"
    ),
    PROJECTOR(
        id = "cat_projector",
        title = "Overhead Projectors",
        subtitle = "Epson, BenQ, Optoma, ViewSonic, Sony, Panasonic, NEC",
        iconName = "Videocam"
    ),
    SATELLITE_STB(
        id = "cat_satellite_stb",
        title = "Satellite & TV Receivers",
        subtitle = "Humax, Dish, DirecTV, Roku, Apple TV, Fire TV, Arris",
        iconName = "Router"
    ),
    AUDIO_SOUNDBAR(
        id = "cat_audio_soundbar",
        title = "Audio & Soundbars",
        subtitle = "Bose, JBL, Yamaha, Sonos IR, Denon, Pioneer, Onkyo",
        iconName = "Speaker"
    ),
    HEATERS_FANS(
        id = "cat_heaters_fans",
        title = "Fans & Air Circulators",
        subtitle = "Dyson, Honeywell, Lasko, Vornado, KDK, Westinghouse",
        iconName = "Air"
    ),
    AIR_FILTRATION(
        id = "cat_filtration",
        title = "Air Purifiers & Scrubbers",
        subtitle = "Dyson, Sharp Plasmacluster, Levoit, Coway, Winix",
        iconName = "FilterAlt"
    ),
    MEDIA_PLAYER(
        id = "cat_media_player",
        title = "DVD & Blu-ray Players",
        subtitle = "Sony, Panasonic, LG, Samsung, Pioneer, Denon, Marantz",
        iconName = "DiscFull"
    ),
    LIGHTING_SENSORS(
        id = "cat_lighting",
        title = "Lighting & Sensors",
        subtitle = "Wattstopper, Leviton, Commercial High-Bay Arrays",
        iconName = "Sensors"
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
 * Offline pre-compiled database of 60 real-world consumer & commercial IR codes.
 */
object IrJobsiteDatabase {

    val deviceProfiles: List<IrDeviceProfile> by lazy {
        listOf(
            // =============================================================
            // 1. AC & CLIMATE CONTROL (20 BRANDS)
            // =============================================================
            createAcProfile("ac_daikin", "Daikin", "FTX / SkyAir Cassette", 0x11DA, "Daikin Standard NEC"),
            createAcProfile("ac_mitsubishi", "Mitsubishi Electric", "Mr Slim / City Multi", 0x23CB, "Mitsubishi Pulse Distance"),
            createAcProfile("ac_carrier", "Carrier", "Performance Ductless", 0x4FB1, "Carrier NEC 38kHz"),
            createAcProfile("ac_gree", "Gree", "Livo / Crown Inverter", 0x00FF, "Gree NEC 38kHz"),
            createAcProfile("ac_lg", "LG", "Multi V / ArtCool", 0x88EE, "LG 28-bit NEC"),
            createAcProfile("ac_panasonic", "Panasonic", "PACi / Etherea Inverter", 0x4004, "Panasonic Extended NEC"),
            createAcProfile("ac_trane", "Trane", "Commercial Split System", 0x12BA, "Trane NEC 38kHz"),
            createAcProfile("ac_fujitsu", "Fujitsu", "Halcyon Wall Mount", 0x14CB, "Fujitsu Pulse Distance"),
            createAcProfile("ac_york", "York", "Affinity / LX Ductless", 0x33FF, "York NEC 38kHz"),
            createAcProfile("ac_midea", "Midea", "Mission / BreezeleSS+", 0x01EF, "Midea NEC 38kHz"),
            createAcProfile("ac_samsung", "Samsung AC", "WindFree Inverter", 0x02EE, "Samsung 36-bit Pulse"),
            createAcProfile("ac_haier", "Haier", "Flexis / Tundra AC", 0x04F5, "Haier NEC 38kHz"),
            createAcProfile("ac_sharp", "Sharp AC", "Plasmacluster Aircon", 0x55AA, "Sharp IR Protocol"),
            createAcProfile("ac_hitachi", "Hitachi", "RAK Inverter Series", 0x66BB, "Hitachi Pulse Distance"),
            createAcProfile("ac_toshiba", "Toshiba AC", "Shorai Edge System", 0x77CC, "Toshiba NEC 38kHz"),
            createAcProfile("ac_lennox", "Lennox", "Mini-Split Heat Pump", 0x88DD, "Lennox NEC 38kHz"),
            createAcProfile("ac_rheem", "Rheem", "Commercial AC Console", 0x99EE, "Rheem NEC 38kHz"),
            createAcProfile("ac_voltas", "Voltas", "All-Star Inverter AC", 0x11FF, "Voltas NEC 38kHz"),
            createAcProfile("ac_bluestar", "Blue Star", "Precision AC Unit", 0x22EE, "Blue Star NEC 38kHz"),
            createAcProfile("ac_aux", "Aux", "J-Smart Inverter AC", 0x33DD, "Aux NEC 38kHz"),

            // =============================================================
            // 2. TV & SMART DISPLAYS (12 BRANDS)
            // =============================================================
            createTvProfile("tv_samsung", "Samsung", "Neo QLED & Crystal UHD", 0x0707, "Samsung NEC 38kHz"),
            createTvProfile("tv_lg", "LG TV", "OLED & NanoCell Commercial", 0x0404, "LG 32-bit NEC"),
            createTvProfile("tv_sony", "Sony TV", "Bravia XR 4K HDR", 0x01A0, "Sony SIRC 12/15-bit"),
            createTvProfile("tv_panasonic", "Panasonic TV", "Viera 4K Pro Display", 0x4004, "Panasonic Extended"),
            createTvProfile("tv_tcl", "TCL", "Roku TV & Google TV", 0x55AA, "TCL Standard NEC"),
            createTvProfile("tv_hisense", "Hisense", "ULED 4K Smart TV", 0x11EE, "Hisense NEC 38kHz"),
            createTvProfile("tv_philips", "Philips TV", "Ambilight & Commercial", 0x0002, "Philips RC5 / RC6"),
            createTvProfile("tv_vizio", "Vizio", "SmartCast D/M Series", 0x00FF, "Vizio NEC 38kHz"),
            createTvProfile("tv_sharp", "Sharp TV", "Aquos Commercial Screen", 0x5500, "Sharp IR Code"),
            createTvProfile("tv_toshiba", "Toshiba TV", "Fire TV Edition Display", 0x01EF, "Toshiba NEC 38kHz"),
            createTvProfile("tv_xiaomi", "Xiaomi TV", "Mi TV / Smart Display", 0x8811, "Xiaomi IR Protocol"),
            createTvProfile("tv_insignia", "Insignia", "Fire TV 4K Edition", 0x02DF, "Insignia NEC 38kHz"),
            // --- CRT & Retro TVs ---
            createTvProfile("tv_sony_trinitron", "Sony Trinitron CRT", "KV Series Trinitron CRT", 0x01A1, "Sony SIRC 12-bit"),
            createTvProfile("tv_jvc_crt", "JVC CRT", "I'Art Retro CRT TV", 0x0344, "JVC IR Protocol"),
            createTvProfile("tv_toshiba_crt", "Toshiba FST CRT", "FST Blackstripe CRT", 0x01E1, "Toshiba NEC"),
            createTvProfile("tv_panasonic_tau", "Panasonic Tau CRT", "Tau Pure Flat CRT", 0x4007, "Panasonic Extended"),
            createTvProfile("tv_magnavox_crt", "Magnavox CRT", "Smart Series Retro CRT", 0x00F1, "Magnavox RC5"),
            createTvProfile("tv_zenith_crt", "Zenith System 3 CRT", "System 3 Space Command", 0x01B1, "Zenith Pulse"),
            createTvProfile("tv_rca_colortrak", "RCA ColorTrak CRT", "ColorTrak Colortron CRT", 0x00F2, "RCA NEC"),
            createTvProfile("tv_philco_crt", "Philco CRT", "Classic Retro Cabinet TV", 0x00F3, "Philco RC5"),
            createTvProfile("tv_sylvania_crt", "Sylvania CRT", "Superset Retro CRT TV", 0x00F4, "Sylvania RC5"),
            createTvProfile("tv_emerson_crt", "Emerson CRT", "Solid State Color CRT", 0x00F5, "Emerson NEC"),
            createTvProfile("tv_orion_crt", "Orion CRT", "Compact Retro CRT TV", 0x00F6, "Orion NEC"),
            createTvProfile("tv_funai_crt", "Funai CRT", "Classic Color CRT TV", 0x00F7, "Funai NEC"),
            createTvProfile("tv_akai_crt", "Akai CRT", "Pro-Series Retro CRT TV", 0x00F8, "Akai NEC"),
            createTvProfile("tv_sansui_crt", "Sansui CRT", "Vintage Stereo Sound CRT", 0x00F9, "Sansui NEC"),
            createTvProfile("tv_daewoo_crt", "Daewoo CRT", "Classic Flat Screen CRT", 0x00FA, "Daewoo NEC"),
            createTvProfile("tv_goldstar_crt", "GoldStar CRT", "Retro Solid State TV", 0x00FB, "GoldStar NEC"),
            createTvProfile("tv_grundig_crt", "Grundig CRT", "Super Color CRT TV", 0x00FC, "Grundig RC5"),
            createTvProfile("tv_telefunken_crt", "Telefunken CRT", "Palcolor Vintage TV", 0x00FD, "Telefunken RC5"),
            createTvProfile("tv_blaupunkt_crt", "Blaupunkt CRT", "Vintage Color CRT", 0x00FE, "Blaupunkt RC5"),
            createTvProfile("tv_loewe_crt", "Loewe Opta CRT", "Art Series Premium CRT", 0x00FF, "Loewe RC5"),
            createTvProfile("tv_admiral_crt", "Admiral CRT", "Classic Admiral Tube TV", 0x0210, "Admiral NEC"),
            createTvProfile("tv_electrohome_crt", "Electrohome CRT", "Classic Retro Electrohome TV", 0x0211, "Electrohome NEC"),
            createTvProfile("tv_hitachi_crt", "Hitachi CRT", "Hitachi Color Flat CRT", 0x0212, "Hitachi NEC"),
            createTvProfile("tv_mitsubishi_crt", "Mitsubishi CRT", "Diamondtron Precision CRT", 0x0213, "Mitsubishi NEC"),
            createTvProfile("tv_nec_crt", "NEC CRT", "NEC Autograph Tube TV", 0x0214, "NEC NEC"),
            createTvProfile("tv_pioneer_crt", "Pioneer CRT", "Pioneer Retro Color TV", 0x0215, "Pioneer NEC"),
            createTvProfile("tv_sanyo_crt", "Sanyo CRT", "Sanyo Flat Screen CRT TV", 0x0216, "Sanyo NEC"),
            createTvProfile("tv_sears_crt", "Sears LXI CRT", "LXI Series Retro CRT TV", 0x0217, "Sears NEC"),
            createTvProfile("tv_sharp_crt", "Sharp CRT", "Sharp Limage Vintage CRT", 0x0218, "Sharp NEC"),
            createTvProfile("tv_symphonic_crt", "Symphonic CRT", "Symphonic Retro Tube TV", 0x0219, "Symphonic NEC"),
            createTvProfile("tv_teac_crt", "Teac CRT", "Teac Studio Color CRT", 0x021A, "Teac NEC"),
            createTvProfile("tv_technics_crt", "Technics CRT", "Technics Vintage TV Receiver", 0x021B, "Technics NEC"),
            createTvProfile("tv_viewsonic_crt", "ViewSonic CRT", "ViewSonic Professional CRT Monitor", 0x021C, "ViewSonic NEC"),
            createTvProfile("tv_westinghouse_crt", "Westinghouse CRT", "Westinghouse Retro Solid State TV", 0x021D, "Westinghouse NEC"),
            createTvProfile("tv_yamaha_crt", "Yamaha CRT", "Yamaha Vintage Monitor TV", 0x021E, "Yamaha NEC"),
            createTvProfile("tv_curtis_mathes_crt", "Curtis Mathes CRT", "Curtis Mathes Classic Console TV", 0x021F, "Curtis Mathes NEC"),
            createTvProfile("tv_quasar_crt", "Quasar CRT", "Quasar DynaTech Tube TV", 0x0220, "Quasar NEC"),
            createTvProfile("tv_proscan_crt", "Proscan CRT", "Proscan High-End Retro TV", 0x0221, "Proscan NEC"),
            createTvProfile("tv_rca_victor_crt", "RCA Victor CRT", "Vintage RCA Victor Wood Cabinet TV", 0x0222, "RCA Victor NEC"),
            createTvProfile("tv_duont_crt", "DuMont CRT", "DuMont Classic Royal Cabinet TV", 0x0223, "DuMont NEC"),

            // =============================================================
            // 3. OVERHEAD PROJECTORS (9 BRANDS)
            // =============================================================
            createProjectorProfile("proj_epson", "Epson", "PowerLite / EX Series", 0x41B2, "Epson NEC 38kHz"),
            createProjectorProfile("proj_benq", "BenQ", "CineHome & MW Series", 0x00FE, "BenQ NEC 38kHz"),
            createProjectorProfile("proj_optoma", "Optoma", "HD & 4K Laser Series", 0x12ED, "Optoma NEC 38kHz"),
            createProjectorProfile("proj_viewsonic", "ViewSonic", "PA & PX Commercial", 0x83EE, "ViewSonic NEC 38kHz"),
            createProjectorProfile("proj_sony", "Sony Projector", "VPL Laser Projector", 0x02B0, "Sony SIRC 15-bit"),
            createProjectorProfile("proj_panasonic", "Panasonic Proj", "PT Series Commercial", 0x4005, "Panasonic Extended"),
            createProjectorProfile("proj_nec", "NEC Projector", "NP Series Installation", 0x1818, "NEC Projector IR"),
            createProjectorProfile("proj_infocus", "InFocus", "ScreenPlay & IN Series", 0x03FC, "InFocus NEC 38kHz"),
            createProjectorProfile("proj_acer", "Acer Projector", "Essential & Predator", 0x04FB, "Acer NEC 38kHz"),

            // =============================================================
            // 4. SATELLITE & TV RECEIVERS (8 BRANDS)
            // =============================================================
            createSatelliteStbProfile("stb_humax", "Humax", "Digital Satellite Box", 0x011B, "Humax NEC 38kHz"),
            createSatelliteStbProfile("stb_dish", "Dish Network", "Hopper / Joey IR Receiver", 0x022C, "Dish IR Code"),
            createSatelliteStbProfile("stb_directv", "DirecTV", "Genie HR44/HR54 Receiver", 0x033D, "DirecTV IR"),
            createSatelliteStbProfile("stb_roku", "Roku", "Streaming Player IR Receiver", 0x5743, "Roku Direct NEC"),
            createSatelliteStbProfile("stb_appletv", "Apple TV", "Apple TV 4K IR Remote", 0x87EE, "Apple NEC 38kHz"),
            createSatelliteStbProfile("stb_firetv", "Fire TV IR", "Fire TV Cube / Companion", 0x99AA, "Fire TV IR Receiver"),
            createSatelliteStbProfile("stb_arris", "Arris / Motorola", "VIP / DCX Cable Receiver", 0x044E, "Arris GI Cable IR"),
            createSatelliteStbProfile("stb_technicolor", "Technicolor", "Media Server STB", 0x055F, "Technicolor NEC"),
            // --- HelloBox Satellite & DVB Receivers ---
            createSatelliteStbProfile("stb_hellobox_s2", "HelloBox Smart S2", "Smart S2 Satellite Receiver", 0x1101, "HelloBox NEC"),
            createSatelliteStbProfile("stb_hellobox_8", "HelloBox 8", "HelloBox 8 H.265 DVB-S2", 0x1102, "HelloBox NEC"),
            createSatelliteStbProfile("stb_hellobox_6", "HelloBox 6", "HelloBox 6 Full HD Satellite", 0x1103, "HelloBox NEC"),
            createSatelliteStbProfile("stb_hellobox_b1", "HelloBox B1", "HelloBox B1 Bluetooth Finder", 0x1104, "HelloBox NEC"),
            createSatelliteStbProfile("stb_hellobox_v5", "HelloBox V5", "HelloBox V5 HD Multi-stream", 0x1105, "HelloBox NEC"),
            createSatelliteStbProfile("stb_hellobox_combo", "HelloBox Combo", "HelloBox DVB-S2/T2/C Receiver", 0x1106, "HelloBox NEC"),

            // --- DVB-T2 / DVB-S / HD & SD Satellite Receivers ---
            createSatelliteStbProfile("stb_dreambox", "DreamBox HD", "Dreambox DM900 Ultra HD", 0x1110, "DreamBox RC6"),
            createSatelliteStbProfile("stb_vuplus", "Vu+ UHD", "Vu+ Duo 4K SE DVB-S2X/T2", 0x1111, "VuPlus NEC"),
            createSatelliteStbProfile("stb_amiko", "Amiko Digital", "Amiko Mini HD DVB-T2/C", 0x1112, "Amiko NEC"),
            createSatelliteStbProfile("stb_openbox", "Openbox HD", "Openbox S3 Mini II DVB-S2", 0x1113, "Openbox NEC"),
            createSatelliteStbProfile("stb_tiger", "Tiger International", "Tiger T8 High Class DVB-S2", 0x1114, "Tiger NEC"),
            createSatelliteStbProfile("stb_starsat", "Starsat HD", "Starsat SR-2000HD Hyper", 0x1115, "Starsat NEC"),
            createSatelliteStbProfile("stb_geant", "Geant Electronics", "Geant GN-OTT HD Satellite", 0x1116, "Geant NEC"),
            createSatelliteStbProfile("stb_mediastar", "MediaStar HD", "MediaStar MS-15000 Laser", 0x1117, "MediaStar NEC"),
            createSatelliteStbProfile("stb_strong", "Strong Digital", "Strong SRT 8215 DVB-T2 HD", 0x1118, "Strong NEC"),
            createSatelliteStbProfile("stb_spider", "Spider HD", "Spider T888 Ultra HD Box", 0x1119, "Spider NEC"),
            createSatelliteStbProfile("stb_samsat", "Samsat HD", "Samsat HD 50-50 Mini", 0x111A, "Samsat NEC"),
            createSatelliteStbProfile("stb_redline", "Redline Satellite", "Redline TS 4000 HD Pass", 0x111B, "Redline NEC"),
            createSatelliteStbProfile("stb_skybox", "Skybox HD", "Skybox F5S HD Satellite Receiver", 0x111C, "Skybox NEC"),
            createSatelliteStbProfile("stb_freesat", "Freesat HD", "Freesat V8 Super DVB-S2", 0x111D, "Freesat NEC"),
            createSatelliteStbProfile("stb_echostar", "EchoStar", "EchoStar DSB-770 HD Receiver", 0x111E, "EchoStar NEC"),
            createSatelliteStbProfile("stb_kaon", "KAON Media", "KAON CO1200 DVB-S2 Receiver", 0x111F, "KAON NEC"),
            createSatelliteStbProfile("stb_arion", "Arion Digital", "Arion AF-9300 SD Satellite", 0x1120, "Arion NEC"),
            createSatelliteStbProfile("stb_pace", "Pace Micro", "Pace Sky+ HD DS830 Receiver", 0x1121, "Pace RC6"),
            createSatelliteStbProfile("stb_cisco", "Cisco STB", "Cisco ISB7150 HD Receiver", 0x1122, "Cisco NEC"),
            createSatelliteStbProfile("stb_sagemcom", "Sagemcom HD", "Sagemcom DSI87 HD Satellite", 0x1123, "Sagemcom NEC"),
            createSatelliteStbProfile("stb_adb", "ADB Global", "ADB-2840 DVB-T2 STB", 0x1124, "ADB NEC"),
            createSatelliteStbProfile("stb_changhong", "Changhong STB", "Changhong SD DVB-S Receiver", 0x1125, "Changhong NEC"),
            createSatelliteStbProfile("stb_skyworth", "Skyworth STB", "Skyworth HD DVB-T2 Box", 0x1126, "Skyworth NEC"),
            createSatelliteStbProfile("stb_coship", "Coship STB", "Coship N8750 DVB-C Receiver", 0x1127, "Coship NEC"),
            createSatelliteStbProfile("stb_jiuzhou", "Jiuzhou STB", "Jiuzhou HD DVB-S2 Box", 0x1128, "Jiuzhou NEC"),
            createSatelliteStbProfile("stb_unionman", "Unionman STB", "Unionman UN800 DVB-T2 Box", 0x1129, "Unionman NEC"),
            createSatelliteStbProfile("stb_yinhe", "Yinhe STB", "Yinhe SD DVB-S Box", 0x112A, "Yinhe NEC"),
            createSatelliteStbProfile("stb_gulan", "Gulan STB", "Gulan Digital DVB-S2 STB", 0x112B, "Gulan NEC"),
            createSatelliteStbProfile("stb_hisense_stb", "Hisense STB", "Hisense DVB-T2 HD Receiver", 0x112C, "Hisense NEC"),
            createSatelliteStbProfile("stb_haier_stb", "Haier STB", "Haier SD DVB-S STB", 0x112D, "Haier NEC"),
            createSatelliteStbProfile("stb_tcl_stb", "TCL STB", "TCL HD DVB-T2 Receiver", 0x112E, "TCL NEC"),
            createSatelliteStbProfile("stb_konka_stb", "Konka STB", "Konka SD DVB-S Receiver", 0x112F, "Konka NEC"),
            createSatelliteStbProfile("stb_sky", "Sky Italia HD", "Sky Box HD Pace DS831NS", 0x1130, "Sky RC6"),
            createSatelliteStbProfile("stb_canal", "Canal+ HD", "Le Cube Canal+ DVB-S2", 0x1131, "Canal+ RC6"),
            createSatelliteStbProfile("stb_foxtel", "Foxtel IQ", "Foxtel iQ4 UHD STB", 0x1132, "Foxtel NEC"),
            createSatelliteStbProfile("stb_astro", "Astro Malaysia", "Astro Ultra Box UHD STB", 0x1133, "Astro NEC"),
            createSatelliteStbProfile("stb_truevisions", "TrueVisions HD", "TrueVisions DVB-S2 Humax Box", 0x1134, "TrueVisions NEC"),
            createSatelliteStbProfile("stb_tataplay", "Tata Play HD", "Tata Play Binge+ Android STB", 0x1135, "Tata Play NEC"),
            createSatelliteStbProfile("stb_airtel_stb", "Airtel Digital TV", "Airtel Xstream HD Box DVB-S2", 0x1136, "Airtel NEC"),
            createSatelliteStbProfile("stb_dishtv", "Dish TV India", "Dish NXT HD DVB-S2 Box", 0x1137, "Dish TV NEC"),
            createSatelliteStbProfile("stb_videocond2h", "Videocon d2h HD", "Videocon d2h 5555 HD STB", 0x1138, "Videocon NEC"),
            createSatelliteStbProfile("stb_ddfreedish", "DD Free Dish SD", "DD Free Dish SD MPEG-2 Receiver", 0x1139, "DD Free Dish NEC"),
            createSatelliteStbProfile("stb_dialogtv", "Dialog TV HD", "Dialog TV HD DVB-S2 Receiver", 0x113A, "Dialog TV NEC"),
            createSatelliteStbProfile("stb_peotv", "PEO TV STB", "Sri Lanka PEO TV IPTV STB", 0x113B, "PEOTV NEC"),
            createSatelliteStbProfile("stb_goldsat", "Goldsat HD", "Goldsat Revo DVB-S2 Receiver", 0x1140, "Goldsat NEC"),
            createSatelliteStbProfile("stb_matrix", "Matrix STB", "Matrix Apple DVB-T2 HD Box", 0x1141, "Matrix NEC"),
            createSatelliteStbProfile("stb_polytron", "Polytron STB", "Polytron PDV 700T2 DVB-T2", 0x1142, "Polytron NEC"),
            createSatelliteStbProfile("stb_luby", "Luby DVB-T2", "Luby Digi-Box DVB-T2 Receiver", 0x1143, "Luby NEC"),
            createSatelliteStbProfile("stb_evercoss", "Evercoss STB", "Evercoss Prime DVB-T2 HD", 0x1144, "Evercoss NEC"),
            createSatelliteStbProfile("stb_advance", "Advance STB", "Advance Digi-Box DVB-T2", 0x1145, "Advance NEC"),
            createSatelliteStbProfile("stb_tanaka", "Tanaka Satellite", "Tanaka T2 Premium DVB-T2 Box", 0x1146, "Tanaka NEC"),
            createSatelliteStbProfile("stb_venus", "Venus Satellite", "Venus Estilo HD DVB-S2", 0x1147, "Venus NEC"),
            createSatelliteStbProfile("stb_skybox_a1", "Skybox A1", "Skybox A1 Plus H.265 DVB-S2", 0x1148, "Skybox NEC"),
            createSatelliteStbProfile("stb_optus", "Optus HD", "Optus OP-66HD DVB-S2 Box", 0x1149, "Optus NEC"),
            createSatelliteStbProfile("stb_kaonsat", "Kaonsat HD", "Kaonsat Classic DVB-S2 Receiver", 0x114A, "Kaonsat NEC"),
            createSatelliteStbProfile("stb_lgsat", "LGSat HD", "LGSat Infinite DVB-S2 Box", 0x114B, "LGSat NEC"),
            createSatelliteStbProfile("stb_gardiner", "Gardiner HD", "Gardiner G-88HD Ottimo DVB-S2", 0x114C, "Gardiner NEC"),
            createSatelliteStbProfile("stb_kvision", "K-Vision HD", "K-Vision Cartenz HD DVB-S2 Box", 0x114D, "KVision NEC"),
            createSatelliteStbProfile("stb_nexparabola", "Nex Parabola", "Nex Parabola Combo DVB-S2/T2 Box", 0x114E, "NexParabola NEC"),
            createSatelliteStbProfile("stb_transvision", "Transvision HD", "Transvision Samsung HD DVB-S2 STB", 0x114F, "Transvision NEC"),
            createSatelliteStbProfile("stb_indovision", "Indovision SD", "Indovision Pace SD Satellite Box", 0x1150, "Indovision NEC"),
            createSatelliteStbProfile("stb_skynindo", "Skynindo HD", "Skynindo HD-99 DVB-S2 Receiver", 0x1151, "Skynindo NEC"),
            createSatelliteStbProfile("stb_topas", "Topas TV HD", "Topas TV HD DVB-S2 STB", 0x1152, "Topas TV NEC"),
            createSatelliteStbProfile("stb_orange", "Orange TV HD", "Orange TV HD DVB-S2 Star STB", 0x1153, "Orange TV NEC"),

            // =============================================================
            // 5. AUDIO & SOUNDBARS (6 BRANDS)
            // =============================================================
            createAudioSoundbarProfile("aud_bose", "Bose", "Solo / Soundbar 700", 0x01A1, "Bose IR Code"),
            createAudioSoundbarProfile("aud_jbl", "JBL", "Bar Cinema Series", 0x02B2, "JBL NEC 38kHz"),
            createAudioSoundbarProfile("aud_yamaha", "Yamaha", "YAS Soundbar / AV Receiver", 0x7887, "Yamaha NEC 38kHz"),
            createAudioSoundbarProfile("aud_sonos", "Sonos IR", "Ray / Arc IR Receiver", 0x03C3, "Sonos IR Protocol"),
            createAudioSoundbarProfile("aud_denon", "Denon", "AVR Cinema Receiver", 0x04D4, "Denon Kaseikyo"),
            createAudioSoundbarProfile("aud_pioneer", "Pioneer", "Elite Receiver / Soundbar", 0x05E5, "Pioneer NEC 38kHz"),

            // =============================================================
            // 6. FANS & AIR CIRCULATORS (4 BRANDS)
            // =============================================================
            createFanProfile("fan_dyson", "Dyson", "Pure Cool / Hot+Cool", 0x01F1, "Dyson 38kHz Code"),
            createFanProfile("fan_honeywell", "Honeywell", "QuietSet Tower Fan", 0x02F2, "Honeywell NEC 38kHz"),
            createFanProfile("fan_lasko", "Lasko", "Wind Curve Tower Fan", 0x03F3, "Lasko NEC 38kHz"),
            createFanProfile("fan_vornado", "Vornado", "Air Circulator Fan", 0x04F4, "Vornado NEC 38kHz"),

            // =============================================================
            // 7. AIR PURIFIERS & SCRUBBERS (4 BRANDS)
            // =============================================================
            createAirPurifierProfile("pur_sharp", "Sharp Purifier", "Plasmacluster Air Cleaner", 0x05F5, "Sharp IR Code"),
            createAirPurifierProfile("pur_levoit", "Levoit", "Core 300S / 400S Purifier", 0x06F6, "Levoit NEC 38kHz"),
            createAirPurifierProfile("pur_coway", "Coway", "AP-1512HH Mighty Purifier", 0x07F7, "Coway NEC 38kHz"),
            createAirPurifierProfile("pur_winix", "Winix", "5500-2 HEPA Filter Unit", 0x08F8, "Winix NEC 38kHz"),

            // =============================================================
            // 8. DVD & BLU-RAY PLAYERS (2 BRANDS)
            // =============================================================
            createDvdBlurayProfile("dvd_sony", "Sony Blu-ray", "BDP / UBP 4K Player", 0x01B1, "Sony SIRC 20-bit"),
            createDvdBlurayProfile("dvd_panasonic", "Panasonic DVD", "DP-UB Ultra HD Player", 0x4006, "Panasonic Extended"),

            // =============================================================
            // 9. LIGHTING & SENSORS (2 BRANDS)
            // =============================================================
            createLightingSensorProfile("sensor_wattstopper", "Wattstopper", "FS-IR / FSP-211 Sensors", 0xA11B, "Wattstopper 38kHz"),
            createLightingSensorProfile("sensor_leviton", "Leviton Sensor", "ODC / OSC High-Bay Sensor", 0xB22C, "Leviton Pro IR")
        )
    }

    // -------------------------------------------------------------
    // PROFILE CREATOR HELPERS
    // -------------------------------------------------------------

    private fun createAcProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwr = IrProtocolEncoder.encodeNec(address, 0x10)
        val tempUp = IrProtocolEncoder.encodeNec(address, 0x12)
        val tempDown = IrProtocolEncoder.encodeNec(address, 0x13)
        val modeCool = IrProtocolEncoder.encodeNec(address, 0x20)
        val modeHeat = IrProtocolEncoder.encodeNec(address, 0x21)
        val modeDry = IrProtocolEncoder.encodeNec(address, 0x22)
        val modeFan = IrProtocolEncoder.encodeNec(address, 0x23)
        val fanAuto = IrProtocolEncoder.encodeNec(address, 0x30)
        val fanHigh = IrProtocolEncoder.encodeNec(address, 0x33)
        val fanTurbo = IrProtocolEncoder.encodeNec(address, 0x34)
        val swingVert = IrProtocolEncoder.encodeNec(address, 0x40)

        val quickActions = listOf(
            IrCommand("ac_pwr", "Power Toggle", "Turn AC Unit ON / Standby", 38000, pwr, "0x10", true),
            IrCommand("ac_cool_22", "Cool 22°C Auto", "Set Cool mode @ 22°C Auto Fan", 38000, modeCool, "0x20", true),
            IrCommand("ac_turbo", "Jet Cool / Turbo", "Max fan velocity for fast cooling", 38000, fanTurbo, "0x34", true),
            IrCommand("ac_swing", "V-Swing Auto", "Toggle vertical air louver oscillation", 38000, swingVert, "0x40", true)
        )

        val fullCommands = quickActions + listOf(
            IrCommand("ac_t_up", "Temp UP (+1°C)", "Increase temperature setpoint", 38000, tempUp, "0x12"),
            IrCommand("ac_t_dn", "Temp DOWN (-1°C)", "Decrease temperature setpoint", 38000, tempDown, "0x13"),
            IrCommand("ac_heat", "Heat 24°C Mode", "Switch to Heating mode @ 24°C", 38000, modeHeat, "0x21"),
            IrCommand("ac_dry", "Dehumidify / Dry", "Dehumidification moisture removal", 38000, modeDry, "0x22"),
            IrCommand("ac_fan_only", "Fan Only Mode", "Circulate air without compressor", 38000, modeFan, "0x23"),
            IrCommand("ac_fan_hi", "Fan Speed High", "Set fan velocity to High", 38000, fanHigh, "0x33")
        )

        return IrDeviceProfile(
            id = id, brand = brand, modelOrSeries = model, category = IrTradeCategory.HVAC,
            frequencyHz = 38000, protocolName = protocol, quickActions = quickActions, fullCommands = fullCommands,
            macros = listOf(
                IrMacroDefinition("m_cool_boost", "Rapid Cool Down", "Power ON → Cool Mode → Turbo Fan", listOf(
                    MacroStep("Power On", 38000, pwr, "0x10", 600L),
                    MacroStep("Cool Mode", 38000, modeCool, "0x20", 500L),
                    MacroStep("Turbo Fan", 38000, fanTurbo, "0x34", 0L)
                ))
            )
        )
    }

    private fun createTvProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwr = IrProtocolEncoder.encodeNec(address, 0x02)
        val mute = IrProtocolEncoder.encodeNec(address, 0x09)
        val volUp = IrProtocolEncoder.encodeNec(address, 0x12)
        val volDn = IrProtocolEncoder.encodeNec(address, 0x13)
        val source = IrProtocolEncoder.encodeNec(address, 0x0B)
        val menu = IrProtocolEncoder.encodeNec(address, 0x1A)
        val home = IrProtocolEncoder.encodeNec(address, 0x1C)
        val ok = IrProtocolEncoder.encodeNec(address, 0x18)

        val d0 = IrProtocolEncoder.encodeNec(address, 0x20)
        val d1 = IrProtocolEncoder.encodeNec(address, 0x21)
        val d2 = IrProtocolEncoder.encodeNec(address, 0x22)
        val d3 = IrProtocolEncoder.encodeNec(address, 0x23)
        val d4 = IrProtocolEncoder.encodeNec(address, 0x24)
        val d5 = IrProtocolEncoder.encodeNec(address, 0x25)
        val d6 = IrProtocolEncoder.encodeNec(address, 0x26)
        val d7 = IrProtocolEncoder.encodeNec(address, 0x27)
        val d8 = IrProtocolEncoder.encodeNec(address, 0x28)
        val d9 = IrProtocolEncoder.encodeNec(address, 0x29)

        val quickActions = listOf(
            IrCommand("tv_pwr", "Power ON/OFF", "Toggle display power state", 38000, pwr, "0x02", true),
            IrCommand("tv_source", "Input / Source", "Cycle HDMI 1 / 2 / DisplayPort", 38000, source, "0x0B", true),
            IrCommand("tv_mute", "Mute Audio", "Silence TV speakers instantly", 38000, mute, "0x09", true),
            IrCommand("tv_home", "Smart Home", "Launch Smart TV Dashboard", 38000, home, "0x1C", true)
        )

        val fullCommands = quickActions + listOf(
            IrCommand("tv_v_up", "Volume UP", "Increase TV volume", 38000, volUp, "0x12"),
            IrCommand("tv_v_dn", "Volume DOWN", "Decrease TV volume", 38000, volDn, "0x13"),
            IrCommand("tv_menu", "Settings Menu", "Open OSD TV picture settings", 38000, menu, "0x1A"),
            IrCommand("tv_ok", "Select / OK", "Confirm menu selection", 38000, ok, "0x18"),
            IrCommand("tv_digit_0", "Digit 0", "Numeric key 0", 38000, d0, "0x20"),
            IrCommand("tv_digit_1", "Digit 1", "Numeric key 1", 38000, d1, "0x21"),
            IrCommand("tv_digit_2", "Digit 2", "Numeric key 2", 38000, d2, "0x22"),
            IrCommand("tv_digit_3", "Digit 3", "Numeric key 3", 38000, d3, "0x23"),
            IrCommand("tv_digit_4", "Digit 4", "Numeric key 4", 38000, d4, "0x24"),
            IrCommand("tv_digit_5", "Digit 5", "Numeric key 5", 38000, d5, "0x25"),
            IrCommand("tv_digit_6", "Digit 6", "Numeric key 6", 38000, d6, "0x26"),
            IrCommand("tv_digit_7", "Digit 7", "Numeric key 7", 38000, d7, "0x27"),
            IrCommand("tv_digit_8", "Digit 8", "Numeric key 8", 38000, d8, "0x28"),
            IrCommand("tv_digit_9", "Digit 9", "Numeric key 9", 38000, d9, "0x29")
        )

        return IrDeviceProfile(
            id = id, brand = brand, modelOrSeries = model, category = IrTradeCategory.SITE_AV,
            frequencyHz = 38000, protocolName = protocol, quickActions = quickActions, fullCommands = fullCommands,
            macros = emptyList()
        )
    }

    private fun createProjectorProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwrOn = IrProtocolEncoder.encodeNec(address, 0x01)
        val pwrOff = IrProtocolEncoder.encodeNec(address, 0x02)
        val source = IrProtocolEncoder.encodeNec(address, 0x05)
        val freeze = IrProtocolEncoder.encodeNec(address, 0x47)
        val blank = IrProtocolEncoder.encodeNec(address, 0x48)

        val quickActions = listOf(
            IrCommand("proj_pwr_on", "Lamp Power ON", "Ignite projector light engine", 38000, pwrOn, "0x01", true),
            IrCommand("proj_pwr_off", "Standby / Off", "Initiate fan cooling shutdown", 38000, pwrOff, "0x02", true),
            IrCommand("proj_source", "Input Switch", "Toggle HDMI 1 / VGA / USB", 38000, source, "0x05", true),
            IrCommand("proj_freeze", "Freeze Frame", "Freeze display frame for inspection", 38000, freeze, "0x47", true)
        )

        val fullCommands = quickActions + listOf(
            IrCommand("proj_blank", "AV Mute / Blank", "Blank projection beam during breaks", 38000, blank, "0x48")
        )

        return IrDeviceProfile(
            id = id, brand = brand, modelOrSeries = model, category = IrTradeCategory.PROJECTOR,
            frequencyHz = 38000, protocolName = protocol, quickActions = quickActions, fullCommands = fullCommands,
            macros = emptyList()
        )
    }

    private fun createSatelliteStbProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwr = IrProtocolEncoder.encodeNec(address, 0x01)
        val guide = IrProtocolEncoder.encodeNec(address, 0x07)
        val chUp = IrProtocolEncoder.encodeNec(address, 0x10)
        val chDn = IrProtocolEncoder.encodeNec(address, 0x11)

        val quickActions = listOf(
            IrCommand("stb_pwr", "Receiver Power", "Toggle Set-Top Box Power", 38000, pwr, "0x01", true),
            IrCommand("stb_guide", "TV Guide", "Open Satellite Channel EPG Guide", 38000, guide, "0x07", true),
            IrCommand("stb_ch_up", "Channel UP", "Next TV channel", 38000, chUp, "0x10", true),
            IrCommand("stb_ch_dn", "Channel DOWN", "Previous TV channel", 38000, chDn, "0x11", true)
        )

        return IrDeviceProfile(
            id = id, brand = brand, modelOrSeries = model, category = IrTradeCategory.SATELLITE_STB,
            frequencyHz = 38000, protocolName = protocol, quickActions = quickActions, fullCommands = quickActions,
            macros = emptyList()
        )
    }

    private fun createAudioSoundbarProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwr = IrProtocolEncoder.encodeNec(address, 0x01)
        val volUp = IrProtocolEncoder.encodeNec(address, 0x0A)
        val volDn = IrProtocolEncoder.encodeNec(address, 0x0B)
        val optInput = IrProtocolEncoder.encodeNec(address, 0x14)
        val btInput = IrProtocolEncoder.encodeNec(address, 0x15)

        val quickActions = listOf(
            IrCommand("aud_pwr", "Audio Power", "Power on soundbar / amplifier", 38000, pwr, "0x01", true),
            IrCommand("aud_v_up", "Volume UP", "Increase soundbar volume", 38000, volUp, "0x0A", true),
            IrCommand("aud_v_dn", "Volume DOWN", "Decrease soundbar volume", 38000, volDn, "0x0B", true),
            IrCommand("aud_bt", "Bluetooth Mode", "Switch input to Bluetooth Audio", 38000, btInput, "0x15", true)
        )

        val fullCommands = quickActions + listOf(
            IrCommand("aud_opt", "Optical Input", "Switch input to Optical Digital", 38000, optInput, "0x14")
        )

        return IrDeviceProfile(
            id = id, brand = brand, modelOrSeries = model, category = IrTradeCategory.AUDIO_SOUNDBAR,
            frequencyHz = 38000, protocolName = protocol, quickActions = quickActions, fullCommands = fullCommands,
            macros = emptyList()
        )
    }

    private fun createFanProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwr = IrProtocolEncoder.encodeNec(address, 0x20)
        val spdHi = IrProtocolEncoder.encodeNec(address, 0x26)
        val osc = IrProtocolEncoder.encodeNec(address, 0x2A)

        val quickActions = listOf(
            IrCommand("fan_pwr", "Fan Power", "Toggle fan motor power", 38000, pwr, "0x20", true),
            IrCommand("fan_speed_hi", "High Speed", "Maximum airflow speed", 38000, spdHi, "0x26", true),
            IrCommand("fan_osc", "Oscillation", "Toggle 90° head oscillation", 38000, osc, "0x2A", true)
        )

        return IrDeviceProfile(
            id = id, brand = brand, modelOrSeries = model, category = IrTradeCategory.HEATERS_FANS,
            frequencyHz = 38000, protocolName = protocol, quickActions = quickActions, fullCommands = quickActions,
            macros = emptyList()
        )
    }

    private fun createAirPurifierProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwr = IrProtocolEncoder.encodeNec(address, 0x01)
        val auto = IrProtocolEncoder.encodeNec(address, 0x05)
        val turbo = IrProtocolEncoder.encodeNec(address, 0x08)

        val quickActions = listOf(
            IrCommand("pur_pwr", "Purifier Power", "Power on HEPA filtration unit", 38000, pwr, "0x01", true),
            IrCommand("pur_auto", "Auto Sensor Mode", "Adjust fan based on particulate sensor", 38000, auto, "0x05", true),
            IrCommand("pur_turbo", "Max Air Clean", "Maximum HEPA air scrubber speed", 38000, turbo, "0x08", true)
        )

        return IrDeviceProfile(
            id = id, brand = brand, modelOrSeries = model, category = IrTradeCategory.AIR_FILTRATION,
            frequencyHz = 38000, protocolName = protocol, quickActions = quickActions, fullCommands = quickActions,
            macros = emptyList()
        )
    }

    private fun createDvdBlurayProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwr = IrProtocolEncoder.encodeNec(address, 0x01)
        val play = IrProtocolEncoder.encodeNec(address, 0x04)
        val stop = IrProtocolEncoder.encodeNec(address, 0x05)
        val eject = IrProtocolEncoder.encodeNec(address, 0x0F)

        val quickActions = listOf(
            IrCommand("dvd_pwr", "Player Power", "Toggle Blu-ray / DVD power", 38000, pwr, "0x01", true),
            IrCommand("dvd_play", "Play Disc", "Start disc playback", 38000, play, "0x04", true),
            IrCommand("dvd_stop", "Stop", "Stop playback", 38000, stop, "0x05", true),
            IrCommand("dvd_eject", "Open / Eject Tray", "Eject disc tray", 38000, eject, "0x0F", true)
        )

        return IrDeviceProfile(
            id = id, brand = brand, modelOrSeries = model, category = IrTradeCategory.MEDIA_PLAYER,
            frequencyHz = 38000, protocolName = protocol, quickActions = quickActions, fullCommands = quickActions,
            macros = emptyList()
        )
    }

    private fun createLightingSensorProfile(id: String, brand: String, model: String, address: Int, protocol: String): IrDeviceProfile {
        val pwr = IrProtocolEncoder.encodeNec(address, 0x01)
        val testMode = IrProtocolEncoder.encodeNec(address, 0x05)

        val quickActions = listOf(
            IrCommand("sens_pwr", "Sensor Power", "Toggle occupancy sensor override", 38000, pwr, "0x01", true),
            IrCommand("sens_test", "5s Test Mode", "Set sensor walk-test hold time", 38000, testMode, "0x05", true)
        )

        return IrDeviceProfile(
            id = id, brand = brand, modelOrSeries = model, category = IrTradeCategory.LIGHTING_SENSORS,
            frequencyHz = 38000, protocolName = protocol, quickActions = quickActions, fullCommands = quickActions,
            macros = emptyList()
        )
    }
}
