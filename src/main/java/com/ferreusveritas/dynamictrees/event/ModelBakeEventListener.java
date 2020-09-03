package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBranchBasic;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DynamicTrees.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelBakeEventListener {

	@SubscribeEvent
	public static void onModelBakeEvent(ModelBakeEvent event) {

		event.getModelRegistry().put(new ModelResourceLocation(new ResourceLocation(DynamicTrees.MODID, "block/oak_branch"), ""),
				new BakedModelBlockBranchBasic());

	}
	
}
