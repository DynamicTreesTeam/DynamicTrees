package com.ferreusveritas.dynamictrees.api.configuration;

import com.ferreusveritas.dynamictrees.api.registry.SimpleRegistry;

/**
 * @author Harley O'Connor
 */
public final class ConfigurableRegistry<V extends ConfigurableRegistryEntry<V, C>, C extends Configuration<C, V>>
        extends SimpleRegistry<V> {

    private final TemplateRegistry<C> templates;

    public ConfigurableRegistry(Class<V> type, V nullValue, TemplateRegistry<C> templates) {
        super(type, nullValue);
        this.templates = templates;
    }

    public ConfigurableRegistry(String name, Class<V> type, V nullValue, TemplateRegistry<C> templates) {
        super(name, type, nullValue);
        this.templates = templates;
    }

    public ConfigurableRegistry(Class<V> type, V nullValue, boolean clearable, TemplateRegistry<C> templates) {
        super(type, nullValue, clearable);
        this.templates = templates;
    }

    public ConfigurableRegistry(String name, Class<V> type, V nullValue, boolean clearable, TemplateRegistry<C> templates) {
        super(name, type, nullValue, clearable);
        this.templates = templates;
    }

    @Override
    public ConfigurableRegistry<V, C> register(V value) {
        super.register(value);
        if (this.templates != null) {
            this.registerTemplate(value);
        }
        return this;
    }

    private void registerTemplate(V value) {
        this.templates.register(value.getRegistryName(),
                new DefaultConfigurationTemplate<>(value.getDefaultConfiguration(), value));
    }

    public TemplateRegistry<C> getTemplates() {
        return templates;
    }

}
