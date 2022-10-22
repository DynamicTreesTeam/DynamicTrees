package com.ferreusveritas.dynamictrees.block.rooty;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import com.ferreusveritas.dynamictrees.init.DTClient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;

/**
 * @author Max Hyper
 */
public class SpreadableSoilProperties extends SoilProperties {

    public static final TypedRegistry.EntryType<SoilProperties> TYPE = TypedRegistry.newType(SpreadableSoilProperties::new);

    private Integer required_light = null;
    private Item spread_item = null;
    private final List<SoilProperties> spreadable_soils = new LinkedList<>();

    public void setRequiredLight(Integer light) {
        this.required_light = light;
    }

    public void setSpreadItem(Item item) {
        this.spread_item = item;
    }

    public SpreadableSoilProperties(final ResourceLocation registryName) {
        super(null, registryName);
    }

    @Override
    protected RootyBlock createBlock(BlockBehaviour.Properties blockProperties) {
        return new SpreadableRootyBlock(this, blockProperties);
    }

    public void addSpreadableSoils(Block... blocks) {
        for (Block block : blocks) {
            SoilProperties props = SoilHelper.getProperties(block);
            if (props.isValid()) {
                spreadable_soils.add(props);
            }
        }
    }

    public void addSpreadableSoils(SoilProperties... props) {
        spreadable_soils.addAll(Arrays.asList(props));
    }

    public static class SpreadableRootyBlock extends RootyBlock {

        public SpreadableRootyBlock(SpreadableSoilProperties properties, Properties blockProperties) {
            super(properties, blockProperties);
        }

        @Override
        public SpreadableSoilProperties getSoilProperties() {
            return (SpreadableSoilProperties) super.getSoilProperties();
        }

        private Optional<RootyBlock> getRootyBlock(Block block) {
            return SoilHelper.getProperties(block).getBlock();
        }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
            SpreadableSoilProperties properties = getSoilProperties();
            if (properties.spread_item != null) {
                ItemStack handStack = player.getItemInHand(hand);
                if (handStack.getItem().equals(properties.spread_item)) {
                    List<Block> foundBlocks = new LinkedList<>();

                    for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
                        Block block = level.getBlockState(blockpos).getBlock();
                        if (properties.spreadable_soils.stream().anyMatch(prop -> prop.getPrimitiveSoilBlock() == block)) {
                            foundBlocks.add(block);
                        }
                    }
                    if (foundBlocks.size() > 0) {
                        if (!level.isClientSide()) {
                            int blockInt = level.random.nextInt(foundBlocks.size());
                            this.getRootyBlock(foundBlocks.get(blockInt)).ifPresent(rootyBlock ->
                                    level.setBlock(pos, rootyBlock.defaultBlockState(), 3)
                            );
                        }
                        if (!player.isCreative()) {
                            handStack.shrink(1);
                        }
                        DTClient.spawnParticles(level, ParticleTypes.HAPPY_VILLAGER, pos.above(), 2 + level.random.nextInt(5), level.random);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return super.use(state, level, pos, player, hand, hitResult);
        }

        @Override
        public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
            super.randomTick(state, level, pos, random);
            SpreadableSoilProperties properties = getSoilProperties();
            //this is a similar behaviour to vanilla grass spreading but inverted to be handled by the dirt block
            if (!level.isClientSide && properties.required_light != null) {
                if (!level.isAreaLoaded(pos, 3)) {
                    return; // Forge: prevent loading unloaded chunks when checking neighbor's light and spreading
                }
                if (level.getMaxLocalRawBrightness(pos.above()) >= properties.required_light) {
                    for (int i = 0; i < 4; ++i) {
                        BlockPos thatPos = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);

                        if (thatPos.getY() >= 0 && thatPos.getY() < 256 && !level.hasChunkAt(thatPos)) {
                            return;
                        }

                        BlockState thatStateUp = level.getBlockState(thatPos.above());
                        BlockState thatState = level.getBlockState(thatPos);

                        for (SoilProperties spreadable : properties.spreadable_soils) {
                            RootyBlock block = spreadable.getBlock().orElse(null);
                            if (block != null && (thatState.getBlock() == spreadable.getPrimitiveSoilBlock() || thatState.getBlock() == block) && level.getMaxLocalRawBrightness(pos.above()) >= properties.required_light && thatStateUp.getLightBlock(level, thatPos.above()) <= 2) {
                                if (state.hasProperty(FERTILITY)) {
                                    level.setBlockAndUpdate(pos, block.defaultBlockState().setValue(FERTILITY, state.getValue(FERTILITY)));
                                }
                                return;
                            }
                        }
                    }
                }
            }

        }

    }


}
