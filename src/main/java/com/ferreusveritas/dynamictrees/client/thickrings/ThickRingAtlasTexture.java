package com.ferreusveritas.dynamictrees.client.thickrings;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ThickRingAtlasTexture extends AtlasTexture {

    private static final Logger LOGGER = LogManager.getLogger();
    private final int maximumTextureSize;

    private static Map<ResourceLocation, ThickRingTextureAtlasSprite> thickRingSprites = new HashMap<>();

    public ThickRingAtlasTexture(ResourceLocation textureLocationIn) {
        super(textureLocationIn);
        this.maximumTextureSize = RenderSystem.maxSupportedTextureSize();
    }

    public void addThickRingSprite (ResourceLocation thinRings, ResourceLocation thickRings){
        thickRingSprites.put(thickRings, new ThickRingTextureAtlasSprite(this, thickRings,  thinRings));
    }

    public SheetData stitch(IResourceManager resourceManagerIn, IProfiler profilerIn, int maxMipmapLevelIn) {
        return stitch(resourceManagerIn, null, profilerIn, maxMipmapLevelIn);
    }

    @Override
    public SheetData stitch(IResourceManager resourceManagerIn, Stream<ResourceLocation> resourceLocationsIn, IProfiler profilerIn, int maxMipmapLevelIn) {
        profilerIn.startSection("preparing");
        Set<ResourceLocation> set = thickRingSprites.keySet();
        int i = this.maximumTextureSize;
        Stitcher stitcher = new Stitcher(i, i, maxMipmapLevelIn);
        int j = Integer.MAX_VALUE;
        int k = 1 << maxMipmapLevelIn;
        profilerIn.endStartSection("extracting_frames");
        net.minecraftforge.client.ForgeHooksClient.onTextureStitchedPre(this, set);

        for(TextureAtlasSprite.Info textureatlassprite$info : this.makeSprites(set)) {
            j = Math.min(j, Math.min(textureatlassprite$info.getSpriteWidth(), textureatlassprite$info.getSpriteHeight()));
            int l = Math.min(Integer.lowestOneBit(textureatlassprite$info.getSpriteWidth()), Integer.lowestOneBit(textureatlassprite$info.getSpriteHeight()));
            if (l < k) {
                LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", textureatlassprite$info.getSpriteLocation(), textureatlassprite$info.getSpriteWidth(), textureatlassprite$info.getSpriteHeight(), MathHelper.log2(k), MathHelper.log2(l));
                k = l;
            }

            stitcher.addSprite(textureatlassprite$info);
        }

        profilerIn.endStartSection("register");
        stitcher.addSprite(MissingTextureSprite.getSpriteInfo());
        profilerIn.endStartSection("stitching");

        try {
            stitcher.doStitch();
        } catch (StitcherException stitcherexception) {
            CrashReport crashreport = CrashReport.makeCrashReport(stitcherexception, "Stitching");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Stitcher");
            crashreportcategory.addDetail("Sprites", stitcherexception.getSpriteInfos().stream().map((atlsInfo) ->
                    String.format("%s[%dx%d]", atlsInfo.getSpriteLocation(), atlsInfo.getSpriteWidth(), atlsInfo.getSpriteHeight())).collect(Collectors.joining(",")));
            crashreportcategory.addDetail("Max Texture Size", i);
            throw new ReportedException(crashreport);
        }

        profilerIn.endStartSection("loading");
        List<TextureAtlasSprite> list = this.getStitchedSprites(stitcher, maxMipmapLevelIn);
        profilerIn.endSection();
        return new AtlasTexture.SheetData(set, stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), maxMipmapLevelIn, list);
    }

    private Collection<TextureAtlasSprite.Info> makeSprites(Set<ResourceLocation> spriteLocationsIn) {
        List<CompletableFuture<?>> list = Lists.newArrayList();
        ConcurrentLinkedQueue<TextureAtlasSprite.Info> concurrentlinkedqueue = new ConcurrentLinkedQueue<>();

        for(ResourceLocation resourcelocation : spriteLocationsIn) {
            if (!MissingTextureSprite.getLocation().equals(resourcelocation)) {
                list.add(CompletableFuture.runAsync(() -> {
                    TextureAtlasSprite sprite = thickRingSprites.get(resourcelocation);
                    TextureAtlasSprite.Info textureatlassprite$info = new TextureAtlasSprite.Info(resourcelocation, sprite.getWidth(), sprite.getHeight(), AnimationMetadataSection.EMPTY);
                    concurrentlinkedqueue.add(textureatlassprite$info);
                }, Util.getServerExecutor()));
            }
        }

        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        return concurrentlinkedqueue;
    }

    private List<TextureAtlasSprite> getStitchedSprites(Stitcher stitcherIn, int mipmapLevelIn) {
        ConcurrentLinkedQueue<TextureAtlasSprite> concurrentlinkedqueue = new ConcurrentLinkedQueue<>();
        List<CompletableFuture<?>> list = Lists.newArrayList();
        stitcherIn.getStitchSlots((atlasInfo, atlasWidth, atlasHeight, x, y) -> {
            if (atlasInfo == MissingTextureSprite.getSpriteInfo()) {
                MissingTextureSprite missingtexturesprite = MissingTextureSprite.create(this, mipmapLevelIn, atlasWidth, atlasHeight, x, y);
                concurrentlinkedqueue.add(missingtexturesprite);
            } else {
                list.add(CompletableFuture.runAsync(() -> {
                    TextureAtlasSprite textureatlassprite = thickRingSprites.get(atlasInfo.getSpriteLocation());
                    if (textureatlassprite != null) {
                        concurrentlinkedqueue.add(textureatlassprite);
                    }

                }, Util.getServerExecutor()));
            }

        });
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        return Lists.newArrayList(concurrentlinkedqueue);
    }

}
