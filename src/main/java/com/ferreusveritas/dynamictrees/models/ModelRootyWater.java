package com.ferreusveritas.dynamictrees.models;

import com.ferreusveritas.dynamictrees.blocks.BlockRootyWater;
import com.google.common.primitives.Ints;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.List;

public class ModelRootyWater implements IBakedModel {

	protected IBakedModel rootsModel;
	protected TextureAtlasSprite stillWaterTexture;
	protected TextureAtlasSprite flowWaterTexture;

	public ModelRootyWater(IBakedModel rootsModel) {
		this.rootsModel = rootsModel;
		String textureProvider = Loader.isModLoaded("aquaacrobatics") ? "aquaacrobatics" : "minecraft";
		this.stillWaterTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(textureProvider + ":blocks/water_still");
		this.flowWaterTexture = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(textureProvider + ":blocks/water_flow");
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		List<BakedQuad> quads = new ArrayList<BakedQuad>();
		if (side != null) {
			return quads;
		}

		if (state instanceof IExtendedBlockState) {

			BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();

			if (renderLayer == BlockRenderLayer.CUTOUT_MIPPED) {
				quads.addAll(rootsModel.getQuads(state, side, rand));
			}

			if (renderLayer == BlockRenderLayer.TRANSLUCENT) {
				IExtendedBlockState extState = (IExtendedBlockState) state;

				float yOffset = 0.001F;
				float y0 = extState.getValue(BlockRootyWater.CORNER_HEIGHTS[0]) - yOffset;
				float y1 = extState.getValue(BlockRootyWater.CORNER_HEIGHTS[1]) - yOffset;
				float y2 = extState.getValue(BlockRootyWater.CORNER_HEIGHTS[2]) - yOffset;
				float y3 = extState.getValue(BlockRootyWater.CORNER_HEIGHTS[3]) - yOffset;

				TextureAtlasSprite textureatlassprite = stillWaterTexture;

				int[] colors = new int[6];
				for (EnumFacing facing : EnumFacing.values()) {
					int v = (int) (255 * net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(facing));
					colors[facing.ordinal()] = 0xFF000000 | v << 16 | v << 8 | v; //0xAARRGGBB'
				}

				float min = 0.0f;
				float max = 16.0f;

				float p0 = 0.0001f;
				float p1 = 0.9999f;

				//DOWN
				if (extState.getValue(BlockRootyWater.RENDER_SIDES[0])) {
					quads.add(new BakedQuad(Ints.concat(
						vertexToInts(0, 0, 1, colors[0], textureatlassprite, min, max),
						vertexToInts(0, 0, 0, colors[0], textureatlassprite, min, min),
						vertexToInts(1, 0, 0, colors[0], textureatlassprite, max, min),
						vertexToInts(1, 0, 1, colors[0], textureatlassprite, max, max)
					), 0, EnumFacing.DOWN, textureatlassprite, false, DefaultVertexFormats.BLOCK));
				}

				//UP
				if (extState.getValue(BlockRootyWater.RENDER_SIDES[1])) {
					quads.add(new BakedQuad(Ints.concat(
						vertexToInts(0, y0, 0, colors[1], textureatlassprite, min, min),
						vertexToInts(0, y1, 1, colors[1], textureatlassprite, min, max),
						vertexToInts(1, y2, 1, colors[1], textureatlassprite, max, max),
						vertexToInts(1, y3, 0, colors[1], textureatlassprite, max, min)
					), 0, EnumFacing.UP, textureatlassprite, false, DefaultVertexFormats.BLOCK));
					quads.add(new BakedQuad(Ints.concat(
						vertexToInts(0, y0, 0, colors[1], textureatlassprite, min, min),
						vertexToInts(1, y3, 0, colors[1], textureatlassprite, max, min),
						vertexToInts(1, y2, 1, colors[1], textureatlassprite, max, max),
						vertexToInts(0, y1, 1, colors[1], textureatlassprite, min, max)
					), 0, EnumFacing.DOWN, textureatlassprite, false, DefaultVertexFormats.BLOCK));
				}

				//NORTH
				if (extState.getValue(BlockRootyWater.RENDER_SIDES[2])) {
					quads.add(new BakedQuad(Ints.concat(
						vertexToInts(p1, y3, p0, colors[2], flowWaterTexture, 8.0f, (1.0f - y3) * 8.0f),
						vertexToInts(p1, 0, p0, colors[2], flowWaterTexture, 8.0f, 8.0f),
						vertexToInts(p0, 0, p0, colors[2], flowWaterTexture, 0.0f, 8.0f),
						vertexToInts(p0, y0, p0, colors[2], flowWaterTexture, 0.0f, (1.0f - y0) * 8.0f)
					), 0, EnumFacing.NORTH, flowWaterTexture, false, DefaultVertexFormats.BLOCK));
					quads.add(new BakedQuad(Ints.concat(
						vertexToInts(p0, y3, p0, colors[2], flowWaterTexture, 8.0f, (1.0f - y3) * 8.0f),
						vertexToInts(p0, 0, p0, colors[2], flowWaterTexture, 8.0f, 8.0f),
						vertexToInts(p1, 0, p0, colors[2], flowWaterTexture, 0.0f, 8.0f),
						vertexToInts(p1, y0, p0, colors[2], flowWaterTexture, 0.0f, (1.0f - y0) * 8.0f)
					), 0, EnumFacing.SOUTH, flowWaterTexture, false, DefaultVertexFormats.BLOCK));
				}

				//SOUTH
				if (extState.getValue(BlockRootyWater.RENDER_SIDES[3])) {
					quads.add(new BakedQuad(Ints.concat(
						vertexToInts(p0, y1, p1, colors[3], flowWaterTexture, 8.0f, (1.0f - y1) * 8.0f),
						vertexToInts(p0, 0, p1, colors[3], flowWaterTexture, 8.0f, 8.0f),
						vertexToInts(p1, 0, p1, colors[3], flowWaterTexture, 0.0f, 8.0f),
						vertexToInts(p1, y2, p1, colors[3], flowWaterTexture, 0.0f, (1.0f - y2) * 8.0f)
					), 0, EnumFacing.SOUTH, flowWaterTexture, false, DefaultVertexFormats.BLOCK));
					quads.add(new BakedQuad(Ints.concat(
						vertexToInts(p1, y1, p1, colors[3], flowWaterTexture, 8.0f, (1.0f - y1) * 8.0f),
						vertexToInts(p1, 0, p1, colors[3], flowWaterTexture, 8.0f, 8.0f),
						vertexToInts(p0, 0, p1, colors[3], flowWaterTexture, 0.0f, 8.0f),
						vertexToInts(p0, y2, p1, colors[3], flowWaterTexture, 0.0f, (1.0f - y2) * 8.0f)
					), 0, EnumFacing.NORTH, flowWaterTexture, false, DefaultVertexFormats.BLOCK));
				}

				//WEST
				if (extState.getValue(BlockRootyWater.RENDER_SIDES[4])) {
					quads.add(new BakedQuad(Ints.concat(
						vertexToInts(p0, y0, p0, colors[4], flowWaterTexture, 8.0f, (1.0f - y0) * 8.0f),
						vertexToInts(p0, 0, p0, colors[4], flowWaterTexture, 8.0f, 8.0f),
						vertexToInts(p0, 0, p1, colors[4], flowWaterTexture, 0.0f, 8.0f),
						vertexToInts(p0, y1, p1, colors[4], flowWaterTexture, 0.0f, (1.0f - y1) * 8.0f)
					), 0, EnumFacing.WEST, flowWaterTexture, false, DefaultVertexFormats.BLOCK));
					quads.add(new BakedQuad(Ints.concat(
						vertexToInts(p0, y0, p1, colors[4], flowWaterTexture, 8.0f, (1.0f - y0) * 8.0f),
						vertexToInts(p0, 0, p1, colors[4], flowWaterTexture, 8.0f, 8.0f),
						vertexToInts(p0, 0, p0, colors[4], flowWaterTexture, 0.0f, 8.0f),
						vertexToInts(p0, y1, p0, colors[4], flowWaterTexture, 0.0f, (1.0f - y1) * 8.0f)
					), 0, EnumFacing.EAST, flowWaterTexture, false, DefaultVertexFormats.BLOCK));
				}

				//EAST
				if (extState.getValue(BlockRootyWater.RENDER_SIDES[5])) {
					quads.add(new BakedQuad(Ints.concat(
						vertexToInts(p1, y2, p1, colors[5], flowWaterTexture, 8.0f, (1.0f - y2) * 8.0f),
						vertexToInts(p1, 0, p1, colors[5], flowWaterTexture, 8.0f, 8.0f),
						vertexToInts(p1, 0, p0, colors[5], flowWaterTexture, 0.0f, 8.0f),
						vertexToInts(p1, y3, p0, colors[5], flowWaterTexture, 0.0f, (1.0f - y3) * 8.0f)
					), 0, EnumFacing.EAST, flowWaterTexture, false, DefaultVertexFormats.BLOCK));
					quads.add(new BakedQuad(Ints.concat(
						vertexToInts(p1, y2, p0, colors[5], flowWaterTexture, 8.0f, (1.0f - y2) * 8.0f),
						vertexToInts(p1, 0, p0, colors[5], flowWaterTexture, 8.0f, 8.0f),
						vertexToInts(p1, 0, p1, colors[5], flowWaterTexture, 0.0f, 8.0f),
						vertexToInts(p1, y3, p1, colors[5], flowWaterTexture, 0.0f, (1.0f - y3) * 8.0f)
					), 0, EnumFacing.WEST, flowWaterTexture, false, DefaultVertexFormats.BLOCK));

				}

			}
		}

		return quads;
	}

	protected int[] vertexToInts(float x, float y, float z, int color, TextureAtlasSprite texture, float u, float v) {
		return new int[]{
			Float.floatToRawIntBits(x), Float.floatToRawIntBits(y), Float.floatToRawIntBits(z),
			color,
			Float.floatToRawIntBits(texture.getInterpolatedU(u)), Float.floatToRawIntBits(texture.getInterpolatedV(v)),
			0,
		};
	}

	@Override
	public boolean isAmbientOcclusion() {
		return false;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return true;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return stillWaterTexture;
	}

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}

}
