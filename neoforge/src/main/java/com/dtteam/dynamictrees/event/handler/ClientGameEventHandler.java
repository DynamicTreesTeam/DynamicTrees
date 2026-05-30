package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.item.Seed;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = DynamicTrees.MOD_ID, value = Dist.CLIENT)
public class ClientGameEventHandler {

    ///////////////////////////////////////////
    // ITEM
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void onItemTooltipAdded(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof Seed seed){
            Player player = event.getEntity();
            if (player == null) return;

            LevelContext levelContext = LevelContext.create(player.level());
            seed.appendHoverText(stack, levelContext, event.getToolTip(), player);
        }
    }

}