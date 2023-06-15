package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * @author Harley O'Connor
 */
public final class LootTableSupplier {

    private final String path;
    private final ResourceLocation name;
    private final ResourceLocation baseName;

    public LootTableSupplier(String basePath, ResourceLocation name) {
        this.path = basePath;
        this.name = name;
        this.baseName = ResourceLocationUtils.prefix(name, path);
    }

    public LootTable get(LootDataManager lootTables, Species species) {
        final LootTable speciesOverrideTable = lootTables.getLootTable(getName(species));
        if (speciesOverrideTable != LootTable.EMPTY) {
            return speciesOverrideTable;
        }
        return lootTables.getLootTable(baseName);
    }

    public ResourceLocation getName(Species species) {
        final ResourceLocation speciesName = species.getRegistryName();
        return ResourceLocationUtils.surround(name, path, "/" + speciesName.getNamespace() + "/" + speciesName.getPath());
    }

    public ResourceLocation getName() {
        return baseName;
    }


}
