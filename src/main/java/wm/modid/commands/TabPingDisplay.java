package wm.modid.commands;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player latency display in the tab list.
 *
 * Displays each player's ping (latency in milliseconds) next to their name
 * in the tab list with color coding for visual feedback:
 * - Green (§a): Ping < 80ms (excellent)
 * - Yellow (§e): Ping 80-149ms (good)
 * - Red (§c): Ping >= 150ms (poor)
 *
 * Updates occur every second (20 ticks) for performance efficiency.
 */
public class TabPingDisplay {

    /**
     * Cache of player display names with ping information.
     * Maps UUID to the formatted Component containing player name and ping.
     */
    private static final Map<UUID, Component> displayNames = new HashMap<>();

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private TabPingDisplay() {
        throw new AssertionError("Cannot instantiate TabPingDisplay");
    }

    /**
     * Retrieves the display name component for a specific player.
     *
     * @param uuid The UUID of the player
     * @return The Component containing the player's name and ping display, or null if not found
     */
    public static Component getDisplayName(UUID uuid) {
        return displayNames.get(uuid);
    }

    /**
     * Registers the tab ping display listener on the server tick event.
     *
     * Updates player display names in the tab list every 20 ticks (1 second)
     * with current latency information. Broadcasts player info updates to all clients.
     */
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 != 0) return;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                int ping = player.connection.latency();
                String pingColor = ping < 80 ? "§a" : ping < 150 ? "§e" : "§c";

                Component displayName = Component.literal(player.getName().getString())
                        .append(Component.literal(" " + pingColor + "[" + ping + "ms]"));

                displayNames.put(player.getUUID(), displayName);

                // Broadcast the tab display name update
                server.getPlayerList().broadcastAll(
                        new ClientboundPlayerInfoUpdatePacket(
                                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
                                List.of(player)
                        )
                );
            }
        });
    }
}