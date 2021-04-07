package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
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

    @Override
    public ArgumentBuilder<CommandSource, ?> registerArguments() {
        return blockPosArgument().then(speciesArgument().executes(context -> this.setTree(context.getSource(), blockPosArgument(context),
                speciesArgument(context), JO_CODE, DEFAULT_TURNS))
                .then(stringArgument(JO_CODE).suggests(((context, builder) -> ISuggestionProvider.suggest(speciesArgument(context).getJoCodes()
                        .stream().map(JoCode::toString).collect(Collectors.toList()), builder)))
                        .executes(context -> this.setTree(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                stringArgument(context, JO_CODE), DEFAULT_TURNS))
                        .then(intArgument(TURNS).suggests(((context, builder) -> ISuggestionProvider.suggest(TURNS_SUGGESTIONS, builder)))
                                .executes(context -> this.setTree(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                stringArgument(context, JO_CODE), intArgument(context, TURNS))))));
    }

    private int setTree(final CommandSource source, final BlockPos pos, final Species species, final String codeString, final int turns) {
        final ServerWorld world = source.getLevel();
        final BlockPos rootPos = pos.relative(Direction.DOWN);
        final JoCode joCode = species.getJoCode(codeString).rotate(Direction.from2DDataValue((3 - (turns % 4)) + 3)).setCareful(true);

        source.sendSuccess(new TranslationTextComponent("commands.dynamictrees.success.set_tree", CommandHelper.posComponent(rootPos),
                species.getTextComponent(), joCode.getTextComponent()), true);
        joCode.generate(world, world, species, rootPos, source.getLevel().getBiome(pos),
                        Direction.SOUTH, 8, SafeChunkBounds.ANY);

        return 1;
    }

}
