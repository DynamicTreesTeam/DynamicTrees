package com.ferreusveritas.dynamictrees.models.loaders;

import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBranchCactus;
import com.ferreusveritas.dynamictrees.models.blockmodels.ModelBlockBranchBasic;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.Function;

@SideOnly(Side.CLIENT)
public class ModelLoaderBlockBranchCactus extends ModelLoaderGeneric {

	public ModelLoaderBlockBranchCactus() {
		super("dynamiccactus", new ResourceLocation("dynamictrees", "block/smartmodel/branch"));
	}

	@Override
	protected IModel loadModel(ResourceLocation resourceLocation, ModelBlock baseModelBlock) {
		return new ModelBlockBranchBasic(baseModelBlock) {
			@Override
			public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
				try {
					return new BakedModelBlockBranchCactus(barkTexture, ringsTexture, bakedTextureGetter);
				} catch (Exception exception) {
					System.err.println("ThickModel.bake() failed due to exception:" + exception);
					return ModelLoaderRegistry.getMissingModel().bake(state, format, bakedTextureGetter);
				}
			}

		};
	}

}
