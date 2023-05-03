package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.ferreusveritas.dynamictrees.util.LevelContext;
import com.ferreusveritas.dynamictrees.util.Null;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.GenerationContext;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.stream.Collectors;

import static com.ferreusveritas.dynamictrees.command.CommandConstants.*;

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
        return blockPosArgument().then(speciesArgument().executes(context -> this.setTree(context.getSource(), blockPosArgument(context),
                        speciesArgument(context), JO_CODE, DEFAULT_TURNS, DEFAULT_FERTILITY))
                .then(stringArgument(JO_CODE).suggests(((context, builder) -> SharedSuggestionProvider.suggest(speciesArgument(context).getJoCodes()
                                .stream().map(JoCode::toString).collect(Collectors.toList()), builder)))
                        .executes(context -> this.setTree(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                stringArgument(context, JO_CODE), DEFAULT_TURNS, DEFAULT_FERTILITY))
                        .then(intArgument(TURNS).suggests(TURNS_SUGGESTIONS)
                                .executes(context -> this.setTree(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                        stringArgument(context, JO_CODE), intArgument(context, TURNS), DEFAULT_FERTILITY))
                                .then(intArgument(FERTILITY).suggests(FERTILITY_SUGGESTIONS)
                                        .executes(context -> this.setTree(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                                stringArgument(context, JO_CODE), intArgument(context, TURNS), intArgument(context, FERTILITY)))))));
    }

    private int setTree(final CommandSourceStack source, final BlockPos rootPos, final Species species, final String codeString, final int turns, final int fertility) {
        final ServerLevel level = source.getLevel();
        final JoCode joCode = species.getJoCode(codeString).rotate(Direction.from2DDataValue((3 - (turns % 4)) + 3)).setCareful(true);

        sendSuccessAndLog(source, Component.translatable("commands.dynamictrees.success.set_tree", CommandHelper.posComponent(rootPos),
                species.getTextComponent(), joCode.getTextComponent()));
        GenerationContext context = new GenerationContext(LevelContext.create(level), species, rootPos, rootPos.mutable(), source.getLevel().getBiome(rootPos), Direction.SOUTH, 8, SafeChunkBounds.ANY);
        joCode.generate(context);

        // Try to set the fertility.
        Null.consumeIfNonnull(TreeHelper.getRooty(level.getBlockState(rootPos)),
                rootyBlock -> rootyBlock.setFertility(level, rootPos, fertility));

        return 1;
    }

}
