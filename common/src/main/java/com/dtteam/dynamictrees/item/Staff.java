package com.dtteam.dynamictrees.item;

import com.dtteam.dynamictrees.api.treedata.TreePart;
import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.block.branch.BranchBlock;
import com.dtteam.dynamictrees.block.branch.TrunkShellBlock;
import com.dtteam.dynamictrees.data.tags.DTBlockTags;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.worldgen.DynamicTreeGenerationContext;
import com.dtteam.dynamictrees.worldgen.JoCode;
import com.dtteam.dynamictrees.worldgen.RootsJoCode;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Consumer;


/**
 * Try the following in a command block to demonstrate the extra tag functionality. {@code /give @p
 * dynamictrees:staff{color:0x88FF00,code:"OUiVpPzkbtJ9uSRPbZP",read_only:1,tree:"dynamictrees:birch",max_uses:16,display:{Name:'[{"text":"Name","italic":false}]'}}}
 */
public class Staff extends Item {

    public final static float REACH_DISTANCE = 512;

    public Staff(Identifier id) {
        super(new Properties().stacksTo(1).component(DataComponents.RARITY, Rarity.RARE).component(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.builder()
                        //.add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(DynamicTrees.location("dynamictrees_staff_range"), REACH_DISTANCE, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HAND)
                        .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 5.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                        .build()
        ).setId(ResourceKey.create(Registries.ITEM, id)));
        DTRegistries.CREATIVE_TAB_ITEMS.add(this);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.getBlock() instanceof BranchBlock || state.getBlock() instanceof TrunkShellBlock) {
            return 64.0f;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (state.getBlock() instanceof BranchBlock || state.getBlock() instanceof TrunkShellBlock) {
            if (damage(stack)) {
                stack.shrink(1);
            }
            return true;
        }
        return false;
    }

    // This extends the reach of the wand if the player is creative


    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !player.isCreative()) return super.use(level, player, hand);
        HitResult hitResult = player.pick(REACH_DISTANCE, 0.0F, false);
        if (hitResult.getType() == HitResult.Type.BLOCK)
            if (useOn(new UseOnContext(player, hand, (BlockHitResult) hitResult)) == InteractionResult.SUCCESS){
                return InteractionResult.SUCCESS;
            }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        ItemStack heldStack = context.getPlayer().getItemInHand(context.getHand());

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.canBeReplaced() || state.is(DTBlockTags.FOLIAGE)) {
            pos = pos.below();
            state = level.getBlockState(pos);
        }

        BlockPos rootPos = TreeHelper.findRootNode(level, pos);
        TreePart treePart = TreeHelper.getTreePart(level.getBlockState(rootPos));

        // Get the code from a tree or rooty dirt and set it in the staff
        if (!isReadOnly(heldStack) && treePart.isRootNode()) {
            Species species = TreeHelper.getExactSpecies(level, rootPos);
            if (species.isValid()) {
                if (!context.getPlayer().isShiftKeyDown()) {
                    String code = new JoCode(level, rootPos, context.getPlayer().getDirection()).toString();
                    setCode(heldStack, code);
                    String rootCode = new RootsJoCode(level, rootPos, context.getPlayer().getDirection()).toString();
                    setRootsCode(heldStack, rootCode);
                    if (level.isClientSide()) { // Make sure this doesn't run on the server
                        Minecraft.getInstance().keyboardHandler.setClipboard(code); // Put the code in the system clipboard to annoy everyone.
                    }
                }
                setSpecies(heldStack, species);
                return InteractionResult.SUCCESS;
            }
        }

        //Create a tree from right clicking on soil
        Species species = getSpecies(heldStack);
        if (species.isValid() && species.isAcceptableSoil(level, pos, state)) {
            DynamicTreeGenerationContext generationContext = new DynamicTreeGenerationContext(LevelContext.create(level), species, pos, pos.mutable(), level.getBiome(pos), context.getPlayer().getDirection(), 8, false);
            species.getJoCode(getCode(heldStack)).setCareful(true).generate(generationContext);
            String rootsCode = getRootsCode(heldStack);
            if (!rootsCode.isEmpty())
                species.getRootsJoCode(rootsCode).setCareful(true).generate(generationContext);
            if (hasMaxDamage(heldStack)) {
                if (damage(heldStack)) {
                    heldStack.shrink(1);//If the player is in creative this will have no effect.
                }
            } else {
                heldStack.shrink(1);//If the player is in creative this will have no effect.
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return hasMaxDamage(pStack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int damage = getDamage(stack) / getMaxDamage(stack);
        return 1 - damage;
    }

    public static boolean isReadOnly(ItemStack itemStack) {
        return itemStack.has(DTRegistries.READ_ONLY_DATA_COMPONENT.get());
    }

    public static void setReadOnly(ItemStack itemStack, boolean readonly) {
        itemStack.set(DTRegistries.READ_ONLY_DATA_COMPONENT.get(), Unit.INSTANCE);
    }

    public static void setSpecies(ItemStack itemStack, Species species) {
        String name = species.getRegistryName().toString();
        itemStack.set(DTRegistries.SPECIES_DATA_COMPONENT.get(), name);
    }

    public static void setCode(ItemStack itemStack, String code) {
        itemStack.set(DTRegistries.JOCODE_DATA_COMPONENT.get(), code);
    }

    public static void setRootsCode(ItemStack itemStack, String code) {
        itemStack.set(DTRegistries.ROOTS_JOCODE_DATA_COMPONENT.get(), code);
    }

    public static Species getSpecies(ItemStack itemStack) {
        if (itemStack.has(DTRegistries.SPECIES_DATA_COMPONENT.get())) {
            return Species.findSpecies(itemStack.get(DTRegistries.SPECIES_DATA_COMPONENT.get()));
        } else {
            Species species = Species.findSpeciesSloppy("oak");
            setSpecies(itemStack, species);
            return species;
        }
    }

    /** NeoForge Override */
    public int getDamage(ItemStack itemStack) {
        return Mth.clamp(itemStack.getOrDefault(DataComponents.DAMAGE, 0), 0, itemStack.getMaxDamage());
    }

    /** NeoForge Override */
    public void setDamage(ItemStack itemStack, int value) {
        itemStack.set(DataComponents.DAMAGE, value);
    }

    /** NeoForge Override */
    public int getMaxDamage(ItemStack itemStack) {
        return itemStack.getOrDefault(DataComponents.MAX_DAMAGE, 0);
    }

    public void setMaxDamage(ItemStack itemStack, int value) {
        itemStack.set(DataComponents.MAX_DAMAGE, value);
    }

    public boolean hasMaxDamage(ItemStack itemStack) {
        if (itemStack.has(DataComponents.MAX_DAMAGE)) {
            return getMaxDamage(itemStack) != 0;
        }
        return false;
    }

    public boolean damage(ItemStack itemStack) {
        int maxDmg = getMaxDamage(itemStack);
        int dmg = Math.min(maxDmg, getDamage(itemStack) + 1);
        setDamage(itemStack, dmg);
        return dmg == maxDmg;
    }

    public int getColor(ItemStack itemStack, int tint) {
        if (tint == 0) {
            int color = 0xFF5b472f; // Original brown wood color

            Species species = getSpecies(itemStack);

            if (itemStack.has(DTRegistries.STAFF_HANDLE_COLOR_DATA_COMPONENT.get())) {
                color = itemStack.getOrDefault(DTRegistries.STAFF_HANDLE_COLOR_DATA_COMPONENT.get(), new DyedItemColor(color)).rgb();
            } else if (species.isValid()) {
                color = species.getFamily().woodBarkColor;
            }

            return color;
        } else if (tint == 1) {
            int color = 0xFF00FFFF; // Cyan crystal like Radagast the Brown's staff.

            if (itemStack.has(DTRegistries.STAFF_CRYSTAL_COLOR_DATA_COMPONENT.get())) {
                color = itemStack.get(DTRegistries.STAFF_CRYSTAL_COLOR_DATA_COMPONENT.get()).rgb();
            }

            return color;
        }

        return 0xFFFFFFFF; // white
    }

    public Staff setColor(ItemStack itemStack, int color) {
        itemStack.set(DTRegistries.STAFF_CRYSTAL_COLOR_DATA_COMPONENT.get(), new DyedItemColor(color));
        return this;
    }


    public String getCode(ItemStack itemStack) {
        String code = "P";//Code of a sapling

        if (itemStack.has(DTRegistries.JOCODE_DATA_COMPONENT.get())) {
            String newCode = itemStack.get(DTRegistries.JOCODE_DATA_COMPONENT.get());
            if (newCode != null) code = newCode;
        } else {
            itemStack.set(DTRegistries.JOCODE_DATA_COMPONENT.get(), code);
        }

        return code;
    }

    public String getRootsCode(ItemStack itemStack) {
        if (itemStack.has(DTRegistries.ROOTS_JOCODE_DATA_COMPONENT.get())) {
            String newCode = itemStack.get(DTRegistries.ROOTS_JOCODE_DATA_COMPONENT.get());
            if (newCode != null) return newCode;
        }
        return "";
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltip.dynamictrees.species", this.getSpecies(itemStack).getTextComponent()));
        builder.accept(Component.translatable("tooltip.dynamictrees.jo_code", new JoCode(this.getCode(itemStack)).getTextComponent()));
        String rootsCode = getRootsCode(itemStack);
        if (!rootsCode.isEmpty())
            builder.accept(Component.translatable("tooltip.dynamictrees.roots_jo_code", new RootsJoCode(rootsCode).getTextComponent()));
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }

}
