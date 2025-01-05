package com.dtteam.dynamictrees.command;

import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.util.*;
import com.dtteam.dynamictrees.worldgen.DynamicTreeGenerationContext;
import com.dtteam.dynamictrees.worldgen.JoCode;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.stream.Collectors;

import static com.dtteam.dynamictrees.command.CommandConstants.*;

public final class SetTreeCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.SET_TREE;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    private static final int DEFAULT_FERTILITY = 0;

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return blockPosArgument()
                .then(speciesArgument()//Already suggests
                        .executes(context -> this.setTree(context.getSource(), blockPosArgument(context), speciesArgument(context), DEFAULT_JO_CODE, DEFAULT_TURNS, DEFAULT_FERTILITY))
                        .then(stringArgument(JO_CODE)
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(speciesArgument(context).getJoCodes().stream().map(JoCode::toString).collect(Collectors.toList()), builder))
                                .executes(context -> this.setTree(context.getSource(), blockPosArgument(context), speciesArgument(context), stringArgument(context, JO_CODE), DEFAULT_TURNS, DEFAULT_FERTILITY))
                                .then(intArgument(TURNS)
                                        //.suggests(TURNS_SUGGESTIONS)
                                        .executes(context -> this.setTree(context.getSource(), blockPosArgument(context), speciesArgument(context), stringArgument(context, JO_CODE), intArgument(context, TURNS), DEFAULT_FERTILITY))
                                        .then(intArgument(FERTILITY)
                                                .suggests(FERTILITY_SUGGESTIONS)
                                                .executes(context -> this.setTree(context.getSource(), blockPosArgument(context), speciesArgument(context), stringArgument(context, JO_CODE), intArgument(context, TURNS), intArgument(context, FERTILITY)))))
                                .then(stringArgument(ROOTS_JO_CODE)
                                        .suggests(((context, builder) -> SharedSuggestionProvider.suggest(speciesArgument(context).getRootsJoCodes().stream().map(JoCode::toString).collect(Collectors.toList()), builder)))
                                        .executes(context -> this.setTreeWithRoots(context.getSource(), blockPosArgument(context), speciesArgument(context), stringArgument(context, JO_CODE), stringArgument(context, ROOTS_JO_CODE), DEFAULT_TURNS, DEFAULT_FERTILITY))
                                        .then(intArgument(TURNS)
                                                .suggests(TURNS_SUGGESTIONS)
                                                .executes(context -> this.setTreeWithRoots(context.getSource(), blockPosArgument(context), speciesArgument(context), stringArgument(context, JO_CODE), stringArgument(context, ROOTS_JO_CODE), intArgument(context, TURNS), DEFAULT_FERTILITY))
                                                .then(intArgument(FERTILITY)
                                                        .suggests(FERTILITY_SUGGESTIONS)
                                                        .executes(context -> this.setTreeWithRoots(context.getSource(), blockPosArgument(context), speciesArgument(context), stringArgument(context, JO_CODE), stringArgument(context, ROOTS_JO_CODE), intArgument(context, TURNS), intArgument(context, FERTILITY))))))));
    }

    private int setTree(final CommandSourceStack source, final BlockPos rootPos, final Species species, final String codeString, final int turns, final int fertility) {
        final ServerLevel level = source.getLevel();
        final JoCode joCode = species.getJoCode(codeString).rotate(Direction.from2DDataValue((3 - (turns % 4)) + 3)).setCareful(true);

        sendSuccessAndLog(source, Component.translatable("commands.dynamictrees.success.set_tree", CommandHelper.posComponent(rootPos),
                species.getTextComponent(), joCode.getTextComponent()));
        DynamicTreeGenerationContext context = new DynamicTreeGenerationContext(LevelContext.create(level), species, rootPos, rootPos.mutable(), source.getLevel().getBiome(rootPos), Direction.SOUTH, 8, false);
        joCode.generate(context);

        // Try to set the fertility.
        Null.consumeIfNonnull(TreeHelper.getRooty(level.getBlockState(rootPos)),
                rootyBlock -> rootyBlock.setFertility(level, rootPos, fertility));

        return 1;
    }
    private int setTreeWithRoots(final CommandSourceStack source, final BlockPos rootPos, final Species species, final String codeString, final String rootsCodeString, final int turns, final int fertility) {
        final JoCode rootsJoCode = species.getRootsJoCode(rootsCodeString);
        if (!species.getFamily().hasRootSystem()) {
            sendFailure(source, Component.translatable("commands.dynamictrees.error.no_roots", species.getTextComponent(), rootsJoCode.getTextComponent()));
            return 0;
        }

        final ServerLevel level = source.getLevel();
        Direction dir = Direction.from2DDataValue((3 - (turns % 4)) + 3);
        final JoCode joCode = species.getJoCode(codeString).rotate(dir).setCareful(true);
        rootsJoCode.rotate(dir).setCareful(true);

        sendSuccessAndLog(source, Component.translatable("commands.dynamictrees.success.set_tree_roots", CommandHelper.posComponent(rootPos),
                species.getTextComponent(), joCode.getTextComponent(), rootsJoCode.getTextComponent()));
        DynamicTreeGenerationContext context = new DynamicTreeGenerationContext(LevelContext.create(level), species, rootPos, rootPos.mutable(), source.getLevel().getBiome(rootPos), Direction.SOUTH, 8, false);
        joCode.generate(context);
        rootsJoCode.generate(context);

        // Try to set the fertility.
        Null.consumeIfNonnull(TreeHelper.getRooty(level.getBlockState(rootPos)),
                rootyBlock -> rootyBlock.setFertility(level, rootPos, fertility));

        return 1;
    }

}
