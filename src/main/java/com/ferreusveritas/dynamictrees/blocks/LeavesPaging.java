package com.ferreusveritas.dynamictrees.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.util.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

/**
 * @author ferreusveritas
 */
public class LeavesPaging {
	
	private static HashMap<String, List<BlockDynamicLeaves> > modLeavesArray = new HashMap<>();
	
	///////////////////////////////////////////
	//BLOCK PAGING
	///////////////////////////////////////////
		
	private static String autoModId(@Nullable String modid) {
		if(modid == null || "".equals(modid)) {
//			ModContainer mc = Loader.getClassLoader();
			modid = DynamicTrees.MODID;
//			modid = mc == null ? DynamicTrees.MODID : mc.getModId();
		}
		return modid;
	}
	
	private static BlockDynamicLeaves createLeavesBlock(@Nullable String modid, @Nonnull ILeavesProperties leavesProperties, String name) {
		BlockDynamicLeaves leaves = createLeavesBlock(modid, name);
		leavesProperties.setDynamicLeavesState(leaves.getDefaultState());
		leaves.setProperties(leavesProperties);
		return leaves;
	}

	private static BlockDynamicLeaves createLeavesBlock(@Nullable String modid, String leavesName) {
		String regname = "dynamic_" + leavesName + "_leaves";

		List<BlockDynamicLeaves> map = getLeavesListForModId(modid);
		BlockDynamicLeaves newLeaves = (BlockDynamicLeaves)new BlockDynamicLeaves().setDefaultNaming(autoModId(modid), regname);
		map.add(newLeaves);
		return newLeaves;
	}
	
	/**
	 * 	Get the map of leaves from for the appropriate modid.
	 *  If the map does not exist then one is created.
	 *
	 * @param modid The ModId of the mod accessing this
	 * @return The map of {@link BlockDynamicLeaves}
	 */
	public static List<BlockDynamicLeaves> getLeavesListForModId(@Nullable String modid) {
		return modLeavesArray.computeIfAbsent(autoModId(modid), k -> new ArrayList<BlockDynamicLeaves>());
	}

	public static Map<String, ILeavesProperties> buildAll(Object ... leavesProperties) {
		return buildAllForMod(autoModId(""), leavesProperties);
	}

	public static Map<String, ILeavesProperties> buildAllForMod(String modid, Object ... leavesProperties) {
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

			createLeavesBlock(modid, newLp, (String)obj);
			leafMap.put(label, newLp);
		}

		return leafMap;
	}

	public static Map<String, ILeavesProperties> build(String jsonData) {
		return build(autoModId(""), jsonData);
	}

	public static Map<String, ILeavesProperties> build(String modid, String jsonData) {
		return build(modid, LeavesPropertiesJson.getJsonObject(jsonData));
	}

	public static Map<String, ILeavesProperties> build(JsonObject root) {
		return build(autoModId(""), root);
	}

	public static Map<String, ILeavesProperties> build(String modid, JsonObject root) {
		Map<String, ILeavesProperties> leafMap = new HashMap<>();

		if(root != null) {
			for(Entry<String, JsonElement> entry : root.entrySet()) {
				String label = entry.getKey();
				JsonObject jsonObj = entry.getValue().getAsJsonObject();
				ILeavesProperties newLp = new LeavesPropertiesJson(jsonObj);
				createLeavesBlock(modid, newLp, label);
				leafMap.put(label, newLp);
			}
		}

		return leafMap;
	}

	public static Map<String, ILeavesProperties> build(ResourceLocation jsonLocation) {
		return build(autoModId(""), jsonLocation);
	}

	public static Map<String, ILeavesProperties> build(String modid, ResourceLocation jsonLocation) {
		JsonElement element = JsonHelper.load(jsonLocation);
		if(element != null && element.isJsonObject()) {
			return build(element.getAsJsonObject());
		}

		Logger.getLogger(DynamicTrees.MODID).log(Level.SEVERE, "Error building leaves paging for mod: " + modid + " at " + jsonLocation);

		return null;
	}
	
}
