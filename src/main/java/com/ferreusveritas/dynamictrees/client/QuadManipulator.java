package com.ferreusveritas.dynamictrees.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class QuadManipulator {
	
	public static final EnumFacing everyFace[] = { EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST, null };
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, IBlockState stateIn) {
		return getQuads(modelIn, stateIn, Vec3d.ZERO, everyFace, 0);
	}
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, IBlockState stateIn, EnumFacing[] sides) {
		return getQuads(modelIn, stateIn, Vec3d.ZERO, sides, 0);
	}
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, IBlockState stateIn, long rand) {
		return getQuads(modelIn, stateIn, Vec3d.ZERO, everyFace, rand);
	}
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, IBlockState stateIn, Vec3d offset, long rand) {
		return getQuads(modelIn, stateIn, offset, everyFace, rand);
	}
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, IBlockState stateIn, Vec3d offset) {
		return getQuads(modelIn, stateIn, offset, everyFace, 0);
	}
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, IBlockState stateIn, Vec3d offset, EnumFacing[] sides) {
		return getQuads(modelIn, stateIn, offset, sides, 0);
	}
	
	public static List<BakedQuad> getQuads(IBakedModel modelIn, IBlockState stateIn, Vec3d offset, EnumFacing[] sides, long rand) {
		ArrayList<BakedQuad> outQuads = new ArrayList<BakedQuad>();
		
		if(stateIn != null) {
			for (EnumFacing enumfacing : sides) {
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
					if(vfe.getUsage() == EnumUsage.POSITION) {
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
	
	public static IModel getModelForState(IBlockState state) {		
		IModel model = null;
		
		try {
			model = ModelLoaderRegistry.getModel(getModelLocation(state));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return model;
	}
	
	private static ModelManager modelManager = null;
	
	public static ModelManager getModelManager() {
		if(modelManager == null) {
			try {
				Field[] fields = Minecraft.class.getDeclaredFields();
				for(Field f : fields) {
					if(f.getType() == ModelManager.class) {
						f.setAccessible(true);
						return modelManager = (ModelManager) f.get(Minecraft.getMinecraft());
					}
				}
			}
			catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return modelManager;
	}
	
	public static ModelResourceLocation getModelLocation(IBlockState state) {
		return getModelManager().getBlockModelShapes().getBlockStateMapper().getVariants(state.getBlock()).get(state);//This gives us earlier access
		//return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getBlockStateMapper().getVariants(state.getBlock()).get(state);
	}
	
	public static ResourceLocation getModelTexture(IModel model, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter, IBlockState state, EnumFacing dir) {
		
		float uvs[] = getSpriteUVFromBlockState(state, dir);
		
		if(uvs != null) {
			List<TextureAtlasSprite> sprites = new ArrayList<>();
			
			float closest = Float.POSITIVE_INFINITY;
			ResourceLocation closestTex = new ResourceLocation("missingno");
			if(model != null) {
				for(ResourceLocation tex : model.getTextures()) {
					TextureAtlasSprite tas = bakedTextureGetter.apply(tex);
					float u = tas.getInterpolatedU(8);
					float v = tas.getInterpolatedV(8);
					sprites.add(tas);
					float du = u - uvs[0];
					float dv = v - uvs[1];
					float distSq = du * du + dv * dv;
					if(distSq < closest) {
						closest = distSq;
						closestTex = tex;
					}
				}
			}
			
			return closestTex;
		}
		
		return null;
	}
	
	public static float[] getSpriteUVFromBlockState(IBlockState state, EnumFacing side) {
		IBakedModel bakedModel = getModelManager().getBlockModelShapes().getModelForState(state);
		List<BakedQuad> quads = bakedModel.getQuads(state, side, 0);
		
		if(quads.size() != 0) { 
			BakedQuad quad = quads.get(0);
			
			float u = 0.0f;
			float v = 0.0f;
			
			int[] vertexData = quad.getVertexData();
			int numVertices = 0;
			for(int i = 0; i < vertexData.length; i += quad.getFormat().getIntegerSize()) {
				int pos = 0;
				for(VertexFormatElement vfe: quad.getFormat().getElements()) {
					if(vfe.getUsage() == EnumUsage.UV) {
						u += Float.intBitsToFloat(vertexData[i + pos + 0]);
						v += Float.intBitsToFloat(vertexData[i + pos + 1]);
					}
					pos += vfe.getSize() / 4;//Size is always in bytes but we are dealing with an array of int32s
				}
				numVertices++;
			}
			
			return new float[] { u / numVertices, v / numVertices };
		}
		return null;
	}
	
}
