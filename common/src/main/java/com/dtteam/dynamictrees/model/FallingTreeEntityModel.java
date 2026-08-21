package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.api.network.BranchDestructionData;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.stream.Collectors;

public class FallingTreeEntityModel {

    protected final List<TreeQuadData> quads;
    protected final int entityId;
    protected final Species species;

    public FallingTreeEntityModel(FallingTreeEntity entity) {
        BranchDestructionData destructionData = entity.getDestroyData();
        this.species = destructionData.species;
        this.quads = generateTreeQuads(entity);
        this.entityId = entity.getId();
    }

    public List<TreeQuadData> getQuads() {
        return quads;
    }

    public int getEntityId() {
        return entityId;
    }

    public Species getSpecies() {
        return species;
    }

    public static int getBrightness(FallingTreeEntity entity) {
        final BranchDestructionData destructionData = entity.getDestroyData();
        final Level world = entity.level();
        return world.getBlockState(destructionData.cutPos).getLightEmission();
    }

    public List<TreeQuadData> generateTreeQuads(FallingTreeEntity entity) {
        return List.of();
    }

    public static List<TreeQuadData> toTreeQuadData(List<BakedQuad> bakedQuads, BlockState state) {
        return toTreeQuadData(bakedQuads, 0xFFFFFF, state);
    }

    public static List<TreeQuadData> toTreeQuadData(List<BakedQuad> bakedQuads, int defaultColor, BlockState state) {
        return bakedQuads.stream().map(bakedQuad -> new TreeQuadData(bakedQuad, defaultColor, state)).collect(Collectors.toList());
    }

    public record TreeQuadData(BakedQuad bakedQuad, int color, BlockState state) { }
}
