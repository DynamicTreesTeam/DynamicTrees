package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.compat.SereneSeasonsSeasonProvider;
import com.dtteam.dynamictrees.platform.services.ICompatHelper;

public class FabricCompatHelper implements ICompatHelper {

    @Override
    public void registerSeasonProvider(String modId) {
        switch (modId) {
            case DynamicTrees.SERENE_SEASONS ->
                    SereneSeasonsSeasonProvider.registerSereneSeasonsProvider();
            default -> {
                // No other season mods supported on Fabric yet.
            }
        }
    }

}
