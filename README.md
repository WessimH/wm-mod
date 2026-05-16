# Wm (Waystone Manager) - Minecraft Mod

A comprehensive Fabric mod for Minecraft that provides advanced server monitoring, player statistics, and waystone management features.

## 🎯 Features

- **📊 Server Performance Monitoring**
  - Real-time TPS (Ticks Per Second) tracking
  - MSPT (Milliseconds Per Tick) measurements
  - Automatic performance alerts for server operators
  - Color-coded performance indicators

- **👥 Player Information Display**
  - Live ping display in the player tab list
  - Color-coded latency indicators
  - Real-time network latency monitoring

- **🗺️ BlueMap Integration**
  - Automatic waystone synchronization to BlueMap markers
  - Real-time waystone tracking across dimensions
  - POI markers with waystone details
  - Automatic marker cleanup for deleted waystones

- **⚙️ Admin Commands**
  - `/tps` - Display current server performance metrics

## 📋 Requirements

- **Minecraft Version**: 1.26.1.2
- **Loader**: Fabric Loader 0.19.2+
- **Java**: JDK 25+

### Optional Dependencies

- **BlueMap** (for map marker integration)
- **Waystones** (for waystone tracking)

> **Note**: BlueMap integration only activates if both BlueMap and Waystones mods are present on the server.

## 📦 Installation

1. Download the compiled mod JAR from the [Releases](../../releases) page
2. Place it in your `mods` folder
3. Launch your Minecraft server with Fabric Loader
4. The mod will initialize automatically on server startup

## 🔨 Building from Source

### Prerequisites
- Git
- JDK 25 or higher
- Gradle 8.0+ (included via wrapper)

### Build Steps

```bash
# Clone the repository
git clone https://github.com/yourusername/wm-mod.git
cd wm-mod

# Build the mod
./gradlew build

# The compiled JAR will be in build/libs/
```

## 📚 Project Structure

```
src/
├── main/
│   ├── java/wm/modid/
│   │   ├── Wm.java                           # Main mod entry point
│   │   ├── commands/
│   │   │   ├── TpsCommand.java              # /tps command handler
│   │   │   ├── TpsTracker.java              # Performance monitoring
│   │   │   └── TabPingDisplay.java          # Player ping display
│   │   ├── mixin/
│   │   │   └── ServerPlayerMixin.java       # Tab name customization
│   │   └── waystone/
│   │       └── WaystoneBlueMapSync.java     # Waystone→BlueMap sync
│   └── resources/
│       └── fabric.mod.json                   # Mod metadata
├── client/
│   └── java/wm/modid/client/
│       ├── WmClient.java                     # Client entry point
│       └── mixin/
│           └── ExampleClientMixin.java       # Example client mixin
└── test/
    └── java/wm/modid/waystone/
        ├── WaystoneBlueMapSyncTest.java     # Unit tests
        └── WaystoneBlueMapSyncTestHelper.java # Test utilities
```

## 🎮 Configuration

The mod works out-of-the-box with sensible defaults. Here are the performance alert thresholds:

| Metric | Warning | Critical |
|--------|---------|----------|
| TPS | ≤ 15.0 | ≤ 10.0 |
| MSPT | ≥ 60ms | - |

Alert cooldown: 5 seconds (prevents message spam)

## 📖 Usage

### /tps Command

Display current server performance metrics:

```
/tps
```

Output shows:
- Current TPS (color-coded)
- Current MSPT
- Your player ping

### Performance Monitoring

Server operators will receive automatic alerts when:
- TPS drops below the warning threshold
- MSPT exceeds the warning threshold
- Server performance returns to normal

### Tab List Enhancement

Players will see live ping measurements next to each player's name in the tab list:
- 🟢 Green: ≤ 80ms (excellent)
- 🟡 Yellow: 80-149ms (good)
- 🔴 Red: ≥ 150ms (poor)

### BlueMap Markers

When both BlueMap and Waystones mods are loaded:
- Waystones automatically appear as markers on the map
- Markers update every minute
- Hover over markers to see waystone coordinates
- Markers maintain dimension accuracy

## 🧪 Testing

Run the test suite:

```bash
./gradlew test
```

Tests include:
- Waystone marker creation and validation
- Marker label correctness
- Waystone deletion handling
- Error handling for missing worlds

## 🛠️ Development

### Code Structure

The mod follows the Fabric development guidelines:

- **Server-side**: Performance tracking, command handling, marker synchronization
- **Client-side**: Minimal; primarily for rendering and UI updates
- **Mixins**: Used to inject custom behavior into Minecraft classes

### Adding Features

1. Create new classes in appropriate packages
2. Add comprehensive JavaDoc comments
3. Register event listeners in `Wm.java`
4. Write unit tests for new functionality
5. Update documentation

### Code Style

- Use meaningful variable and method names
- Add JavaDoc comments to all public methods
- Follow Fabric naming conventions
- Keep methods focused and testable

## 📄 License

This project is licensed under the CC0 License. See the [LICENSE](LICENSE) file for details.

This means you can use, modify, and distribute this code freely with no attribution required.

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes with clear commit messages
4. Add tests for new functionality
5. Update documentation as needed
6. Submit a pull request

## 🐛 Bug Reports & Feature Requests

Found a bug or have a feature idea?
- [Open an Issue](../../issues)
- Include reproduction steps for bugs
- Provide clear descriptions and examples

## 📞 Support

For questions or issues:
- Check the [Documentation](DOCUMENTATION.md)
- Review existing [Issues](../../issues)
- See [Fabric Documentation](https://docs.fabricmc.net/)

## 🙏 Credits

Built with:
- [Fabric](https://fabricmc.net/) - Minecraft modding framework
- [Waystones](https://www.curseforge.com/minecraft/mc-mods/waystones) - Waystone mod
- [BlueMap](https://www.spigotmc.org/resources/bluemap.83557/) - Map rendering

## 📋 Changelog

See [CHANGELOG.md](CHANGELOG.md) for version history and updates.

---

**Made with ❤️ for the Minecraft community**
