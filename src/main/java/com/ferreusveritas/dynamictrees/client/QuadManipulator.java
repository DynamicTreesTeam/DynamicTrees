package com.ferreusveritas.dynamictrees.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
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
	
}
