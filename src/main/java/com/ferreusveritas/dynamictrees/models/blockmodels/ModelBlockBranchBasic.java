package com.ferreusveritas.dynamictrees.models.blockmodels;

//import java.util.function.Function;
//
//import com.ferreusveritas.dynamictrees.models.bakedmodels.BakedModelBlockBranchBasic;
//
//import net.minecraft.client.renderer.model.BlockModel;
//import net.minecraft.client.renderer.model.IBakedModel;
//import net.minecraft.client.renderer.model.ModelBakery;
//import net.minecraft.client.renderer.texture.ISprite;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.client.renderer.vertex.VertexFormat;
//import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import net.minecraftforge.client.model.IModel;
//import net.minecraftforge.client.model.ModelLoaderRegistry;
//
//@OnlyIn(Dist.CLIENT)
//public class ModelBlockBranchBasic implements IModel {
//
//	public ResourceLocation barkTexture;
//	public ResourceLocation ringsTexture;
//
//	public ModelBlockBranchBasic(BlockModel blockModel) {
//		barkTexture = new ResourceLocation(blockModel.resolveTextureName("bark"));
//		ringsTexture = new ResourceLocation(blockModel.resolveTextureName("rings"));
//	}
//
////	// return all other resources used by this model
////	@Override
////	public Collection<ResourceLocation> getDependencies() {
////		return ImmutableList.copyOf(new ResourceLocation[]{});
////	}
////
////	// return all the textures used by this model
////	@Override
////	public Collection<ResourceLocation> getTextures() {
////		return ImmutableList.copyOf(new ResourceLocation[]{barkTexture, ringsTexture});
////	}
//
//	// Bake the subcomponents into a CompositeModel
//	@Override
//	public IBakedModel bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format) {
//		/*try {
//			return new BakedModelBlockBranchBasic();
//		} catch (Exception exception) {
//			System.err.println("BranchModel.bake() failed due to exception:" + exception);
//			return ModelLoaderRegistry.getMissingModel().bake(bakery, spriteGetter, sprite);
//		}*/
//		return null;
//	}
//
//}