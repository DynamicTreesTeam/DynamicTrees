package com.ferreusveritas.dynamictrees.client;

import java.util.Collection;
import java.util.function.Function;

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
		int[][] baseTextureData = baseTexture.getFrameTextureData(0);
		int srcWidth = baseTexture.getIconWidth();
		int srcHeight = baseTexture.getIconHeight();
		
		this.width = srcWidth * 3;
		this.height = srcHeight * 3;
		
		int[][] textureData = new int[baseTextureData.length][];
		// only generate texture data for the first mipmap level, let the Minecraft handle the rest
		textureData[0] = new int[width * height];
		for (int pixelIndex = 0; pixelIndex < textureData[0].length; pixelIndex++) {
			// TODO: generate texture
			int x = pixelIndex % width;
			int y = pixelIndex / width;
			
			int srcX = x % srcWidth;//(int) (x * ((double) srcSize / size));
			int srcY = y % srcHeight;//(int) (y * ((double) srcSize / size));
			int srcIndex = srcX + (srcY * srcWidth);
			
			textureData[0][pixelIndex] = baseTextureData[0][srcIndex];
		}
		this.setFramesTextureData(Lists.<int[][]>newArrayList(textureData));
		
		return false;
	}
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return ImmutableList.of(baseRingLocation);
	}

}
