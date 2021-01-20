package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.util.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
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
	
	private static HashMap<String, List<DynamicLeavesBlock> > modLeavesArray = new HashMap<>();
	
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
	
	private static DynamicLeavesBlock createLeavesBlock(@Nullable String modid, @Nonnull ILeavesProperties leavesProperties, String name, ILeavesProperties.FoliageTypes type) {
		DynamicLeavesBlock leaves;
		switch (type){
			default:
			case LEAVES:
				leaves = createLeavesBlock(modid, name);
				break;
			case FUNGUS:
				leaves = createFungusBlock(modid, name);
				break;
			case WART:
				leaves = createWartBlock(modid, name);
		}
		leavesProperties.setDynamicLeavesState(leaves.getDefaultState());
		leaves.setProperties(leavesProperties);
		return leaves;
	}

	private static DynamicLeavesBlock createLeavesBlock(@Nullable String modid, String leavesName) {
		String regname = leavesName + "_leaves";

		List<DynamicLeavesBlock> map = getLeavesListForModId(modid);
		DynamicLeavesBlock newLeaves = (DynamicLeavesBlock)new DynamicLeavesBlock().setDefaultNaming(autoModId(modid), regname);
		map.add(newLeaves);
		return newLeaves;
	}
	private static DynamicFungusBlock createFungusBlock(@Nullable String modid, String leavesName) {
		String regname = leavesName + "_cap";

		List<DynamicLeavesBlock> map = getLeavesListForModId(modid);
		DynamicFungusBlock newLeaves = (DynamicFungusBlock)new DynamicFungusBlock().setDefaultNaming(autoModId(modid), regname);
		map.add(newLeaves);
		return newLeaves;
	}
	private static DynamicWartBlock createWartBlock(@Nullable String modid, String leavesName) {
		String regname = leavesName + "_wart";

		List<DynamicLeavesBlock> map = getLeavesListForModId(modid);
		DynamicWartBlock newLeaves = (DynamicWartBlock)new DynamicWartBlock().setDefaultNaming(autoModId(modid), regname);
		map.add(newLeaves);
		return newLeaves;
	}
	
	/**
	 * 	Get the map of leaves from for the appropriate modid.
	 *  If the map does not exist then one is created.
	 *
	 * @param modid The ModId of the mod accessing this
	 * @return The map of {@link DynamicLeavesBlock}
	 */
	public static List<DynamicLeavesBlock> getLeavesListForModId(@Nullable String modid) {
		return modLeavesArray.computeIfAbsent(autoModId(modid), k -> new ArrayList<DynamicLeavesBlock>());
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

			createLeavesBlock(modid, newLp, (String)obj, newLp.getFoliageType());
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
				ILeavesProperties.FoliageTypes type = ILeavesProperties.FoliageTypes.LEAVES;
				if (jsonObj.has("foliageType"))
					type = ILeavesProperties.FoliageTypes.valueOf(jsonObj.get("foliageType").getAsString().toUpperCase());
				createLeavesBlock(modid, newLp, label, type);
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
