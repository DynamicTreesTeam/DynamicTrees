package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.platform.services.IConfigHelper;

public class FabricConfigHelper implements IConfigHelper {

    @Override
    public <T> T getConfig(String config, Class<T> tClass) {
        return null;
    }

    @Override
    public boolean isServerConfigLoaded() {
        return false;
    }

    @Override
    public boolean isCommonConfigLoaded() {
        return false;
    }

    @Override
    public boolean isClientConfigLoaded() {
        return false;
    }
}