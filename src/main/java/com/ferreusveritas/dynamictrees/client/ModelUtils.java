package com.ferreusveritas.dynamictrees.client;

import org.lwjgl.util.vector.Vector3f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;

public class ModelUtils {
	
	public static float[] getUVs(AxisAlignedBB box, EnumFacing face) {
		switch(face) {
			default:
			case DOWN:  return new float[]{ (float) box.minX, 16f - (float) box.minZ, (float) box.maxX, 16f - (float) box.maxZ };
			case UP:    return new float[]{ (float) box.minX, (float) box.minZ, (float) box.maxX, (float) box.maxZ };
			case NORTH: return new float[]{ 16f - (float) box.maxX, (float) box.minY, 16f - (float) box.minX, (float) box.maxY };
			case SOUTH: return new float[]{ (float) box.minX, (float) box.minY, (float) box.maxX, (float) box.maxY };
			case WEST:  return new float[]{ (float) box.minZ, (float) box.minY, (float) box.maxZ, (float) box.maxY };
			case EAST:  return new float[]{ 16f - (float) box.maxZ, (float) box.minY, 16f - (float) box.minZ, (float) box.maxY };
		}
	}
	
	/**
	 * A Hack to determine the UV face angle for a block column on a certain axis
	 * 
	 * @param axis
	 * @param face
	 * @return
	 */
	public static int getFaceAngle (Axis axis, EnumFacing face) {
		if(axis == Axis.Y) { //UP / DOWN
			return 0;
		}
		else if(axis == Axis.Z) {//NORTH / SOUTH
			switch(face) {
				case UP: return 0;
				case WEST: return 270;
				case DOWN: return 180;
				case NORTH: return 270;
				default: return 90;
			}
		} else { //EAST/WEST
			return (face == EnumFacing.NORTH) ? 270 : 90;
		}
	}
	
	public static float[] modUV(float[] uvs) {
		uvs[0] = (int)uvs[0] & 0xf;
		uvs[1] = (int)uvs[1] & 0xf;
		uvs[2] = (((int)uvs[2] - 1) & 0xf) + 1;
		uvs[3] = (((int)uvs[3] - 1) & 0xf) + 1;
		return uvs;
	}
	
	public static Vector3f[] AABBLimits(AxisAlignedBB aabb) {
		return new Vector3f[] {
				new Vector3f((float)aabb.minX, (float)aabb.minY, (float)aabb.minZ),
				new Vector3f((float)aabb.maxX, (float)aabb.maxY, (float)aabb.maxZ),
		};
	}
	
	public static BakedQuad makeBakedQuad(BlockPart blockPart, BlockPartFace partFace, TextureAtlasSprite atlasSprite, EnumFacing dir, net.minecraftforge.common.model.ITransformation transform, boolean uvlocked) {
		return new FaceBakery().makeBakedQuad(blockPart.positionFrom, blockPart.positionTo, partFace, atlasSprite, dir, transform, blockPart.partRotation, uvlocked, blockPart.shade);
	}
	
}
