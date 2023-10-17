package com.ferreusveritas.dynamictrees.systems.genfeature;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.block.branch.SurfaceRootBlock;
import com.ferreusveritas.dynamictrees.block.branch.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import com.ferreusveritas.dynamictrees.util.function.TetraFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RootsGenFeature extends GenFeature {

    public static final ConfigurationProperty<Integer> MIN_TRUNK_RADIUS = ConfigurationProperty.integer("min_trunk_radius");
    public static final ConfigurationProperty<Integer> LEVEL_LIMIT = ConfigurationProperty.integer("level_limit");
    public static final ConfigurationProperty<Float> SCALE_FACTOR = ConfigurationProperty.floatProperty("scale_factor");

    private TetraFunction<Integer, Integer, Integer, Float, Integer> scaler = (inRadius, trunkRadius, minTrunkRadius, scaleFactor) -> {
        float scale = Mth.clamp(trunkRadius >= minTrunkRadius ? (trunkRadius / scaleFactor) : 0, 0, 1);
        return (int) (inRadius * scale);
    };

    private final SimpleVoxmap[] rootMaps;

    public RootsGenFeature(ResourceLocation registryName) {
        super(registryName);

        this.rootMaps = createRootMaps();
    }

    @Override
    protected void registerProperties() {
        this.register(MIN_TRUNK_RADIUS, LEVEL_LIMIT, SCALE_FACTOR);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(MIN_TRUNK_RADIUS, 13)
                .with(LEVEL_LIMIT, 2)
                .with(SCALE_FACTOR, 24f);
    }

    protected SimpleVoxmap[] createRootMaps() {
        //These are basically bitmaps of the root structures
        byte[][] rootData = new byte[][]{
                {0, 3, 0, 0, 0, 0, 0, 0, 5, 6, 7, 0, 3, 2, 0, 0, 0, 8, 0, 5, 0, 0, 6, 8, 0, 8, 7, 0, 0, 0, 0, 7, 0, 0, 0, 0, 3, 4, 6, 4, 0, 0, 0, 2, 0, 0, 3, 2, 1},
                {0, 3, 0, 0, 0, 0, 0, 0, 5, 6, 7, 0, 3, 2, 0, 0, 0, 8, 0, 5, 0, 0, 6, 8, 0, 8, 7, 0, 0, 0, 0, 7, 0, 0, 0, 0, 3, 4, 6, 4, 0, 0, 0, 2, 0, 0, 3, 2, 1},
                {0, 0, 2, 0, 0, 0, 0, 3, 4, 6, 0, 0, 0, 0, 1, 0, 7, 8, 0, 0, 0, 0, 0, 0, 0, 7, 6, 0, 0, 0, 0, 8, 0, 5, 4, 0, 5, 6, 7, 0, 0, 2, 2, 4, 0, 0, 0, 0, 0},
                {0, 4, 0, 0, 0, 0, 0, 0, 5, 6, 0, 0, 1, 0, 0, 0, 7, 0, 0, 3, 0, 0, 0, 8, 0, 8, 7, 0, 0, 0, 0, 8, 0, 5, 4, 0, 0, 6, 7, 3, 0, 2, 0, 4, 5, 0, 0, 0, 0},
                {3, 4, 5, 0, 0, 0, 0, 2, 0, 6, 0, 0, 0, 0, 0, 0, 7, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 7, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 0, 2, 3, 5, 2, 0},
                {0, 0, 4, 0, 0, 0, 0, 0, 0, 6, 7, 0, 2, 0, 0, 0, 0, 8, 0, 3, 0, 5, 7, 8, 0, 6, 5, 0, 3, 0, 0, 8, 0, 2, 1, 0, 3, 0, 7, 0, 0, 0, 0, 4, 5, 6, 0, 0, 0}
        };

        SimpleVoxmap[] maps = new SimpleVoxmap[rootData.length];

        for (int i = 0; i < maps.length; i++) {
            maps[i] = new SimpleVoxmap(7, 1, 7, rootData[i]).setCenter(new BlockPos(3, 0, 3));
        }

        return maps;
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        final BlockPos treePos = context.pos().above();
        final int trunkRadius = TreeHelper.getRadius(context.level(), treePos);
        return trunkRadius >= configuration.get(MIN_TRUNK_RADIUS) &&
                this.startRoots(configuration, context.level(), treePos, context.species(), trunkRadius);
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        final LevelAccessor level = context.level();
        final BlockPos treePos = context.treePos();
        final int trunkRadius = TreeHelper.getRadius(level, treePos);

        if (context.fertility() > 0 && trunkRadius >= configuration.get(MIN_TRUNK_RADIUS)) {
            final Surround surr = Surround.values()[level.getRandom().nextInt(8)];
            final BlockPos dPos = treePos.offset(surr.getOffset());
            if (level.getBlockState(dPos).getBlock() instanceof SurfaceRootBlock) {
                level.setBlock(dPos, DTRegistries.TRUNK_SHELL.get().defaultBlockState().setValue(TrunkShellBlock.CORE_DIR, surr.getOpposite()), Block.UPDATE_ALL);
            }

            this.startRoots(configuration, level, treePos, context.species(), trunkRadius);
        }

        return true;
    }

    public boolean startRoots(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos treePos, Species species, int trunkRadius) {
        int hash = CoordUtils.coordHashCode(treePos, 2);
        SimpleVoxmap rootMap = rootMaps[hash % rootMaps.length];
        this.nextRoot(level, rootMap, treePos, species, trunkRadius, configuration.get(MIN_TRUNK_RADIUS), configuration.get(SCALE_FACTOR), BlockPos.ZERO, 0,
                -1, null, 0, configuration.get(LEVEL_LIMIT));
        return true;
    }

    protected void nextRoot(LevelAccessor level, SimpleVoxmap rootMap, BlockPos trunkPos, Species species, int trunkRadius, int minTrunkRadius, float scaleFactor, BlockPos pos, int height, int levelCount, Direction fromDir, int radius, int levelLimit) {

        for (int depth = 0; depth < 2; depth++) {
            BlockPos currPos = trunkPos.offset(pos).above(height - depth);
            BlockState placeState = level.getBlockState(currPos);
            BlockState belowState = level.getBlockState(currPos.below());

            boolean onNormalCube = belowState.isRedstoneConductor(level, currPos.below());

            if (pos == BlockPos.ZERO || isReplaceableWithRoots(level, placeState, currPos) && (depth == 1 || onNormalCube)) {
                if (radius > 0) {
                    species.getFamily().getSurfaceRoot().ifPresent(root ->
                            root.setRadius(level, currPos, radius, 3)
                    );
                }
                if (onNormalCube) {
                    for (Direction dir : CoordUtils.HORIZONTALS) {
                        if (dir != fromDir) {
                            BlockPos dPos = pos.relative(dir);
                            int nextRad = this.scaler.apply((int) rootMap.getVoxel(dPos), trunkRadius, minTrunkRadius, scaleFactor);
                            if (pos != BlockPos.ZERO && nextRad >= radius) {
                                nextRad = radius - 1;
                            }
                            int thisLevelCount = depth == 1 ? 1 : levelCount + 1;
                            if (nextRad > 0 && thisLevelCount <= levelLimit) {//Don't go longer than 2 adjacent blocks on a single level
                                nextRoot(level, rootMap, trunkPos, species, trunkRadius, minTrunkRadius, scaleFactor, dPos, height - depth, thisLevelCount, dir.getOpposite(), nextRad, levelLimit);//Recurse here
                            }
                        }
                    }
                }
                break;
            }
        }

    }

    protected boolean isReplaceableWithRoots(LevelAccessor level, BlockState placeState, BlockPos pos) {
        if (level.isEmptyBlock(pos) || placeState.getBlock() instanceof TrunkShellBlock) {
            return true;
        }

        return placeState.canBeReplaced() && !placeState.getFluidState().is(FluidTags.LAVA);
    }

    public RootsGenFeature setScaler(TetraFunction<Integer, Integer, Integer, Float, Integer> scaler) {
        this.scaler = scaler;
        return this;
    }

}
