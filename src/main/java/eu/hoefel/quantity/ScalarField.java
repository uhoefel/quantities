package eu.hoefel.quantity;

import java.util.Objects;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.quantity.function.MethodReferenceResolver.SerializableFunction;
import eu.hoefel.utils.Types;

/**
 * A scalar field describes a field that takes a tensor (currently only tensors
 * with an order of 0, 1 or 2 are supported) and creates scalar(s) for each
 * given tensor. The return type of {@link #apply(Quantity)} is always a
 * {@link Quantity1D} to handle cases in which multiple positions are provided
 * by the {@link Quantity} given to {@link #apply(Quantity) apply}.
 * 
 * @param <T>               the input type to the scalar field. Needs to be
 *                          either double/Double, double[] or double[][]. These
 *                          rules may be subject to change after project
 *                          Valhalla is integrated in Java.
 * @param name              the descriptive (short) name of this field, not
 *                          null. Note that while an empty String is allowed, it
 *                          is generally advisable to use a meaningful name as
 *                          otherwise methods down the road that make use of
 *                          this metadata will be less useful.
 * @param field             the scalar field, not null. For allowed input types
 *                          see the description of {@code T}. Note that the
 *                          choice of the input type limits which
 *                          {@link Quantity quantities} can be used successfully
 *                          as input as the input type effectively is bound to
 *                          the {@link Quantity#order() order} of the data
 *                          tensor.
 * @param fieldInputCoords  the coordinate system describing input to the
 *                          {@code field}. May not be null.
 * @param fieldOutputCoords the coordinate system describing the output of the
 *                          {@code field}. May not be null. This determines the
 *                          coordinate system in the {@link Quantity1D} returned
 *                          by {@link #apply(Quantity)}.
 * @author Udo Hoefel
 */
public record ScalarField<T>(String name, SerializableFunction<T, Double> field, CoordinateSystem fieldInputCoords,
        CoordinateSystem fieldOutputCoords) implements SerializableFunction<Quantity<?>, Quantity1D> {

    /**
     * Canonical constructor for a named scalar field.
     * 
     * @param name              the descriptive (short) name of this field, not
     *                          null. Note that while an empty String is allowed, it
     *                          is generally advisable to use a meaningful name as
     *                          otherwise methods down the road that make use of
     *                          this metadata will be less useful.
     * @param field             the scalar field, not null. For allowed input types
     *                          see the description of {@code T}. Note that the
     *                          choice of the input type limits which
     *                          {@link Quantity quantities} can be used successfully
     *                          as input as the input type effectively is bound to
     *                          the {@link Quantity#order() order} of the data
     *                          tensor.
     * @param fieldInputCoords  the coordinate system describing input to the
     *                          {@code field}. May not be null.
     * @param fieldOutputCoords the coordinate system describing the output of the
     *                          {@code field}. May not be null. This determines the
     *                          coordinate system in the {@link Quantity1D} returned
     *                          by {@link #apply(Quantity)}.
     * @throws IllegalArgumentException if the dimensionality of the
     *                                  {@code fieldOutputCoords} is not 1, the
     *                                  input type of the {@code field} is not a
     *                                  double/Double, double[] or double[][] or if
     *                                  the dimensionality of the
     *                                  {@code fieldInputCoords} is &gt;1, but the
     *                                  {@code field} input type is a double/Double
     * @throws NullPointerException     if any of the arguments to the record is
     *                                  null
     */
    public ScalarField {
        Objects.requireNonNull(name);
        Objects.requireNonNull(field);
        Objects.requireNonNull(fieldInputCoords);
        Objects.requireNonNull(fieldOutputCoords);

        if (fieldOutputCoords.dimension() != 1) {
            throw new IllegalArgumentException(
                    "The dimensionality of the coordinate system of the generated scalars is "
                            + fieldOutputCoords.dimension() + ", but should be 1 for scalars.");
        }

        // we can rule out certain combinations if we know the field input type
        Class<?> fieldInputType = fieldInputType(field);

        if (fieldInputType != double.class && fieldInputType != double[].class && fieldInputType != double[][].class) {
            throw new IllegalArgumentException(
                    "The field input type needs to be either a double, double[] or double[][].");
        }

        if (fieldInputCoords.dimension() > 1 && fieldInputType == double.class) {
            throw new IllegalArgumentException("The field input type needs cannot be a double if the dimensionality "
                    + "required for the input coordinate system is " + fieldInputCoords.dimension() + ".");
        }
    }

    /**
     * Gets the input type of the given function by checking the instantiated method
     * type. Note that if the input type is a {@link Double}, {@code double.class},
     * not {@code Double.class} gets returned, i.e., the class of the primitive, not
     * its wrapper gets returned.
     * 
     * @param function the function for which to get the input type. May not be
     *                 null.
     * @return the (unboxed) input type for the given function, never null.
     * @throws IllegalArgumentException if the input type is not a {@link Double},
     *                                  {@code double[]} or {@code double[][]}
     */
    private Class<?> fieldInputType(SerializableFunction<T, Double> function) {
        return switch (function.serialized().getInstantiatedMethodType()) {
            case "(Ljava/lang/Double;)Ljava/lang/Double;" -> double.class;
            case "([D)Ljava/lang/Double;" -> double[].class;
            case "([[D)Ljava/lang/Double;" -> double[][].class;
            default -> throw new IllegalArgumentException(
                    "Unexpected signature of field: " + function.serialized().getInstantiatedMethodType());
        };
    }

    /**
     * {@inheritDoc}
     * 
     * @throws UnsupportedOperationException if the order of the data in
     *                                       {@code position} is &gt;2.
     * @throws IllegalArgumentException      if the field cannot handle the type of
     *                                       the {@link Quantity#value()}
     * @throws AssertionError                if another implementation is added to
     *                                       the sealed {@link Quantity} class
     */
    @SuppressWarnings("unchecked") // we have enough checks in place to make sure this is safe
    @Override
    public Quantity1D apply(Quantity<?> position) {
        Objects.requireNonNull(position);

        if (position.order() > 2) {
            throw new UnsupportedOperationException(
                    "Currently only scalars, vectors and matrices can be applied to a scalar field.");
        }

        Quantity<?> transformedPosition = position.to(position.name(), fieldInputCoords);

        Class<?> classOfValue = Types.unboxedClass(transformedPosition.value().getClass());
        Class<?> fieldInputType = fieldInputType(field);
        if (classOfValue != fieldInputType && classOfValue != fieldInputType.arrayType()) {
            throw new IllegalArgumentException("The values of the given position are of type " + classOfValue
                    + ", but the field can only handle " + fieldInputType.getSimpleName() + " or "
                    + fieldInputType.arrayType().getSimpleName() + " as input.");
        }

        // TODO use pattern matching once available
        double[] scalars;
        if (transformedPosition instanceof Quantity0D q0d) {
            scalars = new double[] { field.apply((T) q0d.value()) };
        } else if (transformedPosition instanceof Quantity1D q1d) {
            scalars = switch (q1d.order()) {
                case 0  -> DoubleStream.of(q1d.value()).boxed().mapToDouble(val -> field.apply((T) val)).toArray();
                case 1  -> new double[] { field.apply((T) q1d.value()) };
                default -> throw new AssertionError("Quantity1D cannot logically support this order: " + q1d.order());
            };
        } else if (transformedPosition instanceof Quantity2D q2d) {
            scalars = switch (q2d.order()) {
                case 1  -> Stream.of(q2d.value()).mapToDouble(val -> field.apply((T) val)).toArray();
                case 2  -> new double[] { field.apply((T) q2d.value()) };
                default -> throw new AssertionError("Quantity2D cannot logically support this order: " + q2d.order());
            };
        } else {
            throw new AssertionError("should never get here");
        }

        return new Quantity1D(name, scalars, fieldOutputCoords);
    }
}
