package com.ferreusveritas.dynamictrees.loot;

import com.ferreusveritas.dynamictrees.loot.condition.DTLootConditions;
import com.ferreusveritas.dynamictrees.loot.entry.DTLootEntries;
import com.ferreusveritas.dynamictrees.loot.function.DTLootFunctions;

/**
 * @author Harley O'Connor
 */
public final class DTLoot {

    /** Invoked to initialise static fields. */
    public static void load() {
        DTLootParameterSets.load();
        DTLootParameters.load();
        DTLootEntries.load();
        DTLootConditions.load();
        DTLootFunctions.load();
    }

}
