package com.ferreusveritas.dynamictrees.client;

import java.util.Collection;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.client.TextureUtils.PixelBuffer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
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
		
		PixelBuffer basePixbuf = new PixelBuffer(baseTexture);
		PixelBuffer majPixbuf = createMajorTexture(basePixbuf);
		
		//Load the pixels into the TextureAtlasSprite
		int mipmapLevels = baseTexture.getFrameTextureData(0).length;
		int[][] textureData = new int[mipmapLevels][];
		textureData[0] = majPixbuf.pixels;// only generate texture data for the first mipmap level, let Minecraft handle the rest
		this.setFramesTextureData(Lists.<int[][]>newArrayList(textureData));
		
		return false;
	}
	
	private PixelBuffer createMajorTexture(PixelBuffer baseBuffer) {
		
		int w = baseBuffer.w * 3;
		int h = baseBuffer.h * 3;
		int scale = baseBuffer.w / 16;
		
		PixelBuffer antPixbuf = createBarklessAntecedent(baseBuffer);
		PixelBuffer majPixbuf = new PixelBuffer(w, h);
				
		//Compile a set of texture component pieces from the antecedent
		PixelBuffer corners[] = new PixelBuffer[4];
		PixelBuffer edges[] = new PixelBuffer[4];
		for(int i = 0; i < 4; i++) {
			corners[i] = new PixelBuffer(6 * scale, 6 * scale);
			edges[i] = new PixelBuffer(4 * scale, 6 * scale);
			antPixbuf.blit(corners[i], 0, 0, i);
			antPixbuf.blit(edges[i], -6 * scale, 0, i);
		}
		
		//Fill in the rest of the rings
		int centerX = 24;
		int centerY = 24;
		for(int nesting = 0; nesting < 3; nesting++) {
			int edge = 2;
			int pixbufSel = 0;
			for(EnumFacing dir : EnumFacing.HORIZONTALS) { //SWNE
				EnumFacing out = dir;
				EnumFacing ovr = dir.rotateY();
				int offX = out.getFrontOffsetX();
				int offY = out.getFrontOffsetZ();
				int compX = (offX == 1 ? -6 : 0) + (dir.getAxis() == Axis.Z ? -2 : 0);
				int compY = (offY == 1 ? -6 : 0) + (dir.getAxis() == Axis.X ? -2 : 0);
				int startX = offX * (14 + nesting * 6);
				int startY = offY * (14 + nesting * 6);
				for(int way = -1; way <= 1; way+=2) {
					for(int i = 0; i < 4 + nesting; i++) {
						int rowX = ovr.getFrontOffsetX() * i * way * 4;
						int rowY = ovr.getFrontOffsetZ() * i * way * 4;
						int realX = centerX + startX + compX + rowX;
						int realY = centerY + startY + compY + rowY;
						edges[((pixbufSel++ * 13402141) >> 1) & 3].blit(majPixbuf, realX * scale, realY * scale, edge);
					}
				}
				edge++;
			}
		}
		
		//Create corners
		int cornerX[] = new int[]{ -1,  1,  1, -1 };
		int cornerY[] = new int[]{ -1, -1,  1,  1 };
		antPixbuf.blit(majPixbuf, 16 * scale, 16 * scale);
		for(int nesting = 1; nesting < 4; nesting++) {
			for(int corner = 0; corner < 4; corner++) {
				PixelBuffer cornerPixels = corners[ (corner + nesting) & 0x3 ];
				int cX = cornerX[corner];
				int cY = cornerY[corner];
				int offX = cX * 6 * nesting + cX * 5;
				int offY = cY * 6 * nesting + cY * 5;
				int realX = 16 + 5 + offX;
				int realY = 16 + 5 + offY;
				cornerPixels.blit(majPixbuf, realX * scale, realY * scale, corner);
			}
		}
		
		//Compile a set of bark texture component pieces from the antecedent
		for(int i = 0; i < 4; i++) {
			corners[i] = new PixelBuffer(scale, scale);
			edges[i] = new PixelBuffer(14 * scale, scale);
			baseBuffer.blit(corners[i], 0, 0, i);
			baseBuffer.blit(edges[i], -1 * scale, 0, i);
		}

		//Create bark border
		int pixbufSel = 0;
		for(int row = 0; row < 4; row++) {
			PixelBuffer edge = edges[((pixbufSel++ * 13402141) >> 1) & 3];
			int span = edge.w;
			edge.blit(majPixbuf, (1 + row * span) * scale, 0, 0);
			edge.blit(majPixbuf, (majPixbuf.w - edge.h) * scale, (1 + row * span) * scale, 1);
			edge.blit(majPixbuf, (majPixbuf.w - 1 - span - row * span) * scale, (majPixbuf.h - edge.h) * scale, 2);
			edge.blit(majPixbuf, 0, (majPixbuf.h - 1 - edge.w - row * span) * scale, 3);
		}
		
		for(int corner = 0; corner < 4; corner++) {
			int cX = (cornerX[corner] + 1) >> 1;
			int cY = (cornerY[corner] + 1) >> 1;
			PixelBuffer cornerPixels = corners[corner];
			cornerPixels.blit(majPixbuf, cX * (majPixbuf.w - cornerPixels.w) * scale, cY * (majPixbuf.h - cornerPixels.h) * scale, corner);
		}
		
		return majPixbuf;
	}
	
	private PixelBuffer createBarklessAntecedent(PixelBuffer baseBuffer) {
		PixelBuffer antecedent = new PixelBuffer(baseBuffer);
		
		int scale = baseBuffer.w / 16;

		//Place the 4th pixel ring against the corners of the image.
		//Rotate 90deg to break up the pattern
		baseBuffer.blit(antecedent,  3 * scale,  3 * scale, 1);
		baseBuffer.blit(antecedent, -3 * scale,  3 * scale, 1);
		baseBuffer.blit(antecedent,  3 * scale, -3 * scale, 1);
		baseBuffer.blit(antecedent, -3 * scale, -3 * scale, 1);
		
		//Copy a 6 wide strip of pixels from the 4th pixel ring and place
		//it over the bark texture for all 4 edges.  Alternate the placement
		//to break up the pattern
		PixelBuffer ringStrip = new PixelBuffer(6 * scale, 1 * scale);
		baseBuffer.blit(ringStrip, -5 * scale,-3 * scale);
		ringStrip.blit(antecedent, 0 * scale, 2 * scale, -1);
		ringStrip.blit(antecedent, 15 * scale, 8 * scale, 1);
		
		baseBuffer.blit(ringStrip, -5 * scale,-12 * scale);
		ringStrip.blit(antecedent, 0 * scale, 8 * scale, 1);
		ringStrip.blit(antecedent, 15 * scale, 2 * scale, -1);

		ringStrip = new PixelBuffer(1 * scale, 6 * scale);
		baseBuffer.blit(ringStrip, -3 * scale,-5 * scale);
		ringStrip.blit(antecedent, 2 * scale, 0 * scale, -1);
		ringStrip.blit(antecedent, 8 * scale, 15 * scale, 1);
		
		baseBuffer.blit(ringStrip, -12 * scale,-5 * scale);
		ringStrip.blit(antecedent, 8 * scale, 0 * scale, 1);
		ringStrip.blit(antecedent, 2 * scale, 15 * scale, -1);
		
		ringStrip = null;
		
		//Copy the center 14x14 pixels of the original back over the result 
		PixelBuffer center = new PixelBuffer(14 * scale, 14 * scale);
		baseBuffer.blit(center, -1 * scale, -1 * scale);
		center.blit(antecedent, 1 * scale, 1 * scale);
		
		return antecedent;
	}
	
	@Override
	public Collection<ResourceLocation> getDependencies() {
		return ImmutableList.of(baseRingLocation);
	}

}
