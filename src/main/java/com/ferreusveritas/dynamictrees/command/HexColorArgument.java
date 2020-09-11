package com.ferreusveritas.dynamictrees.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TranslationTextComponent;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public final class HexColorArgument implements ArgumentType<String> {

    public static final DynamicCommandExceptionType COLOR_INVALID = new DynamicCommandExceptionType(colourString -> new TranslationTextComponent("argument.color.invalid", colourString));

    public static HexColorArgument hex () {
        return new HexColorArgument();
    }

    public static String getHexString(final CommandContext<?> context, final String name) {
        return context.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String in = reader.readString();

        try { Color.decode("#" + in).getRGB(); } catch (NumberFormatException e) {
            throw COLOR_INVALID.create(in);
        }

        return in;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ISuggestionProvider.suggest(Arrays.asList("00FFFF"), builder);
    }
}
