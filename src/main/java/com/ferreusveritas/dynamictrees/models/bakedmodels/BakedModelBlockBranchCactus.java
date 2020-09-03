package com.ferreusveritas.dynamictrees.models.bakedmodels;

//import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
//import com.ferreusveritas.dynamictrees.blocks.BlockBranchCactus;
//import com.ferreusveritas.dynamictrees.client.ModelUtils;
//import com.google.common.collect.Maps;
//import com.google.common.primitives.Ints;
//import net.minecraft.block.BlockState;
//import net.minecraft.client.renderer.Vector3f;
//import net.minecraft.client.renderer.model.*;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import net.minecraft.state.IProperty;
//import net.minecraft.util.Direction;
//import net.minecraft.util.Direction.Axis;
//import net.minecraft.util.Direction.AxisDirection;
//import net.minecraft.util.ResourceLocation;
//import net.minecraft.util.math.MathHelper;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import net.minecraftforge.client.model.data.IDynamicBakedModel;
//import net.minecraftforge.client.model.data.IModelData;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.function.Function;
//
//@OnlyIn(Dist.CLIENT)
//public class BakedModelBlockBranchCactus implements IDynamicBakedModel {
//
//    protected BlockModel modelBlock;
//
//	TextureAtlasSprite barkParticles;
//
//	// Not as many baked models as normal branches, although each model has more quads. Still less quads in total, though.
//	private IBakedModel sleeves[][] = new IBakedModel[6][2];
//	private IBakedModel cores[][] = new IBakedModel[3][2]; // 2 Cores for 3 axis with the bark texture all all 6 sides rotated appropriately.
//	private IBakedModel rings[] = new IBakedModel[2]; // 2 Cores with the ring textures on all 6 sides
//	private IBakedModel coreSpikes[] = new IBakedModel[2]; // 2 cores with only the spikey edges
//	private IBakedModel sleeveTopSpikes;
//
//	public BakedModelBlockBranchCactus(ResourceLocation barkRes, ResourceLocation ringsRes, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
//		this.modelBlock = new BlockModel(null, null, null, false, false, ItemCameraTransforms.DEFAULT, null);
//
//		TextureAtlasSprite barkIcon = bakedTextureGetter.apply(barkRes);
//		TextureAtlasSprite ringIcon = bakedTextureGetter.apply(ringsRes);
//		barkParticles = barkIcon;
//
//		for (int i = 0; i < 2; i++) {
//			int radius = i + 4;
//
//			for (Direction dir: Direction.values()) {
//				sleeves[dir.getIndex()][i] = bakeSleeve(radius, dir, barkIcon, ringIcon);
//			}
//
//			cores[0][i] = bakeCore(radius, Axis.Y, barkIcon); //DOWN<->UP
//			cores[1][i] = bakeCore(radius, Axis.Z, barkIcon); //NORTH<->SOUTH
//			cores[2][i] = bakeCore(radius, Axis.X, barkIcon); //WEST<->EAST
//
//			rings[i] = bakeCore(radius, Axis.Y, ringIcon);
//
//			coreSpikes[i] = bakeCoreSpikes(radius, barkIcon);
//			sleeveTopSpikes = bakeTopSleeveSpikes(barkIcon);
//		}
//	}
//
//	public IBakedModel bakeSleeve(int radius, Direction dir, TextureAtlasSprite bark, TextureAtlasSprite top) {
//		// Work in double units(*2)
//		int dradius = radius * 2;
//		int halfSize = (16 - dradius) / 2;
//		int halfSizeX = dir.getXOffset() != 0 ? halfSize : dradius;
//		int halfSizeY = dir.getYOffset() != 0 ? halfSize : dradius;
//		int halfSizeZ = dir.getZOffset() != 0 ? halfSize : dradius;
//		int move = 16 - halfSize;
//		int centerX = 16 + (dir.getXOffset() * move);
//		int centerY = 16 + (dir.getYOffset() * move);
//		int centerZ = 16 + (dir.getZOffset() * move);
//
//		Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, (centerY - halfSizeY) / 2f, (centerZ - halfSizeZ) / 2f);
//		Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, (centerY + halfSizeY) / 2f, (centerZ + halfSizeZ) / 2f);
//
//		boolean negative = dir.getAxisDirection() == AxisDirection.NEGATIVE;
//		if (dir.getAxis() == Axis.Z) { // North/South
//			negative = !negative;
//		}
//
//		Map<Direction, BlockPartFace> mapFacesIn = Maps.newEnumMap(Direction.class);
//
//		for (Direction face: Direction.values()) {
//			if (dir.getOpposite() != face) { // Discard side of sleeve that faces core
//				BlockFaceUV uvface = null;
//				if (dir == face) { // Side of sleeve that faces away from core
//					if (radius == 4 || (radius == 5 && dir == Direction.DOWN)) {
//						uvface = new BlockFaceUV(new float[] {8 - radius, 8 - radius, 8 + radius, 8 + radius}, 0);
//					}
//				} else { // UV for Bark texture
//					uvface = new BlockFaceUV(new float[]{ 8 - radius, negative ? 16 - halfSize : 0, 8 + radius, negative ? 16 : halfSize }, getFaceAngle(dir.getAxis(), face));
//				}
//				if (uvface != null) {
//					mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
//				}
//			}
//		}
//
//		BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
//		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.EMPTY).setTexture(bark);
//
//		for (Map.Entry<Direction, BlockPartFace> e : part.mapFaces.entrySet()) {
//			Direction face = e.getKey();
//			builder.addFaceQuad(face, ModelUtils.makeBakedQuad(part, e.getValue(), (dir == face) ? top : bark, face, ModelRotation.X0_Y0, false));
//		}
//		float minV = negative ? 16 - halfSize : 0;
//		float maxV = negative ? 16 : halfSize;
//		switch (dir.getAxis()) {
//		case X:
//
//			builder.addFaceQuad(Direction.NORTH, new BakedQuad(Ints.concat(
//					vertexToInts(posTo.getX() / 16f, posTo.getY() / 16f + 0.0625f, posFrom.getZ() / 16f - 0.002f, 0xFFFFFFFF, bark, 16, minV),
//					vertexToInts(posTo.getX() / 16f, posTo.getY() / 16f - 0.0625f, posFrom.getZ() / 16f - 0.002f, 0xFFFFFFFF, bark, 14, minV),
//					vertexToInts(posFrom.getX() / 16f, posTo.getY() / 16f - 0.0625f, posFrom.getZ() / 16f - 0.002f, 0xFFFFFFFF, bark, 14, maxV),
//					vertexToInts(posFrom.getX() / 16f, posTo.getY() / 16f + 0.0625f, posFrom.getZ() / 16f - 0.002f, 0xFFFFFFFF, bark, 16, maxV)
//			), 0, Direction.NORTH, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.NORTH, new BakedQuad(Ints.concat(
//					vertexToInts(posTo.getX() / 16f, posFrom.getY() / 16f + 0.0625f, posFrom.getZ() / 16f - 0.002f, 0xFFFFFFFF, bark, 2, minV),
//					vertexToInts(posTo.getX() / 16f, posFrom.getY() / 16f - 0.0625f, posFrom.getZ() / 16f - 0.002f, 0xFFFFFFFF, bark, 0, minV),
//					vertexToInts(posFrom.getX() / 16f, posFrom.getY() / 16f - 0.0625f, posFrom.getZ() / 16f - 0.002f, 0xFFFFFFFF, bark, 0, maxV),
//					vertexToInts(posFrom.getX() / 16f, posFrom.getY() / 16f + 0.0625f, posFrom.getZ() / 16f - 0.002f, 0xFFFFFFFF, bark, 2, maxV)
//			), 0, Direction.NORTH, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.SOUTH, new BakedQuad(Ints.concat(
//					vertexToInts(posFrom.getX() / 16f, posTo.getY() / 16f + 0.0625f, posTo.getZ() / 16f + 0.002f, 0xFFFFFFFF, bark, 16, maxV),
//					vertexToInts(posFrom.getX() / 16f, posTo.getY() / 16f - 0.0625f, posTo.getZ() / 16f + 0.002f, 0xFFFFFFFF, bark, 14, maxV),
//					vertexToInts(posTo.getX() / 16f, posTo.getY() / 16f - 0.0625f, posTo.getZ() / 16f + 0.002f, 0xFFFFFFFF, bark, 14, minV),
//					vertexToInts(posTo.getX() / 16f, posTo.getY() / 16f + 0.0625f, posTo.getZ() / 16f + 0.002f, 0xFFFFFFFF, bark, 16, minV)
//			), 0, Direction.SOUTH, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.SOUTH, new BakedQuad(Ints.concat(
//					vertexToInts(posFrom.getX() / 16f, posFrom.getY() / 16f + 0.0625f, posTo.getZ() / 16f + 0.002f, 0xFFFFFFFF, bark, 2, maxV),
//					vertexToInts(posFrom.getX() / 16f, posFrom.getY() / 16f - 0.0625f, posTo.getZ() / 16f + 0.002f, 0xFFFFFFFF, bark, 0, maxV),
//					vertexToInts(posTo.getX() / 16f, posFrom.getY() / 16f - 0.0625f, posTo.getZ() / 16f + 0.002f, 0xFFFFFFFF, bark, 0, minV),
//					vertexToInts(posTo.getX() / 16f, posFrom.getY() / 16f + 0.0625f, posTo.getZ() / 16f + 0.002f, 0xFFFFFFFF, bark, 2, minV)
//			), 0, Direction.SOUTH, bark, true, DefaultVertexFormats.BLOCK));
//
//			builder.addFaceQuad(Direction.DOWN, new BakedQuad(Ints.concat(
//					vertexToInts(posTo.getX() / 16f, posFrom.getY() / 16f - 0.002f, posTo.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 14, minV),
//					vertexToInts(posTo.getX() / 16f, posFrom.getY() / 16f - 0.002f, posTo.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 16, minV),
//					vertexToInts(posFrom.getX() / 16f, posFrom.getY() / 16f - 0.002f, posTo.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 16, maxV),
//					vertexToInts(posFrom.getX() / 16f, posFrom.getY() / 16f - 0.002f, posTo.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 14, maxV)
//			), 0, Direction.DOWN, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.DOWN, new BakedQuad(Ints.concat(
//					vertexToInts(posTo.getX() / 16f, posFrom.getY() / 16f - 0.002f, posFrom.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 0, minV),
//					vertexToInts(posTo.getX() / 16f, posFrom.getY() / 16f - 0.002f, posFrom.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 2, minV),
//					vertexToInts(posFrom.getX() / 16f, posFrom.getY() / 16f - 0.002f, posFrom.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 2, maxV),
//					vertexToInts(posFrom.getX() / 16f, posFrom.getY() / 16f - 0.002f, posFrom.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 0, maxV)
//			), 0, Direction.DOWN, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.UP, new BakedQuad(Ints.concat(
//					vertexToInts(posFrom.getX() / 16f, posTo.getY() / 16f + 0.002f, posTo.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 14, maxV),
//					vertexToInts(posFrom.getX() / 16f, posTo.getY() / 16f + 0.002f, posTo.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 16, maxV),
//					vertexToInts(posTo.getX() / 16f, posTo.getY() / 16f + 0.002f, posTo.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 16, minV),
//					vertexToInts(posTo.getX() / 16f, posTo.getY() / 16f + 0.002f, posTo.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 14, minV)
//			), 0, Direction.UP, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.UP, new BakedQuad(Ints.concat(
//					vertexToInts(posFrom.getX() / 16f, posTo.getY() / 16f + 0.002f, posFrom.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 0, maxV),
//					vertexToInts(posFrom.getX() / 16f, posTo.getY() / 16f + 0.002f, posFrom.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 2, maxV),
//					vertexToInts(posTo.getX() / 16f, posTo.getY() / 16f + 0.002f, posFrom.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 2, minV),
//					vertexToInts(posTo.getX() / 16f, posTo.getY() / 16f + 0.002f, posFrom.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 0, minV)
//			), 0, Direction.UP, bark, true, DefaultVertexFormats.BLOCK));
//
//			break;
//		case Y:
//
//			builder.addFaceQuad(Direction.WEST, new BakedQuad(Ints.concat(
//					vertexToInts(posFrom.getX() / 16f - 0.001f, posTo.getY() / 16f, posTo.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 16, minV),
//					vertexToInts(posFrom.getX() / 16f - 0.001f, posTo.getY() / 16f, posTo.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 14, minV),
//					vertexToInts(posFrom.getX() / 16f - 0.001f, posFrom.getY() / 16f, posTo.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 14, maxV),
//					vertexToInts(posFrom.getX() / 16f - 0.001f, posFrom.getY() / 16f, posTo.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 16, maxV)
//			), 0, Direction.WEST, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.WEST, new BakedQuad(Ints.concat(
//					vertexToInts(posFrom.getX() / 16f - 0.001f, posTo.getY() / 16f, posFrom.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 2, minV),
//					vertexToInts(posFrom.getX() / 16f - 0.001f, posTo.getY() / 16f, posFrom.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 0, minV),
//					vertexToInts(posFrom.getX() / 16f - 0.001f, posFrom.getY() / 16f, posFrom.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 0, maxV),
//					vertexToInts(posFrom.getX() / 16f - 0.001f, posFrom.getY() / 16f, posFrom.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 2, maxV)
//			), 0, Direction.WEST, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.EAST, new BakedQuad(Ints.concat(
//					vertexToInts(posTo.getX() / 16f + 0.001f, posFrom.getY() / 16f, posTo.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 16, maxV),
//					vertexToInts(posTo.getX() / 16f + 0.001f, posFrom.getY() / 16f, posTo.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 14, maxV),
//					vertexToInts(posTo.getX() / 16f + 0.001f, posTo.getY() / 16f, posTo.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 14, minV),
//					vertexToInts(posTo.getX() / 16f + 0.001f, posTo.getY() / 16f, posTo.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 16, minV)
//			), 0, Direction.EAST, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.EAST, new BakedQuad(Ints.concat(
//					vertexToInts(posTo.getX() / 16f + 0.001f, posFrom.getY() / 16f, posFrom.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 2, maxV),
//					vertexToInts(posTo.getX() / 16f + 0.001f, posFrom.getY() / 16f, posFrom.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 0, maxV),
//					vertexToInts(posTo.getX() / 16f + 0.001f, posTo.getY() / 16f, posFrom.getZ() / 16f - 0.0625f, 0xFFFFFFFF, bark, 0, minV),
//					vertexToInts(posTo.getX() / 16f + 0.001f, posTo.getY() / 16f, posFrom.getZ() / 16f + 0.0625f, 0xFFFFFFFF, bark, 2, minV)
//			), 0, Direction.EAST, bark, true, DefaultVertexFormats.BLOCK));
//
//			builder.addFaceQuad(Direction.NORTH, new BakedQuad(Ints.concat(
//					vertexToInts(posTo.getX() / 16f + 0.0625f, posFrom.getY() / 16f, posFrom.getZ() / 16f - 0.001f, 0xFFFFFFFF, bark, 16, maxV),
//					vertexToInts(posTo.getX() / 16f - 0.0625f, posFrom.getY() / 16f, posFrom.getZ() / 16f - 0.001f, 0xFFFFFFFF, bark, 14, maxV),
//					vertexToInts(posTo.getX() / 16f - 0.0625f, posTo.getY() / 16f, posFrom.getZ() / 16f - 0.001f, 0xFFFFFFFF, bark, 14, minV),
//					vertexToInts(posTo.getX() / 16f + 0.0625f, posTo.getY() / 16f, posFrom.getZ() / 16f - 0.001f, 0xFFFFFFFF, bark, 16, minV)
//			), 0, Direction.NORTH, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.NORTH, new BakedQuad(Ints.concat(
//					vertexToInts(posFrom.getX() / 16f + 0.0625f, posFrom.getY() / 16f, posFrom.getZ() / 16f - 0.001f, 0xFFFFFFFF, bark, 2, maxV),
//					vertexToInts(posFrom.getX() / 16f - 0.0625f, posFrom.getY() / 16f, posFrom.getZ() / 16f - 0.001f, 0xFFFFFFFF, bark, 0, maxV),
//					vertexToInts(posFrom.getX() / 16f - 0.0625f, posTo.getY() / 16f, posFrom.getZ() / 16f - 0.001f, 0xFFFFFFFF, bark, 0, minV),
//					vertexToInts(posFrom.getX() / 16f + 0.0625f, posTo.getY() / 16f, posFrom.getZ() / 16f - 0.001f, 0xFFFFFFFF, bark, 2, minV)
//			), 0, Direction.NORTH, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.SOUTH, new BakedQuad(Ints.concat(
//					vertexToInts(posTo.getX() / 16f + 0.0625f, posTo.getY() / 16f, posTo.getZ() / 16f + 0.001f, 0xFFFFFFFF, bark, 16, minV),
//					vertexToInts(posTo.getX() / 16f - 0.0625f, posTo.getY() / 16f, posTo.getZ() / 16f + 0.001f, 0xFFFFFFFF, bark, 14, minV),
//					vertexToInts(posTo.getX() / 16f - 0.0625f, posFrom.getY() / 16f, posTo.getZ() / 16f + 0.001f, 0xFFFFFFFF, bark, 14, maxV),
//					vertexToInts(posTo.getX() / 16f + 0.0625f, posFrom.getY() / 16f, posTo.getZ() / 16f + 0.001f, 0xFFFFFFFF, bark, 16, maxV)
//			), 0, Direction.SOUTH, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.SOUTH, new BakedQuad(Ints.concat(
//					vertexToInts(posFrom.getX() / 16f + 0.0625f, posTo.getY() / 16f, posTo.getZ() / 16f + 0.001f, 0xFFFFFFFF, bark, 2, minV),
//					vertexToInts(posFrom.getX() / 16f - 0.0625f, posTo.getY() / 16f, posTo.getZ() / 16f + 0.001f, 0xFFFFFFFF, bark, 0, minV),
//					vertexToInts(posFrom.getX() / 16f - 0.0625f, posFrom.getY() / 16f, posTo.getZ() / 16f + 0.001f, 0xFFFFFFFF, bark, 0, maxV),
//					vertexToInts(posFrom.getX() / 16f + 0.0625f, posFrom.getY() / 16f, posTo.getZ() / 16f + 0.001f, 0xFFFFFFFF, bark, 2, maxV)
//			), 0, Direction.SOUTH, bark, true, DefaultVertexFormats.BLOCK));
//
//			break;
//		case Z:
//
//			builder.addFaceQuad(Direction.WEST, new BakedQuad(Ints.concat(
//					vertexToInts(posFrom.getX() / 16f - 0.002f, posTo.getY() / 16f + 0.0625f, posFrom.getZ() / 16f, 0xFFFFFFFF, bark, 16, minV),
//					vertexToInts(posFrom.getX() / 16f - 0.002f, posTo.getY() / 16f - 0.0625f, posFrom.getZ() / 16f, 0xFFFFFFFF, bark, 14, minV),
//					vertexToInts(posFrom.getX() / 16f - 0.002f, posTo.getY() / 16f - 0.0625f, posTo.getZ() / 16f, 0xFFFFFFFF, bark, 14, maxV),
//					vertexToInts(posFrom.getX() / 16f - 0.002f, posTo.getY() / 16f + 0.0625f, posTo.getZ() / 16f, 0xFFFFFFFF, bark, 16, maxV)
//			), 0, Direction.WEST, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.WEST, new BakedQuad(Ints.concat(
//					vertexToInts(posFrom.getX() / 16f - 0.002f, posFrom.getY() / 16f + 0.0625f, posFrom.getZ() / 16f, 0xFFFFFFFF, bark, 2, minV),
//					vertexToInts(posFrom.getX() / 16f - 0.002f, posFrom.getY() / 16f - 0.0625f, posFrom.getZ() / 16f, 0xFFFFFFFF, bark, 0, minV),
//					vertexToInts(posFrom.getX() / 16f - 0.002f, posFrom.getY() / 16f - 0.0625f, posTo.getZ() / 16f, 0xFFFFFFFF, bark, 0, maxV),
//					vertexToInts(posFrom.getX() / 16f - 0.002f, posFrom.getY() / 16f + 0.0625f, posTo.getZ() / 16f, 0xFFFFFFFF, bark, 2, maxV)
//			), 0, Direction.WEST, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.EAST, new BakedQuad(Ints.concat(
//					vertexToInts(posTo.getX() / 16f + 0.002f, posTo.getY() / 16f + 0.0625f, posTo.getZ() / 16f, 0xFFFFFFFF, bark, 16, maxV),
//					vertexToInts(posTo.getX() / 16f + 0.002f, posTo.getY() / 16f - 0.0625f, posTo.getZ() / 16f, 0xFFFFFFFF, bark, 14, maxV),
//					vertexToInts(posTo.getX() / 16f + 0.002f, posTo.getY() / 16f - 0.0625f, posFrom.getZ() / 16f, 0xFFFFFFFF, bark, 14, minV),
//					vertexToInts(posTo.getX() / 16f + 0.002f, posTo.getY() / 16f + 0.0625f, posFrom.getZ() / 16f, 0xFFFFFFFF, bark, 16, minV)
//			), 0, Direction.EAST, bark, true, DefaultVertexFormats.BLOCK));
//			builder.addFaceQuad(Direction.EAST, new BakedQuad(Ints.concat(
//					vertexToInts(posTo.getX() / 16f + 0.002f, posFrom.getY() / 16f + 0.0625f, posTo.getZ() / 16f, 0xFFFFFFFF, bark, 2, maxV),
//					vertexToInts(posTo.getX() / 16f + 0.002f, posFrom.getY() / 16f - 0.0625f, posTo.getZ() / 16f, 0xFFFFFFFF, bark, 0, maxV),
//					vertexToInts(posTo.getX() / 16f + 0.002f, posFrom.getY() / 16f - 0.0625f, posFrom.getZ() / 16f, 0xFFFFFFFF, bark, 0, minV),
//					vertexToInts(posTo.getX() / 16f + 0.002f, posFrom.getY() / 16f + 0.0625f, posFrom.getZ() / 16f, 0xFFFFFFFF, bark, 2, minV)
//			), 0, Direction.EAST, bark, true, DefaultVertexFormats.BLOCK));
//
//			builder.addFaceQuad(Direction.DOWN, new BakedQuad(Ints.concat(
//					vertexToInts(posTo.getX() / 16f + 0.0625f, posFrom.getY() / 16f - 0.001f, posTo.getZ() / 16f, 0xFFFFFFFF, bark, 16, maxV),
//					vertexToInts(posTo.getX() / 16f - 0.0625f, posFrom.getY() / 16f - 0.001f, posTo.getZ() / 16f, 0xFFFFFFFF, bark, 14, maxV),
//					vertexToInts(posTo.getX() / 16f - 0.s