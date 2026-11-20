# Release v1.3.0 - Kerf Curve Lengths, Enhanced Digital Level & Auto-Build CI

### 🚀 Release Highlights
- **Kerf Bending Wood Curve Lengths**: Added Inside Curve and Outside Curve total wood length readouts in both Single Curve Bend and Multi-Section Kerf modes, plus per-section curve breakdowns.
- **Stabilized Digital Level Guide & Auto-Rotation**: Replaced multi-axis tube rotation in `StraightLineLevelGauge` with a fixed horizontal camera-style level guide and enabled automatic sensor rotation (`SCREEN_ORIENTATION_FULL_SENSOR`) in `DigitalLevelScreen`.
- **Streamlined Offline Architecture**: Removed the redundant Offline Sync Center module (`SyncQueueScreen`, sync entities/DAOs, and top bar badge) in favor of clean local persistence.
- **Automated GitHub Actions CI/CD**: Configured automatic semantic versioning (`version.properties`), automatic `CHANGELOG.md` / `RELEASE.md` updates, APK builds, and GitHub Releases on code pushes.

---
