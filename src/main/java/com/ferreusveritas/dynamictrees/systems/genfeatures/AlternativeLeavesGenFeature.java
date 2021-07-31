package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.network.MapSignal;
import com.ferreusveritas.dynamictrees.blocks.leaves.DynamicLeavesBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
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
import net.minecraft.world.biome.Biome;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class AlternativeLeavesGenFeature extends GenFeature implements IPostGrowFeature, IPostGenFeature {

    public static final ConfigurationProperty<LeavesProperties> ALT_LEAVES = ConfigurationProperty.property("alternative_leaves", LeavesProperties.class);
    public static final ConfigurationProperty<Block> ALT_LEAVES_BLOCK = ConfigurationProperty.block("alternative_leaves_block");

    public AlternativeLeavesGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(ALT_LEAVES, PLACE_CHANCE, QUANTITY);
    }

    public ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return super.createDefaultConfiguration().with(ALT_LEAVES_BLOCK, Blocks.AIR).with(ALT_LEAVES, LeavesProperties.NULL_PROPERTIES)
                .with(PLACE_CHANCE, 0.5f).with(QUANTITY, 5);
    }

    @Override
    public boolean onGenFeatureAdded(Species species, ConfiguredGenFeature<GenFeature> configuration) {
        LeavesProperties properties = configuration.get(ALT_LEAVES);
        if (properties.isValid()){
            properties.setFamily(species.getFamily());
            species.addValidLeafBlocks(properties);
        }
        return true;
    }

    @Override
    public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
        BlockBounds bounds = species.getFamily().expandLeavesBlockBounds(new BlockBounds(endPoints));

        return setAltLeaves(configuredGenFeature, world, bounds, safeBounds, species);
    }

    @Override
    public boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int fertility, boolean natural) {
        if (fertility == 0) return false;

        FindEndsNode endFinder = new FindEndsNode();
        TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
        List<BlockPos> endPoints = endFinder.getEnds();
        if (endPoints.isEmpty()) return false;
        BlockPos chosenEndPoint = endPoints.get(world.getRandom().nextInt(endPoints.size()));
        BlockBounds bounds = species.getFamily().expandLeavesBlockBounds(new BlockBounds(chosenEndPoint));

        return setAltLeaves(configuredGenFeature, world, bounds, SafeChunkBounds.ANY, species);
    }

    private Block getAltLeavesBlock (ConfiguredGenFeature<?> configuredGenFeature){
        LeavesProperties properties = configuredGenFeature.get(ALT_LEAVES);
        if (!properties.isValid() || !properties.getDynamicLeavesBlock().isPresent())
            return configuredGenFeature.get(ALT_LEAVES_BLOCK);
        return properties.getDynamicLeavesBlock().get();
    }

    private BlockState getSwapBlockState (ConfiguredGenFeature<?> configuredGenFeature, IWorld world, Species species, BlockState state, boolean worldgen){
        DynamicLeavesBlock originalLeaves = species.getLeavesBlock().orElse(null);
        Block alt = getAltLeavesBlock(configuredGenFeature);
        DynamicLeavesBlock altLeaves = alt instanceof DynamicLeavesBlock ? (DynamicLeavesBlock) alt : null;
        if (originalLeaves != null && altLeaves != null){
            if (worldgen || world.getRandom().nextFloat() < configuredGenFeature.get(PLACE_CHANCE)){
                if (state.getBlock() == originalLeaves)
                    return altLeaves.properties.getDynamicLeavesState(state.getValue(LeavesBlock.DISTANCE));
            } else {
                if (state.getBlock() == altLeaves)
                    return originalLeaves.properties.getDynamicLeavesState(state.getValue(LeavesBlock.DISTANCE));
            }
        }
        return state;
    }

    private boolean setAltLeaves (ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockBounds leafPositions, SafeChunkBounds safeBounds, Species species){
        boolean worldGen = safeBounds != SafeChunkBounds.ANY;

        if (worldGen){
            AtomicBoolean isSet = new AtomicBoolean(false);
            leafPositions.iterator().forEachRemaining((pos)->{
                if (safeBounds.inBounds(pos, true) && world.getRandom().nextFloat() < configuredGenFeature.get(PLACE_CHANCE))
                    if (world.setBlock(pos, getSwapBlockState(configuredGenFeature, world, species, world.getBlockState(pos), true), 2))
                        isSet.set(true);
            });
            return isSet.get();
        } else {
            boolean isSet = false;
            List<BlockPos> posList = new LinkedList<>();
            for (BlockPos leafPosition : leafPositions) posList.add(new BlockPos(leafPosition));
            if (posList.isEmpty()) return false;
            for (int i=0; i<configuredGenFeature.get(QUANTITY); i++){
                BlockPos pos = posList.get(world.getRandom().nextInt(posList.size()));
                if (world.setBlock(pos, getSwapBlockState(configuredGenFeature, world, species, world.getBlockState(pos), false), 2))
                    isSet = true;
            }
            return isSet;
        }
    }
}
