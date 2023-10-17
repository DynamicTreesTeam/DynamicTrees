package com.ferreusveritas.dynamictrees.resources.loader;

import com.ferreusveritas.dynamictrees.api.applier.ApplierRegistryEvent;
import com.ferreusveritas.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.ferreusveritas.dynamictrees.block.GrowableBlock;
import com.ferreusveritas.dynamictrees.deserialisation.JsonDeserialisers;
import com.ferreusveritas.dynamictrees.deserialisation.JsonHelper;
import com.ferreusveritas.dynamictrees.deserialisation.ResourceLocationDeserialiser;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import com.ferreusveritas.dynamictrees.util.Null;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
public final class FruitResourceLoader extends JsonRegistryResourceLoader<Fruit> {

    public FruitResourceLoader() {
        super(Fruit.REGISTRY, ApplierRegistryEvent.FRUITS);
    }

    @Override
    public void registerAppliers() {
        this.loadAppliers
                .register("max_age", Integer.class, Fruit::setMaxAge);

        this.commonAppliers
                .registerListApplier("block_shapes", VoxelShape.class, (fruit, list) ->
                        fruit.setBlockShapes(list.toArray(new VoxelShape[0]))
                )
                .register("item_stack", ResourceLocation.class, (fruit, resourceLocation) ->
                        Species.REGISTRY.runOnNextLock(
                                ()-> fruit.setItemStack(new ItemStack(ForgeRegistries.ITEMS.getValue(resourceLocation)))
                        ));

        this.reloadAppliers
                .register("item_stack", ItemStack.class, Fruit::setItemStack)
                .register("can_bone_meal", Boolean.class, Fruit::setCanBoneMeal)
                .register("growth_chance", Float.class, Fruit::setGrowthChance)
                .register("flower_hold_period_length", Float.class, Fruit::setFlowerHoldPeriodLength)
                .register("season_offset", Float.class, Fruit::setSeasonOffset)
                .register("min_production_factor", Float.class, Fruit::setMinProductionFactor)
                .register("mature_action", GrowableBlock.MatureAction.class, Fruit::setMatureAction);
    }

    @Override
    protected void applyLoadAppliers(JsonRegistryResourceLoader<Fruit>.LoadData loadData, JsonObject json) {
        super.applyLoadAppliers(loadData, json);
        final JsonObject propertiesJson = getBlockPropertiesJson(json);
        if (propertiesJson == null) {
            this.createBlock(loadData.getResource(), json);
        } else {
            this.createBlock(loadData.getResource(), json, propertiesJson);
        }
    }

    @Nullable
    private JsonObject getBlockPropertiesJson(JsonObject json) {
        return Null.applyIfNonnull(json.get("block_properties"), element ->
                JsonDeserialisers.JSON_OBJECT.deserialise(element).orElse(null)
        );
    }

    private void createBlock(Fruit fruit, JsonObject json) {
        fruit.createBlock(getBlockRegistryName(fruit, json), fruit.getDefaultBlockProperties());
    }

    private void createBlock(Fruit fruit, JsonObject json, JsonObject propertiesJson) {
        final BlockBehaviour.Properties blockProperties = JsonHelper.getBlockProperties(
                propertiesJson,
                fruit.getDefaultMapColor(),
                fruit::getDefaultBlockProperties,
                error -> this.logError(fruit.getRegistryName(), error),
                warning -> this.logWarning(fruit.getRegistryName(), warning)
        );
        fruit.createBlock(getBlockRegistryName(fruit, json), blockProperties);
    }

    /**
     * @return the registry name to set for the fruit block, or {@code null} if it was not set (which defaults to the
     * using the fruit's registry name)
     */
    @Nullable
    private ResourceLocation getBlockRegistryName(Fruit fruit, JsonObject json) {
        return Null.applyIfNonnull(json.get("block_registry_name"), element ->
                ResourceLocationDeserialiser.create(fruit.getRegistryName().getNamespace())
                        .deserialise(element)
                        .orElse(null)
        );
    }

}
