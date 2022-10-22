package com.ferreusveritas.dynamictrees.systems.genfeature;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.compat.season.SeasonHelper;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.systems.nodemapper.FindEndsNode;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FruitGenFeature extends GenFeature {

    public static final ConfigurationProperty<Fruit> FRUIT = ConfigurationProperty.property("fruit", Fruit.class);

    public FruitGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(FRUIT, VERTICAL_SPREAD, QUANTITY, RAY_DISTANCE, FRUITING_RADIUS, PLACE_CHANCE);
    }

    @Override
    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(FRUIT, Fruit.NULL)
                .with(VERTICAL_SPREAD, 30f)
                .with(QUANTITY, 4)
                .with(FRUITING_RADIUS, 8)
                .with(PLACE_CHANCE, 1f);
    }

    @Override
    public boolean shouldApply(Species species, GenFeatureConfiguration configuration) {
        return species.hasFruit(configuration.get(FRUIT));
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (!context.endPoints().isEmpty()) {
            int qty = configuration.get(QUANTITY);
            qty *= context.fruitProductionFactor();
            for (int i = 0; i < qty; i++) {
                final BlockPos endPoint = context.endPoints().get(context.random().nextInt(context.endPoints().size()));
                this.placeDuringWorldGen(configuration, context.species(), context.level(), context.pos().above(),
                        endPoint, context.bounds(), context.seasonValue());
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        final LevelAccessor level = context.level();
        final BlockState blockState = level.getBlockState(context.treePos());
        final BranchBlock branch = TreeHelper.getBranch(blockState);
        final Fruit fruit = configuration.get(FRUIT);

        if (branch != null && branch.getRadius(blockState) >= configuration.get(FRUITING_RADIUS) && context.natural()) {
            final BlockPos rootPos = context.pos();
            final float fruitingFactor = fruit.seasonalFruitProductionFactor(context.levelContext(), rootPos);

            if (fruitingFactor > fruit.getMinProductionFactor() && fruitingFactor > level.getRandom().nextFloat()) {
                final FindEndsNode endFinder = new FindEndsNode();
                TreeHelper.startAnalysisFromRoot(level, rootPos, new MapSignal(endFinder));
                final List<BlockPos> endPoints = endFinder.getEnds();
                int qty = configuration.get(QUANTITY);
                if (!endPoints.isEmpty()) {
                    for (int i = 0; i < qty; i++) {
                        final BlockPos endPoint = endPoints.get(level.getRandom().nextInt(endPoints.size()));
                        this.place(configuration, context.species(), level, rootPos.above(), endPoint,
                                SeasonHelper.getSeasonValue(context.levelContext(), rootPos));
                    }
                }
            }
        }

        return true;
    }

    protected void place(GenFeatureConfiguration configuration, Species species, LevelAccessor level, BlockPos treePos,
                         BlockPos branchPos, Float seasonValue) {
        final BlockPos fruitPos =
                CoordUtils.getRayTraceFruitPos(level, species, treePos, branchPos, SafeChunkBounds.ANY);
        if (shouldPlace(configuration, level, fruitPos)) {
            configuration.get(FRUIT).place(level, fruitPos, seasonValue);
        }
    }

    protected boolean shouldPlace(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos pos) {
        return pos != BlockPos.ZERO &&
                (CoordUtils.coordHashCode(pos, 0) & 3) == 0 &&
                level.getRandom().nextFloat() <= configuration.get(PLACE_CHANCE);
    }

    protected void placeDuringWorldGen(GenFeatureConfiguration configuration, Species species, LevelAccessor level,
                                       BlockPos treePos, BlockPos branchPos, SafeChunkBounds bounds,
                                       Float seasonValue) {
        final BlockPos fruitPos = CoordUtils.getRayTraceFruitPos(level, species, treePos, branchPos, bounds);
        if (shouldPlaceDuringWorldGen(configuration, level, fruitPos)) {
            configuration.get(FRUIT).placeDuringWorldGen(level, fruitPos, seasonValue);
        }
    }

    protected boolean shouldPlaceDuringWorldGen(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos pos) {
        return pos != BlockPos.ZERO && level.getRandom().nextFloat() <= configuration.get(PLACE_CHANCE);
    }

}
