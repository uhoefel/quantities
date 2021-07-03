package eu.hoefel.quantity;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.stream.DoubleStream;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.coordinates.CoordinateSystems;
import eu.hoefel.coordinates.axes.Axes;
import eu.hoefel.coordinates.axes.Axis;
import eu.hoefel.unit.Unit;

/**
 * Represents a 1D quantity, i.e. either multiple scalar values (respectively
 * data tensors of {@link #order() order} 0) or a single vector (respectively a
 * data tensor of order 1). Which of the aforementioned cases is identified
 * depends on the {@link CoordinateSystem#dimension() dimension} of the
 * {@code coords}.
 * 
 * @param name   the descriptive (short) name of this quantity, not null. Note
 *               that while an empty String is allowed, it is generally
 *               advisable to use a meaningful name as otherwise methods down
 *               the road that make use of this metadata will be less useful.
 * @param value  the numerical value, not null. This array will be made
 *               immutable if it ever becomes possible, so do not rely on
 *               mutation of it.
 * @param coords the coordinate system, not null. Its
 *               {@link CoordinateSystem#dimension() dimension} has to be either
 *               1 or identical to the length of the given {@code value}.
 * @author Udo Hoefel
 */
public record Quantity1D(String name, double[] value, CoordinateSystem coords) implements Quantity<double[]> {

    /**
     * Constructor for an unnamed 1D quantity with a default Cartesian coordinate
     * system. Note that not providing a name may hamper the usefulness of the
     * provided metadata for methods down the road.
     * 
     * @param value the numerical value, not null. See also the description of
     *              {@code value} in the documentation of {@link Quantity1D}.
     * @param units the units of the numerical value(s), not null. Also, none of its
     *              elements can be null. Its length determines the dimensionality
     *              of the Cartesian coordinate system.
     */
    public Quantity1D(double[] value, Unit... units) {
        this("", value, units);
    }

    /**
     * Constructor for a named 1D quantity with a default Cartesian coordinate
     * system.
     * 
     * @param name  the name of this quantity, not null. Note that while an empty
     *              String is allowed, it is generally advisable to use a meaningful
     *              name as otherwise methods down the road that make use of this
     *              metadata will be less useful.
     * @param value the numerical value, not null. See also the description of
     *              {@code value} in the documentation of {@link Quantity1D}.
     * @param units the units of the numerical value(s), not null. Also, none of its
     *              elements can be null. Its length determines the dimensionality
     *              of the Cartesian coordinate system.
     */
    public Quantity1D(String name, double[] value, Unit... units) {
        this(name, value, new CartesianCoordinates(units.length, Axes.withUnits(units)));
    }

    /**
     * Constructor for a named 1D quantity.
     * 
     * @param name   the name of this quantity, not null. Note that while an empty
     *               String is allowed, it is generally advisable to use a
     *               meaningful name as otherwise methods down the road that make
     *               use of this metadata will be less useful.
     * @param value  the numerical value, not null. Also, none of its elements may
     *               be null. This array will be made immutable if it ever becomes
     *               possible, so do not rely on mutation of it.
     * @param coords the coordinate system, not null. Its
     *               {@link CoordinateSystem#dimension() dimension} has to be either
     *               1 or identical to the length of the given {@code value}.
     * @throws IllegalArgumentException if any of the requirements specified for
     *                                  {@code coords} are violated
     * @throws NullPointerException     if any of the arguments to the record is
     *                                  null
     */
    public Quantity1D {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        Objects.requireNonNull(coords);

        if (coords.dimension() > 1 && value.length != coords.dimension()) {
            throw new IllegalArgumentException("The given coordinate system is multidimensional "
                    + "(implying that the given values correspond to a single point in the "
                    + "multidimensional coordinate system), but the given values are not of the "
                    + "correct dimensionality (point: %d vs. coords: %d)".formatted(value.length, coords.dimension()));
        }
    }

    /**
     * Merges the given {@link Quantity0D quantities} into one {@link Quantity1D}
     * with {@link Quantity1D#order() order} 0, by taking the first given non-null
     * quantity as reference, i.e., all other quantities are converted to this
     * reference quantity.
     * 
     * @param quantities the quantities to merge. Null elements are discarded while
     *                   merging. All elements need to be convertible to the
     *                   coordinate system of the first non-null element, which
     *                   serves as the reference quantity also for constructing the
     *                   returned merged quantity. May not be null.
     * @return the quantity containing the merged quantities. Never returns null.
     * @throws NoSuchElementException if no non-null {@link Quantity0D} was given
     */
    public static final Quantity1D from(Quantity0D... quantities) {
        Objects.requireNonNull(quantities);

        Quantity0D refQuantity = Arrays.stream(quantities)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No valid Quantity0D given!"));

        double[] newValues = Arrays.stream(quantities)
                .filter(Objects::nonNull)
                .map(q -> q.to(q.name(), refQuantity.coords()))
                .mapToDouble(Quantity::value)
                .toArray();
        
        return new Quantity1D(refQuantity.name(), newValues, refQuantity.coords());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that in contrast to the standard record {@link Record#toString}
     * implementation the array is not represented by its memory address but rather
     * via {@link Arrays#toString(double[])}.
     */
    @Override
    public String toString() {
        return Quantity1D.class.getSimpleName() + "[name=" + name + ", value=" + Arrays.toString(value) + ", coords="
                + coords + "]";
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * The order of the data tensors contained in a {@link Quantity1D} is either 0,
     * if there are multiple scalars given, or 1, if a <i>n</i>-dimensional vector
     * is given.
     */
    @Override
    public final int order() {
        return switch (coords.dimension()) {
            case 1  -> 0;
            default -> 1;
        };
    }

    @Override
    public Quantity<double[]> to(String name, CoordinateSystem coords) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(coords);

        return switch (this.coords.dimension()) {
            case 1  -> new Quantity1D(name, transformMulti0D(value, coords), coords);
            default -> new Quantity1D(name, transform1D(value, this.coords, coords), coords);
        };
    }

    @Override
    public Quantity1D apply(DoubleUnaryOperator function, String name) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(name);

        return new Quantity1D(name, DoubleStream.of(value).map(function).toArray(), coords);
    }

    @Override
    public Quantity<double[]> approach(Map<Integer, ToDoubleFunction<double[]>> costFunctions) {
        Objects.requireNonNull(costFunctions);

        return switch (order()) {
            case 0 -> reduceN0DPoints(costFunctions.getOrDefault(0, costFunctions.get(Axes.DEFAULT_DIMENSION)));
            case 1 -> reduceOneNDPoint(costFunctions);
            default -> throw new AssertionError("Unexpected order: " + order());
        };
    }

    /**
     * Performs a coordinate transformation of the given point from the original
     * coordinate system to the target coordinate system.
     * 
     * @param values the coordinates in the original coordinate system, not null
     * @param origin the original coordinate system, not null. The dimensionality
     *               and units need to be compatible to {@code target}.
     * @param target the target coordinate system, not null. The dimensionality and
     *               units need to be compatible to {@code origin}.
     * @return the coordinates in the target coordinate system
     */
    static double[] transform1D(double[] values, CoordinateSystem origin, CoordinateSystem target) {
        return CoordinateSystems.transform(values, origin, target);
    }

    /**
     * Transforms the given coordinates from the original coordinate system to the
     * target coordinate system.
     * 
     * @param values the values, not null
     * @param target the original coordinate system, not null. The dimensionality
     *               and units need to be compatible to {@code origin}.
     * @return the values in the {@code target} coordinate system
     */
    private double[] transformMulti0D(double[] values, CoordinateSystem target) {
        return DoubleStream.of(values)
                           .map(value -> Quantity0D.transform0D(value, coords, target))
                           .toArray();
    }

    /**
     * Reduces the numerical representations of the 1D quantity to have values
     * associated with the lowest cost as calculated by the specified cost function
     * by changing the prefixes of the unit, if possible.
     * 
     * @param costFunction the cost function to be applied to the axis. May not be null.
     * @return a new quantity with an adjusted unit on the axis and correspondingly
     *         adjusted numerical values
     */
    private Quantity1D reduceN0DPoints(ToDoubleFunction<double[]> costFunction) {
        Axis axis = coords.axis(0);
        Unit unit = axis.unit();
        var trafos = Quantity.potentialTransformations(unit);
        double cost = Double.POSITIVE_INFINITY;
        double[] transformedValues = value.clone();
        
        for (var trafo : trafos.entrySet()) {
            double[] proposedTransformedValues = DoubleStream.of(value).map(trafo.getValue()).toArray();
            double proposedCost = costFunction.applyAsDouble(proposedTransformedValues);
            if (proposedCost < cost) {
                transformedValues = proposedTransformedValues;
                unit = trafo.getKey();
                cost = proposedCost;
            }
        }
        axis = new Axis(0, unit, axis.name());
        
        var coordSymbol = coords.symbols().get(0);
        Set<Class<? extends CoordinateSystem>> coordClass = Set.of(coords.getClass());
        
        // the coord sys record may have additional parameters that we have to respect
        Object[] args = Quantity.argsWithNewUnitsOnAxes(coords, axis);
        
        return new Quantity1D(name, transformedValues, CoordinateSystem.from(coordSymbol, coordClass, args));
    }

    /**
     * Reduces the numerical representation of the 1D quantity to have values
     * associated with the lowest cost as calculated by the specified cost functions
     * by changing the prefixes of the units for each axis, if possible.
     * 
     * @param costFunctions the cost functions to be applied to the axes. The key of
     *                      the map corresponds to the dimension of a axis. Use of
     *                      {@link Axes#DEFAULT_DIMENSION} is supported. May not be
     *                      null.
     * @return a new quantity with adjusted units on the axes and correspondingly
     *         adjusted numerical values
     */
    private Quantity1D reduceOneNDPoint(Map<Integer, ToDoubleFunction<double[]>> costFunctions) {
        int dimension = coords.dimension();

        Axis[] newAxes = new Axis[dimension];
        double[] vals = value.clone();
        for (int i = 0; i < dimension; i++) {
            Axis axis = coords.axis(i);
            Unit unit = axis.unit();
            System.out.println(unit);
            double cost = Double.POSITIVE_INFINITY;

            var trafos = Quantity.potentialTransformations(unit);
            for (var trafo : trafos.entrySet()) {
                double proposedTransformedValues = trafo.getValue().applyAsDouble(value[i]);
                double proposedCost = costFunctions.getOrDefault(i, costFunctions.get(Axes.DEFAULT_DIMENSION))
                        .applyAsDouble(new double[] { proposedTransformedValues });
                if (proposedCost < cost) {
                    vals[i] = proposedTransformedValues;
                    unit = trafo.getKey();
                    cost = proposedCost;
                }
            }

            newAxes[i] = new Axis(i, unit, axis.name());
        }
        
        var coordSymbol = coords.symbols().get(0);
        Set<Class<? extends CoordinateSystem>> coordClass = Set.of(coords.getClass());
        
        // the coord sys record may have additional parameters that we have to respect
        Object[] args = Quantity.argsWithNewUnitsOnAxes(coords, newAxes);
        
        return new Quantity1D(name, vals, CoordinateSystem.from(coordSymbol, coordClass, args));
    }
}