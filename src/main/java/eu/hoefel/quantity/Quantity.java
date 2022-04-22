package eu.hoefel.quantity;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.coordinates.axes.Axes;
import eu.hoefel.coordinates.axes.Axis;
import eu.hoefel.unit.Unit;
import eu.hoefel.unit.UnitInfo;
import eu.hoefel.unit.UnitPrefix;
import eu.hoefel.unit.Units;
import eu.hoefel.utils.Maths;

/**
 * Interface representing all quantities, i.e., numerical values with an
 * associated coordinate system.
 * <p>
 * Note that this approach is in contrast to all other quantity-packages I am
 * aware of, not only in Java, but also in python etc., which all combine a
 * quantity with a unit, not a coordinate system. The advantage of using a
 * coordinate system instead of just a unit is that more metadata is conveyed by
 * the former, opening the avenue to, e.g., more automated plotting.
 * 
 * @apiNote All implementing classes should be immutable and may be changed to
 *          value classes once project Valhalla is ready.
 * @author Udo Hoefel
 * @param <T> the type of the contained numerical values. May be a Double,
 *            double[] or double[][]. This may change in the future to support
 *            further {@link Number numbers}.
 */
public sealed interface Quantity<T> permits Quantity0D, Quantity1D, Quantity2D {

    /**
     * Gets the name of the quantity. You can generally think of it as the name you
     * would give this quantity in a plot.
     * 
     * @return the name of the contained quantity. Never null.
     */
    public String name();

    /**
     * Gets the values/numbers of the quantity. This may be a single number,
     * multiple numbers, an <i>n</i>D-point or multiple <i>n</i>D-points or even a
     * matrix (higher order tensors are currently not supported).
     * 
     * @return the numerical value(s). Never null.
     */
    public T value();

    /**
     * Gets the coordinate system associated with the quantity. Note that the
     * {@link CoordinateSystem#axes() axes} of the coordinate system contain
     * information about the {@link Unit units} of each
     * {@link CoordinateSystem#axis(int) axis}.
     * 
     * @return the coordinate system. Never null.
     */
    public CoordinateSystem coords();

    /**
     * Gets the axis of the specified dimension.
     * 
     * @param dimension the dimension of which to get the axis. Needs to be &gt;0
     *                  and less than the dimensionality of the coordinate system
     *                  (as the axes indices start with 0, not 1).
     * @return the axis of the specified dimension
     * @throws IllegalArgumentException if the specified axis dimension is out of
     *                                  range
     */
    default Axis axis(int dimension) {
        if (dimension < 0 || dimension >= coords().dimension()) {
            throw new IllegalArgumentException("The requested axis dimension needs to be within [0,%d] but was %d."
                    .formatted(coords().dimension(), dimension));
        }
        return coords().axis(dimension);
    }

    /**
     * Gets the order (also known as degree or rank) of the data tensor contained in
     * the {@link Quantity}.
     * <p>
     * <em>Note that the order of the data tensor does not necessarily match the
     * dimension of the array returned by {@link #value()}, as e.g.
     * {@link Quantity2D} may contain multiple data tensors of order 1.</em>
     * <p>
     * A tensor is also known as scalar if its order is 0, as vector if its order is
     * 1 and as matrix if its order is 2. For a single tensor its order can be
     * thought of as the number of indices necessary to describe a component of the
     * tensor uniquely.
     * 
     * @return the order of the data contained in the {@link Quantity}
     */
    public int order();

    /**
     * Converts the {@link Quantity} to the new coordinate system and creates a
     * new {@link Quantity} with the given name. Preserves the {@link #order()
     * order} of the contained data.
     * 
     * @param name the new name. May not be null.
     * @param coords the coordinate system to convert to. May not be null.
     * @return the new {@link Quantity}, never null.
     */
    public Quantity<T> to(String name, CoordinateSystem coords);

    /**
     * Applies the specified function for each position for each dimension, keeping
     * the {@link #name() name}. Note that a new {@link Quantity} is returned,
     * as they are immutable.
     * 
     * @param function the function to apply, e.g. {@code Math::abs}. May not be
     *                 null.
     * @return a new {@link Quantity} with the current name kept and the
     *         function applied for each position for each dimension. Never null.
     */
    default Quantity<T> apply(DoubleUnaryOperator function) {
        return apply(function, name());
    }

    /**
     * Applies the specified function for each position for each dimension, also
     * changing the {@link #name() name} (if the old name is still appropriate it is
     * recommended to use {@link #apply(DoubleUnaryOperator)}). Note that a new
     * {@link Quantity} is returned, as they are immutable.
     * 
     * @param function the function to apply, e.g. {@link Math#abs(double)
     *                 Math::abs}. May not be null.
     * @param name     the (new) name. May not be null.
     * @return a new {@link Quantity} with a new name and the function applied
     *         for each position for each dimension. Never null.
     */
    public Quantity<T> apply(DoubleUnaryOperator function, String name);

    /**
     * Approaches values as close as possible to the specified target values with
     * the numerical representation(s) of the current quantity by potentially
     * changing prefixes of the units on the axes. For determining the best prefix,
     * the following rules apply:
     * <ol>
     * <li>A larger (absolute) exponent implies a higher "cost". Note that each of
     * the values is put in the format <i>a</i>&times;10<sup><i>b</i></sup> with
     * <i>a</i> ranging between 1 and 9.99… beforehand.
     * <li>Furthermore, the magnitude of the difference between the target value and
     * the value of <i>a</i> is inflicting a minor penalty (i.e., the exponent
     * penalty as described in step 1 dominates).
     * <li>The prefix with the smallest maximal cost is chosen, i.e. the prefix that
     * minimizes the absolute maximum value of the exponents.
     * </ol>
     * 
     * @param targets the values to try to get close to. May not be null. The number
     *                of targets may be a single value, which is then taken as the
     *                default value, i.e. it will work for any <i>n</i>D coordinate
     *                system. Alternatively, the number of targets need to match the
     *                dimensionality of the coordinate system exactly, in which case
     *                each of the target values is matched to its corresponding
     *                coordinate system axis and only applied there. Works best for
     *                values between 1 and 9.99…
     * @return a new {@link Quantity} with adjusted units on the axis and
     *         correspondingly adjusted numerical values
     * @throws IllegalArgumentException if the number of targets does not adhere to
     *                                  the constraints specified above
     */
    default Quantity<T> approach(double... targets) {
        Objects.requireNonNull(targets);

        if (targets.length != coords().dimension() && targets.length != 1) {
            // if targets.length == 1 we assume it is meant to be the default dimension
            throw new IllegalArgumentException(
                    "The number of targets (%d) does not fit to the necessarily needed dimensions (%d)"
                            .formatted(targets.length, coords().dimension()));
        }

        // we pick a simple cost function. In principle, one could also use something
        // more sophisticated here, but I am not sure it is necessary.
        Map<Integer, ToDoubleFunction<double[]>> costFunctions = new HashMap<>();
        if (targets.length == 1) {
            // use as default
            costFunctions.put(Axes.DEFAULT_DIMENSION, d -> calculateCostForArray(d, targets[0]));
        } else {
            for (int i = 0; i < targets.length; i++) {
                double target = targets[i];
                costFunctions.put(i, d -> calculateCostForArray(d, target));
            }
        }

        return approach(costFunctions);
    }

    /**
     * Changes the numerical representation(s) of the current quantity to have
     * value(s) associated with the lowest cost as calculated by the specified cost
     * functions by changing the prefixes of the units for each axis, if possible.
     * 
     * @param costFunctions the cost functions to be applied to the axes. The key of
     *                      the map corresponds to the dimension of a axis. Use of
     *                      {@link Axes#DEFAULT_DIMENSION} is supported. May not be
     *                      null.
     * @return a new quantity with adjusted units on the axis and correspondingly
     *         adjusted numerical values
     */
    public Quantity<T> approach(Map<Integer, ToDoubleFunction<double[]>> costFunctions);

    /**
     * Calculates the "cost" for an array of values with respect to a reference
     * value ({@code target}) via a simple cost function.
     * 
     * @param array  the array
     * @param target the reference value
     * @return the cost
     */
    private static double calculateCostForArray(double[] array, double target) {
        int targetExponent = Maths.getBase10Exponent(target);
        double targetMantissa = target / Math.pow(10, targetExponent);

        return Arrays.stream(array)
                     .map(val -> calculateSimpleCostForSinglePoint(val, targetMantissa, targetExponent))
                     .max()
                     .orElse(Double.NaN);
    }

    /**
     * Calculates a "cost" for a single point by calculating some simple measure of
     * "distance" of the given point to the specified reference.
     * 
     * @param value          the point to calculate the cost respectively distance
     *                       for
     * @param targetMantissa the reference mantissa, between 1 and 9.99…
     * @param targetExponent the reference exponent
     * @return the "cost" or "distance"
     */
    private static double calculateSimpleCostForSinglePoint(Number value, double targetMantissa, int targetExponent) {
        // always between 0 and 308
        int exponent = Math.abs(targetExponent - Maths.getBase10Exponent(value.doubleValue()));

        // always between 1.0 and 9.99...
        double mantissa = Math.abs(targetMantissa - value.doubleValue() / Math.pow(10, targetExponent));

        return 0.1 * mantissa + exponent;
    }

    /**
     * Finds the potential transformations of the given unit, i.e., by a simple
     * change of the prefixes, if allowed.
     * 
     * @param unit the unit to check for alternative prefixes
     * @return for each different prefix its corresponding unit and the function to
     *         be applied to values to get to that unit.
     */
    static Map<Unit, DoubleUnaryOperator> potentialTransformations(Unit unit) {
        UnitInfo[] infos = Units.collectInfo(unit.symbols().get(0)).values().toArray(UnitInfo[]::new);

        int indexOfPrefixableUnit = IntStream.range(0, infos.length)
                 .filter(index -> mayChangePrefix(infos[index].unit()))
                 .findFirst()
                 .orElse(-1);

        if (indexOfPrefixableUnit == -1) {
            // so no prefixable unit found -> return old unit
            return Map.of(unit, DoubleUnaryOperator.identity());
        }

        Map<Unit, DoubleUnaryOperator> transformations = new HashMap<>();
        Set<UnitPrefix> allowedPrefixes = new HashSet<>(infos[indexOfPrefixableUnit].unit().prefixes());

        // make sure we have the identity prefix in there
        allowedPrefixes.add(Units.IDENTITY_PREFIX);

        for (UnitPrefix prefix : allowedPrefixes) {
            StringBuilder fullSymbol = new StringBuilder();
            List<Unit> extraUnits = new ArrayList<>();
            DoubleUnaryOperator func = null;
            for (int i = 0; i < infos.length; i++) {
                extraUnits.add(infos[i].unit());
                if (i == indexOfPrefixableUnit) {
                    Unit infoUnit = infos[i].unit();
                    // we need to find the symbol that allows to change the prefix, which might not
                    // be the first (think of "kg")
                    String symbol = infoUnit.symbols().stream()
                            .filter(infoUnit::prefixAllowed)
                            .findFirst()
                            .orElseThrow(() -> new UnsupportedOperationException(infoUnit + " appears to have no prefixable symbol"));

                    symbol += infos[i].exponent() == 1 ? "" : "^" + infos[i].exponent();
                    
                    String symbolWithPrefix = prefix.symbols().get(0) + symbol;
                    fullSymbol.append(symbolWithPrefix);
                    Unit targetUnit = Unit.of(symbolWithPrefix, new Unit[] { infoUnit });
                    func = val -> Units.convert(val, unit, targetUnit);
                } else {
                    String symbol = infos[i].symbol() + (infos[i].exponent() == 1 ? "" : "^" + infos[i].exponent());
                    fullSymbol.append(symbol);
                }
                fullSymbol.append(" ");
            }

            transformations.put(Unit.of(fullSymbol.toString(), extraUnits.toArray(Unit[]::new)), func);
        }

        return Map.copyOf(transformations);
    }

    /**
     * Checks whether any of the symbols of a unit allows prefixes. Note that this
     * method covers the edge case of {@link eu.hoefel.unit.si.SiBaseUnit#KILOGRAM
     * kilogram}, of which the primary reference symbol happens to be "kg" (which
     * contains already a prefix) and not "g".
     * 
     * @param unit the unit to check
     * @return true if any of the symbols allows a prefix
     */
    private static boolean mayChangePrefix(Unit unit) {
        return unit.symbols().stream().anyMatch(unit::prefixAllowed);
    }

    /**
     * Gets the components of the coordinate system record with updated axes of the
     * coordinate system. The order of the record components is preserved, such that
     * the values returned from this method can be used for the canonical
     * constructor of the record.
     * 
     * @param coords  the coordinate system to take the arguments from. May not be
     *                null.
     * @param newAxes the new axes to put on the coordinate system. May not be null.
     * @return the arguments of the {@code coords} with the given new axes. Never
     *         null.
     * @throws IllegalArgumentException      if the length of the given new axes
     *                                       does not match the dimensionality of
     *                                       the coordinate system
     * @throws UnsupportedOperationException if the coordinate system implementation
     *                                       is not a record
     */
    static Object[] argsWithNewUnitsOnAxes(CoordinateSystem coords, Axis... newAxes) {
        Objects.requireNonNull(coords);
        Objects.requireNonNull(newAxes);
 
        if (!coords.getClass().isRecord()) {
            throw new UnsupportedOperationException(
                    "Currently, only coordinate systems that are records are supported.");
        }

        if (newAxes.length != coords.dimension()) {
            throw new IllegalArgumentException("The given axes need to be of the length that "
                    + "matches the dimensionality of the coordinate system, but the values were: given axes: "
                    + newAxes.length + ", dimensionality of the coordinate system: " + coords.dimension() + ".");
        }

        var components = coords.getClass().getRecordComponents();

        Object[] args = new Object[components.length];
        var coordAxes = coords.axes();
        for (int i = 0; i < components.length; i++) {
            try {
                Object arg = components[i].getAccessor().invoke(coords);
                if (coordAxes.equals(arg)) {
                    arg = Axes.of(newAxes);
                }
                args[i] = arg;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Cannot get " + components[i].getName(), e);
            }
        }

        return args;
    }
}
