package wm.modid.grave;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GraveManager {

    // Active graves indexed by "x,y,z,dimension"
    private static final Map<String, Grave> graves = new ConcurrentHashMap<>();

    /**
     * Registers all event listeners for the grave system.
     */
    public static void register() {
        // Listen for player deaths
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                onPlayerDeath(player);
            }
        });

        // Listen for block breaks to drop grave items
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                onBlockBroken(serverPlayer, pos, state);
            }
        });

        // Listen for right-clicks on grave heads to open the inventory
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (!state.is(Blocks.PLAYER_HEAD) && !state.is(Blocks.PLAYER_WALL_HEAD)) return InteractionResult.PASS;

            String key = graveKey(pos, world.dimension().identifier().toString());
            Grave grave = graves.get(key);
            if (grave == null) return InteractionResult.PASS;

            openGraveInventory(serverPlayer, grave);
            return InteractionResult.SUCCESS;
        });
    }

    /**
     * Called when a player dies. Saves their inventory and places a grave head.
     */
    private static void onPlayerDeath(ServerPlayer player) {
        BlockPos pos = player.blockPosition();

        // Copy all inventory slots before Minecraft clears them
        List<ItemStack> items   = copyInventory(player, 0, 36);
        List<ItemStack> armor   = copyInventory(player, 36, 40);
        List<ItemStack> offhand = copyInventory(player, 40, 41);

        Grave grave = new Grave(
            player.getUUID(),
            player.getName().getString(),
            pos,
            player.level().dimension(),
            items, armor, offhand
        );

        String key = graveKey(pos, player.level().dimension().identifier().toString());
        graves.put(key, grave);

        // Place the grave head with the dead player's skin
        player.level().setBlock(pos, Blocks.PLAYER_HEAD.defaultBlockState(), 3);
        BlockEntity be = player.level().getBlockEntity(pos);
        if (be instanceof SkullBlockEntity skull) {
            ItemStack headStack = new ItemStack(Items.PLAYER_HEAD);
            headStack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(player.getGameProfile()));
            skull.applyComponentsFromItemStack(headStack);
        }

        // Add BlueMap marker if BlueMap is loaded
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("bluemap")) {
            GraveBlueMapDisplay.addMarker(grave);
        }

        player.sendSystemMessage(Component.literal(
            "§7Your grave is at §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
        ));
    }

    /**
     * Opens a chest-like inventory containing the grave's items.
     */
    private static void openGraveInventory(ServerPlayer player, Grave grave) {
        // Merge all grave items into a single list (max 45 slots for GENERIC_9x5)
        List<ItemStack> all = new ArrayList<>();
        all.addAll(grave.getItems());
        all.addAll(grave.getArmor());
        all.addAll(grave.getOffhand());

        // Use a 45-slot container (9x5 chest)
        SimpleContainer container = new SimpleContainer(45);
        for (int i = 0; i < Math.min(all.size(), 45); i++) {
            container.setItem(i, all.get(i));
        }

        String key = graveKey(grave.getPos(), grave.getDimension().identifier().toString());

        player.openMenu(new SimpleMenuProvider(
            (syncId, inv, p) -> {
                ChestMenu menu = new ChestMenu(MenuType.GENERIC_9x5, syncId, inv, container, 5);

                // Remove the grave when all items have been taken
                menu.addSlotListener(new ContainerListener() {
                    @Override
                    public void slotChanged(AbstractContainerMenu menu, int slot, ItemStack stack) {
                        boolean allEmpty = true;
                        for (int i = 0; i < container.getContainerSize(); i++) {
                            if (!container.getItem(i).isEmpty()) {
                                allEmpty = false;
                                break;
                            }
                        }
                        if (allEmpty) {
                            graves.remove(key);
                            // Remove BlueMap marker
                            if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("bluemap")) {
                                GraveBlueMapDisplay.removeMarker(grave);
                            }
                            // Remove the head block from the world
                            ServerLevel level = (ServerLevel) p.level();
                            BlockPos pos = grave.getPos();
                            if (level.getBlockState(pos).is(Blocks.PLAYER_HEAD)
                                    || level.getBlockState(pos).is(Blocks.PLAYER_WALL_HEAD)) {
                                level.removeBlock(pos, false);
                            }
                        }
                    }

                    @Override
                    public void dataChanged(AbstractContainerMenu menu, int id, int value) {}
                });

                return menu;
            },
            Component.literal("⚰ " + grave.getPlayerName() + "'s Grave")
        ));
    }

    /**
     * Called when a block is broken. Drops stored grave items if it was a grave head.
     */
    private static void onBlockBroken(ServerPlayer player, BlockPos pos, BlockState state) {
        if (!state.is(Blocks.PLAYER_HEAD) && !state.is(Blocks.PLAYER_WALL_HEAD)) return;

        String key = graveKey(pos, player.level().dimension().identifier().toString());
        Grave grave = graves.remove(key);
        if (grave == null) return;

        // Remove BlueMap marker
        if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("bluemap")) {
            GraveBlueMapDisplay.removeMarker(grave);
        }

        // Drop all items at the grave position
        ServerLevel level = player.level();
        dropItems(level, pos, grave.getItems());
        dropItems(level, pos, grave.getArmor());
        dropItems(level, pos, grave.getOffhand());
    }

    private static void dropItems(ServerLevel level, BlockPos pos, List<ItemStack> items) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                ItemEntity entity = new ItemEntity(
                    level,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    stack.copy()
                );
                level.addFreshEntity(entity);
            }
        }
    }

    /**
     * Copies a range of inventory slots into a list.
     */
    private static List<ItemStack> copyInventory(ServerPlayer player, int from, int to) {
        List<ItemStack> copy = new ArrayList<>();
        for (int i = from; i < to; i++) {
            copy.add(player.getInventory().getItem(i).copy());
        }
        return copy;
    }

    private static String graveKey(BlockPos pos, String dimension) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ() + "," + dimension;
    }
}
