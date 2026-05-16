package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import com.dtteam.dynamictrees.tree.family.Family;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.Optional;

public record UnbakedCreakingHeartModel(Identifier heartBark, Identifier heartRings, Identifier bark, Optional<Family> family) implements CustomUnbakedBlockStateModel {

    public static final String BARK_TEXTURE = "bark";
    public static final String HEART_BARK_TEXTURE = "heart_bark";
    public static final String HEART_RINGS_TEXTURE = "heart_rings";
    public static final String TEXTURES = "textures";
    public static final String FAMILY = "family";

    private record HeartTextures(Identifier bark, Identifier heartBark, Identifier heartRings) {
        public static final MapCodec<HeartTextures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf(BARK_TEXTURE).forGetter(HeartTextures::bark),
                Identifier.CODEC.fieldOf(HEART_BARK_TEXTURE).forGetter(HeartTextures::heartBark),
                Identifier.CODEC.fieldOf(HEART_RINGS_TEXTURE).forGetter(HeartTextures::heartRings)
        ).apply(i, HeartTextures::new));
    }

    public static final MapCodec<UnbakedCreakingHeartModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            HeartTextures.CODEC.codec().fieldOf(TEXTURES).forGetter(m -> new HeartTextures(m.bark, m.heartBark, m.heartRings)),
            Family.CODEC.optionalFieldOf(FAMILY).forGetter(UnbakedCreakingHeartModel::family)
    ).apply(i, (textures, family) -> new UnbakedCreakingHeartModel(textures.heartBark, textures.heartRings, textures.bark, family)));

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {}

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        Material.Baked heartMat = baker.materials().get(new Material(heartBark), heartBark::toDebugFileName);
        Material.Baked ringsMat = baker.materials().get(new Material(heartRings), heartRings::toDebugFileName);
        Material.Baked barkMat = baker.materials().get(new Material(bark), bark::toDebugFileName);

        return UnbakedBranchModel.bakeBasic(baker,
                new BranchModelPart.UnbakedHeartCore(heartMat, barkMat),
                new BranchModelPart.UnbakedSleeve(heartMat),
                new BranchModelPart.UnbakedCore(ringsMat),
                null);
    }

}