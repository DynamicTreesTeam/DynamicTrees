package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBranchBasic;
import com.ferreusveritas.dynamictrees.trees.Species;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DynamicTrees.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelBakeEventListener {

	@SubscribeEvent
	public static void onModelBake(ModelBakeEvent event) {
		
		//TODO: This is garbage, but it's progress
		bakeBranch(event, DTTrees.OAK, "block/oak_log", "block/oak_log_top");
		bakeBranch(event, DTTrees.BIRCH, "block/birch_log", "block/birch_log_top");
		bakeBranch(event, DTTrees.SPRUCE, "block/spruce_log", "block/spruce_log_top");
		bakeBranch(event, DTTrees.JUNGLE, "block/jungle_log", "block/jungle_log_top");
		bakeBranch(event, DTTrees.DARK_OAK, "block/dark_oak_log", "block/dark_oak_log_top");
		bakeBranch(event, DTTrees.ACACIA, "block/acacia_log", "block/acacia_log_top");
	}

	public static void bakeBranch(ModelBakeEvent event, String speciesName, String barkTexture, String ringsTexture) {
		Species species = Species.REGISTRY.getValue(new ResourceLocation(DynamicTrees.MODID, speciesName));
		Block branch = species.getFamily().getDynamicBranch();
		
		ResourceLocation barkRes = new ResourceLocation("minecraft", barkTexture);
		ResourceLocation ringRes = new ResourceLocation("minecraft", ringsTexture);

		ResourceLocation regName = branch.getRegistryName();
		
		BakedModelBlockBranchBasic bakedModel = new BakedModelBlockBranchBasic(regName, barkRes, ringRes);
		
		for(int i = 1; i <= 8; i++) {
			event.getModelRegistry().put(new ModelResourceLocation(regName, "radius=" + i), bakedModel);
		}
	}
	
}
