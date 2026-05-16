package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.AltBranchFamily;
import com.dtteam.dynamictrees.tree.family.CreakingHeartFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ResinBranchBlock extends ThickBranchBlock {

    public ResinBranchBlock(Identifier name, Properties properties) {
        super(name, properties);
    }

    @Override
    public Optional<Block> getPrimitiveLog() {
        if (getFamily() instanceof AltBranchFamily altLogFamily)
            return altLogFamily.getPrimitiveAltLog();
        return super.getPrimitiveLog();
    }
    @Override
    public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
        return ((CreakingHeartFamily)getFamily()).getHeartHardness(state, level, pos, super.getHardness(state, level, pos));
    }

    public void removeResin(BlockState state, Level level, BlockPos pos, @Nullable Player player){
        CreakingHeartFamily family = ((CreakingHeartFamily)getFamily());

        int currentRadius = TreeHelper.getRadius(state);
        family.getBranch().get().setRadius(level, pos, currentRadius, null, 3);

        ItemStack resin = getResinStack(level.getRandom(), family);
        if (player != null)
            player.addItem(resin);
        else
            popResource(level, pos, resin);

        level.playSound(null, pos, SoundEvents.RESIN_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
    }


    private static ItemStack getResinStack(RandomSource random, CreakingHeartFamily family) {
        return new ItemStack(family.getResinItem(), random.nextIntBetweenInclusive(2, 3));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        removeResin(state, level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.AIR)){
            removeResin(state, level, pos, player);
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        removeResin(state, level, pos, player);
    }
}