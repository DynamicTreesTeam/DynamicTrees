package com.ferreusveritas.dynamictrees.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class QuadManipulator {
	
	public static final Direction everyFace[] = { Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null };
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, BlockState stateIn) {
		return getQuads(modelIn, stateIn, Vec3d.ZERO, everyFace, new Random());
	}
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, BlockState stateIn, Direction[] sides) {
		return getQuads(modelIn, stateIn, Vec3d.ZERO, sides, new Random());
	}
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, BlockState stateIn, Random rand) {
		return getQuads(modelIn, stateIn, Vec3d.ZERO, everyFace, rand);
	}
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, BlockState stateIn, Vec3d offset, Random rand) {
		return getQuads(modelIn, stateIn, offset, everyFace, rand);
	}
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, BlockState stateIn, Vec3d offset) {
		return getQuads(modelIn, stateIn, offset, everyFace, new Random());
	}
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, BlockState stateIn, Vec3d offset, Direction[] sides) {
		return getQuads(modelIn, stateIn, offset, sides, new Random());
	}
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, BlockState stateIn, Vec3d offset, Direction[] sides, Random rand) {
		ArrayList<BakedQuad> outQuads = new ArrayList<BakedQuad>();
		
		if(stateIn != null) {
			for (Direction enumfacing : sides) {
				outQuads.addAll(modelIn.getQuads(stateIn, enumfacing, rand));
			}
		}
		
		return offset.equals(Vec3d.ZERO) ? outQuads : moveQuads(outQuads, offset);
	}
	
	public static List<BakedQuad> moveQuads(List<BakedQuad> inQuads, Vec3d offset) {
		ArrayList<BakedQuad> outQuads = new ArrayList<BakedQuad>();
		
		for(BakedQuad inQuad: inQuads) {
			BakedQuad quadCopy = new BakedQuad(inQuad.getVertexData().clone(), inQuad.getTintIndex(), inQuad.getFace(), inQuad.getSprite(), inQuad.shouldApplyDiffuseLighting(), inQuad.getFormat());
			int[] vertexData = quadCopy.getVertexData();
			for(int i = 0; i < vertexData.length; i += inQuad.getFormat().getIntegerSize()) {
				int pos = 0;
				for(VertexFormatElement vfe: inQuad.getFormat().getElements()) {
					if(vfe.getUsage() == VertexFormatElement.Usage.POSITION) {
						float x = Float.intBitsToFloat(vertexData[i + pos + 0]);
						float y = Float.intBitsToFloat(vertexData[i + pos + 1]);
						float z = Float.intBitsToFloat(vertexData[i + pos + 2]);
						x += offset.x;
						y += offset.y;
						z += offset.z;
						vertexData[i + pos + 0] = Float.floatToIntBits(x);
						vertexData[i + pos + 1] = Float.floatToIntBits(y);
						vertexData[i + pos + 2] = Float.floatToIntBits(z);
						break;
					}
					pos += vfe.getSize() / 4;//Size is always in bytes but we are dealing with an array of int32s
				}
			}
			
			outQuads.add(quadCopy);
		}
		
		outQuads.trimToSize();
		return outQuads;
	}
	
//	public static IModel getModelForState(BlockState state) {
//		IModel model = null;
//
//		try {
//			model = ModelLoaderRegistry.getModel(getModelLocation(state));
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		return model;
//	}
	
	private static ModelManager modelManager = null;
	
	public static ModelManager getModelManager() {
		if(modelManager == null) {
			try {
				Field[] fields = Minecraft.class.getDeclaredFields();
				for(Field f : fields) {
					if(f.getType() == ModelManager.class) {
						f.setAccessible(true);
						return modelManager = (ModelManager) f.get(Minecraft.getInstance());
					}
				}
			}
			catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return modelManager;
	}
	
//	public static ModelResourceLocation getModelLocation(BlockState state) {
////		return getModelManager().getBlockModelShapes().getBlockStateMapper().getVariants(state.getBlock()).get(state);//This gives us earlier access
//		return Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getBlockStateMapper().getVariants(state.getBlock()).get(state);
//	}
	
//	public static ResourceLocation getModelTexture(IModel model, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, BlockState state, Direction dir) {
//
//		float uvs[] = getSpriteUVFromBlockState(state, dir);
//
//		if(uvs != null) {
//			List<TextureAtlasSprite> sprites = new ArrayList<>();
//
//			float closest = Float.POSITIVE_INFINITY;
//			ResourceLocation closestTex = new ResourceLocation("missingno");
//			if(model != null) {
//				for(ResourceLocation tex : model.getTextures()) {
//					TextureAtlasSprite tas = bakedTextureGetter.apply(tex);
//					float u = tas.getInterpolatedU(8);
//					float v = tas.getInterpolatedV(8);
//					sprites.add(tas);
//					float du = u - uvs[0];
//					float dv = v - uvs[1];
//					float distSq = du * du + dv * dv;
//					if(distSq < closest) {
//						closest = distSq;
//						closestTex = tex;
//					}
//				}
//			}
//
//			return closestTex;
//		}
//
//		return null;
//	}
//
//	public static float[] getSpriteUVFromBlockState(BlockState state, Direction side) {
//		IBakedModel bakedModel = getModelManager().getBlockModelShapes().getModelForState(state);
//		List<BakedQuad> quads = new ArrayList<BakedQuad>();
//		quads.addAll(bakedModel.getQuads(state, side, 0));
//		quads.addAll(bakedModel.getQuads(state, null, 0));
//
//		Optional<BakedQuad> quad = quads.stream().filter( q -> q.getFace() == side ).findFirst();
//
//		if(quad.isPresent()) {
//
//			float u = 0.0f;
//			float v = 0.0f;
//
//			int[] vertexData = quad.get().getVertexData();
//			int numVertices = 0;
//			for(int i = 0; i < vertexData.length; i += quad.get().getFormat().getIntegerSize()) {
//				int pos = 0;
//				for(VertexFormatElement vfe: quad.get().getFormat().getElements()) {
//					if(vfe.getUsage() == VertexFormatElement.Usage.UV) {
//						u += Float.intBitsToFloat(vertexData[i + pos + 0]);
//						v += Float.intBitsToFloat(vertexData[i + pos + 1]);
//					}
//					pos += vfe.getSize() / 4;//Size is always in bytes but we are dealing with an array of int32s
//				}
//				numVertices++;
//			}
//
//			return new float[] { u / numVertices, v / numVertices };
//		}
//
//		System.err.println("Warning: Could not get \"" + side + "\" side quads from blockstate: " + state);
//
//		return null;
//	}
	
}
