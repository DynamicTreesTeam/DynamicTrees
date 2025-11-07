package com.dtteam.dynamictrees.treepack.loader;

import com.dtteam.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.dtteam.dynamictrees.block.Growable;
import com.dtteam.dynamictrees.block.fruit.Fruit;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.JsonHelper;
import com.dtteam.dynamictrees.deserialization.deserializer.ResourceLocationDeserializer;
import com.dtteam.dynamictrees.utility.NullUtils;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * @author Harley O'Connor
 */
public final class FruitResourceLoader extends JsonRegistryResourceLoader<Fruit> {

    public FruitResourceLoader() {
        super(Fruit.REGISTRY, FRUITS);
    }

    @Override
    public void registerAppliers() {
        this.loadAppliers
                .register("max_age", Integer.class, Fruit::setMaxAge);

        this.commonAppliers.registerListApplier("block_shapes", VoxelShape.class, (fruit, list) ->
                fruit.setBlockShapes(list.toArray(new VoxelShape[0]))
        );

        // Item is needed on datagen and setup
        this.gatherDataAppliers
                .register("item_stack", Item.class, (fruit, item) -> fruit.setItemStack(new ItemStack(item)))
                .register("drop_count", Integer.class, Fruit::setDropCount)
                .register("min_drop_count", Integer.class, Fruit::setMinDropCount)
                .register("max_drop_count", Integer.class, Fruit::setMaxDropCount);
        this.setupAppliers.register("item_stack", Item.class, (fruit, item) -> fruit.setItemStack(new ItemStack(item)));

        this.reloadAppliers
                .register("item_stack", ItemStack.class, Fruit::setItemStack)
                .register("can_bone_meal", Boolean.class, Fruit::setCanBoneMeal)
                .register("growth_chance", Float.class, Fruit::setGrowthChance)
                .register("flower_hold_period_length", Float.class, Fruit::setFlowerHoldPeriodLength)
                .register("season_offset", Float.class, Fruit::setSeasonOffset)
                .register("min_production_factor", Float.class, Fruit::setMinProductionFactor)
                .register("mature_action", Growable.MatureAction.class, Fruit::setMatureAction);
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
        return NullUtils.applyIfNonnull(json.get("block_properties"), element ->
                JsonDeserializers.JSON_OBJECT.deserialize(element).orElse(null)
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
        return NullUtils.applyIfNonnull(json.get("block_registry_name"), element ->
                ResourceLocationDeserializer.create(fruit.getRegistryName().getNamespace())
                        .deserialize(element)
                        .orElse(null)
        );
    }

}
