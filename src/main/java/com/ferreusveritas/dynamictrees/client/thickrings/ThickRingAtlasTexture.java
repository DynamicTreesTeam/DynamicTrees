package com.ferreusveritas.dynamictrees.client.thickrings;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ThickRingAtlasTexture extends AtlasTexture {

    private static final Logger LOGGER = LogManager.getLogger();
    private final int maximumTextureSize;

    private static final int spriteSizeMultiplier = 3;

    public static final ResourceLocation LOCATION_THICKRINGS_TEXTURE = new ResourceLocation(DynamicTrees.MOD_ID, "textures/atlas/thick_rings.png");

    public ThickRingAtlasTexture() {
        super(LOCATION_THICKRINGS_TEXTURE);
        maximumTextureSize = RenderSystem.maxSupportedTextureSize();
    }

    public SheetData stitch(IResourceManager resourceManagerIn, IProfiler profilerIn, int maxMipmapLevelIn) {
        return stitch(resourceManagerIn, null, profilerIn, maxMipmapLevelIn);
    }

    @Override
    public SheetData stitch(IResourceManager resourceManagerIn, Stream<ResourceLocation> resourceLocationsIn, IProfiler profilerIn, int maxMipmapLevelIn) {
        profilerIn.startSection("preparing");
        int i = this.maximumTextureSize;
        Stitcher stitcher = new Stitcher(i, i, maxMipmapLevelIn);
        int k = 1 << maxMipmapLevelIn;
        profilerIn.endStartSection("extracting_frames");

        Set<ResourceLocation> set = ThickRingTextureManager.getThickRingResourceLocations();
        net.minecraftforge.client.ForgeHooksClient.onTextureStitchedPre(this, set);

        for(TextureAtlasSprite.Info textureatlassprite$info : this.makeSprites(resourceManagerIn, set)) {
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

    private Collection<TextureAtlasSprite.Info> makeSprites(IResourceManager resourceManagerIn, Set<ResourceLocation> spriteLocationsIn) {
        ConcurrentLinkedQueue<TextureAtlasSprite.Info> concurrentlinkedqueue = new ConcurrentLinkedQueue<>();

        for(ResourceLocation resourcelocation : spriteLocationsIn) {
            if (!MissingTextureSprite.getLocation().equals(resourcelocation)) {
//                ResourceLocation resourcelocation1 = ThickRingTextureManager.thickRingTextures.get(resourcelocation);

                TextureAtlasSprite.Info textureatlassprite$info;

                Pair<Integer, Integer> pair = AnimationMetadataSection.EMPTY.getSpriteSize(16*spriteSizeMultiplier, 16*spriteSizeMultiplier);
                textureatlassprite$info = new TextureAtlasSprite.Info(resourcelocation, pair.getFirst(), pair.getSecond(), AnimationMetadataSection.EMPTY);

                concurrentlinkedqueue.add(textureatlassprite$info);

//                try (IResource iresource = resourceManagerIn.getResource(resourcelocation1)) {
//                    PngSizeInfo pngsizeinfo = new PngSizeInfo(iresource.toString(), iresource.getInputStream());
//
//                    Pair<Integer, Integer> pair = AnimationMetadataSection.EMPTY.getSpriteSize(pngsizeinfo.width*spriteSizeMultiplier, pngsizeinfo.height*spriteSizeMultiplier);
//                    textureatlassprite$info = new TextureAtlasSprite.Info(resourcelocation, pair.getFirst(), pair.getSecond(), AnimationMetadataSection.EMPTY);
//
//                    concurrentlinkedqueue.add(textureatlassprite$info);
//                } catch (RuntimeException runtimeexception) {
//                    LOGGER.error("Unable to parse metadata from {} : {}", resourcelocation1, runtimeexception);
//                } catch (IOException ioexception) {
//                    LOGGER.error("Using missing texture, unable to load {} : {}", resourcelocation1, ioexception);
//                }
            }
        }

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
                    TextureAtlasSprite textureatlassprite = new ThickRingTextureAtlasSprite(this, atlasInfo, mipmapLevelIn, atlasWidth, atlasHeight, x, y);
                    concurrentlinkedqueue.add(textureatlassprite);

                }, Util.getServerExecutor()));
            }

        });
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        return Lists.newArrayList(concurrentlinkedqueue);
    }

}
