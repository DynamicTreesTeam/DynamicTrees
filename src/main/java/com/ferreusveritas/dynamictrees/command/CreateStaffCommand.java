package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.Arrays;

public final class CreateStaffCommand extends SubCommand {

    public CreateStaffCommand() {
        this.takesCoordinates = true;

        // Add extra arguments.
        this.extraArguments = Commands.argument(CommandConstants.SPECIES_ARGUMENT, ResourceLocationArgument.resourceLocation()).suggests((context, builder) -> ISuggestionProvider.suggestIterable(Species.REGISTRY.getKeys(), builder))
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
        try {
            BlockPos pos = Vec3Argument.getLocation(context, CommandConstants.LOCATION_ARGUMENT).getBlockPos(context.getSource());
            String colour = HexColorArgument.getHexString(context, CommandConstants.COLOR_ARGUMENT);
            int maxUses = IntegerArgumentType.getInteger(context, CommandConstants.MAX_USES_ARGUMENT);

            ItemStack wandStack =  new ItemStack(DTRegistries.treeStaff, 1);
            DTRegistries.treeStaff.setSpecies(wandStack, TreeRegistry.findSpecies(ResourceLocationArgument.getResourceLocation(context, CommandConstants.SPECIES_ARGUMENT)))
                    .setCode(wandStack, StringArgumentType.getString(context, CommandConstants.JO_CODE_ARGUMENT))
                    .setColor(wandStack, colour) // TODO: Fix problem with setting colours. They are currently not set whenever a letter is included in the hex code (...?).
                    .setReadOnly(wandStack, BoolArgumentType.getBool(context, CommandConstants.READ_ONLY_ARGUMENT))
                    .setMaxUses(wandStack, maxUses)
                    .setUses(wandStack, maxUses);

            while(!context.getSource().getWorld().isAirBlock(pos)) pos = pos.up();

            World world = context.getSource().getWorld();
            ItemEntity entityItem = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, wandStack);
            entityItem.setMotion(0, 0, 0);
            world.addEntity(entityItem);
        } catch (IllegalArgumentException e) {
            this.sendMessage(context, new StringTextComponent("Hey! That's not all the args!"));
            return 0;
        }

        return 1;
    }

}
