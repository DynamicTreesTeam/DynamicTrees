package com.ferreusveritas.dynamictrees.models;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ICustomDamageModel {
	
	public List<BakedQuad> getCustomDamageQuads(IBlockState blockState, EnumFacing side, long rand);

}
