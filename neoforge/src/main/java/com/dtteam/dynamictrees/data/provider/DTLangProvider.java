package com.dtteam.dynamictrees.data.provider;

import com.dtteam.dynamictrees.api.registry.Registry;
import com.dtteam.dynamictrees.data.DTDataProvider;
import com.dtteam.dynamictrees.data.GatherDataHelper;
import com.dtteam.dynamictrees.data.Generator;
import com.google.common.collect.ImmutableList;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Collection;
import java.util.List;

public class DTLangProvider extends LanguageProvider implements DTDataProvider {
    private final String modId;
    private final List<Registry<?>> registries;

    public DTLangProvider(PackOutput gen, String modId, Collection<Registry<?>> registries) {
        super(gen, modId, "en_us");
        this.modId = modId;
        this.registries = ImmutableList.copyOf(registries);
    }

    @Override
    protected void addTranslations() {
        this.registries.forEach(registry ->
                registry.dataGenerationStream(this.modId).forEach(entry ->
                        entry.generateLangData(this)
                )
        );
        Generator<DTLangProvider, String> generator = GatherDataHelper.getExtraLangGenerators().get(modId);
        if (generator != null) {
            generator.generate(this, "", new Generator.Dependencies());
        }
    }
}
