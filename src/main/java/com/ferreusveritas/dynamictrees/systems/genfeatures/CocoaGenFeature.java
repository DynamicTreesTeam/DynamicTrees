package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.nodemappers.CocoaFruitNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class CocoaGenFeature extends GenFeature implements IPostGenFeature, IPostGrowFeature {

    public CocoaGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
    }

    @Override
    public boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int fertility, boolean natural) {
        if (fertility == 0 && world.random.nextInt() % 16 == 0) {
            if (species.seasonalFruitProductionFactor(world, treePos) > world.random.nextFloat()) {
                this.addCocoa(world, rootPos, false);
            }
        }
        return false;
    }

    @Override
    public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
        if (world.getRandom().nextInt() % 8 == 0) {
            this.addCocoa(world, rootPos, true);
            return true;
        }
        return false;
    }

    private void addCocoa(IWorld world, BlockPos rootPos, boolean worldGen) {
        TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(new CocoaFruitNode().setWorldGen(worldGen)));
    }

}
