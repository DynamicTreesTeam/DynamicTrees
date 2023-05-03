package com.ferreusveritas.dynamictrees.event.handler;

import com.ferreusveritas.dynamictrees.client.TooltipHandler;
import com.ferreusveritas.dynamictrees.compat.season.SeasonHelper;
import com.ferreusveritas.dynamictrees.event.FutureBreak;
import com.ferreusveritas.dynamictrees.init.DTClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class CommonEventHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.side == LogicalSide.SERVER) {
            FutureBreak.process(event.level);
        }

        if (event.type == TickEvent.Type.LEVEL && event.phase == TickEvent.Phase.START) {
            SeasonHelper.updateTick(event.level, event.level.getDayTime());
        }
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            DTClient.discoverWoodColors();
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onItemTooltipAdded(ItemTooltipEvent event) {
        TooltipHandler.setupTooltips(event);
    }

}