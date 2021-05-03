package com.ferreusveritas.dynamictrees.event;

import com.ferreusveritas.dynamictrees.api.IFutureBreakable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FutureBreak {
	
	public static final List<FutureBreak> FUTURE_BREAKS = new LinkedList<>();
	
	public final BlockState state;
	public final World world;
	public final BlockPos pos;
	public final LivingEntity entity;
	public int ticks;
	
	public FutureBreak(BlockState state, World world, BlockPos pos, LivingEntity entity, int ticks) {
		this.state = state;
		this.world = world;
		this.pos = pos;
		this.entity = entity;
		this.ticks = ticks;
	}
	
	public static void add(FutureBreak fb) {
		if (!fb.world.isClientSide) {
			FUTURE_BREAKS.add(fb);
		}
	}
	
	public static void process(World world) {
		if (FUTURE_BREAKS.isEmpty())
			return;

		for (final FutureBreak futureBreak : new LinkedList<>(FUTURE_BREAKS)) {
			if (world != futureBreak.world)
				continue;

			if (!(futureBreak.state.getBlock() instanceof IFutureBreakable)) {
				FUTURE_BREAKS.remove(futureBreak);
				continue;
			}

			if (futureBreak.ticks-- > 0)
				continue;

			final IFutureBreakable futureBreakable = (IFutureBreakable) futureBreak.state.getBlock();
			futureBreakable.futureBreak(futureBreak.state, world, futureBreak.pos, futureBreak.entity);
			FUTURE_BREAKS.remove(futureBreak);
		}
	}
	
}

