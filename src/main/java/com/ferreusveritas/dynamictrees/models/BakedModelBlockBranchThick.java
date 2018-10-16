package com.ferreusveritas.dynamictrees.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.lwjgl.util.vector.Vector3f;

import com.ferreusveritas.dynamictrees.ModConfigs;
import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BakedModelBlockBranchThick extends BakedModelBlockBranchBasic {
	
	private IBakedModel trunksBark[] = new IBakedModel[16];//The trunk will always feature bark on it's sides
	private IBakedModel trunksTopBark[] = new IBakedModel[16];//The trunk will feature bark on it's top when there's more tree on it's surface 
	private IBakedModel trunksTopRings[] = new IBakedModel[16];//The trunk will feature rings on it's top when there's not any tree on it's surface(cut)
	private IBakedModel trunksBotRings[] = new IBakedModel[16];//The trunk will always feature rings on it's bottom surface(or nothing)
	
	public BakedModelBlockBranchThick(ResourceLocation barkRes, ResourceLocation ringsRes, ResourceLocation thickRingsRes, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		super(barkRes, ringsRes, bakedTextureGetter);
		
		TextureAtlasSprite barkIcon = bakedTextureGetter.apply(barkRes);
		TextureAtlasSprite ringIcon = bakedTextureGetter.apply(ringsRes);
		TextureAtlasSprite thickRingIcon = bakedTextureGetter.apply(thickRingsRes);
		barkParticles = barkIcon;
		
		for(int i = 0; i < 16; i++) {
			int radius = i + 9;
			trunksBark[i] = bakeTrunkBark(radius, barkIcon, true);
			trunksTopBark[i] = bakeTrunkBark(radius, barkIcon, false);
			trunksTopRings[i] = bakeTrunkRings(radius, ModConfigs.fancyThickRings ? thickRingIcon : ringIcon, EnumFacing.UP);
			trunksBotRings[i] = bakeTrunkRings(radius, ModConfigs.fancyThickRings ? thickRingIcon : ringIcon, EnumFacing.DOWN);
		}
		
	}
	
	public IBakedModel bakeTrunkBark(int radius, TextureAtlasSprite bark, boolean side) {
		
		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.NONE).setTexture(bark);
		AxisAlignedBB wholeVolume = new AxisAlignedBB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);
		
		final EnumFacing[] run = side ? EnumFacing.HORIZONTALS : new EnumFacing[] { EnumFacing.UP };
		ArrayList<Vec3i> offsets = new ArrayList<>();
		
		for(Surround dir: Surround.values()) {
			offsets.add(dir.getOffset());//8 surrounding component pieces
		}
		offsets.add(new Vec3i(0, 0, 0));//Center
		
		for(EnumFacing face: run) {
			for(Vec3i offset : offsets) {
				if(face == EnumFacing.UP || new Vec3d(face.getDirectionVec()).add(new Vec3d(offset)).lengthSquared() > 2.25) { //This means that the dir and face share a common direction
					Vec3d scaledOffset = new Vec3d(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16);//Scale the dimensions to match standard minecraft texels
					AxisAlignedBB partBoundary = new AxisAlignedBB(0, 0, 0, 16, 16, 16).offset(scaledOffset).intersect(wholeVolume);
					
					Vector3f posFrom = new Vector3f((float)partBoundary.minX, (float)partBoundary.minY, (float)partBoundary.minZ);
					Vector3f posTo = new Vector3f((float)partBoundary.maxX, (float)partBoundary.maxY, (float)partBoundary.maxZ);
					
					Map<EnumFacing, BlockPartFace> mapFacesIn = Maps.newEnumMap(EnumFacing.class);
					
					BlockFaceUV uvface = new BlockFaceUV(modUV(getUVs(partBoundary, face)), getFaceAngle(Axis.Y, face));
					mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
					
					BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
					builder.addFaceQuad(face, makeBakedQuad(part, part.mapFaces.get(face), bark, face, ModelRotation.X0_Y0, false));
				}
				
			}
		}
		
		return builder.makeBakedModel();
	}
	
	public IBakedModel bakeTrunkRings(int radius, TextureAtlasSprite ring, EnumFacing face) {
		return ModConfigs.fancyThickRings ? bakeTrunkRingsFancy(radius, ring, face) : bakeTrunkRingsStretched(radius, ring, face);
	}
	
	public IBakedModel bakeTrunkRingsFancy(int radius, TextureAtlasSprite ring, EnumFacing face) {
		return bakeTrunkRingsStretched(radius, ring, face);//TODO: Create fancy ring texture
	}
	
	public IBakedModel bakeTrunkRingsStretched(int radius, TextureAtlasSprite ring, EnumFacing face) {
		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.NONE).setTexture(ring);
		AxisAlignedBB wholeVolume = new AxisAlignedBB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);
		
		Vector3f posFrom = new Vector3f((float)wholeVolume.minX, (float)wholeVolume.minY, (float)wholeVolume.minZ);
		Vector3f posTo = new Vector3f((float)wholeVolume.maxX, (float)wholeVolume.maxY, (float)wholeVolume.maxZ);
		
		Map<EnumFacing, BlockPartFace> mapFacesIn = Maps.newEnumMap(EnumFacing.class);
				
		BlockFaceUV uvface = new BlockFaceUV(new float[] {0, 0, 16, 16}, getFaceAngle(Axis.Y, face));
		mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
		
		BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
		builder.addFaceQuad(face, makeBakedQuad(part, part.mapFaces.get(face), ring, face, ModelRotation.X0_Y0, false));
		
		return builder.makeBakedModel();
	}
	
	@Override
	public List<BakedQuad> getQuads(IBlockState blockState, EnumFacing side, long rand) {
		int coreRadius = getRadius(blockState);

		if(coreRadius <= BlockBranch.RADMAX_NORMAL) {
			return super.getQuads(blockState, side, rand);
		}
		
		coreRadius = MathHelper.clamp(coreRadius, 9, 24);
		
		List<BakedQuad> quadsList = new LinkedList<BakedQuad>();
		quadsList.addAll(trunksBark[coreRadius-9].getQuads(blockState, side, rand));
		
		if (blockState instanceof IExtendedBlockState) {
			IExtendedBlockState extendedBlockState = (IExtendedBlockState) blockState;
			int[] connections = pollConnections(coreRadius, extendedBlockState);
			
			//if (connections[0] < 1) {
				quadsList.addAll(trunksBotRings[coreRadius-9].getQuads(blockState, side, rand));
			//}
			if (connections[1] < 1) {
				quadsList.addAll(trunksTopRings[coreRadius-9].getQuads(blockState, side, rand));
			} else if (connections[1] == 1) {
				quadsList.addAll(trunksTopBark[coreRadius-9].getQuads(blockState, side, rand));
			}
				
		}
		
		return quadsList;
	}
	
}
