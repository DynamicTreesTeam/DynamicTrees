package com.ferreusveritas.dynamictrees.systems.dropcreators;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.dropcreators.context.DropContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;

/**
 * A drop creator for the sticks that can be harvested from leaves. Rarity of 1 equals a 1/50 chance of getting between
 * {1 - maxCount} sticks.
 *
 * @author Max Hyper
 */
public class StickDropCreator extends DropCreator {

    public static final ConfigurationProperty<ItemStack> STICK = ConfigurationProperty.property("stick", ItemStack.class);
    public static final ConfigurationProperty<Integer> MAX_COUNT = ConfigurationProperty.integer("max_count");

    public static final ItemStack STICK_STACK = new ItemStack(Items.STICK);

    public StickDropCreator(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(STICK, RARITY, MAX_COUNT);
    }

    @Override
    protected DropCreatorConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(STICK, STICK_STACK)
                .with(RARITY, 1f)
                .with(MAX_COUNT, 2);
    }

    @Override
    public void appendLeavesDrops(DropCreatorConfiguration configuration, DropContext context) {
        this.appendSticks(configuration, context);
    }

    @Override
    public void appendHarvestDrops(DropCreatorConfiguration configuration, DropContext context) {
        this.appendSticks(configuration, context);
    }

    private void appendSticks(DropCreatorConfiguration configuration, DropContext context) {
        int chance = 50;
        if (context.fortune() > 0) {
            chance -= 2 << context.fortune();
            if (chance < 10) {
                chance = 10;
            }
        }
        if (context.random().nextInt((int) (chance / configuration.get(RARITY))) == 0) {
            final ItemStack drop = configuration.getOrInvalidDefault(STICK, stick -> stick != ItemStack.EMPTY,
                    context.species().getSeedStack(1)).copy();
            drop.setCount(1 + context.random().nextInt(configuration.get(MAX_COUNT)));
            context.drops().add(drop);
        }
    }

}
