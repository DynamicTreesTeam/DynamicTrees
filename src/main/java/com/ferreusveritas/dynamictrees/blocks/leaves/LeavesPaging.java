package com.ferreusveritas.dynamictrees.blocks.leaves;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author ferreusveritas
 */
public class LeavesPaging {
	
	private static final HashMap<String, List<DynamicLeavesBlock>> modLeavesArray = new HashMap<>();
	
	///////////////////////////////////////////
	// BLOCK PAGING
	///////////////////////////////////////////

	public static List<DynamicLeavesBlock> getLeavesList () {
		final List<DynamicLeavesBlock> leavesBlocks = new ArrayList<>();
		modLeavesArray.values().forEach(leavesBlocks::addAll);
		return leavesBlocks;
	}

	/**
	 * 	Get the map of leaves from for the appropriate modid.
	 *  If the map does not exist then one is created.
	 *
	 * @param modid The ModId of the mod accessing this
	 * @return The map of {@link DynamicLeavesBlock}
	 */
	public static List<DynamicLeavesBlock> getLeavesListForModId(@Nullable String modid) {
		return modLeavesArray.computeIfAbsent(modid, k -> new ArrayList<>());
	}

	public static void addLeavesBlockForModId(DynamicLeavesBlock block, String modid){
		getLeavesListForModId(modid).add(block);
	}

}
