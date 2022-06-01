package com.ferreusveritas.dynamictrees.util;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
public final class WorldContext {

    private final RegistryKey<World> dimensionKey;
    private final ResourceLocation dimensionName;
    @Nullable
    private final Long seed;
    private final IWorld access;
    private final World level;

    public WorldContext(RegistryKey<World> dimensionKey, @Nullable Long seed, IWorld acess, World level) {
        this.dimensionKey = dimensionKey;
        this.dimensionName = dimensionKey.location();
        this.seed = seed;
        this.access = acess;
        this.level = level;
    }

    public RegistryKey<World> dimensionKey() {
        return dimensionKey;
    }

    public ResourceLocation dimensionName() {
        return dimensionName;
    }

    @Nullable
    public Long seed() {
        return seed;
    }

    public IWorld access() {
        return access;
    }

    /**
     * Returns the level's {@link World} object. Consumers should be warned that this is not always appropriate for use.
     * In some cases, such as world gen, alternate world objects are used and should be accessed instead using {@link
     * #access()} .
     */
    public World level() {
        return level;
    }

    public static WorldContext create(IWorld access) {
        World level = null;
        Long seed = null;
        if (access instanceof World) {
            level = ((World) access);
        } else if (access instanceof IServerWorld) {
            level = ((IServerWorld) access).getLevel();
        }
        if (level == null) {
            throw new RuntimeException("Could not handle custom IWorld object: " + access.getClass());
        }
        if (access instanceof ISeedReader) {
            seed = ((ISeedReader) access).getSeed();
        }
        return new WorldContext(level.dimension(), seed, access, level);
    }

}
