package wm.modid.waystone;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.blay09.mods.waystones.api.Waystone;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.Optional;

/**
 * Helper de test qui reproduit la logique de WaystoneBlueMapSync
 * sans dépendre du serveur Minecraft.
 */
public class WaystoneBlueMapSyncTestHelper {

    private static final String MARKER_SET_ID = "waystones";

    public static void addMarkerPublic(BlueMapAPI api, Waystone waystone, Map<String, MarkerSet> markerSets) {
        Optional<BlueMapWorld> worldOpt = api.getWorld(new Object());
        if (worldOpt.isEmpty()) return;

        BlueMapWorld world = worldOpt.get();
        for (BlueMapMap map : world.getMaps()) {
            MarkerSet set = markerSets.computeIfAbsent(
                MARKER_SET_ID,
                id -> MarkerSet.builder().label("✦ Waystones").build()
            );

            BlockPos pos = waystone.getPos();
            String label = waystone.getName().getString();

            set.put(waystone.getWaystoneUid().toString(),
                POIMarker.builder()
                    .label(label)
                    .detail("<b>" + label + "</b><br>"
                        + pos.getX() + ", " + pos.getY() + ", " + pos.getZ())
                    .position(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)
                    .maxDistance(100000)
                    .build()
            );
        }
    }
}
