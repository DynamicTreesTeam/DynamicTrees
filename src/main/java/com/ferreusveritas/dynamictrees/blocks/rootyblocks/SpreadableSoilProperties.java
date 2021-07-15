package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.init.DTClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author Max Hyper
 */
public class SpreadableSoilProperties extends SoilProperties{

    public static final TypedRegistry.EntryType<SoilProperties> TYPE = TypedRegistry.newType(SpreadableSoilProperties::new);

    public SpreadableSoilProperties (final ResourceLocation registryName){
        super(null, registryName);
    }

    @Override
    protected RootyBlock createDynamicSoil() {
        return new SpreadableRootyBlock(this);
    }

    public static class SpreadableRootyBlock extends RootyBlock {

        private Map<Block, SoilProperties> rootyBlocks;
        private Integer requiredLight;
        private Item spreadItem;

        public SpreadableRootyBlock(SoilProperties properties){
            super(properties);
        }

        public SpreadableRootyBlock(Block primitiveDirt, int requiredLight, Block ... spreadableBlocks) {
            this(primitiveDirt, requiredLight, null, spreadableBlocks);
        }
        public SpreadableRootyBlock(Block primitiveDirt, Item requiredItem, Block ... spreadableBlocks) {
            this(primitiveDirt, null, requiredItem, spreadableBlocks);
        }
        //A non-null required light will allow the blocks to spread on their own.
        //A non-null required item will allow the use of said item to spread the blocks.
        public SpreadableRootyBlock(Block primitiveDirt, @Nullable Integer requiredLight, @Nullable Item requiredItem, Block ... spreadableBlocks) {
            super(primitiveDirt);
            this.requiredLight = requiredLight;
            this.spreadItem = requiredItem;
            if (rootyBlocks == null) {
                rootyBlocks = new HashMap<>();
                for (Block block : spreadableBlocks) {
                    addSpreadableBlock(block);
                }
            }
        }

        @Nullable
        private RootyBlock getRootyBlock (Block block){
            if (rootyBlocks.get(block) == SoilProperties.NULL_PROPERTIES)
                rootyBlocks.put(block, SoilHelper.getProperties(block));
            return rootyBlocks.get(block).getDynamicSoilBlock();
        }

        public void addSpreadableBlock(Block primitiveDirt) {
            if (SoilHelper.isSoilRegistered(primitiveDirt))
                addSpreadableBlock(primitiveDirt, SoilHelper.getProperties(primitiveDirt));
            else {
                //System.err.println("Spreadable rooty dirt for "+primitiveDirt+" could not find rooty dirt for "+primitiveDirt+"! Make sure it is registered BEFORE adding it to this spreadable dirt.");
                addSpreadableBlock(primitiveDirt, SoilProperties.NULL_PROPERTIES);
            }
        }
        public void addSpreadableBlock(Block primitiveDirt, SoilProperties soilProperties) {
            rootyBlocks.put(primitiveDirt, soilProperties);
        }

        @Override
        public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
            if (spreadItem != null){
                ItemStack handStack = player.getItemInHand(handIn);
                if (handStack.getItem().equals(spreadItem)){
                    List<Block> foundBlocks = new LinkedList<>();

                    for(BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                        BlockState blockstate = worldIn.getBlockState(blockpos);
                        for (Block block : rootyBlocks.keySet()){
                            if (blockstate.is(block)) foundBlocks.add(block);
                        }
                    }
                    if (foundBlocks.size() > 0){
                        if (!worldIn.isClientSide()){
                            int blockInt = worldIn.random.nextInt(foundBlocks.size());
                            RootyBlock rootyBlock = getRootyBlock(foundBlocks.get(blockInt));
                            if (rootyBlock != null)
                                worldIn.setBlock(pos, rootyBlock.defaultBlockState(), 3);
                        }
                        if (!player.abilities.instabuild) {
                            handStack.shrink(1);
                        }
                        DTClient.spawnParticles(worldIn, ParticleTypes.HAPPY_VILLAGER, pos.above(),2 + worldIn.random.nextInt(5), worldIn.random);
                        return ActionResultType.SUCCESS;
                    }
                }
            }
            return super.use(state, worldIn, pos, player, handIn, hit);
        }

        @Override
        public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
            super.randomTick(state, world, pos, random);
            //this is a similar behaviour to vanilla grass spreading but inverted to be handled by the dirt block
            if (!world.isClientSide && requiredLight != null)
            {
                if (!world.isAreaLoaded(pos, 3)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light and spreading
                if (world.getMaxLocalRawBrightness(pos.above()) >= requiredLight)
                {
                    for (int i = 0; i < 4; ++i)
                    {
                        BlockPos thatPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);

                        if (thatPos.getY() >= 0 && thatPos.getY() < 256 && !world.hasChunkAt(thatPos)) return;

                        BlockState thatStateUp = world.getBlockState(thatPos.above());
                        BlockState thatState = world.getBlockState(thatPos);

                        for (Block key : rootyBlocks.keySet()){
                            RootyBlock block = getRootyBlock(key);
                            if (block != null && (thatState.getBlock() == key || thatState.getBlock() == block) && world.getMaxLocalRawBrightness(pos.above()) >= requiredLight && thatStateUp.getLightBlock(world, thatPos.above()) <= 2) {
                                if (state.hasProperty(FERTILITY))
                                    world.setBlockAndUpdate(pos, block.defaultBlockState().setValue(FERTILITY, state.getValue(FERTILITY)));
                                return;
                            }
                        }
                    }
                }
            }

        }

    }


}
