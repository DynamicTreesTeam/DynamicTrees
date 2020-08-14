package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBonsaiPot;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockRooty;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ModelBakeEventListener {
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event) {
		
		Block[] rootyBlocks = new Block[] { DTRegistries.blockRootyDirt, DTRegistries.blockRootyDirtSpecies, DTRegistries.blockRootySand, DTRegistries.blockRootyDirtFake};
		
		for(Block block: rootyBlocks) {
			IBakedModel rootsObject = event.getModelRegistry().get(new ModelResourceLocation(block.getRegistryName(), "normal"));
			if (rootsObject instanceof IBakedModel) {
				IBakedModel rootsModel = (IBakedModel) rootsObject;
				BakedModelBlockRooty rootyModel = new BakedModelBlockRooty(rootsModel);
				event.getModelRegistry().put(new ModelResourceLocation(block.getRegistryName(), "normal"), rootyModel);
			}
		}
		
		IBakedModel flowerPotObject = event.getModelRegistry().get(new ModelResourceLocation(DTRegistries.blockBonsaiPot.getRegistryName(), "normal"));
		if (flowerPotObject instanceof IBakedModel) {
			IBakedModel flowerPotModel = (IBakedModel) flowerPotObject;
			BakedModelBlockBonsaiPot bonsaiPotModel = new BakedModelBlockBonsaiPot(flowerPotModel);
			event.getModelRegistry().put(new ModelResourceLocation(DTRegistries.blockBonsaiPot.getRegistryName(), "normal"), bonsaiPotModel);
		}
	}
	
}
