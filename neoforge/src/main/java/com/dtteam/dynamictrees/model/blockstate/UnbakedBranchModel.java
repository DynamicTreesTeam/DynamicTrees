package com.dtteam.dynamictrees.model.blockstate;

import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.ThickBranchBlock;
import com.dtteam.dynamictrees.model.parts.BranchBlockStateModelPart;
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

    public static final String BARK_TEXTURE = "bark_texture";
    public static final String RINGS_TEXTURE = "rings_texture";
    public static final String FAMILY = "family";

    public static final MapCodec<UnbakedBranchModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Identifier.CODEC.fieldOf(BARK_TEXTURE).forGetter(UnbakedBranchModel::barkTexture),
            Identifier.CODEC.fieldOf(RINGS_TEXTURE).forGetter(UnbakedBranchModel::ringsTexture),
            Family.CODEC.optionalFieldOf(FAMILY).forGetter(UnbakedBranchModel::family)
    ).apply(i, UnbakedBranchModel::new));

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
        BranchBlockStateModelPart[][] sleeves = new BranchBlockStateModelPart[6][7];
        BranchBlockStateModelPart[][] cores = new BranchBlockStateModelPart[3][8]; // 8 Cores for 3 axis with the bark texture all all 6 sides rotated appropriately.
        BranchBlockStateModelPart[] rings = new BranchBlockStateModelPart[8]; // 8 Cores with the ring textures on all 6 sides.

        Material.Baked barkMat = baker.materials().get(new Material(barkTexture, false), barkTexture::toDebugFileName);
        Material.Baked ringsMat = baker.materials().get(new Material(ringsTexture, false), ringsTexture::toDebugFileName);

        BranchBlockStateModelPart.UnbakedCore unbakedCores = new BranchBlockStateModelPart.UnbakedCore(barkMat, false);
        BranchBlockStateModelPart.UnbakedSleeve unbakedSleeves = new BranchBlockStateModelPart.UnbakedSleeve(barkMat);
        BranchBlockStateModelPart.UnbakedCore unbakedRings = new BranchBlockStateModelPart.UnbakedCore(ringsMat, false);

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
        BranchBlockStateModelPart[] trunksSideBark = new BranchBlockStateModelPart[16]; // The trunk will always feature bark on its sides.
        BranchBlockStateModelPart[] trunksTopBark = new BranchBlockStateModelPart[16]; // The trunk will feature bark on its top when there's a branch on top of it.
        BranchBlockStateModelPart[] trunksTopRings = new BranchBlockStateModelPart[16]; // The trunk will feature rings on its top when there's no branches on top of it.
        BranchBlockStateModelPart[] trunksBotRings = new BranchBlockStateModelPart[16]; // The trunk will always feature rings on its bottom surface if nothing is below it.

        Identifier thickRings = IdentifierUtils.suffix(ringsTexture, "_thick");

        Material.Baked barkMat = baker.materials().get(new Material(barkTexture, false), barkTexture::toDebugFileName);
        Material.Baked ringsMat = baker.materials().get(new Material(thickRings, false), thickRings::toDebugFileName);

        BranchBlockStateModelPart.UnbakedThickTrunk unbakedThickTrunk = new BranchBlockStateModelPart.UnbakedThickTrunk(barkMat, false);
        BranchBlockStateModelPart.UnbakedThickTrunk unbakedThickRings = new BranchBlockStateModelPart.UnbakedThickTrunk(ringsMat, true);

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