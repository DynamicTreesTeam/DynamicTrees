package com.ferreusveritas.growingtrees.worldgen;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.growingtrees.trees.GrowingTree;
import com.ferreusveritas.growingtrees.util.Circle;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.ForgeDirection;

public class TreeGenerator implements IWorldGenerator {

	public TreeCodeStore codeStore;
	public BiomeTreeSelector selector;
	public ChunkCircleManager cm;

	public TreeGenerator(){
		selector = new BiomeTreeSelector();
		codeStore = new TreeCodeStore();
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

	ForgeDirection getRandomDir(Random rand) {
		return ForgeDirection.getOrientation(2 + rand.nextInt(4));//Return NSWE
	}

	private void generateOverworld(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		ChunkCircleManager cm = ChunkCircleManager.getInstance();
		ArrayList<Circle> circles = cm.getCircles(world, random, chunkX, chunkZ);

		for(Circle c: circles) {
			makeTree(world, c);
		}
	}

	private void makeCircle(World world, Circle circle, int h) {
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

		circle.add(8, 8);

		int bottom = world.getHeightValue(circle.x, circle.z);
		bottom = MathHelper.clamp_int(bottom, 1, bottom);

		Block block = null;

		while(bottom > 1) {
			block = world.getBlock(circle.x, bottom, circle.z);
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
			bottom--;
		}

		//Uncomment below to display wool circles for testing the circle growing algorithm
		//makeCircle(world, circle, bottom);

		GrowingTree tree = selector.select(world.getBiomeGenForCoords(circle.x, circle.z));

		if(tree != null && tree.getSeed().isAcceptableSoil(block)) {
			int chance = 1;
			
			if(circle.radius > 3) {
				chance = (int) (circle.radius / 1.5f);
			}

			if(world.rand.nextInt(chance) == 0) {
				JoCode code = codeStore.getRandomCode(tree, circle.radius, world.rand);
				if(code != null) {
					code.growTree(world, tree, circle.x, bottom, circle.z, getRandomDir(world.rand), circle.radius + 3);
				}
			}
		}

		circle.add(-8, -8);
	}

}
