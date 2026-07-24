package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.model.baked.BasicBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.baked.ThickBranchBlockBakedModel;
import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Fabric port of the NeoForge {@code UnbakedBranchModel}; deserialized from blockstate JSONs
 * with type {@code dynamictrees:branch}.
 */
public record UnbakedBranchModel(Identifier barkTexture, Identifier ringsTexture, Optional<Family> family) implements CustomUnbakedBlockStateModel {

    public static final String BARK_TEXTURE = "bark";
    public static final String RINGS_TEXTURE = "rings";
    public static final String TEXTURES = "textures";
    public static final String FAMILY = "family";

    private record BranchTextures(Identifier bark, Identifier rings) {
        public static final MapCodec<BranchTextures> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf(BARK_TEXTURE).forGetter(BranchTextures::bark),
                Identifier.CODEC.fieldOf(RINGS_TEXTURE).forGetter(BranchTextures::rings)
        ).apply(i, BranchTextures::new));
    }

    public static final MapCodec<UnbakedBranchModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BranchTextures.CODEC.codec().fieldOf(TEXTURES).forGetter(m -> new BranchTextures(m.barkTexture(), m.ringsTexture())),
            Family.CODEC.optionalFieldOf(FAMILY).forGetter(UnbakedBranchModel::family)
    ).apply(i, (textures, family) -> new UnbakedBranchModel(textures.bark(), textures.rings(), family)));

    @Override
    public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
        return CODEC;
    }

    @Override
    public void resolveDependencies(ResolvableModel.Resolver resolver) {}

    @Override
    public BlockStateModel bake(ModelBaker baker) {
        Material.Baked barkMat = baker.materials().get(new Material(barkTexture), barkTexture::toDebugFileName);
        Material.Baked ringsMat = baker.materials().get(new Material(ringsTexture), ringsTexture::toDebugFileName);

        BasicBranchBlockBakedModel regular = BasicBranchBlockBakedModel.bakeBasic(baker,
                new BranchModelPart.UnbakedCore(barkMat),
                new BranchModelPart.UnbakedSleeve(barkMat),
                new BranchModelPart.UnbakedCore(ringsMat),
                null);

        if (family.isPresent() && family.get().isThick()) {
            Identifier thickRings = getThickRingsTexture(ringsTexture);
            Material.Baked thickRingsMat = baker.materials().get(new Material(thickRings), thickRings::toDebugFileName);

            return ThickBranchBlockBakedModel.bakeThick(baker, regular,
                    new BranchModelPart.UnbakedThickTrunk(barkMat, false),
                    new BranchModelPart.UnbakedThickTrunk(thickRingsMat, true));
        }
        return regular;
    }

    private @NotNull Identifier getThickRingsTexture(Identifier ringsTexture) {
        if (ringsTexture.equals(DynamicTrees.location("block/air")))
            return ringsTexture;
        return IdentifierUtils.suffix(ringsTexture, "_thick");
    }
}
