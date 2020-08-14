package com.ferreusveritas.dynamictrees.models.bakedmodels;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.ferreusveritas.dynamictrees.client.ModelUtils;
import com.ferreusveritas.dynamictrees.models.ICustomDamageModel;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.google.common.collect.Maps;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

//@OnlyIn(Dist.CLIENT)
//public class BakedModelBlockBranchThick extends BakedModelBlockBranchBasic implements ICustomDamageModel {
//
//	private IBakedModel trunksBark[] = new IBakedModel[16];//The trunk will always feature bark on it's sides
//	private IBakedModel trunksTopBark[] = new IBakedModel[16];//The trunk will feature bark on it's top when there's more tree on it's surface
//	private IBakedModel trunksTopRings[] = new IBakedModel[16];//The trunk will feature rings on it's top when there's not any tree on it's surface(cut)
//	private IBakedModel trunksBotRings[] = new IBakedModel[16];//The trunk will always feature rings on it's bottom surface(or nothing)
//
//	public BakedModelBlockBranchThick(ResourceLocation barkRes, ResourceLocation ringsRes, ResourceLocation thickRingsRes, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
//		super(barkRes, ringsRes, bakedTextureGetter);
//
//		TextureAtlasSprite barkIcon = bakedTextureGetter.apply(barkRes);
//		TextureAtlasSprite ringIcon = bakedTextureGetter.apply(ringsRes);
//		TextureAtlasSprite thickRingIcon = bakedTextureGetter.apply(thickRingsRes);
//		barkParticles = barkIcon;
//
//		for (int i = 0; i < 16; i++) {
//			int radius = i + 9;
//			trunksBark[i] = bakeTrunkBark(radius, barkIcon, true);
//			trunksTopBark[i] = bakeTrunkBark(radius, barkIcon, false);
//			trunksTopRings[i] = bakeTrunkRings(radius, ModConfigs.fancyThickRings ? thickRingIcon : ringIcon, Direction.UP);
//			trunksBotRings[i] = bakeTrunkRings(radius, ModConfigs.fancyThickRings ? thickRingIcon : ringIcon, Direction.DOWN);
//		}
//
//	}
//
//	public IBakedModel bakeTrunkBark(int radius, TextureAtlasSprite bark, boolean side) {
//
//		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.NONE).setTexture(bark);
//		AxisAlignedBB wholeVolume = new AxisAlignedBB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);
//
//		final Direction[] run = side ? Direction.HORIZONTALS : new Direction[] { Direction.UP, Direction.DOWN };
//		ArrayList<Vec3i> offsets = new ArrayList<>();
//
//		for (Surround dir: Surround.values()) {
//			offsets.add(dir.getOffset());//8 surrounding component pieces
//		}
//		offsets.add(new Vec3i(0, 0, 0));//Center
//
//		for (Direction face: run) {
//			for (Vec3i offset : offsets) {
//				if (face.getAxis() == Axis.Y || new Vec3d(face.getDirectionVec()).add(new Vec3d(offset)).lengthSquared() > 2.25) { //This means that the dir and face share a common direction
//					Vec3d scaledOffset = new Vec3d(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16);//Scale the dimensions to match standard minecraft texels
//					AxisAlignedBB partBoundary = new AxisAlignedBB(0, 0, 0, 16, 16, 16).offset(scaledOffset).intersect(wholeVolume);
//
//					Vector3f limits[] = ModelUtils.AABBLimits(partBoundary);
//
//					Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);
//
//					BlockFaceUV uvface = new BlockFaceUV(ModelUtils.modUV(ModelUtils.getUVs(partBoundary, face)), getFaceAngle(Axis.Y, face));
//					mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
//
//					BlockPart part = new BlockPart(limits[0], limits[1], mapFacesIn, null, true);
//					builder.addFaceQuad(face, ModelUtils.makeBakedQuad(part, part.mapFaces.get(face), bark, face, ModelRotation.X0_Y0, false));
//				}
//
//			}
//		}
//
//		return builder.makeBakedModel();
//	}
//
//	public IBakedModel bakeTrunkRings(int radius, TextureAtlasSprite ring, Direction face) {
//		return bakeTrunkRings(radius, ring, face, ModConfigs.fancyThickRings);
//	}
//
//	public IBakedModel bakeTrunkRings(int radius, TextureAtlasSprite ring, Direction face, boolean fancy) {
//		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.NONE).setTexture(ring);
//		AxisAlignedBB wholeVolume = new AxisAlignedBB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);
//		int wholeVolumeWidth = fancy ? 48 : radius * 2;
//
//		ArrayList<Vec3i> offsets = new ArrayList<>();
//
//		for (Surround dir: Surround.values()) {
//			offsets.add(dir.getOffset()); // 8 surrounding component pieces
//		}
//		offsets.add(new Vec3i(0, 0, 0)); // Center
//
//		for (Vec3i offset : offsets) {
//			Vec3d scaledOffset = new Vec3d(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16); // Scale the dimensions to match standard minecraft texels
//			AxisAlignedBB partBoundary = new AxisAlignedBB(0, 0, 0, 16, 16, 16).offset(scaledOffset).intersect(wholeVolume);
//
//			Vector3f posFrom = new Vector3f((float) partBoundary.minX, (float) partBoundary.minY, (float) partBoundary.minZ);
//			Vector3f posTo = new Vector3f((float) partBoundary.maxX, (float) partBoundary.maxY, (float) partBoundary.maxZ);
//
//			Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);
//			float textureOffsetX = fancy ? (float) (-16f) : (float) wholeVolume.minX;
//			float textureOffsetZ = fancy ? (float) (-16f) : (float) wholeVolume.minZ;
//
//			float minX = ((float) ((partBoundary.minX - textureOffsetX) / wholeVolumeWidth)) * 16f;
//			float maxX = ((float) ((partBoundary.maxX - textureOffsetX) / wholeVolumeWidth)) * 16f;
//			float minZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
//			float maxZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
//
//			if (face == Direction.DOWN) {
//				minZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
//				maxZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
//			}
//
//			float[] uvs = new float[]{ minX, minZ, maxX, maxZ };
//
//			BlockFaceUV uvface = new BlockFaceUV(uvs, getFaceAngle(Axis.Y, face));
//			mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
//
//			BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
//			builder.addFaceQuad(face, ModelUtils.makeBakedQuad(part, part.mapFaces.get(face), ring, face, ModelRotation.X0_Y0, false));
//		}
//
//		return builder.makeBakedModel();
//	}
//
//	@Override
//	public List<BakedQuad> getQuads(BlockState blockState, Direction side, long rand) {
//		int coreRadius = getRadius(blockState);
//
//		if (coreRadius <= BlockBranch.RADMAX_NORMAL) {
//			return super.getQuads(blockState, side, rand);
//		}
//
//		coreRadius = MathHelper.clamp(coreRadius, 9, 24);
//
//		List<BakedQuad> quadsList = new ArrayList<>(30);
//		quadsList.addAll(trunksBark[coreRadius-9].getQuads(blockState, side, rand));
//
//		if (blockState instanceof BlockState) {
//			BlockState extendedBlockState = (BlockState) blockState;
//			int[] connections = pollConnections(coreRadius, extendedBlockState);
//
//			if (connections[0] < 1) {
//				quadsList.addAll(trunksBotRings[coreRadius-9].getQuads(blockState, side, rand));
//			}
//			if (connections[1] < 1) {
//				quadsList.addAll(trunksTopRings[coreRadius-9].getQuads(blockState, side, rand));
//			} else if (connections[1] == 1 && side == Direction.UP) {
//				quadsList.addAll(trunksTopBark[coreRadius-9].getQuads(blockState, side, rand));
//			}
//
//		}
//
//		return quadsList;
//	}
//
//	@Override
//	public List<BakedQuad> getCustomDamageQuads(BlockState blockState, Direction side, long rand) {
//		int coreRadius = getRadius(blockState);
//
//		if (coreRadius <= BlockBranch.RADMAX_NORMAL) {
//			return super.getQuads(blockState, side, rand);
//		}
//
//		coreRadius = MathHelper.clamp(coreRadius, 9, 24);
//
//		List<BakedQuad> quadsList = new LinkedList<BakedQuad>();
//
//		quadsList.addAll(trunksBark[coreRadius - 9].getQuads(blockState, side, rand));
//		quadsList.addAll(trunksTopBark[coreRadius - 9].getQuads(blockState, side, rand));
//
//		return quadsList;
//	}
//
//}
