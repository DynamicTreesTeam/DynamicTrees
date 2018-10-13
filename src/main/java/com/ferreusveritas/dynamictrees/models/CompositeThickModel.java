package com.ferreusveritas.dynamictrees.models;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.lwjgl.util.vector.Vector3f;

import com.ferreusveritas.dynamictrees.blocks.BlockBranch;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPart;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.SimpleBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;

public class CompositeThickModel extends CompositeBasicModel {
	
	private IBakedModel trunks[] = new IBakedModel[16];
	
	public CompositeThickModel(ResourceLocation barkRes, ResourceLocation ringsRes, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		super(barkRes, ringsRes, bakedTextureGetter);
		
		TextureAtlasSprite barkIcon = bakedTextureGetter.apply(barkRes);
		TextureAtlasSprite ringIcon = bakedTextureGetter.apply(ringsRes);
		barkParticles = barkIcon;
		
		for(int i = 0; i < 16; i++) {
			int radius = i + 9;
			trunks[i] = bakeTrunk(radius, barkIcon, ringIcon);
		}
		
	}
	
	public IBakedModel bakeTrunk(int radius, TextureAtlasSprite bark, TextureAtlasSprite ring) {
		
		Vector3f posFrom = new Vector3f(8 - radius, 0, 8 - radius);
		Vector3f posTo = new Vector3f(8 + radius, 16, 8 + radius);
		
		Map<EnumFacing, BlockPartFace> mapFacesIn = Maps.newEnumMap(EnumFacing.class);
		
		for(EnumFacing face: EnumFacing.VALUES) {
			BlockFaceUV uvface = new BlockFaceUV(new float[]{ 8 - radius, 8 - radius, 8 + radius, 8 + radius }, getFaceAngle(Axis.Y, face));
			mapFacesIn.put(face, new BlockPartFace(null, -1, null, uvface));
		}
		
		BlockPart part = new BlockPart(posFrom, posTo, mapFacesIn, null, true);
		SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(modelBlock, ItemOverrideList.NONE).setTexture(ring);
		
		for(Map.Entry<EnumFacing, BlockPartFace> e : part.mapFaces.entrySet()) {
			EnumFacing face = e.getKey();
			builder.addFaceQuad(face, makeBakedQuad(part, e.getValue(), face.getAxis() == Axis.Y ? ring : bark, face, ModelRotation.X0_Y0, false));
		}

		return builder.makeBakedModel();
	}
	
	protected BakedQuad makeBakedQuad(BlockPart blockPart, BlockPartFace partFace, TextureAtlasSprite atlasSprite, EnumFacing dir, net.minecraftforge.common.model.ITransformation transform, boolean uvlocked) {
		return new FaceBakery().makeBakedQuad(blockPart.positionFrom, blockPart.positionTo, partFace, atlasSprite, dir, transform, blockPart.partRotation, uvlocked, blockPart.shade);
	}
	
	@Override
	public List<BakedQuad> getQuads(IBlockState blockState, EnumFacing side, long rand) {
		System.out.println("awd");
		int coreRadius = getRadius(blockState);
		
		if(coreRadius <= BlockBranch.RADMAX_NORMAL) {
			return super.getQuads(blockState, side, rand);
		}
		
		coreRadius = MathHelper.clamp(coreRadius, 9, 24);
		
		List<BakedQuad> quadsList = new LinkedList<BakedQuad>();
		quadsList.addAll(trunks[coreRadius-9].getQuads(blockState, side, rand));
		
		return quadsList;
	}
	
}
