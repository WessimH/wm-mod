# Wm Mod - Technical Documentation

Complete technical documentation for developers working with the Wm (Waystone Manager) mod.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Core Components](#core-components)
3. [Performance Monitoring System](#performance-monitoring-system)
4. [Waystone Synchronization](#waystone-synchronization)
5. [Mixin System](#mixin-system)
6. [Testing Guide](#testing-guide)
7. [Configuration](#configuration)
8. [Troubleshooting](#troubleshooting)

## Architecture Overview

The Wm mod follows a modular architecture with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│         Minecraft Server/Client         │
└──────────────┬──────────────────────────┘
               │
      ┌────────▼────────┐
      │    Wm.java      │ Entry Point
      └────────┬────────┘
               │
        ┌──────┴──────┐
        │             │
    ┌───▼───┐    ┌────▼────┐
    │Server │    │ Client   │
    │Mods   │    │  Mods    │
    └───┬───┘    └────┬─────┘
        │             │
    ┌───┴──────────────┴─────┐
    │  TPS Tracking          │
    │  Ping Display          │
    │  BlueMap Sync          │
    └──────────────────────┘
```

## Core Components

### 1. **Wm.java** - Main Entry Point

**Location**: `src/main/java/wm/modid/Wm.java`

**Purpose**: Initializes all mod features on server startup

**Key Responsibilities**:
- Register performance trackers
- Setup command dispatchers
- Enable conditional features (BlueMap/Waystones)
- Provide central logging

**Example Usage**:
```java
Wm.LOGGER.info("Custom log message");
```

### 2. **TpsCommand.java** - Command Handler

**Location**: `src/main/java/wm/modid/commands/TpsCommand.java`

**Purpose**: Implements the `/tps` command for operators

**Features**:
- Displays current TPS and MSPT
- Show player ping
- Color-coded output

**Command Usage**:
```
/tps
```

**Output Example**:
```
§6TPS: §a20.0 §7| §6MSPT: §f50.0m §7| §6Ping: §f25ms
```

### 3. **TpsTracker.java** - Performance Monitor

**Location**: `src/main/java/wm/modid/commands/TpsTracker.java`

**Purpose**: Continuously monitors server performance

**Key Metrics**:
- **TPS** (Ticks Per Second): Server tick rate (max 20.0)
- **MSPT** (Milliseconds Per Tick): Time per server tick

**Alert System**:
```java
// Alert thresholds
TPS_WARN = 15.0      // Warning when TPS drops to or below this
TPS_CRITICAL = 10.0  // Critical when TPS drops to or below this
MSPT_WARN = 60.0     // Warning when MSPT reaches or exceeds this
ALERT_COOLDOWN = 100 // Ticks (5 seconds between alerts)
```

**Performance Calculation**:
```
TPS = 1000.0 / MSPT (capped at 20.0)
MSPT = (nanoTime_now - nanoTime_last) / 1,000,000
```

### 4. **TabPingDisplay.java** - Ping Display

**Location**: `src/main/java/wm/modid/commands/TabPingDisplay.java`

**Purpose**: Shows player latency in the tab list

**Features**:
- Updates every 20 ticks (1 second)
- Color-coded ping values:
  - Green (§a): < 80ms
  - Yellow (§e): 80-149ms
  - Red (§c): ≥ 150ms
- Uses player display names

**Display Format**:
```
PlayerName §a[25ms]
```

### 5. **ServerPlayerMixin.java** - Tab Name Injection

**Location**: `src/main/java/wm/modid/mixin/ServerPlayerMixin.java`

**Purpose**: Intercepts tab list display names

**Injection Point**: `ServerPlayer.getTabListDisplayName()`

**Behavior**:
1. Called when Minecraft requests player display name
2. Checks if custom name exists in TabPingDisplay cache
3. Returns custom name (with ping) or allows default

## Performance Monitoring System

### Data Flow

```
Server Tick End
     ↓
TpsTracker.register() listener fires
     ↓
Measure time delta (nanoTime)
     ↓
Calculate MSPT and TPS
     ↓
Check against thresholds
     ↓
Broadcast alert if needed
     ↓
Cooldown management
```

### Alert Levels

| Level | Condition | Color | Message |
|-------|-----------|-------|---------|
| NONE | Normal | 🟢 Green | "TPS back to normal" |
| WARN | TPS ≤ 15.0 or MSPT ≥ 60.0 | 🟡 Yellow | "Low TPS: X.X TPS" |
| CRITICAL | TPS ≤ 10.0 | 🔴 Red | "CRITICAL TPS: X.X TPS" |

### Alert Cooldown

- **Purpose**: Prevents message spam
- **Duration**: 100 ticks (5 seconds)
- **Behavior**: Only operators receive alerts
- **Reset**: When performance returns to normal

### Accessing Performance Data

```java
// Get current TPS
double currentTps = TpsTracker.getTps();

// Get current MSPT
double currentMspt = TpsTracker.getMspt();

// Max TPS value
double maxTps = 20.0;
```

## Waystone Synchronization

### Overview

Synchronizes waystones from the Waystones mod to BlueMap markers automatically.

### Architecture

```
Waystones Mod API
     ↓
WaystoneBlueMapSync.java
     ↓
BlueMap API
     ↓
Map Markers
```

### Key Classes

**WaystoneBlueMapSync.java**:
- Main synchronization logic
- Handles marker creation/deletion
- Manages update intervals

**WaystoneBlueMapSyncTestHelper.java**:
- Test utilities for marker operations
- Reproduces sync logic without server

### Synchronization Process

```
1. Register on server start
   ├─ Wait for BlueMap to initialize
   ├─ Trigger immediate sync
   └─ Setup periodic sync (1200 ticks)

2. On each sync cycle
   ├─ Collect all active waystones
   ├─ Create/update markers for each waystone
   ├─ Remove markers for deleted waystones
   └─ Broadcast to all connected clients
```

### Marker Structure

**POI Marker Properties**:
```java
{
  "label": "Waystone Name",
  "detail": "<b>Waystone Name</b><br>X, Y, Z",
  "position": {x: 100.5, y: 64.0, z: 200.5},
  "maxDistance": 100000  // Visibility range
}
```

### Update Frequency

- **Initial Sync**: On BlueMap ready
- **Periodic Sync**: Every 1200 ticks (60 seconds)
- **Per Server**: Configurable via REFRESH_INTERVAL

### Dimension Handling

- Tracks waystone dimensions automatically
- Creates markers in correct dimension
- Updates per-dimension marker sets

## Mixin System

### Mixin Basics

Mixins allow code injection into Minecraft classes without modifying them directly.

### Implementation Details

**ServerPlayerMixin.java**:
```java
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void getCustomTabName(CallbackInfoReturnable<Component> cir) {
        // Custom logic here
    }
}
```

**Injection Parameters**:
- `method`: Target method name and descriptor
- `at = @At("HEAD")`: Inject at method start
- `cancellable = true`: Allow canceling original method

### ExampleClientMixin.java

Client-side mixin template for future features:
- Shows how to inject into Minecraft class
- Currently a placeholder
- Can be expanded for client features

## Testing Guide

### Test Structure

**Location**: `src/test/java/wm/modid/waystone/`

**Test Classes**:
- `WaystoneBlueMapSyncTest.java`: Main test suite
- `WaystoneBlueMapSyncTestHelper.java`: Test utilities

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests WaystoneBlueMapSyncTest

# Run with verbose output
./gradlew test --info
```

### Test Coverage

| Component | Test Count | Critical Tests |
|-----------|------------|-----------------|
| Marker Creation | 1 | testMarkerIsAddedForWaystone |
| Marker Properties | 1 | testMarkerHasCorrectLabel |
| Marker Deletion | 1 | testMarkerIsRemovedWhenWaystoneDeleted |
| Error Handling | 1 | testNoMarkerSetCreatedWhenWorldNotFound |

### Mock Usage

Tests use Mockito for isolation:
```java
@Mock BlueMapAPI api;
@Mock Waystone waystone;

when(waystone.getName()).thenReturn(Component.literal("Test"));
```

### Writing New Tests

1. Use `@BeforeEach` for setup
2. Mock external dependencies  
3. Test single behavior per test
4. Use descriptive assertion messages
5. Document test purpose with JavaDoc

## Configuration

### gradle.properties

**Key Settings**:
```ini
minecraft_version=26.1.2
waystones_version=26.1.2.2
loader_version=0.19.2
mod_version=1.0.0
maven_group=wm.modid
```

### fabric.mod.json

**Mod Metadata**:
- MOD_ID: "wm"
- Name: "Wm"
- Description: Mod functionality description
- Contact: Author information
- License: CC0

### Build Configuration (build.gradle)

- **Java Version**: 25
- **Gradle Wrapper**: Latest stable
- **Publishing**: Maven publication configured

## Troubleshooting

### Issue: Command not recognized

**Cause**: Mod not loaded properly

**Solution**:
1. Verify mod JAR in mods folder
2. Check Minecraft version compatibility
3. Review server logs for errors

### Issue: No ping display in tab list

**Causes & Solutions**:
- BlueMap not installed or not loaded at startup → Install/enable BlueMap
- Player info update failing → Check server logs
- Mixin not applied → Clear cache and rebuild

### Issue: TPS alerts not appearing

**Causes**:
1. Player not an operator (ops.json)
2. Server not under load threshold
3. Cooldown period still active

**Solution**: Check operator status or trigger server load

### Issue: Waystones not syncing to BlueMap

**Causes**:
1. BlueMap not installed
2. Waystones not installed
3. World not found in BlueMap
4. Dimension mismatch

**Solution**:
- Verify both mods are installed
- Check server logs for sync messages
- Review BlueMap marker sets

### Debug Logging

Enable debug logging in your launcher config:
```
-Dlog4j2.Level=DEBUG
```

Check logs in: `logs/latest.log`

---

## Development Workflow

### Setting Up Development Environment

1. **Clone Repository**
   ```bash
   git clone <repo-url>
   cd wm-mod
   ```

2. **Load IDE**
   ```bash
   ./gradlew idea  # For IntelliJ IDEA
   ./gradlew eclipse  # For Eclipse
   ```

3. **Run Dev Server**
   ```bash
   ./gradlew runServer
   ```

### Making Changes

1. Edit source files
2. Changes auto-refresh in dev environment
3. Verify in-game
4. Write tests for new features
5. Build and test: `./gradlew build`

## Performance Optimization

### Current Optimizations

- **Tick-based Updates**: Batch updates to reduce packet frequency
- **Cooldown Caching**: Alert cooldown prevents rapid message sending
- **Lazy Initialization**: Features only init if dependencies present
- **Minimal Client Load**: Most logic server-side

### Potential Improvements

- Cache waystone data to reduce API calls
- Compress marker updates
- Implement regional marker loading
- Add configuration options

---

**Last Updated**: 2026-05-16
**Documentation Version**: 1.0.0

