package eu.hoefel.quantity.function;

import java.util.function.Function;

/**
 * Represents a function that accepts one argument and produces a result.
 * 
 * <p>
 * Note that this functional interface is intended to be used with method
 * references.
 * 
 * @param <T> the type of the input to the function
 * @param <U> the type of the result of the function
 */
@FunctionalInterface
public non-sealed interface SerializableFunction<T, U> extends Function<T, U>, MethodReferenceResolver {
    // nothing to add here, the method to be implemented is in Function, and the
    // MethodReferenceResolver has default implementations which are fine
}
