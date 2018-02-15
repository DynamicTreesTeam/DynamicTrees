package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.models.BonsaiCompositeModel;
import com.ferreusveritas.dynamictrees.models.RootyCompositeModel;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModelBakeEventListener {
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRootyDirtModelBakeEvent(ModelBakeEvent event) {
		Object rootsObject =  event.getModelRegistry().getObject(new ModelResourceLocation(ModBlocks.blockRootyDirt.getRegistryName(), "roots"));
		if (rootsObject instanceof IBakedModel) {
		      IBakedModel rootsModel = (IBakedModel) rootsObject;
		      RootyCompositeModel rootyModel = new RootyCompositeModel(rootsModel);
		      event.getModelRegistry().putObject(new ModelResourceLocation(ModBlocks.blockRootyDirt.getRegistryName(), "roots"), rootyModel);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRootyDirtSpeciesModelBakeEvent(ModelBakeEvent event) {
		Object rootsObject =  event.getModelRegistry().getObject(new ModelResourceLocation(ModBlocks.blockRootyDirtSpecies.getRegistryName(), "roots"));
		if (rootsObject instanceof IBakedModel) {
		      IBakedModel rootsModel = (IBakedModel) rootsObject;
		      RootyCompositeModel rootyModel = new RootyCompositeModel(rootsModel);
		      event.getModelRegistry().putObject(new ModelResourceLocation(ModBlocks.blockRootyDirtSpecies.getRegistryName(), "roots"), rootyModel);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onRootySandModelBakeEvent(ModelBakeEvent event) {
		Object rootsObject =  event.getModelRegistry().getObject(new ModelResourceLocation(ModBlocks.blockRootySand.getRegistryName(), "roots"));
		if (rootsObject instanceof IBakedModel) {
		      IBakedModel rootsModel = (IBakedModel) rootsObject;
		      RootyCompositeModel rootyModel = new RootyCompositeModel(rootsModel);
		      event.getModelRegistry().putObject(new ModelResourceLocation(ModBlocks.blockRootySand.getRegistryName(), "roots"), rootyModel);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onBonsaiModelBakeEvent(ModelBakeEvent event) {
				
		Object flowerPotObject =  event.getModelRegistry().getObject(new ModelResourceLocation(ModBlocks.blockBonsaiPot.getRegistryName(), "normal"));
		if (flowerPotObject instanceof IBakedModel) {
		      IBakedModel flowerPotModel = (IBakedModel) flowerPotObject;
		      BonsaiCompositeModel bonsaiPotModel = new BonsaiCompositeModel(flowerPotModel);
		      event.getModelRegistry().putObject(new ModelResourceLocation(ModBlocks.blockBonsaiPot.getRegistryName(), "normal"), bonsaiPotModel);
		}
	}
	
}
