package com.ferreusveritas.dynamictrees.models.bakedmodels;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.branches.ThickBranchBlock;
import com.ferreusveritas.dynamictrees.client.ModelUtils;
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
import java.util.concurrent.atomic.AtomicInteger;

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

	private boolean isTextureNull (@Nullable TextureAtlasSprite sprite){
		return sprite == null || sprite.equals(ModelUtils.getTexture(new ResourceLocation("")));
	}

	@Override
	public void setupModels() {
		super.setupModels();

		TextureAtlasSprite thickRingsTexture = ModelUtils.getTexture(this.thickRingsResLoc);

		//if (isTextureNull(thickRingsTexture)) {
			//thickRingsTexture = ThickRingTextureManager.uploader.getTextureAtlas().getSprite(thickRingsResLoc);
			//thickRingsTexture = ModelUtils.getTexture(thickRingsResLoc, ThickRingTextureManager.LOCATION_THICKRINGS_TEXTURE);

			if (isTextureNull(thickRingsTexture)){
				thickRingsTexture = this.ringsTexture;
			}
		//}
		
		for (int i = 0; i < ThickBranchBlock.MAX_RADIUS_TICK -ThickBranchBlock.MAX_RADIUS; i++) {
			final int radius = i + ThickBranchBlock.MAX_RADIUS + 1;
			this.trunksBark[i] = this.bakeTrunkBark(radius, this.barkTexture, true);
			this.trunksTopBark[i] = this.bakeTrunkBark(radius, this.barkTexture, false);
			this.trunksTopRings[i] = this.bakeTrunkRings(radius,  thickRingsTexture, Direction.UP);
			this.trunksBotRings[i] = this.bakeTrunkRings(radius,  thickRingsTexture, Direction.DOWN);
		}
	}

	public IBakedModel bakeTrunkBark(int radius, TextureAtlasSprite bark, boolean side) {

		final SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(this.blockModel.customData, ItemOverrideList.EMPTY).particle(bark);
		final AxisAlignedBB wholeVolume = new AxisAlignedBB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);

		final Direction[] run = side ? CoordUtils.HORIZONTALS : new Direction[] { Direction.UP, Direction.DOWN };
		final ArrayList<Vector3i> offsets = new ArrayList<>();

		for (Surround dir: Surround.values()) {
			offsets.add(dir.getOffset()); // 8 surrounding component pieces.
		}
		offsets.add(new Vector3i(0, 0, 0)); // Center

		for (final Direction face: run) {
			final Vector3i dirVector = face.getNormal();

			for (Vector3i offset : offsets) {
				if (face.getAxis() != Axis.Y ||
						new Vector3d(dirVector.getX(), dirVector.getY(), dirVector.getZ())
								.add(new Vector3d(offset.getX(), offset.getY(), offset.getZ()))
								.lengthSqr() <= 2.25)
					continue;

				// This means that the dir and face share a common direction.
				final Vector3d scaledOffset = new Vector3d(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16);//Scale the dimensions to match standard minecraft texels
				final AxisAlignedBB partBoundary = new AxisAlignedBB(0, 0, 0, 16, 16, 16).move(scaledOffset).intersect(wholeVolume);

				final Vector3f[] limits = ModelUtils.AABBLimits(partBoundary);

				final Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);

				final BlockFaceUV uvface = new BlockFaceUV(ModelUtils.modUV(ModelUtils.getUVs(partBoundary, face)), getFaceAngle(Axis.Y, face));
				mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));

				final BlockPart part = new BlockPart(limits[0], limits[1], mapFacesIn, null, true);
				builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, part.faces.get(face), bark, face, ModelRotation.X0_Y0, this.modelResLoc));
			}
		}

		return builder.build();
	}

	public IBakedModel bakeTrunkRings(int radius, TextureAtlasSprite ring, Direction face) {
		final SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(this.blockModel.customData, ItemOverrideList.EMPTY).particle(ring);
		final AxisAlignedBB wholeVolume = new AxisAlignedBB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);
		final int wholeVolumeWidth = 48;

		final ArrayList<Vector3i> offsets = new ArrayList<>();

		for (Surround dir: Surround.values()) {
			offsets.add(dir.getOffset()); // 8 surrounding component pieces.
		}
		offsets.add(new Vector3i(0, 0, 0)); // Center

		for (final Vector3i offset : offsets) {
			final Vector3d scaledOffset = new Vector3d(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16); // Scale the dimensions to match standard minecraft texels.
			final AxisAlignedBB partBoundary = new AxisAlignedBB(0, 0, 0, 16, 16, 16).move(scaledOffset).intersect(wholeVolume);

			final Vector3f posFrom = new Vector3f((float) partBoundary.minX, (float) partBoundary.minY, (float) partBoundary.minZ);
			final Vector3f posTo = new Vector3f((float) partBoundary.maxX, (float) partBoundary.maxY, (float) partBoundary.maxZ);

			final Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);
			final float textureOffsetX = -16f;
			final float textureOffsetZ = -16f;

			final float minX = ((float) ((partBoundary.minX - textureOffsetX) / wholeVolumeWidth)) * 16f;
			final float maxX = ((float) ((partBoundary.maxX - textureOffsetX) / wholeVolumeWidth)) * 16f;
			float minZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
			float maxZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;

			if (face == Direction.DOWN) {
				minZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
				maxZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
			}

			final float[] uvs = new float[]{ minX, minZ, maxX, maxZ };

			final BlockFaceUV uvface = new BlockFaceUV(uvs, getFaceAngle(Axis.Y, face));
			mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));

			final BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
			builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, part.faces.get(face), ring, face, ModelRotation.X0_Y0, this.modelResLoc));
		}

		return builder.build();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final Random rand, final IModelData extraData) {
		if (state == null || side != null)
			return Collections.emptyList();

		int coreRadius = this.getRadius(state);

		if (coreRadius <= BranchBlock.MAX_RADIUS)
			return super.getQuads(state, null, rand, extraData);

		coreRadius = MathHelper.clamp(coreRadius, 9, 24);

		final List<BakedQuad> quads = new ArrayList<>(30);

		int[] connections = new int[] {0,0,0,0,0,0};
		Direction forceRingDir = null;
		final AtomicInteger twigRadius = new AtomicInteger(1);

		if (extraData instanceof ModelConnections) {
			final ModelConnections connectionsData = (ModelConnections) extraData;
			connections = connectionsData.getAllRadii();
			forceRingDir = connectionsData.getRingOnly();

			connectionsData.getFamily().ifValid(family ->
					twigRadius.set(family.getPrimaryThickness()));
		}

		// Count number of connections.
		int numConnections = 0;
		for (int i: connections) {
			numConnections += (i != 0) ? 1 : 0;
		}

		if (numConnections == 0 && forceRingDir != null) {
			return quads;
		}

		if (forceRingDir != null) {
			connections[forceRingDir.get3DDataValue()] = 0;
			quads.addAll(this.trunksBotRings[coreRadius - 9].getQuads(state, forceRingDir, rand, extraData));
		}

		for (Direction face : Direction.values()) {
			quads.addAll(this.trunksBark[coreRadius - 9].getQuads(state, face, rand, extraData));
			if (face == Direction.UP || face == Direction.DOWN) {
				if (connections[face.get3DDataValue()] < twigRadius.get()) {
					quads.addAll(this.trunksTopRings[coreRadius - 9].getQuads(state, face, rand, extraData));
				} else if (connections[face.get3DDataValue()] < coreRadius) {
					quads.addAll(this.trunksTopBark[coreRadius - 9].getQuads(state, face, rand, extraData));
				}
			}
		}

		return quads;
	}

}
