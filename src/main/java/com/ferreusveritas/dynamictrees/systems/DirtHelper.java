package com.ferreusveritas.dynamictrees.systems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.block.Block;

public class DirtHelper {
	
	public static enum Type {
		DIRTLIKE,
		SANDLIKE,
		STONELIKE,
		GRAVELLIKE,
		WATERLIKE,
		NETHERLIKE,
		MUDLIKE,
		HARDCLAYLIKE;
		
		private final int flag;
		
		Type() {
			flag = 1 << ordinal();
		}
	}
	
	private static Map<Block, Integer> dirtMap = new HashMap<>();
	
	public static void registerSoil(Block block, Type type) {
		dirtMap.compute(block, (k, v) -> (v == null) ? type.flag : v | type.flag);
	}
	
	public static boolean isSoilAcceptable(Block block, int soilFlags) {
		return (dirtMap.getOrDefault(block, 0) & soilFlags) != 0;
	}
	
	public static int getSoilFlags(Type ... types) {
		int flags = 0;
		
		for(Type t : types) {
			flags |= t.flag;
		}
		
		return flags;
	}
	
	/** DO NOT USE THIS FUNCTION!  THIS WILL SOON BE REMOVED! */
	@Deprecated
	public static Set<Block> getBlocks(int soilFlags) {
		Set<Block> blocks = new HashSet<>();
		
		for(Entry<Block, Integer> entry : dirtMap.entrySet()) {
			if((entry.getValue() & soilFlags) != 0) {
				blocks.add(entry.getKey());
			}
		}
		
		return blocks;
	}
	
}
