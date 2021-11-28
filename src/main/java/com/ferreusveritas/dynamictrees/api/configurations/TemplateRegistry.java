package com.ferreusveritas.dynamictrees.api.configurations;

import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.Optional;

/**
 * @author Harley O'Connor
 */
public class TemplateRegistry<C extends Configuration<C, ?>> {

    private final Map<ResourceLocation, ConfigurationTemplate<C>>  templates = Maps.newHashMap();

    public void register(ResourceLocation name, ConfigurationTemplate<C> template) {
        this.templates.put(name, template);
    }

    public Optional<ConfigurationTemplate<C>> get(ResourceLocation name) {
        return Optional.ofNullable(this.templates.get(name));
    }

}
