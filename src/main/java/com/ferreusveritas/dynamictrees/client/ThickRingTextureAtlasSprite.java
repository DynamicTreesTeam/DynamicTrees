package com.ferreusveritas.dynamictrees.client;

import java.util.Collection;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

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
		
		this.width = baseTexture.getIconWidth() * 3;
		this.height = baseTexture.getIconHeight() * 3;
		
		// TODO: copy animation data
		// TODO: generate texture
		
		return true;
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return ImmutableList.of(baseRingLocation);
	}

}
