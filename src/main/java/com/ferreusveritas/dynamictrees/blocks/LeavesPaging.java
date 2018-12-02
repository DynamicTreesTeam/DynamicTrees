package com.ferreusveritas.dynamictrees.blocks;

import java.util.HashMap;
import java.util.Map;

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
 * The data is only used by mods for initialization purposes and all
 * contained data will be erased during postInit().
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
	public static Map<Integer, BlockDynamicLeaves> getLeavesMapForModId(@Nullable String modid) {
		return modLeavesArray.computeIfAbsent(autoModId(modid), k -> new HashMap<Integer, BlockDynamicLeaves>());
	}
	
	public static Map<String, ILeavesProperties> build(Object ... leavesProperties) {
		return buildForMod(autoModId(""), leavesProperties);
	}
	
	public static Map<String, ILeavesProperties> buildForMod(String modid, Object ... leavesProperties) {
		Map<String, ILeavesProperties> leafMap = new HashMap<>();
		
		for(int i = 0; i < (leavesProperties.length & ~1); i+=2) {
			String label = leavesProperties[i].toString();
			Object obj = leavesProperties[i+1];
			
			ILeavesProperties newLp = LeavesProperties.NULLPROPERTIES;
			
			if(obj instanceof ILeavesProperties) {
				newLp = (ILeavesProperties) obj;
			} else
			if(obj instanceof String && !"".equals(obj)) {
				newLp = new LeavesPropertiesJson((String) obj);
			}
			
			getNextLeavesBlock(modid, newLp);
			leafMap.put(label, newLp);
		}
		
		return leafMap;
	}
	
	/**
	 * Frees up the memory since this is only used during startup 
	 */
	public static void cleanUp() {
		modLeavesArray = null;
		modLastSeq = null;
	}
	
}
