package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.block.branch.BasicBranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.BasicRootsBlock;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeatureConfiguration;
import com.ferreusveritas.dynamictrees.systems.genfeature.MushroomRotGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeature.RotSoilGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.GenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostRotContext;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import org.slf4j.Logger;

/**
 * Detects rapid-rot recursion that revisits a position without any successful rot write in between.
 * Failed WorldGenRegion writes can leave neighbouring branches intact forever. The guard is limited
 * to that exact implementation, whose generation write boundary is fixed during the cascade. Successful writes
 * allow revisiting a position, preserving cascades that make progress. Unknown branch/species/feature
 * implementations retain their original behavior because their side effects cannot be inferred here.
 */
public final class RotCycleGuard {
    private static final AtomicLong CUTS = new AtomicLong();
    private static final AtomicLong FOREIGN = new AtomicLong();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile boolean announced, warned;
    private static final ThreadLocal<Cascade> CASCADE = ThreadLocal.withInitial(Cascade::new);

    private RotCycleGuard() {
    }

    static final class Cascade {
        long writes;                      // successful Dynamic Trees rot writes on this thread so far (only ever grows)
        int depth;
        Object level, block, species, rand;
        int fertility;
        long[] pos = new long[32];
        int[] radius = new int[32];
        long[] start = new long[32];
    }

    public static boolean cut(Object block, Species species, LevelAccessor level, BlockPos pos, int radius, int fertility, RandomSource rand,
                              boolean rotRapid, boolean roots) {
        // Branches pass
        // their own rapid flag to Species.rot; roots always pass true and are recorded either way (a root call that is not
        // rapid never recurses, so its record is only ever the bottom one and is dropped at its return).
        return rotRapid && level.getClass() == WorldGenRegion.class
                && cutOrRecord(block, level, pos, species, fertility, radius, rand, roots);
    }

    static boolean cutOrRecord(Object block, LevelAccessor level, BlockPos pos, Species species, int fertility, int radius,
                               RandomSource rand, boolean roots) {
        Cascade c = CASCADE.get();
        int d = c.depth;
        long p = pos.asLong();
        if (d > 0 && !(c.level == level && c.block == block && c.species == species && c.fertility == fertility && c.rand == rand
                && adjacent(c.pos[d - 1], p))) {
            // Not the recursion of the record on top (a call from elsewhere): start afresh.
            // Forgetting records only means fewer cuts, never a wrong one.
            d = 0;
            c.depth = 0;
        }
        if (d == 0) {
            c.level = level;
            c.block = block;
            c.species = species;
            c.fertility = fertility;
            c.rand = rand;
        } else {
            for (int i = d - 1; i >= 0; i--) {
                if (c.pos[i] != p) {
                    continue;
                }
                // the newest record for this position has seen the fewest writes since
                if (c.radius[i] == radius && c.start[i] == c.writes) {
                    if ((roots || fertility <= 0) && ownCode(block, species)) {
                        announceCut(pos);
                        return true;
                    }
                    foreign(pos, block, species);
                }
                break;
            }
        }
        if (d == c.pos.length) {
            c.pos = java.util.Arrays.copyOf(c.pos, d * 2);
            c.radius = java.util.Arrays.copyOf(c.radius, d * 2);
            c.start = java.util.Arrays.copyOf(c.start, d * 2);
        }
        c.pos[d] = p;
        c.radius[d] = radius;
        c.start[d] = c.writes;
        c.depth = d + 1;
        return false;
    }

    public static void exit(Object block, LevelAccessor level, BlockPos pos) {
        Cascade c = CASCADE.get();
        int d = c.depth;
        if (d > 0 && c.pos[d - 1] == pos.asLong() && c.level == level && c.block == block) {
            c.depth = --d;
            if (d == 0) {
                c.level = c.block = c.species = c.rand = null;
            }
        }
    }

    public static boolean wrote(boolean success) {
        if (success) {
            CASCADE.get().writes++;
        }
        return success;
    }

    private static boolean adjacent(long a, long b) {
        int dx = Math.abs(BlockPos.getX(a) - BlockPos.getX(b));
        int dy = Math.abs(BlockPos.getY(a) - BlockPos.getY(b));
        int dz = Math.abs(BlockPos.getZ(a) - BlockPos.getZ(b));
        return dx + dy + dz == 1;
    }

    private static void announceCut(BlockPos pos) {
        long n = CUTS.incrementAndGet();
        if (!announced) {
            announced = true;
            LOGGER.info("Dynamic Trees: stopped an endless rapid-rot loop at {} (a branch the "
                    + "generation region does not let Dynamic Trees remove); later stops are logged at DEBUG", pos);
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Dynamic Trees: stopped rapid-rot loop #{} at {}", n, pos);
        }
    }

    private static void foreign(BlockPos pos, Object block, Species species) {
        FOREIGN.incrementAndGet();
        if (!warned) {
            warned = true;
            LOGGER.warn("Dynamic Trees: the rapid-rot loop at {} ({} / {}) runs code that does not come "
                    + "with Dynamic Trees (or a positive fertility); it is left to the original code", pos,
                    block.getClass().getName(), species.getClass().getName());
        }
    }

    // ---- Is the code between two calls Dynamic Trees's own? (once per class) ----

    private static final ClassValue<Boolean> BLOCK_OK = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> c) {
            Class<?> check = declarer(c, "checkForRot", LevelAccessor.class, BlockPos.class, Species.class, int.class, int.class,
                    RandomSource.class, float.class, boolean.class);
            return (check == BasicBranchBlock.class || check == BasicRootsBlock.class)
                    && declarer(c, "rot", LevelAccessor.class, BlockPos.class) == BranchBlock.class
                    && declarer(c, "breakDeliberate", LevelAccessor.class, BlockPos.class,
                    com.ferreusveritas.dynamictrees.DynamicTrees.DestroyMode.class) == BranchBlock.class;
        }
    };
    private static final ClassValue<Boolean> SPECIES_OK = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> c) {
            return declarer(c, "rot", LevelAccessor.class, BlockPos.class, int.class, int.class, int.class, RandomSource.class,
                    boolean.class, boolean.class) == Species.class
                    && declarer(c, "postRot", PostRotContext.class) == Species.class
                    && declarer(c, "getGenFeatures") == Species.class;
        }
    };
    private static final Set<Class<?>> OWN_POST_ROT = Set.of(GenFeature.class, MushroomRotGenFeature.class, RotSoilGenFeature.class);
    private static final ClassValue<Boolean> FEATURE_OK = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> c) {
            return declarer(c, "generate", GenFeatureConfiguration.class, GenFeature.Type.class, GenerationContext.class) == GenFeature.class
                    && OWN_POST_ROT.contains(declarer(c, "postRot", GenFeatureConfiguration.class, PostRotContext.class));
        }
    };

    static boolean ownCode(Object block, Species species) {
        if (!BLOCK_OK.get(block.getClass()) || !SPECIES_OK.get(species.getClass())) {
            return false;
        }
        List<GenFeatureConfiguration> features = species.getGenFeatures();
        for (int i = 0; i < features.size(); i++) {
            GenFeatureConfiguration cfg = features.get(i);
            if (cfg.getClass() != GenFeatureConfiguration.class || !FEATURE_OK.get(cfg.getGenFeature().getClass())) {
                return false;
            }
        }
        return true;
    }

    static Class<?> declarer(Class<?> c, String name, Class<?>... params) {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            try {
                Method m = k.getDeclaredMethod(name, params);
                return m.getDeclaringClass();
            } catch (NoSuchMethodException e) {
                // keep looking in the superclass
            }
        }
        return null;
    }
}
