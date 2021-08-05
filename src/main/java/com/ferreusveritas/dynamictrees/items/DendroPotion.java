package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.ModTabs;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffectProvider;
import com.ferreusveritas.dynamictrees.systems.substances.*;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.block.Block;
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
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;

public class DendroPotion extends Item implements ISubstanceEffectProvider, IEmptiable {

	public static final String name = "dendropotion";

	public enum DendroPotionType {
		BIOCHAR(0, true, "biochar", 0x27231c),
		DEPLETION(1, true, "depletion", 0x76be6d),
		GIGAS(2, true, "gigas", 0xe3901d),
		BURGEONING(3, true, "burgeoning", 0xa9bebe),
		FERTILITY(4, true, "fertility", 0x4ad400),
		PERSISTANCE(5, true, "persistance", 0x389aff),
		TRANSFORM(6, true, "transform", 0x7fb8a4);

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
			return getTranslationText("potion." + this.name + ".description" + (this == TRANSFORM ? ".empty" : ""), "");
		}
	}

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
		if (isInCreativeTab(tab)) {
			for (DendroPotionType type : DendroPotionType.values()) {
				if (type.getActive()) {
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
		switch (getPotionType(itemStack)) {
			default:
			case BIOCHAR:
				return null;
			case BURGEONING:
				return new SubstanceGrowth();
			case GIGAS:
				return new SubstanceMega();
			case DEPLETION:
				return new SubstanceDeplete().setAmount(15);
			case FERTILITY:
				return new SubstanceFertilize().setAmount(15);
			case PERSISTANCE:
				return new SubstanceFreeze();
			case TRANSFORM:
				return new SubstanceTransform(getTargetSpecies(itemStack));
		}
	}

	public Species getTargetSpecies(ItemStack itemStack) {
		if (itemStack.hasTagCompound()) {
			NBTTagCompound nbtTag = itemStack.getTagCompound();
			if (nbtTag.hasKey("target")) {
				String targetTree = nbtTag.getString("target");
				if (!targetTree.equals("")) {
					return TreeRegistry.findSpecies(new ResourceLocation(targetTree));
				}
			}
		}

		return null;
	}

	public ItemStack setTargetTree(ItemStack itemStack, TreeFamily family) {
		return this.setTargetSpecies(itemStack, family.getCommonSpecies());
	}

	public ItemStack setTargetSpecies(ItemStack itemStack, Species species) {
		NBTTagCompound nbtTag = itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
		nbtTag.setString("target", species.getRegistryName().toString());
		itemStack.setTagCompound(nbtTag);
		return itemStack;
	}

	public DendroPotion registerRecipes(IForgeRegistry<IRecipe> registry) {
		ItemStack awkwardStack = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionType.REGISTRY.getObject(new ResourceLocation("awkward")));

		this.addRecipe(awkwardStack, new ItemStack(Items.COAL, 1, 1), this.getPotionStack(DendroPotionType.BIOCHAR)); // Ingredient: charcoal
		this.addRecipe(Items.SLIME_BALL, DendroPotionType.DEPLETION);
		this.addRecipe(Items.PUMPKIN_SEEDS, DendroPotionType.GIGAS);
		this.addRecipe(Items.GHAST_TEAR, DendroPotionType.BURGEONING);
		this.addRecipe(Items.FISH, DendroPotionType.FERTILITY);
		this.addRecipe(Blocks.RED_FLOWER, 1, DendroPotionType.PERSISTANCE); // Ingredient: blue orchid. 
		this.addRecipe(Items.PRISMARINE_CRYSTALS, DendroPotionType.TRANSFORM);

		for (Species species : TreeRegistry.getPotionTransformableSpecies()) {
			ItemStack outputStack = setTargetSpecies(new ItemStack(this, 1, DendroPotionType.TRANSFORM.getIndex()), species);
			this.addRecipe(this.getPotionStack(DendroPotionType.TRANSFORM), species.getSeedStack(1), outputStack);
		}

		return this;
	}

	private void addRecipe(Item ingredient, DendroPotionType typeOut) {
		this.addRecipe(new ItemStack(ingredient), typeOut);
	}

	private void addRecipe(Block ingredient, int ingredientMeta, DendroPotionType typeOut) {
		this.addRecipe(new ItemStack(ingredient, 1, ingredientMeta), typeOut);
	}

	private void addRecipe(ItemStack ingredient, DendroPotionType typeOut) {
		this.addRecipe(ingredient, this.getPotionStack(typeOut));
	}

	private void addRecipe(ItemStack ingredientStack, ItemStack stackOut) {
		this.addRecipe(this.getPotionStack(DendroPotionType.BIOCHAR), ingredientStack, stackOut);
	}

	private void addRecipe(ItemStack stackIn, ItemStack ingredientStack, ItemStack stackOut) {
		BrewingRecipeRegistry.addRecipe(stackIn, ingredientStack, stackOut);
	}

	private ItemStack getPotionStack(DendroPotionType type) {
		return new ItemStack(this, 1, type.getIndex());
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag tooltipFlag) {
		super.addInformation(stack, world, tooltip, tooltipFlag);

		DendroPotionType potionType = getPotionType(stack);

		if (potionType == DendroPotionType.TRANSFORM) {
			Species species = getTargetSpecies(stack);
			if (species == null) {
				tooltip.add(getPotionType(stack).getLore());
			} else {
				tooltip.add(getTranslationText("potion.transform.description", species.getLocalizedName()));
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

	private static String getTranslationText(String path, String... args) {
		return new TextComponentTranslation(path, (Object[]) args).getUnformattedComponentText();
	}

}
