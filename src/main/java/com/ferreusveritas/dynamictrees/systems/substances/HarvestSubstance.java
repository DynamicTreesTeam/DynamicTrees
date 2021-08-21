package com.ferreusveritas.dynamictrees.systems.substances;

import com.ferreusveritas.dynamictrees.api.GeneratesFruit;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.blocks.FruitBlock;
import com.ferreusveritas.dynamictrees.blocks.rootyblocks.RootyBlock;
import com.ferreusveritas.dynamictrees.init.DTClient;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.systems.nodemappers.FindEndsNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.Set;

/**
 * @author Harley O'Connor
 */
public class HarvestSubstance implements ISubstanceEffect {

    private Species species = Species.NULL_SPECIES;
    private final int duration;
    private final int ticksPerParticlePulse;
    private final int ticksPerGrowthPulse;
    private final int growthPulses;
    private final int ticksPerSpawnAttempt;

    public final Set<BlockPos> fruitPositions = Sets.newHashSet();
    private final Set<FruitBlock> compatibleFruitBlocks = Sets.newHashSet();

    public HarvestSubstance() {
        this(1600, 12, 12, 1, 16);
    }

    public HarvestSubstance(int duration, int ticksPerParticlePulse, int ticksPerGrowthPulse, int growthPulses, int ticksPerSpawnAttempt) {
        this.duration = duration;
        this.ticksPerParticlePulse = ticksPerParticlePulse;
        this.ticksPerGrowthPulse = ticksPerGrowthPulse;
        this.growthPulses = growthPulses;
        this.ticksPerSpawnAttempt = ticksPerSpawnAttempt;
    }

    @Override
    public boolean apply(World world, BlockPos rootPos) {
        final BlockState rootState = world.getBlockState(rootPos);
        final RootyBlock rootyBlock = TreeHelper.getRooty(rootState);

        if (rootyBlock == null) {
            return false;
        }

        this.species = rootyBlock.getSpecies(rootState, world, rootPos);
        this.compatibleFruitBlocks.addAll(FruitBlock.getFruitBlocksForSpecies(species));

        // If the species is invalid or doesn't have any compatible fruit, don't apply substance.
        if (!this.species.isValid() || this.compatibleFruitBlocks.size() < 1) {
            return false;
        }

        this.recalculateFruitPositions(world, rootPos, rootyBlock);

        return true;
    }

    private void recalculateFruitPositions(final IWorld world, final BlockPos rootPos, final RootyBlock rootyBlock) {
        this.fruitPositions.clear();

        final FindEndsNode findEndsNode = new FindEndsNode();
        rootyBlock.startAnalysis(world, rootPos, new MapSignal(findEndsNode));

        findEndsNode.getEnds().forEach(endPos ->
                BlockPos.betweenClosedStream(endPos.offset(-3, -3, -3), endPos.offset(3, 3, 3)).forEach(pos -> {
                    final BlockState state = world.getBlockState(pos);
                    final Block block = state.getBlock();

                    if (block instanceof FruitBlock && this.compatibleFruitBlocks.contains(block)) {
                        this.fruitPositions.add(pos.immutable());
                    }
                })
        );
    }

    @Override
    public boolean update(World world, BlockPos rootPos, int deltaTicks, int fertility) {
        if (deltaTicks > this.duration) {
            return false;
        }

        final RootyBlock rootyBlock = TreeHelper.getRooty(world.getBlockState(rootPos));

        if (rootyBlock == null) {
            return false;
        }

        if (world.isClientSide) {
            if (deltaTicks % this.ticksPerParticlePulse == 0) {
                // Recalculate fruit positions every time in case new fruit spawned.
                this.recalculateFruitPositions(world, rootPos, rootyBlock);

                this.fruitPositions.forEach(fruitPos ->
                        DTClient.spawnParticles(world, ParticleTypes.EFFECT, fruitPos.getX(), fruitPos.getY(), fruitPos.getZ(), 3, world.getRandom())
                );
            }
        } else {
            final boolean growPulse = deltaTicks % this.ticksPerGrowthPulse == 0;
            final boolean spawnAttempt = deltaTicks % this.ticksPerSpawnAttempt == 0;

            // Only recalculate fruit positions if necessary, and don't do it twice.
            if (growPulse || spawnAttempt) {
                this.recalculateFruitPositions(world, rootPos, rootyBlock);
            }

            if (growPulse) {
                this.fruitPositions.removeIf(fruitPos -> {
                    final BlockState state = world.getBlockState(fruitPos);
                    final Block block = state.getBlock();

                    if (!(block instanceof FruitBlock) || !this.compatibleFruitBlocks.contains(block)) {
                        return true;
                    }

                    // Force tick for each fruit block - effectively multiplies growth speed.
                    for (int i = 0; i < this.growthPulses; i++) {
                        ((FruitBlock) block).doTick(state, world, fruitPos, world.random);
                    }
                    return false;
                });
            }

            // Force a growth attempt of all fruit gen features.
            if (spawnAttempt) {
                this.species.getGenFeatures().stream()
                        .filter(configuration -> configuration.getGenFeature().getClass().isAnnotationPresent(GeneratesFruit.class))
                        .forEach(configuration -> configuration.getGenFeature().generate(
                                configuration,
                                GenFeature.Type.POST_GROW,
                                new PostGrowContext(world, rootPos, species, rootPos.relative(rootyBlock.getTrunkDirection(world, rootPos)), fertility, true)
                        ));
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return "harvest";
    }

    @Override
    public boolean isLingering() {
        return true;
    }

}
