package com.ferreusveritas.dynamictrees.loot;

import com.ferreusveritas.dynamictrees.loot.condition.DTLootConditions;
import com.ferreusveritas.dynamictrees.loot.entry.DTLootPoolEntries;
import com.ferreusveritas.dynamictrees.loot.function.DTLootFunctions;

/**
 * @author Harley O'Connor
 */
public final class DTLoot {

    /** Invoked to initialise static fields. */
    public static void load() {
        DTLootParameterSets.load();
        DTLootContextParams.load();
        DTLootPoolEntries.load();
        DTLootConditions.load();
        DTLootFunctions.load();
    }

}
