package com.dtteam.dynamictrees.platform;

import com.dtteam.dynamictrees.platform.services.IMiscHelper;
import com.dtteam.dynamictrees.worldgen.IDTBiomeHolderSet;
import com.dtteam.dynamictrees.worldgen.holderset.DTBiomeHolderSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class NeoForgeMiscHelper implements IMiscHelper {


    @Override
    public boolean isLevelRestoringBlockSnapshots(Level level) {
        return level.restoringBlockSnapshots;
    }

    @Override
    public MinecraftServer getCurrentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public IDTBiomeHolderSet newDTBiomeHolderSet() {
        return new DTBiomeHolderSet();
    }

}