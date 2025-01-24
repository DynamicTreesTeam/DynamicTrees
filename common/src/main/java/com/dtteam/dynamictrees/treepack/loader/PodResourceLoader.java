package com.dtteam.dynamictrees.treepack.loader;

import com.dtteam.dynamictrees.api.resource.loading.preparation.JsonRegistryResourceLoader;
import com.dtteam.dynamictrees.block.Growable;
import com.dtteam.dynamictrees.block.pod.Pod;
import com.dtteam.dynamictrees.deserialization.JsonDeserializers;
import com.dtteam.dynamictrees.deserialization.JsonHelper;
import com.dtteam.dynamictrees.deserialization.deserializer.ResourceLocationDeserializer;
import com.dtteam.dynamictrees.utility.helper.NullHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author Harley O'Connor
 */
public final class PodResourceLoader extends JsonRegistryResourceLoader<Pod> {

    public PodResourceLoader() {
        super(Pod.REGISTRY, PODS);
    }

    @Override
    public void registerAppliers() {
        this.loadAppliers
                .register("max_age", Integer.class, Pod::setMaxAge)
                .register("minimum_radius", Integer.class, Pod::setMinRadius)
                .register("maximum_radius", Integer.class, Pod::setMaxRadius);

        this.commonAppliers.register("block_shapes", JsonObject.class, this::readBlockShapes);

        // Item is needed on datagen and setup
        this.gatherDataAppliers
                .register("item_stack", Item.class, (pod, item) -> pod.setItemStack(new ItemStack(item)))
                .register("drop_count", Integer.class, Pod::setDropCount)
                .register("min_drop_count", Integer.class, Pod::setMinDropCount)
                .register("max_drop_count", Integer.class, Pod::setMaxDropCount);
        this.setupAppliers.register("item_stack", Item.class, (pod, item) -> pod.setItemStack(new ItemStack(item)));

        this.reloadAppliers
                .register("item_stack", ItemStack.class, Pod::setItemStack)
                .register("can_bone_meal", Boolean.class, Pod::setCanBoneMeal)
                .register("growth_chance", Float.class, Pod::setGrowthChance)
                .register("season_offset", Float.class, Pod::setSeasonOffset)
                .register("flower_hold_period_length", Float.class, Pod::setFlowerHoldPeriodLength)
                .register("min_production_factor", Float.class, Pod::setMinProductionFactor)
                .register("mature_action", Growable.MatureAction.class, Pod::setMatureAction);
    }

    private void readBlockShapes(Pod pod, JsonObject json) {
        Direction.Plane.HORIZONTAL.stream().forEach(facing -> {
            JsonElement shapeArrayElement = json.get(facing.getName().toLowerCase(Locale.ENGLISH));
            if (!shapeArrayElement.isJsonArray()) {
                return;
            }
            List<VoxelShape> shapes = new LinkedList<>();
            shapeArrayElement.getAsJsonArray().forEach(shapeElement ->
                    JsonDeserializers.VOXEL_SHAPE.deserialize(shapeElement).ifSuccess(shapes::add)
            );
            pod.setBlockShapes(facing, shapes.toArray(new VoxelShape[0]));
        });
    }

    @Override
    protected void applyLoadAppliers(JsonRegistryResourceLoader<Pod>.LoadData loadData, JsonObject json) {
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
        return NullHelper.applyIfNonnull(json.get("block_properties"), element ->
                JsonDeserializers.JSON_OBJECT.deserialize(element).orElse(null)
        );
    }

    private void createBlock(Pod pod, JsonObject json) {
        pod.createBlock(getBlockRegistryName(pod, json), pod.getDefaultBlockProperties());
    }

    private void createBlock(Pod pod, JsonObject json, JsonObject propertiesJson) {
        final BlockBehaviour.Properties blockProperties = JsonHelper.getBlockProperties(
                propertiesJson,
                pod.getDefaultMapColor(),
                pod::getDefaultBlockProperties,
                error -> this.logError(pod.getRegistryName(), error),
                warning -> this.logWarning(pod.getRegistryName(), warning)
        );
        pod.createBlock(getBlockRegistryName(pod, json), blockProperties);
    }

    /**
     * @return the registry name to set for the pod block, or {@code null} if it was not set (which defaults to the
     * using the pod's registry name)
     */
    @Nullable
    private ResourceLocation getBlockRegistryName(Pod pod, JsonObject json) {
        return NullHelper.applyIfNonnull(json.get("block_registry_name"), element ->
                ResourceLocationDeserializer.create(pod.getRegistryName().getNamespace())
                        .deserialize(element)
                        .orElse(null)
        );
    }

}
