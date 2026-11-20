# Changelog

All notable changes to **Brillian Tools** will be documented in this file.

## [v1.3.0] - 2026-11-20

### 🚀 Features & Enhancements
- **Kerf Bending Curve Lengths**: Added Total Wood Length for both Inside Curve (`innerArcLengthMm`) and Outside Curve (`outerArcLengthMm`) in Single Curve Bend and Multi-Section Kerf modes, along with per-section inside/outside curve dimensions.
- **Auto-Rotating Digital Level & Inclinometer**: Enabled dynamic sensor-based full screen orientation (`SCREEN_ORIENTATION_FULL_SENSOR`) in `DigitalLevelScreen` so the level automatically adapts to portrait and landscape rotation regardless of global settings.
- **Stabilized Camera-Style Level Guide**: Redesigned `StraightLineLevelGauge` so the spirit tube and center reference line stay completely horizontal and stable while the bubble slides smoothly left and right along the roll axis.
- **Automated CI/CD APK Release Pipeline**: Added GitHub Actions workflow to automatically bump semantic versions, generate release changelogs, build APKs, and publish GitHub Releases on code updates.

### 🧹 Refactoring & Maintenance
- **Removed Offline Sync Center**: Streamlined local Room persistence by removing the obsolete Offline Sync Center UI, sync queue entities/DAOs/repositories, and top app bar sync status badge.
- **Dynamic In-App Versioning**: Synchronized `AboutScreen` and Gradle builds via `version.properties` and `BuildConfig`.

---

## [v1.2.0]

### 🚀 Release Highlights
- **AI Agent-Driven Fieldwork Companion**: Positioned local AI copilot tools front-and-center for instant offline trade calculations and conversational assistance on the jobsite.
- **Standout AI Features**: Intelligent 1D & 2D Cutting List Optimizer and Paint & Coating Analyzer.
- **Complete 111+ Feature Catalog**: Documented all 111 professional trade tools across Woodworking, Electrical, Plumbing, Safety, Civil Engineering, Sensor telemetry, Metalworks, Mechanical/HVAC, Painting, and Utility modules.
- **App Logo & Visual Polish**: Integrated custom adaptive app logo (`logo.webp`) and upgraded the Library module.
