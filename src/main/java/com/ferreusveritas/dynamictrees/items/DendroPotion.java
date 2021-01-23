package com.ferreusveritas.dynamictrees.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffectProvider;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.init.DTTrees;
import com.ferreusveritas.dynamictrees.systems.substances.DepleteSubstance;
import com.ferreusveritas.dynamictrees.systems.substances.FertilizeSubstance;
import com.ferreusveritas.dynamictrees.systems.substances.FreezeSubstance;
import com.ferreusveritas.dynamictrees.systems.substances.GrowthSubstance;
import com.ferreusveritas.dynamictrees.systems.substances.MegaSubstance;
import com.ferreusveritas.dynamictrees.systems.substances.TransformSubstance;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import com.ferreusveritas.dynamictrees.util.DendroBrewingRecipe;

import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;

public class DendroPotion extends Item implements ISubstanceEffectProvider, IEmptiable {
	
	public static final List<DendroBrewingRecipe> brewingRecipes = new ArrayList<>();
	
	public static final String INDEX_TAG_KEY = "potion_index";
	public static final String TREE_TAG_KEY = "target";
	public static final String name = "dendro_potion";
	
	public enum DendroPotionType {
		BIOCHAR(    0, true, "biochar",     0x27231c, "Base tree potion for brewing"),
		DEPLETION(  1, true, "depletion",	  0x76be6d, "Destroys tree soil fertility"),
		GIGAS( 2, true, "gigas",  0xe3901d, "Allows a tree to become mega size"),
		BURGEONING( 3, true, "burgeoning",  0xa9bebe, "Quickly grow a tree"),
		FERTILITY(  4, true, "fertility",   0x4ad400, "Fully fertilizes tree soil"),
		PERSISTANCE(5, true, "persistance", 0x389aff, "Stops tree from changing"),
		TRANSFORM(  6, true, "transform",   0x7fb8a4, "Base tree potion for brewing transformations");
		
		private final int index;
		private final boolean active;
		private final String name;
		private final int color;
		private final String lore;
		
		DendroPotionType(int index, boolean active, String name, int color, String lore) {
			this.index = index;
			this.active = active;
			this.name = name;
			this.color = color;
			this.lore = lore;
		}
		
		PotionItem p;
		
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
			return lore;
		}
	};
	
	public DendroPotion() {
		this(name);
	}
	
	public DendroPotion(String name) {
		super(new Item.Properties().group(DTRegistries.dynamicTreesTab).maxStackSize(1));
		this.setRegistryName(name);
	}
	
	public ItemStack applyIndexTag (final ItemStack potionStack, final int potionIndex) {
		potionStack.getOrCreateTag().putInt(INDEX_TAG_KEY, potionIndex);
		return potionStack;
	}
	
	@Override
	public void fillItemGroup(final ItemGroup group, final NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			for (final DendroPotionType potion : DendroPotionType.values()) {
				if (potion.getActive()) {
					final ItemStack potionStack = new ItemStack(this, 1);
					items.add(this.applyIndexTag(potionStack, potion.getIndex()));
				}
			}
		}
	}
	
	public static DendroPotionType getPotionType (ItemStack stack) {
		return DendroPotionType.values()[stack.getTag().getInt(INDEX_TAG_KEY)];
	}
	
	@Override
	public ISubstanceEffect getSubstanceEffect(ItemStack itemStack) {
		switch (getPotionType(itemStack)) {
			default:
			case BIOCHAR: return null;
			case BURGEONING: return new GrowthSubstance();
			case GIGAS: return new MegaSubstance();
			case DEPLETION: return new DepleteSubstance().setAmount(15);
			case FERTILITY: return new FertilizeSubstance().setAmount(15);
			case PERSISTANCE: return new FreezeSubstance();
			case TRANSFORM: return new TransformSubstance(getTargetSpecies(itemStack));
		}
	}
	
	public Species getTargetSpecies(ItemStack itemStack) {
		if (itemStack.hasTag()){
			final CompoundNBT nbtTag = itemStack.getTag();

			if (nbtTag.contains(TREE_TAG_KEY)) {
				return TreeRegistry.findSpecies(new ResourceLocation(nbtTag.getString(TREE_TAG_KEY)));
			}
		}

		return null;
	}
	
	public ItemStack setTargetTree(ItemStack itemStack, TreeFamily tree) {
		itemStack.getOrCreateTag().putString(TREE_TAG_KEY, tree.getCommonSpecies().getRegistryName().toString()); // Only store the common species
		return itemStack;
	}
	
	public DendroPotion registerRecipes() {
		final Potion awkward = Potion.getPotionTypeForName("awkward");
		
		brewingRecipes.add(new DendroBrewingRecipe(
				PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), awkward),
				new ItemStack(Items.CHARCOAL, 1), // Charcoal
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.BIOCHAR.getIndex())));
		brewingRecipes.add(new DendroBrewingRecipe(
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.SLIME_BALL), // Slime ball
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.DEPLETION.getIndex())));
		brewingRecipes.add(new DendroBrewingRecipe(
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.PUMPKIN_SEEDS), // Pumpkin seeds
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.GIGAS.getIndex())));
		brewingRecipes.add(new DendroBrewingRecipe(
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.GHAST_TEAR), // Ghast Tear
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.BURGEONING.getIndex())));
		brewingRecipes.add(new DendroBrewingRecipe(
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.COD), // Raw Fish
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.FERTILITY.getIndex())));
		brewingRecipes.add(new DendroBrewingRecipe(
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Blocks.BLUE_ORCHID), // Blue Orchid
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.PERSISTANCE.getIndex())));
		brewingRecipes.add(new DendroBrewingRecipe(
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.BIOCHAR.getIndex()),
				new ItemStack(Items.PRISMARINE_CRYSTALS), // Prismarine Crystals
				this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.TRANSFORM.getIndex())));
		
		for (TreeFamily tree : DTTrees.baseFamilies) {
			final ItemStack outputStack = setTargetTree(this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.TRANSFORM.getIndex()), tree);
			brewingRecipes.add(new DendroBrewingRecipe(this.applyIndexTag(new ItemStack(this, 1), DendroPotionType.TRANSFORM.getIndex()), tree.getCommonSpecies().getSeedStack(1), outputStack));
		}
		
		brewingRecipes.forEach(BrewingRecipeRegistry::addRecipe);
		
		return this;
	}
	
	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		return new TranslationTextComponent(super.getName().getString() + "." + getPotionType(stack).getName());
	}
	
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		
		final DendroPotionType potionType = getPotionType(stack);
		
		if (potionType == DendroPotionType.TRANSFORM) {
			Species species = getTargetSpecies(stack);
			if(species == null) {
				tooltip.add(new StringTextComponent("§7" + getPotionType(stack).getLore()));
			} else {
				tooltip.add(new StringTextComponent("§7Transform a tree into a §a" + species.getLocalizedName() + " §7tree"));
			}
		} else {
			tooltip.add(new StringTextComponent("§7" + getPotionType(stack).getLore()));
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
