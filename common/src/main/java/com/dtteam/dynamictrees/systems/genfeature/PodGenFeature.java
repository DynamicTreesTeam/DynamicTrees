package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.pod.Pod;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.dtteam.dynamictrees.systems.nodemapper.PodGenerationNode;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PodGenFeature extends GenFeature {

    public static final ConfigurationProperty<Pod> POD = ConfigurationProperty.property("pod", Pod.class);
    //inverse of pods per block. 8 means one pod will generate for every 8 attempts.
    public static final ConfigurationProperty<Integer> BLOCKS_PER_POD = ConfigurationProperty.integer("blocks_per_pod");
    public static final ConfigurationProperty<Integer> LOWEST_TRUNK_HEIGHT = ConfigurationProperty.integer("lowest_trunk_height");

    public PodGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(POD, PLACE_CHANCE, FRUITING_RADIUS, BLOCKS_PER_POD, LOWEST_TRUNK_HEIGHT);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(POD, Pod.NULL)
                .with(PLACE_CHANCE, 0.8F)
                .with(FRUITING_RADIUS, 8)
                .with(BLOCKS_PER_POD, 29)
                .with(LOWEST_TRUNK_HEIGHT, 0);
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        final LevelAccessor level = context.level();
        final BlockState blockState = level.getBlockState(context.treePos());
        final BranchBlock branch = TreeHelper.getBranch(blockState);
        if (context.natural() && branch != null && branch.getRadius(blockState) >= configuration.get(FRUITING_RADIUS)
                && shouldGrow(configuration, context.levelContext(), context.treePos(), context.random())) {
            Pod pod = configuration.get(POD);
            this.place(pod, pod::place, level, context.pos(),
                    SeasonHelper.getSeasonValue(context.levelContext(), context.pos()), configuration.get(BLOCKS_PER_POD), configuration.get(LOWEST_TRUNK_HEIGHT));
        }
        return false;
    }

    private boolean shouldGrow(GenFeatureConfiguration configuration, LevelContext levelContext, BlockPos treePos, RandomSource random) {
        Pod pod = configuration.get(POD);
        final float fruitingFactor = pod.seasonalFruitProductionFactor(levelContext, treePos);
        return fruitingFactor > pod.getRequiredProductionFactor()
                && fruitingFactor > random.nextFloat()
                && random.nextFloat() <= configuration.get(PLACE_CHANCE);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (shouldGenerate(configuration, context.random())) {
            Pod pod = configuration.get(POD);
            this.place(pod, pod::placeDuringWorldGen, context.level(), context.pos(),
                    context.seasonValue(), configuration.get(BLOCKS_PER_POD), configuration.get(LOWEST_TRUNK_HEIGHT));
            return true;
        }
        return false;
    }

    private boolean shouldGenerate(GenFeatureConfiguration configuration, RandomSource random) {
        return random.nextFloat() <= configuration.get(PLACE_CHANCE);
    }

    private void place(Pod pod, PodGenerationNode.PodPlacer podPlacer, LevelAccessor level, BlockPos rootPos,
                       @Nullable Float seasonValue, int blocksPerPod, int lowestTrunkHeight) {
        TreeHelper.startAnalysisFromRoot(level, rootPos,
                new MapSignal(new PodGenerationNode(pod, podPlacer, seasonValue, blocksPerPod, rootPos, lowestTrunkHeight)));
    }

}
