package com.ferreusveritas.growingtrees.worldgen;

import java.util.ArrayList;
import java.util.Random;

import com.ferreusveritas.growingtrees.TreeRegistry;
import com.ferreusveritas.growingtrees.util.Circle;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
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
		
		bottom = MathHelper.clamp_int(bottom, 1, bottom);
		
		//System.out.println("Making circle at: " + circle.x + "," + circle.z + ":" + circle.radius + " Bottom: " + bottom + "  H: " + h);
		
    	for(int ix = -circle.radius; ix <= circle.radius; ix++){
    	 	for(int iz = -circle.radius; iz <= circle.radius; iz++){
    	 		if(circle.isEdge(circle.x + ix, circle.z + iz)){
   	 				world.setBlock(circle.x + ix, bottom, circle.z + iz, Blocks.stained_glass, (circle.x ^ circle.z) & 0xF, 0);
    	 		}
        	}
    	}

    	
    	if(circle.radius > 6){
        	String codes[] = {
           			"JOJxxxxxx6+d86vz9viPyWXkx87Znzf0kxOfnzZ89uLxOfvbfufuPv",
           			"JOJ0hx69xxZ69y9x7+SxOOOf09686ytPkxx867eJOJnb9",
           			"JOOOOOOPyXf09V7+nq6WkPq8f",
           			"JJxxxxxOJx8+Xz8nyRXzZfntOL96tvbxnfyWNS+XkM+kMxU89"
            	};
    		
    		JoCode code = new JoCode(codes[world.rand.nextInt(codes.length)]).setCareful(false);
    		code.growTree(world, TreeRegistry.findTree("oak"), circle.x, bottom, circle.z);
    	}
    	
    	circle.add(-8, -8);
    	
	}


}
