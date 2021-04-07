package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.items.Staff;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.CommandHelper;
import com.ferreusveritas.dynamictrees.util.ItemUtils;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ferreusveritas.dynamictrees.command.CommandConstants.DEFAULT_JO_CODE;
import static com.ferreusveritas.dynamictrees.command.CommandConstants.JO_CODE;

public final class CreateStaffCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.CREATE_STAFF;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    private static final String COLOR = "color";
    private static final String READ_ONLY = "readOnly";
    private static final String MAX_USES = "maxUses";

    private static final String DEFAULT_COLOUR = "00FFFF";
    private static final boolean DEFAULT_READ_ONLY = false;
    private static final int DEFAULT_MAX_USES = 64;

    @Override
    public ArgumentBuilder<CommandSource, ?> registerArguments() {
        return blockPosArgument()
                .then(speciesArgument().executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context), speciesArgument(context),
                        DEFAULT_JO_CODE, DEFAULT_COLOUR, DEFAULT_READ_ONLY, DEFAULT_MAX_USES))
                        .then(stringArgument(JO_CODE).suggests(((context, builder) -> ISuggestionProvider.suggest(speciesArgument(context).getJoCodes()
                                .stream().map(JoCode::toString).collect(Collectors.toList()), builder)))
                                .executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                        stringArgument(context, JO_CODE), DEFAULT_COLOUR, DEFAULT_READ_ONLY, DEFAULT_MAX_USES))
                                .then(Commands.argument(COLOR, HexColorArgument.hex()).executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context),
                                        speciesArgument(context), stringArgument(context, JO_CODE), HexColorArgument.getHexString(context, COLOR), DEFAULT_READ_ONLY,
                                        DEFAULT_MAX_USES))
                                        .then(Commands.argument(READ_ONLY, BoolArgumentType.bool()).executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context),
                                                speciesArgument(context), stringArgument(context, JO_CODE), HexColorArgument.getHexString(context, COLOR),
                                                BoolArgumentType.getBool(context, READ_ONLY), DEFAULT_MAX_USES))
                                                .then(intArgument(MAX_USES).suggests(((context, builder) -> ISuggestionProvider.suggest(Stream.of(1, 3, 32, 64, 128).map(String::valueOf).collect(Collectors.toList()), builder)))
                                                        .executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                                                stringArgument(context, JO_CODE), HexColorArgument.getHexString(context, COLOR), BoolArgumentType.getBool(context, READ_ONLY),
                                                                intArgument(context, MAX_USES))))))));
    }

    private int spawnStaff(final CommandSource source, final BlockPos pos, final Species species, final String code, String colour, final boolean readOnly, final int maxUses) throws CommandSyntaxException {
        if (!colour.startsWith("#"))
            colour = "#" + colour;

        final Staff staff = DTRegistries.STAFF;

        final ItemStack wandStack = new ItemStack(staff, 1);

        staff.setSpecies(wandStack, species)
                .setCode(wandStack, code)
                .setColor(wandStack, colour)
                .setReadOnly(wandStack, readOnly)
                .setMaxUses(wandStack, maxUses)
                .setUses(wandStack, maxUses);

        ItemUtils.spawnItemStack(source.getLevel(), pos, wandStack, true);
        source.sendSuccess(new TranslationTextComponent("commands.dynamictrees.success.create_staff", species.getTextComponent(),
                new JoCode(code).getTextComponent(), aqua(colour), aqua(readOnly), aqua(maxUses), CommandHelper.posComponent(pos, TextFormatting.AQUA)),
                true);

        return 1;
    }

}
