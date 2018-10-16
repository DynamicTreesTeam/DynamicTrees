package com.ferreusveritas.dynamictrees.client;

import java.util.Collection;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.client.TextureUtils.PixelBuffer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class ThickRingTextureAtlasSprite extends TextureAtlasSprite {

	ResourceLocation baseRingLocation;
	
	public ThickRingTextureAtlasSprite(ResourceLocation spriteName, ResourceLocation baseRingLocation) {
		super(spriteName.toString());
		
		this.baseRingLocation = baseRingLocation;
	}
	
	@Override
	public boolean hasCustomLoader(net.minecraft.client.resources.IResourceManager manager, net.minecraft.util.ResourceLocation location) {
		return true;
	}

	@Override
	public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
		TextureAtlasSprite baseTexture = textureGetter.apply(baseRingLocation);
		int srcWidth = baseTexture.getIconWidth();
		int srcHeight = baseTexture.getIconHeight();
		
		this.width = srcWidth * 3;
		this.height = srcHeight * 3;
		
		PixelBuffer srcPixbuf = new PixelBuffer(baseTexture);
		PixelBuffer dstPixbuf = new PixelBuffer(width, height);
		
		for(int i = 0; i < 9; i++) {
			srcPixbuf.blit(dstPixbuf, (i % 3) * srcWidth, (i / 3) * srcHeight);
		}
				
		//Load the pixels into the TextureAtlasSprite
		int mipmapLevels = baseTexture.getFrameTextureData(0).length;
		int[][] textureData = new int[mipmapLevels][];
		textureData[0] = dstPixbuf.pixels;// only generate texture data for the first mipmap level, let Minecraft handle the rest
		this.setFramesTextureData(Lists.<int[][]>newArrayList(textureData));
		
		return false;
	}
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return ImmutableList.of(baseRingLocation);
	}

}
