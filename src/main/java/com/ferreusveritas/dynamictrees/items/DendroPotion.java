package com.ferreusveritas.dynamictrees.items;

import java.util.List;

import com.ferreusveritas.dynamictrees.ModTabs;
import com.ferreusveritas.dynamictrees.ModTrees;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffectProvider;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceDeplete;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceFertilize;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceFreeze;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceGrowth;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceMega;
import com.ferreusveritas.dynamictrees.systems.substances.SubstanceTransform;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class DendroPotion extends Item implements ISubstanceEffectProvider, IEmptiable {
	
	public static final String name = "dendropotion";
	
	public enum DendroPotionType {
		BIOCHAR(    0, true, "biochar",     0x27231c),
		DEPLETION(  1, true, "depletion",   0x76be6d),
		GIGAS(      2, true, "gigas",       0xe3901d),
		BURGEONING( 3, true, "burgeoning",  0xa9bebe),
		FERTILITY(  4, true, "fertility",   0x4ad400),
		PERSISTANCE(5, true, "persistance", 0x389aff),
		TRANSFORM(  6, true, "transform",   0x7fb8a4);
		
		private final int index;
		private final boolean active;
		private final String name;
		private final int color;
		
		DendroPotionType(int index, boolean active, String name, int color) {
			this.index = index;
			this.active = active;
			this.name = name;
			this.color = color;
		}
		
		ItemPotion p;
		
		public int getIndex() {
			return index;
		}
		
		public boolean getActive() {
			return active;
		}
		
		public String getName() {
			return name;
		}
		
		public int getColor() {
			return color;
		}
		
		public String getLore() {
			return new TextComponentTranslation("potion." + this.name + ".description" + (this == TRANSFORM ? ".empty" : "")).getUnformattedComponentText();
		}
	};
	
	public DendroPotion() {
		this(name);
	}
	
	public DendroPotion(String name) {
		setRegistryName(name);
		setUnlocalizedName(getRegistryName().toString());
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ModTabs.dynamicTreesTab);
	}
	
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if(isInCreativeTab(tab)) {
			for(DendroPotionType type : DendroPotionType.values()) {
				if(type.getActive()) {
					subItems.add(new ItemStack(this, 1, type.getIndex()));
				}
			}
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
			case GIGAS: return new SubstanceMega();
			case DEPLETION: return new SubstanceDeplete().setAmount(15);
			case FERTILITY: return new SubstanceFertilize().setAmount(15);
			case PERSISTANCE: return new SubstanceFreeze();
			case TRANSFORM: return new SubstanceTransform(getTargetSpecies(itemStack));
		}
	}
	
	public Species getTargetSpecies(ItemStack itemStack) {
		if(itemStack.hasTagCompound()){
			NBTTagCompound nbtTag = itemStack.getTagCompound();
			if(nbtTag.hasKey("target")) {
				String targetTree = nbtTag.getString("target");
				if(!targetTree.equals("")) {
					return TreeRegistry.findSpecies(new ResourceLocation(targetTree));
				}
			}
		}
		
		return null;
	}
	
	public ItemStack setTargetTree (ItemStack itemStack, TreeFamily family) {
		return this.setTargetSpecies(itemStack, family.getCommonSpecies());
	}
	
	public ItemStack setTargetSpecies(ItemStack itemStack, Species species) {
		NBTTagCompound nbtTag = itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
		nbtTag.setString("target", species.getRegistryName().toString());
		itemStack.setTagCompound(nbtTag);
		return itemStack;
	}
	
	public DendroPotion registerRecipes(IForgeRegistry<IRecipe> registry) {
		
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
				new ItemStack(this, 1, DendroPotionType.GIGAS.getIndex()));
		
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
		
		for(Species species : TreeRegistry.getPotionTransformableSpecies()) {
			ItemStack outputStack = setTargetSpecies(new ItemStack(this, 1, DendroPotionType.TRANSFORM.getIndex()), species);
			BrewingRecipeRegistry.addRecipe(new ItemStack(this, 1, DendroPotionType.TRANSFORM.getIndex()), species.getSeedStack(1), outputStack);
		}
		
		return this;
	}
	
	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag tooltipFlag) {
		super.addInformation(stack, world, tooltip, tooltipFlag);
		
		DendroPotionType potionType = getPotionType(stack);
		
		if(potionType == DendroPotionType.TRANSFORM) {
			Species species = getTargetSpecies(stack);
			if(species == null) {
				tooltip.add(getPotionType(stack).getLore());
			} else {
				tooltip.add(new TextComponentTranslation("potion.transform.description", species.getLocalizedName()).getUnformattedComponentText());
			}
		} else {
			tooltip.add(getPotionType(stack).getLore());
		}
		
	}
	
	public int getColor(ItemStack stack, int tint) {
		return tint == 0 ? getPotionType(stack).getColor() : 0xFFFFFFFF;
	}
	
	@Override
	public ItemStack getEmptyContainer() {
		return new ItemStack(Items.GLASS_BOTTLE);
	}
	
}
