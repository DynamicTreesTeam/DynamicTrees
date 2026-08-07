package com.dtteam.dynamictrees.model;

import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Direct handle on DT's own dynamic block state models, bypassing the model manager.
 *
 * <p>DT's falling-tree geometry is built by asking a model for its parts through
 * {@link BlockStateModelWithConnectionData} / {@link BlockStateModelWithRadius}. Those are DT-specific
 * interfaces, so the lookup only works if the object handed back is genuinely DT's model.
 *
 * <p>Other mods are free to replace or decorate baked models — Continuity, for instance, wraps them in
 * its own emissive/connected-texture model. Such a wrapper implements neither DT interface, so a plain
 * {@code modelSet.get(state)} would silently fall through to
 * {@code BlockStateModel#collectParts(RandomSource, List)}, which for a dynamic model is a no-op: the
 * tree would fall with its trunk invisible while the leaves (ordinary models) still render.
 *
 * <p>So DT records its own models here as they are baked and consults this registry first. Wrappers keep
 * working for normal in-world rendering — where they are applied through the level-aware path and are
 * exactly what we want — while DT's own geometry passes stay on the real model.
 */
public final class DynamicModelRegistry {

    private static final Map<BlockState, BlockStateModel> MODELS = new ConcurrentHashMap<>();

    private DynamicModelRegistry() {
    }

    /**
     * Drops every recorded model. Must be called at the start of each model bake, before
     * {@link #register} runs for the new set.
     */
    public static void clear() {
        MODELS.clear();
    }

    /**
     * Records DT's model for the given state. Called by the platform's model loading hook.
     */
    public static void register(BlockState state, BlockStateModel model) {
        MODELS.put(state, model);
    }

    /**
     * @return DT's own model for {@code state}, or {@code null} if DT did not supply one.
     */
    @Nullable
    public static BlockStateModel get(BlockState state) {
        return MODELS.get(state);
    }

    /**
     * DT's own model for {@code state} if there is one, otherwise whatever the model manager holds
     * (which may be another mod's wrapper). Use this anywhere DT needs to interrogate its own geometry.
     */
    public static BlockStateModel getOrFallback(BlockState state, BlockStateModelSet modelSet) {
        final BlockStateModel own = MODELS.get(state);
        return own != null ? own : modelSet.get(state);
    }
}
