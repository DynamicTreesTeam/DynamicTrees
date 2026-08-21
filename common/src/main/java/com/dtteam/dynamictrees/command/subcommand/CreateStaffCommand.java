package com.dtteam.dynamictrees.command.subcommand;

import com.dtteam.dynamictrees.command.CommandConstants;
import com.dtteam.dynamictrees.command.CommandHelper;
import com.dtteam.dynamictrees.command.HexColorArgument;
import com.dtteam.dynamictrees.item.Staff;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.utility.ItemUtils;
import com.dtteam.dynamictrees.worldgen.JoCode;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.dtteam.dynamictrees.command.CommandConstants.DEFAULT_JO_CODE;
import static com.dtteam.dynamictrees.command.CommandConstants.JO_CODE;

public final class CreateStaffCommand extends SubCommand {

    protected String getName() {
        return CommandConstants.CREATE_STAFF;
    }

    protected int getPermissionLevel() {
        return 2;
    }

    private static final String COLOR = "color";
    private static final String READ_ONLY = "readOnly";
    private static final String MAX_USES = "maxUses";

    private static final int DEFAULT_COLOUR = 0x00FFFF;
    private static final boolean DEFAULT_READ_ONLY = false;
    private static final int DEFAULT_MAX_USES = 64;

    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return blockPosArgument()
                .then(speciesArgument().executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                DEFAULT_JO_CODE, DEFAULT_COLOUR, DEFAULT_READ_ONLY, DEFAULT_MAX_USES))
                        .then(stringArgument(JO_CODE).suggests(((context, builder) -> SharedSuggestionProvider.suggest(speciesArgument(context).getJoCodes()
                                        .stream().map(JoCode::toString).collect(Collectors.toList()), builder)))
                                .executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                        stringArgument(context, JO_CODE), DEFAULT_COLOUR, DEFAULT_READ_ONLY, DEFAULT_MAX_USES))
                                .then(Commands.argument(COLOR, HexColorArgument.hex()).executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context),
                                                speciesArgument(context), stringArgument(context, JO_CODE), HexColorArgument.getHexCode(context, COLOR), DEFAULT_READ_ONLY,
                                                DEFAULT_MAX_USES))
                                        .then(Commands.argument(READ_ONLY, BoolArgumentType.bool()).executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context),
                                                        speciesArgument(context), stringArgument(context, JO_CODE), HexColorArgument.getHexCode(context, COLOR),
                                                        BoolArgumentType.getBool(context, READ_ONLY), DEFAULT_MAX_USES))
                                                .then(intArgument(MAX_USES).suggests(((context, builder) -> SharedSuggestionProvider.suggest(Stream.of(1, 3, 32, 64, 128).map(String::valueOf).collect(Collectors.toList()), builder)))
                                                        .executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                                                stringArgument(context, JO_CODE), HexColorArgument.getHexCode(context, COLOR), BoolArgumentType.getBool(context, READ_ONLY),
                                                                intArgument(context, MAX_USES))))))));
    }

    private int spawnStaff(final CommandSourceStack source, final BlockPos pos, final Species species, final String code, final int colour, final boolean readOnly, final int maxDamage) {
        final Staff staff = DTRegistries.STAFF.get();

        final ItemStack wandStack = new ItemStack(staff, 1);

        staff.setSpecies(wandStack, species);
                staff.setCode(wandStack, code);
                staff.setColor(wandStack, colour);
                staff.setReadOnly(wandStack, readOnly);
                staff.setMaxDamage(wandStack, maxDamage);
                staff.setDamage(wandStack, 0);

        ItemUtils.spawnItemStack(source.getLevel(), pos, wandStack, true);

        sendSuccessAndLog(source, Component.translatable("commands.dynamictrees.success.create_staff", species.getTextComponent(),
                new JoCode(code).getTextComponent(), aqua(String.format("#%08X", colour)), aqua(readOnly), aqua(maxDamage), CommandHelper.posComponent(pos, ChatFormatting.AQUA)));

        return 1;
    }

}
