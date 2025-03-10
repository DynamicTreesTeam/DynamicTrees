package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.FallingTreeEntityModel;
import com.dtteam.dynamictrees.platform.services.IMiscHelper;
import com.dtteam.dynamictrees.worldgen.IDTBiomeHolderSet;
import com.dtteam.dynamictrees.worldgen.holderset.DTBiomeHolderSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public class FabricMiscHelper implements IMiscHelper {

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

    public static MinecraftServer currentServer;

    @Override
    public MinecraftServer getCurrentServer() {
        return currentServer;
    }

    @Override
    public IDTBiomeHolderSet newDTBiomeHolderSet() {
        return new DTBiomeHolderSet();
    }
}
