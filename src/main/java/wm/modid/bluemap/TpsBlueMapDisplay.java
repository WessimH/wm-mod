package wm.modid.bluemap;

import de.bluecolored.bluemap.api.BlueMapAPI;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import wm.modid.Wm;
import wm.modid.commands.TpsTracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TpsBlueMapDisplay {

    private static final int UPDATE_INTERVAL = 20;
    private static final String JSON_FILE = "wm-tps.json";
    private static final String JS_FILE   = "wm-tps-display.js";

    public static void register(BlueMapAPI api) {
        try {
            Path webRoot = api.getWebApp().getWebRoot();
            writeJsFile(webRoot);
            api.getWebApp().registerScript(JS_FILE);

            ServerTickEvents.END_SERVER_TICK.register(server -> {
                if (server.getTickCount() % UPDATE_INTERVAL != 0) return;
                writeTpsJson(webRoot);
            });

            Wm.LOGGER.info("TPS BlueMap display activé.");
        } catch (IOException e) {
            Wm.LOGGER.error("Erreur TPS BlueMap display", e);
        }
    }

    private static void writeTpsJson(Path webRoot) {
        try {
            String json = String.format(
                "{\"tps\":%.1f,\"mspt\":%.1f}",
                TpsTracker.getTps(),
                TpsTracker.getMspt()
            );
            Files.writeString(webRoot.resolve(JSON_FILE), json);
        } catch (IOException e) {
            Wm.LOGGER.warn("Impossible d'écrire wm-tps.json", e);
        }
    }

    private static void writeJsFile(Path webRoot) throws IOException {
        String js = """
                (function () {
                    const div = document.createElement('div');
                    div.id = 'wm-tps';
                    div.style.cssText = [
                        'position:fixed',
                        'bottom:20px',
                        'right:20px',
                        'background:rgba(0,0,0,0.65)',
                        'color:#fff',
                        'padding:8px 14px',
                        'border-radius:8px',
                        'font-family:monospace',
                        'font-size:14px',
                        'z-index:9999',
                        'pointer-events:none',
                        'user-select:none'
                    ].join(';');
                    document.body.appendChild(div);

                    function tpsColor(tps) {
                        if (tps >= 18) return '#4caf50';
                        if (tps >= 12) return '#ff9800';
                        return '#f44336';
                    }

                    function update() {
                        fetch('/wm-tps.json?_=' + Date.now())
                            .then(r => r.json())
                            .then(d => {
                                div.innerHTML =
                                    '<span style="color:' + tpsColor(d.tps) + '">⬡ ' +
                                    d.tps.toFixed(1) + ' TPS</span>' +
                                    '<span style="color:#aaa"> / ' + d.mspt.toFixed(1) + ' ms</span>';
                            })
                            .catch(() => {});
                    }

                    update();
                    setInterval(update, 5000);
                })();
                """;
        Files.writeString(webRoot.resolve(JS_FILE), js);
    }
}
