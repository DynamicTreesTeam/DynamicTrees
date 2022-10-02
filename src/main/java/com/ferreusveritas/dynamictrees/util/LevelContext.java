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
    private final LevelAccessor access;
    private final Level level;

    public LevelContext(ResourceKey<Level> dimensionKey, @Nullable Long seed, LevelAccessor acess, Level level) {
        this.dimensionKey = dimensionKey;
        this.dimensionName = dimensionKey.location();
        this.seed = seed;
        this.access = acess;
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

    public LevelAccessor access() {
        return access;
    }

    /**
     * Returns the level's {@link Level} object. Consumers should be warned that this is not always appropriate for use.
     * In some cases, such as world gen, alternate world objects are used and should be accessed instead using {@link
     * #access()} .
     */
    public Level level() {
        return level;
    }

    public static LevelContext create(LevelAccessor access) {
        Level level = null;
        Long seed = null;
        if (access instanceof Level) {
            level = ((Level) access);
        } else if (access instanceof ServerLevelAccessor) {
            level = ((ServerLevelAccessor) access).getLevel();
        }
        if (level == null) {
            throw new RuntimeException("Could not handle custom LevelAccessor object: " + access.getClass());
        }
        if (access instanceof WorldGenLevel) {
            seed = ((WorldGenLevel) access).getSeed();
        }
        return new LevelContext(level.dimension(), seed, access, level);
    }

    public static ServerLevel getServerWorldOrThrow(LevelAccessor access) {
        if (access instanceof ServerLevel) {
            return ((ServerLevel) access);
        } else if (access instanceof ServerLevelAccessor) {
            return ((ServerLevelAccessor) access).getLevel();
        }
        throw new IllegalArgumentException("Cannot get ServerLevel from LevelAccessor of type: " + access.getClass());
    }

}
