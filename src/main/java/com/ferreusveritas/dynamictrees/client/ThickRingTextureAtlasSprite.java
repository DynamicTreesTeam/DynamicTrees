package com.ferreusveritas.dynamictrees.client;

import com.ferreusveritas.dynamictrees.client.TextureUtils.PixelBuffer;
import com.ferreusveritas.dynamictrees.util.CoordUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.function.Function;

public class ThickRingTextureAtlasSprite extends TextureAtlasSprite {
	
	private final ResourceLocation baseRingLocation;
	private final ResourceLocation baseRingLocationAlternate;
	
	public ThickRingTextureAtlasSprite(ResourceLocation spriteName, ResourceLocation baseRingLocation) {
		super(spriteName, 32, 32);
		
		this.baseRingLocation = baseRingLocation;
		this.baseRingLocationAlternate = null;
	}
	
	/**
	 * Given these two resources attempt to figure out which is the ringed texture.
	 * 
	 * @param spriteName
	 * @param baseRingLocation
	 * @param baseRingLocationAlternate
	 */
	public ThickRingTextureAtlasSprite(ResourceLocation spriteName, ResourceLocation baseRingLocation, ResourceLocation baseRingLocationAlternate) {
		super(spriteName, 32, 32);
		
		this.baseRingLocation = baseRingLocation;
		this.baseRingLocationAlternate = baseRingLocationAlternate;
	}
	
	@Override
	public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
		return true;
	}
	
	public ResourceLocation solveRingTexture(Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
		
		//If there's no alternative then obviously we must use the primary
		if(this.baseRingLocationAlternate == null) {
			return this.baseRingLocation;
		}
		
		//A basic check that fits 80% of the time.  Usually the ringed texture's resource ends in "top" e.g. "oak_log_top"
		if(baseRingLocation.getPath().endsWith("top")) {
			return baseRingLocation;
		}
		if(baseRingLocationAlternate.getPath().endsWith("top")) {
			return baseRingLocationAlternate;
		}
		
		//Sample the pixels themselves to determine which is the ringed texture
		int deltaA = getDeltaBorderVsCenterColor(textureGetter.apply(baseRingLocation));
		int deltaB = getDeltaBorderVsCenterColor(textureGetter.apply(baseRingLocationAlternate));
		
		return deltaA > deltaB ? baseRingLocation : baseRingLocationAlternate;
	}
	
	/**
	 * This compares the color of the sprite border with the color of the sprite middle
	 * and returns a the RGB delta squared. 
	 * 
	 * @param sprite The sprite to generate the delta
	 * @return RGB delta squared
	 */
	private int getDeltaBorderVsCenterColor(TextureAtlasSprite sprite) {
		PixelBuffer pixbuf = new PixelBuffer(sprite);
		int u = pixbuf.w / 16;
		PixelBuffer wide = new PixelBuffer(u * 14, u *  1);
		PixelBuffer tall = new PixelBuffer(u *  1, u * 14);
		
		int samples[] = new int[4];
		
		//Sample top and bottom border
		pixbuf.blit(wide, u * -1, u * -0);
		samples[0] = wide.averageColor();
		pixbuf.blit(wide, u * -1, u * -15);
		samples[1] = wide.averageColor();
		
		//Sample left and right border
		pixbuf.blit(tall, u * -0, u * -1);
		samples[2] = tall.averageColor();
		pixbuf.blit(tall, u * -15, u * -1);
		samples[3] = tall.averageColor();
		
		int borderColor = TextureUtils.avgColors(samples);
		
		//Sample 4 lines that don't contain the pixels on the left/right border
		for(int i = 0; i < 4; i++) {
			pixbuf.blit(wide, u * -1, u * -(i * 3 + 3)); //Lines 3, 6, 9, 12
			samples[i] = wide.averageColor();
		}
		
		int innerColor = TextureUtils.avgColors(samples);
				
		//Decompose pixels into an RGBA array
		int cA[] = TextureUtils.decompose(borderColor);
		int cB[] = TextureUtils.decompose(innerColor);
		
		//Find the delta of the rgb components between the border and the middle, ingore alpha channel
		int delR = cB[0] - cA[0];
		int delG = cB[1] - cA[1];
		int delB = cB[2] - cA[2];
		
		//Get the distance squared of the 2 colors in RGB(3D) space
		return delR * delR + delG * delG + delB * delB;
	}
	
	@Override
	public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
		TextureAtlasSprite baseTexture = textureGetter.apply(solveRingTexture(textureGetter));
		int srcWidth = baseTexture.getWidth();
		int srcHeight = baseTexture.getHeight();
		
//		this.width = srcWidth * 3;
//		this.height = srcHeight * 3;
		
		PixelBuffer basePixbuf = new PixelBuffer(baseTexture);
		PixelBuffer majPixbuf = createMajorTexture(basePixbuf);
		
		//Load the pixels into the TextureAtlasSprite
//		int mipmapLevels = baseTexture.getFrameTextureData(0).length;
//		int[][] textureData = new int[mipmapLevels][];
//		textureData[0] = majPixbuf.pixels;// only generate texture data for the first mipmap level, let Minecraft handle the rest
//		this.setFramesTextureData(Lists.<int[][]>newArrayList(textureData));
		
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
			for(Direction dir : CoordUtils.HORIZONTALS) { //SWNE
				Direction out = dir;
				Direction ovr = dir.rotateY();
				int offX = out.getXOffset();
				int offY = out.getZOffset();
				int compX = (offX == 1 ? -6 : 0) + (dir.getAxis() == Axis.Z ? -2 : 0);
				int compY = (offY == 1 ? -6 : 0) + (dir.getAxis() == Axis.X ? -2 : 0);
				int startX = offX * (14 + nesting * 6);
				int startY = offY * (14 + nesting * 6);
				for(int way = -1; way <= 1; way+=2) {
					for(int i = 0; i < 4 + nesting; i++) {
						int rowX = ovr.getXOffset() * i * way * 4;
						int rowY = ovr.getZOffset() * i * way * 4;
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
		return baseRingLocationAlternate == null ?
			ImmutableList.of(baseRingLocation) :
			ImmutableList.of(baseRingLocation, baseRingLocationAlternate);
	}
	
}
