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
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DendroPotion extends Item implements ISubstanceEffectProvider, IEmptiable {
	
	public static final List<DendroBrewingRecipe> brewingRecipes = new ArrayList<>();
	
	public static final String INDEX_TAG_KEY = "potion_index";
	public static final String TREE_TAG_KEY = "target";

	public enum DendroPotionType {
		BIOCHAR(    0, true, "biochar",     0x27231c, Items.CHARCOAL),
		DEPLETION(  1, true, "depletion",	  0x76be6d, Items.SLIME_BALL),
		GIGAS( 2, true, "gigas",  0xe3901d, Items.PUMPKIN_SEEDS),
		BURGEONING( 3, true, "burgeoning",  0xa9bebe, Items.GHAST_TEAR),
		FERTILITY(  4, false, "fertility",   0x4ad400, Items.COD),
		PERSISTENCE(5, false, "persistence", 0x389aff, Items.BLUE_ORCHID),
		TRANSFORM(  6, true, "transform",   0x7fb8a4, Items.PRISMARINE_CRYSTALS),
		HARVEST(7, true, "harvest", 0xd9b856, Items.GOLDEN_APPLE),
		DENUDING(8, true, "denuding", 0xcaa35f, Items.IRON_AXE);
		
		private final int index;
		private final boolean active;
		private final String name;
		private final int color;
		private final ItemStack ingredient;

		DendroPotionType(int index, boolean active, String name, int color, Item ingredient) {
			this(index, active, name, color, new ItemStack(ingredient));
		}

		DendroPotionType(int index, boolean active, String name, int color, ItemStack ingredient) {
			this.index = index;
			this.active = active;
			this.name = name;
			this.color = color;
			this.ingredient = ingredient;
		}

		public int getIndex() {
			return index;
		}
		
		public boolean isActive() {
			return active;
		}
		
		public String getName() {
			return name;
		}
		
		public int getColor() {
			return color;
		}

		public ItemStack getIngredient() {
			return this.ingredient;
		}
		
		public ITextComponent getDescription() {
			return new TranslationTextComponent("potion." + this.name +
					".description" + (this == TRANSFORM ? ".empty" : ""))
					.withStyle(style -> style.withColor(TextFormatting.GRAY));
		}
	};

	public DendroPotion() {
		super(new Item.Properties().tab(DTRegistries.ITEM_GROUP).stacksTo(1));
	}
	
	public ItemStack applyIndexTag (final ItemStack potionStack, final int potionIndex) {
		potionStack.getOrCreateTag().putInt(INDEX_TAG_KEY, potionIndex);
		return potionStack;
	}
	
	@Override
	public void fillItemCategory(final ItemGroup group, final NonNullList<ItemStack> items) {
		if (this.allowdedIn(group)) {
			for (final DendroPotionType potion : DendroPotionType.values()) {
				if (potion.isActive()) {
					items.add(this.applyIndexTag(new ItemStack(this, 1), potion.getIndex()));
				}
			}
		}
	}
	
	public static DendroPotionType getPotionType (ItemStack stack) {
		return DendroPotionType.values()[stack.getOrCreateTag().getInt(INDEX_TAG_KEY)];
	}

	@Nullable
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
			case TRANSFORM: return new TransformSubstance(this.getTargetSpecies(itemStack));
			case HARVEST: return new HarvestSubstance();
			case DENUDING: return new DenudeSubstance();
		}
	}
	
	public Species getTargetSpecies(ItemStack itemStack) {
		final CompoundNBT nbtTag = itemStack.getOrCreateTag();

		return nbtTag.contains(TREE_TAG_KEY) ?
				TreeRegistry.findSpecies(nbtTag.getString(TREE_TAG_KEY)) :
				Species.NULL_SPECIES;
	}
	
	public ItemStack setTargetSpecies(ItemStack itemStack, Species species) {
		itemStack.getOrCreateTag().putString(TREE_TAG_KEY, species.getRegistryName().toString());
		return itemStack;
	}

	public void registerRecipes() {
		final ItemStack awkwardStack = PotionUtils.setPotion(new ItemStack(Items.POTION), Potion.byName("awkward"));

		brewingRecipes.add(this.getRecipe(awkwardStack, new ItemStack(Items.CHARCOAL), this.getPotionStack(DendroPotionType.BIOCHAR)));

		for (int i = 1; i < DendroPotionType.values().length; i++) {
			final DendroPotionType type = DendroPotionType.values()[i];

			if (!type.isActive())
				continue;

			brewingRecipes.add(this.getRecipe(type.getIngredient(), type));
		}

		for (Species species : TreeRegistry.getPotionTransformableSpecies()) {
			brewingRecipes.add(new DendroBrewingRecipe(this.getPotionStack(DendroPotionType.TRANSFORM), species.getSeedStack(1),
					this.setTargetSpecies(this.getPotionStack(DendroPotionType.TRANSFORM), species)));
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
	public String getDescriptionId(ItemStack stack) {
		return this.getDescriptionId() + "." + getPotionType(stack).getName();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		final DendroPotionType potionType = getPotionType(stack);

		if (potionType != DendroPotionType.TRANSFORM || !this.getTargetSpecies(stack).isValid()) {
			tooltip.add(getPotionType(stack).getDescription());
			return;
		}
		
		final Species species = this.getTargetSpecies(stack);
		tooltip.add(new TranslationTextComponent("potion.transform.description", species.getTextComponent())
				.withStyle(style -> style.withColor(TextFormatting.GRAY)));
	}
	
	public int getColor(ItemStack stack, int tint) {
		return tint == 0 ? getPotionType(stack).getColor() : 0xFFFFFFFF;
	}
	
	@Override
	public ItemStack getEmptyContainer() {
		return new ItemStack(Items.GLASS_BOTTLE);
	}
	
}
