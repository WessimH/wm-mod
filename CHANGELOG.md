# Changelog

All notable changes to the Wm mod will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-05-18

### Added
- **Grave System**
  - Player head with skin placed at death position
  - Right-click grave head to open chest inventory with all stored items
  - Grave inventory contains main slots (36), armor (4) and offhand (1)
  - Auto-removes grave and head block when all items are collected
  - Breaking the grave head drops all items at the position
  - Death message sent to player with grave coordinates

- **BlueMap Grave Markers**
  - 💀 skull marker added on BlueMap at every grave location
  - Marker removed automatically when grave is collected or broken
  - Only active when BlueMap mod is loaded

### Changed
- Removed automatic TPS chat alerts (too spammy)
- Waystone markers now use `HtmlMarker` instead of `POIMarker` for cleaner look
- Waystone markers filtered to only show player-activated waystones (`wasSeen()`)
- TPS widget displayed on BlueMap web interface (bottom-right corner)

## [1.0.0] - 2026-05-16

### Added
- **Server Performance Monitoring**
  - Real-time TPS (Ticks Per Second) tracking
  - MSPT (Milliseconds Per Tick) measurements
  - Automatic performance alerts for operators
  - Alert thresholds: TPS ≤ 15.0 (warning), TPS ≤ 10.0 (critical)
  - Color-coded performance indicators

- **Player Ping Display**
  - Live ping display in player tab list
  - Color-coded latency indicators (green/yellow/red)
  - Updates every second with player latency data
  - Display format: `PlayerName §a[25ms]`

- **BlueMap Integration**
  - Automatic waystone synchronization to BlueMap
  - Real-time waystone marker creation and updates
  - Automatic marker cleanup for deleted waystones
  - Per-dimension waystone tracking
  - Markers update every minute (1200 ticks)

- **Admin Commands**
  - `/tps` command for server performance metrics
  - Shows current TPS, MSPT, and player ping
  - Color-coded output for easy reading

- **Core Features**
  - Fabric mod support for Minecraft 1.26.1.2
  - MixIn system for tab name customization
  - Event-based performance tracking
  - Conditional feature loading based on mod availability

### Documentation
- Comprehensive README with feature descriptions
- Technical documentation (DOCUMENTATION.md)
- JavaDoc comments on all public methods
- Inline code documentation for complex logic
- Test coverage documentation

### Testing
- Unit test suite for waystone sync functionality
- Mock-based testing with Mockito
- Test coverage for:
  - Marker creation validation
  - Marker property correctness
  - Waystone deletion handling
  - Error handling for missing worlds

### Development
- Gradle build configuration
- JUnit 5 test framework setup
- Maven publication configuration
- Development environment support (IntelliJ IDEA, Eclipse)

## [Unreleased]

### Planned Features
- Configuration file support
- Customizable alert thresholds
- Per-player ping statistics
- Performance history tracking
- Web dashboard for performance metrics
- Custom waystone icons/colors in BlueMap
- Performance prediction system
- Alert cooldown configuration
- Multi-language support

### Known Issues
- None reported

### Potential Improvements
- Optimize marker sync for large waystone counts
- Add caching mechanism for performance data
- Implement regional marker loading in BlueMap
- Add configuration UI in-game
- Support for custom dimension names

---

## Notes

### Version Compatibility

| Component | Version | Status |
|-----------|---------|--------|
| Minecraft | 1.26.1.2 | ✓ Supported |
| Fabric | Latest | ✓ Required |
| Waystones | 26.1.2.2 | ✓ Optional |
| BlueMap | 2.7.7+ | ✓ Optional |
| Java | 25+ | ✓ Required |

### Breaking Changes

Currently no breaking changes have been introduced.

### Migration Guide

Not applicable for version 1.0.0 (initial release).

---

**Current Version**: 1.1.0
**Latest Release Date**: 2026-05-18
**Maintenance Status**: Active Development

