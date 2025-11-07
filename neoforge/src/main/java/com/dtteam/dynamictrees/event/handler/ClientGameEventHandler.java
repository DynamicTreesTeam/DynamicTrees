package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.client.Tooltips;
import com.dtteam.dynamictrees.item.Seed;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
        Item item = stack.getItem();

        if (!(item instanceof Seed seed)) {
            return;
        }

        Player player = event.getEntity();

        if (player == null) {
            return;
        }

        LevelContext levelContext = LevelContext.create(player.level());
        Species species = seed.getSpecies();

        if (SeasonHelper.getSeasonValue(levelContext, BlockPos.ZERO) == null || !species.isValid()) {
            return;
        }

        int flags = seed.getSpecies().getSeasonalTooltipFlags(levelContext);
        Tooltips.applySeasonalTooltips(event.getToolTip(), flags);
    }

}