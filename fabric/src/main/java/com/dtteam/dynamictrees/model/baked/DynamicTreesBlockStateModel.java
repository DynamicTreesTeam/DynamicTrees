package com.dtteam.dynamictrees.model.baked;

import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * Fabric 26.2 branch/root models implement {@link FabricBlockStateModel#emitQuads}.
 */
public class DynamicTreesBlockStateModel implements BlockStateModel, FabricBlockStateModel {
    private final Material.Baked particle;

    public DynamicTreesBlockStateModel(TextureAtlasSprite particleSprite) {
        this.particle = new Material.Baked(particleSprite, false);
    }

    public DynamicTreesBlockStateModel(Material.Baked particle) {
        this.particle = particle;
    }

    @Override
    public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
    }

    @Override
    public Material.Baked particleMaterial() {
        return particle;
    }

    @Override
    public int materialFlags() {
        return 0;
    }
}
