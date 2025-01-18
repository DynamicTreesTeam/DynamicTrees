package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.FallingTreeEntityModel;
import com.dtteam.dynamictrees.worldgen.BiomeGenSettingsBuilderWrapper;
import com.dtteam.dynamictrees.worldgen.IDTBiomeHolderSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public interface IMiscHelper {

    int getPixelRGBA (TextureAtlasSprite sprite, int x, int y);

    FallingTreeEntityModel newFallingTreeEntityModel (FallingTreeEntity entity);

    boolean isLevelRestoringBlockSnapshots (Level level);

    MinecraftServer getCurrentServer();

    IDTBiomeHolderSet newDTBiomeHolderSet();

}
