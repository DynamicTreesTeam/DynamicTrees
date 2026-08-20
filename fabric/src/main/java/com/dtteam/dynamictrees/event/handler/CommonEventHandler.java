package com.dtteam.dynamictrees.event.handler;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import com.dtteam.dynamictrees.block.sapling.PottedSaplingBlock;
import com.dtteam.dynamictrees.block.soil.SoilBlock;
import com.dtteam.dynamictrees.command.DTCommand;
import com.dtteam.dynamictrees.systems.FutureBreak;
import com.dtteam.dynamictrees.systems.season.SeasonCompatibilityHandler;
import com.dtteam.dynamictrees.systems.season.SeasonHelper;
import com.dtteam.dynamictrees.treepack.Resources;
import com.dtteam.dynamictrees.worldgen.BiomeDatabases;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class CommonEventHandler {

    public static void RegisterEvents(){

        ServerTickEvents.START_LEVEL_TICK.register((level)->{
            FutureBreak.process(level);
            SeasonHelper.updateTick(level, level.getOverworldClockTime());
        });

        ServerLevelEvents.LOAD.register(((minecraftServer, serverLevel) -> {
            BiomeDatabases.populateBlacklistFromConfig();
        }));

        ServerLevelEvents.UNLOAD.register(((minecraftServer, serverLevel) -> {
            DynamicTreeFeature.DISC_PROVIDER.unloadWorld(serverLevel);
        }));


        ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer -> {
            SeasonCompatibilityHandler.getSeasonManager().flushMappings();
        }));
        
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandBuildContext, commandSelection) -> {
            new DTCommand().registerDTCommand(commandDispatcher);
        }));

        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            Block block = state.getBlock();
            if (block instanceof BranchBlock branchBlock) {
                return branchBlock.onDestroyedByPlayer(state, level, pos, player, true, level.getFluidState(pos));
            } else if (block instanceof TrunkShellBlock trunkShellBlock) {
                return trunkShellBlock.onDestroyedByPlayer(state, level, pos, player, true, level.getFluidState(pos));
            } else if (block instanceof SoilBlock soilBlock) {
                return soilBlock.onDestroyedByPlayer(state, level, pos, player, true, level.getFluidState(pos));
            } else if (block instanceof PottedSaplingBlock pottedSaplingBlock) {
                return pottedSaplingBlock.onDestroyedByPlayer(state, level, pos, player, true, level.getFluidState(pos));
            }
            return true;
        });

        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FabricReloadListener());

    }

    public static final class FabricReloadListener extends Resources.ReloadListener implements IdentifiableResourceReloadListener{

        public FabricReloadListener() {
            super(null);
        }

        @Override
        public CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor prepareExecutor,
                                              PreparationBarrier stage, Executor applyExecutor) {
            com.dtteam.dynamictrees.compat.DeferredItemStacks.flush();
            return super.reload(sharedState, prepareExecutor, stage, applyExecutor);
        }

        @Override
        public Identifier getFabricId() {
            return DynamicTrees.location(DynamicTrees.MOD_ID);
        }
    }

}
