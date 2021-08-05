package com.ferreusveritas.dynamictrees.blocks;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cells.ICellKit;
import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.client.BlockColorMultipliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class LeavesPropertiesJson extends LeavesProperties {

	static List<LeavesPropertiesJson> resolutionList = new ArrayList<>();

	private int lightRequirement = 13;
	private int smotherLeavesMax = 4;
	private JsonObject jsonObj = null;
	private boolean connectAnyRadius = false;
	private int flammability = 60;// Mimic vanilla leaves
	private int fireSpreadSpeed = 30;// Mimic vanilla leaves

	public LeavesPropertiesJson(String jsonData) {
		this(getJsonObject(jsonData));
	}

	public LeavesPropertiesJson(JsonObject jsonObj) {
		super(ModBlocks.blockStates.air, ItemStack.EMPTY);//Assigns deciduous cell kit by default
		this.jsonObj = jsonObj;
		resolutionList.add(this);
	}

	public void resolve() {

		if (jsonObj != null) {
			for (Entry<String, JsonElement> entry : jsonObj.entrySet()) {
				String key = entry.getKey();
				JsonElement element = entry.getValue();

				if ("color".equals(key)) {
					if (element.isJsonPrimitive()) {
						colorPrimitive = element.getAsJsonPrimitive();
					}
				} else if ("leaves".equals(key)) {
					if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
						getPrimitiveLeaves(element.getAsString()).assignTo(this);
					} else if (element.isJsonObject()) {
						getPrimitiveLeaves(element.getAsJsonObject()).assignTo(this);
					}
				} else if ("cellkit".equals(key)) {
					ICellKit kit = TreeRegistry.findCellKit(element.getAsString());
					if (kit != null) {
						cellKit = kit;
					}
				} else if ("smother".equals(key)) {
					smotherLeavesMax = MathHelper.clamp(element.getAsInt(), 0, 64);
				} else if ("light".equals(key)) {
					lightRequirement = MathHelper.clamp(element.getAsInt(), 0, 15);
				} else if ("connectAny".equals(key)) {
					if (element.isJsonPrimitive()) {
						connectAnyRadius = element.getAsBoolean();
					}
				} else if ("flammability".equals(key)) {
					if (element.isJsonPrimitive()) {
						flammability = element.getAsInt();
					}
				} else if ("fireSpreadSpeed".equals(key)) {
					if (element.isJsonPrimitive()) {
						fireSpreadSpeed = element.getAsInt();
					}
				}
			}

			jsonObj = null;//Free up json object since it is no longer used
		}

	}

	public static class PrimitiveLeavesComponents {
		public IBlockState state;
		public ItemStack stack;

		public PrimitiveLeavesComponents(IBlockState state, ItemStack stack) {
			this.state = state;
			this.stack = stack;
		}

		public void assignTo(LeavesPropertiesJson lpJson) {
			lpJson.primitiveLeaves = state;
			lpJson.primitiveLeavesItemStack = stack;
		}
	}

	private static PrimitiveLeavesComponents getPrimitiveLeaves(String leavesDesc) {
		IBlockState state = getBlockState(leavesDesc);
		ItemStack stack = ItemStack.EMPTY;

		if (state.getBlock() != Blocks.AIR) {
			int meta = state.getBlock().damageDropped(state);

			//Minecraft metadata logic for leaves is buggy and needs this work around
			if (state.getBlock() == Blocks.LEAVES2 && meta >= 4) {//Bug in minecraft where the damageDropped doesn't work for Leaves2
				meta -= 4;
			}

			stack = new ItemStack(Item.getItemFromBlock(state.getBlock()), 1, meta);
		}

		return new PrimitiveLeavesComponents(state, stack);
	}

	private static PrimitiveLeavesComponents getPrimitiveLeaves(JsonObject jsonObj) {

		for (Entry<String, JsonElement> entry : jsonObj.entrySet()) {
			String name = entry.getKey();
			PrimitiveLeavesComponents comp = findPrimitiveComponents(name, entry.getValue());
			if (comp != null) {
				return comp;
			}
		}

		return new PrimitiveLeavesComponents(Blocks.AIR.getDefaultState(), ItemStack.EMPTY);
	}

	private static IBlockState getBlockState(String blockStateDesc) {
		String blockString = blockStateDesc;
		String argString = "default";
		String[] args = blockStateDesc.split(" ");

		if (args.length == 2) {
			blockString = args[0];
			argString = args[1];
		}

		Block block = Block.REGISTRY.getObject(new ResourceLocation(blockString));

		try {
			return CommandBase.convertArgToBlockState(block, argString);
		} catch (NumberInvalidException | InvalidBlockStateException e) {
			e.printStackTrace();
			return block.getDefaultState();
		}
	}

	public static JsonObject getJsonObject(String jsonData) {
		try {
			JsonParser parser = new JsonParser();
			JsonElement je = parser.parse(jsonData.replace('`', '"'));
			return je.getAsJsonObject();
		} catch (Exception e) {
			System.err.println(e);
			return null;
		}
	}

	public static void resolveAll() {
		for (LeavesPropertiesJson res : resolutionList) {
			res.resolve();
		}
	}

	public static void cleanUp() {
		//Free memory
		resolutionList = null;
		blockstateFinderMap = null;
	}

	@Override
	public int getLightRequirement() {
		return lightRequirement;
	}

	@Override
	public int getSmotherLeavesMax() {
		return smotherLeavesMax;
	}

	@Override
	public int getRadiusForConnection(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, BlockBranch from, EnumFacing side, int fromRadius) {
		return (fromRadius == 1 || connectAnyRadius) && from.getFamily().isCompatibleDynamicLeaves(blockAccess.getBlockState(pos), blockAccess, pos) ? 1 : 0;
	}

	@Override
	public int getFlammability() {
		return flammability;
	}

	@Override
	public int getFireSpreadSpeed() {
		return fireSpreadSpeed;
	}

	///////////////////////////////////////////
	//UPDATE INTERFACE
	///////////////////////////////////////////

	protected ILeavesUpdate leavesUpdate = (w, p, s, r, l) -> true;

	public interface ILeavesUpdate {
		boolean updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand, ILeavesProperties leavesProperties);
	}

	public void setLeavesUpdate(ILeavesUpdate leavesUpdate) {
		this.leavesUpdate = leavesUpdate;
	}

	@Override
	public boolean updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		return leavesUpdate.updateTick(worldIn, pos, state, rand, this);
	}

	///////////////////////////////////////////
	//PRIMITIVE LEAVES FINDING
	///////////////////////////////////////////

	protected static Map<String, Function<JsonElement, PrimitiveLeavesComponents>> blockstateFinderMap = new HashMap<>();

	public static void addLeavesFinderFunction(String finderName, Function<JsonElement, PrimitiveLeavesComponents> function) {
		blockstateFinderMap.put(finderName, function);
	}

	public static PrimitiveLeavesComponents findPrimitiveComponents(String finderName, JsonElement jsonElement) {
		Function<JsonElement, PrimitiveLeavesComponents> function = blockstateFinderMap.get(finderName);
		if (function != null) {
			return function.apply(jsonElement);
		}

		return new PrimitiveLeavesComponents(Blocks.AIR.getDefaultState(), ItemStack.EMPTY);
	}

	///////////////////////////////////////////
	//BLOCK COLORING
	///////////////////////////////////////////

	private JsonPrimitive colorPrimitive = null;

	@SideOnly(Side.CLIENT)
	private IBlockColor colorMultiplier;

	@SideOnly(Side.CLIENT)
	@Override
	public int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos) {
		return colorMultiplier.colorMultiplier(state, world, pos, -1);
	}

	@SideOnly(Side.CLIENT)
	private IBlockColor processColor(JsonPrimitive primitive) {
		int color = -1;
		if (primitive.isNumber()) {
			color = (int) primitive.getAsNumber();
		} else if (primitive.isString()) {
			String code = primitive.getAsString();
			if (code.startsWith("@")) {
				code = code.substring(1);
				if ("biome".equals(code)) { //Built in code since we need access to super
					return (state, world, pos, t) -> {
						return world.getBiome(pos).getModdedBiomeFoliageColor(super.foliageColorMultiplier(state, world, pos));
					};
				}
				IBlockColor blockColor = BlockColorMultipliers.find(code);
				if (blockColor != null) {
					return blockColor;
				} else {
					System.err.println("Error: ColorMultiplier resource \"" + code + "\" could not be found.");
				}
			} else {
				color = Color.decode(code).getRGB();
			}
		}
		int c = color;
		return (s, w, p, t) -> c;
	}

	@SideOnly(Side.CLIENT)
	public void resolveClient() {
		if (colorPrimitive != null) {
			colorMultiplier = processColor(colorPrimitive);
			colorPrimitive = null;
		} else {
			colorMultiplier = (s, w, p, t) -> super.foliageColorMultiplier(s, w, p);
		}
	}

	@SideOnly(Side.CLIENT)
	public static void postInitClient() {
		for (LeavesPropertiesJson res : resolutionList) {
			res.resolveClient();
		}
	}

}
