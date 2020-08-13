package com.ferreusveritas.dynamictrees.models.bakedmodels;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicSapling;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.function.Function;


/**
 * 
 * @author ferreusveritas
 *
 */
@OnlyIn(Dist.CLIENT)
public class BakedModelSapling implements IBakedModel {
	
	private static IBakedModel[] modelMap;
	private static IBakedModel errorSaplingModel;
	private static TextureAtlasSprite particleTexture;
	
	public static IBakedModel getModelForSapling(Species species) {
		int modelId = species.saplingModelId;
		IBakedModel bakedModel = modelMap[modelId];
		return bakedModel != null ? bakedModel : errorSaplingModel;
	}
	
//	public BakedModelSapling(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
//
//		try {
//			IModel model = ModelLoaderRegistry.getModel(new ResourceLocation(DynamicTrees.MODID, "block/saplings/error"));
//			if(model != null) {
//				errorSaplingModel = model.bake(state, format, bakedTextureGetter);
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		particleTexture = bakedTextureGetter.apply(new ResourceLocation("blocks/dirt"));
//
//		modelMap = new IBakedModel[Species.REGISTRY.getEntries().size()];
//
//		int modelId = 0;
//
//		for(Entry<ResourceLocation, Species> entry : Species.REGISTRY.getEntries()) {
//			Species species = entry.getValue();
//			ResourceLocation resLoc = species.getSaplingName();
//			species.saplingModelId = modelId;
//			if(species != Species.NULLSPECIES) {
//				try {
//					IModel model = ModelLoaderRegistry.getModel(new ResourceLocation(resLoc.getResourceDomain(), "block/saplings/" + resLoc.getResourcePath()));
//					if(model != null) {
//						modelMap[modelId] = model.bake(state, format, bakedTextureGetter);
//					}
//				}
//				catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			modelId++;
//		}
//
//	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
		List<BakedQuad> quads = new ArrayList<>(12);

//		if (state != null && state.getBlock() instanceof BlockDynamicSapling && state instanceof BlockState) {
//			Species species = ((BlockState) state).get(SpeciesProperty.SPECIES);
//			quads.addAll(getModelForSapling(species).getQuads(DTRegistries.blockDynamicSapling.getDefaultState(), side, rand));
//		}

		return quads;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}
	
	@Override
	public boolean isGui3d() {
		return true;
	}
	
	@Override
	public boolean isBuiltInRenderer() {
		return true;
	}
	
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return particleTexture;
	}
	
	@Override
	public ItemOverrideList getOverrides() {
		return null;
	}
	
}
