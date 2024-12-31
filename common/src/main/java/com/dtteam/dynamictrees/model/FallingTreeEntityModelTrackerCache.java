package com.dtteam.dynamictrees.model;

import com.dtteam.dynamictrees.entity.FallingTreeEntity;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

//@OnlyIn(Dist.CLIENT)
public class FallingTreeEntityModelTrackerCache {

    private static ConcurrentMap<Integer, FallingTreeEntityModel> models = new ConcurrentHashMap<>();

    public static FallingTreeEntityModel getOrCreateModel(FallingTreeEntity entity) {
        return models.computeIfAbsent(entity.getId(), e -> new FallingTreeEntityModel(entity));
    }

    public static void cleanupModels(Level level, FallingTreeEntity entity) {
        models.remove(entity.getId());
        cleanupModels(level);
    }

    public static void cleanupModels(Level level) {
        models = models.entrySet().stream()
                .filter(map -> level.getEntity(map.getKey()) != null)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
