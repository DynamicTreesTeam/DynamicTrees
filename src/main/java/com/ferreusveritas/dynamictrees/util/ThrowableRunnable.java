package com.ferreusveritas.dynamictrees.util;

/**
 * Identical to {@link Runnable}, but {@code throws} a {@link Throwable} of type {@link T}.
 *
 * @param <T> The {@link Throwable} type.
 * @author Harley O'Connor
 */
public interface ThrowableRunnable<T extends Throwable> {

    /**
     * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
     * causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may take any action whatsoever.
     *
     * @see java.lang.Thread#run()
     */
    void run() throws T;

}
