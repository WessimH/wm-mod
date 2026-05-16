package wm.modid.commands;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.lang.reflect.Field;
import static net.minecraft.commands.Commands.literal;

/**
 * Command handler for the /tps command.
 *
 * Provides server operators with a command to check real-time server
 * performance metrics including current TPS (Ticks Per Second) and
 * MSPT (Milliseconds Per Tick) values.
 *
 * Command: /tps
 * Output: Displays current TPS and MSPT with color coding:
 *         - Green (§a): TPS >= 18
 *         - Yellow (§e): TPS 12-17
 *         - Red (§c): TPS < 12
 */
public class TpsCommand {

    /**
     * Registers the /tps command with the command dispatcher.
     * The command returns its execution status (1 for success).
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) ->
                dispatcher.register(literal("tps")
                        .executes(TpsCommand::executeTps)
                )
        );
    }

    /**
     * Executes the /tps command and sends performance metrics to the command sender.
     *
     * Retrieves current TPS and MSPT values from TpsTracker and formats them
     * with color coding based on performance thresholds.
     *
     * @param context The command context containing the command source and arguments
     * @return 1 to indicate successful command execution
     */
    private static int executeTps(CommandContext<CommandSourceStack> context) {
        double tps = TpsTracker.getTps();
        double mspt = TpsTracker.getMspt();

        ServerPlayer player = context.getSource().getEntity() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        String ping = player != null ? getPing(player) + "ms" : "N/A";

        String color;
        if (tps >= 18) {
            color = "§a";
        } else if (tps >= 12) {
            color = "§e";
        } else {
            color = "§c";
        }

        context.getSource().sendSuccess(
                () -> Component.literal(
                        String.format("§6TPS: %s%.1f §7| §6MSPT: §f%.1fm §7| §6Ping: §f%s", color, tps, mspt, ping)
                ),
                false
        );
        return 1;
    }

    private static int getPing(ServerPlayer player) {
        try {
            Field latencyField = player.connection.getClass().getDeclaredField("latency");
            latencyField.setAccessible(true);
            return latencyField.getInt(player.connection);
        } catch (ReflectiveOperationException e) {
            return -1;
        }
    }
}