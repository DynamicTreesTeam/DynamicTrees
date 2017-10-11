package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class VanillaSaplingEventHandler {

	@SubscribeEvent
	public void onPlayerPlaceBlock(PlaceEvent event) {
		if(event.placedBlock == Blocks.sapling) {
			String treeNames[] = {"oak", "spruce", "birch", "jungle", "acacia", "darkoak"};
			int metadata = event.world.getBlockMetadata(event.x, event.y, event.z);
			DynamicTree tree = TreeRegistry.findTree(treeNames[metadata]);
			event.world.setBlockToAir(event.x, event.y, event.z);//Set the block to air so the plantTree function won't automatically fail.
			if(!tree.getSeed().plantSapling(event.world, new BlockPos(event.x, event.y, event.z), tree.getSeedStack())) { //If it fails then give a seed back to the player
				double x = event.x + 0.5;
				double y = event.y + 0.5;
				double z = event.z + 0.5;
				EntityItem itemEntity = new EntityItem(event.world, x, y, z, new ItemStack(tree.getSeed()));
				event.world.spawnEntityInWorld(itemEntity);
			}
		}
	}

}
