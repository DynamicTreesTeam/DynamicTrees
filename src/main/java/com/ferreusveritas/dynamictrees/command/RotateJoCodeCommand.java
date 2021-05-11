package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collections;

import static com.ferreusveritas.dynamictrees.command.CommandConstants.*;

public final class RotateJoCodeCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.ROTATE_JO_CODE;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

    @Override
    public ArgumentBuilder<CommandSource, ?> registerArguments() {
        return stringArgument(JO_CODE, Collections.singleton(DEFAULT_JO_CODE)).then(intArgument(TURNS).suggests(TURNS_SUGGESTIONS))
                .executes(context -> executesSuccess(() ->
                        this.rotateJoCode(context.getSource(), stringArgument(context, JO_CODE), intArgument(context, TURNS))));
    }

    private void rotateJoCode(final CommandSource source, final String code, final int turns) {
        sendSuccess(source, new TranslationTextComponent("commands.dynamictrees.success.rotate_jo_code",
                new JoCode(code).rotate(Direction.from2DDataValue((3 - (turns % 4)) + 3)).getTextComponent()));
    }

}
