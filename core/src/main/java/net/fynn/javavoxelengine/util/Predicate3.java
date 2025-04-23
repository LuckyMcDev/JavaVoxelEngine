package net.fynn.javavoxelengine.util;

/**
 * Ein funktionales Interface für ein Prädikat mit drei Argumenten.
 * <p>
 * Es testet eine Bedingung basierend auf drei Eingabewerten und gibt ein {@code boolean} zurück.
 *
 * @param <A> der Typ des ersten Eingabewerts
 * @param <B> der Typ des zweiten Eingabewerts
 * @param <C> der Typ des dritten Eingabewerts
 */
@FunctionalInterface
public interface Predicate3<A, B, C> {
    /**
     * Prüft, ob die Bedingung auf die gegebenen Eingabewerte zutrifft.
     *
     * @param a der erste Eingabewert
     * @param b der zweite Eingabewert
     * @param c der dritte Eingabewert
     * @return {@code true}, wenn die Bedingung erfüllt ist, sonst {@code false}
     */
    boolean test(A a, B b, C c);
}

