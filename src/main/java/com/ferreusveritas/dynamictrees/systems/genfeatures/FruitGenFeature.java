package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.GeneratesFruit;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.systems.nodemappers.FindEndsNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Supplier;

@GeneratesFruit
public class FruitGenFeature extends GenFeature {

    @SuppressWarnings("unchecked")
    public static final ConfigurationProperty<Supplier<FruitBlock>> FRUIT_BLOCK = ConfigurationProperty.property("fruit_block", (Class<Supplier<FruitBlock>>) (Class) Supplier.class);

    public FruitGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(FRUIT_BLOCK, VERTICAL_SPREAD, QUANTITY, RAY_DISTANCE, FRUITING_RADIUS, PLACE_CHANCE);
    }

    @Override
    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(FRUIT_BLOCK, DTRegistries.APPLE_FRUIT)
                .with(VERTICAL_SPREAD, 30f)
                .with(QUANTITY, 4)
                .with(FRUITING_RADIUS, 8)
                .with(PLACE_CHANCE, 1f);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (!context.endPoints().isEmpty()) {
            int qty = configuration.get(QUANTITY);
            qty *= context.fruitProductionFactor();
            for (int i = 0; i < qty; i++) {
                final BlockPos endPoint = context.endPoints().get(context.random().nextInt(context.endPoints().size()));
                this.addFruit(configuration, context.world(), context.species(), context.pos().above(), endPoint, true,
                        false, context.bounds(), context.seasonValue());
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        final Level world = context.world();
        final BlockState blockState = world.getBlockState(context.treePos());
        final BranchBlock branch = TreeHelper.getBranch(blockState);

        if (branch != null && branch.getRadius(blockState) >= configuration.get(FRUITING_RADIUS) && context.natural()) {
            final BlockPos rootPos = context.pos();
            final float fruitingFactor = context.species().seasonalFruitProductionFactor(world, rootPos);

            if (fruitingFactor > configuration.get(FRUIT_BLOCK).get().getMinimumSeasonalValue() && fruitingFactor > world.random.nextFloat()) {
                final FindEndsNode endFinder = new FindEndsNode();
                TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
                final List<BlockPos> endPoints = endFinder.getEnds();
                int qty = configuration.get(QUANTITY);
                if (!endPoints.isEmpty()) {
                    for (int i = 0; i < qty; i++) {
                        final BlockPos endPoint = endPoints.get(world.random.nextInt(endPoints.size()));
                        this.addFruit(configuration, world, context.species(), rootPos.above(), endPoint, false, true,
                                SafeChunkBounds.ANY, SeasonHelper.getSeasonValue(world, rootPos));
                    }
                }
            }
        }

        return true;
    }

    protected void addFruit(GenFeatureConfiguration configuration, LevelAccessor world, Species species, BlockPos treePos, BlockPos branchPos, boolean worldGen, boolean enableHash, SafeChunkBounds safeBounds, Float seasonValue) {
        final BlockPos fruitPos = CoordUtils.getRayTraceFruitPos(world, species, treePos, branchPos, safeBounds);
        if (fruitPos != BlockPos.ZERO &&
                (!enableHash || ((CoordUtils.coordHashCode(fruitPos, 0) & 3) == 0)) &&
                world.getRandom().nextFloat() <= configuration.get(PLACE_CHANCE)) {
            FruitBlock fruitBlock = configuration.get(FRUIT_BLOCK).get();
            BlockState setState = fruitBlock.getStateForAge(worldGen ? fruitBlock.getAgeForSeasonalWorldGen(world, fruitPos, seasonValue) : 0);
            world.setBlock(fruitPos, setState, 3);
        }
    }

}
