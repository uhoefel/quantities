package eu.hoefel.quantity.function;

import java.lang.invoke.SerializedLambda;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Utility class as {@link MethodReferenceResolver} cannot hold the non-public
 * cache field.
 * 
 * @author Udo Hoefel
 */
class MethodReferenceResolverCache {

    /** The maximum number of entries in the cache. */
    static final int MAX_CACHE_SIZE = 100;

    /** The cache for the resolved serialized lambdas. */
    static final ConcurrentMap<MethodReferenceResolver, SerializedLambda> serializationCache = new ConcurrentHashMap<>(
            MAX_CACHE_SIZE);

    /** Private constructor to hide the public one for this utility class. */
    private MethodReferenceResolverCache() {
        throw new IllegalStateException("Utility class");
    }
}
