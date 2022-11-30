package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.event.handler.BakedModelEventHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class BranchLoaderBuilder extends CustomLoaderBuilder<BlockModelBuilder> {

    private final Map<String, String> textures = new LinkedHashMap<>();

    public BranchLoaderBuilder(ResourceLocation loaderId, BlockModelBuilder parent, ExistingFileHelper existingFileHelper) {
        super(loaderId, parent, existingFileHelper);
    }

    public BranchLoaderBuilder texture(String key, ResourceLocation location) {
        this.textures.put(key, location.toString());
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);

        final JsonObject textures = new JsonObject();
        this.textures.forEach((key, location) ->
                textures.add(key, new JsonPrimitive(location)));
        json.add("textures", textures);

        return json;
    }

    public static BranchLoaderBuilder branch(BlockModelBuilder parent, ExistingFileHelper existingFileHelper) {
        return new BranchLoaderBuilder(BakedModelEventHandler.BRANCH, parent, existingFileHelper);
    }

    public static BranchLoaderBuilder root(BlockModelBuilder parent, ExistingFileHelper existingFileHelper) {
        return new BranchLoaderBuilder(BakedModelEventHandler.ROOT, parent, existingFileHelper);
    }

}
