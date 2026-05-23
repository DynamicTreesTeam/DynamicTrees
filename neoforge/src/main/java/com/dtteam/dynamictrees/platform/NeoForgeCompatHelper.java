package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.compat.EclipticSeasonsProvider;
import com.dtteam.dynamictrees.compat.SereneSeasonsProvider;
import com.dtteam.dynamictrees.platform.services.ICompatHelper;

public class NeoForgeCompatHelper implements ICompatHelper {

    @Override
    public void registerSeasonProvider(String modId) {
        switch (modId){
            case DynamicTrees.SERENE_SEASONS ->
                    SereneSeasonsProvider.registerSereneSeasonsProvider();
            case DynamicTrees.ECLIPTIC_SEASONS ->
                    EclipticSeasonsProvider.registerEclipticSeasonsProvider();
        }
    }

}