 package com.ferreusveritas.growingtrees.special;

import java.util.Random;

import com.ferreusveritas.growingtrees.blocks.BlockGrowingLeaves;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BottomListenerVine implements IBottomListener {

	@Override
	public void run(World world, BlockGrowingLeaves leaves, int x, int y, int z, int subBlockNum, Random random){

		ForgeDirection around[] = { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.DOWN };
		ForgeDirection dir = around[random.nextInt(around.length)];
		
		int dx = x + dir.offsetX;
		int dy = y + dir.offsetY;
		int dz = z + dir.offsetZ;
		
		if(world.isAirBlock(dx, dy, dz) && (coordHashCode(dx, dy, dz) & 7) == 0){
			int metadata =
				dir == ForgeDirection.NORTH ? 1 :
				dir == ForgeDirection.SOUTH ? 4 :
				dir == ForgeDirection.EAST ? 2 :
				dir == ForgeDirection.WEST ? 8 : 0;
			world.setBlock(x + dir.offsetX, y, z + dir.offsetZ, Blocks.vine, metadata, 2);
		}
		
	}

	@Override
	public float chance() {
		return 1f;
	}

	@Override
	public String getName() {
		return "vine";
	}

    public static int coordHashCode(int x, int y, int z) {
        int hash = (x * 4111 ^ y * 271 ^ z * 3067) >> 1;
        return hash & 0xFFFF;
    }
	
}
