package com.dtteam.dynamictrees.data.builder;

import com.dtteam.dynamictrees.event.handler.ClientModEventHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class BranchLoaderBuilder extends CustomLoaderBuilder<BlockModelBuilder> {

    private final Map<String, String> textures = new LinkedHashMap<>();

    public BranchLoaderBuilder(ResourceLocation loaderId, BlockModelBuilder parent, ExistingFileHelper fileHelper) {
        super(loaderId, parent, fileHelper, false);
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

    public static BranchLoaderBuilder branch(BlockModelBuilder parent, ExistingFileHelper fileHelper) {
        return new BranchLoaderBuilder(ClientModEventHandler.BRANCH, parent, fileHelper);
    }

    public static BranchLoaderBuilder surfaceRoot(BlockModelBuilder parent, ExistingFileHelper fileHelper) {
        return new BranchLoaderBuilder(ClientModEventHandler.SURFACE_ROOT, parent, fileHelper);
    }

    public static BranchLoaderBuilder Roots(BlockModelBuilder parent, ExistingFileHelper fileHelper) {
        return new BranchLoaderBuilder(ClientModEventHandler.ROOTS, parent, fileHelper);
    }

}
