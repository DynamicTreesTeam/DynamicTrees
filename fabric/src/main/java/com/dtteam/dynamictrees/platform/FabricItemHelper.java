package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.platform.services.IItemHelper;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

public class FabricItemHelper implements IItemHelper {

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
}
