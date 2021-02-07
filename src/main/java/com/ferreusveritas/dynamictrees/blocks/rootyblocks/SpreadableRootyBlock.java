package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.init.DTClient;
import com.ferreusveritas.dynamictrees.systems.RootyBlockHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

/**
 * @author Max Hyper
 */
public class SpreadableRootyBlock extends RootyBlock {

    private Map<Block, RootyBlock> rootyBlocks;
    private Integer requiredLight;
    private Item spreadItem;
    //A non-null required light will allow the blocks to spread on their own.
    //A non-null required item will allow the use of said item to spread the blocks.
    public SpreadableRootyBlock(Block primitiveDirt, Integer requiredLight, Item requiredItem, Block ... spreadableBlocks) {
        super(primitiveDirt);
        this.requiredLight = requiredLight;
        this.spreadItem = requiredItem;
        if (rootyBlocks == null){
            rootyBlocks = new HashMap<>();
            for (Block block : spreadableBlocks){
                if (RootyBlockHelper.isBlockRegistered(block))
                    rootyBlocks.put(block, RootyBlockHelper.getRootyBlock(block));
                else
                    System.err.println("Spreadable rooty dirt for "+primitiveDirt+" could not find rooty dirt for "+block+"! Make sure it is registered BEFORE this one.");
            }
        }
    }
    public SpreadableRootyBlock(Block primitiveDirt, int requiredLight, Block ... spreadableBlocks) {
        this(primitiveDirt, requiredLight, null, spreadableBlocks);
    }
    public SpreadableRootyBlock(Block primitiveDirt, Item requiredItem, Block ... spreadableBlocks) {
        this(primitiveDirt, null, requiredItem, spreadableBlocks);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (spreadItem != null){
            ItemStack handStack = player.getHeldItem(handIn);
            if (handStack.getItem().equals(spreadItem)){
                List<Block> foundBlocks = new LinkedList<>();

                for(BlockPos blockpos : BlockPos.getAllInBoxMutable(pos.add(-1, -1, -1), pos.add(1, 1, 1))) {
                    BlockState blockstate = worldIn.getBlockState(blockpos);
                    for (Block block : rootyBlocks.keySet()){
                        if (blockstate.isIn(block)) foundBlocks.add(block);
                    }
                }
                if (foundBlocks.size() > 0){
                    if (!worldIn.isRemote()){
                        int blockInt = worldIn.rand.nextInt(foundBlocks.size());
                        worldIn.setBlockState(pos, rootyBlocks.get(foundBlocks.get(blockInt)).getDefaultState(), 3);
                    }
                    if (!player.isCreative()) handStack.shrink(1);
                    DTClient.spawnParticles(worldIn, ParticleTypes.HAPPY_VILLAGER, pos.up(),2 + worldIn.rand.nextInt(5), worldIn.rand);
                    return ActionResultType.SUCCESS;
                }
            }
        }
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        //this is a similar behaviour to vanilla grass spreading but inverted to be handled by the dirt block
        if (!world.isRemote && requiredLight != null)
        {
            if (!world.isAreaLoaded(pos, 3)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light and spreading
            if (world.getLight(pos.up()) >= requiredLight)
            {
                for (int i = 0; i < 4; ++i)
                {
                    BlockPos thatPos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);

                    if (thatPos.getY() >= 0 && thatPos.getY() < 256 && !world.isBlockLoaded(thatPos)) return;

                    BlockState thatStateUp = world.getBlockState(thatPos.up());
                    BlockState thatState = world.getBlockState(thatPos);

                    for (Map.Entry<Block, RootyBlock> entry : rootyBlocks.entrySet()){
                        if ((thatState.getBlock() == entry.getKey() || thatState.getBlock() == entry.getValue()) && world.getLight(pos.up()) >= requiredLight && thatStateUp.getOpacity(world, thatPos.up()) <= 2) {
                            world.setBlockState(pos, entry.getValue().getDefaultState().with(FERTILITY, world.getBlockState(pos).get(FERTILITY)));
                            return;
                        }
                    }
                }
            }
        }

    }

}
