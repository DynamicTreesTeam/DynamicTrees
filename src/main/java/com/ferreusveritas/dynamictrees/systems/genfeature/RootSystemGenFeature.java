package com.ferreusveritas.dynamictrees.systems.genfeature;

import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;
import java.util.function.Predicate;

/**
 * Generates a column of rooted dirt beneath a tree, from the origin to the rooty block of the tree. Based on
 * how {@link net.minecraft.world.level.levelgen.feature.RootSystemFeature} does the same thing.
 */
public class RootSystemGenFeature extends GenFeature {

    public static final ConfigurationProperty<Integer> MAX_ROOT_COLUMN_HEIGHT = ConfigurationProperty.integer("max_root_column_height");
    public static final ConfigurationProperty<Integer> ROOT_RADIUS = ConfigurationProperty.integer("root_radius");
    public static final ConfigurationProperty<ResourceLocation> ROOT_REPLACEABLE_TAG = ConfigurationProperty.property("root_replaceable_tag", ResourceLocation.class);
    public static final ConfigurationProperty<Integer> ROOT_PLACEMENT_ATTEMPTS = ConfigurationProperty.integer("root_replacement_attempts");
    public static final ConfigurationProperty<Block> ROOT_BLOCK = ConfigurationProperty.block("root_block");
    public static final ConfigurationProperty<Integer> HANGING_ROOT_RADIUS = ConfigurationProperty.integer("hanging_root_radius");
    public static final ConfigurationProperty<Integer> HANGING_ROOT_VERTICAL_SPAN = ConfigurationProperty.integer("hanging_root_vertical_span");
    public static final ConfigurationProperty<Integer> HANGING_ROOT_PLACEMENT_ATTEMPTS = ConfigurationProperty.integer("hanging_root_replacement_attempts");
    public static final ConfigurationProperty<Block> HANGING_ROOT_BLOCK = ConfigurationProperty.block("hanging_root_block");

    public RootSystemGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        register(
                MAX_ROOT_COLUMN_HEIGHT, ROOT_RADIUS, ROOT_REPLACEABLE_TAG, ROOT_PLACEMENT_ATTEMPTS, ROOT_BLOCK,
                HANGING_ROOT_RADIUS, HANGING_ROOT_VERTICAL_SPAN, HANGING_ROOT_PLACEMENT_ATTEMPTS, HANGING_ROOT_BLOCK
        );
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MAX_ROOT_COLUMN_HEIGHT, 100)
                .with(ROOT_RADIUS, 3)
                .with(ROOT_REPLACEABLE_TAG, BlockTags.AZALEA_ROOT_REPLACEABLE.location())
                .with(ROOT_BLOCK, Blocks.ROOTED_DIRT)
                .with(ROOT_PLACEMENT_ATTEMPTS, 20)
                .with(HANGING_ROOT_RADIUS, 3)
                .with(HANGING_ROOT_VERTICAL_SPAN, 2)
                .with(HANGING_ROOT_BLOCK, Blocks.HANGING_ROOTS)
                .with(HANGING_ROOT_PLACEMENT_ATTEMPTS, 20);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (!context.isWorldGen()) {
            return true;
        }
        BlockPos.MutableBlockPos pos = context.originPos().mutable();
        BlockPos rootPos = context.pos();
        for (int y = pos.getY(); y < rootPos.getY(); y++) {
            placeRootedDirt(context.level(), configuration, context.random(), rootPos.getX(), rootPos.getZ(), pos);
            pos.move(Direction.UP);
        }
        placeHangingRoots(context.level(), configuration, context.random(), context.originPos(), pos);
        return true;
    }

    private static void placeRootedDirt(LevelAccessor level, GenFeatureConfiguration configuration, Random random, int x, int z, BlockPos.MutableBlockPos pos) {
        int radius = configuration.get(ROOT_RADIUS);
        TagKey<Block> rootReplaceableTag = BlockTags.create(configuration.get(ROOT_REPLACEABLE_TAG));
        Predicate<BlockState> stateReplaceable = (state) -> state.is(rootReplaceableTag);

        for (int i = 0; i < configuration.get(ROOT_PLACEMENT_ATTEMPTS); i++) {
            pos.setWithOffset(pos, random.nextInt(radius) - random.nextInt(radius), 0, random.nextInt(radius) - random.nextInt(radius));
            BlockState state = level.getBlockState(pos);
            if (stateReplaceable.test(state)) {
                level.setBlock(pos, configuration.get(ROOT_BLOCK).defaultBlockState(), 2);
            }

            pos.setX(x);
            pos.setZ(z);
        }
    }

    private static void placeHangingRoots(LevelAccessor level, GenFeatureConfiguration configuration, Random random, BlockPos originPos, BlockPos.MutableBlockPos pos) {
        int radius = configuration.get(HANGING_ROOT_RADIUS);
        int verticalSpan = configuration.get(HANGING_ROOT_VERTICAL_SPAN);

        for(int k = 0; k < configuration.get(HANGING_ROOT_PLACEMENT_ATTEMPTS); ++k) {
            pos.setWithOffset(originPos, random.nextInt(radius) - random.nextInt(radius), random.nextInt(verticalSpan) - random.nextInt(verticalSpan), random.nextInt(radius) - random.nextInt(radius));
            if (level.isEmptyBlock(pos)) {
                BlockState state = configuration.get(HANGING_ROOT_BLOCK).defaultBlockState();
                if (state.canSurvive(level, pos) && level.getBlockState(pos.above()).isFaceSturdy(level, pos, Direction.DOWN)) {
                    level.setBlock(pos, state, 2);
                }
            }
        }

    }

}
