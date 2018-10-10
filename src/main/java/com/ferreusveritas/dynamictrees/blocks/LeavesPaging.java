package com.ferreusveritas.dynamictrees.blocks;

import java.util.HashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/**
 * This class provides a mechanism for mapping leaves sub blocks to
 * their corresponding leaves properties.  This is currently a 4:1 ratio.
 * This method will be obsolete in MC 1.13 since each blockstate will
 * be able to contain it's own leaves properties.
 * 
 * @author ferreusveritas
 */
public class LeavesPaging {
	
	///////////////////////////////////////////
	//BLOCK PAGING
	///////////////////////////////////////////
	
	private static HashMap<String, HashMap<Integer, BlockDynamicLeaves> > modLeavesArray = new HashMap<>();
	private static HashMap<String, Integer> modLastSeq = new HashMap<>();
		
	private static String autoModId(@Nullable String modid) {
		if(modid == null || "".equals(modid)) {
			ModContainer mc = Loader.instance().activeModContainer();
			modid = mc == null ? ModConstants.MODID : mc.getModId();
		}
		return modid;
	}
	
	public static BlockDynamicLeaves getNextLeavesBlock(@Nullable String modid, @Nonnull ILeavesProperties leavesProperties) {
		return getLeavesBlockForSequence(modid, getNextSequenceNumber(modid), leavesProperties);
	}
	
	public static int getNextSequenceNumber(@Nullable String modid) {
		modid = autoModId(modid);
		int seq = modLastSeq.computeIfAbsent(modid, i -> 0);
		modLastSeq.put(modid, seq + 1);
		return seq;
	}
	
	public static int getLastSequenceNumber(@Nullable String modid) {
		return modLastSeq.computeIfAbsent(autoModId(modid), i -> 0) - 1;
	}
	
	public static BlockDynamicLeaves getLeavesBlockForSequence(@Nullable String modid, int seq, @Nonnull ILeavesProperties leavesProperties) {
		BlockDynamicLeaves leaves = getLeavesBlockForSequence(modid, seq);
		int tree = seq & 3;
		leavesProperties.setDynamicLeavesState(leaves.getDefaultState().withProperty(BlockDynamicLeaves.TREE, tree));
		leaves.setProperties(tree, leavesProperties);
		return leaves;
	}
	
	/**
	 * A convenience function for packing 4 {@link BlockDynamicLeaves} blocks into one Minecraft block using metadata.
	 * 
	 * @param modid
	 * @param seq
	 * @return
	 */
	private static BlockDynamicLeaves getLeavesBlockForSequence(@Nullable String modid, int seq) {
		int key = seq / 4;
		String regname = "leaves" + key;
		
		return getLeavesMapForModId(modid).computeIfAbsent(key, k -> (BlockDynamicLeaves)new BlockDynamicLeaves().setDefaultNaming(autoModId(modid), regname));
	}
	
	/**
	 * 	Get the map of leaves from for the appropriate modid.
	 *  If the map does not exist then one is created.
	 * 
	 * @param modid The ModId of the mod accessing this
	 * @return The map of {@link BlockDynamicLeaves}
	 */
	public static HashMap<Integer, BlockDynamicLeaves> getLeavesMapForModId(@Nullable String modid) {
		return modLeavesArray.computeIfAbsent(autoModId(modid), k -> new HashMap<Integer, BlockDynamicLeaves>());
	}
	
}
