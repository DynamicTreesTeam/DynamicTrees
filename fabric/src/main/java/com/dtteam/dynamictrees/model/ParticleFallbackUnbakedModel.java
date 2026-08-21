package com.dtteam.dynamictrees.model;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;

/**
 * Copies an unbaked model and fills in a missing {@code particle} texture slot.
 */
public final class ParticleFallbackUnbakedModel implements UnbakedModel {

    private final UnbakedModel inner;
    private final TextureSlots.Data textureSlots;

    public ParticleFallbackUnbakedModel(UnbakedModel inner, TextureSlots.Data textureSlots) {
        this.inner = inner;
        this.textureSlots = textureSlots;
    }

    @Override
    public Boolean ambientOcclusion() {
        return inner.ambientOcclusion();
    }

    @Override
    public UnbakedModel.GuiLight guiLight() {
        return inner.guiLight();
    }

    @Override
    public ItemTransforms transforms() {
        return inner.transforms();
    }

    @Override
    public TextureSlots.Data textureSlots() {
        return textureSlots;
    }

    @Override
    public UnbakedGeometry geometry() {
        return inner.geometry();
    }

    @Override
    public Identifier parent() {
        return inner.parent();
    }
}
