package com.ferreusveritas.dynamictrees.worldgen;

//import com.ferreusveritas.dynamictrees.api.worldgen.IGroundFinder;
//import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
//import com.ferreusveritas.dynamictrees.worldgen.BiomeDataBase.BiomeEntry;
//import net.minecraft.block.Block;
//import net.minecraft.block.material.Material;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.init.Blocks;
//import net.minecraft.util.EnumFacing;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.BlockPos.MutableBlockPos;
//import net.minecraft.util.math.ChunkPos;
//import net.minecraft.world.World;
//import net.minecraft.world.WorldType;
//import net.minecraft.world.chunk.Chunk;
//import net.minecraft.world.chunk.IChunkProvider;
//import net.minecraft.world.gen.IChunkGenerator;
//import net.minecraftforge.fml.common.IWorldGenerator;
//
//import java.util.ArrayList;
//import java.util.Random;
//
//public class WorldGeneratorTrees implements IWorldGenerator {
//
//	public static class GroundFinder implements IGroundFinder {
//
//		protected boolean inNetherRange(BlockPos pos) {
//			return pos.getY() >= 0 && pos.getY() <= 128;
//		}
//
//		protected ArrayList<Integer> findSubterraneanLayerHeights(World world, BlockPos start) {
//
//			MutableBlockPos pos = new MutableBlockPos(world.getHeight(start)).move(EnumFacing.DOWN);
//
//			ArrayList<Integer> layers = new ArrayList();
//
//			while(inNetherRange(pos)) {
//				while(!world.isAirBlock(pos) && inNetherRange(pos)) { pos.move(EnumFacing.UP, 4); } //Zip up 4 blocks at a time until we hit air
//				while(world.isAirBlock(pos) && inNetherRange(pos))  { pos.move(EnumFacing.DOWN); } //Move down 1 block at a time until we hit not-air
//				if (world.getBlockState(pos).getMaterial() != Material.LAVA) { layers.add(pos.getY()); } //Record this position
//				pos.move(EnumFacing.UP, 16); //Move up 16 blocks
//				while(world.isAirBlock(pos) && inNetherRange(pos)) { pos.move(EnumFacing.UP, 4); } //Zip up 4 blocks at a time until we hit ground
//			}
//
//			//Discard the last result as it's just the top of the biome(bedrock for nether)
//			if (layers.size() > 0) {
//				layers.remove(layers.size() - 1);
//			}
//
//			return layers;
//		}
//
//		protected BlockPos findSubterraneanGround(World world, BlockPos start) {
//			ArrayList<Integer> layers = findSubterraneanLayerHeights(world, start);
//			if (layers.size() < 1) {
//				return BlockPos.ORIGIN;
//			}
//			int y = layers.get(world.rand.nextInt(layers.size()));
//
//			return new BlockPos(start.getX(), y, start.getZ());
//		}
//
//		protected boolean inOverworldRange(BlockPos pos) {
//			return pos.getY() >= 0 && pos.getY() <= 255;
//		}
//
//		protected BlockPos findOverworldGround(World world, BlockPos start) {
//
//			Chunk chunk = world.getChunkFromBlockCoords(start);//We'll use a chunk for the search so we don't have to keep looking up the chunk for every block
//
//			MutableBlockPos mPos = new MutableBlockPos(world.getHeight(start)).move(EnumFacing.UP, 2);//Mutable allows us to change the test position easily
//			while(inOverworldRange(mPos)) {
//
//				IBlockState state = chunk.getBlockState(mPos);
//				Block testBlock = state.getBlock();
//
//				if(testBlock != Blocks.AIR) {
//					Material material = state.getMaterial();
//					if( material == Material.GROUND || material == Material.WATER || //These will account for > 90% of blocks in the world so we can solve this early
//							(state.getMaterial().blocksMovement() &&
//							!testBlock.isLeaves(state, world, mPos) &&
//							!testBlock.isFoliage(world, mPos))) {
//						return mPos.toImmutable();
//					}
//				}
//
//				mPos.move(EnumFacing.DOWN);
//			}
//
//			return BlockPos.ORIGIN;
//		}
//
//		@Override
//		public BlockPos findGround(BiomeEntry biomeEntry, World world, BlockPos start) {
//			return biomeEntry.isSubterraneanBiome() ? findSubterraneanGround(world, start) : findOverworldGround(world, start);
//		}
//
//	}
//
//	@Override
//	public void generate(Random randomUnused, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
//		if(world.getWorldType() == WorldType.FLAT) {
//			return;
//		}
//		TreeGenerator treeGenerator = TreeGenerator.getTreeGenerator();
//		BiomeDataBase dbase = treeGenerator.getBiomeDataBase(world);
//		if(dbase != TreeGenerator.DIMENSIONBLACKLISTED) {
//			SafeChunkBounds safeBounds = new SafeChunkBounds(world, new ChunkPos(chunkX, chunkZ));//Area that is safe to place blocks during worldgen
//			treeGenerator.getCircleProvider().getPoissonDiscs(world, chunkX, 0, chunkZ).forEach(c -> treeGenerator.makeTree(world, dbase, c, new GroundFinder(), safeBounds));
//		}
//	}
//
//}
