package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.platform.services.IInteractionHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public class FabricInteractionHelper implements IInteractionHelper {

    //Fabric doesn't have any implementation of tool abilities
    //so a simple axe check will have to do.
    @Override
    public boolean canToolAxeStrip(ItemStack stack) {
        return stack.is(ItemTags.AXES);
    }

    @Override
    public boolean canToolAxeDig(ItemStack stack) {
        return stack.is(ItemTags.AXES);
    }

    @Override
    public int setSeedItemEntityLifespan(ItemEntity entityItem, Seed seed) {
        return 0;
    }
}
