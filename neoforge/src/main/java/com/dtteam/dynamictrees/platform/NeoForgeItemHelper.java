package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.platform.services.IItemHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;

public class NeoForgeItemHelper implements IItemHelper {

    @Override
    public boolean canToolAxeStrip(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.AXE_STRIP);
    }

    @Override
    public boolean canToolAxeDig(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.AXE_DIG);
    }
}