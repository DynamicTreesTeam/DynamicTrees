package com.dtteam.dynamictrees.model.entity;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import com.dtteam.dynamictrees.platform.ClientServices;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class FallingTreeEntityModelTrackerCache {

    private static ConcurrentMap<Integer, FallingTreeEntityModel> models = new ConcurrentHashMap<>();

    @Nullable
    public static FallingTreeEntityModel getOrCreateModel(FallingTreeEntity entity) {
        if (entity.level().isClientSide())
            return models.computeIfAbsent(entity.getId(), i -> ClientServices.CLIENT.newFallingTreeEntityModel(entity));
        return null;
    }

    public static void cleanupModels(Level level, FallingTreeEntity entity) {
        if (level.isClientSide()){
            models.remove(entity.getId());
            cleanupModels(level);
        }
    }

    public static void cleanupModels(Level level) {
        models = models.entrySet().stream()
                .filter(map -> level.getEntity(map.getKey()) != null)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
