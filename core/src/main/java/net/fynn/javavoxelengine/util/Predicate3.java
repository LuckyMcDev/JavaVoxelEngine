package net.fynn.javavoxelengine.util;

// Einfaches Drei-Argument-Prädikat
@FunctionalInterface
public interface Predicate3<A,B,C> {
    boolean test(A a, B b, C c);
}
