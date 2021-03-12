package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.ItemUtils;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;

public final class CreateStaffCommand extends SubCommand {

    public CreateStaffCommand() {
        this.takesCoordinates = true;
        this.executesWithCoordinates = false;
        this.defaultToExecute = false;

        // Add extra arguments.
        this.extraArguments = Commands.argument(CommandConstants.SPECIES_ARGUMENT, ResourceLocationArgument.resourceLocation()).suggests((context, builder) -> ISuggestionProvider.suggestIterable(Species.REGISTRY.getRegistryNames(), builder))
                .then(Commands.argument(CommandConstants.JO_CODE_ARGUMENT, StringArgumentType.string()).suggests((context, builder) -> ISuggestionProvider.suggest(Arrays.asList("JP"), builder))
                        .then(Commands.argument(CommandConstants.COLOR_ARGUMENT, HexColorArgument.hex())
                                .then(Commands.argument(CommandConstants.READ_ONLY_ARGUMENT, BoolArgumentType.bool())
                                        .then(Commands.argument(CommandConstants.MAX_USES_ARGUMENT, IntegerArgumentType.integer(1)).suggests((context, builder) -> ISuggestionProvider.suggest(Arrays.asList("64"), builder)).executes(this::execute)))));
    }

    @Override
    protected String getName() {
        return CommandConstants.CREATE_STAFF;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        BlockPos pos = this.getPositionArg(context);
        String colour = HexColorArgument.getHexString(context, CommandConstants.COLOR_ARGUMENT);
        final int maxUses = IntegerArgumentType.getInteger(context, CommandConstants.MAX_USES_ARGUMENT);
        final Species species = this.getSpeciesArg(context);

        // Ensure species given exists.
        if (!species.isValid()) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.error.unknownspecies", ResourceLocationArgument.getResourceLocation(context, CommandConstants.SPECIES_ARGUMENT)));
            return 0;
        }

        if (!colour.startsWith("#"))
            colour = "#" + colour;

        ItemStack wandStack =  new ItemStack(DTRegistries.treeStaff, 1);
        DTRegistries.treeStaff.setSpecies(wandStack, species)
                .setCode(wandStack, StringArgumentType.getString(context, CommandConstants.JO_CODE_ARGUMENT))
                .setColor(wandStack, colour)
                .setReadOnly(wandStack, BoolArgumentType.getBool(context, CommandConstants.READ_ONLY_ARGUMENT))
                .setMaxUses(wandStack, maxUses)
                .setUses(wandStack, maxUses);

        ItemUtils.spawnItemStack(context.getSource().getWorld(), pos, wandStack, true);

        return 1;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

}
