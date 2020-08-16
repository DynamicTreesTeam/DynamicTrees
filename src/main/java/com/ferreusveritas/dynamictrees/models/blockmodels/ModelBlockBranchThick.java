package com.ferreusveritas.dynamictrees.models.blockmodels;

import com.ferreusveritas.dynamictrees.event.TextureGenerationHandler;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import java.util.Collection;
import java.util.function.Function;

//@OnlyIn(Dist.CLIENT)
//public class ModelBlockBranchThick extends ModelBlockBranchBasic {
//
//	public ResourceLocation thickRingsTexture;
//
//	public ModelBlockBranchThick(ModelBlock modelBlock) {
//		super(modelBlock);
//
//		this.thickRingsTexture = TextureGenerationHandler.addRingTextureLocation(ringsTexture);
//	}
//
//	// return all the textures used by this model
//	@Override
//	public Collection<ResourceLocation> getTextures() {
//		return ImmutableList.copyOf(new ResourceLocation[]{barkTexture, ringsTexture, thickRingsTexture});
//	}
//
//	// Bake the subcomponents into a CompositeModel
//	@Override
//	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
//		try {
//			return new BakedModelBlockBranchThick(barkTexture, ringsTexture, thickRingsTexture, bakedTextureGetter);
//		} catch (Exception exception) {
//			System.err.println("BranchModel.bake() failed due to exception:" + exception);
//			return ModelLoaderRegistry.getMissingModel().bake(state, format, bakedTextureGetter);
//		}
//	}
//
//}
