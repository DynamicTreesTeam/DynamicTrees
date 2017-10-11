package com.ferreusveritas.dynamictrees.worldgen;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.EnumFacing;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.Circle;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import cpw.mods.fml.common.IWorldGenerator;

public class TreeGenerator implements IWorldGenerator {

	public BiomeTreeHandler treeHandler; //Provides forest properties for a biome
	public BiomeRadiusCoordinator radiusCoordinator; //Finds radius for coordinates
	public TreeCodeStore codeStore;
	protected ChunkCircleManager circleMan;

	public TreeGenerator() {
		treeHandler = new BiomeTreeHandler();
		radiusCoordinator = new BiomeRadiusCoordinator(treeHandler);
		codeStore = new TreeCodeStore();
		circleMan = new ChunkCircleManager(radiusCoordinator);
	}

	public void onWorldUnload() {
		circleMan = new ChunkCircleManager(radiusCoordinator);//Clears the cached circles
	}

	public ChunkCircleManager getChunkCircleManager() {
		return circleMan;
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator,	IChunkProvider chunkProvider) {
		switch (world.provider.dimensionId) {
		case 0: //Overworld
			generateOverworld(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
			break;
		case -1: //Nether

			break;
		case 1: //End

			break;
		}
	}

	EnumFacing getRandomDir(Random rand) {
		return EnumFacing.getOrientation(2 + rand.nextInt(4));//Return NSWE
	}

	private void generateOverworld(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		ArrayList<Circle> circles = circleMan.getCircles(world, random, chunkX, chunkZ);

		for(Circle c: circles) {
			makeTree(world, c);
		}
		
		BlockPos pos = new BlockPos(chunkX * 16, 0, chunkZ * 16);
		if(BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(pos.getX(), pos.getZ()), Type.SPOOKY)) {
			roofedForestCompensation(world, random, pos);
		}
	}

	/**
	 * Decorate the roofedForest exactly like Minecraft, except leave out the trees and just make giant mushrooms
	 * 
	 * @param world
	 * @param random
	 * @param pos
	 */
	public void roofedForestCompensation(World world, Random random, BlockPos pos) {
		for (int xi = 0; xi < 4; ++xi) {
			for (int zi = 0; zi < 4; ++zi) {
				int posX = pos.getX() + xi * 4 + 1 + 8 + random.nextInt(3);
				int posZ = pos.getZ() + zi * 4 + 1 + 8 + random.nextInt(3);
				int posY = world.getHeightValue(posX, posZ);

				if (random.nextInt(16) == 0) {
					new WorldGenBigMushroom().generate(world, random, posX, posY, posZ);
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void makeWoolCircle(World world, Circle circle, int h) {
		//System.out.println("Making circle at: " + circle.x + "," + circle.z + ":" + circle.radius + " H: " + h);

		for(int ix = -circle.radius; ix <= circle.radius; ix++) {
			for(int iz = -circle.radius; iz <= circle.radius; iz++) {
				if(circle.isEdge(circle.x + ix, circle.z + iz)) {
					world.setBlock(circle.x + ix, h, circle.z + iz, Blocks.wool, (circle.x ^ circle.z) & 0xF, 0);
				}
			}
		}
	}

	private void makeTree(World world, Circle circle) {

		circle.add(8, 8);//Move the circle into the "stage"

		BlockPos pos = new BlockPos(circle.x, world.getHeightValue(circle.x, circle.z), circle.z);
		IBlockState blockAndMeta = null;

		while(pos.getY() > 1) {
			blockAndMeta = pos.getBlockState(world);
			Block block = blockAndMeta.getBlock();
			if(block == Blocks.grass || 
				block == Blocks.dirt ||
				block == Blocks.mycelium || 
				block == Blocks.sand || 
				block == Blocks.gravel || 
				block == Blocks.stone || 
				block == Blocks.bedrock || 
				block == Blocks.water || 
				block == Blocks.flowing_water){
				break;
			}
			pos = pos.down();
		}

		//Uncomment below to display wool circles for testing the circle growing algorithm
		//makeWoolCircle(world, circle, pos.getY());

		BiomeGenBase biome = world.getBiomeGenForCoords(circle.x, circle.z);
		DynamicTree tree = treeHandler.getTree(world.getBiomeGenForCoords(circle.x, circle.z));

		if(tree != null && tree.getSeed().isAcceptableSoil(blockAndMeta, tree.getSeedStack())) {
			if(treeHandler.chance(biome, tree, circle.radius, world.rand)) {
				JoCode code = codeStore.getRandomCode(tree, circle.radius, world.rand);
				if(code != null) {
					code.growTree(world, tree, pos, getRandomDir(world.rand), circle.radius + 3);
				}
			}
		}

		circle.add(-8, -8);//Move the circle back to normal coords
	}

}
