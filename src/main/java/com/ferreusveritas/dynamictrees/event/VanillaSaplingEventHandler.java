package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class VanillaSaplingEventHandler {

	@SubscribeEvent //EVENT_BUS
	public void onPlayerPlaceBlock(PlaceEvent event) {
		IBlockState blockState = event.getPlacedBlock();
		
		if(TreeRegistry.saplingReplacers.containsKey(blockState)) {
			Species species = TreeRegistry.saplingReplacers.get(blockState);
			event.getWorld().setBlockToAir(event.getPos());//Set the block to air so the plantTree function won't automatically fail.
			
			if(!species.plantSapling(event.getWorld(), event.getPos())) { //If it fails then give a seed back to the player
				double x = event.getPos().getX() + 0.5;
				double y = event.getPos().getY() + 0.5;
				double z = event.getPos().getZ() + 0.5;
				EntityItem itemEntity = new EntityItem(event.getWorld(), x, y, z, species.getSeedStack(1));
				event.getWorld().spawnEntity(itemEntity);
			}
		}
	}

	@SubscribeEvent	//TERRAIN_GEN_BUS
	public void onSaplingGrowTree(SaplingGrowTreeEvent event) {
		
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		IBlockState blockState = world.getBlockState(pos);
		
		if(TreeRegistry.saplingReplacers.containsKey(blockState)) {
			Species species = TreeRegistry.saplingReplacers.get(blockState);
			event.getWorld().setBlockToAir(event.getPos());//Set the block to air so the plantTree function won't automatically fail.
			event.setResult(Result.DENY);

			if(species.isValid()) {
				if(BlockDynamicSapling.canSaplingStay(world, species, pos)) {
					species.transitionToTree(world, pos);
				}
			}
		}

	}
	
}
