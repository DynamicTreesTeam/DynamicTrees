package com.ferreusveritas.dynamictrees.models;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface ICustomDamageModel {
	
	public List<BakedQuad> getCustomDamageQuads(IBlockState blockState, EnumFacing side, long rand);

}
