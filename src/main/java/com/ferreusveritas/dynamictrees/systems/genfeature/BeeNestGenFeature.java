package com.ferreusveritas.dynamictrees.systems.genfeature;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configuration.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.ferreusveritas.dynamictrees.systems.genfeature.context.PostGrowContext;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.function.TetraFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.BiomeDictionary;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Gen feature for bee nests. Can be fully customized with a custom predicate for natural growth and with a custom
 * function for worldgen chances. It is recommended for the generated block to be made connectable using {@link
 * com.ferreusveritas.dynamictrees.systems.BranchConnectables#makeBlockConnectable(Block, TetraFunction)}
 *
 * @author Max Hyper
 */
public class BeeNestGenFeature extends GenFeature {

    public static final ConfigurationProperty<Block> NEST_BLOCK = ConfigurationProperty.block("nest");
    public static final ConfigurationProperty<WorldGenChanceFunction> WORLD_GEN_CHANCE_FUNCTION = ConfigurationProperty.property("world_gen_chance", WorldGenChanceFunction.class);

    private static final double MEADOWS_CHANCE = 1.0D;
    private static final double PLAINS_CHANCE = 0.05f;
    private static final double FLOWER_FOREST_CHANCE = 0.02f;
    private static final double FOREST_CHANCE = 0.0005f;
    private static final double CHANCE = 0.001f;

    public BeeNestGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(NEST_BLOCK, MAX_HEIGHT, CAN_GROW_PREDICATE, WORLD_GEN_CHANCE_FUNCTION, MAX_COUNT);
    }

    @Override
    public GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration().with(NEST_BLOCK, Blocks.BEE_NEST).with(MAX_HEIGHT, 32).with(CAN_GROW_PREDICATE, (level, pos) -> {
            if (level.getRandom().nextFloat() > CHANCE) {
                return false;
            }
            // Default flower check predicate, straight from the sapling class
            for (BlockPos blockpos : BlockPos.MutableBlockPos.betweenClosed(pos.below().north(2).west(2), pos.above().south(2).east(2))) {
                if (level.getBlockState(blockpos).is(BlockTags.FLOWERS)) {
                    return true;
                }
            }
            return false;
        }).with(WORLD_GEN_CHANCE_FUNCTION, (level, pos) -> {
            // Default biome check chance function. Uses vanilla chances
            ResourceKey<Biome> biomeKey = level.getUncachedNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2).unwrapKey().orElseThrow();
            if (biomeKey == Biomes.MEADOW)
                return MEADOWS_CHANCE;
            if (BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.PLAINS)) {
                return PLAINS_CHANCE;
            }
            if (biomeKey == Biomes.FLOWER_FOREST) {
                return FLOWER_FOREST_CHANCE;
            }
            if (BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.FOREST)) {
                return FOREST_CHANCE;
            }
            return 0D;
        }).with(MAX_COUNT, 1);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        final LevelAccessor level = context.level();
        final BlockPos rootPos = context.pos();
        return !(level.getRandom().nextFloat() > configuration.get(WORLD_GEN_CHANCE_FUNCTION).apply(level, rootPos)) &&
                this.placeBeeNestInValidPlace(configuration, level, rootPos, true);
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (!context.natural() || !configuration.get(CAN_GROW_PREDICATE).test(context.level(), context.pos().above()) ||
                context.fertility() == 0) {
            return false;
        }

        return this.placeBeeNestInValidPlace(configuration, context.level(), context.pos(), false);
    }

    private boolean placeBeeNestInValidPlace(GenFeatureConfiguration configuration, LevelAccessor level, BlockPos rootPos, boolean worldGen) {
        Block nestBlock = configuration.get(NEST_BLOCK);

        int treeHeight = getTreeHeight(level, rootPos, configuration.get(MAX_HEIGHT));
        //This prevents trees from having multiple bee nests. There should be only one per tree.
        if (nestAlreadyPresent(level, nestBlock, rootPos, treeHeight)) {
            return false;
        }

        //Finds the valid places next to the trunk and under an existing branch.
        //The places are mapped to a direction list that hold the valid orientations with an air block in front
        List<Pair<BlockPos, List<Direction>>> validSpaces = findBranchPits(configuration, level, rootPos, treeHeight);
        if (validSpaces == null) {
            return false;
        }
        if (validSpaces.size() > 0) {
            Pair<BlockPos, List<Direction>> chosenSpace = validSpaces.get(level.getRandom().nextInt(validSpaces.size()));
            //There is always AT LEAST one valid direction, since if there were none the pos would not have been added to validSpaces
            Direction chosenDir = chosenSpace.getValue().get(level.getRandom().nextInt(chosenSpace.getValue().size()));

            return placeBeeNestWithBees(level, nestBlock, chosenSpace.getKey(), chosenDir, worldGen);
        }
        return false;
    }

    private boolean placeBeeNestWithBees(LevelAccessor world, Block nestBlock, BlockPos pos, Direction faceDir, boolean worldGen) {
        int honeyLevel = worldGen ? world.getRandom().nextInt(6) : 0;
        BlockState nestState = nestBlock.defaultBlockState();
        if (nestState.hasProperty(BeehiveBlock.FACING)) {
            nestState = nestState.setValue(BeehiveBlock.FACING, faceDir);
        }
        if (nestState.hasProperty(BeehiveBlock.HONEY_LEVEL)) {
            nestState = nestState.setValue(BeehiveBlock.HONEY_LEVEL, honeyLevel);
        }
        // Sets the nest block, but the bees still need to be added.
        world.setBlock(pos, nestState, 2);
        BlockEntity blockEntity = world.getBlockEntity(pos);
        // Populates the bee nest with 3 bees if the nest was generated, or with 2-3 bees if it was grown.
        if (blockEntity instanceof BeehiveBlockEntity) {
            BeehiveBlockEntity beehivetileentity = (BeehiveBlockEntity) blockEntity;
            Level thisWorld = worldFromIWorld(world);
            if (thisWorld == null) {
                return false;
            }
            int beeCount = worldGen ? 3 : 2 + world.getRandom().nextInt(2);
            for (int i = 0; i < beeCount; ++i) {
                Bee beeEntity = new Bee(EntityType.BEE, thisWorld);
                beehivetileentity.addOccupantWithPresetTicks(beeEntity, false, world.getRandom().nextInt(599));
            }
            return true;
        }
        return false;
    }

    //This just fetches a World instance from an IWorld instance, since IWorld cannot be used to create bees.
    @Nullable
    private Level worldFromIWorld(LevelAccessor iWorld) {
        if (iWorld instanceof WorldGenRegion) {
            return ((WorldGenRegion) iWorld).getLevel();
        } else if (iWorld instanceof Level) {
            return (Level) iWorld;
        }
        return null;
    }

    private boolean nestAlreadyPresent(LevelAccessor world, Block nestBlock, BlockPos rootPos, int maxHeight) {
        for (int y = 2; y < maxHeight; y++) {
            BlockPos trunkPos = rootPos.above(y);
            for (Direction dir : CoordUtils.HORIZONTALS) {
                if (world.getBlockState(trunkPos.relative(dir)).getBlock() == nestBlock) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getTreeHeight(LevelAccessor world, BlockPos rootPos, int maxHeight) {
        for (int i = 1; i < maxHeight; i++) {
            if (!TreeHelper.isBranch(world.getBlockState(rootPos.above(i)))) {
                return i - 1;
            }
        }
        return maxHeight;
    }

    //The valid places this genFeature looks for are empty blocks under branches next to the trunk, similar to armpits lol
    @Nullable
    private List<Pair<BlockPos, List<Direction>>> findBranchPits(GenFeatureConfiguration configuration, LevelAccessor world, BlockPos rootPos, int maxHeight) {
        int existingBlocks = 0;
        List<Pair<BlockPos, List<Direction>>> validSpaces = new LinkedList<>();
        for (int y = 2; y < maxHeight; y++) {
            BlockPos trunkPos = rootPos.above(y);
            for (Direction dir : CoordUtils.HORIZONTALS) {
                BlockPos sidePos = trunkPos.relative(dir);
                if (world.isEmptyBlock(sidePos) && TreeHelper.isBranch(world.getBlockState(sidePos.above()))) {
                    //the valid positions must also have a face facing towards air, otherwise bees wouldn't be able to exist the nest.
                    List<Direction> validDirs = new LinkedList<>();
                    for (Direction dir2 : CoordUtils.HORIZONTALS) {
                        if (world.isEmptyBlock(sidePos.relative(dir2))) {
                            validDirs.add(dir2);
                        }
                    }
                    if (validDirs.size() > 0) {
                        validSpaces.add(Pair.of(sidePos, validDirs));
                    }
                } else if (world.getBlockState(sidePos).getBlock() == configuration.get(NEST_BLOCK)) {
                    existingBlocks++;
                    if (existingBlocks > configuration.get(MAX_COUNT)) {
                        return null;
                    }
                }
            }
        }
        return validSpaces;
    }

    public interface WorldGenChanceFunction extends BiFunction<LevelAccessor, BlockPos, Double> {
    }

}
