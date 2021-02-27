package com.ferreusveritas.dynamictrees.blocks.leaves;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.util.json.JsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
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

	public static void registerAll (final String namespace, final Object ... leavesProperties) {
		final List<LeavesProperties> leaves = new ArrayList<>();

		for(int i = 0; i < (leavesProperties.length & ~1); i += 2) {
			String label = leavesProperties[i].toString();
			Object obj = leavesProperties[i+1];

			LeavesProperties newProperties = LeavesProperties.NULL_PROPERTIES;

			if(obj instanceof LeavesProperties) {
				newProperties = (LeavesProperties) obj;
			} else
			if(obj instanceof String && !"".equals(obj)) {
				newProperties = new LeavesPropertiesJson((String) obj, new ResourceLocation(namespace, label));
			}

			leaves.add(newProperties);
		}

		leaves.forEach(LeavesProperties.REGISTRY::register);
	}

	public static void register(final JsonObject root, final String namespace) {
		if (root == null)
			return;

		final List<LeavesProperties> leaves = new ArrayList<>();

		for (Entry<String, JsonElement> entry : root.entrySet()) {
			leaves.add(new LeavesPropertiesJson(entry.getValue().getAsJsonObject(), new ResourceLocation(namespace, entry.getKey())));
		}

		leaves.forEach(LeavesProperties.REGISTRY::register);
	}

	public static void register(final ResourceLocation jsonLocation) {
		final JsonElement element = JsonHelper.load(jsonLocation, JsonHelper.ResourceFolder.TREES);

		if (element != null && element.isJsonObject()) {
			register(element.getAsJsonObject(), jsonLocation.getNamespace());
			return;
		}

		LogManager.getLogger().warn("Error building leaves paging for mod: " + jsonLocation.getNamespace() + " at " + jsonLocation.getPath());
	}
	
}
