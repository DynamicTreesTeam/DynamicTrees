package com.ferreusveritas.dynamictrees.client.thickrings;

import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
public class ThickRingSpriteUploader extends ReloadListener<AtlasTexture.SheetData> implements AutoCloseable {

    private final ThickRingAtlasTexture textureAtlas;

    public ThickRingSpriteUploader(TextureManager textureManagerIn) {
        textureAtlas = new ThickRingAtlasTexture();
        textureManagerIn.loadTexture(textureAtlas.getTextureLocation(), textureAtlas);
    }

    public ThickRingAtlasTexture getTextureAtlas (){
        return textureAtlas;
    }

    boolean prepared = false;
    public AtlasTexture.SheetData prepare(IResourceManager resourceManager, Stream<ResourceLocation> resourceLocationsIn) {
        if (prepared) return ThickRingTextureManager.thickRingData;
        IProfiler profiler = EmptyProfiler.INSTANCE;
        profiler.startTick();
        profiler.startSection("stitching");
        AtlasTexture.SheetData atlastexture$sheetdata = textureAtlas.stitch(resourceManager, resourceLocationsIn, profiler, 0);
        profiler.endSection();
        profiler.endTick();
        prepared = true;
        return atlastexture$sheetdata;
    }
    public void apply(AtlasTexture.SheetData sheetData) {
        if (sheetData == null) {
            System.out.println("AAAA");
            return;
        }
        IProfiler profiler = EmptyProfiler.INSTANCE;
        profiler.startTick();
        profiler.startSection("upload");
        textureAtlas.upload(sheetData);
        profiler.endSection();
        profiler.endTick();
    }

    /**
     * We dont need these since we are doing a god awful hack instead
     */
    protected AtlasTexture.SheetData prepare(IResourceManager resourceManager, IProfiler profiler) { return null; }
    protected void apply(AtlasTexture.SheetData sheetData, IResourceManager resourceManager, IProfiler profiler) { }

    public void close() {
        textureAtlas.clear();
    }

}