package de.feu.massim22.group3.map;

/**
 * The Interface <code>Disposable</code> defines a method to clean resources allocated by a class.
 *
 * @author Heinz Stadler
 */
public interface Disposable {
    /**
     * Disposes resources allocated by the class which don't get cleaned by the garbage collector.
     */
    void dispose();
}
