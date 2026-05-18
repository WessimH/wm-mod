package wm.modid.grave;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.markers.HtmlMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import wm.modid.Wm;

public class GraveBlueMapDisplay {

    private static final String MARKER_SET_ID = "graves";

    /**
     * Adds a skull marker on BlueMap at the grave's position.
     */
    public static void addMarker(Grave grave) {
        BlueMapAPI.getInstance().ifPresent(api -> {
            api.getWorld(grave.getDimension()).ifPresent(world -> {
                world.getMaps().forEach(map -> {
                    MarkerSet set = map.getMarkerSets().computeIfAbsent(
                        MARKER_SET_ID,
                        id -> MarkerSet.builder().label("💀 Graves").build()
                    );

                    BlockPos pos = grave.getPos();
                    String html =
                        "<div style='" +
                            "background:rgba(0,0,0,0.6);" +
                            "color:#fff;" +
                            "padding:3px 8px;" +
                            "border-radius:2px;" +
                            "font-family:\"Courier New\",monospace;" +
                            "font-size:13px;" +
                            "white-space:nowrap;" +
                            "pointer-events:none;" +
                        "'>💀 " + grave.getPlayerName() + "</div>";

                    set.put(grave.getGraveId().toString(),
                        HtmlMarker.builder()
                            .label("💀 " + grave.getPlayerName())
                            .html(html)
                            .position(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5)
                            .maxDistance(100000)
                            .build()
                    );
                });
            });
        });
    }

    /**
     * Removes the BlueMap marker when the grave is collected.
     */
    public static void removeMarker(Grave grave) {
        BlueMapAPI.getInstance().ifPresent(api -> {
            api.getWorld(grave.getDimension()).ifPresent(world -> {
                world.getMaps().forEach(map -> {
                    MarkerSet set = map.getMarkerSets().get(MARKER_SET_ID);
                    if (set != null) {
                        set.remove(grave.getGraveId().toString());
                    }
                });
            });
        });
    }
}
