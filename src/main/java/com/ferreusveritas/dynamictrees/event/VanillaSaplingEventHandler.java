package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.backport.BlockPos;
import com.ferreusveritas.dynamictrees.api.backport.World;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CompatHelper;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;

public class VanillaSaplingEventHandler {

	@SubscribeEvent
	public void onPlayerPlaceBlock(PlaceEvent event) {
		if(event.placedBlock == Blocks.sapling) {
			World world = new World(event.world);
			String treeNames[] = {"oak", "spruce", "birch", "jungle", "acacia", "darkoak"};
			int metadata = event.blockMetadata;
			Species species = TreeRegistry.findSpecies(new ResourceLocation(ModConstants.MODID, treeNames[metadata]));
			world.setBlockToAir(new BlockPos(event.x, event.y, event.z));//Set the block to air so the plantTree function won't automatically fail.
			if(!species.getSeed().plantSapling(world, new BlockPos(event.x, event.y, event.z), species.getSeedStack(1))) { //If it fails then give a seed back to the player
				double x = event.x + 0.5;
				double y = event.y + 0.5;
				double z = event.z + 0.5;
				EntityItem itemEntity = new EntityItem(world.real(), x, y, z, species.getSeedStack(1));
				CompatHelper.spawnEntity(world, itemEntity);
			}
		}
	}

}
