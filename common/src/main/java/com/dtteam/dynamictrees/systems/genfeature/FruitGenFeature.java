package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.api.network.MapSignal;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.fruit.Fruit;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.dtteam.dynamictrees.systems.nodemapper.FindEndsNode;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.CoordUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FruitGenFeature extends GenFeature {

    public static final ConfigurationProperty<Fruit> FRUIT = ConfigurationProperty.property("fruit", Fruit.class);

    public FruitGenFeature(Identifier registryName) {
        super(registryName);
    }

    protected void registerProperties() {
        this.register(FRUIT, VERTICAL_SPREAD, QUANTITY, RAY_DISTANCE, FRUITING_RADIUS, PLACE_CHANCE);
    }

    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(FRUIT, Fruit.NULL)
                .with(VERTICAL_SPREAD, 30f)
                .with(QUANTITY, 4)
                .with(FRUITING_RADIUS, 8)
                .with(PLACE_CHANCE, 1f);
    }

    public boolean shouldApply(Species species, GenFeatureConfiguration configuration) {
        return species.hasFruit(configuration.get(FRUIT));
    }

    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (!context.endPoints().isEmpty()) {
            int qty = configuration.get(QUANTITY);
            qty *= context.fruitProductionFactor();
            for (int i = 0; i < qty; i++) {
                final BlockPos endPoint = context.endPoints().get(context.random().nextInt(context.endPoints().size()));
                this.placeDuringWorldGen(configuration, context.species(), context.level(), context.pos().above(),
                        endPoint, context.isWorldGen(), context.seasonValue());
            }
            return true;
        }
        return false;
    }

    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        final LevelAccessor level = context.level();
        final BlockState blockState = level.getBlockState(context.treePos());
        final BranchBlock branch = TreeHelper.getBranch(blockState);
        final Fruit fruit = configuration.get(FRUIT);

        if (branch != null && branch.getRadius(blockState) >= configuration.get(FRUITING_RADIUS) && context.natural()) {
            final BlockPos rootPos = context.pos();
            final float fruitingFactor = fruit.seasonalFruitProductionFactor(context.levelContext(), rootPos);

            if (fruitingFactor > fruit.getRequiredProductionFactor() && fruitingFactor > level.getRandom().nextFloat()) {
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
                CoordUtils.getRayTraceFruitPos(level, species, treePos, branchPos, false);
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
                                       BlockPos treePos, BlockPos branchPos, boolean worldGen,
                                       Float seasonValue) {
        final BlockPos fruitPos = CoordUtils.getRayTraceFruitPos(level, species, treePos, branchPos, worldGen);
        if (shouldPlaceDuringWorldGen(configuration, level, fruitPos)) {
            configuration.get(FRUIT).placeDuringWorldGen(level, fruitPos, seasonValue);
        }
    }

    protected boolean shouldPlaceDuringWorldGen(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos pos) {
        return pos != BlockPos.ZERO && level.getRandom().nextFloat() <= configuration.get(PLACE_CHANCE);
    }

}
