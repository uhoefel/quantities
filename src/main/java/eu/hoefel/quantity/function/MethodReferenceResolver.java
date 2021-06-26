package eu.hoefel.quantity.function;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import eu.hoefel.utils.Types;

/**
 * An interface intended to be implemented by functional interfaces, allowing to
 * automatically resolve some metainformation if a method reference is used for
 * the functional interface.
 * <p>
 * <em>It is mainly intended to be used for method references!</em>
 * 
 * @author Udo Hoefel
 */
public sealed interface MethodReferenceResolver extends Serializable permits SerializableFunction<?, ?> {

	/**
	 * Gets the serialized representation of the method reference.
	 * 
	 * @return the serialized method reference
	 */
    default SerializedLambda serialized() {
        if (MethodReferenceResolverCache.serializationCache.size() > MethodReferenceResolverCache.MAX_CACHE_SIZE) {
            MethodReferenceResolverCache.serializationCache.clear();
        }
        return MethodReferenceResolverCache.serializationCache.computeIfAbsent(this,
                MethodReferenceResolver::serializeMethodReferenceResolver);
    }

    /**
     * Serializes the current lambda/method reference.
     * 
     * @return the serialized lambda
     * @throws AssertionError if either the method necessary for serialization is
     *                        not accessible, or a {@link NoSuchMethodException},
     *                        {@link IllegalAccessException},
     *                        {@link InvocationTargetException} is thrown while
     *                        getting the writeReplace method reflectively and
     *                        invoking it
     */
	private SerializedLambda serializeMethodReferenceResolver() {
	    try {
            Method replaceMethod = getClass().getDeclaredMethod("writeReplace");
            if (!replaceMethod.trySetAccessible()) {
                throw new AssertionError("Cannot access method necessary to serialize method reference!");
            }
            return (SerializedLambda) replaceMethod.invoke(this);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError(e);
        }
	}

	/**
	 * Gets the method corresponding to the serialized lambda. Works correctly for
	 * arbitrarily overloaded methods.
	 * 
	 * @param lambda the serialized lambda, not null.
	 * @return the method
	 */
	public static Method method(SerializedLambda lambda) {
	    Objects.requireNonNull(lambda);

		Class<?> containingClass = lambda.getCapturedArg(0).getClass();
		String methodSignature = lambda.getImplMethodSignature();

		return Arrays.stream(containingClass.getMethods())
				.filter(method -> Objects.equals(method.getName(), lambda.getImplMethodName()))
				.filter(method -> Objects.equals(methodSignature, toSignature(method)))
				.findAny() // can only be one
				.orElseThrow(UnresolvableMethodException::new);
	}

	/**
	 * Converts the method to its signature.
	 * 
	 * @param m the method
	 * @return the signature of the method
	 */
	private static String toSignature(Method m) {
		StringBuilder sb = new StringBuilder("(");
		for (var param : m.getParameterTypes()) {
			sb.append(toName(param));
		}
		sb.append(")");
		sb.append(toName(m.getReturnType()));
		return sb.toString();
	}

	/**
	 * Converts the class to the name used e.g. in signatures.
	 * 
	 * @param clazz the class
	 * @return the name of the class as used in e.g. signatures
	 */
	private static String toName(Class<?> clazz) {
		if (clazz == boolean.class) {
			return "Z";
		} else if (clazz == byte.class) {
			return "B";
		} else if (clazz == char.class) {
			return "C";
		} else if (clazz == short.class) {
			return "S";
		} else if (clazz == int.class) {
			return "I";
		} else if (clazz == long.class) {
			return "J";
		} else if (clazz == float.class) {
			return "F";
		} else if (clazz == double.class) {
			return "D";
		} else if (clazz == void.class) {
			return "V";
		} else if (clazz.isArray()) {
		    Class<?> elementType = Types.elementType(clazz);
            int dimension = Types.dimension(clazz);
            return "[".repeat(dimension) + toName(elementType);
        } else if (!clazz.isPrimitive()) {
		    return "L" + clazz.getCanonicalName().replace('.', '/') + ";";
		}
		
		throw new AssertionError("This should be unreachable!");
	}
}