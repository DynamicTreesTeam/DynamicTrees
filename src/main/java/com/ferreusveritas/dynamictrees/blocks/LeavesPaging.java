package com.ferreusveritas.dynamictrees.blocks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.ModConstants;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
			
			getNextLeavesBlock(modid, newLp);
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
				ILeavesProperties newLp = LeavesProperties.NULLPROPERTIES;
				if(!label.startsWith("-")) { //A hyphen can be prepended to a label to create an unused gap
					JsonObject jsonObj = entry.getValue().getAsJsonObject();
					newLp = new LeavesPropertiesJson(jsonObj);
				}
				getNextLeavesBlock(modid, newLp);
				leafMap.put(label, newLp);
			}
		}
		
		return leafMap;
	}
	
	public static Map<String, ILeavesProperties> build(ResourceLocation jsonLocation) {
		return build(autoModId(""), jsonLocation);
	}

	public static Map<String, ILeavesProperties> build(String modid, ResourceLocation jsonLocation) {
		
		JsonObject root = null;
		
		try {
			InputStream in = Minecraft.getMinecraft().getResourceManager().getResource(jsonLocation).getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			JsonElement je = new Gson().fromJson(reader, JsonElement.class);
			root = je.getAsJsonObject();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return build(root);
	}
	
	@SideOnly(Side.CLIENT)
	public static void setStateMappers() {
		for(String modId : modLeavesArray.keySet()) {
			LeavesPaging.getLeavesMapForModId(modId).forEach((key,leaves) -> ModelLoader.setCustomStateMapper(leaves, new LeavesStateMapper()));
		}
	}
	
	/**
	 * Frees up the memory since this is only used during startup 
	 */
	public static void cleanUp() {
		modLeavesArray = null;
		modLastSeq = null;
	}
	
}
