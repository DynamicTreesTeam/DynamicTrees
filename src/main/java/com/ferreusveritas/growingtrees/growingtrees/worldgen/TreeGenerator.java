package com.ferreusveritas.growingtrees.worldgen;

import java.util.ArrayList;
import java.util.Random;
import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;

public class TreeGenerator implements IWorldGenerator {

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

	private void generateOverworld(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider){
    	//System.out.println("Gen: " + chunkX + "," + chunkZ);
   	
    	ChunkCircleManager cm = ChunkCircleManager.getInstance();
		ArrayList<Circle> circles = cm.getCircles(random, chunkX, chunkZ);
		
		//Make the circles from the circle set
    	for(Circle c: circles){
    		makeCircle(world, c);
    	}
		
	}
	
	private void makeCircle(World world, Circle circle){
		
		circle.add(8, 8);
		
		int bottom = world.getHeightValue(circle.x, circle.z);
		
		while(bottom > 1){
			Block block = world.getBlock(circle.x, bottom, circle.z);
			if(block == Blocks.grass || block == Blocks.dirt || block == Blocks.sand || block == Blocks.gravel || block == Blocks.stone || block == Blocks.bedrock){
				break;
			}
			bottom--;
		}
		
		bottom = MathHelper.clamp_int(bottom, 1, bottom) + 1;
		
		int h = (world.rand.nextInt(16) ^ circle.x ^ circle.z) & 15;
		h *= (9 - circle.radius) / 2;
		if(h <= 1){
			h = 2;
		}

		h = 2;
		
		//System.out.println("Making circle at: " + circle.x + "," + circle.z + ":" + circle.radius + " Bottom: " + bottom + "  H: " + h);
		
    	for(int ix = -circle.radius; ix <= circle.radius; ix++){
    	 	for(int iz = -circle.radius; iz <= circle.radius; iz++){
    	 		if(circle.isInside(circle.x + ix, circle.z + iz)){
    	 			for(int iy = 0; iy < h; iy++){
    	 				world.setBlock(circle.x + ix, bottom + iy, circle.z + iz, Blocks.wool, (circle.x ^ circle.z) & 0xF, 0);
    	 			}
    	 		}
        	}
    	}

    	circle.add(-8, -8);
    	
	}


}
