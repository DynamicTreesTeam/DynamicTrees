package com.ferreusveritas.dynamictrees.items;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.substances.IEmptiable;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffect;
import com.ferreusveritas.dynamictrees.api.substances.ISubstanceEffectProvider;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.substances.*;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.DendroBrewingRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DendroPotion extends Item implements ISubstanceEffectProvider, IEmptiable {
	
	public static final List<DendroBrewingRecipe> brewingRecipes = new ArrayList<>();
	
	public static final String INDEX_TAG_KEY = "potion_index";
	public static final String TREE_TAG_KEY = "target";

	public enum DendroPotionType {
		BIOCHAR(    0, true, "biochar",     0x27231c),
		DEPLETION(  1, true, "depletion",	  0x76be6d),
		GIGAS( 2, true, "gigas",  0xe3901d),
		BURGEONING( 3, true, "burgeoning",  0xa9bebe),
		FERTILITY(  4, true, "fertility",   0x4ad400),
		PERSISTENCE(5, true, "persistence", 0x389aff),
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
		
		public ITextComponent getDescription() {
			return new TranslationTextComponent("potion." + this.name + ".description" + (this == TRANSFORM ? ".empty" : ""));
		}
	};

	public DendroPotion() {
		super(new Item.Properties().group(DTRegistries.dynamicTreesTab).maxStackSize(1));
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
		return DendroPotionType.values()[stack.getOrCreateTag().getInt(INDEX_TAG_KEY)];
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
			case PERSISTENCE: return new FreezeSubstance();
			case TRANSFORM: return new TransformSubstance(getTargetSpecies(itemStack));
		}
	}
	
	public Species getTargetSpecies(ItemStack itemStack) {
		final CompoundNBT nbtTag = itemStack.getOrCreateTag();

		if (nbtTag.contains(TREE_TAG_KEY)) {
			return TreeRegistry.findSpecies(new ResourceLocation(nbtTag.getString(TREE_TAG_KEY)));
		}

		return null;
	}
	
	public ItemStack setTargetSpecies(ItemStack itemStack, Species species) {
		itemStack.getOrCreateTag().putString(TREE_TAG_KEY, species.getRegistryName().toString());
		return itemStack;
	}
	
	public void registerRecipes() {
		ItemStack awkwardStack = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potion.getPotionTypeForName("awkward"));

		Collections.addAll(brewingRecipes, this.getRecipe(awkwardStack, new ItemStack(Items.CHARCOAL), this.getPotionStack(DendroPotionType.BIOCHAR)),
				this.getRecipe(Items.SLIME_BALL, DendroPotionType.DEPLETION),
				this.getRecipe(Items.PUMPKIN_SEEDS, DendroPotionType.GIGAS),
				this.getRecipe(Items.GHAST_TEAR, DendroPotionType.BURGEONING),
				this.getRecipe(Items.COD, DendroPotionType.FERTILITY),
				this.getRecipe(Blocks.BLUE_ORCHID, DendroPotionType.PERSISTENCE),
				this.getRecipe(Items.PRISMARINE_CRYSTALS, DendroPotionType.TRANSFORM));

		for (Species species : TreeRegistry.getPotionTransformableSpecies()) {
			final ItemStack outputStack = setTargetSpecies(this.getPotionStack(DendroPotionType.TRANSFORM), species);
			brewingRecipes.add(new DendroBrewingRecipe(this.getPotionStack(DendroPotionType.TRANSFORM), species.getSeedStack(1), outputStack));
		}
		
		brewingRecipes.forEach(BrewingRecipeRegistry::addRecipe);
	}

	private DendroBrewingRecipe getRecipe(Item ingredient, DendroPotionType typeOut) {
		return this.getRecipe(new ItemStack(ingredient), typeOut);
	}

	private DendroBrewingRecipe getRecipe(Block ingredient, DendroPotionType typeOut) {
		return this.getRecipe(new ItemStack(ingredient), typeOut);
	}

	private DendroBrewingRecipe getRecipe(ItemStack ingredient, DendroPotionType typeOut) {
		return this.getRecipe(ingredient, this.getPotionStack(typeOut));
	}

	private DendroBrewingRecipe getRecipe(ItemStack ingredientStack, ItemStack stackOut) {
		return this.getRecipe(this.getPotionStack(DendroPotionType.BIOCHAR), ingredientStack, stackOut);
	}

	private DendroBrewingRecipe getRecipe(ItemStack stackIn, ItemStack ingredientStack, ItemStack stackOut) {
		return new DendroBrewingRecipe(stackIn, ingredientStack, stackOut);
	}

	private ItemStack getPotionStack (DendroPotionType type) {
		return this.applyIndexTag(new ItemStack(this), type.getIndex());
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
				tooltip.add(getPotionType(stack).getDescription());
			} else {
				tooltip.add(new TranslationTextComponent("potion.transform.description", species.getLocalizedName()));
			}
		} else {
			tooltip.add(getPotionType(stack).getDescription());
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
