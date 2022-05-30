package com.ferreusveritas.dynamictrees.systems.dropcreators.context;

import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

/**
 * @author Harley O'Connor
 */
public class LogDropContext extends DropContext {

    private final NetVolumeNode.Volume volume;

    public LogDropContext(World world, BlockPos pos, Species species, List<ItemStack> dropList, NetVolumeNode.Volume volume, ItemStack tool) {
        super(world, pos, species, dropList, tool, -1, 0);
        this.volume = volume;
    }

    public NetVolumeNode.Volume volume() {
        return volume;
    }

}
