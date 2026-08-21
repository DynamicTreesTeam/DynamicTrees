package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.compat.SereneSeasonsSeasonProvider;
import com.dtteam.dynamictrees.platform.services.ICompatHelper;

public class FabricCompatHelper implements ICompatHelper {

    @Override
    public void registerSeasonProvider(String modId) {
        // Only Serene Seasons has a Fabric compat dependency wired up (see fabric/build.gradle);
        // Ecliptic Seasons (NeoForge's other branch here) has no Fabric port pinned yet.
        if (DynamicTrees.SERENE_SEASONS.equals(modId)) {
            SereneSeasonsSeasonProvider.registerSereneSeasonsProvider();
        }
    }

}