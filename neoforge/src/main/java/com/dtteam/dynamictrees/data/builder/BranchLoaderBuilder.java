package com.dtteam.dynamictrees.data.builder;

import com.dtteam.dynamictrees.event.handler.ClientModEventHandler;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class BranchLoaderBuilder extends CustomLoaderBuilder {

    public static final HashMap<Identifier, Supplier<BranchLoaderBuilder>> branchBuilders = new HashMap<>();

    static {
        branchBuilders.put(
                ClientModEventHandler.BRANCH, ()-> new BranchLoaderBuilder(ClientModEventHandler.BRANCH));
        branchBuilders.put(
                ClientModEventHandler.SURFACE_ROOT, ()-> new BranchLoaderBuilder(ClientModEventHandler.SURFACE_ROOT));
        branchBuilders.put(
                ClientModEventHandler.ROOTS, ()-> new BranchLoaderBuilder(ClientModEventHandler.ROOTS));
    }

    private final Map<String, String> textures = new LinkedHashMap<>();

    public BranchLoaderBuilder(Identifier loaderId) {
        super(loaderId, false);
    }

    public BranchLoaderBuilder texture(String key, Identifier location) {
        this.textures.put(key, location.toString());
        return this;
    }

    @Override
    protected CustomLoaderBuilder copyInternal() {
        BranchLoaderBuilder copy = new BranchLoaderBuilder(this.loaderId);
        copy.textures.putAll(this.textures);
        return copy;
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

}
