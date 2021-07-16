package com.ferreusveritas.dynamictrees.blocks.rootyblocks;

import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.init.DTClient;
import com.ferreusveritas.dynamictrees.util.json.ListGetter;
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

    public static final ConfigurationProperty<Integer> REQUIRED_LIGHT = ConfigurationProperty.integer("required_light");
    public static final ConfigurationProperty<Item> SPREAD_ITEM = ConfigurationProperty.item("spread_item");
    public static final ConfigurationProperty<List<SoilProperties>> SPREADABLE_SOILS = ConfigurationProperty.property("spreadable_soils", ListGetter.getListClass(SoilProperties.class));


    public SpreadableSoilProperties (final ResourceLocation registryName){
        super(null, registryName);
    }

    @Override
    protected void registerProperties() {
        register(REQUIRED_LIGHT, SPREAD_ITEM, SPREADABLE_SOILS);
    }

    @Override
    protected ConfiguredSoilProperties<SoilProperties> createDefaultConfiguration() {
        return super.createDefaultConfiguration().with(REQUIRED_LIGHT, null).with(SPREAD_ITEM, null).with(SPREADABLE_SOILS, new LinkedList<>());
    }

    @Override
    protected RootyBlock createDynamicSoil() {
        return new SpreadableRootyBlock(getPrimitiveSoilBlock());
    }

    public static class SpreadableRootyBlock extends RootyBlock {

        public SpreadableRootyBlock(Block primitiveSoil){
            super(primitiveSoil);
        }

        @Nullable
        private RootyBlock getRootyBlock (Block block){
            return SoilHelper.getConfiguredProperties(block).getConfigurable().getDynamicSoilBlock();
        }

        @Override
        public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
            if (getConfiguration().get(SPREAD_ITEM) != null){
                ItemStack handStack = player.getItemInHand(handIn);
                if (handStack.getItem().equals(getConfiguration().get(SPREAD_ITEM))){
                    List<Block> foundBlocks = new LinkedList<>();

                    for(BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                        Block block = worldIn.getBlockState(blockpos).getBlock();
                        if (getConfiguration().get(SPREADABLE_SOILS).stream().anyMatch(prop->prop.getPrimitiveSoilBlock()==block))
                            foundBlocks.add(block);
                    }
                    if (foundBlocks.size() > 0){
                        if (!worldIn.isClientSide()){
                            int blockInt = worldIn.random.nextInt(foundBlocks.size());
                            RootyBlock rootyBlock = getRootyBlock(foundBlocks.get(blockInt));
                            if (rootyBlock != null)
                                worldIn.setBlock(pos, rootyBlock.defaultBlockState(), 3);
                        }
                        if (!player.isCreative()) {
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
            if (!world.isClientSide && getConfiguration().get(REQUIRED_LIGHT) != null)
            {
                if (!world.isAreaLoaded(pos, 3)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light and spreading
                if (world.getMaxLocalRawBrightness(pos.above()) >= getConfiguration().get(REQUIRED_LIGHT))
                {
                    for (int i = 0; i < 4; ++i)
                    {
                        BlockPos thatPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);

                        if (thatPos.getY() >= 0 && thatPos.getY() < 256 && !world.hasChunkAt(thatPos)) return;

                        BlockState thatStateUp = world.getBlockState(thatPos.above());
                        BlockState thatState = world.getBlockState(thatPos);

                        for (SoilProperties properties : getConfiguration().get(SPREADABLE_SOILS)){
                            RootyBlock block = properties.getDynamicSoilBlock();
                            if (block != null && (thatState.getBlock() == properties.getPrimitiveSoilBlock() || thatState.getBlock() == block) && world.getMaxLocalRawBrightness(pos.above()) >= getConfiguration().get(REQUIRED_LIGHT) && thatStateUp.getLightBlock(world, thatPos.above()) <= 2) {
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
