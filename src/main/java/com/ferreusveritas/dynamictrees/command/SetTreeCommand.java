package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.ferreusveritas.dynamictrees.util.Null;
import com.ferreusveritas.dynamictrees.util.SafeChunkBounds;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

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
    public ArgumentBuilder<CommandSource, ?> registerArgument() {
        return blockPosArgument().then(speciesArgument().executes(context -> this.setTree(context.getSource(), blockPosArgument(context),
                speciesArgument(context), JO_CODE, DEFAULT_TURNS, DEFAULT_FERTILITY))
                .then(stringArgument(JO_CODE).suggests(((context, builder) -> ISuggestionProvider.suggest(speciesArgument(context).getJoCodes()
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

    private int setTree(final CommandSource source, final BlockPos rootPos, final Species species, final String codeString, final int turns, final int fertility) {
        final ServerWorld world = source.getLevel();
        final JoCode joCode = species.getJoCode(codeString).rotate(Direction.from2DDataValue((3 - (turns % 4)) + 3)).setCareful(true);

        sendSuccessAndLog(source, new TranslationTextComponent("commands.dynamictrees.success.set_tree", CommandHelper.posComponent(rootPos),
                species.getTextComponent(), joCode.getTextComponent()));
        joCode.generate(world, world, species, rootPos, source.getLevel().getBiome(rootPos),
                        Direction.SOUTH, 8, SafeChunkBounds.ANY);

        // Try to set the fertility.
        Null.consumeIfNonnull(TreeHelper.getRooty(world.getBlockState(rootPos)),
                rootyBlock -> rootyBlock.setFertility(world, rootPos, fertility));

        return 1;
    }

}
