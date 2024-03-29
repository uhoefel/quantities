package eu.hoefel.quantity;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.coordinates.CoordinateSystems;
import eu.hoefel.coordinates.axes.Axes;
import eu.hoefel.coordinates.axes.Axis;
import eu.hoefel.unit.Unit;

/**
 * Represents a 0D quantity, i.e. a single numerical value with a unit,
 * corresponding to a data tensor of {@link #order() order} 0.
 * 
 * @param name   the descriptive (short) name of this quantity, not null. Note
 *               that while an empty String is allowed, it is generally
 *               advisable to use a meaningful name as otherwise methods down
 *               the road that make use of this metadata will be less useful.
 * @param value  the numerical value, not null. This may change its type to a
 *               double once project Valhalla is integrated into Java.
 * @param coords the coordinate system, not null. Its
 *               {@link CoordinateSystem#dimension() dimension} has to be 1.
 * @author Udo Hoefel
 */
public record Quantity0D(String name, Double value, CoordinateSystem coords) implements Quantity<Double> {

    /**
     * Constructor for an unnamed 0D quantity. Note that not providing a name may
     * hamper the usefulness of the provided metadata for methods down the road.
     * 
     * @param value the numerical value, not null. See also the description of
     *              {@code value} in the documentation of {@link Quantity0D}
     * @param unit  the unit of the numerical value, not null
     */
    public Quantity0D(double value, Unit unit) {
        this("", value, unit);
    }

    /**
     * Constructor for a named 0D quantity.
     * 
     * @param name  the name of this quantity, not null. Note that while an empty
     *              String is allowed, it is generally advisable to use a meaningful
     *              name as otherwise methods down the road that make use of this
     *              metadata will be less useful.
     * @param value the numerical value, not null. See also the description of
     *              {@code value} in the documentation of {@link Quantity0D}
     * @param unit  the unit of the numerical value, not null
     */
    public Quantity0D(String name, double value, Unit unit) {
        this(name, value, new CartesianCoordinates(new Axis(0, unit, "")));
    }

    /**
     * Constructor for a named 0D quantity.
     * 
     * @param name   the name of this quantity, not null. Note that while an empty
     *               String is allowed, it is generally advisable to use a
     *               meaningful name as otherwise methods down the road that make
     *               use of this metadata will be less useful.
     * @param value  the numerical value, not null
     * @param coords the coordinate system, not null. Its
     *               {@link CoordinateSystem#dimension() dimension} has to be 1.
     * @throws IllegalArgumentException if any of the requirements specified for
     *                                  {@code coords} are violated
     * @throws NullPointerException     if any of the arguments to the record is
     *                                  null
     */
    public Quantity0D {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        Objects.requireNonNull(coords);

        if (coords.dimension() != 1) {
            throw new IllegalArgumentException("The dimensionality of a coordinate system of a "
                    + "Quantity0D must be exactly 1, but it was " + coords().dimension());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * The order of the data tensor contained in a {@link Quantity0D} is always 0. 
     */
    @Override
    public final int order() {
        return 0;
    }

    @Override
    public Quantity0D to(String name, CoordinateSystem coords) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(coords);

        return new Quantity0D(name, transform0D(value, this.coords, coords), coords);
    }
    
    @Override
    public Quantity0D apply(DoubleUnaryOperator function, String name) {
        Objects.requireNonNull(function);
        Objects.requireNonNull(name);

        return new Quantity0D(name, function.applyAsDouble(value), coords);
    }

    /**
     * {@inheritDoc}
     * 
     * @param costFunctions the cost function to be applied to the axis. May not be
     *                      null. The integer corresponds to the axis dimension and
     *                      consequently has to be either {@code 0} or
     *                      {@link Axes#DEFAULT_DIMENSION} for a {@link Quantity0D}
     */
    @Override
    public Quantity0D approach(Map<Integer, ToDoubleFunction<double[]>> costFunctions) {
        Objects.requireNonNull(costFunctions);

        var costFunction = costFunctions.getOrDefault(0, costFunctions.get(Axes.DEFAULT_DIMENSION));

        Axis axis = coords.axis(0);
        Unit unit = axis.unit();
        var trafos = Quantity.potentialTransformations(unit);
        double cost = Double.POSITIVE_INFINITY;
        double transformedValues = value;

        for (var trafo : trafos.entrySet()) {
            double proposedTransformedValues = trafo.getValue().applyAsDouble(value);
            double proposedCost = costFunction.applyAsDouble(new double[] { proposedTransformedValues });
            if (proposedCost < cost) {
                transformedValues = proposedTransformedValues;
                unit = trafo.getKey();
                cost = proposedCost;
            }
        }
        axis = new Axis(0, unit, axis.name());

        var coordSymbol = coords.symbols().get(0);
        var coordClass = Set.of(coords.getClass());

        // the coord sys record may have additional parameters that we have to respect
        Object[] args = Quantity.argsWithNewUnitsOnAxes(coords, axis);

        return new Quantity0D(name, transformedValues, CoordinateSystem.from(coordSymbol, coordClass, args));
    }

    /**
     * Transforms the given coordinates from the original coordinate system to the
     * target coordinate system.
     * 
     * @param value  the value, not null
     * @param origin the original coordinate system, not null. The dimensionality
     *               and units need to be compatible to {@code target}.
     * @param target the original coordinate system, not null. The dimensionality
     *               and units need to be compatible to {@code origin}.
     * @return the transformed value
     */
    static double transform0D(double value, CoordinateSystem origin, CoordinateSystem target) {
        return CoordinateSystems.transform(new double[] { value }, origin, target)[0];
    }
}
