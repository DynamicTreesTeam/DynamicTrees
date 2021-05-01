package com.ferreusveritas.dynamictrees.api.registry;

import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.loading.AdvancedLogMessageAdapter;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A custom registry which can be safely unlocked at any point. Largely based off {@link ForgeRegistry}.
 *
 * @param <V> The {@link RegistryEntry} type that will be registered.
 * @author Harley O'Connor
 */
public class Registry<V extends RegistryEntry<V>> implements Iterable<V> {

    private static final Marker REGISTRY_DUMP = MarkerManager.getMarker("REGISTRY_DUMP");

    private static final Comparator<String> STRING_COMPARATOR = Comparator.naturalOrder();

    /**
     * The {@link Set} of {@link RegistryEntry} objects currently registered.
     */
    private final Set<V> entries = Sets.newHashSet();

    private final Class<V> type;

    /**
     * The name of this {@link Registry}. This will usually be obtained from calling
     * {@link Class#getSimpleName()} on the {@link RegistryEntry}, but some registries
     * may choose to use custom names.
     */
    protected final String name;

    /**
     * The "null" value. This is what will be returned by {@link #get(ResourceLocation)} if the entry
     * was not found in the registry.
     */
    protected final V nullValue;

    /**
     * Holds whether or not the {@link Registry} is currently locked. This is false (unlocked)
     * by default, and should then be locked after all initial registries are created by
     * {@link #postRegistryEvent()}.
     *
     * <p>It can then be unlocked by calling {@link #unlock()} to register new values, but should
     * always be locked after by calling {@link #lock()} again, which performs additional tasks
     * like {@link #dump()}.</p>
     */
    private boolean locked = false;

    /**
     * A {@link List} of runnables that will be called on the next {@link #lock()} call. Allows
     * for things to be run once all registries are "final" (at least for the time being). Note
     * that these will be cleared after use (every time the registry is locked).
     */
    private final List<Runnable> onLockRunnables = new ArrayList<>();

    /**
     * If this {@link Registry} is clearable, {@link #clear()} can be called, which wipes all
     * the values and locks the registry.
     */
    private final boolean clearable;

    private final Codec<V> getterCodec;

    /**
     * Constructs a new {@link Registry} with the name being set to {@link Class#getSimpleName()}
     * of the given {@link RegistryEntry}.
     *
     * @param type The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     */
    public Registry(final Class<V> type, final V nullValue) {
        this(type.getSimpleName(), type, nullValue);
    }

    /**
     * Constructs a new {@link Registry}.
     *
     * @param name The {@link #name} for this {@link Registry}.
     * @param type The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     */
    public Registry(final String name, final Class<V> type, final V nullValue) {
        this(name, type, nullValue, false);
    }

    /**
     * Constructs a new {@link Registry} with the name being set to {@link Class#getSimpleName()}
     * of the given {@link RegistryEntry}.
     *
     * @param type The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     * @param clearable True if {@link #clear()} can be called to wipe the registry.
     */
    public Registry(final Class<V> type, final V nullValue, final boolean clearable) {
        this(type.getSimpleName(), type, nullValue, clearable);
    }

    /**
     * Constructs a new {@link Registry}.
     *
     * @param name The {@link #name} for this {@link Registry}.
     * @param type The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     * @param clearable True if {@link #clear()} can be called to wipe the registry.
     */
    public Registry(final String name, final Class<V> type, final V nullValue, final boolean clearable) {
        this.name = name;
        this.type = type;
        this.nullValue = nullValue.nullEntry();
        this.clearable = clearable;
        this.getterCodec = ResourceLocation.CODEC.comapFlatMap(this::getAsDataResult, RegistryEntry::getRegistryName);

        this.register(nullValue);
    }

    /**
     * Registers the given {@link RegistryEntry} to this {@link Registry}.
     *
     * <p>Note that this will throw a runtime exception if this {@link Registry} is locked, or if
     * the {@link ResourceLocation} already has a value registered, therefore {@link #isLocked()}
     * or/and {@link #has(ResourceLocation)} should be checked before calling if either conditions
     * are uncertain.</p>
     *
     * <p>If you're thinking of using this you should probably be doing it from a
     * {@link RegistryEvent}, in which case you don't have to worry about locking.</p>
     *
     * @param value The {@link RegistryEntry} to register.
     * @return This {@link Registry} object for chaining.
     */
    public final Registry<V> register(final V value) {
        final ResourceLocation registryName = value.getRegistryName();

        if (this.locked) {
            throw new RuntimeException(this.getErrorMessage(value, registryName, " to locked registry "));
        }

        if (this.has(registryName)) {
            throw new RuntimeException(this.getErrorMessage(value, registryName, " that already had a value registered in registry "));
        }

        this.entries.add(value);
        return this;
    }

    private String getErrorMessage (final V value, final ResourceLocation registryName, final String message) {
        return "Tried to register '" + value + "' under registry name '" + registryName + "' " + message + " '" + this.name + "'.";
    }

    /**
     * Registers all the given {@link RegistryEntry} to this {@link Registry}. See
     * {@link #register(RegistryEntry)} for more details on the specific registry objects.
     *
     * @param values The {@link RegistryEntry} objects to register.
     */
    @SafeVarargs
    public final void registerAll(final V... values) {
        for (final V value : values)
            register(value);
    }

    public final boolean has(final ResourceLocation registryName) {
        return this.entries.stream().map(RegistryEntry::getRegistryName).anyMatch(registryName::equals);
    }

    public final Optional<V> getOptional(final ResourceLocation registryName) {
        return this.entries.stream().filter(entry -> entry.getRegistryName().equals(registryName)).findFirst();
    }

    public final Optional<V> getOptional(final String registryName) {
        return this.getOptional(ResourceLocation.tryParse(registryName));
    }

    public final V get(final ResourceLocation registryName) {
        return this.getOptional(registryName).orElse(this.nullValue);
    }

    public final V get(final String registryName) {
        return this.get(ResourceLocation.tryParse(registryName));
    }

    public final DataResult<V> getAsDataResult(final ResourceLocation registryName) {
        return this.getOptional(TreeRegistry.processResLoc(registryName)).map(DataResult::success)
                .orElse(DataResult.error("Could not find " + this.name + " '" + registryName + "'."));
    }

    /**
     * Gets all {@link RegistryEntry} objects currently registered. Note this are obtained as an
     * <b>unmodifiable set</b>, meaning they should only be read from this. For registering values
     * use {@link #register(RegistryEntry)}.
     *
     * @return All {@link RegistryEntry} objects currently registered.
     */
    public final Set<V> getAll() {
        return Collections.unmodifiableSet(this.entries);
    }

    /**
     * Gets all the registry name {@link ResourceLocation} objects of all the entries currently in
     * this {@link Registry}.
     *
     * @return The {@link Set} of registry names.
     */
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
     * Locks the registries, dumping all registry objects to debug log and running any {@link #onLockRunnables}.
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

    public final void clear () {
        if (!this.clearable)
            return;

        this.lock();
        this.entries.clear();
    }

    public final Codec<V> getGetterCodec() {
        return getterCodec;
    }

    /**
     * Generates a runnable that runs the given {@link Consumer} only if the {@link RegistryEntry}
     * obtained from the given {@link ResourceLocation} is valid (not null), and if it's not runs
     * the given {@link Runnable}.
     *
     * @param registryName The {@link ResourceLocation} of the {@link RegistryEntry}.
     * @param consumer The {@link Consumer} to accept if the {@link RegistryEntry} is vaid.
     * @param elseRunnable A {@link Runnable} to run if the entry is not valid.
     * @return The generated {@link Runnable}.
     */
    public final Runnable generateIfValidRunnable (final ResourceLocation registryName, final Consumer<V> consumer, final Runnable elseRunnable) {
        return () -> {
            if (!this.get(registryName).ifValid(consumer)) {
                elseRunnable.run();
            }
        };
    }

    /**
     * Posts a {@link RegistryEvent} to the mod event bus. Note that this is posted using
     * {@link ModLoader#postEvent(Event)} and as such should only be called during the initial
     * loading phase.
     */
    public final void postRegistryEvent() {
        ModLoader.get().postEvent(new RegistryEvent<>(this));
    }

    /** Comparator for sorting the {@link RegistryEntry} objects by their registry names (in natural order). */
    private final Comparator<V> comparator = (entry, entryToCompareTo) -> STRING_COMPARATOR
            .compare(entry.getRegistryName().getPath(), entryToCompareTo.getRegistryName().getPath());

    /**
     * @return The {@link #comparator} for sorting the registry objects by their registry names
     * in natural order.
     */
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

}
