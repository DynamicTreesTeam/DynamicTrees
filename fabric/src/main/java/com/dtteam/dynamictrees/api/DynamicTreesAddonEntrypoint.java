package com.dtteam.dynamictrees.api;

import com.dtteam.dynamictrees.api.resource.TreeResourceManager;
import com.dtteam.dynamictrees.api.resource.loading.StagedApplierResourceLoader;
import com.dtteam.dynamictrees.deserialization.PropertyAppliers;

public interface DynamicTreesAddonEntrypoint {
    void onDynamicTreesPreSetup();

    default void onAddResourceLoaders(TreeResourceManager resourceManager) {
    }

    default <O, I> void onRegisterStagedApplier(StagedApplierResourceLoader.ApplierStage stage, PropertyAppliers<O, I> appliers, String identifier) {
    }
}

