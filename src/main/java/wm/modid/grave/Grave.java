package wm.modid.grave;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Grave {

    private final UUID graveId;
    private final UUID playerUuid;
    private final String playerName;
    private final BlockPos pos;
    private final ResourceKey<Level> dimension;
    private final List<ItemStack> items;
    private final List<ItemStack> armor;
    private final List<ItemStack> offhand;

    public Grave(UUID playerUuid, String playerName, BlockPos pos, ResourceKey<Level> dimension,
                 List<ItemStack> items, List<ItemStack> armor, List<ItemStack> offhand) {
        this.graveId   = UUID.randomUUID();
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.pos        = pos;
        this.dimension  = dimension;
        // Copy the lists so they aren't affected when Minecraft clears the player's inventory
        this.items   = new ArrayList<>(items);
        this.armor   = new ArrayList<>(armor);
        this.offhand = new ArrayList<>(offhand);
    }

    public UUID getGraveId()              { return graveId; }
    public UUID getPlayerUuid()           { return playerUuid; }
    public String getPlayerName()         { return playerName; }
    public BlockPos getPos()              { return pos; }
    public ResourceKey<Level> getDimension() { return dimension; }
    public List<ItemStack> getItems()     { return items; }
    public List<ItemStack> getArmor()     { return armor; }
    public List<ItemStack> getOffhand()   { return offhand; }
}
