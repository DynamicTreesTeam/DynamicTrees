package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.compat.SereneSeasonsSeasonProvider;
import com.dtteam.dynamictrees.platform.services.ICompatHelper;

public class FabricCompatHelper implements ICompatHelper {

    @Override
    public void registerSereneSeasonsSeasonProvider() {
        SereneSeasonsSeasonProvider.registerSereneSeasonsProvider();
    }
}