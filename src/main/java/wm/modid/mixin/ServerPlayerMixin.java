package wm.modid.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wm.modid.commands.TabPingDisplay;
import net.minecraft.resources.Identifier;

/**
 * Mixin for ServerPlayer class to customize tab list display names.
 *
 * This mixin intercepts the getTabListDisplayName() method to inject
 * custom display names with ping information from TabPingDisplay.
 * If a custom display name exists for the player, it replaces the default name.
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    /**
     * Injects custom tab display name at the start of getTabListDisplayName().
     *
     * Attempts to retrieve a custom display name (with ping) from TabPingDisplay.
     * If found, sets it as the return value and cancels the original method.
     * Otherwise, allows the original method to proceed.
     *
     * @param cir The callback info returnable for controlling method execution
     */
    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    private void getCustomTabName(CallbackInfoReturnable<Component> cir) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        Component custom = TabPingDisplay.getDisplayName(self.getUUID());
        if (custom != null) {
            cir.setReturnValue(custom);
        }
    }
}