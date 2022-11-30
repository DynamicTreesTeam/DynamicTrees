package com.ferreusveritas.dynamictrees.api.data;

import com.ferreusveritas.dynamictrees.data.provider.DTDataProvider;
import net.minecraft.data.DataProvider;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A generator for a resource, providing the means to construct a Json file using provided {@link Dependencies} and
 * using a provided {@link DataProvider}.
 *
 * @param <P> the type of the data provider to use
 * @param <I> the type of the input to get the dependencies from
 * @author Harley O'Connor
 */
public interface Generator<P extends DataProvider & DTDataProvider, I> {

    /**
     * Gathers dependencies from the specified {@code input}, then generating the relevant files if dependencies and
     * input are valid.
     *
     * @param provider the provider to use to generate
     * @param input the input to gather dependencies and generate from
     * @see #verifyInput(Object)
     * @see #verifyDependencies(Dependencies)
     */
    default void generate(P provider, I input) {
        final Dependencies dependencies = this.gatherDependencies(input);
        if (this.verifyInput(input) && this.verifyDependencies(dependencies)) {
            this.generate(provider, input, dependencies);
        }
    }

    /**
     * Handles generating the relevant files. External invokers should prefer using {@link #generate(IDataProvider,
     * Object)} to this method since it gathers dependencies automatically.
     *
     * @param provider the provider to use to generate
     * @param input the input to generate from
     * @param dependencies the dependencies to generate from
     */
    void generate(P provider, I input, Dependencies dependencies);

    /**
     * Gathers the dependencies required for this generator from the specified {@code input}.
     *
     * @param input the input object
     * @return the gathered dependencies
     */
    Dependencies gatherDependencies(I input);

    /**
     * Verifies that the specified {@code input} is valid. Can be used for setting certain conditions that input must
     * meet to be generated.
     *
     * @param input the input object to verify
     * @return {@code true} if the input is valid; {@code false} if not
     */
    default boolean verifyInput(I input) {
        return true;
    }

    /**
     * Verifies that all the specified {@code dependencies} are valid. A dependency will be considered invalid if its
     * key specifies it as non-optional, but it is not present.
     *
     * @param dependencies the dependencies to verify
     * @return {@code true} if the specified {@code dependencies} are valid for this generator; {@code false} if not
     */
    default boolean verifyDependencies(Dependencies dependencies) {
        return dependencies.allRequiredPresent();
    }

    /**
     * This class represents a unique identifier for a dependency in terms of its {@link #name} and whether o not it is
     * {@link #optional}.
     *
     * @param <T> the type of the dependency
     */
    class DependencyKey<T> {
        /** The name of the dependency. */
        private final String name;
        /** {@code true} if this dependency is optional; {@code false} if not. */
        private final boolean optional;

        public DependencyKey(String name) {
            this(name, false);
        }

        public DependencyKey(String name, boolean optional) {
            this.name = name;
            this.optional = optional;
        }

        public String getName() {
            return name;
        }

        public boolean isOptional() {
            return optional;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            DependencyKey<?> that = (DependencyKey<?>) other;
            return optional == that.optional && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, optional);
        }
    }

    /**
     * Wrapper class for a map containing all dependencies by {@link DependencyKey} to their {@link Optional} value.
     * This ensures that the map is safely interacting with, only allowing the correct value to be tied to its
     * corresponding key.
     *
     * @see DependencyKey
     */
    class Dependencies {
        private final Map<DependencyKey<?>, Optional<?>> dependencies = new HashMap<>();

        public <T> Dependencies appendValue(DependencyKey<T> key, @Nullable T value) {
            return this.append(key, Optional.ofNullable(value));
        }

        public <T> Dependencies append(DependencyKey<T> key, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<T> optional) {
            this.dependencies.put(key, optional);
            return this;
        }

        /**
         * Gets the plain value for the specified {@code key}. This must be a non-optional dependency.
         *
         * @param key the key to get the dependency value for
         * @param <T> the type of the dependency value
         * @return the dependency value
         * @throws IllegalArgumentException if the specified {@code key} is optional
         * @throws IllegalStateException if the dependency for the specified {@code key} did not exist
         * @see #getOptional(DependencyKey)
         */
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        public <T> T get(DependencyKey<T> key) {
            if (key.isOptional()) {
                throw new IllegalArgumentException("Tried to get plain value for optional dependency with key \"" + key.getName() + "\".");
            }
            return this.getOptional(key).get();
        }

        /**
         * Returns an {@link Optional} containing the value of the dependency from the specified {@code key}, or being
         * empty if it was assigned {@link Optional#empty()}.
         *
         * @param key the key to get the dependency value for
         * @param <T> the type of the dependency value
         * @throws IllegalStateException if the dependency for the specified {@code key} did not exist
         * @return an optional containing the value of the obtained dependency, or being empty if it was assigned
         * {@link Optional#empty()}
         * @see #get(DependencyKey)
         */
        @SuppressWarnings({"unchecked", "OptionalAssignedToNull"})
        public <T> Optional<T> getOptional(DependencyKey<T> key) {
            final Optional<?> optional = this.dependencies.get(key);
            if (optional == null) {
                throw new IllegalStateException("Tried to obtain absent dependency from key \"" + key.getName() + "\"");
            }
            return (Optional<T>) optional;
        }

        /**
         * @return {@code true} if all required (non-optional) dependencies are present; otherwise {@code false}
         */
        public boolean allRequiredPresent() {
            return this.dependencies.entrySet().stream()
                    .filter(entry -> !entry.getKey().isOptional())
                    .map(Map.Entry::getValue)
                    .allMatch(Optional::isPresent);
        }
    }

}
