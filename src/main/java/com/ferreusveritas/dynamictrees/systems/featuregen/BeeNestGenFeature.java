package com.ferreusveritas.dynamictrees.systems.featuregen;

import com.ferreusveritas.dynamictrees.api.IPostGenFeature;
import com.ferreusveritas.dynamictrees.api.IPostGrowFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import javafx.util.Pair;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeDictionary;

import java.util.LinkedList;
import java.util.List;

public class BeeNestGenFeature implements IPostGenFeature, IPostGrowFeature {

    private static final Direction[] HORIZONTALS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private static final Block nestBlock = Blocks.BEE_NEST;
    private static final double nestGenerateChancePlains = 0.05f;
    private static final double nestGenerateChanceFlowerForest = 0.02f;
    private static final double nestGenerateChanceForest = 0.002f;
    private static final double nestGrowChance = 0.001f;

    @Override
    public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState) {
        if (world.getRandom().nextFloat() > getGenerationChanceForBiome(world, rootPos)) return false;

        //TODO: ISeedReader is NOT an instance of World, meaning bees cannot be created
        if (world instanceof World){
            return placeBeeNestInValidPlace((World) world, rootPos, 3);
        }

        return false;
    }

    @Override
    public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
        if (!hasNearbyFlora(world, rootPos.up()) || world.rand.nextFloat() > nestGrowChance || !natural) return false;

        return placeBeeNestInValidPlace(world, rootPos, 2 + world.rand.nextInt(2));
    }

    private boolean placeBeeNestInValidPlace(World world, BlockPos rootPos, int beeCount){
        int treeHeight = getTreeHeight(world, rootPos);
        if (nestAlreadyPresent(world, rootPos, treeHeight)) return false;

        List<Pair<BlockPos, List<Direction>>> validSpaces = findBranchPits(world, rootPos, treeHeight);
        if (validSpaces.size() > 0){
            Pair<BlockPos, List<Direction>> chosenSpace = validSpaces.get(world.getRandom().nextInt(validSpaces.size()));
            Direction chosenDir = chosenSpace.getValue().get(world.getRandom().nextInt(chosenSpace.getValue().size()));

            return placeBeeNestWithBees(world, chosenSpace.getKey(), chosenDir, beeCount);
        }
        return false;
    }

    private boolean placeBeeNestWithBees(World world, BlockPos pos, Direction faceDir, int beeCount){
        world.setBlockState(pos, nestBlock.getDefaultState().with(BeehiveBlock.FACING, faceDir));
        TileEntity tileentity = world.getTileEntity(pos);
        if (tileentity instanceof BeehiveTileEntity) {
            BeehiveTileEntity beehivetileentity = (BeehiveTileEntity)tileentity;

            for(int i = 0; i < Math.min(3, beeCount); ++i) {
                BeeEntity beeentity = new BeeEntity(EntityType.BEE, world);
                beehivetileentity.tryEnterHive(beeentity, false, world.rand.nextInt(599));
            }
            return true;
        }
        return false;
    }

    private int getTreeHeight (IWorld world, BlockPos rootPos){
        int treeHeight = 0;
        BlockPos testPos = rootPos.up();
        while (TreeHelper.isBranch(world.getBlockState(testPos))){
            treeHeight++;
            testPos = testPos.up();
        }
        return treeHeight;
    }

    private double getGenerationChanceForBiome (IWorld world, BlockPos pos){
        RegistryKey<Biome> biomeKey = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, world.getBiome(pos).getRegistryName());
        if (BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.PLAINS)){
            return nestGenerateChancePlains;
        }
        if (biomeKey == Biomes.FLOWER_FOREST){
            return nestGenerateChanceFlowerForest;
        }
        if (BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.FOREST)){
            return nestGenerateChanceForest;
        }
        return 0;
    }

    private List<Pair<BlockPos, List<Direction>>> findBranchPits (IWorld world, BlockPos rootPos, int maxHeight){
        List<Pair<BlockPos, List<Direction>>> validSpaces = new LinkedList<>();
        for (int y = 2; y < maxHeight; y++){
            BlockPos trunkPos = rootPos.up(y);
            for (Direction dir : HORIZONTALS){
                BlockPos sidePos = trunkPos.offset(dir);
                if (world.isAirBlock(sidePos) && TreeHelper.isBranch(world.getBlockState(sidePos.up()))){

                    List<Direction> validDirs = new LinkedList<>();
                    for (Direction dir2 : HORIZONTALS){
                        if (world.isAirBlock(sidePos.offset(dir2))){
                            validDirs.add(dir2);
                        }
                    }
                    if (validDirs.size() > 0){
                        validSpaces.add(new Pair<>(sidePos, validDirs));
                    }
                }
            }
        }
        return validSpaces;
    }

    private boolean nestAlreadyPresent(IWorld world, BlockPos rootPos, int maxHeight){
        for (int y = 2; y < maxHeight; y++){
            BlockPos trunkPos = rootPos.up(y);
            for (Direction dir : HORIZONTALS){
                if (world.getBlockState(trunkPos.offset(dir)).getBlock() == nestBlock){
                    return true;
                }
            }
        }
        return false;
    }

    //Straight from the sapling class
    private boolean hasNearbyFlora(IWorld world, BlockPos pos) {
        for(BlockPos blockpos : BlockPos.Mutable.getAllInBoxMutable(pos.down().north(2).west(2), pos.up().south(2).east(2))) {
            if (world.getBlockState(blockpos).isIn(BlockTags.FLOWERS)) {
                return true;
            }
        }
        return false;
    }
}
