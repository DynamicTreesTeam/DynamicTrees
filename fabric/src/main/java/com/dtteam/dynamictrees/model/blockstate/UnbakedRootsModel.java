package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.model.baked.BasicRootsBlockBakedModel;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * Fabric port of the NeoForge {@code UnbakedRootsModel}; deserialized from blockstate JSONs
 * with type {@code dynamictrees:roots}.
 */
public record UnbakedRootsModel(Identifier side, Identifier top, boolean opaque) implements CustomUnbakedBlockStateModel {

    public static final String SIDE = "side";
    public static final String TOP = "top";
    public static final String OPAQUE = "opaque";
    public static final String TEXTURES = "textures";

    private record RootsTextures(Identifier exposedSide, Identifier exposedTop) {
        public static final MapCodec<RootsTextures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf(SIDE).forGetter(RootsTextures::exposedSide),
                Identifier.CODEC.fieldOf(TOP).forGetter(RootsTextures::exposedTop)
        ).apply(i, RootsTextures::new));
    }

    public static final MapCodec<UnbakedRootsModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RootsTextures.CODEC.codec().fieldOf(TEXTURES).forGetter(m -> new RootsTextures(m.side, m.top)),
            Codec.BOOL.optionalFieldOf(OPAQUE).forGetter(o -> Optional.of(o.opaque))
    ).apply(i, (textures, opaque) -> new UnbakedRootsModel(textures.exposedSide, textures.exposedTop, opaque.orElse(false))));

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {}

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        Material.Baked barkMat = baker.materials().get(new Material(side), side::toDebugFileName);
        Material.Baked ringsMat = baker.materials().get(new Material(top), top::toDebugFileName);

        return BasicRootsBlockBakedModel.bakeRoots(baker, barkMat, ringsMat, opaque);
    }
}
