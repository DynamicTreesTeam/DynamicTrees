package com.ferreusveritas.dynamictrees.trees;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public final class DarkOakSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(DarkOakSpecies::new);

    public DarkOakSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
    }

    @Override
    public int getLowestBranchHeight(World world, BlockPos pos) {
        return (int) (super.getLowestBranchHeight(world, pos) * this.biomeSuitability(world, pos));
    }

    @Override
    public float getGrowthRate(World world, BlockPos pos) {
        return super.getGrowthRate(world, pos) * biomeSuitability(world, pos);
    }

}
