package com.ferreusveritas.dynamictrees.models.bakedmodels;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.ThickBranchBlock;
import com.ferreusveritas.dynamictrees.client.ModelUtils;
import com.ferreusveritas.dynamictrees.client.thickrings.ThickRingAtlasTexture;
import com.ferreusveritas.dynamictrees.client.thickrings.ThickRingTextureManager;
import com.ferreusveritas.dynamictrees.models.modeldata.ModelConnections;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.ferreusveritas.dynamictrees.util.CoordUtils.Surround;
import com.google.common.collect.Maps;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ThickBranchBlockBakedModel extends BasicBranchBlockBakedModel {

	protected final ResourceLocation thickRingsResLoc;

	private final IBakedModel[] trunksBark = new IBakedModel[16]; // The trunk will always feature bark on its sides.
	private final IBakedModel[] trunksTopBark = new IBakedModel[16]; // The trunk will feature bark on its top when there's a branch on top of it.
	private final IBakedModel[] trunksTopRings = new IBakedModel[16]; // The trunk will feature rings on its top when there's no branches on top of it.
	private final IBakedModel[] trunksBotRings = new IBakedModel[16]; // The trunk will always feature rings on its bottom surface if nothing is below it.

	public ThickBranchBlockBakedModel (ResourceLocation modelResLoc, ResourceLocation barkResLoc, ResourceLocation ringsResLoc, ResourceLocation thickRingsResLoc) {
		super(modelResLoc, barkResLoc, ringsResLoc);
		this.thickRingsResLoc = thickRingsResLoc;
	}

	private boolean isTextureNull (TextureAtlasSprite sprite){
		return sprite == null || sprite.equals(ModelUtils.getTexture(new ResourceLocation("")));
	}

	@Override
	public void setupModels() {
		super.setupModels();

		final TextureAtlasSprite ringsTexture = ModelUtils.getTexture(this.ringsResLoc);
		TextureAtlasSprite thickRingsTexture = ModelUtils.getTexture(this.thickRingsResLoc);

		//if (isTextureNull(thickRingsTexture)){
			//thickRingsTexture = ThickRingTextureManager.uploader.getTextureAtlas().getSprite(thickRingsResLoc);
			//thickRingsTexture = ModelUtils.getTexture(thickRingsResLoc, ThickRingTextureManager.LOCATION_THICKRINGS_TEXTURE);

			if (isTextureNull(thickRingsTexture)){
				thickRingsTexture = ringsTexture;
			}
		//}
		
		for (int i = 0; i < ThickBranchBlock.RADMAX_THICK-ThickBranchBlock.RADMAX_NORMAL; i++) {
			int radius = i + ThickBranchBlock.RADMAX_NORMAL + 1;
			trunksBark[i] = bakeTrunkBark(radius, this.barkTexture, true);
			trunksTopBark[i] = bakeTrunkBark(radius, this.barkTexture, false);
			trunksTopRings[i] = bakeTrunkRings(radius,  thickRingsTexture, Direction.UP);
			trunksBotRings[i] = bakeTrunkRings(radius,  thickRingsTexture, Direction.DOWN);
		}
	}

	public IBakedModel bakeTrunkBark(int radius, TextureAtlasSprite bark, boolean side) {

		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(this.blockModel.customData, ItemOverrideList.EMPTY).setTexture(bark);
		AxisAlignedBB wholeVolume = new AxisAlignedBB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);

		final Direction[] run = side ? CoordUtils.HORIZONTALS : new Direction[] { Direction.UP, Direction.DOWN };
		ArrayList<Vector3i> offsets = new ArrayList<>();

		for (Surround dir: Surround.values()) {
			offsets.add(dir.getOffset()); // 8 surrounding component pieces
		}
		offsets.add(new Vector3i(0, 0, 0));//Center

		for (Direction face: run) {
			final Vector3i dirVector = face.getDirectionVec();

			for (Vector3i offset : offsets) {
				if (face.getAxis() == Axis.Y || new Vector3d(dirVector.getX(), dirVector.getY(), dirVector.getZ()).add(new Vector3d(offset.getX(), offset.getY(), offset.getZ())).lengthSquared() > 2.25) { //This means that the dir and face share a common direction
					Vector3d scaledOffset = new Vector3d(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16);//Scale the dimensions to match standard minecraft texels
					AxisAlignedBB partBoundary = new AxisAlignedBB(0, 0, 0, 16, 16, 16).offset(scaledOffset).intersect(wholeVolume);

					Vector3f[] limits = ModelUtils.AABBLimits(partBoundary);

					Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);

					BlockFaceUV uvface = new BlockFaceUV(ModelUtils.modUV(ModelUtils.getUVs(partBoundary, face)), getFaceAngle(Axis.Y, face));
					mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));

					BlockPart part = new BlockPart(limits[0], limits[1], mapFacesIn, null, true);
					builder.addFaceQuad(face, ModelUtils.makeBakedQuad(part, part.mapFaces.get(face), bark, face, ModelRotation.X0_Y0, this.modelResLoc));
				}

			}
		}

		return builder.build();
	}

	public IBakedModel bakeTrunkRings(int radius, TextureAtlasSprite ring, Direction face) {
		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(this.blockModel.customData, ItemOverrideList.EMPTY).setTexture(ring);
		AxisAlignedBB wholeVolume = new AxisAlignedBB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);
		int wholeVolumeWidth = 48;

		ArrayList<Vector3i> offsets = new ArrayList<>();

		for (Surround dir: Surround.values()) {
			offsets.add(dir.getOffset()); // 8 surrounding component pieces
		}
		offsets.add(new Vector3i(0, 0, 0)); // Center

		for (Vector3i offset : offsets) {
			Vector3d scaledOffset = new Vector3d(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16); // Scale the dimensions to match standard minecraft texels
			AxisAlignedBB partBoundary = new AxisAlignedBB(0, 0, 0, 16, 16, 16).offset(scaledOffset).intersect(wholeVolume);

			Vector3f posFrom = new Vector3f((float) partBoundary.minX, (float) partBoundary.minY, (float) partBoundary.minZ);
			Vector3f posTo = new Vector3f((float) partBoundary.maxX, (float) partBoundary.maxY, (float) partBoundary.maxZ);

			Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);
			float textureOffsetX = -16f;
			float textureOffsetZ = -16f;

			float minX = ((float) ((partBoundary.minX - textureOffsetX) / wholeVolumeWidth)) * 16f;
			float maxX = ((float) ((partBoundary.maxX - textureOffsetX) / wholeVolumeWidth)) * 16f;
			float minZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
			float maxZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;

			if (face == Direction.DOWN) {
				minZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
				maxZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
			}

			float[] uvs = new float[]{ minX, minZ, maxX, maxZ };

			BlockFaceUV uvface = new BlockFaceUV(uvs, getFaceAngle(Axis.Y, face));
			mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));

			BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
			builder.addFaceQuad(face, ModelUtils.makeBakedQuad(part, part.mapFaces.get(face), ring, face, ModelRotation.X0_Y0, this.modelResLoc));
		}

		return builder.build();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final Random rand, final IModelData extraData) {
		if (state == null || side != null) return Collections.emptyList();

		int coreRadius = this.getRadius(state);

		if (coreRadius <= BranchBlock.RADMAX_NORMAL)
			return super.getQuads(state, null, rand, extraData);

		coreRadius = MathHelper.clamp(coreRadius, 9, 24);

		List<BakedQuad> quads = new ArrayList<>(30);

		int[] connections = new int[] {0,0,0,0,0,0};

		Direction forceRingDir = null;
		if (extraData instanceof ModelConnections){
			ModelConnections connectionsData = (ModelConnections) extraData;
			connections = connectionsData.getAllRadii();
			forceRingDir = connectionsData.getRingOnly();
		}

		//Count number of connections
		int numConnections = 0;
		for(int i: connections) {
			numConnections += (i != 0) ? 1: 0;
		}

		if (numConnections == 0 && forceRingDir != null){
			return quads;
		}

		if (forceRingDir != null){
			connections[forceRingDir.getIndex()] = 0;
			quads.addAll(this.trunksBotRings[coreRadius - 9].getQuads(state, forceRingDir, rand, extraData));
		}

		for (Direction face : Direction.values()) {
			quads.addAll(this.trunksBark[coreRadius - 9].getQuads(state, face, rand, extraData));
			if (face == Direction.UP || face == Direction.DOWN) {
				if (connections[face.getIndex()] < 1) {
					quads.addAll(this.trunksTopRings[coreRadius - 9].getQuads(state, face, rand, extraData));
				} else if (connections[face.getIndex()] < coreRadius) {
					quads.addAll(this.trunksTopBark[coreRadius - 9].getQuads(state, face, rand, extraData));
				}
			}
		}

		return quads;
	}

}
