package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.utility.TooltipHandler;
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
        TooltipHandler.setupTooltips(event);
    }
}