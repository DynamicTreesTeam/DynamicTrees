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
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraftforge.common.BiomeDictionary;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class BeeNestGenFeature implements IPostGenFeature, IPostGrowFeature {

    Direction[] HORIZONTALS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private Block nestBlock;
    private BiPredicate<World, BlockPos> canGrowPredicate;
    private BiFunction<IWorld, BlockPos, Double> worldGenChanceFunction;
    private int maxHeight = 32;

    private static final double vanillaGenChancePlains = 0.05f;
    private static final double vanillaGenChanceFlowerForest = 0.02f;
    private static final double vanillaGenChanceForest = 0.002f;
    private static final double vanillaGrowChance = 0.001f;

    public BeeNestGenFeature (){
        this (Blocks.BEE_NEST);
    }

    public BeeNestGenFeature (Block nestBlock){
        this(nestBlock, (world, pos)->{
            if (world.getRandom().nextFloat() > vanillaGrowChance) return false;
            //Default flower check predicate, straight from the sapling class
            for(BlockPos blockpos : BlockPos.Mutable.getAllInBoxMutable(pos.down().north(2).west(2), pos.up().south(2).east(2))) {
                if (world.getBlockState(blockpos).isIn(BlockTags.FLOWERS)) {
                    return true;
                }
            }
            return false;
        }, ((world, pos) -> {
            //Default biome check chance function. Uses vanilla chances
            RegistryKey<Biome> biomeKey = RegistryKey.getOrCreateKey(Registry.BIOME_KEY, Objects.requireNonNull(world.getNoiseBiomeRaw(pos.getX(), pos.getY(), pos.getZ()).getRegistryName()));
            if (BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.PLAINS))
                return vanillaGenChancePlains;
            if (biomeKey == Biomes.FLOWER_FOREST)
                return vanillaGenChanceFlowerForest;
            if (BiomeDictionary.hasType(biomeKey, BiomeDictionary.Type.FOREST))
                return vanillaGenChanceForest;
            return 0D;
        }));
    }

    public BeeNestGenFeature (Block nestBlock, BiPredicate<World, BlockPos> canGrow, BiFunction<IWorld, BlockPos, Double> worldGenChance){
        this.nestBlock = nestBlock;
        this.canGrowPredicate = canGrow;
        this.worldGenChanceFunction = worldGenChance;
    }

    public void setCanGrowPredicate (BiPredicate<World, BlockPos> predicate){ this.canGrowPredicate = predicate; }
    public void setWorldGenChanceFunction (BiFunction<IWorld, BlockPos, Double> function){ this.worldGenChanceFunction = function; }
    public void setMaxHeight (int maxHeight){ this.maxHeight = maxHeight; }

    @Override
    public boolean postGeneration(IWorld world, BlockPos rootPos, Species species, Biome biome, int radius, List<BlockPos> endPoints, SafeChunkBounds safeBounds, BlockState initialDirtState, float seasonValue, float seasonFruitProductionFactor) {
        if (world.getRandom().nextFloat() > worldGenChanceFunction.apply(world, rootPos)) return false;

        return placeBeeNestInValidPlace(world, rootPos, true);
    }

    @Override
    public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, Species species, int soilLife, boolean natural) {
        if (!natural || !canGrowPredicate.test(world, rootPos.up())) return false;

        return placeBeeNestInValidPlace(world, rootPos, false);
    }

    private boolean placeBeeNestInValidPlace(IWorld world, BlockPos rootPos, boolean worldGen){
        int treeHeight = getTreeHeight(world, rootPos);
        if (nestAlreadyPresent(world, rootPos, treeHeight)) return false;

        List<Pair<BlockPos, List<Direction>>> validSpaces = findBranchPits(world, rootPos, treeHeight);
        if (validSpaces.size() > 0){
            Pair<BlockPos, List<Direction>> chosenSpace = validSpaces.get(world.getRandom().nextInt(validSpaces.size()));
            Direction chosenDir = chosenSpace.getValue().get(world.getRandom().nextInt(chosenSpace.getValue().size()));

            return placeBeeNestWithBees(world, chosenSpace.getKey(), chosenDir, worldGen);
        }
        return false;
    }

    private boolean placeBeeNestWithBees(IWorld world, BlockPos pos, Direction faceDir, boolean worldGen){
        int honeyLevel = worldGen? world.getRandom().nextInt(6) : 0;
        BlockState nestState = nestBlock.getDefaultState();
        if (nestState.hasProperty(BeehiveBlock.FACING)) nestState = nestState.with(BeehiveBlock.FACING, faceDir);
        if (nestState.hasProperty(BeehiveBlock.HONEY_LEVEL)) nestState = nestState.with(BeehiveBlock.HONEY_LEVEL, honeyLevel);
        world.setBlockState(pos, nestState, 2);
        TileEntity tileentity = world.getTileEntity(pos);
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

    private World worldFromIWorld (IWorld iWorld){
        if (iWorld instanceof WorldGenRegion){
            return  ((WorldGenRegion)iWorld).getWorld();
        } else if (iWorld instanceof World){
            return  (World)iWorld;
        }
        return null;
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

    private int getTreeHeight (IWorld world, BlockPos rootPos){
        for (int i = 1; i<maxHeight; i++){
            if (!TreeHelper.isBranch(world.getBlockState(rootPos.up(i)))){
                return i-1;
            }
        }
        return maxHeight;
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
}
