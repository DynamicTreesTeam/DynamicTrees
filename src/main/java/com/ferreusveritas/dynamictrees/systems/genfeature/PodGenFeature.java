package com.ferreusveritas.dynamictrees.systems.genfeature;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.compat.season.SeasonHelper;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.systems.nodemapper.PodGenerationNode;
import com.ferreusveritas.dynamictrees.systems.pod.Pod;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Random;

public class PodGenFeature extends GenFeature {

    public static final ConfigurationProperty<Pod> POD = ConfigurationProperty.property("pod", Pod.class);
    //inverse of pods per block. 8 means one pod will generate for every 8 attempts.
    public static final ConfigurationProperty<Integer> BLOCKS_PER_POD = ConfigurationProperty.integer("blocks_per_pod");

    public PodGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(POD, PLACE_CHANCE, FRUITING_RADIUS, BLOCKS_PER_POD);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(POD, Pod.NULL)
                .with(PLACE_CHANCE, 0.8F)
                .with(FRUITING_RADIUS, 8)
                .with(BLOCKS_PER_POD, 29);
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        final LevelAccessor level = context.level();
        final BlockState blockState = level.getBlockState(context.treePos());
        final BranchBlock branch = TreeHelper.getBranch(blockState);
        if (context.natural() && branch != null && branch.getRadius(blockState) >= configuration.get(FRUITING_RADIUS)
                && shouldGrow(configuration, context.species(), context.levelContext(), context.treePos(), context.random())) {
            Pod pod = configuration.get(POD);
            this.place(pod, pod::place, level, context.pos(),
                    SeasonHelper.getSeasonValue(context.levelContext(), context.pos()), configuration.get(BLOCKS_PER_POD));
        }
        return false;
    }

    private boolean shouldGrow(GenFeatureConfiguration configuration, Species species, LevelContext levelContext, BlockPos treePos,
                               Random random) {
        return species.seasonalFruitProductionFactor(levelContext, treePos) >
                random.nextFloat() && random.nextFloat() <= configuration.get(PLACE_CHANCE);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (shouldGenerate(configuration, context.random())) {
            Pod pod = configuration.get(POD);
            this.place(pod, pod::placeDuringWorldGen, context.level(), context.pos(),
                    context.seasonValue(), configuration.get(BLOCKS_PER_POD));
            return true;
        }
        return false;
    }

    private boolean shouldGenerate(GenFeatureConfiguration configuration, Random random) {
        return random.nextFloat() <= configuration.get(PLACE_CHANCE);
    }

    private void place(Pod pod, PodGenerationNode.PodPlacer podPlacer, LevelAccessor level, BlockPos rootPos,
                       @Nullable Float seasonValue, int blocksPerPod) {
        TreeHelper.startAnalysisFromRoot(level, rootPos,
                new MapSignal(new PodGenerationNode(pod, podPlacer, seasonValue, blocksPerPod)));
    }

}
