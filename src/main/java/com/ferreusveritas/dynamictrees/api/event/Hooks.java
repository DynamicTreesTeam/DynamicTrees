package com.ferreusveritas.dynamictrees.api.event;

import com.ferreusveritas.dynamictrees.api.resource.TreeResourceManager;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoader;

/**
 * @author Harley O'Connor
 */
public final class Hooks {

    public static void onAddResourceLoaders(TreeResourceManager resourceManager) {
        ModLoader.get().postEvent(new AddResourceLoadersEvent(resourceManager));
    }

    public static boolean onTransitionSaplingToTree(Species species, Level level, BlockPos pos) {
        return MinecraftForge.EVENT_BUS.post(new TransitionSaplingToTreeEvent(species, level, pos));
    }

}
