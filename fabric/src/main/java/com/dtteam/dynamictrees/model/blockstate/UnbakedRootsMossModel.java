package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

/**
 * Fabric port of the NeoForge {@code UnbakedRootsMossModel}; deserialized from blockstate JSONs
 * with type {@code dynamictrees:roots_moss}.
 */
public record UnbakedRootsMossModel(Identifier moss) implements CustomUnbakedBlockStateModel {

    public static final String MOSS = "moss";
    public static final String TEXTURES = "textures";

    private record RootsTextures(Identifier moss) {
        public static final MapCodec<RootsTextures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf(MOSS).forGetter(RootsTextures::moss)
        ).apply(i, RootsTextures::new));
    }

    public static final MapCodec<UnbakedRootsMossModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RootsTextures.CODEC.codec().fieldOf(TEXTURES).forGetter(m -> new RootsTextures(m.moss))
    ).apply(i, textures -> new UnbakedRootsMossModel(textures.moss)));

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {}

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        Material.Baked mossMat = baker.materials().get(new Material(moss), moss::toDebugFileName);

        return BasicBranchBlockBakedModel.bakeBasic(baker,
                new BranchModelPart.UnbakedMossCore(mossMat),
                new BranchModelPart.UnbakedMossSleeve(mossMat),
                new BranchModelPart.UnbakedMossCore(mossMat),
                null);
    }
}
