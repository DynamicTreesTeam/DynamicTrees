package com.ferreusveritas.dynamictrees.blocks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import com.ferreusveritas.dynamictrees.ModBlocks;
import com.ferreusveritas.dynamictrees.ModConstants;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LeavesPropertiesJson extends LeavesProperties {
	
	static List<LeavesPropertiesJson> resolutionList = new ArrayList<>();
	
	private int lightRequirement = 13;
	private int smotherLeavesMax = 4;
	private final JsonObject jsonObj;
	
	public LeavesPropertiesJson(String jsonData) {
		this(getJsonObject(jsonData));
	}
	
	public LeavesPropertiesJson(JsonObject jsonObj) {
		super(ModBlocks.blockStates.air, ItemStack.EMPTY);//Assigns deciduous cell kit by default
		this.jsonObj = jsonObj;
		resolutionList.add(this);
	}
	
	public void resolve() {
		
		if(jsonObj != null) {
			for(Entry<String, JsonElement> entry : jsonObj.entrySet()) {
				String key = entry.getKey();
				JsonPrimitive element = entry.getValue().getAsJsonPrimitive();
				
				if("color".equals(key)) {
					colorPrimitive = element;
				} else
				if("leaves".equals(key)) {
					String leavesDesc = element.getAsString();
					primitiveLeaves = getBlockState(leavesDesc);
				} else
				if("cellkit".equals(key)) {
					String cellkitDesc = element.getAsString();
					ICellKit kit = TreeRegistry.findCellKit(new ResourceLocation(ModConstants.MODID, cellkitDesc));
					if(kit != null) {
						cellKit = kit;
					}
				} else
				if("smother".equals(key)) {
					smotherLeavesMax = MathHelper.clamp(element.getAsInt(), 0, 64);
				} else
				if("light".equals(key)) {
					lightRequirement = MathHelper.clamp(element.getAsInt(), 0, 15);
				}
				
			}
		}
        
		if(primitiveLeaves.getBlock() == Blocks.AIR) {
			primitiveLeaves = Blocks.LEAVES.getDefaultState();
		}
		
		int meta = primitiveLeaves.getBlock().damageDropped(primitiveLeaves);
		if(primitiveLeaves.getBlock() == Blocks.LEAVES2 && meta >= 4) {//Bug in minecraft where the damageDropped doesn't work for Leaves2
			meta -= 4;
		}
		
		primitiveLeavesItemStack = new ItemStack(Item.getItemFromBlock(primitiveLeaves.getBlock()), 1, meta);
	}
	
	private IBlockState getBlockState(String blockStateDesc) {
		String blockString = blockStateDesc;
		String argString = "default";
		String[] args = blockStateDesc.split(" ");
		
		if(args.length == 2) {
			blockString = args[0];
			argString = args[1];
		}
		
		Block block = Block.REGISTRY.getObject(new ResourceLocation(blockString));
		
		try {
			return CommandBase.convertArgToBlockState(block, argString);
		}
		catch (NumberInvalidException | InvalidBlockStateException e) {
			e.printStackTrace();
			return block.getDefaultState();
		}
	}
	
	public static JsonObject getJsonObject(String jsonData) {
		try {
			JsonParser parser = new JsonParser();
			JsonElement je = parser.parse(jsonData.replace('`', '"'));
			return je.getAsJsonObject();
		}
		catch (Exception e) {
			System.err.println(e);
			return null;
		}
	}
	
	public static void postInit() {
		for(LeavesPropertiesJson res: resolutionList) {
			res.resolve();
		}
	}
	
	public static void cleanUp() {
		resolutionList = null;//Free memory
	}
	
	@Override
	public int getLightRequirement() {
		return lightRequirement;
	}
	
	@Override
	public int getSmotherLeavesMax() {
		return smotherLeavesMax;
	}
	
	///////////////////////////////////////////
	//UPDATE INTERFACE
	///////////////////////////////////////////
	
	protected ILeavesUpdate leavesUpdate = (w,p,s,r,l) -> true;
	
	public static interface ILeavesUpdate {
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
	//BLOCK COLORING
	///////////////////////////////////////////
	
	private JsonPrimitive colorPrimitive = null;
	
	@SideOnly(Side.CLIENT)
	private IBlockColor colorMultiplier = (s,w,p,t) -> super.foliageColorMultiplier(s, w, p);
	
	@SideOnly(Side.CLIENT)
	@Override
	public int foliageColorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos) {
		return colorMultiplier.colorMultiplier(state, world, pos, -1);
	}

	@SideOnly(Side.CLIENT)
	private IBlockColor processColor(JsonPrimitive primitive) {
		int color = -1;
		if(primitive.isNumber()) {
			color = (int) primitive.getAsNumber();
		} else
		if(primitive.isString()) {
			String code = primitive.getAsString();
			if(code.startsWith("@")) {
				code = code.substring(1);
				if("biome".equals(code)) { //Built in code since we need access to super
					return (state, world, pos, t) -> {
						return world.getBiome(pos).getModdedBiomeFoliageColor(super.foliageColorMultiplier(state, world, pos));
					};
				}
				IBlockColor blockColor = BlockColorMultipliers.find(code);
				if(blockColor != null) {
					return blockColor;
				} else {
					System.err.println("Error: ColorMultiplier resource \"" + code + "\" could not be found.");
				}
			} else {
				color = Color.decode(code).getRGB();
			}
		}
		int c = color;
		return (s,w,p,t) -> c;
	}
	
	@SideOnly(Side.CLIENT)
	public void resolveClient() {
		if(colorPrimitive != null) {
			colorMultiplier = processColor(colorPrimitive);
			colorPrimitive = null;
		}
	}

	@SideOnly(Side.CLIENT)
	public static void postInitClient() {
		for(LeavesPropertiesJson res: resolutionList) {
			res.resolveClient();
		}
	}
	
}
