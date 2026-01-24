package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.model.FallingTreeEntityModel;
import com.dtteam.dynamictrees.model.FallingTreeEntityModelFabric;
import com.dtteam.dynamictrees.platform.services.IMiscHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.worldgen.IDTBiomeHolderSet;
import com.dtteam.dynamictrees.worldgen.holderset.DTBiomeHolderSet;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public class FabricMiscHelper implements IMiscHelper {

    public static void debugSpeciesRegistry() {
        DynamicTrees.LOG.info("=== Species Registry Debug ===");
        DynamicTrees.LOG.info("Total species count: {}", Species.REGISTRY.getAll().size());
        Species cherry = Species.REGISTRY.get(DynamicTrees.location("cherry"));
        DynamicTrees.LOG.info("Cherry species: {} (valid: {})", cherry, cherry != null && cherry.isValid());
        Species oak = Species.REGISTRY.get(DynamicTrees.location("oak"));
        DynamicTrees.LOG.info("Oak species: {} (valid: {})", oak, oak != null && oak.isValid());
        DynamicTrees.LOG.info("=== End Species Registry Debug ===");
    }

    @Override
    public int getPixelRGBA(TextureAtlasSprite sprite, int x, int y) {
        try {
            SpriteContents contents = sprite.contents();
            NativeImage image = contents.originalImage;
            if (image != null) {
                return image.getPixelRGBA(x, y);
            }
            return 0;
        } catch (Exception e) {
            DynamicTrees.LOG.warn("Failed to get pixel from sprite: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public FallingTreeEntityModel newFallingTreeEntityModel(FallingTreeEntity entity) {
        return new FallingTreeEntityModelFabric(entity);
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
