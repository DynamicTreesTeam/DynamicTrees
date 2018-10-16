package com.ferreusveritas.dynamictrees.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import com.ferreusveritas.dynamictrees.client.TextureUtils.PixelBuffer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class StitchHandler {
	
	public class Carl extends TextureAtlasSprite {
		
		protected Carl(String spriteName) {
			super(spriteName);
		}
		
		@Override
		public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
			return true;
		}
		
		@Override
		public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
			
			System.out.println("**********************************************************");
			
			TextureAtlasSprite oakSeed = textureGetter.apply(new ResourceLocation("dynamictrees","items/oakseed"));
			
			this.framesTextureData = Lists.newArrayList();
			this.frameCounter = 0;
			this.tickCounter = 0;
			
			this.copyFrom(oakSeed);
			
			PixelBuffer src = new PixelBuffer(oakSeed, true);
			PixelBuffer src2 = new PixelBuffer(oakSeed, true);

			src.blit(src2, 0, 0, 3);
			src.grayScale();
			
			int[][] original = oakSeed.getFrameTextureData(0);
			int[][] data = new int[original.length][];
			data[0] = src.pixels;
			
			this.framesTextureData.add(data);
			
			//src2.apply(oakSeed);
			int[][] mmdata = oakSeed.getFrameTextureData(0);
			mmdata[0] = src2.pixels;
			ArrayList<int[][]> ftd = new ArrayList<>();
			ftd.add(mmdata);
			oakSeed.setFramesTextureData(ftd);
			
			System.out.println("**********************************************************");
			
			return false;
		}
		
		@Override
		public Collection<ResourceLocation> getDependencies() {
			return ImmutableList.of(new ResourceLocation("dynamictrees","items/oakseed"));
		}
	}
	
	public static final StitchHandler INSTANCE = new StitchHandler();
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void createCustomTextures(TextureStitchEvent.Pre event) {
		event.getMap().setTextureEntry(new Carl("dynamictrees:items/birchseed"));
	}
	
}
