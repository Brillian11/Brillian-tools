package com.example.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import kotlinx.coroutines.delay

/**
 * Encapsulates infrared protocol encoding (NEC, Sony SIRC, RC5, Pronto HEX, and Raw timings)
 * for physical transmission through Android ConsumerIrManager.
 */
object IrProtocolEncoder {

    /**
     * Encodes 32-bit NEC protocol command (Address 16-bit, Command 8-bit, ~Command 8-bit or extended 32-bit).
     * Carrier: 38,000 Hz.
     * Lead-in: 9000µs mark, 4500µs space
     * Bit 0: 560µs mark, 560µs space
     * Bit 1: 560µs mark, 1690µs space
     * Stop bit: 560µs mark
     */
    fun encodeNec(address: Int, command: Int): IntArray {
        val pattern = ArrayList<Int>()
        // Header
        pattern.add(9000)
        pattern.add(4500)

        // 16-bit address (or 8-bit address + inverted address)
        val fullData: Long = if (address > 0xFF) {
            // Extended NEC (16-bit address + 8-bit command + 8-bit inverted command)
            val cmdInv = (command.inv() and 0xFF).toLong()
            (address.toLong() and 0xFFFFL) or ((command.toLong() and 0xFFL) shl 16) or (cmdInv shl 24)
        } else {
            // Standard NEC (8-bit address, 8-bit ~address, 8-bit cmd, 8-bit ~cmd)
            val addrInv = (address.inv() and 0xFF).toLong()
            val cmdInv = (command.inv() and 0xFF).toLong()
            (address.toLong() and 0xFFL) or (addrInv shl 8) or ((command.toLong() and 0xFFL) shl 16) or (cmdInv shl 24)
        }

        // Transmit 32 bits, LSB first
        for (i in 0 until 32) {
            val bit = (fullData shr i) and 1L
            pattern.add(560)
            if (bit == 1L) {
                pattern.add(1690)
            } else {
                pattern.add(560)
            }
        }

        // Stop bit
        pattern.add(560)
        return pattern.toIntArray()
    }

    /**
     * Encodes Sony SIRC 12-bit protocol (7-bit command, 5-bit address).
     * Carrier: 40,000 Hz.
     * Header: 2400µs mark, 600µs space
     * Bit 0: 600µs mark, 600µs space
     * Bit 1: 1200µs mark, 600µs space
     */
    fun encodeSony12(address: Int, command: Int): IntArray {
        val pattern = ArrayList<Int>()
        pattern.add(2400)
        pattern.add(600)

        // 7-bit command
        for (i in 0 until 7) {
            val bit = (command shr i) and 1
            if (bit == 1) {
                pattern.add(1200)
            } else {
                pattern.add(600)
            }
            pattern.add(600)
        }

        // 5-bit address
        for (i in 0 until 5) {
            val bit = (address shr i) and 1
            if (bit == 1) {
                pattern.add(1200)
            } else {
                pattern.add(600)
            }
            pattern.add(600)
        }

        return pattern.toIntArray()
    }

    /**
     * Encodes Philips RC5 protocol (Manchester encoding @ 36,000 Hz).
     */
    fun encodeRc5(address: Int, command: Int, toggle: Boolean = false): IntArray {
        val pattern = ArrayList<Int>()
        val halfBitUs = 889

        // 14 bits: S1(1), S2(1), Toggle(1), 5-bit Address, 6-bit Command
        var bits = (1 shl 13) or (1 shl 12) or ((if (toggle) 1 else 0) shl 11)
        bits = bits or ((address and 0x1F) shl 6)
        bits = bits or (command and 0x3F)

        // Build Manchester stream
        var currentLevel = false // false = space, true = mark
        var currentDuration = 0

        for (i in 13 downTo 0) {
            val bit = (bits shr i) and 1
            // In RC5: '1' is space then mark, '0' is mark then space
            val firstHalf = bit == 0
            val secondHalf = bit == 1

            // 1st half
            if (firstHalf == currentLevel) {
                currentDuration += halfBitUs
            } else {
                if (currentDuration > 0) pattern.add(currentDuration)
                currentLevel = firstHalf
                currentDuration = halfBitUs
            }

            // 2nd half
            if (secondHalf == currentLevel) {
                currentDuration += halfBitUs
            } else {
                if (currentDuration > 0) pattern.add(currentDuration)
                currentLevel = secondHalf
                currentDuration = halfBitUs
            }
        }
        if (currentDuration > 0) pattern.add(currentDuration)

        return pattern.toIntArray()
    }

    /**
     * Parses standard Pronto Hex format (4-digit hex strings).
     * e.g., "0000 006D 0022 0002 0155 00AA 0015 0015 ..."
     * Returns Pair<CarrierFrequencyHz, IntArrayOfMicroseconds>
     */
    fun parseProntoHex(hexString: String): Pair<Int, IntArray>? {
        val tokens = hexString.trim().split("\\s+".toRegex())
        if (tokens.size < 4) return null

        try {
            val preamble = tokens[0].toInt(16)
            if (preamble != 0x0000) return null // Standard raw format starts with 0000

            val frequencyCode = tokens[1].toInt(16)
            val carrierFrequency = if (frequencyCode > 0) (1000000.0 / (frequencyCode * 0.241246)).toInt() else 38000
            val burstPairSeq1 = tokens[2].toInt(16)
            val burstPairSeq2 = tokens[3].toInt(16)

            val totalPairs = burstPairSeq1 + burstPairSeq2
            val microsecondMultiplier = 1000000.0 / carrierFrequency

            val pattern = ArrayList<Int>()
            var idx = 4
            for (i in 0 until totalPairs) {
                if (idx + 1 >= tokens.size) break
                val markCycles = tokens[idx++].toInt(16)
                val spaceCycles = tokens[idx++].toInt(16)
                pattern.add((markCycles * microsecondMultiplier).toInt())
                pattern.add((spaceCycles * microsecondMultiplier).toInt())
            }

            return Pair(carrierFrequency, pattern.toIntArray())
        } catch (e: Exception) {
            return null
        }
    }
}

/**
 * Live record of an optical transmission for display and diagnostic feedback.
 */
data class IrTransmissionEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestampMs: Long = System.currentTimeMillis(),
    val title: String,
    val brand: String,
    val category: String,
    val frequencyHz: Int,
    val pulseCount: Int,
    val totalDurationUs: Int,
    val timingPattern: IntArray,
    val hexSignature: String,
    val isHardwareTransmitted: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as IrTransmissionEvent
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Primary Controller bridging physical ConsumerIrManager and jobsite operations.
 */
class JobsiteIrController(private val context: Context) {

    private val irManager: ConsumerIrManager? = try {
        context.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
    } catch (e: Exception) {
        null
    }

    fun isHardwareSupported(): Boolean {
        return try {
            irManager?.hasIrEmitter() == true
        } catch (e: Exception) {
            false
        }
    }

    fun getCarrierFrequencyRanges(): List<Pair<Int, Int>> {
        val ranges = ArrayList<Pair<Int, Int>>()
        try {
            irManager?.carrierFrequencies?.forEach { range ->
                ranges.add(Pair(range.minFrequency, range.maxFrequency))
            }
        } catch (e: Exception) {
            // Ignore
        }
        return ranges
    }

    /**
     * Transmits raw pulse pattern via hardware IR emitter if present,
     * returning a diagnostic transmission event for UI waveform visualization.
     */
    fun transmit(
        title: String,
        brand: String,
        category: String,
        frequencyHz: Int = 38000,
        patternUs: IntArray,
        hexSignature: String = ""
    ): IrTransmissionEvent {
        val hasHw = isHardwareSupported()
        var transmitted = false

        if (hasHw && patternUs.isNotEmpty()) {
            try {
                irManager?.transmit(frequencyHz, patternUs)
                transmitted = true
            } catch (e: Exception) {
                transmitted = false
            }
        }

        val totalDuration = patternUs.sum()
        return IrTransmissionEvent(
            title = title,
            brand = brand,
            category = category,
            frequencyHz = frequencyHz,
            pulseCount = patternUs.size,
            totalDurationUs = totalDuration,
            timingPattern = patternUs,
            hexSignature = hexSignature.ifEmpty { "0x${patternUs.size.toString(16).uppercase()}-BURST" },
            isHardwareTransmitted = transmitted
        )
    }

    /**
     * Executes a queued macro sequence with millisecond delays between commands.
     */
    suspend fun executeMacro(
        macroName: String,
        brand: String,
        category: String,
        steps: List<MacroStep>,
        onStepProgress: (stepIndex: Int, stepTitle: String, event: IrTransmissionEvent) -> Unit
    ) {
        steps.forEachIndexed { index, step ->
            val event = transmit(
                title = "${macroName}: ${step.title}",
                brand = brand,
                category = category,
                frequencyHz = step.frequencyHz,
                patternUs = step.patternUs,
                hexSignature = step.hexCode
            )
            onStepProgress(index, step.title, event)
            if (step.delayAfterMs > 0) {
                delay(step.delayAfterMs)
            }
        }
    }
}

data class MacroStep(
    val title: String,
    val frequencyHz: Int = 38000,
    val patternUs: IntArray,
    val hexCode: String,
    val delayAfterMs: Long = 400L
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MacroStep
        return title == other.title && hexCode == other.hexCode
    }

    override fun hashCode(): Int = title.hashCode()
}
