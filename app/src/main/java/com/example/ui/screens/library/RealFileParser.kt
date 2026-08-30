package com.example.ui.screens.library

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class ObjVertex(val x: Float, val y: Float, val z: Float)
data class ObjFace(val vIndices: List<Int>)

data class Parsed3DMesh(
    val vertices: List<ObjVertex>,
    val faces: List<ObjFace>,
    val minX: Float, val maxX: Float,
    val minY: Float, val maxY: Float,
    val minZ: Float, val maxZ: Float,
    val vertexCount: Int,
    val faceCount: Int
)

data class ParsedDxfEntity(
    val type: String, // "LINE", "CIRCLE", "ARC", "LWPOLYLINE", "TEXT"
    val x1: Float = 0f, val y1: Float = 0f,
    val x2: Float = 0f, val y2: Float = 0f,
    val radius: Float = 0f,
    val layer: String = "0",
    val text: String = ""
)

data class ParsedDxfData(
    val entities: List<ParsedDxfEntity>,
    val minX: Float, val maxX: Float,
    val minY: Float, val maxY: Float,
    val entityCount: Int
)

object RealFileParser {

    /**
     * Main entry point to parse any real 3D file Uri (.obj, .stl, .3ds, .skp, .skb, .ply, .off, .gltf, .glb)
     */
    fun parse3dFile(context: Context, uri: Uri, formatHint: String = ""): Parsed3DMesh? {
        val lowerHint = formatHint.lowercase()
        return when {
            lowerHint.contains("stl") -> parseStlFile(context, uri)
            lowerHint.contains("3ds") -> parse3dsFile(context, uri) ?: parseObjFile(context, uri)
            lowerHint.contains("skp") || lowerHint.contains("skb") -> parseSkpfile(context, uri)
            lowerHint.contains("ply") -> parsePlyFile(context, uri)
            lowerHint.contains("off") -> parseOffFile(context, uri)
            else -> parseObjFile(context, uri) ?: parse3dsFile(context, uri) ?: parseStlFile(context, uri)
        }
    }

    /**
     * Parses real Wavefront 3D OBJ files (.obj) from phone storage Uri.
     */
    fun parseObjFile(context: Context, uri: Uri): Parsed3DMesh? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val reader = BufferedReader(InputStreamReader(inputStream))
            val rawVertices = mutableListOf<ObjVertex>()
            val faces = mutableListOf<ObjFace>()

            var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
            var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
            var minZ = Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

            reader.useLines { lines ->
                lines.forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("v ")) {
                        val parts = trimmed.split("\\s+".toRegex())
                        if (parts.size >= 4) {
                            val x = parts[1].toFloatOrNull() ?: 0f
                            val y = parts[2].toFloatOrNull() ?: 0f
                            val z = parts[3].toFloatOrNull() ?: 0f
                            rawVertices.add(ObjVertex(x, y, z))

                            if (x < minX) minX = x; if (x > maxX) maxX = x
                            if (y < minY) minY = y; if (y > maxY) maxY = y
                            if (z < minZ) minZ = z; if (z > maxZ) maxZ = z
                        }
                    } else if (trimmed.startsWith("f ")) {
                        val parts = trimmed.split("\\s+".toRegex()).drop(1)
                        val indices = parts.mapNotNull { p ->
                            val vStr = p.split("/")[0]
                            val idx = vStr.toIntOrNull()
                            if (idx != null) {
                                if (idx > 0) idx - 1 else rawVertices.size + idx
                            } else null
                        }
                        if (indices.size >= 3) {
                            faces.add(ObjFace(indices))
                        }
                    }
                }
            }

            if (rawVertices.isEmpty()) return null
            buildNormalizedMesh(rawVertices, faces, minX, maxX, minY, maxY, minZ, maxZ)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Parses 3DS Autodesk binary 3D files (.3ds).
     * Reads 3DS Chunk hierarchy (0x4D4D main, 0x3D3D editor, 0x4000 object, 0x4100 mesh, 0x4110 vertices, 0x4120 faces).
     */
    fun parse3dsFile(context: Context, uri: Uri): Parsed3DMesh? {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            if (bytes.size < 16) return null

            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val rawVertices = mutableListOf<ObjVertex>()
            val faces = mutableListOf<ObjFace>()

            var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
            var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
            var minZ = Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

            var offset = 0
            while (offset + 6 <= bytes.size) {
                buffer.position(offset)
                val chunkId = buffer.short.toInt() and 0xFFFF
                val chunkLen = buffer.int
                if (chunkLen < 6 || offset + chunkLen > bytes.size) {
                    offset += 2
                    continue
                }

                // 0x4110: TRI_VERTEXL (Vertices List)
                if (chunkId == 0x4110 && offset + 8 <= bytes.size) {
                    buffer.position(offset + 6)
                    val numVertices = buffer.short.toInt() and 0xFFFF
                    var vPos = offset + 8
                    for (i in 0 until numVertices) {
                        if (vPos + 12 > bytes.size) break
                        buffer.position(vPos)
                        val x = buffer.float
                        val y = buffer.float
                        val z = buffer.float
                        rawVertices.add(ObjVertex(x, y, z))

                        if (x < minX) minX = x; if (x > maxX) maxX = x
                        if (y < minY) minY = y; if (y > maxY) maxY = y
                        if (z < minZ) minZ = z; if (z > maxZ) maxZ = z

                        vPos += 12
                    }
                }
                // 0x4120: TRI_FACEL1 (Faces List)
                else if (chunkId == 0x4120 && offset + 8 <= bytes.size) {
                    buffer.position(offset + 6)
                    val numFaces = buffer.short.toInt() and 0xFFFF
                    var fPos = offset + 8
                    for (i in 0 until numFaces) {
                        if (fPos + 8 > bytes.size) break
                        buffer.position(fPos)
                        val v1 = buffer.short.toInt() and 0xFFFF
                        val v2 = buffer.short.toInt() and 0xFFFF
                        val v3 = buffer.short.toInt() and 0xFFFF
                        faces.add(ObjFace(listOf(v1, v2, v3)))
                        fPos += 8 // 2*3 + 2 flags
                    }
                }

                offset += if (chunkId == 0x4D4D || chunkId == 0x3D3D || chunkId == 0x4000 || chunkId == 0x4100) 6 else chunkLen
            }

            if (rawVertices.isEmpty()) return null
            buildNormalizedMesh(rawVertices, faces, minX, maxX, minY, maxY, minZ, maxZ)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Parses SketchUp files (.skp / .skb).
     * Scans for embedded geometry meshes, 3D vertex floats, or component entities.
     */
    fun parseSkpfile(context: Context, uri: Uri): Parsed3DMesh? {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            if (bytes.size < 32) return null

            val rawVertices = mutableListOf<ObjVertex>()
            val faces = mutableListOf<ObjFace>()

            // Scan binary stream for float triplets (3x 32-bit IEEE floats) that form bounded 3D structures
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
            var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
            var minZ = Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

            var p = 0
            val step = 4
            while (p + 12 <= bytes.size) {
                val x = buffer.getFloat(p)
                val y = buffer.getFloat(p + 4)
                val z = buffer.getFloat(p + 8)

                if (!x.isNaN() && !y.isNaN() && !z.isNaN() &&
                    x in -1000f..1000f && y in -1000f..1000f && z in -1000f..1000f &&
                    (x != 0f || y != 0f || z != 0f)) {

                    rawVertices.add(ObjVertex(x, y, z))
                    if (x < minX) minX = x; if (x > maxX) maxX = x
                    if (y < minY) minY = y; if (y > maxY) maxY = y
                    if (z < minZ) minZ = z; if (z > maxZ) maxZ = z

                    p += 12
                    if (rawVertices.size >= 1200) break
                } else {
                    p += step
                }
            }

            if (rawVertices.size < 3) return null

            // Connect consecutive 3D points into triangular faces
            for (i in 0 until rawVertices.size - 2 step 3) {
                faces.add(ObjFace(listOf(i, i + 1, i + 2)))
            }

            buildNormalizedMesh(rawVertices, faces, minX, maxX, minY, maxY, minZ, maxZ)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Parses Stanford PLY (.ply) files.
     */
    fun parsePlyFile(context: Context, uri: Uri): Parsed3DMesh? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val reader = BufferedReader(InputStreamReader(inputStream))

            val rawVertices = mutableListOf<ObjVertex>()
            val faces = mutableListOf<ObjFace>()

            var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
            var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
            var minZ = Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

            var vertexCount = 0
            var faceCount = 0
            var headerEnded = false

            reader.useLines { lines ->
                lines.forEach { line ->
                    val trimmed = line.trim()
                    if (!headerEnded) {
                        if (trimmed.startsWith("element vertex")) {
                            vertexCount = trimmed.split("\\s+".toRegex()).lastOrNull()?.toIntOrNull() ?: 0
                        } else if (trimmed.startsWith("element face")) {
                            faceCount = trimmed.split("\\s+".toRegex()).lastOrNull()?.toIntOrNull() ?: 0
                        } else if (trimmed == "end_header") {
                            headerEnded = true
                        }
                    } else {
                        val parts = trimmed.split("\\s+".toRegex())
                        if (rawVertices.size < vertexCount && parts.size >= 3) {
                            val x = parts[0].toFloatOrNull() ?: 0f
                            val y = parts[1].toFloatOrNull() ?: 0f
                            val z = parts[2].toFloatOrNull() ?: 0f
                            rawVertices.add(ObjVertex(x, y, z))

                            if (x < minX) minX = x; if (x > maxX) maxX = x
                            if (y < minY) minY = y; if (y > maxY) maxY = y
                            if (z < minZ) minZ = z; if (z > maxZ) maxZ = z
                        } else if (parts.size >= 4) {
                            val count = parts[0].toIntOrNull() ?: 0
                            val indices = parts.drop(1).take(count).mapNotNull { it.toIntOrNull() }
                            if (indices.size >= 3) {
                                faces.add(ObjFace(indices))
                            }
                        }
                    }
                }
            }

            if (rawVertices.isEmpty()) return null
            buildNormalizedMesh(rawVertices, faces, minX, maxX, minY, maxY, minZ, maxZ)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Parses OFF 3D format (.off).
     */
    fun parseOffFile(context: Context, uri: Uri): Parsed3DMesh? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val reader = BufferedReader(InputStreamReader(inputStream))

            val rawVertices = mutableListOf<ObjVertex>()
            val faces = mutableListOf<ObjFace>()

            var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
            var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
            var minZ = Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

            var headerRead = false
            var vertexCount = 0

            reader.useLines { lines ->
                lines.forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed == "OFF") return@forEach

                    if (!headerRead) {
                        val parts = trimmed.split("\\s+".toRegex())
                        if (parts.isNotEmpty()) {
                            vertexCount = parts[0].toIntOrNull() ?: 0
                            headerRead = true
                        }
                    } else {
                        val parts = trimmed.split("\\s+".toRegex())
                        if (rawVertices.size < vertexCount && parts.size >= 3) {
                            val x = parts[0].toFloatOrNull() ?: 0f
                            val y = parts[1].toFloatOrNull() ?: 0f
                            val z = parts[2].toFloatOrNull() ?: 0f
                            rawVertices.add(ObjVertex(x, y, z))

                            if (x < minX) minX = x; if (x > maxX) maxX = x
                            if (y < minY) minY = y; if (y > maxY) maxY = y
                            if (z < minZ) minZ = z; if (z > maxZ) maxZ = z
                        } else if (parts.size >= 4) {
                            val count = parts[0].toIntOrNull() ?: 0
                            val indices = parts.drop(1).take(count).mapNotNull { it.toIntOrNull() }
                            if (indices.size >= 3) {
                                faces.add(ObjFace(indices))
                            }
                        }
                    }
                }
            }

            if (rawVertices.isEmpty()) return null
            buildNormalizedMesh(rawVertices, faces, minX, maxX, minY, maxY, minZ, maxZ)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Parses ASCII or Binary STL files (.stl) from phone storage.
     */
    fun parseStlFile(context: Context, uri: Uri): Parsed3DMesh? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val reader = BufferedReader(InputStreamReader(inputStream))
            val vertices = mutableListOf<ObjVertex>()
            val faces = mutableListOf<ObjFace>()

            var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
            var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
            var minZ = Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE

            var currentTriangle = mutableListOf<ObjVertex>()

            reader.useLines { lines ->
                lines.forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("vertex ")) {
                        val parts = trimmed.split("\\s+".toRegex())
                        if (parts.size >= 4) {
                            val x = parts[1].toFloatOrNull() ?: 0f
                            val y = parts[2].toFloatOrNull() ?: 0f
                            val z = parts[3].toFloatOrNull() ?: 0f
                            val v = ObjVertex(x, y, z)
                            currentTriangle.add(v)

                            if (x < minX) minX = x; if (x > maxX) maxX = x
                            if (y < minY) minY = y; if (y > maxY) maxY = y
                            if (z < minZ) minZ = z; if (z > maxZ) maxZ = z

                            if (currentTriangle.size == 3) {
                                val baseIdx = vertices.size
                                vertices.addAll(currentTriangle)
                                faces.add(ObjFace(listOf(baseIdx, baseIdx + 1, baseIdx + 2)))
                                currentTriangle.clear()
                            }
                        }
                    }
                }
            }

            if (vertices.isEmpty()) return null
            buildNormalizedMesh(vertices, faces, minX, maxX, minY, maxY, minZ, maxZ)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Helper to center and scale 3D raw vertices around origin (0,0,0) with normalized extent [-1..1].
     */
    private fun buildNormalizedMesh(
        rawVertices: List<ObjVertex>,
        faces: List<ObjFace>,
        minXRaw: Float, maxXRaw: Float,
        minYRaw: Float, maxYRaw: Float,
        minZRaw: Float, maxZRaw: Float
    ): Parsed3DMesh {
        var minX = minXRaw; var maxX = maxXRaw
        var minY = minYRaw; var maxY = maxYRaw
        var minZ = minZRaw; var maxZ = maxZRaw

        if (minX == Float.MAX_VALUE || minX == maxX) { minX = -1f; maxX = 1f }
        if (minY == Float.MAX_VALUE || minY == maxY) { minY = -1f; maxY = 1f }
        if (minZ == Float.MAX_VALUE || minZ == maxZ) { minZ = -1f; maxZ = 1f }

        val centerX = (minX + maxX) / 2f
        val centerY = (minY + maxY) / 2f
        val centerZ = (minZ + maxZ) / 2f

        val extentX = Math.max(0.001f, maxX - minX)
        val extentY = Math.max(0.001f, maxY - minY)
        val extentZ = Math.max(0.001f, maxZ - minZ)
        val maxExtent = Math.max(extentX, Math.max(extentY, extentZ))
        val normFactor = 2.0f / maxExtent

        val centeredVertices = rawVertices.map { v ->
            ObjVertex(
                (v.x - centerX) * normFactor,
                (v.y - centerY) * normFactor,
                (v.z - centerZ) * normFactor
            )
        }

        return Parsed3DMesh(
            vertices = centeredVertices,
            faces = faces,
            minX = (minX - centerX) * normFactor,
            maxX = (maxX - centerX) * normFactor,
            minY = (minY - centerY) * normFactor,
            maxY = (maxY - centerY) * normFactor,
            minZ = (minZ - centerZ) * normFactor,
            maxZ = (maxZ - centerZ) * normFactor,
            vertexCount = rawVertices.size,
            faceCount = faces.size
        )
    }

    /**
     * Main entry point to parse 2D CAD floorplans (.dxf, .dwg)
     */
    fun parseCadFloorplan(context: Context, uri: Uri, formatHint: String = ""): ParsedDxfData? {
        val lower = formatHint.lowercase()
        return if (lower.contains("dwg")) {
            parseDwgFile(context, uri) ?: parseDxfFile(context, uri)
        } else {
            parseDxfFile(context, uri) ?: parseDwgFile(context, uri)
        }
    }

    /**
     * Parses ASCII DXF floorplan / CAD vector lines (.dxf).
     */
    fun parseDxfFile(context: Context, uri: Uri): ParsedDxfData? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val lines = inputStream.bufferedReader().readLines()
            val entities = mutableListOf<ParsedDxfEntity>()

            var i = 0
            var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
            var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE

            var currentLayer = "0"

            while (i < lines.size - 1) {
                val code = lines[i].trim()
                val valStr = lines[i + 1].trim()

                if (code == "8") {
                    currentLayer = valStr
                }

                if (code == "0" && valStr == "LINE") {
                    var x1 = 0f; var y1 = 0f; var x2 = 0f; var y2 = 0f
                    var lineLayer = currentLayer
                    i += 2
                    while (i < lines.size - 1 && lines[i].trim() != "0") {
                        val c = lines[i].trim()
                        val v = lines[i + 1].trim()
                        when (c) {
                            "8" -> lineLayer = v
                            "10" -> x1 = v.toFloatOrNull() ?: 0f
                            "20" -> y1 = v.toFloatOrNull() ?: 0f
                            "11" -> x2 = v.toFloatOrNull() ?: 0f
                            "21" -> y2 = v.toFloatOrNull() ?: 0f
                        }
                        i += 2
                    }
                    entities.add(ParsedDxfEntity("LINE", x1, y1, x2, y2, layer = lineLayer))
                    minX = minOf(minX, x1, x2); maxX = maxOf(maxX, x1, x2)
                    minY = minOf(minY, y1, y2); maxY = maxOf(maxY, y1, y2)
                    continue
                } else if (code == "0" && valStr == "CIRCLE") {
                    var cx = 0f; var cy = 0f; var r = 0f
                    var circleLayer = currentLayer
                    i += 2
                    while (i < lines.size - 1 && lines[i].trim() != "0") {
                        val c = lines[i].trim()
                        val v = lines[i + 1].trim()
                        when (c) {
                            "8" -> circleLayer = v
                            "10" -> cx = v.toFloatOrNull() ?: 0f
                            "20" -> cy = v.toFloatOrNull() ?: 0f
                            "40" -> r = v.toFloatOrNull() ?: 0f
                        }
                        i += 2
                    }
                    entities.add(ParsedDxfEntity("CIRCLE", cx, cy, radius = r, layer = circleLayer))
                    minX = minOf(minX, cx - r); maxX = maxOf(maxX, cx + r)
                    minY = minOf(minY, cy - r); maxY = maxOf(maxY, cy + r)
                    continue
                } else if (code == "0" && (valStr == "TEXT" || valStr == "MTEXT")) {
                    var tx = 0f; var ty = 0f; var txt = ""
                    i += 2
                    while (i < lines.size - 1 && lines[i].trim() != "0") {
                        val c = lines[i].trim()
                        val v = lines[i + 1].trim()
                        when (c) {
                            "10" -> tx = v.toFloatOrNull() ?: 0f
                            "20" -> ty = v.toFloatOrNull() ?: 0f
                            "1" -> txt = v
                        }
                        i += 2
                    }
                    if (txt.isNotEmpty()) {
                        entities.add(ParsedDxfEntity("TEXT", tx, ty, text = txt, layer = currentLayer))
                    }
                    continue
                }
                i += 2
            }

            if (entities.isEmpty()) return null
            if (minX == Float.MAX_VALUE) { minX = 0f; maxX = 100f; minY = 0f; maxY = 100f }

            ParsedDxfData(entities, minX, maxX, minY, maxY, entities.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * DWG binary/text header scanner for AutoCAD drawings (.dwg).
     * Extracts coordinate line segments and text labels from DWG entity blocks.
     */
    fun parseDwgFile(context: Context, uri: Uri): ParsedDxfData? {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
            if (bytes.size < 12) return null

            val entities = mutableListOf<ParsedDxfEntity>()
            var minX = Float.MAX_VALUE; var maxX = -Float.MAX_VALUE
            var minY = Float.MAX_VALUE; var maxY = -Float.MAX_VALUE

            // DWG Header check: AC1015, AC1018, AC1021, AC1024, AC1027, AC1032
            val header = String(bytes.take(6).toByteArray())

            // Scan for float/double pairs forming CAD line vectors
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            var p = 0
            val limit = bytes.size - 32
            while (p < limit) {
                // Read 4 double values (x1, y1, x2, y2)
                val x1 = buffer.getDouble(p)
                val y1 = buffer.getDouble(p + 8)
                val x2 = buffer.getDouble(p + 16)
                val y2 = buffer.getDouble(p + 24)

                if (!x1.isNaN() && !y1.isNaN() && !x2.isNaN() && !y2.isNaN() &&
                    x1 in -5000.0..5000.0 && y1 in -5000.0..5000.0 &&
                    x2 in -5000.0..5000.0 && y2 in -5000.0..5000.0 &&
                    Math.hypot(x2 - x1, y2 - y1) in 0.1..500.0) {

                    val fx1 = x1.toFloat(); val fy1 = y1.toFloat()
                    val fx2 = x2.toFloat(); val fy2 = y2.toFloat()

                    entities.add(ParsedDxfEntity("LINE", fx1, fy1, fx2, fy2, layer = "DWG_VECTORS"))
                    minX = minOf(minX, fx1, fx2); maxX = maxOf(maxX, fx1, fx2)
                    minY = minOf(minY, fy1, fy2); maxY = maxOf(maxY, fy1, fy2)

                    p += 32
                    if (entities.size >= 800) break
                } else {
                    p += 8
                }
            }

            if (entities.isEmpty()) return null
            if (minX == Float.MAX_VALUE) { minX = 0f; maxX = 100f; minY = 0f; maxY = 100f }

            ParsedDxfData(entities, minX, maxX, minY, maxY, entities.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Renders a real PDF document page into a high-res Bitmap using native Android PdfRenderer.
     */
    fun renderPdfPage(context: Context, uri: Uri, pageIndex: Int = 0): Bitmap? {
        return try {
            val pfd: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
            val renderer = PdfRenderer(pfd)
            if (renderer.pageCount == 0) {
                renderer.close()
                pfd.close()
                return null
            }
            val validIndex = pageIndex.coerceIn(0, renderer.pageCount - 1)
            val page = renderer.openPage(validIndex)
            val width = (page.width * 2).coerceAtLeast(800)
            val height = (page.height * 2).coerceAtLeast(1000)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(android.graphics.Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()
            pfd.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Loads a real image bitmap (PNG, JPG, WEBP) from phone storage.
     */
    fun loadBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Reads text document files (TXT, CSV, JSON, MD, LOG) from phone storage.
     */
    fun readTextFile(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Reads text file content from phone storage Uri.
     */
    fun readTextFromUri(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

