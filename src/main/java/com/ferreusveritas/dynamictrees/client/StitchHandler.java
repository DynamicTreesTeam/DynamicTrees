package com.ferreusveritas.dynamictrees.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

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
			int[][] data;
			int[][] original = oakSeed.getFrameTextureData(0);
			data = new int[original.length][];
			data[0] = Arrays.copyOf(original[0], original[0].length);
			
			if(this.framesTextureData.isEmpty()) {
				this.framesTextureData.add(data);
			}
			
			int[][] frames = oakSeed.getFrameTextureData(0);
			
			int[] pixels = frames[0];
			
			for(int i = 0; i < pixels.length; i++) {
				int a = (pixels[i] >> 24) & 0xff;
				int r = (pixels[i] >> 16) & 0xff;
				int g = (pixels[i] >> 8) & 0xff;
				int b = (pixels[i] >> 0) & 0xff;
				
				int gray = ((r * 30) + (g * 59) + (b * 11)) / 100; 
				
				pixels[i] = (a << 24) + (gray << 16) + (gray << 8) + gray;
			}
			
			ArrayList<int[][]> ff = new ArrayList<>();
			ff.add(frames);
			
			oakSeed.setFramesTextureData(ff);
			
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
