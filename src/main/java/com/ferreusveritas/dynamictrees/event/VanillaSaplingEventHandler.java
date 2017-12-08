package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.treedata.ISpecies;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class VanillaSaplingEventHandler {

	@SubscribeEvent
	public void onPlayerPlaceBlock(PlaceEvent event) {
		IBlockState blockState = event.getPlacedBlock();
		if(blockState.getBlock() == Blocks.SAPLING) {
			BlockPlanks.EnumType saplingType = blockState.getValue(BlockSapling.TYPE);
			String treeName = saplingType.getName().replace("_","");//DynamicTrees Mod doesn't respect underscores
			ISpecies species = TreeRegistry.findSpecies(new ResourceLocation(ModConstants.MODID, treeName));
			event.getWorld().setBlockToAir(event.getPos());//Set the block to air so the plantTree function won't automatically fail.
			if(!species.getSeed().plantSapling(event.getWorld(), event.getPos(), species.getSeedStack(1))) { //If it fails then give a seed back to the player
				double x = event.getPos().getX() + 0.5;
				double y = event.getPos().getY() + 0.5;
				double z = event.getPos().getZ() + 0.5;
				EntityItem itemEntity = new EntityItem(event.getWorld(), x, y, z, species.getSeedStack(1));
				CompatHelper.spawnEntity(event.getWorld(), itemEntity);
			}
		}
	}

}
