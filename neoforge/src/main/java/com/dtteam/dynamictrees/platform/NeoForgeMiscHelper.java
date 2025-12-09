package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.FallingTreeEntityModel;
import com.dtteam.dynamictrees.platform.services.IMiscHelper;
import com.dtteam.dynamictrees.registry.FallingTreeEntityModelNF;
import com.dtteam.dynamictrees.worldgen.IDTBiomeHolderSet;
import com.dtteam.dynamictrees.worldgen.holderset.DTBiomeHolderSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class NeoForgeMiscHelper implements IMiscHelper {

    @Override
    public int getPixelRGBA(TextureAtlasSprite sprite, int x, int y) {
        try{
            return sprite.getPixelRGBA(0, x, y);
        } catch (IllegalStateException e){
            DynamicTrees.LOG.warn("Image {} is not allocated.", sprite);
            return 0;
        }
    }

    @Override 
    public FallingTreeEntityModel newFallingTreeEntityModel(FallingTreeEntity entity) {
        return new FallingTreeEntityModelNF(entity);
    }

    @Override
    public boolean isLevelRestoringBlockSnapshots(Level level) {
        return level.restoringBlockSnapshots;
    }

    @Override
    public MinecraftServer getCurrentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public IDTBiomeHolderSet newDTBiomeHolderSet() {
        return new DTBiomeHolderSet();
    }

}