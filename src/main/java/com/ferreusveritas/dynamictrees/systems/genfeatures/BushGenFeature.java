package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.cells.LeafClusters;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.FullGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.util.SimpleVoxmap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;

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
    public static final ConfigurationProperty<Integer> SECONDARY_LEAVES_CHANCE = ConfigurationProperty.integer("secondary_leaves_chance");

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
        this.commonGen(configuration, context.world(), context.pos(), context.species(), context.random(), context.radius(), context.bounds());
        return true;
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (context.bounds() != SafeChunkBounds.ANY && configuration.get(BIOME_PREDICATE).test(context.biome())) {
            this.commonGen(configuration, context.world(), context.pos(), context.species(), context.random(), context.radius(), context.bounds());
            return true;
        }
        return false;
    }

    protected void commonGen(GenFeatureConfiguration configuration, IWorld world, BlockPos rootPos, Species species, Random random, int radius, SafeChunkBounds safeBounds) {
        if (radius <= 2) {
            return;
        }

        final boolean worldGen = safeBounds != SafeChunkBounds.ANY;

        Vector3d vTree = new Vector3d(rootPos.getX(), rootPos.getY(), rootPos.getZ()).add(0.5, 0.5, 0.5);

        for (int i = 0; i < 2; i++) {
            int rad = MathHelper.clamp(random.nextInt(radius - 2) + 2, 2, radius - 1);
            Vector3d v = vTree.add(new Vector3d(1, 0, 0).scale(rad).yRot((float) (random.nextFloat() * Math.PI * 2)));
            BlockPos vPos = new BlockPos(v);

            if (!safeBounds.inBounds(vPos, true)) {
                continue;
            }

            final BlockPos groundPos = CoordUtils.findWorldSurface(world, vPos, worldGen);
            final BlockState soilBlockState = world.getBlockState(groundPos);

            final BlockPos pos = groundPos.above();
            if (!world.getBlockState(groundPos).getMaterial().isLiquid() && species.isAcceptableSoil(world, groundPos, soilBlockState)) {
                world.setBlock(pos, configuration.get(LOG).defaultBlockState(), 3);

                SimpleVoxmap leafMap = LeafClusters.BUSH;
                BlockPos.Mutable leafPos = new BlockPos.Mutable();
                for (BlockPos.Mutable dPos : leafMap.getAllNonZero()) {
                    leafPos.set(pos.getX() + dPos.getX(), pos.getY() + dPos.getY(), pos.getZ() + dPos.getZ());
                    if (safeBounds.inBounds(leafPos, true) && (coordHashCode(leafPos) % 5) != 0 && world.getBlockState(leafPos).getMaterial().isReplaceable()) {
                        Block leaf = ((configuration.get(SECONDARY_LEAVES) == null ||
                                random.nextInt(configuration.get(SECONDARY_LEAVES_CHANCE)) != 0) ?
                                configuration.get(LEAVES) : configuration.get(SECONDARY_LEAVES));
                        BlockState leafState = leaf.defaultBlockState();
                        if (leaf instanceof LeavesBlock) {
                            leafState = leafState.setValue(LeavesBlock.PERSISTENT, true);
                        }
                        world.setBlock(leafPos, leafState, 3);
                    }
                }
            }
        }
    }

    public static int coordHashCode(BlockPos pos) {
        int hash = (pos.getX() * 4111 ^ pos.getY() * 271 ^ pos.getZ() * 3067) >> 1;
        return hash & 0xFFFF;
    }

}
