package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.systems.nodemappers.FindEndsNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

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
                .with(FRUIT, Fruit.NULL_FRUIT)
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
                this.placeDuringWorldGen(configuration, context.species(), context.world(), context.pos().above(),
                        endPoint, context.bounds(), context.seasonValue());
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        final World world = context.world();
        final BlockState blockState = world.getBlockState(context.treePos());
        final BranchBlock branch = TreeHelper.getBranch(blockState);
        final Fruit fruit = configuration.get(FRUIT);

        if (branch != null && branch.getRadius(blockState) >= configuration.get(FRUITING_RADIUS) && context.natural()) {
            final BlockPos rootPos = context.pos();
            final float fruitingFactor = fruit.seasonalFruitProductionFactor(world, rootPos);

            if (fruitingFactor > fruit.getMinProductionFactor() && fruitingFactor > world.random.nextFloat()) {
                final FindEndsNode endFinder = new FindEndsNode();
                TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
                final List<BlockPos> endPoints = endFinder.getEnds();
                int qty = configuration.get(QUANTITY);
                if (!endPoints.isEmpty()) {
                    for (int i = 0; i < qty; i++) {
                        final BlockPos endPoint = endPoints.get(world.random.nextInt(endPoints.size()));
                        this.place(configuration, context.species(), world, rootPos.above(), endPoint,
                                SeasonHelper.getSeasonValue(world, rootPos));
                    }
                }
            }
        }

        return true;
    }

    protected void place(GenFeatureConfiguration configuration, Species species, IWorld world, BlockPos treePos,
                         BlockPos branchPos, Float seasonValue) {
        final BlockPos fruitPos =
                CoordUtils.getRayTraceFruitPos(world, species, treePos, branchPos, SafeChunkBounds.ANY);
        if (shouldPlace(configuration, world, fruitPos)) {
            configuration.get(FRUIT).place(world, fruitPos, seasonValue);
        }
    }

    protected boolean shouldPlace(GenFeatureConfiguration configuration, IWorld world, BlockPos pos) {
        return pos != BlockPos.ZERO &&
                (CoordUtils.coordHashCode(pos, 0) & 3) == 0 &&
                world.getRandom().nextFloat() <= configuration.get(PLACE_CHANCE);
    }

    protected void placeDuringWorldGen(GenFeatureConfiguration configuration, Species species, IWorld world,
                                       BlockPos treePos, BlockPos branchPos, SafeChunkBounds bounds,
                                       Float seasonValue) {
        final BlockPos fruitPos = CoordUtils.getRayTraceFruitPos(world, species, treePos, branchPos, bounds);
        if (shouldPlaceDuringWorldGen(configuration, world, fruitPos)) {
            configuration.get(FRUIT).placeDuringWorldGen(world, fruitPos, seasonValue);
        }
    }

    protected boolean shouldPlaceDuringWorldGen(GenFeatureConfiguration configuration, IWorld world, BlockPos pos) {
        return pos != BlockPos.ZERO && world.getRandom().nextFloat() <= configuration.get(PLACE_CHANCE);
    }

}
