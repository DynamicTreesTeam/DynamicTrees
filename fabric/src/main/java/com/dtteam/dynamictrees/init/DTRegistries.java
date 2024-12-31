package com.dtteam.dynamictrees.init;

import com.dtteam.dynamictrees.DynamicTrees;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.LinkedList;

public class DTRegistries {

    public static final LinkedList<Item> CREATIVE_TAB_ITEMS = new LinkedList<>();
    public static final CreativeModeTab DT_CREATIVE_TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, DynamicTrees.location(DynamicTrees.MOD_ID),
            FabricItemGroup.builder()
                    .icon(() ->  new ItemStack(Items.STICK))
                    .title(Component.translatable("itemGroup.dynamictrees"))
                    .displayItems((parameters, output) -> {
                        output.accept(Items.STICK);
//                for (final DendroPotion.DendroPotionType potion : DendroPotion.DendroPotionType.values()) {
//                    if (potion.isActive()) {
//                        output.accept(DendroPotion.applyIndexTag(new ItemStack(DTRegistries.DENDRO_POTION.get()), potion.getIndex()));
//                    }
//                }
                        CREATIVE_TAB_ITEMS.forEach(e -> output.accept(e.getDefaultInstance()));
                    }).build()) ;

    public static void setup (){

    }

}
