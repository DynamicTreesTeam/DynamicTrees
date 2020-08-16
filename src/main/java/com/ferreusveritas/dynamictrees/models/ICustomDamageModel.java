package com.ferreusveritas.dynamictrees.models;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public interface ICustomDamageModel {
	
	public List<BakedQuad> getCustomDamageQuads(BlockState blockState, Direction side, long rand);

}
