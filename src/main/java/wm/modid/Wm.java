package wm.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wm.modid.commands.TabPingDisplay;
import wm.modid.commands.TpsCommand;

/**
 * Main entry point for the Wm (Waystone Manager) mod.
 *
 * This class initializes all core mod features including:
 * - Server performance monitoring (TPS/MSPT tracking)
 * - Player ping display in the tab list
 * - /tps command for server operators
 * - BlueMap integration for waystone markers (if both mods are loaded)
 *
 * The mod is designed for Fabric and provides operators with
 * real-time performance metrics and enhanced server monitoring capabilities.
 */
public class Wm implements ModInitializer {

    /**
     * Unique identifier for the Wm mod.
     * Used for logging, configuration files, and asset locations.
     */
    public static final String MOD_ID = "wm";

    /**
     * Logger instance for the mod.
     * Use this to log debug information, warnings, and errors.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /**
     * Initializes all mod features when the server starts.
     *
     * Registers the following components:
     * 1. TPS Tracker - Monitors server performance and alerts operators
     * 2. TPS Command - Registers the /tps command
     * 3. Tab Ping Display - Shows player latency in the tab list
     * 4. Waystone BlueMap Sync - Syncs waystones to BlueMap (if both mods are loaded)
     *
     * The BlueMap integration is conditional and only activates if both
     * BlueMap and Waystones mods are detected on the server.
     */
    @Override
    public void onInitialize() {
        TpsCommand.register();
        TabPingDisplay.register();
        wm.modid.grave.GraveManager.register();

        if (FabricLoader.getInstance().isModLoaded("bluemap")
                && FabricLoader.getInstance().isModLoaded("waystones")) {
            ServerLifecycleEvents.SERVER_STARTED.register(server ->
                wm.modid.waystone.WaystoneBlueMapSync.register(server)
            );
            LOGGER.info("BlueMap + Waystones détectés, sync activée.");
        } else {
            LOGGER.info("BlueMap ou Waystones absent, sync désactivée.");
        }

        LOGGER.info("Wm mod chargé !");
    }
}
