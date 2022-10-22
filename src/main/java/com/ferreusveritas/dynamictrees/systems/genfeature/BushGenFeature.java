package com.ferreusveritas.dynamictrees.systems.genfeature;

import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.cell.LeafClusters;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.FullGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Random;

public class BushGenFeature extends GenFeature {

    /**
     * Defines the logs {@link Block} for the bush. Defaults to {@link Blocks#OAK_LOG}.
     */
    public static final ConfigurationProperty<Block> LOG = ConfigurationProperty.block("log");
    /**
     * Defines the leaves {@link Block} for the bush. Set these to {@link Blocks#AIR} to if the bush should be dead.
     * Defaults to {@link Blocks#OAK_LEAVES}.
     */
    public static final ConfigurationProperty<Block> LEAVES = ConfigurationProperty.block("leaves");
    /**
     * Secondary leaves for the bush, have a chance defined by {@link #SECONDARY_LEAVES_CHANCE} of generating instead of
     * {@link #LEAVES} if set (not {@code null}). Set these to {@link Blocks#AIR} to create a dying effect. Defaults to
     * {code null}.
     */
    public static final ConfigurationProperty<Block> SECONDARY_LEAVES = ConfigurationProperty.block("secondary_leaves");
    /**
     * The chance for the {@link #SECONDARY_LEAVES} (if set) to generate in place of {@link #LEAVES}. Defaults to {@code
     * 4}, giving them a 1 in 4 chance of spawning.
     */
    public static final ConfigurationProperty<Integer> SECONDARY_LEAVES_CHANCE =
            ConfigurationProperty.integer("secondary_leaves_chance");

    public BushGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(BIOME_PREDICATE, LOG, LEAVES, SECONDARY_LEAVES, SECONDARY_LEAVES_CHANCE);
    }

    @Override
    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(BIOME_PREDICATE, i -> true)
                .with(LOG, Blocks.OAK_LOG)
                .with(LEAVES, Blocks.OAK_LEAVES)
                .with(SECONDARY_LEAVES, null)
                .with(SECONDARY_LEAVES_CHANCE, 4);
    }

    @Override
    protected boolean generate(GenFeatureConfiguration configuration, FullGenerationContext context) {
        this.commonGen(configuration, context.level(), context.pos(), context.species(), context.random(),
                context.radius(), context.bounds());
        return true;
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (context.bounds() != SafeChunkBounds.ANY && configuration.get(BIOME_PREDICATE).test(context.biome())) {
            this.commonGen(configuration, context.level(), context.pos(), context.species(), context.random(),
                    context.radius(), context.bounds());
            return true;
        }
        return false;
    }

    protected void commonGen(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos rootPos, Species species,
                             Random random, int radius, SafeChunkBounds safeBounds) {
        if (radius <= 2) {
            return;
        }

        final boolean worldGen = safeBounds != SafeChunkBounds.ANY;

        Vec3 vTree = new Vec3(rootPos.getX(), rootPos.getY(), rootPos.getZ()).add(0.5, 0.5, 0.5);

        for (int i = 0; i < 2; i++) {
            int rad = Mth.clamp(random.nextInt(radius - 2) + 2, 2, radius - 1);
            Vec3 v = vTree.add(new Vec3(1, 0, 0).scale(rad).yRot((float) (random.nextFloat() * Math.PI * 2)));
            BlockPos vPos = new BlockPos(v);

            if (!safeBounds.inBounds(vPos, true)) {
                continue;
            }

            final BlockPos groundPos = CoordUtils.findWorldSurface(level, vPos, worldGen);
            final BlockState soilBlockState = level.getBlockState(groundPos);

            final BlockPos pos = groundPos.above();
            if (!level.getBlockState(groundPos).getMaterial().isLiquid() &&
                    species.isAcceptableSoil(level, groundPos, soilBlockState)) {
                level.setBlock(pos, configuration.get(LOG).defaultBlockState(), 3);

                SimpleVoxmap leafMap = LeafClusters.BUSH;
                BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos();
                for (BlockPos.MutableBlockPos dPos : leafMap.getAllNonZero()) {
                    leafPos.set(pos.getX() + dPos.getX(), pos.getY() + dPos.getY(), pos.getZ() + dPos.getZ());
                    if (safeBounds.inBounds(leafPos, true) && (coordHashCode(leafPos) % 5) != 0 &&
                            level.getBlockState(leafPos).getMaterial().isReplaceable()) {
                        placeLeaves(configuration, level, random, leafPos);
                    }
                }
            }
        }
    }

    private void placeLeaves(GenFeatureConfiguration configuration, LevelAccessor level, Random random,
                             BlockPos leafPos) {
        final Block leavesBlock = selectLeavesBlock(random, configuration.get(SECONDARY_LEAVES_CHANCE),
                configuration.get(LEAVES), configuration.getAsOptional(SECONDARY_LEAVES).orElse(null));
        placeLeavesBlock(level, leafPos, leavesBlock);
    }

    private Block selectLeavesBlock(Random random, int secondaryLeavesChance, Block leavesBlock,
                                    @Nullable Block secondaryLeavesBlock) {
        return secondaryLeavesBlock == null || random.nextInt(secondaryLeavesChance) != 0 ? leavesBlock :
                secondaryLeavesBlock;
    }

    private void placeLeavesBlock(LevelAccessor level, BlockPos leafPos, Block leavesBlock) {
        BlockState leafState = leavesBlock.defaultBlockState();
        if (leavesBlock instanceof LeavesBlock) {
            leafState = leafState.setValue(LeavesBlock.PERSISTENT, true);
        }
        level.setBlock(leafPos, leafState, 3);
    }

    public static int coordHashCode(BlockPos pos) {
        int hash = (pos.getX() * 4111 ^ pos.getY() * 271 ^ pos.getZ() * 3067) >> 1;
        return hash & 0xFFFF;
    }

}
