package com.ferreusveritas.dynamictrees.models;

import net.minecraft.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public interface ICustomDamageModel {
	
	public List<BakedQuad> getCustomDamageQuads(BlockState blockState, Direction side, long rand);

}
