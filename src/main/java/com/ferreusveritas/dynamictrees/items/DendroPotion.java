package com.ferreusveritas.dynamictrees.items;

import java.util.List;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.ModTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffectProvider;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceDeplete;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceDisease;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceFertilize;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceFreeze;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceGrowth;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceTransform;
import com.ferreusveritas.dynamictrees.trees.DynamicTree;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;

public class DendroPotion extends Item implements ISubstanceEffectProvider, IEmptiable {

	public static final String name = "dendropotion";

	public enum DendroPotionType {
		BIOCHAR(    0, "biochar",     0x27231c, "Base tree potion for brewing"),
		DEPLETION(  1, "depletion",	  0x76be6d, "Destroys tree soil fertility"),
		DEFOLIANCE( 2, "defoliance",  0xe3901d, "Slowly destroy a tree"),
		BURGEONING( 3, "burgeoning",  0xa9bebe, "Quickly grow a tree"),
		FERTILITY(  4, "fertility",   0x4ad400, "Fully fertilizes tree soil"),
		PERSISTANCE(5, "persistance", 0x389aff, "Stops tree from changing"),
		TRANSFORM(  6, "transform",   0x7fb8a4, "Base tree potion for brewing transformations");
				
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
	public void getSubItems(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> subItems) {
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
					return TreeRegistry.findSpecies(new ResourceLocation(targetTree)).getTree();
				}
			}
		}
		
		return null;
	}
	
	public ItemStack setTargetTree(ItemStack itemStack, DynamicTree tree) {
		NBTTagCompound nbtTag = itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
		nbtTag.setString("target", tree.getCommonSpecies().getRegistryName().toString());//Only store the common species
		itemStack.setTagCompound(nbtTag);
		return itemStack;
	}
	
	public DendroPotion registerRecipes() {
		
		PotionType awkward = PotionType.REGISTRY.getObject(new ResourceLocation("awkward"));
		
		BrewingRecipeRegistry.addRecipe(
				PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), awkward),
				new ItemStack(Items.COAL, 1, 1), //Charcoal
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()));

		BrewingRecipeRegistry.addRecipe(
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.SLIME_BALL), //Slimeball
				new ItemStack(this, 1, DendroPotionType.DEPLETION.getIndex()));
		
		BrewingRecipeRegistry.addRecipe(
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.PUMPKIN_SEEDS), //Pumpkin seeds
				new ItemStack(this, 1, DendroPotionType.DEFOLIANCE.getIndex()));

		BrewingRecipeRegistry.addRecipe(
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.GHAST_TEAR), //Ghast Tear
				new ItemStack(this, 1, DendroPotionType.BURGEONING.getIndex()));

		BrewingRecipeRegistry.addRecipe(
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.FISH), //Raw Fish
				new ItemStack(this, 1, DendroPotionType.FERTILITY.getIndex()));

		BrewingRecipeRegistry.addRecipe(
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Blocks.RED_FLOWER, 1, 1), //Blue Orchid
				new ItemStack(this, 1, DendroPotionType.PERSISTANCE.getIndex()));

		BrewingRecipeRegistry.addRecipe(
				new ItemStack(this, 1, DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.PRISMARINE_CRYSTALS), //Prismarine Crystals
				new ItemStack(this, 1, DendroPotionType.TRANSFORM.getIndex()));

		for(DynamicTree tree : ModTrees.baseTrees) {
			ItemStack outputStack = setTargetTree(new ItemStack(this, 1, DendroPotionType.TRANSFORM.getIndex()), tree);
			BrewingRecipeRegistry.addRecipe(new ItemStack(this, 1, DendroPotionType.TRANSFORM.getIndex()), tree.getCommonSpecies().getSeedStack(1), outputStack);
		}

		return this;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, playerIn, tooltip, advanced);
		
		DendroPotionType potionType = getPotionType(stack);
		
		if(potionType == DendroPotionType.TRANSFORM) {
			DynamicTree tree = getTargetTree(stack);
			if(tree == null) {
				tooltip.add(getPotionType(stack).getLore());
			} else {
				tooltip.add("Transform a tree into a " + tree.getName().getResourcePath() + " tree");
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
		return new ItemStack(Items.GLASS_BOTTLE);
	}

}
