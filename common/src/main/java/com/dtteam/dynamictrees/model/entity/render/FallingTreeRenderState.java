package com.dtteam.dynamictrees.model.entity.render;

import com.dtteam.dynamictrees.model.entity.FallingTreeEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

import java.util.function.Consumer;

public class FallingTreeRenderState extends EntityRenderState {

    public boolean isClientBuilt;
    public Consumer<PoseStack> renderAnimation;
    public FallingTreeEntityModel model;

}
