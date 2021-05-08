package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
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

    @SuppressWarnings("all")
    public static final ConfigurationProperty<HashMap<DropType<DropContext>, Drops>> DROP_MAP = ConfigurationProperty.property("drops",
            (Class<HashMap<DropType<DropContext>, Drops>>) new HashMap<DropType<DropContext>, Drops>().getClass());

    // temp, remove later
    public final Map<DropType<DropContext>, Drops> dropsMap = new HashMap<>();

    public NormalDropCreator(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(DROP_MAP);
    }

    @Override
    public List<ItemStack> getHarvestDrop(World world, Species species, BlockPos leafPos, Random random, List<ItemStack> drops, int soilLife, int fortune) {
        return this.dropsMap.get(DropType.HARVEST).appendDrops(drops, random, fortune);
    }

    @Override
    public List<ItemStack> getVoluntaryDrop(World world, Species species, BlockPos rootPos, Random random, List<ItemStack> drops, int soilLife) {
        return this.dropsMap.get(DropType.VOLUNTARY).appendDrops(drops, random, 0);
    }

    @Override
    public List<ItemStack> getLeavesDrop(World access, Species species, BlockPos breakPos, Random random, List<ItemStack> drops, int fortune) {
        return this.dropsMap.get(DropType.LEAVES).appendDrops(drops, random, fortune);
    }

    @Override
    public List<ItemStack> getLogsDrop(World world, Species species, BlockPos breakPos, Random random, List<ItemStack> drops, NetVolumeNode.Volume volume) {
        return this.dropsMap.get(DropType.LOGS).appendDrops(drops, random, 0);
    }

}
