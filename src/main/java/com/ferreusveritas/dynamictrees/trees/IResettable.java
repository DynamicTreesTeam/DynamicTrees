package com.ferreusveritas.dynamictrees.trees;

/**
 * Represents an object that can be reset and have pre and post reload defaults set.
 *
 * @param <T> The object implementing {@link IResettable}.
 * @author Harley O'Connor
 */
@SuppressWarnings("unchecked")
public interface IResettable<T extends IResettable<T>> {

    /**
     * Can be overridden for resetting values, such as lists and maps. Should be called before {@link
     * #setPreReloadDefaults()}.
     *
     * @return This {@link IResettable} object for chaining.
     */
    default T reset() {
        return (T) this;
    }

    /**
     * Can be overridden for setting pre-reload defaults. These are any defaults that may be overridden during reload if
     * needed.
     *
     * @return This {@link IResettable} object for chaining.
     */
    default T setPreReloadDefaults() {
        return (T) this;
    }

    /**
     * Can be overridden for setting post-reload defaults. This may be used for defaults which are a requirement of a
     * sub-class, or for setting default lists if they were not populated during reload.
     *
     * @return This {@link IResettable} object for chaining.
     */
    default T setPostReloadDefaults() {
        return (T) this;
    }

}
