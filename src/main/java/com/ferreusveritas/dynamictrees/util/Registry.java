package com.ferreusveritas.dynamictrees.util;

import com.google.common.collect.Sets;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.loading.AdvancedLogMessageAdapter;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A custom registry which can be safely unlocked at any point. Largely based off {@link ForgeRegistry}.
 *
 * @author Harley O'Connor
 */
public final class Registry<V extends RegistryEntry<V>, T extends Registry.EntryType<V>> implements Iterable<V> {

    private static final Marker REGISTRY_DUMP = MarkerManager.getMarker("REGISTRY_DUMP");

    private static final Comparator<String> STRING_COMPARATOR = Comparator.naturalOrder();

    private final Map<ResourceLocation, T> typeRegistry = new HashMap<>();
    private final Set<V> entries = Sets.newHashSet();
    private final Class<V> type;
    private final T defaultType;
    private final String name;
    private final V nullValue;
    private boolean locked = false;

    private final List<Runnable> onLockRunnables = new ArrayList<>();

    public Registry(final Class<V> type, final T defaultType, final V nullValue) {
        this.type = type;
        this.defaultType = defaultType;
        this.name = type.getSimpleName();
        this.nullValue = nullValue;

        this.register(nullValue);
    }

    public final void register(final V value) {
        final ResourceLocation registryName = value.getRegistryName();

        if (this.locked) {
            throw new RuntimeException(this.getErrorMessage(value, registryName, " to locked registry "));
        }

        if (this.has(registryName)) {
            throw new RuntimeException(this.getErrorMessage(value, registryName, " that already had a value registered in registry "));
        }

        this.entries.add(value);
    }

    @SafeVarargs
    public final void registerAll(final V... values) {
        for (final V value : values)
            register(value);
    }

    public final void registerType(final ResourceLocation registryName, final T type) {
        this.typeRegistry.put(registryName, type);
    }

    public final boolean hasType(final ResourceLocation registryName) {
        return this.typeRegistry.containsKey(registryName);
    }

    @Nullable
    public final T getType(final ResourceLocation registryName) {
        return this.typeRegistry.get(registryName);
    }

    public final T getDefaultType() {
        return defaultType;
    }

    public final String getErrorMessage (final V value, final ResourceLocation registryName, final String message) {
        return "Tried to register '" + value + "' under registry name '" + registryName + "' " + message + " '" + this.name + "'.";
    }

    public final boolean has(final ResourceLocation registryName) {
        return this.getRegistryNames().contains(registryName);
    }

    public final V get(final ResourceLocation registryName) {
        final Optional<V> optionalValue = this.entries.stream().filter(entry -> entry.getRegistryName().equals(registryName)).findFirst();
        return optionalValue.orElse(this.nullValue);
    }

    public final Set<V> getAll() {
        return this.entries;
    }

    public final Set<ResourceLocation> getRegistryNames () {
        return this.entries.stream().map(RegistryEntry::getRegistryName).collect(Collectors.toSet());
    }

    public final Class<V> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public final boolean isLocked() {
        return locked;
    }

    /**
     * Locks the registries, dumping all registry objects to debug log.
     */
    public final void lock () {
        this.locked = true;
        this.dump();

        // Run all of the on lock runnables and then clear them.
        this.onLockRunnables.forEach(Runnable::run);
        this.onLockRunnables.clear();
    }

    /**
     * Unlocks the registries for modification.
     */
    public final void unlock () {
        this.locked = false;
    }

    /**
     * Runs the {@link Runnable} next time this {@link Registry} is locked.
     *
     * @param runnable The {@link Runnable} to run on lock.
     */
    public final void runOnNextLock (final Runnable runnable) {
        this.onLockRunnables.add(runnable);
    }

    public final Runnable generateIfValidRunnable (final ResourceLocation registryName, final Consumer<V> consumer, final Runnable elseRunnable) {
        return () -> {
            if (!this.get(registryName).ifValid(consumer)) {
                elseRunnable.run();
            }
        };
    }

    public final void postRegistryEvent() {
        ModLoader.get().postEvent(new RegistryEvent<>(this));
    }

    /** Comparator for sorting the {@link RegistryEntry} objects by their registry names (in natural order). */
    private final Comparator<V> comparator = (entry, entryToCompareTo) -> STRING_COMPARATOR
            .compare(entry.getRegistryName().getPath(), entryToCompareTo.getRegistryName().getPath());

    public final Comparator<V> getComparator() {
        return comparator;
    }

    /**
     * Dumps all entries with their registry names in the debug log, based off the
     * {@link ForgeRegistry} dump method.
     */
    public final void dump () {
        LogManager.getLogger().debug(REGISTRY_DUMP, () -> new AdvancedLogMessageAdapter(builder -> {
            builder.append("Name: ").append(this.name).append('\n');
            this.getAll().stream().sorted(this.comparator).forEach(entry -> builder.append("\tEntry: ")
                    .append(entry.getRegistryName()).append(", ").append(entry).append('\n'));
        }));
    }

    @Override
    public final Iterator<V> iterator() {
        return this.entries.iterator();
    }

    public static class EntryType<V extends RegistryEntry<V>> {}

}
