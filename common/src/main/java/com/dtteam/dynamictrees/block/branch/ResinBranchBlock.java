package com.dtteam.dynamictrees.block.branch;

import com.dtteam.dynamictrees.data.DTLootTableBuilder;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.family.AltBranchFamily;
import com.dtteam.dynamictrees.tree.family.CreakingHeartFamily;
import com.dtteam.dynamictrees.tree.family.MossyAerialRootsFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootTable;
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

    public void removeResin(BlockState state, Level level, BlockPos pos, @Nullable Player player){
        CreakingHeartFamily family = ((CreakingHeartFamily)getFamily());

        int currentRadius = TreeHelper.getRadius(state);
        family.getBranch().get().setRadius(level, pos, currentRadius, null, 3);

        ItemStack resin = getResinStack(level.getRandom(), family, currentRadius);
        if (player != null && !player.isCreative())
            player.addItem(resin);
        else
            popResource(level, pos, resin);

        level.playSound(null, pos, SoundEvents.RESIN_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
    }


    private static ItemStack getResinStack(RandomSource random, CreakingHeartFamily family, int radius) {
        int count = Math.max(1, Math.round(random.nextIntBetweenInclusive(2, 3) * (radius/8f)));
        return new ItemStack(family.getResinItem(), count);
    }

    @Override
    public LootTable.Builder createBranchDrops(HolderLookup.Provider registries) {
        return DTLootTableBuilder.createResinBranchDrops(getPrimitiveLog().get(),
                getFamily().getStick(), ((CreakingHeartFamily)getFamily()).getResinItem(), 2, 3, registries);
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
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack toolStack, boolean willHarvest, FluidState fluid) {
        removeResin(state, level, pos, player);
        return false;
    }

    @Override
    public float getHardness(BlockState state, BlockGetter level, BlockPos pos) {
        if (getFamily() instanceof CreakingHeartFamily heartFamily) {
            return heartFamily.getResinBlock().defaultBlockState().getDestroySpeed(level, pos);
        }
        return super.getHardness(state, level, pos);
    }

}