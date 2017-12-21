package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.IBlockState;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.api.network.IBurningListener;

import net.minecraft.block.material.Material;
//import net.minecraft.world.IWorldEventListener;

/** Sadly Minecraft 1.7.10 has no IWorldEventListener for reacting to block changes..  So this won't work */
public class BurningEventListener { //implements IWorldEventListener {
	
	//@Override
	public void notifyBlockUpdate(net.minecraft.world.World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {

		if(flags == 3 && oldState.getBlock() instanceof IBurningListener) { //The old block was a Burning Listener
			if(newState.getMaterial() == Material.fire) { //The new block is made of fire.  It certainly burned.
				((IBurningListener)oldState.getBlock()).onBurned(new World(worldIn), oldState, pos);//Tell the block what happened
			}
		}
		
	}
	

}
