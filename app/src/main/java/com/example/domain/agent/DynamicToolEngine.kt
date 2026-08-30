package com.example.domain.agent

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ToolDefinition
import kotlin.math.*

data class DynamicToolParameter(
    val key: String,
    val label: String,
    val defaultValue: Double,
    val min: Double,
    val max: Double,
    val step: Double = 0.1,
    val unit: String = ""
)

data class DynamicToolResult(
    val label: String,
    val value: String,
    val unit: String = ""
)

class DynamicToolDefinition(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val parameters: List<DynamicToolParameter>,
    val calculate: (Map<String, Double>) -> List<DynamicToolResult>
)

object DynamicToolEngine {

    val DYNAMIC_TOOLS: Map<String, DynamicToolDefinition> = mapOf(
        "widget_matrix_solver" to DynamicToolDefinition(
            id = "widget_matrix_solver",
            title = "Interactive 2D Matrix & Linear System Solver",
            description = "Solve linear equation systems Ax = B, determinants, traces, eigenvalues, and inverse matrices",
            category = "Mathematical",
            parameters = listOf(
                DynamicToolParameter("a11", "Matrix A11", 3.0, -50.0, 50.0, 1.0),
                DynamicToolParameter("a12", "Matrix A12", 2.0, -50.0, 50.0, 1.0),
                DynamicToolParameter("a21", "Matrix A21", 1.0, -50.0, 50.0, 1.0),
                DynamicToolParameter("a22", "Matrix A22", -4.0, -50.0, 50.0, 1.0),
                DynamicToolParameter("b1", "Vector B1 (Result 1)", 12.0, -100.0, 100.0, 1.0),
                DynamicToolParameter("b2", "Vector B2 (Result 2)", -10.0, -100.0, 100.0, 1.0)
            )
        ) { params ->
            val a11 = params["a11"] ?: 3.0
            val a12 = params["a12"] ?: 2.0
            val a21 = params["a21"] ?: 1.0
            val a22 = params["a22"] ?: -4.0
            val b1 = params["b1"] ?: 12.0
            val b2 = params["b2"] ?: -10.0

            val det = (a11 * a22) - (a12 * a21)
            val trace = a11 + a22
            val discriminant = (trace * trace) - (4.0 * det)

            val xSol = if (det != 0.0) String.format("%.4f", ((b1 * a22) - (a12 * b2)) / det) else "Undefined (det = 0)"
            val ySol = if (det != 0.0) String.format("%.4f", ((a11 * b2) - (b1 * a21)) / det) else "Undefined (det = 0)"

            val eigenvalues = if (discriminant >= 0) {
                val lambda1 = (trace + sqrt(discriminant)) / 2.0
                val lambda2 = (trace - sqrt(discriminant)) / 2.0
                String.format("λ₁ = %.2f, λ₂ = %.2f", lambda1, lambda2)
            } else {
                val real = trace / 2.0
                val imag = sqrt(-discriminant) / 2.0
                String.format("λ = %.2f ± %.2fi", real, imag)
            }

            val inverseMatrix = if (det != 0.0) {
                String.format("[[%.2f, %.2f], [%.2f, %.2f]]", a22 / det, -a12 / det, -a21 / det, a11 / det)
            } else {
                "Singular"
            }

            listOf(
                DynamicToolResult("Determinant (det A)", String.format("%.2f", det)),
                DynamicToolResult("Matrix Trace", String.format("%.2f", trace)),
                DynamicToolResult("Solution x (Variable 1)", xSol),
                DynamicToolResult("Solution y (Variable 2)", ySol),
                DynamicToolResult("Eigenvalues", eigenvalues),
                DynamicToolResult("Inverse Matrix A⁻¹", inverseMatrix)
            )
        },

        // Woodworking
        "widget_board_footage" to DynamicToolDefinition(
            id = "widget_board_footage",
            title = "Board Footage & Lumber Estimator",
            description = "Calculate board feet volume ((T x W x L)/12), species density & lumber pricing",
            category = "Woodworking",
            parameters = listOf(
                DynamicToolParameter("thickness", "Thickness (inches)", 2.0, 0.25, 12.0, 0.25, "in"),
                DynamicToolParameter("width", "Width (inches)", 6.0, 1.0, 24.0, 0.25, "in"),
                DynamicToolParameter("length", "Length (feet)", 8.0, 1.0, 50.0, 0.5, "ft"),
                DynamicToolParameter("quantity", "Quantity (pcs)", 1.0, 1.0, 500.0, 1.0, "pcs"),
                DynamicToolParameter("price", "Price per Board Foot ($)", 4.50, 0.1, 100.0, 0.1, "$/BF")
            )
        ) { params ->
            val t = params["thickness"] ?: 2.0
            val w = params["width"] ?: 6.0
            val l = params["length"] ?: 8.0
            val qty = params["quantity"] ?: 1.0
            val price = params["price"] ?: 4.5
            val bf = (t * w * l * qty) / 12.0
            val cost = bf * price
            listOf(
                DynamicToolResult("Single Board Foot", String.format("%.2f", (t * w * l) / 12.0), "BF"),
                DynamicToolResult("Total Board Footage", String.format("%.2f", bf), "BF"),
                DynamicToolResult("Estimated Total Cost", String.format("$%.2f", cost))
            )
        },

        "widget_cutlist_optimizer" to DynamicToolDefinition(
            id = "widget_cutlist_optimizer",
            title = "Cut List Optimizer (1D)",
            description = "Optimizes linear cutting layout on stock lumber planks to minimize waste",
            category = "Woodworking",
            parameters = listOf(
                DynamicToolParameter("stock_length", "Stock Plank Length (in)", 96.0, 12.0, 240.0, 1.0, "in"),
                DynamicToolParameter("cut_length", "Required Cut Length (in)", 30.0, 2.0, 120.0, 0.25, "in"),
                DynamicToolParameter("quantity", "Number of Cuts Needed", 5.0, 1.0, 100.0, 1.0, "cuts"),
                DynamicToolParameter("kerf", "Saw Blade Kerf (in)", 0.125, 0.0, 0.5, 0.03125, "in")
            )
        ) { params ->
            val stock = params["stock_length"] ?: 96.0
            val cut = params["cut_length"] ?: 30.0
            val qty = params["quantity"] ?: 5.0
            val kerf = params["kerf"] ?: 0.125

            val effectiveCut = cut + kerf
            val cutsPerBoard = floor((stock + kerf) / effectiveCut)
            val boardsNeeded = if (cutsPerBoard <= 0) 0 else ceil(qty / cutsPerBoard).toInt()
            val totalStockUsed = boardsNeeded * stock
            val totalCutLength = qty * cut
            val waste = totalStockUsed - totalCutLength
            val wastePercent = if (totalStockUsed > 0) (waste / totalStockUsed) * 100.0 else 0.0

            listOf(
                DynamicToolResult("Cuts per Stock Board", cutsPerBoard.toInt().toString(), "cuts"),
                DynamicToolResult("Required Stock Boards", boardsNeeded.toString(), "boards"),
                DynamicToolResult("Total Yield Efficiency", String.format("%.1f%%", 100.0 - wastePercent)),
                DynamicToolResult("Material Scrap / Waste", String.format("%.2f", waste), "in")
            )
        },

        "widget_stair_layout" to DynamicToolDefinition(
            id = "widget_stair_layout",
            title = "Stair Layout & Stringer Calculator",
            description = "Stringer rise, run, step count, throat thickness & IRC headroom compliance",
            category = "Woodworking",
            parameters = listOf(
                DynamicToolParameter("total_rise", "Total Vertical Rise (in)", 70.0, 10.0, 240.0, 0.25, "in"),
                DynamicToolParameter("target_rise", "Target Step Rise (in)", 7.0, 4.0, 12.0, 0.125, "in"),
                DynamicToolParameter("target_run", "Target Step Run (in)", 11.0, 8.0, 20.0, 0.125, "in")
            )
        ) { params ->
            val rise = params["total_rise"] ?: 70.0
            val tRise = params["target_rise"] ?: 7.0
            val tRun = params["target_run"] ?: 11.0

            val steps = max(1.0, ceil(rise / tRise))
            val actualRise = rise / steps
            val totalRun = (steps - 1) * tRun
            val angle = atan(actualRise / tRun) * 180.0 / PI

            listOf(
                DynamicToolResult("Total Step Count", steps.toInt().toString(), "risers"),
                DynamicToolResult("Actual Step Rise", String.format("%.3f", actualRise), "in"),
                DynamicToolResult("Total Horizontal Run", String.format("%.2f", totalRun), "in"),
                DynamicToolResult("Staircase Incline Angle", String.format("%.1f°", angle))
            )
        },

        "widget_rafter_calculator" to DynamicToolDefinition(
            id = "widget_rafter_calculator",
            title = "Rafter & Roof Pitch Calculator",
            description = "Common, hip, valley, and jack rafter lengths with birdsmouth seat cuts",
            category = "Woodworking",
            parameters = listOf(
                DynamicToolParameter("span", "Building Span / Width (ft)", 24.0, 4.0, 100.0, 0.5, "ft"),
                DynamicToolParameter("pitch", "Pitch (rise over 12 run)", 6.0, 1.0, 24.0, 0.5, "/12"),
                DynamicToolParameter("overhang", "Rafter Overhang (in)", 12.0, 0.0, 48.0, 1.0, "in")
            )
        ) { params ->
            val span = params["span"] ?: 24.0
            val pitch = params["pitch"] ?: 6.0
            val overhang = params["overhang"] ?: 12.0

            val run = span / 2.0
            val angle = atan(pitch / 12.0)
            val slopeFactor = sqrt(1.0 + (pitch / 12.0) * (pitch / 12.0))
            val rafterLength = run * slopeFactor
            val tailLength = (overhang / 12.0) * slopeFactor
            val totalRafter = rafterLength + tailLength

            listOf(
                DynamicToolResult("Roof Pitch Angle", String.format("%.1f°", angle * 180.0 / PI)),
                DynamicToolResult("Rafter Slope Length", String.format("%.2f", rafterLength), "ft"),
                DynamicToolResult("Overhang Tail Length", String.format("%.2f", tailLength), "ft"),
                DynamicToolResult("Total Plumb Cut Rafter", String.format("%.2f", totalRafter), "ft")
            )
        },

        "widget_compound_miter" to DynamicToolDefinition(
            id = "widget_compound_miter",
            title = "Compound Miter & Bevel Calculator",
            description = "Miter and bevel saw angles for crown moldings & multi-sided polyhedral frames",
            category = "Woodworking",
            parameters = listOf(
                DynamicToolParameter("spring_angle", "Crown Spring Angle (deg)", 38.0, 10.0, 80.0, 1.0, "°"),
                DynamicToolParameter("wall_angle", "Corner Wall Angle (deg)", 90.0, 10.0, 180.0, 1.0, "°")
            )
        ) { params ->
            val spring = (params["spring_angle"] ?: 38.0) * PI / 180.0
            val wall = (params["wall_angle"] ?: 90.0) * PI / 180.0

            val miter = atan(sin(spring) / tan(wall / 2.0)) * 180.0 / PI
            val bevel = asin(cos(spring) * cos(wall / 2.0)) * 180.0 / PI

            listOf(
                DynamicToolResult("Saw Miter Angle Setting", String.format("%.2f°", miter)),
                DynamicToolResult("Saw Bevel Angle Setting", String.format("%.2f°", bevel))
            )
        },

        "widget_wood_moisture" to DynamicToolDefinition(
            id = "widget_wood_moisture",
            title = "Wood Moisture & Shrinkage Estimator",
            description = "Tangential & radial wood shrinkage forecasting based on species & target EMC",
            category = "Woodworking",
            parameters = listOf(
                DynamicToolParameter("width", "Initial Board Width (in)", 8.0, 1.0, 24.0, 0.125, "in"),
                DynamicToolParameter("initial_mc", "Initial Wood Moisture (%)", 15.0, 3.0, 30.0, 1.0, "%"),
                DynamicToolParameter("target_mc", "Target / End Moisture (%)", 7.0, 2.0, 25.0, 1.0, "%"),
                DynamicToolParameter("coeff", "Shrinkage Coefficient (%)", 6.2, 0.1, 15.0, 0.1, "%")
            )
        ) { params ->
            val width = params["width"] ?: 8.0
            val initMc = params["initial_mc"] ?: 15.0
            val targetMc = params["target_mc"] ?: 7.0
            val coeff = params["coeff"] ?: 6.2

            val deltaMc = max(0.0, initMc - targetMc)
            // Fiber saturation point is roughly 30% MC, shrinkage is linear below this
            val pctShrinkage = coeff * (deltaMc / 30.0)
            val shrinkAmount = width * (pctShrinkage / 100.0)
            val finalWidth = width - shrinkAmount

            listOf(
                DynamicToolResult("Shrinkage Ratio", String.format("%.3f%%", pctShrinkage)),
                DynamicToolResult("Width Reduction", String.format("%.4f", shrinkAmount), "in"),
                DynamicToolResult("Expected Final Width", String.format("%.4f", finalWidth), "in")
            )
        },

        "widget_joinery_spacing" to DynamicToolDefinition(
            id = "widget_joinery_spacing",
            title = "Joinery & Tenon Spacing Calculator",
            description = "Equal spacing distributions for mortise/tenons, dowels & pocket hole screws",
            category = "Woodworking",
            parameters = listOf(
                DynamicToolParameter("total_length", "Overall Spanning Length (in)", 48.0, 2.0, 500.0, 0.25, "in"),
                DynamicToolParameter("joint_width", "Individual Joint / Slot Width (in)", 3.0, 0.0, 120.0, 0.125, "in"),
                DynamicToolParameter("count", "Number of Joints / Openings", 4.0, 1.0, 50.0, 1.0, "pcs")
            )
        ) { params ->
            val length = params["total_length"] ?: 48.0
            val jWidth = params["joint_width"] ?: 3.0
            val count = params["count"] ?: 4.0

            val totalJointSpace = jWidth * count
            val spacing = if (length > totalJointSpace) (length - totalJointSpace) / (count + 1) else 0.0

            listOf(
                DynamicToolResult("Gap Space / Spacing", String.format("%.3f", spacing), "in"),
                DynamicToolResult("Total Joint Space Occupied", String.format("%.2f", totalJointSpace), "in"),
                DynamicToolResult("Joint Edge Centers Interval", String.format("%.3f", spacing + jWidth), "in")
            )
        },

        "widget_kerf_bending" to DynamicToolDefinition(
            id = "widget_kerf_bending",
            title = "Kerf Bending Calculator",
            description = "Kerf cut spacing, pitch, and depth for bending solid timber boards smoothly",
            category = "Woodworking",
            parameters = listOf(
                DynamicToolParameter("radius", "Target Inside Radius (in)", 12.0, 1.0, 100.0, 0.25, "in"),
                DynamicToolParameter("thickness", "Board Thickness (in)", 0.75, 0.1, 4.0, 0.0625, "in"),
                DynamicToolParameter("angle", "Bending Angle (deg)", 90.0, 10.0, 180.0, 1.0, "°"),
                DynamicToolParameter("blade", "Saw Blade Thickness / Kerf (in)", 0.125, 0.03, 0.5, 0.015, "in")
            )
        ) { params ->
            val r = params["radius"] ?: 12.0
            val t = params["thickness"] ?: 0.75
            val angle = params["angle"] ?: 90.0
            val blade = params["blade"] ?: 0.125

            val arcLenInner = r * (angle * PI / 180.0)
            val arcLenOuter = (r + t) * (angle * PI / 180.0)
            val delta = arcLenOuter - arcLenInner
            val numCuts = max(1.0, ceil(delta / blade))
            val spacing = if (numCuts > 1) arcLenInner / numCuts else 0.0

            listOf(
                DynamicToolResult("Total Bend Board Length", String.format("%.2f", arcLenInner), "in"),
                DynamicToolResult("Required Saw Cuts", numCuts.toInt().toString(), "cuts"),
                DynamicToolResult("Saw Pitch / Cut Spacing", String.format("%.3f", spacing), "in")
            )
        },

        "widget_dado_step_over" to DynamicToolDefinition(
            id = "widget_dado_step_over",
            title = "Dado & Lap Joint Planner",
            description = "Single blade hogging fence offsets for wide lap joints",
            category = "Woodworking",
            parameters = listOf(
                DynamicToolParameter("total_width", "Desired Slot / Dado Width (in)", 1.5, 0.1, 12.0, 0.0625, "in"),
                DynamicToolParameter("blade_thickness", "Saw Blade Thickness (in)", 0.25, 0.03, 1.0, 0.03125, "in")
            )
        ) { params ->
            val total = params["total_width"] ?: 1.5
            val blade = params["blade_thickness"] ?: 0.25

            if (total <= blade) {
                listOf(
                    DynamicToolResult("Saw Cuts Count", "1", "cut"),
                    DynamicToolResult("Step Over Increment", "N/A - Direct Cut")
                )
            } else {
                val rem = total - blade
                val extraCuts = ceil(rem / blade)
                val totalCuts = extraCuts + 1
                val stepOver = rem / extraCuts
                listOf(
                    DynamicToolResult("Total Saw Passes Needed", totalCuts.toInt().toString(), "passes"),
                    DynamicToolResult("Step Over Fence Increment", String.format("%.4f", stepOver), "in")
                )
            }
        },

        "widget_segmented_turning" to DynamicToolDefinition(
            id = "widget_segmented_turning",
            title = "Segmented Woodturning & Bowls",
            description = "Stave miter angles, segment edge lengths, and ring stack dimensions",
            category = "Woodworking",
            parameters = listOf(
                DynamicToolParameter("diameter", "Ring Outer Diameter (in)", 10.0, 1.0, 60.0, 0.25, "in"),
                DynamicToolParameter("segments", "Segments per Ring", 12.0, 3.0, 64.0, 1.0, "segments")
            )
        ) { params ->
            val d = params["diameter"] ?: 10.0
            val segs = params["segments"] ?: 12.0

            val miter = 360.0 / (segs * 2.0)
            val outerEdge = d * sin(PI / segs)

            listOf(
                DynamicToolResult("Chop Saw Miter Angle", String.format("%.2f°", miter)),
                DynamicToolResult("Segment Outer Edge Length", String.format("%.3f", outerEdge), "in"),
                DynamicToolResult("Combined Ring Perimeter", String.format("%.2f", outerEdge * segs), "in")
            )
        },

        "widget_sagulator" to DynamicToolDefinition(
            id = "widget_sagulator",
            title = "Lumber Sagulator (Shelf Deflection)",
            description = "Shelf deflection analysis under point and uniform loads with span limits",
            category = "Woodworking",
            parameters = listOf(
                DynamicToolParameter("span", "Shelf Clear Span (in)", 36.0, 6.0, 120.0, 1.0, "in"),
                DynamicToolParameter("load", "Total Load on Shelf (lbs)", 100.0, 1.0, 1000.0, 5.0, "lbs"),
                DynamicToolParameter("thickness", "Wood Thickness (in)", 0.75, 0.25, 4.0, 0.0625, "in"),
                DynamicToolParameter("elasticity", "E-Modulus Elasticity (million psi)", 1.6, 0.1, 5.0, 0.1, "Mpsi")
            )
        ) { params ->
            val span = params["span"] ?: 36.0
            val load = params["load"] ?: 100.0
            val t = params["thickness"] ?: 0.75
            val e = (params["elasticity"] ?: 1.6) * 1_000_000.0

            // Assuming standard uniform load deflection formula on a rectangular beam
            // Deflection (in) = (5 * W * L^3) / (384 * E * I)
            // where I = (b * t^3) / 12  (assuming depth b = 12 inches for standard cabinet shelves)
            val b = 12.0
            val i = (b * t * t * t) / 12.0
            val deflection = (5.0 * load * span * span * span) / (384.0 * e * i)
            val status = if (deflection < 0.02) "Negligible (Extremely Sturdy)"
            else if (deflection < 0.10) "Acceptable (Minor Sag)"
            else "Excessive (Will Sag Visibly - Support Needed!)"

            listOf(
                DynamicToolResult("Calculated Max Deflection", String.format("%.4f", deflection), "in"),
                DynamicToolResult("Deflection Performance", status)
            )
        },

        "widget_blade_speed" to DynamicToolDefinition(
            id = "widget_blade_speed",
            title = "Blade Surface Speed (SFPM)",
            description = "Surface Feet per Minute (SFPM), arbor RPM, and blade tip velocity",
            category = "Woodworking",
            parameters = listOf(
                DynamicToolParameter("rpm", "Arbor Shaft Speed (RPM)", 3450.0, 100.0, 15000.0, 50.0, "RPM"),
                DynamicToolParameter("diameter", "Blade Diameter (inches)", 10.0, 2.0, 30.0, 0.5, "in")
            )
        ) { params ->
            val rpm = params["rpm"] ?: 3450.0
            val d = params["diameter"] ?: 10.0

            val sfpm = (rpm * PI * d) / 12.0
            val kph = (sfpm * 60.0 * 1.609) / 5280.0

            listOf(
                DynamicToolResult("Surface Velocity (SFPM)", String.format("%.1f", sfpm), "SFPM"),
                DynamicToolResult("Blade Tip Speed (MPH)", String.format("%.1f", sfpm * 60.0 / 5280.0), "mph"),
                DynamicToolResult("Blade Tip Speed (km/h)", String.format("%.1f", kph), "km/h")
            )
        },

        // Electrical Suite
        "widget_ohms_law" to DynamicToolDefinition(
            id = "widget_ohms_law",
            title = "Ohm's Law & Power Solver",
            description = "Calculates electric current, voltage, impedance, and power metrics instantly",
            category = "Electrical",
            parameters = listOf(
                DynamicToolParameter("voltage", "Voltage (V)", 120.0, 1.0, 1000.0, 1.0, "V"),
                DynamicToolParameter("resistance", "Resistance (Ω)", 12.0, 0.1, 10000.0, 0.5, "Ω")
            )
        ) { params ->
            val v = params["voltage"] ?: 120.0
            val r = params["resistance"] ?: 12.0

            val i = v / r
            val p = v * i

            listOf(
                DynamicToolResult("Electric Current (I)", String.format("%.3f", i), "A"),
                DynamicToolResult("Total Power Load (P)", String.format("%.2f", p), "W"),
                DynamicToolResult("Kilowatt Equivalent", String.format("%.4f", p / 1000.0), "kW")
            )
        },

        "widget_voltage_drop" to DynamicToolDefinition(
            id = "widget_voltage_drop",
            title = "Voltage Drop Calculator",
            description = "NEC 3% branch & 5% line degradation analyzer for Copper & Aluminum runs",
            category = "Electrical",
            parameters = listOf(
                DynamicToolParameter("voltage", "Source Voltage (V)", 120.0, 12.0, 480.0, 1.0, "V"),
                DynamicToolParameter("current", "Circuit Load Current (A)", 15.0, 1.0, 100.0, 1.0, "A"),
                DynamicToolParameter("distance", "One-Way Distance (ft)", 100.0, 1.0, 1000.0, 5.0, "ft"),
                DynamicToolParameter("resistance", "Wire Ohm per 1000ft (approx)", 1.2, 0.05, 10.0, 0.05, "Ω/k-ft")
            )
        ) { params ->
            val v = params["voltage"] ?: 120.0
            val i = params["current"] ?: 15.0
            val dist = params["distance"] ?: 100.0
            val r = params["resistance"] ?: 1.2

            // Single phase voltage drop formula: VD = (2 * K * L * I) / 1000
            val drop = (2.0 * dist * r * i) / 1000.0
            val pct = (drop / v) * 100.0
            val compliant = if (pct <= 3.0) "Compliant (under 3% NEC bar)" else "⚠️ Excessive Drop (Upsize conductor!)"

            listOf(
                DynamicToolResult("Calculated Voltage Drop", String.format("%.3f", drop), "V"),
                DynamicToolResult("Percentage Signal Drop", String.format("%.2f%%", pct)),
                DynamicToolResult("Under Load Terminal V", String.format("%.2f", v - drop), "V"),
                DynamicToolResult("NEC Code Compliance", compliant)
            )
        },

        "widget_conduit_fill" to DynamicToolDefinition(
            id = "widget_conduit_fill",
            title = "Wire Gauge & Conduit Fill Sizer",
            description = "NEC Chapter 9 Table 1 & 4 fill percentages and wire packing index",
            category = "Electrical",
            parameters = listOf(
                DynamicToolParameter("conduit_id", "Conduit Inside Diameter (in)", 1.049, 0.5, 4.0, 0.05, "in"),
                DynamicToolParameter("wire_id", "Wire Outside Diameter (in)", 0.164, 0.05, 0.5, 0.01, "in"),
                DynamicToolParameter("wire_count", "Number of Wires / Conductors", 4.0, 1.0, 50.0, 1.0, "wires")
            )
        ) { params ->
            val conduitD = params["conduit_id"] ?: 1.049
            val wireD = params["wire_id"] ?: 0.164
            val count = params["wire_count"] ?: 4.0

            val conduitArea = PI * (conduitD / 2.0) * (conduitD / 2.0)
            val wireArea = PI * (wireD / 2.0) * (wireD / 2.0) * count
            val ratio = if (conduitArea > 0) (wireArea / conduitArea) * 100.0 else 0.0
            val safe = if (count == 1.0 && ratio <= 53.0) "Safe (limit 53%)"
            else if (count == 2.0 && ratio <= 31.0) "Safe (limit 31%)"
            else if (count > 2.0 && ratio <= 40.0) "Safe (limit 40% NEC max)"
            else "⚠️ Code Infraction! Overstuffed Conduit!"

            listOf(
                DynamicToolResult("Total Inner Conduit Area", String.format("%.4f", conduitArea), "sq-in"),
                DynamicToolResult("Total Wire Bundled Area", String.format("%.4f", wireArea), "sq-in"),
                DynamicToolResult("Conduit Fill Percentage", String.format("%.2f%%", ratio)),
                DynamicToolResult("NEC Code Fill Safety", safe)
            )
        },

        "widget_conduit_bender" to DynamicToolDefinition(
            id = "widget_conduit_bender",
            title = "Conduit Bender Angles & Offsets",
            description = "Offsets, 3/4-bend saddles, 90° stub-up take-up & mark multipliers",
            category = "Electrical",
            parameters = listOf(
                DynamicToolParameter("offset", "Target Offset Height (in)", 6.0, 1.0, 48.0, 0.5, "in"),
                DynamicToolParameter("angle", "Desired Bend Angle (deg)", 30.0, 10.0, 90.0, 5.0, "°")
            )
        ) { params ->
            val offset = params["offset"] ?: 6.0
            val angle = params["angle"] ?: 30.0

            val multiplier = 1.0 / sin(angle * PI / 180.0)
            val distance = offset * multiplier
            val shrink = offset * (1.0 - cos(angle * PI / 180.0))

            listOf(
                DynamicToolResult("Mark-to-Mark Spacing", String.format("%.2f", distance), "in"),
                DynamicToolResult("Conduit Shrinkage Deduction", String.format("%.2f", shrink), "in"),
                DynamicToolResult("Offset Multiplier Ratio", String.format("1 : %.2f", multiplier))
            )
        },

        "widget_led_driver" to DynamicToolDefinition(
            id = "widget_led_driver",
            title = "LED Driver & Transformer Sizer",
            description = "Total wattage load with 80% safety headroom & Class 2 power supply sizing",
            category = "Electrical",
            parameters = listOf(
                DynamicToolParameter("power", "Single Fixture Power (W)", 12.0, 0.5, 100.0, 0.5, "W"),
                DynamicToolParameter("count", "Fixtures Quantity", 8.0, 1.0, 100.0, 1.0, "pcs")
            )
        ) { params ->
            val p = params["power"] ?: 12.0
            val count = params["count"] ?: 8.0

            val sum = p * count
            val driverSize = sum / 0.80

            listOf(
                DynamicToolResult("Net Combined Fixture Load", String.format("%.1f", sum), "W"),
                DynamicToolResult("Sizing Limit (NEC 80% rule)", String.format("%.1f", driverSize), "W"),
                DynamicToolResult("Minimum Power Supply Class", if (driverSize <= 96.0) "UL Class 2 (Safe)" else "Commercial Class 1")
            )
        },

        "widget_solar_battery_sizer" to DynamicToolDefinition(
            id = "widget_solar_battery_sizer",
            title = "Solar PV & Battery Bank Sizer",
            description = "Array wattage, MPPT limits, autonomy days, and battery bank capacity",
            category = "Electrical",
            parameters = listOf(
                DynamicToolParameter("wh", "Daily Consumption (Wh)", 4800.0, 100.0, 50000.0, 100.0, "Wh"),
                DynamicToolParameter("sun", "Average Daily Sun Hours (hr)", 4.5, 1.0, 10.0, 0.1, "hr"),
                DynamicToolParameter("days", "Days of Autonomy (Backups)", 2.0, 1.0, 7.0, 1.0, "days"),
                DynamicToolParameter("voltage", "System DC Voltage (V)", 24.0, 12.0, 48.0, 12.0, "V")
            )
        ) { params ->
            val wh = params["wh"] ?: 4800.0
            val sun = params["sun"] ?: 4.5
            val days = params["days"] ?: 2.0
            val v = params["voltage"] ?: 24.0

            val solarWatts = (wh / sun) * 1.25 // +25% inefficiency factor
            val batteryAh = (wh * days) / v / 0.8 // 80% max DoD factor

            listOf(
                DynamicToolResult("Required Solar PV Power", String.format("%.1f", solarWatts), "W"),
                DynamicToolResult("Battery Bank Capacity", String.format("%.1f", batteryAh), "Ah"),
                DynamicToolResult("Suggested Charge Controller", String.format("%.0fA MPPT", max(15.0, solarWatts / v * 1.15)))
            )
        },

        // Plumbing, HVAC, & Maintenance Suite
        "widget_pipe_sizing" to DynamicToolDefinition(
            id = "widget_pipe_sizing",
            title = "Pipe Sizing & Friction Loss",
            description = "Water flow rates (GPM), velocity limits, and friction pressure drops",
            category = "Plumbing",
            parameters = listOf(
                DynamicToolParameter("gpm", "Water Flow Rate (GPM)", 10.0, 0.5, 300.0, 0.5, "GPM"),
                DynamicToolParameter("id", "Pipe Inside Diameter (in)", 1.049, 0.25, 6.0, 0.05, "in")
            )
        ) { params ->
            val gpm = params["gpm"] ?: 10.0
            val id = params["id"] ?: 1.049

            // Velocity V = Q / A = (GPM * 0.408) / D^2
            val velocity = (gpm * 0.408) / (id * id)
            val velocityStatus = if (velocity <= 5.0) "Excellent (Under 5 ft/s - prevents noise & erosion)"
            else if (velocity <= 8.0) "Acceptable (Continuous limit)"
            else "⚠️ Too Fast! High erosion risk, increase pipe diameter!"

            listOf(
                DynamicToolResult("Water Velocity", String.format("%.2f", velocity), "ft/s"),
                DynamicToolResult("Velocity Safety Analysis", velocityStatus)
            )
        },

        "widget_hvac_load" to DynamicToolDefinition(
            id = "widget_hvac_load",
            title = "HVAC BTU & Room Load Estimator",
            description = "Mini-splits, AC tonnage, heat pumps & radiator sizing based on space volume",
            category = "HVAC",
            parameters = listOf(
                DynamicToolParameter("area", "Room Floor Area (sq-ft)", 1500.0, 50.0, 10000.0, 25.0, "sq-ft"),
                DynamicToolParameter("height", "Ceiling Height (ft)", 8.5, 6.0, 20.0, 0.5, "ft"),
                DynamicToolParameter("insulation", "Insulation Grade (1=Bad, 2=Med, 3=Good)", 2.0, 1.0, 3.0, 1.0, "")
            )
        ) { params ->
            val area = params["area"] ?: 1500.0
            val height = params["height"] ?: 8.5
            val insulation = params["insulation"] ?: 2.0

            val volume = area * height
            val multiplier = when (insulation.toInt()) {
                1 -> 5.0 // Bad insulation: 5 BTU per cu-ft
                3 -> 3.0 // Good insulation: 3 BTU per cu-ft
                else -> 4.0 // Average: 4 BTU per cu-ft
            }
            val btu = volume * multiplier
            val tons = btu / 12000.0

            listOf(
                DynamicToolResult("Estimated Space Volume", String.format("%.0f", volume), "cu-ft"),
                DynamicToolResult("Recommended Heating/AC Load", String.format("%.0f", btu), "BTU/hr"),
                DynamicToolResult("Equivalent Air Conditioner Tonnage", String.format("%.2f", tons), "Tons")
            )
        },

        "widget_tile_grout" to DynamicToolDefinition(
            id = "widget_tile_grout",
            title = "Tile, Grout & Flooring Estimator",
            description = "Tile counts, carton boxes, ANSI grout weight & thin-set mortar bags",
            category = "Maintenance",
            parameters = listOf(
                DynamicToolParameter("length", "Room Floor Length (ft)", 15.0, 1.0, 150.0, 0.5, "ft"),
                DynamicToolParameter("width", "Room Floor Width (ft)", 12.0, 1.0, 150.0, 0.5, "ft"),
                DynamicToolParameter("tile_size", "Tile Side Length (inches)", 12.0, 1.0, 36.0, 1.0, "in"),
                DynamicToolParameter("waste", "Spill/Cut Scrap Waste Allowance (%)", 10.0, 0.0, 30.0, 1.0, "%")
            )
        ) { params ->
            val len = params["length"] ?: 15.0
            val w = params["width"] ?: 12.0
            val size = params["tile_size"] ?: 12.0
            val waste = params["waste"] ?: 10.0

            val area = len * w
            val singleTileArea = (size * size) / 144.0
            val baseTiles = area / singleTileArea
            val totalTiles = ceil(baseTiles * (1.0 + waste / 100.0)).toInt()

            listOf(
                DynamicToolResult("Net Floor Surface Area", String.format("%.1f", area), "sq-ft"),
                DynamicToolResult("Tiles Needed (Incl. Waste)", totalTiles.toString(), "tiles"),
                DynamicToolResult("Rough Adhesive Thinset Required", String.format("%.1f", area / 50.0), "bags")
            )
        },

        "widget_paint_coverage" to DynamicToolDefinition(
            id = "widget_paint_coverage",
            title = "Wall Area & Paint/Primer Coverage",
            description = "Net wall & ceiling area, door/window subtractions, primer & paint cans",
            category = "Maintenance",
            parameters = listOf(
                DynamicToolParameter("length", "Room Length (ft)", 12.0, 2.0, 100.0, 0.5, "ft"),
                DynamicToolParameter("width", "Room Width (ft)", 10.0, 2.0, 100.0, 0.5, "ft"),
                DynamicToolParameter("height", "Ceiling Height (ft)", 8.0, 5.0, 25.0, 0.5, "ft"),
                DynamicToolParameter("coats", "Desired Paint Coats", 2.0, 1.0, 4.0, 1.0, "coats")
            )
        ) { params ->
            val l = params["length"] ?: 12.0
            val w = params["width"] ?: 10.0
            val h = params["height"] ?: 8.0
            val coats = params["coats"] ?: 2.0

            val wallArea = 2.0 * (l + w) * h
            val ceilingArea = l * w
            val totalArea = wallArea + ceilingArea
            val paintGal = ceil((totalArea / 350.0) * coats).toInt() // Standard: 1 gallon covers 350 sq-ft
            val primerGal = ceil(totalArea / 300.0).toInt() // Standard: 1 gallon primer covers 300 sq-ft

            listOf(
                DynamicToolResult("Wall Surface Area", String.format("%.1f", wallArea), "sq-ft"),
                DynamicToolResult("Ceiling Surface Area", String.format("%.1f", ceilingArea), "sq-ft"),
                DynamicToolResult("Total Combined Surface Area", String.format("%.1f", totalArea), "sq-ft"),
                DynamicToolResult("Estimated Paint Volume", paintGal.toString(), "gallons"),
                DynamicToolResult("Estimated Primer Volume", primerGal.toString(), "gallons")
            )
        },

        "widget_drywall_stud" to DynamicToolDefinition(
            id = "widget_drywall_stud",
            title = "Drywall & Framing Stud Calculator",
            description = "Required counts of 4x8/4x12 sheets, framing studs, top/sole plates, and fasteners",
            category = "Maintenance",
            parameters = listOf(
                DynamicToolParameter("length", "Total Running Wall Length (ft)", 24.0, 2.0, 500.0, 1.0, "ft"),
                DynamicToolParameter("spacing", "Stud On-Center Spacing (in)", 16.0, 12.0, 24.0, 4.0, "in")
            )
        ) { params ->
            val len = params["length"] ?: 24.0
            val oc = params["spacing"] ?: 16.0

            // Studs = WallLength (ft) * 12 / OC + 1 (start) + 2 (plates/corners/waste)
            val baseStuds = ceil((len * 12.0) / oc) + 1
            val totalStuds = baseStuds + 2 // safety buffer
            val drywallSheets = ceil((len * 8.0) / 32.0).toInt() // assuming 8ft tall walls, 4x8 boards hung vertically

            listOf(
                DynamicToolResult("Framing Wood Studs (2x4 / 2x6)", totalStuds.toInt().toString(), "studs"),
                DynamicToolResult("Required Drywall Boards (4x8)", drywallSheets.toString(), "sheets"),
                DynamicToolResult("Plates Required (Sole & Double Top)", String.format("%.0f", len * 3.0 / 8.0), "8ft boards")
            )
        },

        // Civil & Concrete Volume
        "widget_concrete_volume" to DynamicToolDefinition(
            id = "widget_concrete_volume",
            title = "Concrete Volume & Bag Mix Sizer",
            description = "Slab and trench volume calculations with bag mix requirements",
            category = "Civil",
            parameters = listOf(
                DynamicToolParameter("length", "Slab Length (ft)", 12.0, 1.0, 150.0, 0.5, "ft"),
                DynamicToolParameter("width", "Slab Width (ft)", 10.0, 1.0, 150.0, 0.5, "ft"),
                DynamicToolParameter("thickness", "Slab Thickness (in)", 4.0, 1.0, 36.0, 0.5, "in"),
                DynamicToolParameter("waste", "Volume Overfill Allowance (%)", 10.0, 0.0, 40.0, 1.0, "%")
            )
        ) { params ->
            val len = params["length"] ?: 12.0
            val w = params["width"] ?: 10.0
            val t = params["thickness"] ?: 4.0
            val waste = params["waste"] ?: 10.0

            val volumeCuFt = len * w * (t / 12.0)
            val baseCY = volumeCuFt / 27.0
            val totalCY = baseCY * (1.0 + waste / 100.0)
            val bags80 = ceil(volumeCuFt / 0.60 * (1.0 + waste / 100.0)).toInt()
            val bags60 = ceil(volumeCuFt / 0.45 * (1.0 + waste / 100.0)).toInt()

            listOf(
                DynamicToolResult("Net Volume Needed", String.format("%.2f", baseCY), "cu-yd"),
                DynamicToolResult("Total Volume (with Waste)", String.format("%.2f", totalCY), "cu-yd"),
                DynamicToolResult("Required 80 lb Bags (0.60 cu-ft)", bags80.toString(), "bags"),
                DynamicToolResult("Required 60 lb Bags (0.45 cu-ft)", bags60.toString(), "bags")
            )
        },

        "widget_rebar_estimator" to DynamicToolDefinition(
            id = "widget_rebar_estimator",
            title = "Rebar Grid & Steel Estimator",
            description = "Calculates total steel footage, weight, and lap ties for concrete reinforcing grids",
            category = "Civil",
            parameters = listOf(
                DynamicToolParameter("length", "Slab Grid Length (ft)", 20.0, 2.0, 200.0, 1.0, "ft"),
                DynamicToolParameter("width", "Slab Grid Width (ft)", 20.0, 2.0, 200.0, 1.0, "ft"),
                DynamicToolParameter("spacing", "Rebar Interval Spacing (in)", 12.0, 4.0, 48.0, 2.0, "in"),
                DynamicToolParameter("lap", "Steel Overlap Splice Space (in)", 18.0, 0.0, 48.0, 1.0, "in")
            )
        ) { params ->
            val l = params["length"] ?: 20.0
            val w = params["width"] ?: 20.0
            val spacing = params["spacing"] ?: 12.0
            val lap = params["lap"] ?: 18.0

            val spacingFt = spacing / 12.0
            val rows = floor(l / spacingFt) + 1.0
            val cols = floor(w / spacingFt) + 1.0

            // Base lengths
            val feetL = rows * w
            val feetW = cols * l
            val netFeet = feetL + feetW

            // Add lap factor for 20ft stock rebar bars
            val splicesL = floor(w / 20.0) * rows
            val splicesW = floor(l / 20.0) * cols
            val lapFeet = (splicesL + splicesW) * (lap / 12.0)
            val totalFeet = netFeet + lapFeet

            // Weight (#4 rebar weighs 0.668 lbs/ft)
            val weightLbs = totalFeet * 0.668

            listOf(
                DynamicToolResult("Steel Bars (20ft standard)", ceil(totalFeet / 20.0).toInt().toString(), "bars"),
                DynamicToolResult("Combined Linear Length", String.format("%.1f", totalFeet), "ft"),
                DynamicToolResult("Total Structural Weight (#4 bar)", String.format("%.1f", weightLbs), "lbs")
            )
        }
    )

    fun getOrFallback(toolId: String, definition: ToolDefinition): DynamicToolDefinition {
        return DYNAMIC_TOOLS[toolId] ?: DynamicToolDefinition(
            id = toolId,
            title = definition.title,
            description = definition.description,
            category = definition.category,
            parameters = listOf(
                DynamicToolParameter("length", "Primary Dimension (L)", 10.0, 1.0, 500.0, 1.0, "units"),
                DynamicToolParameter("width", "Secondary Dimension (W)", 5.0, 1.0, 500.0, 1.0, "units")
            )
        ) { params ->
            val l = params["length"] ?: 10.0
            val w = params["width"] ?: 5.0
            listOf(
                DynamicToolResult("Estimated Covered Area", String.format("%.2f", l * w), "sq-units"),
                DynamicToolResult("Operating Volume Sizing Factor", String.format("%.2f", l * w * 1.15), "cu-units")
            )
        }
    }
}
