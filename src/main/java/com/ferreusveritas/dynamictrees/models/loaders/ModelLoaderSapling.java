package com.ferreusveritas.dynamictrees.models.loaders;

//import com.ferreusveritas.dynamictrees.DynamicTrees;
//import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelSapling;
//import net.minecraft.client.renderer.block.model.IBakedModel;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.client.renderer.vertex.VertexFormat;
//import net.minecraft.client.resources.IResourceManager;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.client.model.ICustomModelLoader;
//import net.minecraftforge.client.model.IModel;
//import net.minecraftforge.common.model.IModelState;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//
//import java.util.function.Function;
//
//@OnlyIn(Dist.CLIENT)
//public class ModelLoaderSapling implements ICustomModelLoader {
//
//	@Override
//	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
//		return new IModel() {
//			@Override
//			public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
//				return new BakedModelSapling(state, format, bakedTextureGetter);
//			}
//		};
//	}
//
//	@Override
//	public void onResourceManagerReload(IResourceManager resourceManager) {
//	}
//
//	@Override
//	public boolean accepts(ResourceLocation modelLocation) {
//		return modelLocation.getResourceDomain().equals(DynamicTrees.MODID) && modelLocation.getResourcePath().endsWith("sapling.smart");
//	}
//
//}
