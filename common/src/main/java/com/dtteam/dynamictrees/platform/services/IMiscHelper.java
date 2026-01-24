package com.dtteam.dynamictrees.platform.services;

import com.dtteam.dynamictrees.worldgen.IDTBiomeHolderSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public interface IMiscHelper {

    boolean isLevelRestoringBlockSnapshots(Level level);

    MinecraftServer getCurrentServer();

    IDTBiomeHolderSet newDTBiomeHolderSet();

}
