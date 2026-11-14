package com.example.domain.sensor

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.ConsumerIrManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import com.example.domain.model.ToolDefinition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SensorCategory(val displayName: String) {
    MOTION_ORIENTATION("Motion & Orientation"),
    ENVIRONMENTAL("Environmental & Weather"),
    OPTICAL_ACOUSTIC("Optical & Acoustic"),
    CONNECTIVITY_HARDWARE("Connectivity & Hardware Ports")
}

data class HardwareSensorSpec(
    val id: String,
    val name: String,
    val technicalName: String,
    val category: SensorCategory,
    val isAvailable: Boolean,
    val hardwareVendor: String,
    val hardwareModel: String,
    val powerMa: Float,
    val resolution: Float,
    val maxRange: Float,
    val description: String,
    val dependentToolIds: List<String>,
    val dependentToolNames: List<String>
)

data class ToolSensorRequirement(
    val toolId: String,
    val toolTitle: String,
    val requiredSensorName: String,
    val sensorCategory: SensorCategory,
    val isSensorAvailable: Boolean,
    val hardwareNotes: String
)

data class SensorAuditReport(
    val totalScanned: Int,
    val availableCount: Int,
    val missingCount: Int,
    val compatibleToolsCount: Int,
    val totalToolsCount: Int,
    val sensors: List<HardwareSensorSpec>
)

class DeviceSensorDetector private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val packageManager = appContext.packageManager
    private val consumerIrManager = appContext.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
    private val cameraManager = appContext.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private val _auditReport = MutableStateFlow(scanAllSensors())
    val auditReport: StateFlow<SensorAuditReport> = _auditReport.asStateFlow()

    fun refresh() {
        _auditReport.value = scanAllSensors()
    }

    fun scanAllSensors(): SensorAuditReport {
        val list = mutableListOf<HardwareSensorSpec>()

        // 1. Magnetometer
        val magSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val magTools = listOf("widget_stud_detector", "widget_compass")
        list.add(
            HardwareSensorSpec(
                id = "magnetometer",
                name = "Magnetic Field (Magnetometer)",
                technicalName = "TYPE_MAGNETIC_FIELD",
                category = SensorCategory.MOTION_ORIENTATION,
                isAvailable = magSensor != null,
                hardwareVendor = magSensor?.vendor ?: "N/A",
                hardwareModel = magSensor?.name ?: "No magnetometer hardware found",
                powerMa = magSensor?.power ?: 0f,
                resolution = magSensor?.resolution ?: 0f,
                maxRange = magSensor?.maximumRange ?: 0f,
                description = "Required for drywall stud & iron pipe metal detection and 360° digital compass bearings.",
                dependentToolIds = magTools,
                dependentToolNames = magTools.map { getToolName(it) }
            )
        )

        // 2. Accelerometer (Inclinometer)
        val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val accelTools = listOf("widget_digital_level", "widget_plumb_bob", "widget_vibration_analyzer")
        list.add(
            HardwareSensorSpec(
                id = "accelerometer",
                name = "3-Axis Accelerometer & Inclinometer",
                technicalName = "TYPE_ACCELEROMETER",
                category = SensorCategory.MOTION_ORIENTATION,
                isAvailable = accelSensor != null,
                hardwareVendor = accelSensor?.vendor ?: "N/A",
                hardwareModel = accelSensor?.name ?: "No accelerometer hardware found",
                powerMa = accelSensor?.power ?: 0f,
                resolution = accelSensor?.resolution ?: 0f,
                maxRange = accelSensor?.maximumRange ?: 0f,
                description = "Required for water bubble digital levels, vertical plumb bob checking, and machinery vibration FFT analysis.",
                dependentToolIds = accelTools,
                dependentToolNames = accelTools.map { getToolName(it) }
            )
        )

        // 3. Gyroscope
        val gyroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val gyroTools = listOf("widget_compass", "widget_digital_level")
        list.add(
            HardwareSensorSpec(
                id = "gyroscope",
                name = "3-Axis Gyroscope Rate Sensor",
                technicalName = "TYPE_GYROSCOPE",
                category = SensorCategory.MOTION_ORIENTATION,
                isAvailable = gyroSensor != null,
                hardwareVendor = gyroSensor?.vendor ?: "N/A",
                hardwareModel = gyroSensor?.name ?: "No gyroscope hardware found",
                powerMa = gyroSensor?.power ?: 0f,
                resolution = gyroSensor?.resolution ?: 0f,
                maxRange = gyroSensor?.maximumRange ?: 0f,
                description = "Provides precise spatial rotation rate and angular momentum tracking.",
                dependentToolIds = gyroTools,
                dependentToolNames = gyroTools.map { getToolName(it) }
            )
        )

        // 4. Barometer (Pressure)
        val baroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
        val baroTools = listOf("widget_barometric_altimeter")
        list.add(
            HardwareSensorSpec(
                id = "barometer",
                name = "Atmospheric Pressure Barometer",
                technicalName = "TYPE_PRESSURE",
                category = SensorCategory.ENVIRONMENTAL,
                isAvailable = baroSensor != null,
                hardwareVendor = baroSensor?.vendor ?: "N/A",
                hardwareModel = baroSensor?.name ?: "No barometer hardware found",
                powerMa = baroSensor?.power ?: 0f,
                resolution = baroSensor?.resolution ?: 0f,
                maxRange = baroSensor?.maximumRange ?: 0f,
                description = "Required for sub-decimeter hypsometric elevation surveying and jobsite storm front detection.",
                dependentToolIds = baroTools,
                dependentToolNames = baroTools.map { getToolName(it) }
            )
        )

        // 5. Ambient Light Sensor
        val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        val lightTools = listOf("widget_surface_lux_meter")
        list.add(
            HardwareSensorSpec(
                id = "light",
                name = "Ambient Light Photometer",
                technicalName = "TYPE_LIGHT",
                category = SensorCategory.ENVIRONMENTAL,
                isAvailable = lightSensor != null,
                hardwareVendor = lightSensor?.vendor ?: "N/A",
                hardwareModel = lightSensor?.name ?: "No ambient light sensor found",
                powerMa = lightSensor?.power ?: 0f,
                resolution = lightSensor?.resolution ?: 0f,
                maxRange = lightSensor?.maximumRange ?: 0f,
                description = "Required for surface lux / foot-candle photometry and OSHA task illumination compliance.",
                dependentToolIds = lightTools,
                dependentToolNames = lightTools.map { getToolName(it) }
            )
        )

        // 6. Microphone (Acoustic Sound)
        val hasMic = packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
        val micTools = listOf("widget_decibel_meter")
        list.add(
            HardwareSensorSpec(
                id = "microphone",
                name = "Acoustic Audio Microphone",
                technicalName = "FEATURE_MICROPHONE",
                category = SensorCategory.OPTICAL_ACOUSTIC,
                isAvailable = hasMic,
                hardwareVendor = "Android Audio HAL",
                hardwareModel = if (hasMic) "Standard PCM Audio Input Device" else "No audio recording hardware detected",
                powerMa = 0f,
                resolution = 0f,
                maxRange = 140f,
                description = "Required for Decibel Sound Meter and OSHA 85dB noise hazard warnings.",
                dependentToolIds = micTools,
                dependentToolNames = micTools.map { getToolName(it) }
            )
        )

        // 7. Primary Optical Camera
        val hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) ||
                (cameraManager?.cameraIdList?.isNotEmpty() == true)
        val cameraTools = listOf("widget_ar_measurement", "widget_ar_area_calculator", "widget_qr_code_scanner")
        list.add(
            HardwareSensorSpec(
                id = "camera",
                name = "Primary Optical Camera",
                technicalName = "FEATURE_CAMERA_ANY",
                category = SensorCategory.OPTICAL_ACOUSTIC,
                isAvailable = hasCamera,
                hardwareVendor = "Camera2 API Subsystem",
                hardwareModel = if (hasCamera) "Integrated High-Definition Optical Sensor" else "No camera device detected",
                powerMa = 0f,
                resolution = 0f,
                maxRange = 0f,
                description = "Required for augmented reality length/height measurements, area perimeter solvers, and instant QR scanning.",
                dependentToolIds = cameraTools,
                dependentToolNames = cameraTools.map { getToolName(it) }
            )
        )

        // 8. Camera Flash / Strobe LED
        val hasFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        val flashTools = listOf("widget_strobe_tachometer")
        list.add(
            HardwareSensorSpec(
                id = "camera_flash",
                name = "High-Speed Flash / Torch LED",
                technicalName = "FEATURE_CAMERA_FLASH",
                category = SensorCategory.OPTICAL_ACOUSTIC,
                isAvailable = hasFlash,
                hardwareVendor = "LED Driver Subsystem",
                hardwareModel = if (hasFlash) "PWM High-Frequency Camera Torch LED" else "No camera flash hardware detected",
                powerMa = 0f,
                resolution = 0f,
                maxRange = 12000f,
                description = "Required for optical strobe tachometer to freeze rotating saw blades and motors.",
                dependentToolIds = flashTools,
                dependentToolNames = flashTools.map { getToolName(it) }
            )
        )

        // 9. Bluetooth Low Energy (BLE)
        val hasBle = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        val bleTools = listOf("widget_ble_multimeter", "widget_laser_measure")
        list.add(
            HardwareSensorSpec(
                id = "bluetooth_le",
                name = "Bluetooth Low Energy (BLE)",
                technicalName = "FEATURE_BLUETOOTH_LE",
                category = SensorCategory.CONNECTIVITY_HARDWARE,
                isAvailable = hasBle,
                hardwareVendor = "Bluetooth SIG Subsystem",
                hardwareModel = if (hasBle) "Bluetooth 4.0+ Smart LE Wireless Controller" else "No Bluetooth Low Energy controller found",
                powerMa = 0f,
                resolution = 0f,
                maxRange = 30f,
                description = "Required for wireless telemetry streaming from Fluke/Klein multimeters and Bosch/Leica laser measures.",
                dependentToolIds = bleTools,
                dependentToolNames = bleTools.map { getToolName(it) }
            )
        )

        // 10. Consumer Infrared (IR Blaster)
        val hasIr = (consumerIrManager?.hasIrEmitter() == true) ||
                packageManager.hasSystemFeature(PackageManager.FEATURE_CONSUMER_IR)
        val irTools = listOf("widget_jobsite_ir_remote", "widget_ir_remote")
        list.add(
            HardwareSensorSpec(
                id = "consumer_ir",
                name = "Consumer Infrared (IR Blaster)",
                technicalName = "FEATURE_CONSUMER_IR",
                category = SensorCategory.CONNECTIVITY_HARDWARE,
                isAvailable = hasIr,
                hardwareVendor = "Consumer IR Subsystem",
                hardwareModel = if (hasIr) "38kHz Carrier Hardware Infrared Transmitter" else "No hardware IR blaster found on this phone",
                powerMa = 0f,
                resolution = 0f,
                maxRange = 15f,
                description = "Required for direct offline control of jobsite mini-split ACs, shop air scrubbers, and heaters.",
                dependentToolIds = irTools,
                dependentToolNames = irTools.map { getToolName(it) }
            )
        )

        // 11. USB Host (OTG)
        val hasUsbHost = packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)
        val usbTools = listOf("widget_usb_endoscope", "widget_usb_pro_camera", "widget_thermal_camera")
        list.add(
            HardwareSensorSpec(
                id = "usb_host",
                name = "USB Host Controller (OTG)",
                technicalName = "FEATURE_USB_HOST",
                category = SensorCategory.CONNECTIVITY_HARDWARE,
                isAvailable = hasUsbHost,
                hardwareVendor = "USB OTG Host Controller",
                hardwareModel = if (hasUsbHost) "Direct USB-C / Micro-USB Host Controller" else "No USB Host hardware support detected",
                powerMa = 0f,
                resolution = 0f,
                maxRange = 0f,
                description = "Required for external USB-C borescope endoscopes, pro cameras, and thermal imagers.",
                dependentToolIds = usbTools,
                dependentToolNames = usbTools.map { getToolName(it) }
            )
        )

        val totalScanned = list.size
        val availableCount = list.count { it.isAvailable }
        val missingCount = totalScanned - availableCount

        val totalTools = ToolDefinition.ALL_TOOLS.size
        val unavailableToolIds = list.filter { !it.isAvailable }.flatMap { it.dependentToolIds }.toSet()
        val compatibleToolsCount = totalTools - unavailableToolIds.size

        return SensorAuditReport(
            totalScanned = totalScanned,
            availableCount = availableCount,
            missingCount = missingCount,
            compatibleToolsCount = compatibleToolsCount,
            totalToolsCount = totalTools,
            sensors = list
        )
    }

    /**
     * Checks if a specific tool is fully compatible with the physical hardware on this device.
     * Returns true if all required sensors are present, or false if any required sensor is missing.
     */
    fun isToolAvailable(toolId: String): Boolean {
        val missing = getMissingSensorsForTool(toolId)
        return missing.isEmpty()
    }

    /**
     * Returns the list of sensors required by this tool that are missing on this device.
     */
    fun getMissingSensorsForTool(toolId: String): List<HardwareSensorSpec> {
        val report = _auditReport.value
        return report.sensors.filter { !it.isAvailable && it.dependentToolIds.contains(toolId) }
    }

    /**
     * Returns the primary requirement description for a tool.
     */
    fun getToolRequirement(toolId: String): ToolSensorRequirement? {
        val report = _auditReport.value
        val sensorSpec = report.sensors.find { it.dependentToolIds.contains(toolId) } ?: return null
        return ToolSensorRequirement(
            toolId = toolId,
            toolTitle = getToolName(toolId),
            requiredSensorName = sensorSpec.name,
            sensorCategory = sensorSpec.category,
            isSensorAvailable = sensorSpec.isAvailable,
            hardwareNotes = if (sensorSpec.isAvailable) "Hardware present: ${sensorSpec.hardwareModel}" else "Hardware missing: Requires ${sensorSpec.name}"
        )
    }

    private fun getToolName(toolId: String): String {
        return ToolDefinition.ALL_TOOLS.find { it.id == toolId }?.title ?: toolId.replace("widget_", "").replace("_", " ").capitalizeWords()
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

    companion object {
        @Volatile
        private var instance: DeviceSensorDetector? = null

        fun getInstance(context: Context): DeviceSensorDetector {
            return instance ?: synchronized(this) {
                instance ?: DeviceSensorDetector(context).also { instance = it }
            }
        }
    }
}
