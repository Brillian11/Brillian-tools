package com.example.domain.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

enum class FrequencyBand(
    val title: String,
    val defaultFreqGhz: Double,
    val freqRangeText: String,
    val description: String,
    val isCellular: Boolean = false
) {
    C_BAND("C-Band 3.8 GHz", 3.8, "3.4 - 4.2 GHz", "Standard satellite TV broadcast, resilient to rain fade", isCellular = false),
    KU_BAND("Ku-Band 11.7 GHz", 11.7, "10.7 - 12.75 GHz", "High-capacity DTH satellite TV & Starlink", isCellular = false),
    KA_BAND("Ka-Band 20.0 GHz", 20.0, "17.7 - 30.0 GHz", "High-speed broadband satellite internet", isCellular = false),
    S_BAND("S-Band 2.5 GHz", 2.5, "2.0 - 4.0 GHz", "Mobile satellite TV & weather radar", isCellular = false),

    // Cellular & Directional Tower Links
    LTE_700("700 MHz (B28/B12)", 0.700, "700 - 800 MHz", "Long-range rural cellular & deep building penetration", isCellular = true),
    LTE_850_900("850 / 900 MHz (B5/B8)", 0.900, "850 - 900 MHz", "Regional GSM / 3G / 4G cellular grid & parabolic boost", isCellular = true),
    LTE_1300_1800("1300 / 1800 MHz (B3)", 1.800, "1300 - 1880 MHz", "Urban 4G LTE high capacity tower link", isCellular = true),
    LTE_2100("2100 MHz (B1)", 2.100, "1920 - 2170 MHz", "3G / 4G LTE core carrier frequency", isCellular = true),
    LTE_2300_2600("2300 / 2600 MHz (B40/B7)", 2.500, "2300 - 2690 MHz", "High-speed TDD/FDD 4G LTE parabolic grid link", isCellular = true),
    NR_5G_SUB6("5G Sub-6 (3.5 GHz n78)", 3.500, "3.3 - 3.8 GHz", "High-speed 5G NR parabolic directional link", isCellular = true),
    NR_5G_MMWAVE("5G mmWave (28 GHz n257)", 28.000, "24.25 - 29.5 GHz", "Ultra-wideband 5G line-of-sight point-to-point beam link", isCellular = true),

    CUSTOM("Custom RF Frequency", 2.400, "User Defined", "Manual RF frequency input", isCellular = false)
}

data class SatellitePreset(
    val name: String,
    val orbitalLongitudeDeg: Double, // Positive for East, Negative for West
    val band: FrequencyBand,
    val coverageRegion: String
) {
    val longitudeFormatted: String
        get() {
            val absVal = abs(orbitalLongitudeDeg)
            val dir = if (orbitalLongitudeDeg >= 0) "E" else "W"
            return "%.1f° %s".format(absVal, dir)
        }

    companion object {
        val POPULAR_SATELLITES = listOf(
            // --- INDONESIA / SE ASIA / ASIA-PACIFIC ---
            SatellitePreset("Telkom 4 (Merah Putih)", 108.0, FrequencyBand.C_BAND, "Indonesia / SE Asia"),
            SatellitePreset("Telkom 3S", 118.0, FrequencyBand.C_BAND, "Indonesia"),
            SatellitePreset("Palapa D / Nusantara 3", 113.0, FrequencyBand.C_BAND, "Indonesia"),
            SatellitePreset("Nusantara Satu", 146.0, FrequencyBand.KU_BAND, "Indonesia / Pacific"),
            SatellitePreset("AsiaSat 5", 100.5, FrequencyBand.C_BAND, "Asia-Pacific"),
            SatellitePreset("AsiaSat 7", 105.5, FrequencyBand.C_BAND, "Asia-Pacific"),
            SatellitePreset("AsiaSat 9", 122.0, FrequencyBand.C_BAND, "Asia-Pacific"),
            SatellitePreset("Measat 3a / 3b / 3d", 91.5, FrequencyBand.KU_BAND, "SE Asia / Malaysia"),
            SatellitePreset("ChinaSat 6B", 115.5, FrequencyBand.C_BAND, "East & SE Asia"),
            SatellitePreset("ChinaSat 6C", 130.0, FrequencyBand.C_BAND, "China / SE Asia"),
            SatellitePreset("ChinaSat 6D", 125.0, FrequencyBand.C_BAND, "East Asia"),
            SatellitePreset("SES 7 / SES 9", 108.2, FrequencyBand.KU_BAND, "South / SE Asia"),
            SatellitePreset("SES 12", 95.0, FrequencyBand.KU_BAND, "Asia-Pacific"),
            SatellitePreset("Koreasat 5A / 7", 113.0, FrequencyBand.KU_BAND, "Korea / SE Asia"),
            SatellitePreset("Optus D2", 152.0, FrequencyBand.KU_BAND, "Australia / Oceania"),
            SatellitePreset("Optus D1", 160.0, FrequencyBand.KU_BAND, "Australia / NZ"),
            SatellitePreset("Thaicom 6 / 8", 78.5, FrequencyBand.C_BAND, "Thailand / SE Asia"),
            SatellitePreset("Vinasat 1 / 2", 132.0, FrequencyBand.KU_BAND, "Vietnam / SE Asia"),
            SatellitePreset("Apstar 6D", 134.0, FrequencyBand.KU_BAND, "Asia-Pacific"),
            SatellitePreset("Apstar 7", 76.5, FrequencyBand.C_BAND, "Asia / Africa"),
            SatellitePreset("Express 80", 80.0, FrequencyBand.C_BAND, "Eurasia"),
            SatellitePreset("GSAT 15 / 17", 93.5, FrequencyBand.C_BAND, "India / South Asia"),

            // --- CELLULAR & SPECIALIZED ---
            SatellitePreset("4G LTE Tower Boost Grid", 108.0, FrequencyBand.LTE_1300_1800, "Cellular Grid / Parabolic"),
            SatellitePreset("5G NR Sub-6 / mmWave Link", 108.0, FrequencyBand.NR_5G_SUB6, "Cellular Directional Link"),

            // --- EUROPE / MIDDLE EAST / AFRICA ---
            SatellitePreset("Hotbird 13F / 13G", 13.0, FrequencyBand.KU_BAND, "Europe / Middle East"),
            SatellitePreset("Astra 1N / 1M", 19.2, FrequencyBand.KU_BAND, "Europe"),
            SatellitePreset("Astra 2E / 2F", 28.2, FrequencyBand.KU_BAND, "UK / Europe"),
            SatellitePreset("Eutelsat 7 West A", -7.0, FrequencyBand.KU_BAND, "MENA / North Africa"),
            SatellitePreset("Türksat 4A / 5A", 42.0, FrequencyBand.KU_BAND, "Turkey / Europe / MENA"),

            // --- AMERICAS ---
            SatellitePreset("Galaxy 19", -97.0, FrequencyBand.KU_BAND, "North America"),
            SatellitePreset("SES 1", -101.0, FrequencyBand.C_BAND, "North America"),
            SatellitePreset("Star One D2", -70.0, FrequencyBand.C_BAND, "South America"),
            SatellitePreset("Anik F1R", -107.3, FrequencyBand.C_BAND, "Canada / USA"),

            SatellitePreset("Custom Orbital Slot", 108.0, FrequencyBand.C_BAND, "User Defined")
        )

        fun calculateLookAngles(
            userLat: Double,
            userLon: Double,
            satLon: Double
        ): Pair<Double, Double> {
            val latRad = Math.toRadians(userLat)
            val lonRad = Math.toRadians(userLon)
            val satLonRad = Math.toRadians(satLon)
            val deltaLon = satLonRad - lonRad

            val cosGamma = cos(latRad) * cos(deltaLon)
            val sinGamma = sqrt((1.0 - cosGamma * cosGamma).coerceAtLeast(0.0))

            val elev = if (sinGamma == 0.0) 90.0 else Math.toDegrees(atan((cosGamma - 0.1512) / sinGamma))
            var az = Math.toDegrees(atan2(sin(deltaLon), -sin(latRad) * cos(deltaLon)))
            if (az < 0) az += 360.0

            return Pair(az, elev)
        }
    }
}

data class LnbOffsetInfo(
    val lnbIndex: Int,
    val satName: String,
    val satLongitudeDeg: Double,
    val orbitalSeparationDeg: Double,
    val offsetDistanceCm: Double,
    val offsetDistanceInches: Double,
    val isPrimary: Boolean
)

data class SatellitePointerResult(
    val userLatitude: Double = -6.2000,  // Default Jakarta / SE Asia reference
    val userLongitude: Double = 106.8167,
    val selectedSat: SatellitePreset = SatellitePreset.POPULAR_SATELLITES[2], // Telkom 4
    val customSatLongitude: Double = 108.0,
    val dishDiameterCm: Double = 180.0,  // Standard 6ft dish
    val dishDepthCm: Double = 30.0,
    val frequencyGhz: Double = 3.8,
    val efficiencyPercent: Double = 65.0,
    val lnbCount: Int = 1,
    val secondarySatellites: List<SatellitePreset> = listOf(
        SatellitePreset.POPULAR_SATELLITES[1], // AsiaSat 7
        SatellitePreset.POPULAR_SATELLITES[3]  // Palapa D
    )
) {
    val targetSatLongitude: Double
        get() = if (selectedSat.name == "Custom Orbital Slot") customSatLongitude else selectedSat.orbitalLongitudeDeg

    // Focal length f = d^2 / (16 * c)
    val focalLengthCm: Double
        get() {
            if (dishDepthCm <= 0) return 0.0
            return (dishDiameterCm * dishDiameterCm) / (16.0 * dishDepthCm)
        }

    val focalRatio: Double
        get() {
            if (dishDiameterCm <= 0) return 0.0
            return focalLengthCm / dishDiameterCm
        }

    // Dish Type
    val dishTypeCategory: String
        get() = when {
            focalRatio < 0.35 -> "Deep Prime Focus (Wide Illumination)"
            focalRatio <= 0.45 -> "Standard Parabolic Dish (Optimal LNB Focal Spot)"
            else -> "Shallow Dish (Narrow Feed Angle)"
        }

    // Wavelength in meters lambda = 0.3 / f_GHz
    val wavelengthMeters: Double
        get() = 0.3 / frequencyGhz

    // Gain in dBi = 10 * log10( eta * (pi * d_m / lambda)^2 )
    val gainDbi: Double
        get() {
            val dMeters = dishDiameterCm / 100.0
            val eta = efficiencyPercent / 100.0
            val ratio = (PI * dMeters) / wavelengthMeters
            val g = eta * ratio * ratio
            if (g <= 0) return 0.0
            return 10.0 * log10(g)
        }

    // Antenna Beamwidth (3dB beamwidth) theta_3dB = 70 * lambda / d
    val beamwidthDeg: Double
        get() {
            val dMeters = dishDiameterCm / 100.0
            if (dMeters <= 0) return 0.0
            return 70.0 * wavelengthMeters / dMeters
        }

    // --- SATELLITE DISH ALIGNMENT CALCULATIONS (GEOSTAT SATELLITE) ---
    // User lat/lon in radians
    private val latRad = Math.toRadians(userLatitude)
    private val lonRad = Math.toRadians(userLongitude)
    private val satLonRad = Math.toRadians(targetSatLongitude)
    private val deltaLonRad = satLonRad - lonRad

    // Angle gamma subtended at Earth center
    private val cosGamma = cos(latRad) * cos(deltaLonRad)

    // Elevation Angle
    val elevationDeg: Double
        get() {
            val sinGamma = sqrt((1.0 - cosGamma * cosGamma).coerceAtLeast(0.0))
            if (sinGamma == 0.0) return 90.0
            // Earth radius / Geostationary orbital radius ~ 6378 / 42164 = 0.1512
            val num = cosGamma - 0.1512
            val elevRad = atan(num / sinGamma)
            return Math.toDegrees(elevRad)
        }

    val isVisibleAboveHorizon: Boolean
        get() = cosGamma > 0.1512 && elevationDeg > 0.0

    // Azimuth Angle (True Bearing from North 0°..360°)
    val azimuthTrueDeg: Double
        get() {
            val num = sin(deltaLonRad)
            val den = -sin(latRad) * cos(deltaLonRad)
            var azRad = atan2(num, den)
            var azDeg = Math.toDegrees(azRad)
            if (azDeg < 0) azDeg += 360.0
            return azDeg
        }

    // LNB Skew / Polarization Angle (Degrees)
    val lnbSkewDeg: Double
        get() {
            val num = sin(deltaLonRad)
            val den = tan(latRad)
            if (den == 0.0) return 0.0
            val skewRad = atan(num / den)
            return Math.toDegrees(skewRad)
        }

    // --- MULTI-LNB POSITION CORRECTION / OFFSET SPACING ---
    // Calculates horizontal offset distance along focal bracket rail for secondary LNBs
    val multiLnbOffsets: List<LnbOffsetInfo>
        get() {
            val list = mutableListOf<LnbOffsetInfo>()
            // Primary LNB
            list.add(
                LnbOffsetInfo(
                    lnbIndex = 1,
                    satName = selectedSat.name,
                    satLongitudeDeg = targetSatLongitude,
                    orbitalSeparationDeg = 0.0,
                    offsetDistanceCm = 0.0,
                    offsetDistanceInches = 0.0,
                    isPrimary = true
                )
            )

            if (lnbCount > 1) {
                secondarySatellites.take(lnbCount - 1).forEachIndexed { idx, secSat ->
                    val sepDeg = secSat.orbitalLongitudeDeg - targetSatLongitude
                    val sepRad = Math.toRadians(sepDeg)
                    // Offset d_off = f * tan(sepRad)
                    val offsetCm = focalLengthCm * tan(sepRad)
                    list.add(
                        LnbOffsetInfo(
                            lnbIndex = idx + 2,
                            satName = secSat.name,
                            satLongitudeDeg = secSat.orbitalLongitudeDeg,
                            orbitalSeparationDeg = sepDeg,
                            offsetDistanceCm = offsetCm,
                            offsetDistanceInches = offsetCm / 2.54,
                            isPrimary = false
                        )
                    )
                }
            }
            return list
        }
}
