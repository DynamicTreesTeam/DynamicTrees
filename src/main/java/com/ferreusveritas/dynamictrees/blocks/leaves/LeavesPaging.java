package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ferreusveritas
 */
public class LeavesPaging {
	
	private static final HashMap<String, List<DynamicLeavesBlock>> modLeavesArray = new HashMap<>();
	
	///////////////////////////////////////////
	//BLOCK PAGING
	///////////////////////////////////////////

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

	public static Map<String, ILeavesProperties> buildAll (Object ... leavesProperties) {
		Map<String, ILeavesProperties> leafMap = new HashMap<>();

		for(int i = 0; i < (leavesProperties.length & ~1); i+=2) {
			String label = leavesProperties[i].toString();
			Object obj = leavesProperties[i+1];

			ILeavesProperties newLp = LeavesProperties.NULL_PROPERTIES;

			if(obj instanceof ILeavesProperties) {
				newLp = (ILeavesProperties) obj;
			} else
			if(obj instanceof String && !"".equals(obj)) {
				newLp = new LeavesPropertiesJson((String) obj);
			}

			leafMap.put(label, newLp);
		}

		return leafMap;
	}

	public static Map<String, ILeavesProperties> build(JsonObject root) {
		Map<String, ILeavesProperties> leafMap = new HashMap<>();

		if(root != null) {
			for(Entry<String, JsonElement> entry : root.entrySet()) {
				String label = entry.getKey();
				JsonObject jsonObj = entry.getValue().getAsJsonObject();
				ILeavesProperties newLp = new LeavesPropertiesJson(jsonObj);
				leafMap.put(label, newLp);
			}
		}

		return leafMap;
	}

	public static Map<String, ILeavesProperties> build(ResourceLocation jsonLocation) {
		JsonElement element = JsonHelper.load(jsonLocation, JsonHelper.ResourceFolder.TREES);
		if(element != null && element.isJsonObject()) {
			return build(element.getAsJsonObject());
		}
		Logger.getLogger(DynamicTrees.MOD_ID).log(Level.SEVERE, "Error building leaves paging for mod: " + jsonLocation.getNamespace() + " at " + jsonLocation.getPath());

		return null;
	}
	
}
