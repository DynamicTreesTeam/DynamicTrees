package com.ferreusveritas.dynamictrees.client.thickrings;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.StitcherException;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ThickRingAtlasTexture extends TextureAtlas {

    private static final Logger LOGGER = LogManager.getLogger();
    private final int maximumTextureSize;

    private static final int spriteSizeMultiplier = 3;

    public ThickRingAtlasTexture() {
        super(ThickRingTextureManager.LOCATION_THICKRINGS_TEXTURE);
        maximumTextureSize = RenderSystem.maxSupportedTextureSize();
    }

    // @Override
    // public TextureAtlasSprite getSprite(ResourceLocation resloc) {
    //     TextureAtlasSprite sprite = super.getSprite(resloc);
    //     if (sprite instanceof ThickRingTextureAtlasSprite){
    //         ((ThickRingTextureAtlasSprite) sprite).loadAtlasTexture();
    //     }
    //     return sprite;
    // }


    private static boolean uploaded = false;

    @Override
    public void reload(Preparations sheetData) {
        if (!uploaded) {
            super.reload(sheetData);
            uploaded = true;
        }
    }

    public TextureAtlas.Preparations prepareToStitch(ResourceManager resourceManagerIn, Stream<ResourceLocation> resourceLocationsIn, ProfilerFiller profilerIn, int maxMipmapLevelIn) {
        profilerIn.push("preparing");
        Set<ResourceLocation> set = resourceLocationsIn.peek((resloc) -> {
            if (resloc == null) {
                throw new IllegalArgumentException("Location cannot be null!");
            }
        }).collect(Collectors.toSet());
        int i = this.maximumTextureSize;
        Stitcher stitcher = new Stitcher(i, i, maxMipmapLevelIn);
        int j = Integer.MAX_VALUE;
        int k = 1 << maxMipmapLevelIn;
        profilerIn.popPush("extracting_frames");
        net.minecraftforge.client.ForgeHooksClient.onTextureStitchedPre(this, set);

        for (TextureAtlasSprite.Info spriteInfo : this.makeSprites(resourceManagerIn, set)) {
            int spriteWidth = spriteInfo.width() * spriteSizeMultiplier;
            int spriteHeight = spriteInfo.height() * spriteSizeMultiplier;
            j = Math.min(j, Math.min(spriteWidth, spriteHeight));
            int l = Math.min(Integer.lowestOneBit(spriteWidth), Integer.lowestOneBit(spriteHeight));
            if (l < k) {
                LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", spriteInfo.name(), spriteWidth, spriteHeight, Mth.log2(k), Mth.log2(l));
                k = l;
            }

            stitcher.registerSprite(spriteInfo);
        }

        int i1 = Math.min(j, k);
        int j1 = Mth.log2(i1);
        int k1 = maxMipmapLevelIn;
        if (false) // FORGE: do not lower the mipmap level
        {
            if (j1 < maxMipmapLevelIn) {
                LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", ThickRingTextureManager.LOCATION_THICKRINGS_TEXTURE, maxMipmapLevelIn, j1, i1);
                k1 = j1;
            } else {
                k1 = maxMipmapLevelIn;
            }
        }

        profilerIn.popPush("register");
        stitcher.registerSprite(MissingTextureAtlasSprite.info());
        profilerIn.popPush("stitching");

        try {
            stitcher.stitch();
        } catch (StitcherException stitcherexception) {
            CrashReport crashreport = CrashReport.forThrowable(stitcherexception, "Stitching");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Stitcher");
            crashreportcategory.setDetail("Sprites", stitcherexception.getAllSprites().stream().map((p_229216_0_) -> {
                return String.format("%s[%dx%d]", p_229216_0_.name(), p_229216_0_.width(), p_229216_0_.height());
            }).collect(Collectors.joining(",")));
            crashreportcategory.setDetail("Max Texture Size", i);
            throw new ReportedException(crashreport);
        }

        profilerIn.popPush("loading");
        List<TextureAtlasSprite> list = this.getStitchedSprites(resourceManagerIn, stitcher, k1);
        profilerIn.pop();
        return new TextureAtlas.Preparations(set, stitcher.getWidth(), stitcher.getHeight(), k1, list);
    }

    private Collection<TextureAtlasSprite.Info> makeSprites(ResourceManager resourceManagerIn, Set<ResourceLocation> spriteLocationsIn) {
        List<CompletableFuture<?>> list = Lists.newArrayList();
        ConcurrentLinkedQueue<TextureAtlasSprite.Info> concurrentlinkedqueue = new ConcurrentLinkedQueue<>();

        for (ResourceLocation thickSpriteLocation : spriteLocationsIn) {
            if (!MissingTextureAtlasSprite.getLocation().equals(thickSpriteLocation)) {
                list.add(CompletableFuture.runAsync(() -> {
                    ResourceLocation baseSpriteLocation = ThickRingTextureManager.getBaseRingFromThickRing(thickSpriteLocation);
                    ResourceLocation baseSpritePath = this.getSpritePath(baseSpriteLocation);

                    resourceManagerIn.getResource(baseSpritePath).ifPresentOrElse(baseRingResource -> {
                        try {
                            PngInfo pngsizeinfo = new PngInfo(baseRingResource::toString, baseRingResource.open());
                            AnimationMetadataSection animationmetadatasection = baseRingResource.metadata().getSection(AnimationMetadataSection.SERIALIZER)
                                    .orElse(AnimationMetadataSection.EMPTY);

                            Pair<Integer, Integer> pair = animationmetadatasection.getFrameSize(pngsizeinfo.width, pngsizeinfo.height);
                            concurrentlinkedqueue.add(new TextureAtlasSprite.Info(baseSpriteLocation, pair.getFirst(), pair.getSecond(), animationmetadatasection));
                        } catch (RuntimeException runtimeexception) {
                            LOGGER.error("Unable to parse metadata from {} : {}", baseSpritePath, runtimeexception);
                        } catch (IOException ioexception) {
                            LOGGER.error("Using missing texture, unable to load {}", baseSpritePath, ioexception);
                        }
                    }, () -> LOGGER.error("Using missing texture, unable to load {}", baseSpritePath));
                }, Util.backgroundExecutor()));
            }
        }

        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        return concurrentlinkedqueue;
    }

    private List<TextureAtlasSprite> getStitchedSprites(ResourceManager resourceManagerIn, Stitcher stitcherIn, int mipmapLevelIn) {
        ConcurrentLinkedQueue<TextureAtlasSprite> concurrentlinkedqueue = new ConcurrentLinkedQueue<>();
        List<CompletableFuture<?>> list = Lists.newArrayList();
        stitcherIn.gatherSprites((spriteInfo, width, height, x, y) -> {
            if (spriteInfo == MissingTextureAtlasSprite.info()) {
                MissingTextureAtlasSprite missingtexturesprite = MissingTextureAtlasSprite.newInstance(this, mipmapLevelIn, width, height, x, y);
                concurrentlinkedqueue.add(missingtexturesprite);
            } else {
                list.add(CompletableFuture.runAsync(() -> {
                    TextureAtlasSprite textureatlassprite = this.loadSprite(resourceManagerIn, spriteInfo, width, height, mipmapLevelIn, x, y);
                    if (textureatlassprite != null) {
                        concurrentlinkedqueue.add(textureatlassprite);
                    }

                }, Util.backgroundExecutor()));
            }

        });
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        return Lists.newArrayList(concurrentlinkedqueue);
    }

    @Nullable
    private TextureAtlasSprite loadSprite(ResourceManager resourceManagerIn, TextureAtlasSprite.Info spriteInfoIn, int widthIn, int heightIn, int mipmapLevelIn, int originX, int originY) {
        ResourceLocation baseSpritePath = this.getSpritePath(spriteInfoIn.name());

        TextureAtlasSprite.Info thickSpriteInfo = new TextureAtlasSprite.Info(
                ThickRingTextureManager.getThickRingFromBaseRing(spriteInfoIn.name()),
                spriteInfoIn.width() * spriteSizeMultiplier,
                spriteInfoIn.height() * spriteSizeMultiplier,
                AnimationMetadataSection.EMPTY);

        Optional<Resource> resourceOpt = resourceManagerIn.getResource(baseSpritePath);
        if (resourceOpt.isPresent()) {
            try (InputStream inputStream = resourceOpt.get().open()) {
                NativeImage nativeimage = NativeImage.read(inputStream);
                TextureAtlasSprite thinRings = new TextureAtlasSprite(this, spriteInfoIn, mipmapLevelIn, widthIn, heightIn, originX, originY, nativeimage) {};
                return new ThickRingTextureAtlasSprite(this, thickSpriteInfo, mipmapLevelIn, widthIn, heightIn, originX, originY, thinRings, baseSpritePath);
            } catch (RuntimeException runtimeexception) {
                LOGGER.error("Unable to parse metadata from {}", baseSpritePath, runtimeexception);
            } catch (IOException ioexception) {
                LOGGER.error("Using missing texture, unable to load {}", baseSpritePath, ioexception);
            }
        }

        return null;
    }

    private ResourceLocation getSpritePath(ResourceLocation location) {
        return new ResourceLocation(location.getNamespace(), String.format("textures/%s%s", location.getPath(), ".png"));
    }

}