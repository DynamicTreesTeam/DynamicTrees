package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.tree.species.Species;
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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public abstract class SubCommand {

    protected static final DynamicCommandExceptionType NO_TREE_FOUND = new DynamicCommandExceptionType(pos -> Component.translatable("commands.dynamictrees.error.get_tree", Component.translatable("chat.coordinates", getVector3i(pos).getX(), getVector3i(pos).getY(), getVector3i(pos).getZ()).withStyle(style -> style.withColor(ChatFormatting.DARK_RED))));
    protected static final DynamicCommandExceptionType SPECIES_UNKNOWN = new DynamicCommandExceptionType(resLocStr -> Component.translatable("commands.dynamictrees.error.unknown_species", darkRed(resLocStr)));
    protected static final DynamicCommandExceptionType SPECIES_NOT_TRANSFORMABLE = new DynamicCommandExceptionType(nonTransformableSpecies -> Component.translatable("commands.dynamictrees.error.not_transformable", darkRed(nonTransformableSpecies)));

    private static Vec3i getVector3i(final Object vecObj) {
        if (vecObj instanceof Vec3i) {
            return ((Vec3i) vecObj);
        }
        return Vec3i.ZERO;
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

    public ArgumentBuilder<CommandSourceStack, ?> register() {
        final LiteralArgumentBuilder<CommandSourceStack> argumentBuilder = Commands.literal(this.getName())
                .requires(commandSource -> commandSource.hasPermission(this.getPermissionLevel()));

        this.registerArguments().forEach(argumentBuilder::then);
        return argumentBuilder;
    }

    protected List<ArgumentBuilder<CommandSourceStack, ?>> registerArguments() {
        return Lists.newArrayList(this.registerArgument());
    }

    /**
     * Registers the arguments for this implementation of {@link SubCommand}.
     *
     * @return The {@link ArgumentBuilder} created.
     */
    public abstract ArgumentBuilder<CommandSourceStack, ?> registerArgument();

    protected static int executesSuccess(final ThrowableRunnable<CommandSyntaxException> executeRunnable) throws CommandSyntaxException {
        executeRunnable.run();
        return 1;
    }

    protected static int executesSuccess(final CommandContext<CommandSourceStack> context, final Consumer<CommandContext<CommandSourceStack>> executeConsumer) {
        executeConsumer.accept(context);
        return 1;
    }

    protected static RequiredArgumentBuilder<CommandSourceStack, Boolean> booleanArgument(final String name) {
        return Commands.argument(name, BoolArgumentType.bool());
    }

    protected static boolean booleanArgument(final CommandContext<CommandSourceStack> context, final String name) {
        return BoolArgumentType.getBool(context, name);
    }

    protected static RequiredArgumentBuilder<CommandSourceStack, Integer> intArgument(final String name) {
        return Commands.argument(name, IntegerArgumentType.integer());
    }

    protected static int intArgument(final CommandContext<CommandSourceStack> context, final String name) {
        return IntegerArgumentType.getInteger(context, name);
    }

    protected static RequiredArgumentBuilder<CommandSourceStack, String> stringArgument(final String name) {
        return Commands.argument(name, StringArgumentType.string());
    }

    protected static RequiredArgumentBuilder<CommandSourceStack, String> stringArgument(final String name, final Collection<String> suggestions) {
        return Commands.argument(name, StringArgumentType.string()).suggests(((context, builder) -> SharedSuggestionProvider.suggest(suggestions, builder)));
    }

    protected static String stringArgument(final CommandContext<CommandSourceStack> context, final String name) {
        return StringArgumentType.getString(context, name);
    }

    protected static RequiredArgumentBuilder<CommandSourceStack, Coordinates> blockPosArgument() {
        return Commands.argument(CommandConstants.LOCATION, BlockPosArgument.blockPos());
    }

    protected static BlockPos blockPosArgument(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return BlockPosArgument.getLoadedBlockPos(context, CommandConstants.LOCATION);
    }

    protected static BlockPos rootPosArgument(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final BlockPos pos = blockPosArgument(context);
        final BlockPos rootPos = TreeHelper.findRootNode(context.getSource().getLevel(), pos);

        if (rootPos == BlockPos.ZERO) {
            throw NO_TREE_FOUND.create(pos);
        }

        return rootPos;
    }

    protected static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> speciesArgument() {
        return resourceLocationArgument(CommandConstants.SPECIES, Species.REGISTRY::getRegistryNames);
    }

    protected static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> transformableSpeciesArgument() {
        return resourceLocationArgument(CommandConstants.SPECIES, TreeRegistry::getTransformableSpeciesLocations);
    }

    protected static Species speciesArgument(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final ResourceLocation registryName = ResourceLocationArgument.getId(context, CommandConstants.SPECIES);
        final Species species = TreeRegistry.findSpecies(registryName);

        if (!species.isValid()) {
            throw SPECIES_UNKNOWN.create(registryName.toString());
        }

        return species;
    }

    protected static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> resourceLocationArgument(final String name, final Supplier<Collection<ResourceLocation>> suggestionsSupplier) {
        return Commands.argument(name, ResourceLocationArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(suggestionsSupplier.get(), builder));
    }

    protected static Component aqua(final Object object) {
        return CommandHelper.colour(object, ChatFormatting.AQUA);
    }

    protected static Component darkRed(final Object object) {
        return CommandHelper.colour(object, ChatFormatting.DARK_RED);
    }

    protected static void sendSuccess(final CommandSourceStack source, final Component component) {
        source.sendSuccess(component.copy().withStyle(style -> style.withColor(ChatFormatting.GREEN)),
                false);
    }

    protected static void sendSuccessAndLog(final CommandSourceStack source, final Component component) {
        source.sendSuccess(component.copy().withStyle(style -> style.withColor(ChatFormatting.GREEN)),
                true);
    }

    protected static void sendFailure(final CommandSourceStack source, final Component component) {
        source.sendFailure(component.copy().withStyle(style -> style.withColor(ChatFormatting.RED)));
    }

}
