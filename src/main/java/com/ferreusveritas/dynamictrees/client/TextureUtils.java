package com.ferreusveritas.dynamictrees.client;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TextureUtils {
	
	private int createdTextures;
	
	private static Set<ResourceLocation> baseTextures = Sets.newHashSet();
	  public static Map<String, Map<String, TextureAtlasSprite>> sprites = Maps.newHashMap();

	
	// low since other event-handlers might want to register textures beforehand
	@SubscribeEvent(priority = EventPriority.LOW)
	public void createCustomTextures(TextureStitchEvent.Pre event) {
		// get the material info at this point, to override hardcoded material rendering with resources
		//MaterialRenderInfoLoader.INSTANCE.onResourceManagerReload(Minecraft.getMinecraft().getResourceManager());
		
		createdTextures = 0;
		// create textures for each material where needed
		createMaterialTextures(event.getMap());
		
		// add stencil and cast textures for all used toolparts
		//createPatterntextures(event.getMap());
		
		//log.debug("Generated " + createdTextures + " Textures for Materials");
	}
	
	
	  private void createMaterialTextures(TextureMap map) {
		    // Create textures for toolparts and tools - Textures that need 1 per material
		    for(ResourceLocation baseTexture : baseTextures) {
		      // exclude missingno :I
		      if(baseTexture.toString().equals("minecraft:missingno")) {
		        continue;
		      }

		      /*Set<IToolPart> parts = texturePartMapping.get(baseTexture);

		      Map<String, TextureAtlasSprite> builtSprites = Maps.newHashMap();
		      for(Material material : TinkerRegistry.getAllMaterials()) {
		        boolean usable;
		        if(parts == null || material instanceof MaterialGUI) {
		          usable = true;
		        }
		        else {
		          usable = false;
		          for(IToolPart toolPart : parts) {
		            usable |= toolPart.canUseMaterialForRendering(material);
		          }
		        }

		        if(usable) {
		          TextureAtlasSprite sprite = createTexture(material, baseTexture, map);
		          if(sprite != null) {
		            builtSprites.put(material.identifier, sprite);
		          }
		        }
		      }

		      if(belongsToToolPart(baseTexture)) {
		        TextureAtlasSprite sprite = createTexture(guiMaterial, baseTexture, map);
		        if(sprite != null) {
		          builtSprites.put(guiMaterial.identifier, sprite);
		        }
		      }*/

		     // sprites.put(baseTexture.toString(), builtSprites);
		    }
		  }
	
	public static boolean exists(String res) {
		try {
			ResourceLocation loc = new ResourceLocation(res);
			loc = new ResourceLocation(loc.getResourceDomain(), "textures/" + loc.getResourcePath() + ".png");
			Minecraft.getMinecraft().getResourceManager().getAllResources(loc);
			return true;
		} catch(IOException e) {
			return false;
		}
	}
	
	
	private TextureAtlasSprite createTexture(Material material, ResourceLocation baseTexture, TextureMap map) {
		String location = baseTexture.toString() + "_" + material.identifier;
		TextureAtlasSprite sprite;
		
		if(exists(location)) {
			sprite = map.registerSprite(new ResourceLocation(location));
		}
		else {
			// material does not need a special generated texture
			if(material.renderInfo == null) {
				return null;
			}
			
			// different base texture?
			if(material.renderInfo.getTextureSuffix() != null) {
				String loc2 = baseTexture.toString() + "_" + material.renderInfo.getTextureSuffix();
				TextureAtlasSprite base2 = map.getTextureExtry(loc2);
				// can we manually load it?
				if(base2 == null && exists(loc2)) {
					base2 = TinkerTexture.loadManually(new ResourceLocation(loc2));
					// save in the map so it's getting reused by the others and is available
					map.setTextureEntry(base2);
				}
				if(base2 != null) {
					baseTexture = new ResourceLocation(base2.getIconName());
				}
			}
			
			sprite = material.renderInfo.getTexture(baseTexture, location);
			createdTextures++;
		}
		
		// stitch new textures
		if(sprite != null && material.renderInfo.isStitched()) {
			map.setTextureEntry(sprite);
		}
		return sprite;
	}
	
	
}
