package com.dtteam.dynamictrees.item;

import com.dtteam.dynamictrees.api.substance.Emptiable;
import com.dtteam.dynamictrees.api.substance.SubstanceEffect;
import com.dtteam.dynamictrees.api.substance.SubstanceEffectProvider;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.systems.substance.DepleteSubstance;
import com.dtteam.dynamictrees.systems.substance.GrowthSubstance;
import com.dtteam.dynamictrees.systems.substance.HarvestSubstance;
import com.dtteam.dynamictrees.systems.substance.MegaSubstance;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;

public class DendroPotion extends Item implements SubstanceEffectProvider, Emptiable {

    public enum DendroPotionType {
        BIOCHAR(0, true, "biochar", 0xFF27231c, Items.CHARCOAL),
        DEPLETION(1, true, "depletion", 0xFF76be6d, Items.SLIME_BALL),
        MEGA(2, true, "mega", 0xFFe3901d, Items.PUMPKIN_SEEDS),
        BURGEONING(3, true, "burgeoning", 0xFFa9bebe, Items.GHAST_TEAR),
        HARVEST(4, false, "harvest", 0xFFe7c164, Items.GLISTERING_MELON_SLICE);

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
            return Component.translatable("potion." + this.name + ".description")
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
        potionStack.set(DTRegistries.DENDRO_POTION_INDEX_DATA_COMPONENT.get(), potionIndex);
        return potionStack;
    }

    public static DendroPotionType getPotionType(ItemStack stack) {
        int index = stack.getOrDefault(DTRegistries.DENDRO_POTION_INDEX_DATA_COMPONENT.get(), 0);
        return DendroPotionType.values()[Math.clamp(index, 0, DendroPotionType.values().length-1)];
    }

    @Nullable
    @Override
    public SubstanceEffect getSubstanceEffect(ItemStack itemStack) {
        return switch (getPotionType(itemStack)) {
            case BURGEONING -> new GrowthSubstance();
            case MEGA -> new MegaSubstance();
            case DEPLETION -> new DepleteSubstance().setAmount(15);
            case HARVEST -> new HarvestSubstance();
            default -> null;
        };
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return this.getDescriptionId() + "." + getPotionType(stack).getName();
    }

    public int getColor(ItemStack stack, int tint) {
        return tint == 0 ? getPotionType(stack).getColor() : 0xFFFFFFFF;
    }

    @Override
    public ItemStack getEmptyContainer() {
        return new ItemStack(Items.GLASS_BOTTLE);
    }

}
