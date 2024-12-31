//package com.dtteam.dynamictrees.data.provider;
//
//import net.minecraft.data.loot.LootTableSubProvider;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.level.storage.loot.LootTable;
//import net.minecraftforge.common.data.ExistingFileHelper;
//import net.minecraftforge.eventbus.api.Event;
//import net.minecraftforge.fml.event.IModBusEvent;
//
//import java.util.Map;
//
//public class DataGenerationStreamEvent extends Event implements IModBusEvent {
//
//    private final ExistingFileHelper existingFileHelper;
//    private final Map<ResourceLocation, LootTable.Builder> map;
//    private final LootTableSubProvider provider;
//    private final String modId;
//
//    public DataGenerationStreamEvent(final LootTableSubProvider tableProvider, String modId, ExistingFileHelper existingFileHelper, Map<ResourceLocation, LootTable.Builder> map) {
//        super();
//        this.provider = tableProvider;
//        this.modId = modId;
//        this.existingFileHelper = existingFileHelper;
//        this.map = map;
//    }
//
//    public LootTableSubProvider getProvider() {
//        return provider;
//    }
//
//    public String getModId() {
//        return modId;
//    }
//
//    public ExistingFileHelper getExistingFileHelper() {
//        return existingFileHelper;
//    }
//
//    public Map<ResourceLocation, LootTable.Builder> getMap() {
//        return map;
//    }
//}
