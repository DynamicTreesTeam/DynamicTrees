package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.compat.SereneSeasonsSeasonProvider;
import com.dtteam.dynamictrees.compat.waila.WailaOther;
import com.dtteam.dynamictrees.platform.services.ICompatHelper;

public class NeoForgeCompatHelper implements ICompatHelper {

    @Override
    public void registerSereneSeasonsSeasonProvider() {
        SereneSeasonsSeasonProvider.registerSereneSeasonsProvider();
    }

    @Override
    public void invalidateWailaPosition() {
        WailaOther.invalidateWailaPosition();
    }
}