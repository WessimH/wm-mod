package wm.modid.commands;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;

/**
 * Minecraft server performance tracker.
 *
 * This class monitors TPS (Ticks Per Second) and MSPT (Milliseconds Per Tick)
 * and alerts server operators when performance degrades.
 *
 * Alert thresholds are:
 * - WARN: TPS <= 15.0 or MSPT >= 60.0ms
 * - CRITICAL: TPS <= 10.0
 */
public class TpsTracker {

    // alert thresholds
    private static final double TPS_WARN     = 15.0;
    private static final double TPS_CRITICAL = 10.0;
    private static final double MSPT_WARN    = 60.0;

    // Cooldown to prevent spam (in ticks)
    private static final int ALERT_COOLDOWN = 100; // ~5 seconds

    private static long lastTickTime    = 0;
    private static double mspt          = 50.0;
    private static int cooldownTicks    = 0;
    private static AlertLevel lastAlert = AlertLevel.NONE;

    /**
     * Enumeration of performance alert levels.
     *
     * NONE: Normal performance
     * WARN: Degraded performance (low TPS)
     * CRITICAL: Critical performance (significant lag)
     */
    public enum AlertLevel { NONE, WARN, CRITICAL }

    /**
     * Registers the tracker on the END_SERVER_TICK event.
     *
     * Performs performance measurements at each server tick end
     * and triggers alerts if necessary.
     */
    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = System.nanoTime();

            if (lastTickTime > 0) {
                mspt = (now - lastTickTime) / 1_000_000.0;
                checkAndAlert(server);
            }

            lastTickTime = now;
            if (cooldownTicks > 0) cooldownTicks--;
        });
    }

    /**
     * Checks performance and sends alerts if necessary.
     *
     * Compares current TPS/MSPT with thresholds and broadcasts a message
     * to operators if the alert level changes.
     *
     * @param server The Minecraft server
     */
    private static void checkAndAlert(MinecraftServer server) {
        if (cooldownTicks > 0) return;

        double tps = getTps();
        AlertLevel current;
        String message;

        if (tps <= TPS_CRITICAL) {
            current = AlertLevel.CRITICAL;
            message = String.format(
                    "§c⚠ CRITICAL TPS: §f%.1f TPS §7| §fMSPT: %.1fms §c— Server is overloaded!",
                    tps, mspt
            );
        } else if (tps <= TPS_WARN || mspt >= MSPT_WARN) {
            current = AlertLevel.WARN;
            message = String.format(
                    "§e⚠ Low TPS: §f%.1f TPS §7| §fMSPT: %.1fms §e— Possible lag detected.",
                    tps, mspt
            );
        } else {
            if (lastAlert != AlertLevel.NONE) {
                broadcast(server, "§a✔ TPS back to normal: §f" + String.format("%.1f TPS", tps));
                cooldownTicks = ALERT_COOLDOWN;
                lastAlert = AlertLevel.NONE;
            }
            return;
        }

        if (current != lastAlert) {
            broadcast(server, message);
            cooldownTicks = ALERT_COOLDOWN;
            lastAlert = current;
        }
    }

    /**
     * Broadcasts a message to server operators.
     *
     * Sends a system message only to players with operator status.
     *
     * @param server The Minecraft server
     * @param message The message to broadcast
     */
    private static void broadcast(MinecraftServer server, String message) {
        server.getPlayerList().getPlayers().stream()
                .filter(player -> server.getPlayerList().isOp(
                        new NameAndId(player.getUUID(), player.getName().getString())
                ))
                .forEach(player -> player.sendSystemMessage(Component.literal(message)));
    }

    /**
     * Returns the current MSPT (Milliseconds Per Tick).
     *
     * @return The time elapsed per tick in milliseconds
     */
    public static double getMspt() { return mspt; }

    /**
     * Returns the current TPS (Ticks Per Second).
     *
     * Calculated from MSPT (20.0 / (MSPT / 1000)).
     * The maximum value is 20.0 TPS.
     *
     * @return The current TPS (max 20.0)
     */
    public static double getTps()  { return Math.min(20.0, 1000.0 / mspt); }
}