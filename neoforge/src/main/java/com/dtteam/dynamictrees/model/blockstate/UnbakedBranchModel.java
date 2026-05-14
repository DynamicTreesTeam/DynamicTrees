package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.model.BranchMultiPartHolder;
import com.dtteam.dynamictrees.model.parts.BranchModelPart;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Optional;

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

        BranchBlockStateModel regular = bakeBasic(baker,
                new BranchModelPart.UnbakedCore(barkMat),
                new BranchModelPart.UnbakedSleeve(barkMat),
                new BranchModelPart.UnbakedCore(ringsMat),
                null);

        if (family.isPresent() && family.get().isThick()){
            Identifier thickRings = IdentifierUtils.suffix(ringsTexture, "_thick");
            Material.Baked thickRingsMat = baker.materials().get(new Material(thickRings), thickRings::toDebugFileName);

            return bakeThick(baker, regular,
                    new BranchModelPart.UnbakedThickTrunk(barkMat, false),
                    new BranchModelPart.UnbakedThickTrunk(thickRingsMat, true));
        }
        return regular;
    }

    public static BranchBlockStateModel bakeBasic(
            ModelBaker baker, BranchModelPart.UnbakedCore unbakedCores, BranchModelPart.UnbakedSleeve unbakedSleeves, BranchModelPart.UnbakedCore unbakedRings, @Nullable BranchModelPart.UnbakedSleeve unbakedSleeveRings
            ) {
        BranchMultiPartHolder sleeves = new BranchMultiPartHolder();
        BranchMultiPartHolder cores = new BranchMultiPartHolder();
        BranchMultiPartHolder rings = new BranchMultiPartHolder();
        BranchMultiPartHolder sleeveRings = new BranchMultiPartHolder();

        for (int radius = 1; radius <= BranchBlock.MAX_RADIUS; radius++) {
            if (radius < BranchBlock.MAX_RADIUS) {
                sleeves.putAllParts(radius, unbakedSleeves.bakeAllSides(baker, radius));
                if (unbakedSleeveRings != null)
                    sleeveRings.putAllParts(radius, unbakedSleeveRings.bakeAllSides(baker, radius));
            }

            cores.putAllParts(Direction.Axis.Y, radius, unbakedCores.bakeAllSides(baker, radius, Direction.Axis.Y)); //DOWN<->UP
            cores.putAllParts(Direction.Axis.Z, radius, unbakedCores.bakeAllSides(baker, radius, Direction.Axis.Z)); //NORTH<->SOUTH
            cores.putAllParts(Direction.Axis.X, radius, unbakedCores.bakeAllSides(baker, radius, Direction.Axis.X)); //WEST<->EAST

            rings.putAllParts(radius, unbakedRings.bakeAllSides(baker, radius, Direction.Axis.Y));
        }

        return new BranchBlockStateModel(cores, sleeves, rings, sleeveRings);
    }

    public static ThickBranchBlockStateModel bakeThick(
            ModelBaker baker, BranchBlockStateModel fallback,
            BranchModelPart.UnbakedThickTrunk unbakedBark, BranchModelPart.UnbakedThickTrunk unbakedRings) {
        BranchMultiPartHolder trunksBark = new BranchMultiPartHolder(); // The trunk will always feature bark on its sides.
        BranchMultiPartHolder trunksRings = new BranchMultiPartHolder(); // The trunk will feature rings on its top and bottom.

        for (int radius = BranchBlock.MAX_RADIUS + 1; radius <= ThickBranchBlock.MAX_RADIUS_THICK; radius++) {
            trunksBark.putAllParts(radius, unbakedBark.bakeAllSides(baker, radius));
            trunksRings.putAllParts(radius, unbakedRings.bakeSides(baker, radius, EnumSet.of(Direction.UP, Direction.DOWN)));
        }

        return new ThickBranchBlockStateModel(fallback, trunksBark, trunksRings);
    }
}