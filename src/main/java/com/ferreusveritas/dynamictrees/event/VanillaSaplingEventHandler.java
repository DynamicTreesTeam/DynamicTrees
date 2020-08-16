package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class VanillaSaplingEventHandler {

	@SubscribeEvent
	public void onPlayerPlaceBlock(BlockEvent.EntityPlaceEvent event) {
		BlockState blockState = event.getPlacedBlock();
		if (event.getWorld() instanceof World){
			if(TreeRegistry.saplingReplacers.containsKey(blockState)) {
				Species species = TreeRegistry.saplingReplacers.get(blockState);
				event.getWorld().removeBlock(event.getPos(), false);//Set the block to air so the plantTree function won't automatically fail.

				if(!species.plantSapling(event.getWorld(), event.getPos())) { //If it fails then give a seed back to the player
					double x = event.getPos().getX() + 0.5;
					double y = event.getPos().getY() + 0.5;
					double z = event.getPos().getZ() + 0.5;
					ItemEntity itemEntity = new ItemEntity((World) event.getWorld(), x, y, z, species.getSeedStack(1));
//					event.getWorld().spawnEntity(itemEntity);
				}
			}
		}

	}

}
