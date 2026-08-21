package com.dtteam.dynamictrees.systems.genfeature;

import com.dtteam.dynamictrees.api.configuration.ConfigurationProperty;
import com.dtteam.dynamictrees.api.function.TetraFunction;
import com.dtteam.dynamictrees.api.voxmap.SimpleVoxmap;
import com.dtteam.dynamictrees.block.branch.SurfaceRootBlock;
import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.CoordUtils;
import com.dtteam.dynamictrees.utility.CoordUtils.Surround;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class RootsGenFeature extends GenFeature {

    public static final ConfigurationProperty<Integer> MIN_TRUNK_RADIUS = ConfigurationProperty.integer("min_trunk_radius");
    public static final ConfigurationProperty<Integer> LEVEL_LIMIT = ConfigurationProperty.integer("level_limit");
    public static final ConfigurationProperty<Float> SCALE_FACTOR = ConfigurationProperty.floatProperty("scale_factor");

    private final TetraFunction<Integer, Integer, Integer, Float, Integer> scaler = (inRadius, trunkRadius, minTrunkRadius, scaleFactor) -> {
        float scale = Mth.clamp(trunkRadius >= minTrunkRadius ? (trunkRadius / scaleFactor) : 0, 0, 1);
        return (int) (inRadius * scale);
    };

    private final SimpleVoxmap[] rootMaps;

    public RootsGenFeature(Identifier registryName) {
        super(registryName);

        this.rootMaps = createRootMaps();
    }

    protected void registerProperties() {
        this.register(MIN_TRUNK_RADIUS, LEVEL_LIMIT, SCALE_FACTOR);
    }

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

    private int getMinTrunkRadius(GenFeatureConfiguration configuration){
        return Math.max(8, configuration.get(MIN_TRUNK_RADIUS));
    }

    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        final BlockPos treePos = context.pos().above();
        final int trunkRadius = TreeHelper.getRadius(context.level(), treePos);
        return trunkRadius >= getMinTrunkRadius(configuration) &&
                this.startRoots(configuration, context.level(), treePos, context.species(), trunkRadius);
    }

    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        final LevelAccessor level = context.level();
        final BlockPos treePos = context.treePos();
        final int trunkRadius = TreeHelper.getRadius(level, treePos);

        if (context.fertility() > 0 && trunkRadius >= getMinTrunkRadius(configuration)) {
            this.startRoots(configuration, level, treePos, context.species(), trunkRadius);
        }

        return true;
    }

    public boolean startRoots(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos treePos, Species species, int trunkRadius) {
        int hash = CoordUtils.coordHashCode(treePos, 2);
        SimpleVoxmap rootMap = rootMaps[hash % rootMaps.length];
        this.nextRoot(level, rootMap, treePos, species, trunkRadius, getMinTrunkRadius(configuration), configuration.get(SCALE_FACTOR), BlockPos.ZERO
                , 0, -1, null, 0, configuration.get(LEVEL_LIMIT));
        return true;
    }

    protected void nextRoot(LevelAccessor level, SimpleVoxmap rootMap, BlockPos trunkPos, Species species, int trunkRadius, int minTrunkRadius, float scaleFactor, BlockPos relativePos, int height, int levelCount, Direction fromDir, int radius, int levelLimit) {
        for (int depth = 0; depth < 2; depth++) {
            BlockPos currPos = trunkPos.offset(relativePos).above(height - depth);
            BlockState placeState = level.getBlockState(currPos);
            BlockState belowState = level.getBlockState(currPos.below());

            boolean onNormalCube = belowState.isFaceSturdy(level, currPos.below(), Direction.UP);
            boolean isStructurallySound = depth == 1 || onNormalCube;
            if (relativePos == BlockPos.ZERO || isReplaceableWithRoots(level, placeState, currPos) && isStructurallySound) {
                if (radius > 0) {
                    species.getFamily().getSurfaceRoot().ifPresent(root ->
                            root.setRadius(level, currPos, radius, 3)
                    );
                }
                if (onNormalCube) {
                    for (Direction dir : CoordUtils.HORIZONTALS) {
                        if (dir != fromDir) {
                            BlockPos dRelativePos = relativePos.relative(dir);
                            int nextRad = this.scaler.apply((int) rootMap.getVoxel(dRelativePos), trunkRadius, minTrunkRadius, scaleFactor);
                            if (relativePos != BlockPos.ZERO && nextRad >= radius) {
                                nextRad = radius - 1;
                            }
                            int thisLevelCount = depth == 1 ? 1 : levelCount + 1;
                            if (nextRad > 0 && thisLevelCount <= levelLimit) {//Don't go longer than 2 adjacent blocks on a single level
                                nextRoot(level, rootMap, trunkPos, species, trunkRadius, minTrunkRadius, scaleFactor, dRelativePos, height - depth, thisLevelCount, dir.getOpposite(), nextRad, levelLimit);//Recurse here
                            }
                        }
                    }
                }
                break;
            }
        }

    }

    protected boolean isReplaceableWithRoots(LevelAccessor level, BlockState placeState, BlockPos pos) {
        return level.isEmptyBlock(pos)
                || placeState.getBlock() instanceof TrunkShellBlock
                || placeState.is(DTBlockTags.FOLIAGE)
                || placeState.canBeReplaced() && !placeState.getFluidState().is(FluidTags.LAVA);
    }

}
