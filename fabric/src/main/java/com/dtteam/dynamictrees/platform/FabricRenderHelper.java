package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.FallingTreeEntityModel;
import com.dtteam.dynamictrees.platform.services.IMiscHelper;
import com.dtteam.dynamictrees.worldgen.IDTBiomeHolderSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public class FabricRenderHelper implements IMiscHelper {

    @Override
    public int getPixelRGBA(TextureAtlasSprite sprite, int x, int y) {
        return 0;
    }

    @Override
    public FallingTreeEntityModel newFallingTreeEntityModel(FallingTreeEntity entity) {
        return new FallingTreeEntityModel(entity);
    }

    @Override
    public boolean isLevelRestoringBlockSnapshots(Level level) {
        return false;
    }

    @Override
    public MinecraftServer getCurrentServer() {
        return null;
    }

    @Override
    public IDTBiomeHolderSet newDTBiomeHolderSet() {
        return null;
    }
}
