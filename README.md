# 🛠️ Brillian Tools Suite

<p align="center">
  <img src="./logo.webp" alt="Brillian Tools Suite Logo" width="140" height="140" style="border-radius: 20px;">
</p>

> **The Ultimate AI Agent-Driven Offline Companion for Advanced Trades & Industrial Engineering.**

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![UI Framework](https://img.shields.io/badge/UI-Jetpack%20Compose%20%28Material%203%29-4285F4?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![AI Agent](https://img.shields.io/badge/AI_Agent-Driven%20Tools-FF6F00?style=flat-square)](https://ai.google.dev)

---

> ⚠️ **DEVELOPMENT NOTICE**
> **This tool is currently at the rapid development stage. It may be non-accurate and requires professional advice before critical on-site execution.**

---

## 🤖 AI Agent-Driven Standpoint: Fieldwork Assisted with AI

**Brillian Tools Suite** revolutionizes trade work on the field by putting **AI agent-driven tools front and center**. Field specialists can now instantly solve complex equations, generate lumber optimizations, and analyze coatings with conversational AI assistance, even in 100% offline environments.

Unlike conventional utility apps, Brillian Tools Suite is built for **off-grid operation** on remote jobsites with zero cell reception or internet dependency. It features a fully on-device, lightweight AI engine (**Brillian Copilot**) running locally optimized models like **SmolLM2 360M** and **Qwen2.5 1.5B** to compute complex trade calculations without latency or privacy concerns.

---

## ⭐ Standout Features

### 1. 🪵 AI Cutting List Optimizer
An intelligent 1D & 2D stock nesting engine powered by agent heuristics to minimize scrap waste and maximize yield across sheet goods and dimensional lumber. Instantly generates optimized cut plans with kerf adjustments and visual diagram breakdowns.

### 2. 🎨 AI Paint & Coating Analyzer
An advanced coating calculation agent that estimates exact volume requirements, dry film thickness (DFT), application methods, and substrate porosity adjustments (e.g. rough brick vs smooth drywall) for commercial and industrial finishes.

---

## 🎨 Visual Identity & Architecture

```
                  ┌─────────────────────────────────────┐
                  │      Brillian Tools Suite (AI)      │
                  │        (Jetpack Compose UI)         │
                  └──────────────────┬──────────────────┘
                                     │
         ┌───────────────────────────┼───────────────────────────┐
         ▼                           ▼                           ▼
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┤
│  AI Agent RAG   │         │  Hardware APIs  │         │ Persistent Logs │
│   LLM Engine    │         │ (Compass/Level) │         │ (Room Database) │
│ (SmolLM2/Qwen)  │         │  Sensor Telemetry│        │ Offline Queue   │
└─────────────────┘         └─────────────────┘         └─────────────────┘
```

The application is built on a reactive, MVVM-based native architecture:
1. **Presentation Layer**: Declarative UI built with Jetpack Compose (Material 3), featuring a gorgeous high-contrast theme, generous negative space, dynamic visual ripples, and fluid layout transitions.
2. **Local AI Inference (RAG Engine)**: A snappy, offline-first rule engine that parses user intent and serves exact technical calculations from a local trade database, embedding interactive Deep Link action cards inside the chat bubble to open corresponding calculator screens instantly.
3. **Hardware Telemetry Interface**: Real-time integration with direct physical sensors (barometer, light meter, compass, accelerometer/gyroscope) to provide microsecond accurate readings.
4. **Data Persistence**: Offline state, work logs, safety checklists, and active calculations are securely persisted in a relational Room SQLite Database.

---

## 📋 Complete Feature Catalog (111+ Professional Tools)

Below is the exhaustive catalog of all 111 features built into the Brillian Tools Suite:

### 📌 Woodworking (15 Tools)
1. **Board Footage & Lumber Estimator**
   - **Core Description**: Calculate board feet volume ((T x W x L)/12), species density & lumber pricing
   - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
2. **Cut List Optimizer (1D & 2D)**
   - **Core Description**: Bin packing cut optimizer for stock boards and 4x8 plywood nesting
   - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
3. **Stair Layout & Stringer Calculator**
   - **Core Description**: Stringer rise, run, step count, throat thickness & IRC headroom compliance
   - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
4. **Ragger & Roof Pitch Calculator**
   - **Core Description**: Common, hip, valley, and jack rafter lengths with birdsmouth seat cuts
   - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
5. **Compound Miter & Bevel Calculator**
   - **Core Description**: Miter and bevel saw angles for crown moldings & multi-sided polyhedral frames
   - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
6. **Wood Moisture & Shrinkage Estimator**
   - **Core Description**: Tangential & radial wood shrinkage forecasting based on species & target EMC
   - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
7. **Joinery & Tenon Spacing Calculator**
   - **Core Description**: Equal spacing distributions for mortise/tenons, dowels & pocket hole screws
   - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
8. **Kerf Bending**
   - **Core Description**: Kerf cut spacing, pitch, and depth for bending solid timber
   - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
9. **Dado & Lap Joint Planner**
   - **Core Description**: Single blade hogging fence offsets for wide lap joints
   - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
10. **Segmented Woodturning & Bowls**
    - **Core Description**: Stave miter angles, segment edge lengths, and ring stack dimensions for multi-sided lathe turnings
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
11. **Timber Sagulator & Shelf Load**
    - **Core Description**: Maximum deflection forecasting for wooden shelving under uniform and concentrated loads
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
12. **Board Foot Pricing & Cost Calculator**
    - **Core Description**: Total project cost estimation based on species board-foot rates and waste factors
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
13. **Wood Glue Clamp Pressure Estimator**
    - **Core Description**: Clamping force requirements (PSI) for PVA, epoxy, and polyurethane glue lines
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
14. **Router Feed Rate & RPM Calculator**
    - **Core Description**: Cutting speed (SFPM) and chip load per tooth recommendations for router bits
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
15. **Veneer & Marquetry Estimator**
    - **Core Description**: Book-matching and slip-matching surface area yield calculations
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.

### 📌 Electrical (12 Tools)
16. **Ohm's Law & Power Calculator**
    - **Core Description**: Solve voltage, current, resistance, and wattage relationships (V = I x R, P = V x I)
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
17. **Conduit Fill Percentage Calculator**
    - **Core Description**: NEC chapter 9 raceway fill limits for 40%, 60%, and single wire installations
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
18. **Voltage Drop & Wire Sizing**
    - **Core Description**: Circular mil copper/aluminum voltage drop calculations complying with NEC 210.19(A)
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
19. **Industrial Motor Full Load Amps (FLA)**
    - **Core Description**: NEC table 430.250 motor current ratings for 3-phase and single-phase AC induction motors
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
20. **Transformer Sizing & KVA Estimator**
    - **Core Description**: Load calculations and overcurrent protection sizing for dry-type distribution transformers
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
21. **Resistor Color Code Decoder (4, 5, 6 Band)**
    - **Core Description**: Resistance value, tolerance, and temperature coefficient lookup for carbon/metal film resistors
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
22. **LED Driver & Series-Parallel Resistor Calculator**
    - **Core Description**: Forward voltage drop matching and constant current driver sizing for LED arrays
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
23. **Solar Panel Array & Battery Sizing**
    - **Core Description**: Amp-hour bank sizing, peak sun hours, and inverter load calculations for off-grid PV
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
24. **Electrical Box Fill Capacity (NEC 314.16)**
    - **Core Description**: Cubic inch volume allowances for device boxes, ground wires, clamps, and switches
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
25. **Power Factor Correction (KVAR)**
    - **Core Description**: Capacitor bank sizing to improve lagging power factor to target 0.95+
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
26. **Conduit Bender Multiplier & Offset Calculator**
    - **Core Description**: Shrink, setback, and center-to-center measurements for 30°, 45°, and 60° conduit offsets
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
27. **4-20mA Industrial Current Loop Scaler**
    - **Core Description**: Sensor process variable conversion for temperature, pressure, and flow transmitters
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.

### 📌 Plumbing & Maintenance (4 Tools)
28. **Pipe Flow Rate & Friction Loss**
    - **Core Description**: Hazen-Williams pressure drop and velocity equations for PVC, copper, and steel pipes
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
29. **Hydrostatic Test Pressure Calculator**
    - **Core Description**: Pneumatic and water test pressure and duration schedules for pressure piping
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
30. **Drain Slope & Fall Calculator**
    - **Core Description**: Minimum gravity drainage fall (1/4 inch or 1/8 inch per foot) for sanitary sewer lines
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
31. **Expansion Loop & Thermal Growth**
    - **Core Description**: Linear pipe thermal expansion sizing for copper, PEX, and carbon steel runs
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.

### 📌 Safety & Compliance (2 Tools)
32. **OSHA Ladder Angle & Fall Protection**
    - **Core Description**: 4-to-1 rule angle verification and harness tie-off clearance calculations
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
33. **Chemical Hazmat MSDS Quick Lookup**
    - **Core Description**: NFPA 704 diamond ratings, PPE requirements, and spill response guidelines
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.

### 📌 Civil Engineering (13 Tools)
34. **Concrete Volume & Ready-Mix Estimator**
    - **Core Description**: Cubic yards calculation for slabs, footings, columns, and curbs with 5% waste factor
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
35. **Aggregate Sieve Analysis & Gradation**
    - **Core Description**: Combined aggregate gradation curves, fineness modulus, and ASTM compliance grading
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
36. **Earthwork Cut & Fill Balancer**
    - **Core Description**: Soil swell and shrinkage bulk density corrections for excavation grading
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
37. **Rebar Weight & Spacing Calculator**
    - **Core Description**: ASTM rebar size bar weights (lbs/ft), lap splice lengths, and grid spacing
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
38. **Soil Bearing Capacity & Footing Sizing**
    - **Core Description**: Allowable soil pressure verification for shallow spread and strip footings
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
39. **Retaining Wall Overturning & Sliding**
    - **Core Description**: Active earth pressure (Rankine/Coulomb), overturning moment safety factor analysis
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
40. **Open Channel Flow (Manning Equation)**
    - **Core Description**: Culvert and ditch flow velocity, hydraulic radius, and discharge capacity
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
41. **Asphalt Paving Tonnage Calculator**
    - **Core Description**: Hot mix asphalt (HMA) tonnage and compaction yield calculations
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
42. **Survey Traverse & Coordinate Geometry**
    - **Core Description**: Northing, Easting, bearing, distance traverse closure and error correction
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
43. **Pavement Thickness & ESAL Design**
    - **Core Description**: Equivalent single axle load (ESAL) flexible pavement structural number sizing
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
44. **Stormwater Runoff (Rational Method)**
    - **Core Description**: Peak discharge Q = ciA stormwater basin sizing calculations
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
45. **Trench Excavation Volume & Shoring**
    - **Core Description**: OSHA sloping, benching, and trench box volume quantities
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
46. **Highway Curve Geometry (Circular & Spiral)**
    - **Core Description**: Degree of curve, tangent length, external distance, and superelevation transition
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.

### 📌 Sensors (21 Tools)
47. **Digital Inclinometer & Surface Level**
    - **Core Description**: Pitch and roll sensor telemetry in degrees and percent slope
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
48. **Magnetic Compass & Azimuth Bearing**
    - **Core Description**: Heading direction, true north calibration, and site orientation mapping
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
49. **Barometric Altimeter & Pressure Tracker**
    - **Core Description**: Elevation above sea level derived from internal barometer sensor calibration
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
50. **Lux Light Meter (Illuminance)**
    - **Core Description**: Foot-candle and lux workplace illumination level measurements
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
51. **Audio Decibel (dB) Sound Level Meter**
    - **Core Description**: Ambient noise monitoring and OSHA time-weighted exposure logging
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
52. **Strobe Tachometer (RPM Frequency)**
    - **Core Description**: Optical flashing rate synchronization to measure rotational motor and fan speeds
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
53. **Bluetooth BLE Multimeter Data Logger**
    - **Core Description**: Real-time voltage, current, and continuity sensor stream recording
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
54. **Thermal Heat Index & Dew Point Calculator**
    - **Core Description**: Psychrometric humidity and apparent temperature safety monitoring
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
55. **Vibration FFT Frequency Analyzer**
    - **Core Description**: Accelerometer-based machine bearing vibration and harmonic resonance monitoring
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
56. **Magnetic Field (EMF Gauss) Meter**
    - **Core Description**: Electromagnetic radiation and high-voltage transformer stray field detection
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
57. **Laser Distance Estimator & Pythagorean Tool**
    - **Core Description**: Indirect height and span calculations using angle sensors and trigonometry
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
58. **Speedometer & GPS Velocity Tracker**
    - **Core Description**: Real-time ground speed, heading, and trip distance telemetry
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
59. **Barometric Weather Trend Forecaster**
    - **Core Description**: Atmospheric pressure drop detection for approaching storm alerts
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
60. **Surface Roughness & Profilometer Simulator**
    - **Core Description**: Micro-surface texture and profile depth evaluation
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
61. **Air Flow Velocity & CFM Anemometer**
    - **Core Description**: Duct air velocity and volumetric flow rate calculations
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
62. **Soil Moisture & Compaction Sensor Interface**
    - **Core Description**: Geotechnical soil density and moisture content logging
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
63. **Ultrasonic Wall Thickness & Depth Log**
    - **Core Description**: Material wall thinning and corrosion monitoring logs
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
64. **Strain Gauge & Load Cell Monitor**
    - **Core Description**: Structural load and tension telemetry recording
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
65. **Optical Plumb Line & Alignment Check**
    - **Core Description**: Vertical tower and wall plumbness verification using camera accelerometer
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
66. **GPS Elevation Profile & Waypoint Logger**
    - **Core Description**: Topographic elevation transects and jobsite boundary recording
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
67. **Battery State of Charge (SoC) Diagnostics**
    - **Core Description**: Lead-acid and lithium battery voltage curve health analysis
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.

### 📌 Metalworks (25 Tools)
68. **Sheet Metal Bend Allowance & Deduction**
    - **Core Description**: K-factor, bend radius, and setback calculations for accurate sheet metal braking
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
69. **Welding Heat Input & Interpass Temperature**
    - **Core Description**: Arc voltage, amperage, and travel speed energy input calculations (kJ/in)
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
70. **CNC Cutting Feed & Speeds (SFM / IPT)**
    - **Core Description**: Spindle RPM and feed rate calculations for end mills, face mills, and drills
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
71. **Structural Steel Beam Weight & Profile Sizing**
    - **Core Description**: W-shape, S-shape, channel, and angle weight per foot and section properties
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
72. **Bolt Torque & Clamp Load Calculator**
    - **Core Description**: SAE/Metric grade fastener torque specifications and preload tension
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
73. **Metal Weight & Bar Stock Estimator**
    - **Core Description**: Weight calculations for steel, aluminum, brass, and copper plates, tubes, and bars
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
74. **Plasma / Laser Cutting Time & Cost Estimator**
    - **Core Description**: Pierce count, cutting linear inches, and machine hourly operating cost breakdown
    - **Advanced Grade**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
75. **Brinell & Rockwell Hardness Conversion**
    - **Core Description**: Hardness scale cross-referencing for tempered alloy steels
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
76. **Pipe Notcher & Fishmouth Layout**
    - **Core Description**: Tubing intersection saddle templates and angle layouts
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
77. **MIG / TIG Shielding Gas Flow & Coverage**
    - **Core Description**: CFH flow rate recommendations for argon, helium, and CO2 shielding mixes
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
78. **Thread Tap Drill Size Calculator**
    - **Core Description**: Unified National (UNC/UNF) and Metric tap drill chart lookup for 75% thread depth
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
79. **Press Brake Tonnage & Air Bending Force**
    - **Core Description**: Required tonnage per foot for mild steel and stainless steel plate bending
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
80. **Structural Weld Fillet Size & Strength**
    - **Core Description**: Effective throat thickness and shear strength capacity for fillet welds
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
81. **Metal Thermal Expansion & Shrinkage**
    - **Core Description**: Post-welding distortion and thermal contraction allowances
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
82. **Structural Bolted Connection Shear/Tension**
    - **Core Description**: Slip-critical and bearing-type connection capacity checks (A325/A490)
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
83. **Forging & Casting Stock Allowance**
    - **Core Description**: Machining finish stock additions for rough castings and forgings
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
84. **Anodizing & Electroplating Surface Area**
    - **Core Description**: Current density and tank time calculations for surface finishing
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
85. **Structural Column Buckling (Euler/AISC)**
    - **Core Description**: Slenderness ratio (KL/r) and critical buckling load estimation
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
86. **Laser Engraving Power & Speed Matrix**
    - **Core Description**: Optimal frequency and power settings for fiber and CO2 laser marking
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
87. **Sheet Metal Blank Diameter Calculator**
    - **Core Description**: Blank sizing for spun metal hemispherical and cylindrical vessels
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
88. **Alloy Identification & Spark Testing Guide**
    - **Core Description**: Spark stream color, length, and burst patterns for steel classification
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
89. **Craning & Rigging Sling Tension Calculator**
    - **Core Description**: Sling angle load multipliers and choker hitch tension distribution
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
90. **Structural Plate Girder Section Modulus**
    - **Core Description**: Moment of inertia and bending stress analysis for fabricated I-beams
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
91. **Punching & Shearing Force Estimator**
    - **Core Description**: Ultimate shear strength calculations for punching holes in steel plate
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
92. **Metal Surface Prep & Sandblasting Media**
    - **Core Description**: Abrasive consumption rates and blast nozzle pressure sizing
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.

### 📌 Mechanical & HVAC (5 Tools)
93. **HVAC Duct Sizing & Friction Rate**
    - **Core Description**: Equal friction method calculations for rectangular and round ductwork
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
94. **Belt Drive & Pulley Ratio Calculator**
    - **Core Description**: RPM, pitch diameter, and center distance calculations for V-belt drives
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
95. **Beam Deflection & Bending Stress (Simple/Cantilever)**
    - **Core Description**: Maximum deflection ($\Delta$) and extreme fiber stress for structural beams
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
96. **Gear Ratio & Torque Multiplier**
    - **Core Description**: Spur gear tooth counts, pitch diameters, and mechanical advantage ratios
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
97. **Hydraulic Cylinder Force & Speed**
    - **Core Description**: Push/pull force output, fluid flow rate, and actuator travel velocity
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.

### 📌 Painting & Coating (1 Tool)
98. **Paint & Coating Coverage Estimator**
    - **Core Description**: Gallons required, dry film thickness (DFT), and substrate porosity adjustments
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.

### 📌 Utility & Misc (13 Tools)
99. **Jobsite IR Remote Controller**
    - **Core Description**: Universal infrared commands for industrial HVAC and equipment testing
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
100. **Material Inventory Stock Tracker**
    - **Core Description**: On-site raw material tracking with low stock alerts and Room DB persistence
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
101. **Work Tracking & Active Task Timer**
    - **Core Description**: Billable hours tracking, stopwatch timers, and productivity logs
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
102. **Focus Work Timer (Pomodoro / Interval)**
    - **Core Description**: Deep work focus sessions with ambient soundscapes and break reminders
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
103. **Offline Sync Queue Monitor**
    - **Core Description**: Background sync status inspector for pending cloud transactions
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
104. **Dashboard Customizer & Widget Manager**
    - **Core Description**: Modular drag-and-drop dashboard layout customization
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
105. **Outdoor Activities & Weather Compass**
    - **Core Description**: Field weather tracking and outdoor sun path orientation
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
106. **Tool Catalog Explorer**
    - **Core Description**: Instant search and deep-link routing for all 111+ trade calculators
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
107. **Settings & Local AI Model Manager**
    - **Core Description**: Configure app preferences, theme modes, and local LLM weights (SmolLM2/Qwen)
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
108. **About & Developer Profile**
    - **Core Description**: System architecture specs and developer acknowledgments
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
109. **Quick Notes & Jobsite Attachments**
    - **Core Description**: Rich text field notes with camera photo attachments and audio memos
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
110. **Safety Checklist & OSHA Audit**
    - **Core Description**: Pre-start job safety analysis and hazard mitigation sign-offs
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.
111. **Satellite / GPS Coordinates Inspector**
    - **Core Description**: Latitude, longitude, altitude, and GPS satellite fix strength telemetry
    - **Advanced Feature**: Fully integrated with the Brillian offline calculation engine, Room SQLite logging, and RAG deep-linking support for instant field referencing.

---

## 📦 Libraries & Technology Stack

The Brillian Tools Suite relies exclusively on robust, developer-trusted libraries:

*   **Kotlin Coroutines & Flow**: Powering synchronous local calculations and streaming reactive sensor states.
*   **Jetpack Compose**: Declarative, Material 3-compliant layouts utilizing unified typography (Plus Jakarta Sans & Playfair Display), custom shapes, and highly accessible touch targets (min 48dp).
*   **Jetpack Navigation Compose**: Handles type-safe, fluid screen transitions and dynamic floating assistant integration.
*   **Room Database (SQLite + KSP)**: Enterprise-grade offline relational persistence for tasks, notes, inventory logs, and history files.
*   **Android WorkManager**: Orchestrates background synchronization queues safely in compliance with OS battery-saver states.
*   **AndroidX Sensor Framework**: Binds physical device hardware sensors for altimeter, level, light, and compass readings.

---

## 🛠️ Installation & Getting Started

### Prerequisites
*   Android Studio Ladybug (or newer)
*   Android SDK 34+
*   Kotlin 1.9.0+

### Setup Instructions
1.  **Clone the Repository**:
    ```bash
    git clone https://github.com/brillian-dsgn/brillian-tools-suite.git
    cd brillian-tools-suite
    ```
2.  **Open in Android Studio**:
    *   Select **File > Open** and choose the `brillian-tools-suite` folder.
    *   Allow Gradle to sync and download necessary dependencies.
3.  **Download Local AI Weights**:
    *   Run the app on a device/emulator.
    *   Navigate to **Settings > Local AI Model Manager**.
    *   Select and activate an offline weight file (e.g. SmolLM2 360M or Qwen2.5 1.5B).
4.  **Run the App**:
    *   Click **Run 'app'** (`Shift + F10`) to compile and install the APK on your device.

---

## 👥 Authors & Core Team

*   **brillian.dsgn** — *Lead Systems Architect & UX/UI Designer (Systems, UX, and industrial controls developer)*
*   **AI Coding Assistant (Google DeepMind)** — *Co-Developer & local RAG Integration Engineer*

---

*Engineered with precision. Secure. Offline. Built for the Trades.*
