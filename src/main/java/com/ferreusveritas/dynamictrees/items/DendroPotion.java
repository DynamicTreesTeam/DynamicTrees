package com.ferreusveritas.dynamictrees.items;

import java.util.List;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.backport.GameRegistry;
import com.ferreusveritas.dynamictrees.api.backport.ItemBackport;
import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffectProvider;
import com.ferreusveritas.dynamictrees.potion.SubstanceDeplete;
import com.ferreusveritas.dynamictrees.potion.SubstanceDisease;
import com.ferreusveritas.dynamictrees.potion.SubstanceFertilize;
import com.ferreusveritas.dynamictrees.potion.SubstanceFreeze;
import com.ferreusveritas.dynamictrees.potion.SubstanceGrowth;
import com.ferreusveritas.dynamictrees.potion.SubstanceTransform;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

public class DendroPotion extends ItemBackport implements ISubstanceEffectProvider, IEmptiable {

	public static final String name = "dendropotion";

	public enum DendroPotionType {
		BIOCHAR(    0, "biochar",     0x27231c, "Base tree potion for brewing"),
		DEPLETION(  1, "depletion",	  0x76be6d, "Destroys tree soil fertility"),
		DEFOLIANCE( 2, "defoliance",  0xe3901d, "Slowly destroy a tree"),
		BURGEONING( 3, "burgeoning",  0xa9bebe, "Quickly grow a tree"),
		FERTILITY(  4, "fertility",   0x4ad400, "Fully fertilizes tree soil"),
		PERSISTANCE(5, "persistance", 0x389aff, "Stops tree from changing"),
		TRANSFORM(6, "transform", 0x7fb8a4, "Base tree potion for brewing transformations");
				
		private final int index;
		private final String name;
		private final int color;
		private final String lore;
		
		DendroPotionType(int index, String name, int color, String lore) {
			this.index = index;
			this.name = name;
			this.color = color;
			this.lore = lore;
		}
		
		ItemPotion p;
		
		public int getIndex() {
			return index;
		}
		
		public String getName() {
			return name;
		}
		
		public int getColor() {
			return color;
		}
		
		public String getLore() {
			return lore;
		}
	};
	
	public DendroPotion() {
		this(name);
	}
	
	public DendroPotion(String name) {
		setUnlocalizedName(name);
		setRegistryName(name);
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(DynamicTrees.dynamicTreesTab);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List subItems) {
		for(DendroPotionType type : DendroPotionType.values()) {
			subItems.add(new ItemStack(this, 1, type.getIndex()));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + getPotionType(stack).getName();
	}
	
	public DendroPotionType getPotionTypeForDamage(int damage) {
		return DendroPotionType.values()[damage % DendroPotionType.values().length];
	}
	
	public DendroPotionType getPotionType(ItemStack stack) {
		return getPotionTypeForDamage(stack.getItemDamage());
	}
	
	@Override
	public ISubstanceEffect getSubstanceEffect(ItemStack itemStack) {
		switch(getPotionType(itemStack)) {
			default:
			case BIOCHAR: return null;
			case BURGEONING: return new SubstanceGrowth();
			case DEFOLIANCE: return new SubstanceDisease();
			case DEPLETION: return new SubstanceDeplete().setAmount(15);
			case FERTILITY: return new SubstanceFertilize().setAmount(15);
			case PERSISTANCE: return new SubstanceFreeze();
			case TRANSFORM: return new SubstanceTransform(getTargetTree(itemStack));
		}
	}
	
	public DynamicTree getTargetTree(ItemStack itemStack) {
		if(itemStack.hasTagCompound()){
			NBTTagCompound nbtTag = itemStack.getTagCompound();
			if(nbtTag.hasKey("target")) {
				String targetTree = nbtTag.getString("target");
				if(!targetTree.equals("")) {
					return TreeRegistry.findTree(targetTree);
				}
			}
		}
		
		return null;
	}
	
	public ItemStack setTargetTree(ItemStack itemStack, DynamicTree tree) {
		NBTTagCompound nbtTag = itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
		nbtTag.setString("target", tree.getName());
		itemStack.setTagCompound(nbtTag);
		return itemStack;
	}
	
	public DendroPotion registerRecipes() {
		
		int awkward = 16;
		
		GameRegistry.addBrewingRecipe(
				new ItemStack(Items.potionitem, 1, awkward),
				new ItemStack(Items.coal, 1, 1), //Charcoal
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()));

		GameRegistry.addBrewingRecipe(
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.slime_ball), //Slimeball
				new ItemStack(this, 1, DendroPotionType.DEPLETION.getIndex()));
		
		GameRegistry.addBrewingRecipe(
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.pumpkin_seeds), //Pumpkin seeds
				new ItemStack(this, 1, DendroPotionType.DEFOLIANCE.getIndex()));

		GameRegistry.addBrewingRecipe(
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.ghast_tear), //Ghast Tear
				new ItemStack(this, 1, DendroPotionType.BURGEONING.getIndex()));

		GameRegistry.addBrewingRecipe(
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.fish), //Raw Fish
				new ItemStack(this, 1, DendroPotionType.FERTILITY.getIndex()));

		GameRegistry.addBrewingRecipe(
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Blocks.red_flower, 1, 1), //Blue Orchid
				new ItemStack(this, 1, DendroPotionType.PERSISTANCE.getIndex()));

		GameRegistry.addBrewingRecipe(
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.diamond), //Prismarine Crystals
				new ItemStack(this, 1, DendroPotionType.TRANSFORM.getIndex()));

		for(DynamicTree tree : DynamicTrees.baseTrees) {
			ItemStack outputStack = setTargetTree(new ItemStack(this, 1, DendroPotionType.TRANSFORM.getIndex()), tree);
			GameRegistry.addBrewingRecipe(new ItemStack(this, 1, DendroPotionType.TRANSFORM.getIndex()), tree.getSeedStack(), outputStack);
		}

		return this;
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		
		DendroPotionType potionType = getPotionType(stack);
		
		if(potionType == DendroPotionType.TRANSFORM) {
			DynamicTree tree = getTargetTree(stack);
			if(tree == null) {
				tooltip.add(getPotionType(stack).getLore());
			} else {
				tooltip.add("Transform a tree into a " + tree.getName() + " tree");
			}
		} else {
			tooltip.add(getPotionType(stack).getLore());
		}
		
	}
	
	public int getColor(ItemStack stack) {
		return getPotionType(stack).getColor();
	}
	
	@Override
	public ItemStack getEmptyContainer() {
		return new ItemStack(Items.glass_bottle);
	}
	
	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		return pass == 1 ? 0x00FFFFFF : getColor(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(ItemStack itemStack, int pass) {
		return Items.potionitem.getIconFromDamageForRenderPass(0, pass);//delegate to vanilla potion
	}
	
}
