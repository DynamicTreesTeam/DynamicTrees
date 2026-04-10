package com.dtteam.dynamictrees.loot;

import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.IdentifierUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * @author Harley O'Connor
 */
public final class LootTableSupplier {

    private final String path;
    private final Identifier name;
    private final Identifier baseName;

    public LootTableSupplier(String basePath, Identifier name) {
        this.path = basePath;
        this.name = name;
        this.baseName = IdentifierUtils.prefix(name, path);
    }

    public LootTable get(ReloadableServerRegistries.Holder lootTables, Species species) {
        final LootTable speciesOverrideTable = lootTables.getLootTable(ResourceKey.create(Registries.LOOT_TABLE, getName(species)));
        if (speciesOverrideTable != LootTable.EMPTY) {
            return speciesOverrideTable;
        }
        return lootTables.getLootTable(ResourceKey.create(Registries.LOOT_TABLE, baseName));
    }

    public Identifier getName(Species species) {
        final Identifier speciesName = species.getRegistryName();
        return IdentifierUtils.surround(name, path, "/" + speciesName.getNamespace() + "/" + speciesName.getPath());
    }

    public Identifier getName() {
        return baseName;
    }


}
