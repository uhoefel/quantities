package eu.hoefel.quantity;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.coordinates.axes.Axes;
import eu.hoefel.coordinates.axes.Axis;
import eu.hoefel.unit.Unit;
import eu.hoefel.utils.Maths;

/**
 * Represents a 2D quantity, i.e. either multiple vector values (respectively
 * data tensors of {@link #order() order} 1) or a single matrix (respectively a
 * data tensor of order 2). Which of the aforementioned cases is identified
 * depends on the {@link CoordinateSystem#dimension() dimension} of the
 * {@code coords}.
 * 
 * @param name   the descriptive (short) name of this quantity, not null. Note
 *               that while an empty String is allowed, it is generally
 *               advisable to use a meaningful name as otherwise methods down
 *               the road that make use of this metadata will be less useful.
 * @param value  the numerical value, not null. Also, none of its elements may
 *               be null. This array will be made immutable if it ever becomes
 *               possible, so do not rely on mutation of it. All elements need
 *               to have the same length.
 * @param coords the coordinate system, not null. Its
 *               {@link CoordinateSystem#dimension() dimension} has to be either
 *               1 or identical to the length of an element of {@code value}.
 * @author Udo Hoefel
 */
public record Quantity2D(String name, double[][] value, CoordinateSystem coords) implements Quantity<double[][]> {

    /**
     * Constructor for an unnamed 2D quantity. Note that not providing a name may
     * hamper the usefulness of the provided metadata for methods down the road.
     * 
     * @param value the numerical value, not null. Also, none of its elements may be
     *              null. All elements need to have the same length. See also the
     *              description of {@code value} in the documentation of
     *              {@link Quantity2D}.
     * @param units the units of the numerical value(s), not null. Also, none of its
     *              elements can be null. Must be of length 1 or, in case the data
     *              is of {@link #order() order} 1, the length of an element of
     *              {@code value}.
     */
    public Quantity2D(double[][] value, Unit... units) {
        this("", value, units);
    }

    /**
     * Constructor for a named 2D quantity.
     * 
     * @param name  the name of this quantity, not null. Note that while an empty
     *              String is allowed, it is generally advisable to use a meaningful
     *              name as otherwise methods down the road that make use of this
     *              metadata will be less useful.
     * @param value the numerical value, not null. Also, none of its elements may be
     *              null. All elements need to have the same length. See also the
     *              description of {@code value} in the documentation of
     *              {@link Quantity2D}.
     * @param units the units of the numerical value(s), not null. Also, none of its
     *              elements can be null. Must be of length 1 or, in case the data
     *              is of {@link #order() order} 1, the length of an element of
     *              {@code value}.
     */
    public Quantity2D(String name, double[][] value, Unit... units) {
        this(name, value, new CartesianCoordinates(value.length, Axes.withUnits(units)));
    }

    /**
     * Constructor for a named 2D quantity.
     * 
     * @param name   the name of this quantity, not null. Note that while an empty
     *               String is allowed, it is generally advisable to use a
     *               meaningful name as otherwise methods down the road that make
     *               use of this metadata will be less useful.
     * @param value  the numerical value, not null. Also, none of its elements may
     *               be null. All elements need to have the same length. This array
     *               will be made immutable if it ever becomes possible, so do not
     *               rely on mutation of it.
     * @param coords the coordinate system, not null. Its
     *               {@link CoordinateSystem#dimension() dimension} has to be either
     *               1 or identical to the length of an element of {@code value}.
     * @throws IllegalArgumentException if any of the requirements specified for
     *                                  {@code coords} are violated
     * @throws NullPointerException     if any of the arguments to the record is
     *                                  null
     */
    public Quantity2D {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        Objects.requireNonNull(coords);

        if (Stream.of(value).anyMatch(Objects::isNull)) {
            throw new NullPointerException("At least one of the elements of value is null, which is not allowed.");
        }

        if (Stream.of(value).mapToInt(val -> val.length).distinct().count() != 1) {
            throw new NullPointerException(
                    "At least one of the elements of value is of a differing length, which is not allowed.");
        }

        if (value.length > 0 && value[0].length != coords.dimension() && coords.dimension() != 1) {
            throw new IllegalArgumentException(String.format(
                    "Dimensionality of the given values (corresponding to a data tensor of order 1) "
                            + "does not match the dimensionality of the coordinate system "
                            + "(values: %d point(s) with %d dimensions each vs. a coordinate "
                            + "system with %d dimensions)",
                    value.length, value[0].length, coords.dimension()));
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that in contrast to the standard record {@link Record#toString}
     * implementation the {@code value} is not represented by its memory address but
     * rather via {@link Arrays#deepToString(Object[])}.
     */
    @Override
    public String toString() {
        return Quantity2D.class.getSimpleName() + "[name=" + name + ", value=" + Arrays.deepToString(value) + ", coords="
                + coords + "]";
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * The order of the data tensors contained in a {@link Quantity2D} is either 2,
     * if a single matrix is given, or 1, if multiple <i>n</i>-dimensional vectors
     * are given.
     */
    @Override
    public final int order() {
        return switch (coords.dimension()) {
            case 1  -> 2;
            default -> 1;
        };
    }
    
    @Override
    public Quantity<double[][]> to(String name, CoordinateSystem coords) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(coords);

        return switch (this.coords.dimension()) {
            case 1  -> throw new UnsupportedOperationException(); // TODO todo
            default -> new Quantity2D(name, transformMulti1D(value, coords), coords);
        };
    }

    @Override
    public Quantity2D apply(DoubleUnaryOperator function, String name) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(name);
        
        double[][] newValues = Stream.of(value)
                                     .map(val -> Arrays.stream(val).map(function))
                                     .toArray(double[][]::new);

        return new Quantity2D(name, newValues, coords);
    }

    @Override
    public Quantity<double[][]> approach(Map<Integer, ToDoubleFunction<double[]>> costFunctions) {
        Objects.requireNonNull(costFunctions);

        double[][] transposedValues = Maths.transpose(value);
        int dimension = coords.dimension();
        Axis[] axes = new Axis[dimension];
        double[][] vals = Maths.deepCopyPrimitiveArray(transposedValues);
        for (int i = 0; i < dimension; i++) {
            Axis axis = coords.axis(i);
            Unit unit = coords.axis(i).unit();
            double cost = Double.POSITIVE_INFINITY;
            var trafos = Quantity.potentialTransformations(unit); // one could cache the transformations if it turns out to be a bottle neck
            for (var trafo : trafos.entrySet()) {
                double[] proposedTransformedValues = DoubleStream.of(transposedValues[i]).map(trafo.getValue()).toArray();
                double proposedCost = costFunctions.getOrDefault(i, costFunctions.get(Axes.DEFAULT_DIMENSION))
                        .applyAsDouble(proposedTransformedValues);
                if (proposedCost < cost) {
                    vals[i] = proposedTransformedValues;
                    unit = trafo.getKey();
                    cost = proposedCost;
                }
            }
            axes[i] = new Axis(i, unit, axis.name());
        }

        var coordSymbol = coords.symbols().get(0);
        Set<Class<? extends CoordinateSystem>> coordClass = Set.of(coords.getClass());
        
        // the coord sys record may have additional parameters that we have to respect
        Object[] args = Quantity.argsWithNewUnitsOnAxes(coords, axes);
        
        return new Quantity2D(name, Maths.transpose(vals), CoordinateSystem.from(coordSymbol, coordClass, args));
    }

    /**
     * Transforms the given coordinates from the original coordinate system to the
     * target coordinate system.
     * 
     * @param values the values, not null. Also, none of its elements may be null.
     * @param target the original coordinate system, not null. The dimensionality
     *               and units need to be compatible to {@code coords}.
     * @return the values in the {@code target} coordinate system, never null.
     */
    private double[][] transformMulti1D(double[][] values, CoordinateSystem target) {
        double[][] numberValues = Maths.deepCopyPrimitiveArray(values);
        return Stream.of(numberValues)
                     .map(value -> Quantity1D.transform1D(value, coords, target))
                     .toArray(double[][]::new);
    }
}
