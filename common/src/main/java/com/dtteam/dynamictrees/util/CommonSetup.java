package com.dtteam.dynamictrees.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Harley O'Connor
 */
public final class CommonSetup {

    private static final List<Runnable> SETUP_HANDLERS = new ArrayList<>();

    public static void runOnCommonSetup(Runnable handler) {
        SETUP_HANDLERS.add(handler);
    }

    public static void onCommonSetup() {
        SETUP_HANDLERS.forEach(Runnable::run);
    }

}
