package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
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
        BranchBlockStateModel regular = bakeRegular(baker);
        if (family.isPresent() && family.get().isThick()){
            return bakeThick(baker, regular);
        }
        return regular;
    }

    public BranchBlockStateModel bakeRegular(ModelBaker baker) {
        BranchModelPart[][] sleeves = new BranchModelPart[6][7];
        BranchModelPart[][] cores = new BranchModelPart[3][8]; // 8 Cores for 3 axis with the bark texture all all 6 sides rotated appropriately.
        BranchModelPart[] rings = new BranchModelPart[8]; // 8 Cores with the ring textures on all 6 sides.

        Material.Baked barkMat = baker.materials().get(new Material(barkTexture, false), barkTexture::toDebugFileName);
        Material.Baked ringsMat = baker.materials().get(new Material(ringsTexture, false), ringsTexture::toDebugFileName);

        BranchModelPart.UnbakedCore unbakedCores = new BranchModelPart.UnbakedCore(barkMat, false);
        BranchModelPart.UnbakedSleeve unbakedSleeves = new BranchModelPart.UnbakedSleeve(barkMat);
        BranchModelPart.UnbakedCore unbakedRings = new BranchModelPart.UnbakedCore(ringsMat, false);

        for (int i = 0; i < 8; i++) {
            int radius = i + 1;
            if (radius < 8) {
                for (Direction dir : Direction.values()) {
                    sleeves[dir.get3DDataValue()][i] = unbakedSleeves.bake(baker, radius, dir);
                }
            }
            cores[0][i] = unbakedCores.bake(baker, radius, Direction.Axis.Y); //DOWN<->UP
            cores[1][i] = unbakedCores.bake(baker, radius, Direction.Axis.Z); //NORTH<->SOUTH
            cores[2][i] = unbakedCores.bake(baker, radius, Direction.Axis.X); //WEST<->EAST

            rings[i] = unbakedRings.bake(baker, radius, Direction.Axis.Y);
        }

        return new BranchBlockStateModel(cores, sleeves, rings, barkMat);
    }

    public ThickBranchBlockStateModel bakeThick(ModelBaker baker, BranchBlockStateModel fallback) {
        BranchModelPart[] trunksSideBark = new BranchModelPart[16]; // The trunk will always feature bark on its sides.
        BranchModelPart[] trunksTopBark = new BranchModelPart[16]; // The trunk will feature bark on its top when there's a branch on top of it.
        BranchModelPart[] trunksTopRings = new BranchModelPart[16]; // The trunk will feature rings on its top when there's no branches on top of it.
        BranchModelPart[] trunksBotRings = new BranchModelPart[16]; // The trunk will always feature rings on its bottom surface if nothing is below it.

        Identifier thickRings = IdentifierUtils.suffix(ringsTexture, "_thick");

        Material.Baked barkMat = baker.materials().get(new Material(barkTexture, false), barkTexture::toDebugFileName);
        Material.Baked ringsMat = baker.materials().get(new Material(thickRings, false), thickRings::toDebugFileName);

        BranchModelPart.UnbakedThickTrunk unbakedThickTrunk = new BranchModelPart.UnbakedThickTrunk(barkMat, false);
        BranchModelPart.UnbakedThickTrunk unbakedThickRings = new BranchModelPart.UnbakedThickTrunk(ringsMat, true);

        for (int i = 0; i < ThickBranchBlock.MAX_RADIUS_THICK - BranchBlock.MAX_RADIUS; i++) {
            int radius = i + BranchBlock.MAX_RADIUS + 1;
            trunksSideBark[i] = unbakedThickTrunk.bake(baker, radius, EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST));
            trunksTopBark[i] = unbakedThickTrunk.bake(baker, radius, EnumSet.of(Direction.UP, Direction.DOWN));
            trunksTopRings[i] = unbakedThickRings.bake(baker, radius, EnumSet.of(Direction.UP));
            trunksBotRings[i] = unbakedThickRings.bake(baker, radius, EnumSet.of(Direction.DOWN));
        }

        return new ThickBranchBlockStateModel(fallback, trunksSideBark, trunksTopBark, trunksTopRings, trunksBotRings);
    }
}