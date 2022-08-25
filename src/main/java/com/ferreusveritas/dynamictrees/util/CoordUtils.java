package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.trees.Family;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class CoordUtils {

    // Used for devs to simulate tree growing in different location hashes.
    public static int coordXor = 0;

    public static final Direction[] HORIZONTALS = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};

    public enum Surround implements StringRepresentable {
        N("n", Direction.NORTH),
        NW("nw", Direction.NORTH, Direction.WEST),
        W("w", Direction.WEST),
        SW("sw", Direction.SOUTH, Direction.WEST),
        S("s", Direction.SOUTH),
        SE("se", Direction.SOUTH, Direction.EAST),
        E("e", Direction.EAST),
        NE("ne", Direction.NORTH, Direction.EAST);

        final private String name;
        final private Vec3i offset;

        Surround(String name, Direction... dirs) {
            this.name = name;
            BlockPos pos = BlockPos.ZERO;
            for (Direction d : dirs) {
                pos = pos.offset(d.getNormal());
            }
            this.offset = pos;
        }

        public String getSerializedName() {
            return name;
        }

        public Vec3i getOffset() {
            return offset;
        }

        public BlockPos getOffsetPos() {
            return new BlockPos(offset);
        }

        public Surround getOpposite() {
            return values()[(ordinal() + 4) & 7];
        }
    }

    public static boolean isSurroundedByLoadedChunks(Level world, BlockPos pos) {
        for (Surround surr : CoordUtils.Surround.values()) {
            Vec3i dir = surr.getOffset();
            if (!((ServerLevel)world).isPositionEntityTicking(pos.offset(dir))) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    public static boolean canAccessStateSafely(BlockGetter blockReader, BlockPos pos) {
        if (blockReader instanceof LevelReader) { // Handles most cases.
            return ((LevelReader) blockReader).hasChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        } else if (blockReader instanceof PathNavigationRegion) { // Handles Region.
            return !(((PathNavigationRegion) blockReader).getChunk(pos) instanceof EmptyLevelChunk);
        }
        // Otherwise assume we can access state safely. In most cases this is true, and if not we know it is a
        // mod compatibility issue and a crash or logging will be more helpful in solving the problem.
        return true;
    }

    /**
     * Gets the {@link BlockState} object at the given position, or null if the block wasn't loaded. This is safer
     * because calling getBlockState on an unloaded block can cause a crash.
     *
     * @param blockReader The {@link BlockGetter} object.
     * @return The {@link BlockState} object, or null if it's not loaded.
     */
    @Nullable
    public static BlockState getStateSafe(BlockGetter blockReader, BlockPos blockPos) {
        return canAccessStateSafely(blockReader, blockPos) ? blockReader.getBlockState(blockPos) : null;
    }

    public static Direction getRandomDir(Random rand) {
        return Direction.values()[2 + rand.nextInt(4)];//Return NSWE
    }

    /**
     * Find a suitable position for seed drops or fruit placement using ray tracing.
     *
     * @param world     The world
     * @param treePos   The block position of the {@link Family} trunk base.
     * @param branchPos The {@link BlockPos} of a {@link BranchBlock} selected as a fruit target
     * @return The {@link BlockPos} of a suitable location.  The block is always air if successful otherwise it is
     * BlockPos.ZERO
     */
    public static BlockPos getRayTraceFruitPos(LevelAccessor world, Species species, BlockPos treePos, BlockPos branchPos, SafeChunkBounds safeBounds) {
        final HitResult result = branchRayTrace(world, species, treePos, branchPos, 45, 60, 4 + world.getRandom().nextInt(3), safeBounds);

        if (result != null) {
            BlockPos hitPos = new BlockPos(result.getLocation());
            if (hitPos != BlockPos.ZERO) {
                do { // Run straight down until we hit a block that's non compatible leaves.
                    hitPos = hitPos.below();
                } while (species.getFamily().isCompatibleGenericLeaves(species, world.getBlockState(hitPos), world, hitPos));

                if (world.isEmptyBlock(hitPos)) { // If that block is air then we have a winner.
                    return hitPos;
                }
            }
        }

        return BlockPos.ZERO;
    }

    @Nullable
    public static BlockHitResult branchRayTrace(LevelAccessor world, Species species, BlockPos treePos, BlockPos branchPos, float spreadHor, float spreadVer, float distance, SafeChunkBounds safeBounds) {
        treePos = new BlockPos(treePos.getX(), branchPos.getY(), treePos.getZ()); // Make the tree pos level with the branch pos.

        Vec3 vOut = new Vec3(branchPos.getX() - treePos.getX(), 0, branchPos.getZ() - treePos.getZ());

        if (vOut.equals(Vec3.ZERO)) {
            vOut = new Vec3(1, 0, 0);
            spreadHor = 180;
        }

        final float deltaYaw = (world.getRandom().nextFloat() * spreadHor * 2) - spreadHor;
        final float deltaPitch = (world.getRandom().nextFloat() * -spreadVer); // Must be greater than -90 degrees(and less than 90) for the tangent function.
        vOut = vOut.normalize(). // Normalize to unit vector.
                add(0, Math.tan(Math.toRadians(deltaPitch)), 0). // Pitch the angle downward by 0 to spreadVer degrees.
                normalize(). // Re-normalize to unit vector.
                yRot((float) Math.toRadians(deltaYaw)). // Vary the yaw by +/- spreadHor.
                scale(distance); // Vary the view distance.

        final Vec3 branchVec = new Vec3(branchPos.getX(), branchPos.getY(), branchPos.getZ()).add(0.5, 0.5, 0.5); // Get the vector of the middle of the branch block.
        final Vec3 vantageVec = branchVec.add(vOut); // Make a vantage point to look at the branch.
        final BlockPos vantagePos = new BlockPos(vantageVec); // Convert Vector to BlockPos for testing.

        if (!safeBounds.inBounds(vantagePos, false) || world.isEmptyBlock(vantagePos)) { // The observing block must be in free space.
            final BlockHitResult result = rayTraceBlocks(world, new CustomRayTraceContext(vantageVec, branchVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE), safeBounds);
            // Beyond here should be safe since the only blocks that can possibly be hit are in loaded chunks.
            final BlockPos hitPos = new BlockPos(result.getLocation());
            if (result.getType() == HitResult.Type.BLOCK && !hitPos.equals(BlockPos.ZERO)) { // We found a block.
                if (species.getFamily().isCompatibleGenericLeaves(species, world.getBlockState(hitPos), world, hitPos)) { // Test if it's the right kind of leaves for the species.
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * I had to import Minecraft's block ray trace algorithm to make it worldgen blocksafe. I honestly don't know much
     * about what's going on in here because I haven't studied it.
     * <p>
     * If an attempt is made to read a block in an unloaded chunk it will simply return AIR or the properties of AIR
     * where applicable.
     *
     * @param world
     * @param context
     * @return
     */
    public static BlockHitResult rayTraceBlocks(LevelAccessor world, CustomRayTraceContext context, SafeChunkBounds safeBounds) {
        return getRayTraceVector(context, (fromContext, blockPos) -> {
            BlockState blockstate = safeBounds.inBounds(blockPos, false) ? world.getBlockState(blockPos) : Blocks.AIR.defaultBlockState();
            FluidState fluidState = safeBounds.inBounds(blockPos, false) ? world.getFluidState(blockPos) : Fluids.EMPTY.defaultFluidState();
            Vec3 startVec = fromContext.getStartVector();
            Vec3 endVec = fromContext.getEndVector();
            VoxelShape voxelshape = safeBounds.inBounds(blockPos, false) ? fromContext.getBlockShape(blockstate, world, blockPos) : Shapes.empty();
            BlockHitResult blockraytraceresult = world.clipWithInteractionOverride(startVec, endVec, blockPos, voxelshape, blockstate);
            VoxelShape voxelshape1 = safeBounds.inBounds(blockPos, false) ? fromContext.getFluidShape(fluidState, world, blockPos) : Shapes.empty();
            BlockHitResult blockraytraceresult1 = voxelshape1.clip(startVec, endVec, blockPos);
            double d0 = blockraytraceresult == null ? Double.MAX_VALUE : fromContext.getStartVector().distanceToSqr(blockraytraceresult.getLocation());
            double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : fromContext.getStartVector().distanceToSqr(blockraytraceresult1.getLocation());
            return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
        }, (context1) -> {
            Vec3 vec3d = context1.getStartVector().subtract(context1.getEndVector());
            return BlockHitResult.miss(context1.getEndVector(), Direction.getNearest(vec3d.x, vec3d.y, vec3d.z), new BlockPos(context1.getEndVector()));
        });
    }

    private static <T> T getRayTraceVector(CustomRayTraceContext context, BiFunction<CustomRayTraceContext, BlockPos, T> biFunction, Function<CustomRayTraceContext, T> function) {
        Vec3 startVec = context.getStartVector();
        Vec3 endVec = context.getEndVector();
        if (startVec.equals(endVec)) {
            return function.apply(context);
        } else {
            double vantX = Mth.lerp(-1.0E-7D, endVec.x, startVec.x);
            double vantY = Mth.lerp(-1.0E-7D, endVec.y, startVec.y);
            double vantZ = Mth.lerp(-1.0E-7D, endVec.z, startVec.z);
            double lookX = Mth.lerp(-1.0E-7D, startVec.x, endVec.x);
            double lookY = Mth.lerp(-1.0E-7D, startVec.y, endVec.y);
            double lookZ = Mth.lerp(-1.0E-7D, startVec.z, endVec.z);
            int i = Mth.floor(lookX);
            int j = Mth.floor(lookY);
            int k = Mth.floor(lookZ);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(i, j, k);
            T t = biFunction.apply(context, blockpos$mutableblockpos);
            if (t != null) {
                return t;
            } else {
                double d6 = vantX - lookX;
                double d7 = vantY - lookY;
                double d8 = vantZ - lookZ;
                int l = Mth.sign(d6);
                int i1 = Mth.sign(d7);
                int j1 = Mth.sign(d8);
                double d9 = l == 0 ? Double.MAX_VALUE : (double) l / d6;
                double d10 = i1 == 0 ? Double.MAX_VALUE : (double) i1 / d7;
                double d11 = j1 == 0 ? Double.MAX_VALUE : (double) j1 / d8;
                double d12 = d9 * (l > 0 ? 1.0D - Mth.frac(lookX) : Mth.frac(lookX));
                double d13 = d10 * (i1 > 0 ? 1.0D - Mth.frac(lookY) : Mth.frac(lookY));
                double d14 = d11 * (j1 > 0 ? 1.0D - Mth.frac(lookZ) : Mth.frac(lookZ));

                while (d12 <= 1.0D || d13 <= 1.0D || d14 <= 1.0D) {
                    if (d12 < d13) {
                        if (d12 < d14) {
                            i += l;
                            d12 += d9;
                        } else {
                            k += j1;
                            d14 += d11;
                        }
                    } else if (d13 < d14) {
                        j += i1;
                        d13 += d10;
                    } else {
                        k += j1;
                        d14 += d11;
                    }

                    T t1 = biFunction.apply(context, blockpos$mutableblockpos.set(i, j, k));
                    if (t1 != null) {
                        return t1;
                    }
                }

                return function.apply(context);
            }
        }
    }

    /**
     * We make a custom ray trace context since vanilla's ray trace context requires an entity (for no reason '-_-)
     */
    private static class CustomRayTraceContext {
        private final Vec3 startVec;
        private final Vec3 endVec;
        private final net.minecraft.world.level.ClipContext.Block blockMode;
        private final net.minecraft.world.level.ClipContext.Fluid fluidMode;

        public CustomRayTraceContext(Vec3 startVecIn, Vec3 endVecIn, net.minecraft.world.level.ClipContext.Block blockModeIn, net.minecraft.world.level.ClipContext.Fluid fluidModeIn) {
            this.startVec = startVecIn;
            this.endVec = endVecIn;
            this.blockMode = blockModeIn;
            this.fluidMode = fluidModeIn;
        }

        public Vec3 getEndVector() {
            return this.endVec;
        }

        public Vec3 getStartVector() {
            return this.startVec;
        }

        public VoxelShape getBlockShape(BlockState state, BlockGetter world, BlockPos pos) {
            return this.blockMode.get(state, world, pos, CollisionContext.empty());
        }

        public VoxelShape getFluidShape(FluidState state, BlockGetter world, BlockPos pos) {
            return this.fluidMode.canPick(state) ? state.getShape(world, pos) : Shapes.empty();
        }
    }

    /**
     * @param world    The world
     * @param startPos The starting position
     * @return The position of the top solid block
     */
    public static BlockPos findWorldSurface(LevelAccessor world, BlockPos startPos, boolean worldGen) {
        return new BlockPos(
                startPos.getX(),
                world.getHeight(worldGen ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.WORLD_SURFACE,
                        startPos.getX(), startPos.getZ()) - 1,
                startPos.getZ()
        );

//		BlockPos.Mutable pos = new BlockPos.Mutable(startPos.getX(), startPos.getY(), startPos.getZ());
//
//		//Rise up until we are no longer in a solid block
//		while(world.getBlockState(pos).canOcclude()) {
//			pos.set(pos.getX(), pos.getY() + 1, pos.getZ());
//		}
//		//Dive down until we are again
//		while(!world.getBlockState(pos).canOcclude() && pos.getY() > 50) {
//			pos.set(pos.getX(), pos.getY() - 1, pos.getZ());
//		}
//		return pos;
    }

    //Some ready made not terrible prime hash factors
    private static final int[][] coordHashMap = {
            {4111, 271, 3067},
            {7933711, 6144389, 9538033},
            {9973, 8287, 9721},
            {7211, 5437, 9613}
    };

    public static int coordHashCode(BlockPos pos, int a, int b, int c) {
        int hash = (pos.getX() * a ^ pos.getY() * b ^ pos.getZ() * c) >> 1;
        return (hash ^ coordXor) & 0xFFFF;
    }

    public static int coordHashCode(BlockPos pos, int readyMade) {
        int[] factors = coordHashMap[readyMade & 3];
        return coordHashCode(pos, factors[0], factors[1], factors[2]);
    }

    public static Iterable<BlockPos> goHorSides(BlockPos pos) {
        return goHorSides(pos, null);
    }

    public static Iterable<BlockPos> goHorSides(final BlockPos pos, @Nullable final Direction ignore) {
        return new Iterable<BlockPos>() {
            @Nonnull
            @Override
            public Iterator<BlockPos> iterator() {
                return new AbstractIterator<BlockPos>() {
                    private int currentDir = 0;

                    @Override
                    protected BlockPos computeNext() {
                        while (true) {
                            if (currentDir < HORIZONTALS.length) {
                                Direction face = HORIZONTALS[currentDir++];
                                if (face != ignore) {
                                    return pos.relative(face);
                                }
                            } else {
                                return this.endOfData();
                            }
                        }
                    }
                };
            }
        };
    }

}
