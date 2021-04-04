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
	
	public static List<FutureBreak> futureBreaks = new LinkedList<>();
	
	public final BlockState state;
	public final World world;
	public final BlockPos pos;
	public final LivingEntity entity;
	public int ticks = 0;
	
	public FutureBreak(BlockState state, World world, BlockPos pos, LivingEntity entity, int ticks) {
		this.state = state;
		this.world = world;
		this.pos = pos;
		this.entity = entity;
		this.ticks = ticks;
	}
	
	public static void add(FutureBreak fb) {
		if(!fb.world.isClientSide) {
			futureBreaks.add(fb);
		}
	}
	
	public static void process(World world) {
		if(!futureBreaks.isEmpty()) {
			Iterator<FutureBreak> i = futureBreaks.iterator();
				
			while(i.hasNext()) {
				FutureBreak fb = i.next();
				if(world == fb.world) { //Make sure we're working in the same world
					if(fb.state.getBlock() instanceof IFutureBreakable) {
						if(fb.ticks-- <= 0) {
							IFutureBreakable branch = (IFutureBreakable) fb.state.getBlock();
							branch.futureBreak(fb.state, world, fb.pos, fb.entity);
							i.remove();
						}
					} else {
						i.remove();
					}
				}
			}
		}
	}
	
}

