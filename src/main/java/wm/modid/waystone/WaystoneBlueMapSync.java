package wm.modid.waystone;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.blay09.mods.waystones.api.WaystonesAPI;
import net.blay09.mods.waystones.api.Waystone;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import wm.modid.Wm;

import java.util.HashSet;
import java.util.Set;

/**
 * Synchronizes in-game waystones to BlueMap markers.
 *
 * This class provides real-time synchronization between the Waystones mod
 * and BlueMap, automatically creating and updating POI (Point of Interest)
 * markers for each waystone on the server's map.
 *
 * Features:
 * - Automatic marker creation when BlueMap is ready
 * - Periodic sync every minute (1200 ticks)
 * - Automatic cleanup of markers for deleted waystones
 * - Per-dimension waystone tracking
 *
 * Dependencies:
 * - BlueMap mod (for map rendering)
 * - Waystones mod (API for waystone data)
 *
 * Note: This class only activates if both BlueMap and Waystones mods are loaded.
 */
public class WaystoneBlueMapSync {

    /**
     * Identifier for the waystone marker set in BlueMap.
     */
    private static final String MARKER_SET_ID = "waystones";

    /**
     * Refresh interval for waystone synchronization.
     * Value: 1200 ticks = 60 seconds (1 minute)
     */
    private static final int REFRESH_INTERVAL = 1200;

    /**
     * Reference to the Minecraft server instance.
     * Used to access waystones and dimension information.
     */
    private static MinecraftServer server;

    /**
     * Registers the waystone sync feature on the server.
     *
     * Sets up handlers for:
     * - Immediate sync when BlueMap API becomes available
     * - Periodic sync every minute
     *
     * @param srv The Minecraft server instance
     */
    public static void register(MinecraftServer srv) {
        server = srv;

        // Quand BlueMap est prêt → sync immédiate + TPS display
        BlueMapAPI.onEnable(api -> {
            Wm.LOGGER.info("BlueMap prêt, sync des waystones...");
            syncAll(api);
            wm.modid.bluemap.TpsBlueMapDisplay.register(api);
        });

        // Rafraichit toutes les minutes
        ServerTickEvents.END_SERVER_TICK.register(s -> {
            if (s.getTickCount() % REFRESH_INTERVAL != 0) return;
            BlueMapAPI.getInstance().ifPresent(WaystoneBlueMapSync::syncAll);
        });

        Wm.LOGGER.info("WaystoneBlueMapSync enregistré.");
    }

    /**
     * Synchronizes all waystones with BlueMap markers.
     *
     * This method:
     * 1. Collects all active waystones from the server
     * 2. Creates or updates markers for each waystone
     * 3. Removes markers for deleted waystones
     *
     * @param api The BlueMapAPI instance
     */
    private static void syncAll(BlueMapAPI api) {
        if (server == null) return;

        // Collecte tous les waystones actuels
        Set<String> currentUids = new HashSet<>();
        WaystonesAPI.getAllWaystones(server).forEach(waystone -> {
            currentUids.add(waystone.getWaystoneUid().toString());
            addMarker(api, waystone);
        });

        // Supprime les marqueurs de waystones qui n'existent plus
        for (ServerLevel level : server.getAllLevels()) {
            api.getWorld(level).ifPresent(world ->
                world.getMaps().forEach(map -> {
                    MarkerSet set = map.getMarkerSets().get(MARKER_SET_ID);
                    if (set == null) return;
                    set.getMarkers().keySet().removeIf(uid -> !currentUids.contains(uid));
                })
            );
        }
    }

    /**
     * Adds a waystone marker to BlueMap.
     *
     * Creates a POI marker for the specified waystone and adds it to the
     * appropriate dimension map. The marker includes:
     * - Waystone name as label
     * - Coordinates in the detail tooltip
     * - Clickable position on the map
     * - Extended visibility range
     *
     * @param api The BlueMapAPI instance
     * @param waystone The waystone to add as a marker
     */
    private static void addMarker(BlueMapAPI api, Waystone waystone) {
        if (server == null) return;

        for (ServerLevel level : server.getAllLevels()) {
            if (!level.dimension().equals(waystone.getDimension())) continue;

            api.getWorld(level).ifPresent(world ->
                world.getMaps().forEach(map -> {
                    MarkerSet set = map.getMarkerSets().computeIfAbsent(
                        MARKER_SET_ID,
                        id -> MarkerSet.builder().label("✦ Waystones").build()
                    );

                    var pos = waystone.getPos();
                    set.put(waystone.getWaystoneUid().toString(),
                        POIMarker.builder()
                            .label(waystone.getName().getString())
                            .detail("<b>" + waystone.getName().getString() + "</b><br>"
                                + pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
                            .position(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)
                            .maxDistance(100000)
                            .build()
                    );
                })
            );
            break;
        }
    }
}
