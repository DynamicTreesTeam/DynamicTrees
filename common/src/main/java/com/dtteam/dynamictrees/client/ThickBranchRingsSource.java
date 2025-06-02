package com.dtteam.dynamictrees.client;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.utility.ResourceLocationUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Optional;

public class ThickBranchRingsSource implements SpriteSource {
    public static final ResourceLocation ID = DynamicTrees.location("thick_branch_rings");
    public static final MapCodec<ThickBranchRingsSource> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                    ResourceLocation.CODEC.fieldOf("resource").forGetter((singleFile) -> singleFile.resourceId)
            ).apply(instance, ThickBranchRingsSource::new)
    );
    public static SpriteSourceType TYPE;
    private final ResourceLocation resourceId;

    public ThickBranchRingsSource(ResourceLocation resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        ResourceLocation resourceLocation = TEXTURE_ID_CONVERTER.idToFile(this.resourceId);
        Optional<Resource> optional = resourceManager.getResource(resourceLocation);
        if (optional.isPresent()) {
            ResourceLocation location = ResourceLocationUtils.suffix(this.resourceId, "_thick");
            output.add(location, (spriteLoader) -> {
                SpriteContents base = spriteLoader.loadSprite(location, optional.get());
                if (base == null) return null;
                return new ThickBranchRingsSprite(location, base);
            });
        } else {
            DynamicTrees.LOG.warn("Missing sprite: {}", resourceLocation);
        }
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }

    public static SpriteSourceType setType (MapCodec<ThickBranchRingsSource> codec){
        TYPE = new SpriteSourceType(codec);
        return TYPE;
    }

}
