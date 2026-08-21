package com.dtteam.dynamictrees.event;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import java.util.Map;

public class DataGenerationStreamEvent extends Event implements IModBusEvent {

    private final Map<ResourceKey<LootTable>, LootTable.Builder> map;
    private final LootTableSubProvider provider;
    private final String modId;
    private final HolderLookup.Provider registries;

    public DataGenerationStreamEvent(final LootTableSubProvider tableProvider, String modId,
                                     Map<ResourceKey<LootTable>, LootTable.Builder> map, HolderLookup.Provider registries) {
        super();
        this.provider = tableProvider;
        this.modId = modId;
        this.map = map;
        this.registries = registries;
    }

    public LootTableSubProvider getProvider() {
        return provider;
    }

    public String getModId() {
        return modId;
    }

    public Map<ResourceKey<LootTable>, LootTable.Builder> getMap() {
        return map;
    }

    public HolderLookup.Provider getRegistries() {
        return registries;
    }
}
