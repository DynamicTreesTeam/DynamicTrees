package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.FutureBreakable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.List;

public class FutureBreak {

    public static final List<FutureBreak> FUTURE_BREAKS = new LinkedList<>();

    public final BlockState state;
    public final Level level;
    public final BlockPos pos;
    public final LivingEntity entity;
    public int ticks;

    public FutureBreak(BlockState state, Level level, BlockPos pos, LivingEntity entity, int ticks) {
        this.state = state;
        this.level = level;
        this.pos = pos;
        this.entity = entity;
        this.ticks = ticks;
    }

    public static void add(FutureBreak fb) {
        if (!fb.level.isClientSide) {
            FUTURE_BREAKS.add(fb);
        }
    }

    public static void process(Level level) {
        if (FUTURE_BREAKS.isEmpty()) {
            return;
        }

        for (final FutureBreak futureBreak : new LinkedList<>(FUTURE_BREAKS)) {
            if (level != futureBreak.level) {
                continue;
            }

            if (!(futureBreak.state.getBlock() instanceof FutureBreakable)) {
                FUTURE_BREAKS.remove(futureBreak);
                continue;
            }

            if (futureBreak.ticks-- > 0) {
                continue;
            }

            final FutureBreakable futureBreakable = (FutureBreakable) futureBreak.state.getBlock();
            futureBreakable.futureBreak(futureBreak.state, level, futureBreak.pos, futureBreak.entity);
            FUTURE_BREAKS.remove(futureBreak);
        }
    }

}

