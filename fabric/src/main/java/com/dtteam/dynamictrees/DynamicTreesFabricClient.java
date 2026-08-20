package com.dtteam.dynamictrees;

import com.dtteam.dynamictrees.api.season.*;
import com.dtteam.dynamictrees.api.worldgen.*;
import com.dtteam.dynamictrees.block.leaves.*;
import com.dtteam.dynamictrees.block.sapling.*;
import com.dtteam.dynamictrees.client.*;
import com.dtteam.dynamictrees.config.*;
import com.dtteam.dynamictrees.entity.render.*;
import com.dtteam.dynamictrees.item.*;
import com.dtteam.dynamictrees.model.*;
import com.dtteam.dynamictrees.registry.*;
import com.dtteam.dynamictrees.systems.season.*;
import com.dtteam.dynamictrees.tree.family.*;
import com.dtteam.dynamictrees.tree.species.*;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.SpriteSourceRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.fml.config.ModConfig;

public class DynamicTreesFabricClient implements ClientModInitializer {

    private static boolean initialized = false;

    @Override
    public void onInitializeClient() {
        ConfigRegistry.INSTANCE.register(DynamicTrees.MOD_ID, ModConfig.Type.CLIENT, DTConfigs.CLIENT_CONFIG);
        SpriteSourceRegistry.register(ThickBranchRingsSource.ID, ThickBranchRingsSource.CODEC);
        com.dtteam.dynamictrees.compat.DeferredItemStacks.flush();
        FabricClientColors.register();
        registerModelLoaders();
        registerEntityRenderers();
        registerTooltipCallback();
        registerClientTick();
        registerClientWorldLoad();
    }

    private void registerClientWorldLoad() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (!initialized && client.level != null) {
                LeavesProperties.postInitClient();
                FabricClientColors.discoverWoodColors();
                BlockColorMultipliers.cleanUp();
                initialized = true;
            }
        });
    }

    private void registerModelLoaders() {
        ModelLoadingPlugin.register(new DTModelLoadingPlugin());
    }

    private void registerEntityRenderers() {
        EntityRendererRegistry.register(DTRegistries.FALLING_TREE.get(), FallingTreeRenderer::new);
        EntityRendererRegistry.register(DTRegistries.LINGERING_EFFECTOR.get(), LingeringEffectorRenderer::new);
    }

    private void registerTooltipCallback() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            Item item = stack.getItem();
            if (!(item instanceof Seed seed)) {
                return;
            }

            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }

            LevelContext levelContext = LevelContext.create(player.level());
            Species species = seed.getSpecies();
            if (!species.isValid()) {
                return;
            }
            if (SeasonHelper.getSeasonValue(levelContext, BlockPos.ZERO) == null) {
                return;
            }

            BlockPos playerPos = BlockPos.containing(player.position());
            ClimateZoneType climate = ClimateHelper.getClimate(player.level(), playerPos);
            int flags = seed.getSpecies().getSeasonalTooltipFlags(levelContext, player);
            Tooltips.applySeasonalTooltips(lines, flags, climate);
        });
    }

    private void registerClientTick() {
        ClientTickEvents.START_LEVEL_TICK.register(level -> SeasonHelper.updateTick(level, level.getOverworldClockTime()));
    }
}
