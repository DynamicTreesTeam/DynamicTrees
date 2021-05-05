package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import com.ferreusveritas.dynamictrees.systems.dropcreators.drops.Drops;
import com.ferreusveritas.dynamictrees.systems.nodemappers.NetVolumeNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author Harley O'Connor
 */
public final class NormalDropCreator extends DropCreator {

    public final Map<DropType<DropContext>, Drops> dropsMap = new HashMap<>();

    public NormalDropCreator(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    public List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> dropList, int soilLife, int fortune) {
        return this.dropsMap.get(DropType.HARVEST).appendDrops(dropList, random, fortune);
    }

    @Override
    public List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> dropList, int soilLife) {
        return this.dropsMap.get(DropType.VOLUNTARY).appendDrops(dropList, random, 0);
    }

    @Override
    public List<ItemStack> getLeavesDrop(World access, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, int fortune) {
        return this.dropsMap.get(DropType.LEAVES).appendDrops(dropList, random, fortune);
    }

    @Override
    public List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> dropList, NetVolumeNode.Volume volume) {
        return this.dropsMap.get(DropType.LOGS).appendDrops(dropList, random, 0);
    }

}
