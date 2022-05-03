package com.ferreusveritas.dynamictrees.client.thickrings;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ThickRingTextureManager {

    /**
     * THIS IS STILL WIP. THICK RINGS ARE NOT YET STITCHED AUTOMATICALLY
     */

    public static final ResourceLocation LOCATION_THICKRINGS_TEXTURE = new ResourceLocation(DynamicTrees.MOD_ID, "textures/atlas/thick_rings.png");

    public static ThickRingAtlasTexture textureAtlas;
    public static TextureAtlas.Preparations thickRingData;

//	protected static final RenderState.TextureState BRANCHES_SHEET_MIPPED = new RenderState.TextureState(LOCATION_THICKRINGS_TEXTURE, false, true);
//	public static final RenderType BRANCH_SOLID = RenderType.makeType("dynamic_trees_branch_solid", DefaultVertexFormats.BLOCK, 7, 2097152, true, false, RenderType.State.getBuilder().shadeModel(new RenderState.ShadeModelState(true)).lightmap(new RenderState.LightmapState(true)).texture(BRANCHES_SHEET_MIPPED).build(true));

    private static final BiMap<ResourceLocation, ResourceLocation> thickRingTextures = HashBiMap.create();

    public static ResourceLocation addRingTextureLocation(ResourceLocation ringsRes) {
        ResourceLocation thickRingSet = new ResourceLocation(ringsRes.getNamespace(), ringsRes.getPath() + "_thick");
        thickRingTextures.put(ringsRes, thickRingSet);
        return thickRingSet;
    }

    public static Set<ResourceLocation> getThickRingResourceLocations() {
        return new HashSet<>(thickRingTextures.values());
    }

    public static Set<Map.Entry<ResourceLocation, ResourceLocation>> getThickRingEntrySet() {
        return thickRingTextures.entrySet();
    }

    public static ResourceLocation getThickRingFromBaseRing(ResourceLocation baseRing) {
        return thickRingTextures.get(baseRing);
    }

    public static ResourceLocation getBaseRingFromThickRing(ResourceLocation thickRing) {
        return thickRingTextures.inverse().get(thickRing);
    }

}
