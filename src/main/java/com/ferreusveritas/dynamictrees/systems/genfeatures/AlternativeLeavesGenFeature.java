package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeatures.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.systems.nodemappers.FindEndsNode;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.BlockBounds;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AlternativeLeavesGenFeature extends GenFeature {

    public static final ConfigurationProperty<LeavesProperties> ALT_LEAVES = ConfigurationProperty.property("alternative_leaves", LeavesProperties.class);
    public static final ConfigurationProperty<Block> ALT_LEAVES_BLOCK = ConfigurationProperty.block("alternative_leaves_block");

    public AlternativeLeavesGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(ALT_LEAVES, ALT_LEAVES_BLOCK, PLACE_CHANCE, QUANTITY);
    }

    public ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return super.createDefaultConfiguration().with(ALT_LEAVES, LeavesProperties.NULL_PROPERTIES).with(ALT_LEAVES_BLOCK, Blocks.AIR)
                .with(PLACE_CHANCE, 0.5f).with(QUANTITY, 5);
    }

    @Override
    public boolean onApplied(Species species, ConfiguredGenFeature<GenFeature> configuration) {
        configuration.get(ALT_LEAVES).ifValid(properties -> {
            properties.setFamily(species.getFamily());
            species.addValidLeafBlocks(properties);
        });
        return true;
    }

    @Override
    protected boolean postGenerate(ConfiguredGenFeature<GenFeature> configuration, PostGenerationContext context) {
        final BlockBounds bounds = context.species().getFamily().expandLeavesBlockBounds(new BlockBounds(context.endPoints()));
        return this.setAltLeaves(configuration, context.world(), bounds, context.bounds(), context.species());
    }

    @Override
    protected boolean postGrow(ConfiguredGenFeature<GenFeature> configuration, PostGrowContext context) {
        if (context.fertility() == 0) {
            return false;
        }

        final World world = context.world();
        final Species species = context.species();

        final FindEndsNode endFinder = new FindEndsNode();
        TreeHelper.startAnalysisFromRoot(world, context.pos(), new MapSignal(endFinder));
        final List<BlockPos> endPoints = endFinder.getEnds();
        if (endPoints.isEmpty()) {
            return false;
        }

        final BlockPos chosenEndPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));
        final BlockBounds bounds = species.getFamily().expandLeavesBlockBounds(new BlockBounds(chosenEndPoint));

        return setAltLeaves(configuration, world, bounds, SafeChunkBounds.ANY, species);
    }

    private Block getAltLeavesBlock(ConfiguredGenFeature<?> conifuration) {
        LeavesProperties properties = conifuration.get(ALT_LEAVES);
        if (!properties.isValid() || !properties.getDynamicLeavesBlock().isPresent()) {
            return conifuration.get(ALT_LEAVES_BLOCK);
        }
        return properties.getDynamicLeavesBlock().get();
    }

    private BlockState getSwapBlockState(ConfiguredGenFeature<?> configuration, IWorld world, Species species, BlockState state, boolean worldgen) {
        DynamicLeavesBlock originalLeaves = species.getLeavesBlock().orElse(null);
        Block alt = getAltLeavesBlock(configuration);
        DynamicLeavesBlock altLeaves = alt instanceof DynamicLeavesBlock ? (DynamicLeavesBlock) alt : null;
        if (originalLeaves != null && altLeaves != null) {
            if (worldgen || world.getRandom().nextFloat() < configuration.get(PLACE_CHANCE)) {
                if (state.getBlock() == originalLeaves) {
                    return altLeaves.properties.getDynamicLeavesState(state.getValue(LeavesBlock.DISTANCE));
                }
            } else {
                if (state.getBlock() == altLeaves) {
                    return originalLeaves.properties.getDynamicLeavesState(state.getValue(LeavesBlock.DISTANCE));
                }
            }
        }
        return state;
    }

    private boolean setAltLeaves(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockBounds leafPositions, SafeChunkBounds safeBounds, Species species) {
        boolean worldGen = safeBounds != SafeChunkBounds.ANY;

        if (worldGen) {
            AtomicBoolean isSet = new AtomicBoolean(false);
            leafPositions.iterator().forEachRemaining((pos) -> {
                if (safeBounds.inBounds(pos, true) && world.getRandom().nextFloat() < configuredGenFeature.get(PLACE_CHANCE)) {
                    if (world.setBlock(pos, getSwapBlockState(configuredGenFeature, world, species, world.getBlockState(pos), true), 2)) {
                        isSet.set(true);
                    }
                }
            });
            return isSet.get();
        } else {
            boolean isSet = false;
            List<BlockPos> posList = new LinkedList<>();
            for (BlockPos leafPosition : leafPositions) {
                posList.add(new BlockPos(leafPosition));
            }
            if (posList.isEmpty()) {
                return false;
            }
            for (int i = 0; i < configuredGenFeature.get(QUANTITY); i++) {
                BlockPos pos = posList.get(world.getRandom().nextInt(posList.size()));
                if (world.setBlock(pos, getSwapBlockState(configuredGenFeature, world, species, world.getBlockState(pos), false), 2)) {
                    isSet = true;
                }
            }
            return isSet;
        }
    }
}
