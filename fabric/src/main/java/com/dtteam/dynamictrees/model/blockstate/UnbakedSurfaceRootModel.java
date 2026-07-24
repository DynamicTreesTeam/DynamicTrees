package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.model.baked.SurfaceRootBlockBakedModel;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;

/**
 * Fabric port of the NeoForge {@code SurfaceRootBlockStateModel.Unbaked}; deserialized from
 * blockstate JSONs with type {@code dynamictrees:surface_root}.
 */
public record UnbakedSurfaceRootModel(Identifier barkTexture) implements CustomUnbakedBlockStateModel {

    public static final String TEXTURES = "textures";
    public static final String BARK_TEXTURE = "bark";

    private record Textures(Identifier bark) {
        public static final MapCodec<Textures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf(BARK_TEXTURE).forGetter(Textures::bark)
        ).apply(i, Textures::new));
    }

    public static final MapCodec<UnbakedSurfaceRootModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Textures.CODEC.codec().fieldOf(TEXTURES).forGetter(m -> new Textures(m.barkTexture()))
    ).apply(i, textures -> new UnbakedSurfaceRootModel(textures.bark())));

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {}

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        return SurfaceRootBlockBakedModel.bake(baker, barkTexture);
    }
}
