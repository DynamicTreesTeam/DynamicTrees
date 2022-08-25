package com.ferreusveritas.dynamictrees.event.handlers;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

@Mod.EventBusSubscriber
public class MissingEventHandler {

    ///////////////////////////////////////////
    // MISSING REMAPPING
    ///////////////////////////////////////////

    /**
     * Here we'll simply remap the old "growingtrees" modid to the new modid for old blocks and items.
     *
     * @param event
     */
    //Missing Blocks Resolved Here
    @SubscribeEvent
    public void missingBlockMappings(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<Block> missing : event.getAllMappings(ForgeRegistries.Keys.BLOCKS)) {
            ResourceLocation resLoc = missing.getKey();
            String domain = resLoc.getNamespace();
            String path = resLoc.getPath();
            if (domain.equals("growingtrees")) {
                Logger.getLogger(DynamicTrees.MOD_ID).log(Level.CONFIG, "Remapping Missing Block: " + path);
                Block mappedBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(DynamicTrees.MOD_ID, path));
                if (mappedBlock != Blocks.AIR) { //Air is what you get when do don't get what you're looking for.
                    assert mappedBlock != null;
                    missing.remap(mappedBlock);
                }
            }
        }
        for (MissingMappingsEvent.Mapping<Item> missing : event.getAllMappings(ForgeRegistries.Keys.ITEMS)) {
            ResourceLocation resLoc = missing.getKey();
            String domain = resLoc.getNamespace();
            String path = resLoc.getPath();
            if (domain.equals("growingtrees")) {
                Logger.getLogger(DynamicTrees.MOD_ID).log(Level.CONFIG, "Remapping Missing Item: " + path);
                Item mappedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(DynamicTrees.MOD_ID, path));
                if (mappedItem != null) { //Null is what you get when do don't get what you're looking for.
                    missing.remap(mappedItem);
                }
            }
        }
    }

    //Missing Items Resolved Here
//    @SubscribeEvent
//    public void missingItemMappings(MissingMappings<Item> event) {
//        for (Mapping<Item> missing : event.getMappings()) {
//            ResourceLocation resLoc = missing.key;
//            String domain = resLoc.getNamespace();
//            String path = resLoc.getPath();
//            if (domain.equals("growingtrees")) {
//                Logger.getLogger(DynamicTrees.MOD_ID).log(Level.CONFIG, "Remapping Missing Item: " + path);
//                Item mappedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(DynamicTrees.MOD_ID, path));
//                if (mappedItem != null) { //Null is what you get when do don't get what you're looking for.
//                    missing.remap(mappedItem);
//                }
//            }
//        }
//    }

}
