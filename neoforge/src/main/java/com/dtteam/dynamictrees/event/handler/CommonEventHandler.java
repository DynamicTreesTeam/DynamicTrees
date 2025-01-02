package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.util.TooltipHandler;
import com.dtteam.dynamictrees.systems.FutureBreak;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class CommonEventHandler {

    @SubscribeEvent
    public void onLevelTick(LevelTickEvent event) {
        if (!event.getLevel().isClientSide) {
            FutureBreak.process(event.getLevel());
        }
    }
    @SubscribeEvent
    public void onPreLevelTick(LevelTickEvent.Pre event) {
        SeasonHelper.updateTick(event.getLevel(), event.getLevel().getDayTime());
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) {
            ClientEventHandler.discoverWoodColors();
        }
    }

    @SubscribeEvent
    public void onItemTooltipAdded(ItemTooltipEvent event) {
        TooltipHandler.setupTooltips(event);
    }

}