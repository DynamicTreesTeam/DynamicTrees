package com.dtteam.dynamictrees.client;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Optional;

public class ThickBranchRingsSource implements SpriteSource {
    public static final Identifier ID = DynamicTrees.location("thick_branch_rings");
    public static final MapCodec<ThickBranchRingsSource> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                    Identifier.CODEC.fieldOf("resource").forGetter((singleFile) -> singleFile.resourceId)
            ).apply(instance, ThickBranchRingsSource::new)
    );
    private final Identifier resourceId;

    public ThickBranchRingsSource(Identifier resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        Identifier identifier = TEXTURE_ID_CONVERTER.idToFile(this.resourceId);
        Optional<Resource> optional = resourceManager.getResource(identifier);
        if (optional.isPresent()) {
            Identifier location = IdentifierUtils.suffix(this.resourceId, "_thick");
            output.add(location, (spriteLoader) -> {
                SpriteContents base = spriteLoader.loadSprite(location, optional.get());
                if (base == null) return null;
                return new ThickBranchRingsSprite(location, base);
            });
        } else {
            DynamicTrees.LOG.warn("Missing sprite: {}", identifier);
        }
    }

    @Override
    public MapCodec<? extends SpriteSource> codec() {
        return CODEC;
    }

}
