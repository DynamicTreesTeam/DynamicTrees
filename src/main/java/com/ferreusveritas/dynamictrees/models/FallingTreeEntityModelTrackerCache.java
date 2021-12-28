package com.ferreusveritas.dynamictrees.models;

import com.ferreusveritas.dynamictrees.entities.FallingTreeEntity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class FallingTreeEntityModelTrackerCache {

    private static ConcurrentMap<Integer, FallingTreeEntityModel> models = new ConcurrentHashMap<>();

    public static FallingTreeEntityModel getOrCreateModel(FallingTreeEntity entity) {
        return models.computeIfAbsent(entity.getId(), e -> new FallingTreeEntityModel(entity));
    }

    public static void cleanupModels(World world, FallingTreeEntity entity) {
        models.remove(entity.getId());
        cleanupModels(world);
    }

    public static void cleanupModels(World world) {
        models = models.entrySet().stream()
                .filter(map -> world.getEntity(map.getKey()) != null)
                .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
