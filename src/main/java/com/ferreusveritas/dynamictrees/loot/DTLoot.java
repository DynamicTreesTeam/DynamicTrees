package com.ferreusveritas.dynamictrees.loot;

import com.ferreusveritas.dynamictrees.loot.condition.DTLootConditions;
import com.ferreusveritas.dynamictrees.loot.function.DTLootFunctions;

/**
 * @author Harley O'Connor
 */
public final class DTLoot {

    /** Invoked to initialise static fields. */
    public static void load() {
        DTLootParameters.load();
        DTLootParameters.load();
        DTLootConditions.load();
        DTLootFunctions.load();
    }

}
