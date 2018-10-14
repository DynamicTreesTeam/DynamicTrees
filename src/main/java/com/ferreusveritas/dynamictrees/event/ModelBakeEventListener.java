package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.models.BakedModelBlockBonsaiPot;
import com.ferreusveritas.dynamictrees.models.BakedModelBlockRooty;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModelBakeEventListener {
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event) {
				
		Block[] rootyBlocks = new Block[] { ModBlocks.blockRootyDirt, ModBlocks.blockRootyDirtSpecies, ModBlocks.blockRootySand, ModBlocks.blockRootyDirtFake};
		
		for(Block block: rootyBlocks) {
			Object rootsObject = event.getModelRegistry().getObject(new ModelResourceLocation(block.getRegistryName(), "normal"));
			if (rootsObject instanceof IBakedModel) {
				IBakedModel rootsModel = (IBakedModel) rootsObject;
				BakedModelBlockRooty rootyModel = new BakedModelBlockRooty(rootsModel);
				event.getModelRegistry().putObject(new ModelResourceLocation(block.getRegistryName(), "normal"), rootyModel);
			}
		}
		
		Object flowerPotObject = event.getModelRegistry().getObject(new ModelResourceLocation(ModBlocks.blockBonsaiPot.getRegistryName(), "normal"));
		if (flowerPotObject instanceof IBakedModel) {
			IBakedModel flowerPotModel = (IBakedModel) flowerPotObject;
			BakedModelBlockBonsaiPot bonsaiPotModel = new BakedModelBlockBonsaiPot(flowerPotModel);
			event.getModelRegistry().putObject(new ModelResourceLocation(ModBlocks.blockBonsaiPot.getRegistryName(), "normal"), bonsaiPotModel);
		}
	}
	
}
