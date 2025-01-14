package com.dtteam.dynamictrees.event;

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Map;

public class DataGenerationStreamEvent extends Event implements IModBusEvent {

    private final ExistingFileHelper fileHelper;
    private final Map<ResourceKey<LootTable>, LootTable.Builder> map;
    private final LootTableSubProvider provider;
    private final String modId;

    public DataGenerationStreamEvent(final LootTableSubProvider tableProvider, String modId, ExistingFileHelper fileHelper, Map<ResourceKey<LootTable>, LootTable.Builder> map) {
        super();
        this.provider = tableProvider;
        this.modId = modId;
        this.fileHelper = fileHelper;
        this.map = map;
    }

    public LootTableSubProvider getProvider() {
        return provider;
    }

    public String getModId() {
        return modId;
    }

    public ExistingFileHelper getFileHelper() {
        return fileHelper;
    }

    public Map<ResourceKey<LootTable>, LootTable.Builder> getMap() {
        return map;
    }
}
