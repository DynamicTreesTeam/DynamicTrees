package com.ferreusveritas.dynamictrees.loot;

/**
 * @author Harley O'Connor
 */
public final class DTLoot {

    /**
     * Invoked to initialise static fields.
     */
    public static void load() {
        DTLootParameterSets.load();
        DTLootContextParams.load();
    }

}
