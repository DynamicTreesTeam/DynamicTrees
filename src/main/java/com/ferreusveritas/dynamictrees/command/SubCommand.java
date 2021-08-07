package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.ferreusveritas.dynamictrees.util.ThrowableRunnable;
import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public abstract class SubCommand {

    protected static final DynamicCommandExceptionType NO_TREE_FOUND = new DynamicCommandExceptionType(pos -> new TranslationTextComponent("commands.dynamictrees.error.get_tree", new TranslationTextComponent("chat.coordinates", getVector3i(pos).getX(), getVector3i(pos).getY(), getVector3i(pos).getZ()).withStyle(style -> style.withColor(TextFormatting.DARK_RED))));
    protected static final DynamicCommandExceptionType SPECIES_UNKNOWN = new DynamicCommandExceptionType(resLocStr -> new TranslationTextComponent("commands.dynamictrees.error.unknown_species", darkRed(resLocStr)));
    protected static final DynamicCommandExceptionType SPECIES_NOT_TRANSFORMABLE = new DynamicCommandExceptionType(nonTransformableSpecies -> new TranslationTextComponent("commands.dynamictrees.error.not_transformable", darkRed(nonTransformableSpecies)));

    private static Vector3i getVector3i(final Object vecObj) {
        if (vecObj instanceof Vector3i) {
            return ((Vector3i) vecObj);
        }
        return Vector3i.ZERO;
    }

    /**
     * Returns the name of the command.
     *
     * @return - Name of command.
     */
    protected abstract String getName();

    /**
     * Returns the permission level required to use the command.
     *
     * @return Permission level required.
     */
    protected abstract int getPermissionLevel();

    public ArgumentBuilder<CommandSource, ?> register() {
        final LiteralArgumentBuilder<CommandSource> argumentBuilder = Commands.literal(this.getName())
                .requires(commandSource -> commandSource.hasPermission(this.getPermissionLevel()));

        this.registerArguments().forEach(argumentBuilder::then);
        return argumentBuilder;
    }

    protected List<ArgumentBuilder<CommandSource, ?>> registerArguments() {
        return Lists.newArrayList(this.registerArgument());
    }

    /**
     * Registers the arguments for this implementation of {@link SubCommand}.
     *
     * @return The {@link ArgumentBuilder} created.
     */
    public abstract ArgumentBuilder<CommandSource, ?> registerArgument();

    protected static int executesSuccess(final ThrowableRunnable<CommandSyntaxException> executeRunnable) throws CommandSyntaxException {
        executeRunnable.run();
        return 1;
    }

    protected static int executesSuccess(final CommandContext<CommandSource> context, final Consumer<CommandContext<CommandSource>> executeConsumer) {
        executeConsumer.accept(context);
        return 1;
    }

    protected static RequiredArgumentBuilder<CommandSource, Boolean> booleanArgument(final String name) {
        return Commands.argument(name, BoolArgumentType.bool());
    }

    protected static boolean booleanArgument(final CommandContext<CommandSource> context, final String name) {
        return BoolArgumentType.getBool(context, name);
    }

    protected static RequiredArgumentBuilder<CommandSource, Integer> intArgument(final String name) {
        return Commands.argument(name, IntegerArgumentType.integer());
    }

    protected static int intArgument(final CommandContext<CommandSource> context, final String name) {
        return IntegerArgumentType.getInteger(context, name);
    }

    protected static RequiredArgumentBuilder<CommandSource, String> stringArgument(final String name) {
        return Commands.argument(name, StringArgumentType.string());
    }

    protected static RequiredArgumentBuilder<CommandSource, String> stringArgument(final String name, final Collection<String> suggestions) {
        return Commands.argument(name, StringArgumentType.string()).suggests(((context, builder) -> ISuggestionProvider.suggest(suggestions, builder)));
    }

    protected static String stringArgument(final CommandContext<CommandSource> context, final String name) {
        return StringArgumentType.getString(context, name);
    }

    protected static RequiredArgumentBuilder<CommandSource, ILocationArgument> blockPosArgument() {
        return Commands.argument(CommandConstants.LOCATION, BlockPosArgument.blockPos());
    }

    protected static BlockPos blockPosArgument(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        return BlockPosArgument.getLoadedBlockPos(context, CommandConstants.LOCATION);
    }

    protected static BlockPos rootPosArgument(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        final BlockPos pos = blockPosArgument(context);
        final BlockPos rootPos = TreeHelper.findRootNode(context.getSource().getLevel(), pos);

        if (rootPos == BlockPos.ZERO) {
            throw NO_TREE_FOUND.create(pos);
        }

        return rootPos;
    }

    protected static RequiredArgumentBuilder<CommandSource, ResourceLocation> speciesArgument() {
        return resourceLocationArgument(CommandConstants.SPECIES, Species.REGISTRY::getRegistryNames);
    }

    protected static RequiredArgumentBuilder<CommandSource, ResourceLocation> transformableSpeciesArgument() {
        return resourceLocationArgument(CommandConstants.SPECIES, TreeRegistry::getTransformableSpeciesLocations);
    }

    protected static Species speciesArgument(final CommandContext<CommandSource> context) throws CommandSyntaxException {
        final ResourceLocation registryName = ResourceLocationArgument.getId(context, CommandConstants.SPECIES);
        final Species species = TreeRegistry.findSpecies(registryName);

        if (!species.isValid()) {
            throw SPECIES_UNKNOWN.create(registryName.toString());
        }

        return species;
    }

    protected static RequiredArgumentBuilder<CommandSource, ResourceLocation> resourceLocationArgument(final String name, final Supplier<Collection<ResourceLocation>> suggestionsSupplier) {
        return Commands.argument(name, ResourceLocationArgument.id())
                .suggests((context, builder) -> ISuggestionProvider.suggestResource(suggestionsSupplier.get(), builder));
    }

    protected static ITextComponent aqua(final Object object) {
        return CommandHelper.colour(object, TextFormatting.AQUA);
    }

    protected static ITextComponent darkRed(final Object object) {
        return CommandHelper.colour(object, TextFormatting.DARK_RED);
    }

    protected static void sendSuccess(final CommandSource source, final ITextComponent component) {
        source.sendSuccess(component.copy().withStyle(style -> style.withColor(TextFormatting.GREEN)),
                false);
    }

    protected static void sendSuccessAndLog(final CommandSource source, final ITextComponent component) {
        source.sendSuccess(component.copy().withStyle(style -> style.withColor(TextFormatting.GREEN)),
                true);
    }

    protected static void sendFailure(final CommandSource source, final ITextComponent component) {
        source.sendFailure(component.copy().withStyle(style -> style.withColor(TextFormatting.RED)));
    }

}
