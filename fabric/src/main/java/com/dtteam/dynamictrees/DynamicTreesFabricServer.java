package com.dtteam.dynamictrees;

import net.fabricmc.api.DedicatedServerModInitializer;

/**
 * Dedicated server entrypoint. Fabric invokes this after the whole main entrypoint phase,
 * so every mod's blocks are registered by the time the treepack setup stage (primitive
 * log/leaves lookups etc.) runs — mirroring NeoForge's FMLCommonSetupEvent timing.
 * The client counterpart is {@link DynamicTreesFabricClient}.
 */
public class DynamicTreesFabricServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        DynamicTrees.commonSetup();
    }
}
