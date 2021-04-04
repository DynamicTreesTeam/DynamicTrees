package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.GenFeatureProperty;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Gen feature for bee nests. Can be fully customized with a custom predicate for natural growth
 * and with a custom function for worldgen chances.
 * It is recommended for the generated block to be made connectable using {@link com.ferreusveritas.dynamictrees.systems.BranchConnectables#makeBlockConnectable(Block, BranchConnectables.RadiusForConnectionFunction) makeBlockConnectable}
 *
 * @author Max Hyper
 */
public class BeeNestGenFeature extends GenFeature implements IPostGenFeature, IPostGrowFeature {

    public static final GenFeatureProperty<Block> NEST_BLOCK = GenFeatureProperty.createBlockProperty("nest");
    public static final GenFeatureProperty<WorldGenChanceFunction> WORLD_GEN_CHANCE_FUNCTION = GenFeatureProperty.createProperty("world_gen_chance", WorldGenChanceFunction.class);

    private static final double vanillaGenChancePlains = 0.05f;
    private static final double vanillaGenChanceFlowerForest = 0.02f;
    private static final double vanillaGenChanceForest = 0.002f;
    private static final double vanillaGrowChance = 0.001f;
    
    public BeeNestGenFeature (ResourceLocation registryName) {
        super(registryName, NEST_BLOCK, MAX_HEIGHT, CAN_GROW_PREDICATE, WORLD_GEN_CHANCE_FUNCTION);
    }

    @Override
    public ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return super.createDefaultConfiguration().with(NEST_BLOCK, Blocks.BEE_NEST).with(MAX_HEIGHT, 32).with(CAN_GROW_PREDICATE, (world, pos) -> {
            if (world.getRandom().nextFloat() > vanillaGrowChance) return false;
            // Default flower check predicate, straight from the sapling class
            for(BlockPos blockpos : BlockPos.Mutable.getAllInBoxMutable(pos.down().north(2).west(2), pos.up().south(2).east(2))) {
                if (world.getBlockState(blockpos).isIn(BlockTags.FLOWERS)) {
                    return true;
                }
            }
            return false;
        }).with(WORLD_GEN_CHANCE_FUNCTION, (world, pos) -> {
            // Default biome check chance function. Uses vanilla chances
            RegistryKey<Biome> biomeKey = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, Objects.requireNonNull(world.getNoiseBiomeRaw(pos.getX(), pos.getY(), pos.getZ()).getRegistryName()));
            if (BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.PLAINS))
                return vanillaGenChancePlains;
            if (biomeKey == Biomes.FLOWER_FOREST)
                return vanillaGenChanceFlowerForest;
            if (BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.FOREST))
                return vanillaGenChanceForest;
            return 0D;
        });
    }

    @Override
    public boolean postGeneration(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, Float seasonValue, Float seasonFruitProductionFactor) {
        if (world.getRandom().nextFloat() > configuredGenFeature.get(WORLD_GEN_CHANCE_FUNCTION).apply(world, rootPos)) return false;

        return placeBeeNestInValidPlace(configuredGenFeature, world, rootPos, true);
    }

    @Override
    public boolean postGrow(ConfiguredGenFeature<?> configuredGenFeature, World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
        if (!natural || !configuredGenFeature.get(CAN_GROW_PREDICATE).test(world, rootPos.up())) return false;

        return placeBeeNestInValidPlace(configuredGenFeature, world, rootPos, false);
    }

    private boolean placeBeeNestInValidPlace(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos rootPos, boolean worldGen) {
        Block nestBlock = configuredGenFeature.get(NEST_BLOCK);

        int treeHeight = getTreeHeight(world, rootPos, configuredGenFeature.get(MAX_HEIGHT));
        //This prevents trees from having multiple bee nests. There should be only one per tree.
        if (nestAlreadyPresent(world, nestBlock, rootPos, treeHeight)) return false;

        //Finds the valid places next to the trunk and under an existing branch.
        //The places are mapped to a direction list that hold the valid orientations with an air block in front
        List<Pair<BlockPos, List<Direction>>> validSpaces = findBranchPits(world, rootPos, treeHeight);
        if (validSpaces.size() > 0) {
            Pair<BlockPos, List<Direction>> chosenSpace = validSpaces.get(world.getRandom().nextInt(validSpaces.size()));
            //There is always AT LEAST one valid direction, since if there were none the pos would not have been added to validSpaces
            Direction chosenDir = chosenSpace.getValue().get(world.getRandom().nextInt(chosenSpace.getValue().size()));

            return placeBeeNestWithBees(world, nestBlock, chosenSpace.getKey(), chosenDir, worldGen);
        }
        return false;
    }

    private boolean placeBeeNestWithBees(IWorld world, Block nestBlock, BlockPos pos, Direction faceDir, boolean worldGen) {
        int honeyLevel = worldGen? world.getRandom().nextInt(6) : 0;
        BlockState nestState = nestBlock.getDefaultState();
        if (nestState.hasProperty(BeehiveBlock.FACING)) nestState = nestState.with(BeehiveBlock.FACING, faceDir);
        if (nestState.hasProperty(BeehiveBlock.HONEY_LEVEL)) nestState = nestState.with(BeehiveBlock.HONEY_LEVEL, honeyLevel);
        //Sets the nest block, but the bees still need to be added
        world.setBlockState(pos, nestState, 2);
        TileEntity tileentity = world.getTileEntity(pos);
        //Populates the bee nest with 3 bees if the nest was generated, or with 2-3 bees if it was grown.
        if (tileentity instanceof BeehiveTileEntity) {
            BeehiveTileEntity beehivetileentity = (BeehiveTileEntity)tileentity;
            World thisWorld = worldFromIWorld(world);
            if (thisWorld == null) return false;
            int beeCount = worldGen? 3 : 2 + world.getRandom().nextInt(2);
            for(int i = 0; i < beeCount; ++i) {
                BeeEntity beeentity = new BeeEntity(EntityType.BEE, thisWorld);
                beehivetileentity.tryEnterHive(beeentity, false, world.getRandom().nextInt(599));
            }
            return true;
        }
        return false;
    }

    //This just fetches a World instance from an IWorld instance, since IWorld cannot be used to create bees.
    @Nullable
    private World worldFromIWorld (IWorld iWorld) {
        if (iWorld instanceof WorldGenRegion) {
            return ((WorldGenRegion)iWorld).getWorld();
        } else if (iWorld instanceof World) {
            return (World)iWorld;
        }
        return null;
    }

    private boolean nestAlreadyPresent(IWorld world, Block nestBlock, BlockPos rootPos, int maxHeight) {
        for (int y = 2; y < maxHeight; y++) {
            BlockPos trunkPos = rootPos.up(y);
            for (Direction dir : CoordUtils.HORIZONTALS) {
                if (world.getBlockState(trunkPos.offset(dir)).getBlock() == nestBlock) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getTreeHeight (IWorld world, BlockPos rootPos, int maxHeight) {
        for (int i = 1; i < maxHeight; i++) {
            if (!TreeHelper.isBranch(world.getBlockState(rootPos.up(i)))) {
                return i - 1;
            }
        }
        return maxHeight;
    }

    //The valid places this genFeature looks for are empty blocks under branches next to the trunk, similar to armpits lol
    private List<Pair<BlockPos, List<Direction>>> findBranchPits (IWorld world, BlockPos rootPos, int maxHeight) {
        List<Pair<BlockPos, List<Direction>>> validSpaces = new LinkedList<>();
        for (int y = 2; y < maxHeight; y++) {
            BlockPos trunkPos = rootPos.up(y);
            for (Direction dir : CoordUtils.HORIZONTALS) {
                BlockPos sidePos = trunkPos.offset(dir);
                if (world.isAirBlock(sidePos) && TreeHelper.isBranch(world.getBlockState(sidePos.up()))) {

                    //the valid positions must also have a face facing towards air, otherwise bees wouldn't be able to exist the nest.
                    List<Direction> validDirs = new LinkedList<>();
                    for (Direction dir2 : CoordUtils.HORIZONTALS) {
                        if (world.isAirBlock(sidePos.offset(dir2))) {
                            validDirs.add(dir2);
                        }
                    }
                    if (validDirs.size() > 0) {
                        validSpaces.add(Pair.of(sidePos, validDirs));
                    }
                }
            }
        }
        return validSpaces;
    }

    public interface WorldGenChanceFunction extends BiFunction<IWorld, BlockPos, Double> {}

}
