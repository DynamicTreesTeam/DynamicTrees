package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.util.ResourceLocation;

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

    public LootTable get(LootTableManager lootTableManager, Species species) {
        final LootTable speciesOverrideTable = lootTableManager.get(getName(species));
        if (speciesOverrideTable != LootTable.EMPTY) {
            return speciesOverrideTable;
        }
        return lootTableManager.get(baseName);
    }

    public ResourceLocation getName(Species species) {
        final ResourceLocation speciesName = species.getRegistryName();
        return ResourceLocationUtils.surround(name, path, "/" + speciesName.getNamespace() + "/" + speciesName.getPath());
    }

    public ResourceLocation getName() {
        return baseName;
    }


}
