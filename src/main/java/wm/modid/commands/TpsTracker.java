package wm.modid.commands;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TpsTracker {

    private static long lastTickTime = 0;
    private static double mspt = 50.0;

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = System.nanoTime();
            if (lastTickTime > 0) {
                mspt = (now - lastTickTime) / 1_000_000.0;
            }
            lastTickTime = now;
        });
    }

    public static double getMspt() { return mspt; }
    public static double getTps()  { return Math.min(20.0, 1000.0 / mspt); }
}
