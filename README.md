# wm-mod

A Fabric server mod for Minecraft 26.1.2. Provides server performance monitoring, player ping display, a grave system, and BlueMap integration for both waystones and graves.

---

## Features

**Server monitoring**
- `/tps` command showing current TPS, MSPT and your ping
- Color-coded output based on performance thresholds

**Tab list**
- Live ping displayed next to each player's name

**Grave system**
- Player head placed at death position with the dead player's skin
- Right-click the head to open an inventory containing all stored items
- Grave disappears automatically when all items are collected
- Breaking the head drops all items on the ground
- Death coordinates sent to the player on death

**BlueMap integration** *(requires BlueMap + Waystones)*
- Activated waystones synced as markers on the map, updated every minute
- Grave markers added on death, removed on collection
- Live TPS/MSPT widget in the bottom-right corner of the map

---

## Requirements

| Dependency | Version | Required |
|---|---|---|
| Minecraft | 26.1.2 | Yes |
| Fabric Loader | 0.19.2+ | Yes |
| Fabric API | 0.149.0+ | Yes |
| Java | 25+ | Yes |
| Waystones | 26.1.2.2 | No |
| BlueMap | 2.7.7+ | No |

BlueMap and Waystones features only activate if both mods are present on the server.

---

## Installation

1. Download the JAR from the [Releases](../../releases) page
2. Drop it in your server's `mods/` folder
3. Start the server

If using BlueMap waystone sync, disable the built-in integration in the Waystones config:
```
blueMap.enabled = false
```

---

## Building from source

```bash
git clone https://github.com/WessimH/wm-mod.git
cd wm-mod
./gradlew build
# Output: build/libs/wm-1.0.0.jar
```

Run tests:
```bash
./gradlew test
```

---

## Project structure

```
src/
  main/java/wm/modid/
    Wm.java                          # mod entry point
    commands/
      TpsTracker.java                # TPS/MSPT measurement
      TpsCommand.java                # /tps command
      TabPingDisplay.java            # ping in tab list
    grave/
      Grave.java                     # grave data class
      GraveManager.java              # death/break/open logic
      GraveBlueMapDisplay.java       # BlueMap markers for graves
      GraveStorage.java              # persistence (WIP)
    waystone/
      WaystoneBlueMapSync.java       # waystone -> BlueMap sync
    bluemap/
      TpsBlueMapDisplay.java         # TPS widget on BlueMap
  client/java/wm/modid/client/
    WmClient.java                    # client entry point
```

---

## License

CC0 — public domain. Do whatever you want with it.
