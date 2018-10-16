package com.ferreusveritas.dynamictrees.client;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.util.MathHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.util.ReportedException;
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
		
		this.width = baseTexture.getIconWidth() * 3;
		this.height = baseTexture.getIconHeight() * 3;
		
		int[][] textureData = new int[baseTextureData.length][this.width * this.height];
		
		for (int mipmapLvl = 0; mipmapLvl < textureData.length; mipmapLvl++) {
			for (int pixelIndex = 0; pixelIndex < textureData[mipmapLvl].length; pixelIndex++) {
				int size = (int) Math.sqrt(textureData[mipmapLvl].length);
				int srcSize = (int) Math.sqrt(baseTextureData[mipmapLvl].length);
				
				int x = pixelIndex % size;
				int y = pixelIndex / size;
				
				int srcX = x % srcSize;//(int) (x * ((double) srcSize / size));
				int srcY = y % srcSize;//(int) (y * ((double) srcSize / size));
				int srcIndex = srcX + (srcY * srcSize);
				
				textureData[mipmapLvl][pixelIndex] = baseTextureData[mipmapLvl][srcIndex];
			}
		}
		
		this.setFramesTextureData(Lists.<int[][]>newArrayList(textureData));
		
		// TODO: copy animation data
		// TODO: generate texture
		
		return false;
	}
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return ImmutableList.of(baseRingLocation);
	}

}
