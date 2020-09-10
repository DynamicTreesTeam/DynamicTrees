package com.ferreusveritas.dynamictrees.command;

import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.worldgen.JoCode;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mcp.mobius.waila.overlay.RayTracing;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public final class GetTreeCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.GET_TREE;
    }

    @Override
    protected int execute(CommandContext<CommandSource> context) {
        if (context.getSource().getEntity() instanceof LivingEntity) {
            // Get block player is looking at.
            RayTracing.INSTANCE.fire();
            Vec3d target = RayTracing.INSTANCE.getTarget().getHitVec();

            this.getTree(context, context.getSource().getWorld(), new BlockPos(target.getX(), target.getY(), target.getZ()));
        } else {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.invalidargs"));
        }

        return 1;
    }

    private void getTree (CommandContext<CommandSource> context, ServerWorld world, BlockPos pos) {
        Species species = TreeHelper.getBestGuessSpecies(world, pos);

        if (species == Species.NULLSPECIES) {
            this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.failure"));
            return;
        }

        String code = TreeHelper.getJoCode(world, pos).map(JoCode::toString).orElse("?");

        this.sendMessage(context, new TranslationTextComponent("commands.dynamictrees.gettree.success", species.toString(), code));
    }

    @Override
    public ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal(this.getName())
                .then(Commands.argument("location", Vec3Argument.vec3()).executes(context -> {
                    this.getTree(context, context.getSource().getWorld(), Vec3Argument.getLocation(context, "location").getBlockPos(context.getSource())); return 1;
                })).executes(this::execute);
    }
}
