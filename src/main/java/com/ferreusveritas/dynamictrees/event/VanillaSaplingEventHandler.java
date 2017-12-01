package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class VanillaSaplingEventHandler {

	@SubscribeEvent
	public void onPlayerPlaceBlock(PlaceEvent event) {
		IBlockState blockState = event.getPlacedBlock();
		if(blockState.getBlock() == Blocks.SAPLING) {
			World world = event.getWorld();
			String treeNames[] = {"oak", "spruce", "birch", "jungle", "acacia", "darkoak"};
			int metadata = blockState.getValue(BlockSapling.TYPE).getMetadata();
			DynamicTree tree = TreeRegistry.findTree(treeNames[metadata]);
			world.setBlockToAir(event.getPos());//Set the block to air so the plantTree function won't automatically fail.
			if(!tree.getSeed().plantSapling(world, event.getPos(), tree.getSeedStack())) { //If it fails then give a seed back to the player
				double x = event.getPos().getX() + 0.5;
				double y = event.getPos().getY() + 0.5;
				double z = event.getPos().getZ() + 0.5;
				EntityItem itemEntity = new EntityItem(world, x, y, z, tree.getSeedStack());
				CompatHelper.spawnEntity(world, itemEntity);
			}
		}
	}

}
