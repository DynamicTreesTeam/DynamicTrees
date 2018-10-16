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
		PixelBuffer antPixbuf = createBarklessAntecedent(srcPixbuf);
		PixelBuffer dstPixbuf = new PixelBuffer(width, height);
		
		for(int i = 0; i < 9; i++) {
			antPixbuf.blit(dstPixbuf, (i % 3) * srcWidth, (i / 3) * srcHeight);
		}
				
		//Load the pixels into the TextureAtlasSprite
		int mipmapLevels = baseTexture.getFrameTextureData(0).length;
		int[][] textureData = new int[mipmapLevels][];
		textureData[0] = dstPixbuf.pixels;// only generate texture data for the first mipmap level, let Minecraft handle the rest
		this.setFramesTextureData(Lists.<int[][]>newArrayList(textureData));
		
		return false;
	}
	
	PixelBuffer createBarklessAntecedent(PixelBuffer baseBuffer) {
		PixelBuffer antecedent = new PixelBuffer(baseBuffer);

		//Place the 4th pixel ring against the corners of the image.
		//Rotate 90deg to break up the pattern
		baseBuffer.blit(antecedent,  3,  3, 1);
		baseBuffer.blit(antecedent, -3,  3, 1);
		baseBuffer.blit(antecedent,  3, -3, 1);
		baseBuffer.blit(antecedent, -3, -3, 1);
		
		//Copy a 6 wide strip of pixels from the 4th pixel ring and place
		//it over the bark texture for all 4 edges.  Alternate the placement
		//to break up the pattern
		PixelBuffer ringStrip = new PixelBuffer(6, 1);
		baseBuffer.blit(ringStrip, -5,-3);
		ringStrip.blit(antecedent, 0, 2, -1);
		ringStrip.blit(antecedent, 15, 8, 1);
		
		baseBuffer.blit(ringStrip, -5,-12);
		ringStrip.blit(antecedent, 0, 8, 1);
		ringStrip.blit(antecedent, 15, 2, -1);

		ringStrip = new PixelBuffer(1, 6);
		baseBuffer.blit(ringStrip, -3,-5);
		ringStrip.blit(antecedent, 2, 0, -1);
		ringStrip.blit(antecedent, 8, 15, 1);
		
		baseBuffer.blit(ringStrip, -12,-5);
		ringStrip.blit(antecedent, 8, 0, 1);
		ringStrip.blit(antecedent, 2, 15, -1);
		
		ringStrip = null;
		
		//Copy the center 14x14 pixels of the original back over the result 
		PixelBuffer center = new PixelBuffer(14, 14);
		baseBuffer.blit(center, -1, -1);
		center.blit(antecedent, 1, 1);
		
		return antecedent;
	}
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return ImmutableList.of(baseRingLocation);
	}

}
