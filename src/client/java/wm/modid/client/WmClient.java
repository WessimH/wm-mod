package wm.modid.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side entry point for the Wm mod.
 *
 * This class initializes client-side features and event listeners.
 * While currently empty, this is where client-specific functionality
 * (rendering, HUD updates, keybinds, etc.) would be registered.
 */
public class WmClient implements ClientModInitializer {
	/**
	 * Initializes client-side mod features when the client is ready.
	 *
	 * This method is called after the Minecraft client has loaded
	 * and is ready to register client-side event listeners and features.
	 */
	@Override
	public void onInitializeClient() {
		// Client-side initialization code goes here
	}
}