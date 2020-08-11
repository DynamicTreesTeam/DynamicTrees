package com.ferreusveritas.dynamictrees;

import com.ferreusveritas.dynamictrees.blocks.TESTBLOCK;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModRegistries {

    public static Block test;
    private static boolean preInitHasRun;

    public static final ItemGroup dynamicTreesTab = new ItemGroup("dynamictrees") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(test);
        }
    };

    public static void preInit() {
        if (preInitHasRun) return;

        test = new TESTBLOCK();



        preInitHasRun = true;
    }

    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
        preInit();
        blockRegistryEvent.getRegistry().register(test);
    }

    @SubscribeEvent
    public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
        preInit();
        Item.Properties properties = new Item.Properties().group(ModRegistries.dynamicTreesTab);
        itemRegistryEvent.getRegistry().register(new BlockItem(test, properties)
                .setRegistryName(Objects.requireNonNull(test.getRegistryName())));
    }

//    @SubscribeEvent
//    public static void onEntitiesRegistry(final RegistryEvent.Register<EntityEvent> blockRegistryEvent) {
//
//    }

}
