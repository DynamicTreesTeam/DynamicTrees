package com.ferreusveritas.dynamictrees.util;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
public final class LevelContext {

    private final ResourceKey<Level> dimensionKey;
    private final ResourceLocation dimensionName;
    @Nullable
    private final Long seed;
    private final LevelAccessor accessor;
    private final Level level;

    public LevelContext(ResourceKey<Level> dimensionKey, @Nullable Long seed, LevelAccessor accessor, Level level) {
        this.dimensionKey = dimensionKey;
        this.dimensionName = dimensionKey.location();
        this.seed = seed;
        this.accessor = accessor;
        this.level = level;
    }

    public ResourceKey<Level> dimensionKey() {
        return dimensionKey;
    }

    public ResourceLocation dimensionName() {
        return dimensionName;
    }

    @Nullable
    public Long seed() {
        return seed;
    }

    public LevelAccessor accessor() {
        return accessor;
    }

    /**
     * Returns the level's {@link Level} object. Consumers should be warned that this is not always appropriate for use.
     * In some cases, such as world gen, alternate world objects are used and should be accessed instead using {@link
     * #accessor()} .
     */
    public Level level() {
        return level;
    }

    public static LevelContext create(LevelAccessor accessor) {
        Level level = null;
        Long seed = null;
        if (accessor instanceof Level) {
            level = ((Level) accessor);
        } else if (accessor instanceof ServerLevelAccessor) {
            level = ((ServerLevelAccessor) accessor).getLevel();
        }
        if (level == null) {
            throw new RuntimeException("Could not handle custom LevelAccessor object: " + accessor.getClass());
        }
        if (accessor instanceof WorldGenLevel) {
            seed = ((WorldGenLevel) accessor).getSeed();
        }
        return new LevelContext(level.dimension(), seed, accessor, level);
    }

    public static ServerLevel getServerLevelOrThrow(LevelAccessor access) {
        if (access instanceof ServerLevel) {
            return ((ServerLevel) access);
        } else if (access instanceof ServerLevelAccessor) {
            return ((ServerLevelAccessor) access).getLevel();
        }
        throw new IllegalArgumentException("Cannot get ServerLevel from LevelAccessor of type: " + access.getClass());
    }

}
