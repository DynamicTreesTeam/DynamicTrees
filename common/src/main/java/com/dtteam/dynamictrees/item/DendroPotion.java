package com.dtteam.dynamictrees.item;

import com.dtteam.dynamictrees.api.substance.Emptiable;
import com.dtteam.dynamictrees.api.substance.SubstanceEffect;
import com.dtteam.dynamictrees.api.substance.SubstanceEffectProvider;
import com.dtteam.dynamictrees.systems.substance.*;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;

public class DendroPotion extends Item implements SubstanceEffectProvider, Emptiable {

//    public static final List<DendroBrewingRecipe> brewingRecipes = new ArrayList<>();

    public static final String INDEX_TAG_KEY = "potion_index";
    public static final String TREE_TAG_KEY = "target";

    public enum DendroPotionType {
        BIOCHAR(0, true, "biochar", 0x27231c, Items.CHARCOAL),
        DEPLETION(1, true, "depletion", 0x76be6d, Items.SLIME_BALL),
        GIGAS(2, true, "gigas", 0xe3901d, Items.PUMPKIN_SEEDS),
        BURGEONING(3, true, "burgeoning", 0xa9bebe, Items.GHAST_TEAR),
        FERTILITY(4, false, "fertility", 0x4ad400, Items.COD),
        PERSISTENCE(5, false, "persistence", 0x389aff, Items.BLUE_ORCHID),
        TRANSFORM(6, true, "transform", 0x7fb8a4, Items.PRISMARINE_CRYSTALS),
        HARVEST(7, false, "harvest", 0xe7c164, Items.GLISTERING_MELON_SLICE),
        DENUDING(8, false, "denuding", 0xa47e46, Items.FERMENTED_SPIDER_EYE, 1);//obtained by corrupting depletion

        private final int index;
        private final boolean active;
        private final String name;
        private final int color;
        private final ItemStack ingredient;
        private final int baseIndex;

        DendroPotionType(int index, boolean active, String name, int color, Item ingredient) {
            this(index, active, name, color, ingredient, 0);
        }

        DendroPotionType(int index, boolean active, String name, int color, Item ingredient, int baseIndex) {
            this.index = index;
            this.active = active;
            this.name = name;
            this.color = color;
            this.ingredient = new ItemStack(ingredient);
            this.baseIndex = baseIndex;
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

        public Component getDescription() {
            return Component.translatable("potion." + this.name +
                    ".description" + (this == TRANSFORM ? ".empty" : ""))
                    .withStyle(style -> style.withColor(ChatFormatting.GRAY));
        }

        public DendroPotionType getBasePotionType() {
            return DendroPotionType.values()[baseIndex];
        }
    }

    public DendroPotion() {
        super(new Properties().stacksTo(1));
    }

    public static ItemStack applyIndexTag(final ItemStack potionStack, final int potionIndex) {
//        potionStack.getOrCreateTag().putInt(INDEX_TAG_KEY, potionIndex);
        return potionStack;
    }

    public static DendroPotionType getPotionType(ItemStack stack) {
//        return DendroPotionType.values()[stack.getOrCreateTag().getInt(INDEX_TAG_KEY)];
        return DendroPotionType.BIOCHAR;
    }

    @Nullable
    @Override
    public SubstanceEffect getSubstanceEffect(ItemStack itemStack) {
        return switch (getPotionType(itemStack)) {
            default -> null;
            case BURGEONING -> new GrowthSubstance();
            case GIGAS -> new MegaSubstance();
            case DEPLETION -> new DepleteSubstance().setAmount(15);
            case FERTILITY -> new FertilizeSubstance().setAmount(15);
            case PERSISTENCE -> new FreezeSubstance();
            case TRANSFORM -> new TransformSubstance(this.getTargetSpecies(itemStack));
            case HARVEST -> new HarvestSubstance();
            case DENUDING -> new DenudeSubstance();
        };
    }

    public Species getTargetSpecies(ItemStack itemStack) {
//        final CompoundTag nbtTag = itemStack.getOrCreateTag();
//
//        return nbtTag.contains(TREE_TAG_KEY) ?
//                TreeRegistry.findSpecies(nbtTag.getString(TREE_TAG_KEY)) :
//                Species.NULL_SPECIES;
        return Species.NULL_SPECIES;
    }

    public ItemStack setTargetSpecies(ItemStack itemStack, Species species) {
//        itemStack.getOrCreateTag().putString(TREE_TAG_KEY, species.getRegistryName().toString());
        return itemStack;
    }

    public void registerRecipes() {
//        final ItemStack baseStack = PotionUtils.setPotion(new ItemStack(Items.POTION), Potion.byName(DTConfigs.BIOCHAR_BASE_BREWING_BASE.get()));
//
//        //Biochar potion
//        brewingRecipes.add(this.getRecipe(baseStack, new ItemStack(Items.CHARCOAL), this.getPotionStack(DendroPotionType.BIOCHAR)));
//
//        //Regular potions
//        for (int i = 1; i < DendroPotionType.values().length; i++) {
//            final DendroPotionType type = DendroPotionType.values()[i];
//
//            if (!type.isActive()) continue;
//
//            brewingRecipes.add(this.getRecipe(type.getIngredient(), type));
//        }
//
//        //Transformation potions
//        for (Species species : TreeRegistry.getPotionTransformableSpecies()) {
//            brewingRecipes.add(new DendroBrewingRecipe(this.getPotionStack(DendroPotionType.TRANSFORM), species.getSeedStack(1),
//                    this.setTargetSpecies(this.getPotionStack(DendroPotionType.TRANSFORM), species)));
//        }
//
//        brewingRecipes.forEach(BrewingRecipeRegistry::addRecipe);
    }

//    private DendroBrewingRecipe getRecipe(ItemStack ingredient, DendroPotionType typeOut) {
//        return this.getRecipe(this.getPotionStack(typeOut.getBasePotionType()), ingredient, this.getPotionStack(typeOut));
//    }
//
//    private DendroBrewingRecipe getRecipe(ItemStack stackIn, ItemStack ingredientStack, ItemStack stackOut) {
//        return new DendroBrewingRecipe(stackIn, ingredientStack, stackOut);
//    }

    private ItemStack getPotionStack(DendroPotionType type) {
        return this.applyIndexTag(new ItemStack(this), type.getIndex());
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.getDescriptionId() + "." + getPotionType(stack).getName();
    }

//    @Override
//    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flagIn) {
//        final DendroPotionType potionType = getPotionType(stack);
//
//        if (potionType != DendroPotionType.TRANSFORM || !this.getTargetSpecies(stack).isValid()) {
//            tooltip.add(getPotionType(stack).getDescription());
//            return;
//        }
//
//        final Species species = this.getTargetSpecies(stack);
//        tooltip.add(Component.translatable("potion.transform.description", species.getTextComponent())
//                .withStyle(style -> style.withColor(ChatFormatting.GRAY)));
//    }

    public int getColor(ItemStack stack, int tint) {
        return tint == 0 ? getPotionType(stack).getColor() : 0xFFFFFFFF;
    }

    @Override
    public ItemStack getEmptyContainer() {
        return new ItemStack(Items.GLASS_BOTTLE);
    }

}
