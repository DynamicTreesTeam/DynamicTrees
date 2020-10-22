package com.ferreusveritas.dynamictrees.systems;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.block.Block;

public class DirtHelper {
	
	public static final String DIRTLIKE = "dirtlike";
	public static final String SANDLIKE = "sandlike";
	public static final String WATERLIKE = "waterlike";
	public static final String NETHERLIKE = "netherlike";
	public static final String MUDLIKE = "mudlike";
	public static final String HARDCLAYLIKE = "hardclaylike";
	public static final String SLIMELIKE = "slimelike";
	public static final String FUNGUSLIKE = "funguslike";
	
	private static final Map<String, Integer> adjectiveMap;
	private static final Map<Block, Integer> dirtMap;
	
	static {
		adjectiveMap = new HashMap<>();
		dirtMap = new HashMap<>();
		
		createNewAdjective(DIRTLIKE);
		createNewAdjective(SANDLIKE);
		createNewAdjective(WATERLIKE);
		createNewAdjective(NETHERLIKE);
		createNewAdjective(MUDLIKE);
		createNewAdjective(HARDCLAYLIKE);
		createNewAdjective(SLIMELIKE);
		createNewAdjective(FUNGUSLIKE);
	}
	
	public static void createNewAdjective(String adjName) {
		adjectiveMap.put(adjName, 1 << adjectiveMap.size());
	}
	
	private static int getFlags(String adjName) {
		return adjectiveMap.getOrDefault(adjName, 0);
	}
	
	public static void registerSoil(Block block, String adjName) {
		if(adjectiveMap.containsKey(adjName)) {
			int flag = adjectiveMap.get(adjName);
			dirtMap.compute(block, (k, v) -> (v == null) ? flag : v | flag);
		} else {
			System.err.println("Adjective \"" + adjName + "\" not found while registering soil block: " + block);
		}
	}
	
	public static boolean isSoilAcceptable(Block block, int soilFlags) {
		return (dirtMap.getOrDefault(block, 0) & soilFlags) != 0;
	}
	
	public static int getSoilFlags(String ... types) {
		int flags = 0;
		
		for(String t : types) {
			flags |= getFlags(t);
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
