package eu.hoefel.quantity;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.coordinates.axes.Axes;
import eu.hoefel.coordinates.axes.Axis;
import eu.hoefel.unit.Unit;
import eu.hoefel.unit.UnitContext;
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
 * @author Udo Hoefel
 * @param <T> the type of the contained numerical values. May be a primitive,
 *            their wrapper classes, {@link BigInteger}, {@link BigDecimal} or
 *            arrays thereof.
 */
public sealed interface Quantifiable<T> {
	
	// TODO:
	// - add/subtract/mul/divide/pow/root? : ~10h
	// - putInContext(UnitContext);/where to put names? axis? : ~6h
	// - merge 1D types to 2D coord?
	// - vectors? matrices? maybe record Point(double[])/Vector(Point, double[] vector)/Matrix(Point, double[][] matrix)?
    // - need to use Number for flexibility
    // - QuantitySupplier, QuantityFunction etc?

	/**
	 * Represents a 0D quantity, i.e. a single numerical value with a unit.
	 * 
	 * @param name  the name of this quantity, not null
	 * @param value the numerical value. This type will be converted to a double
	 *              once generics can handle primitives, so it may not be null.
	 * @param unit  the unit of the numerical value, not null
	 * 
	 * @author Udo Hoefel
	 */
	public record Quantity0D<T extends Number>(String name, T value, Unit unit) implements Quantifiable<T> {

		/**
		 * Constructor.
		 * 
		 * @param name  the name of this quantity, not null
		 * @param value the numerical value. This type will be converted to a double
		 *              once generics can handle primitives, so it may not be null.
		 * @param unit  the unit of the numerical value, not null
		 */
		public Quantity0D {
			Objects.requireNonNull(name);
			Objects.requireNonNull(value);
			Objects.requireNonNull(unit);
		}

		/**
		 * Constructor for an unnamed 0D quantity. Note that not providing a name may hamper the useful
		 * 
		 * @param value the numerical value. This type will be converted to a double
		 *              once generics can handle primitives, so it may not be null.
		 * @param unit  the unit of the numerical value, not null
		 */
		public Quantity0D(T value, Unit unit) {
			this("", value, unit);
		}

		@Override
		public CoordinateSystem coords() {
			return new CartesianCoordinates(new Axis(unit));
		}
	}

	public record Quantity1D<T extends Number>(String name, T[] value, CoordinateSystem coords) implements Quantifiable<T[]> {
		
		// how to handle vector/matrix/tensor fields?
		// maybe smth akin to
		// public Quantity1D(String name, double[] value, CoordinateSystem coords, T[] tensor, T[] units) {
		// problem: 5 args is a bit much
		// maybe we don't need it at all?
		// we could just build up maybe? smth like record Vector(Quantifiable<?> pos, Quantifiable<?> vector)
		// this decouples the positions from the vector though, which is not so nice, but maybe could be catched by the constructor

		public Quantity1D {
			Objects.requireNonNull(name);
			Objects.requireNonNull(value);
			Objects.requireNonNull(coords);
			
			if (coords.dimension() > 1 && value.length != coords.dimension()) {
				throw new IllegalArgumentException("The given coordinate system is multidimensional "
						+ "(implying that the given values correspond to a single point in the multidimensional "
						+ "coordinate system), but the given values are not of the correct dimensionality (point: "
						+ "%d vs. coords: %d)".formatted(value.length, coords.dimension()));
			}
		}

		public Quantity1D(T[] value, Unit unit) {
			this("", value, new CartesianCoordinates(1, new Axis(unit)));
		}
	}
	
	public record Quantity2D<T extends Number>(String name, T[][] value, CoordinateSystem coords) implements Quantifiable<T[][]> {

		public Quantity2D {
			Objects.requireNonNull(name);
			Objects.requireNonNull(value);
			Objects.requireNonNull(coords);
			
			if (value.length != coords.dimension()) {
				throw new IllegalArgumentException(("Dimensionality of the given values does not match the "
						+ "dimensionality of the coordinate system (values: %d point(s) with %d dimensions "
						+ "each vs. a coordinate system with %d dimensions)")
						.formatted(value[0].length, value.length, coords.dimension()));
			}
		}

		public Quantity2D(T[][] value, CoordinateSystem coords) {
			this("", value, coords);
		}

		public Quantity2D(T[][] value, Unit... units) {
			this(value, new CartesianCoordinates(value.length, Axes.withUnits(units)));
		}
	}

	/**
	 * Gets the name of the quantity. You can generally think of it as the name you
	 * would give this quantity in a plot.
	 * 
	 * @return the name of the contained quantity
	 */
	public String name();

	/**
	 * Gets the values/numbers of the quantity. This may be a single number,
	 * multiple numbers, an <i>n</i>D-point or multiple <i>n</i>D-points.
	 * 
	 * @return the numerical value(s)
	 */
	public T value();

	/**
	 * Gets the coordinate system associated with the quantity. Note that the
	 * {@link CoordinateSystem#axes() axes} of the coordinate system contain
	 * information about the {@link Unit units} of each
	 * {@link CoordinateSystem#axis(int) axis}.
	 * 
	 * @return the coordinate system
	 */
	public CoordinateSystem coords();

	/**
	 * Gets the axis of the specified dimension.
	 * 
	 * @param dimension the dimension of which to get the axis. Needs to be &gt;0
	 *                  and less than the dimensionality of the coordinate system
	 *                  (as the axes indices start with 0, not 1)
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
	 * Reduces the numerical representation(s) of the current quantity to have a
	 * value as close as possible to the specified target values by potentially
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
	 * @return a new quantifiable with adjusted units on the axis and
	 *         correspondingly adjusted numerical values
	 * @throws IllegalArgumentException if the number of targets does not adhere to
	 *                                  the constraints specified above
	 */
	default Quantifiable<T> reduce(double... targets) {
		Objects.requireNonNull(targets);
		
		if (targets.length != coords().dimension() && targets.length != 1) {
			// if targets.length == 1 we assume it is meant to be the default dimension
			throw new IllegalArgumentException(
					"The number of targets (%d) does not fit to the necessarily needed dimensions (%d)"
							.formatted(targets.length, coords().dimension()));
		}

		// we pick a simple cost function. In principle, one could also use something
		// more sophisticated here, but I am not sure it is necessary.
		Map<Integer, ToDoubleFunction<Number[]>> costFunctions = new HashMap<>();
		if (targets.length == 1) {
			// use as default
			costFunctions.put(Axes.DEFAULT_DIMENSION, d -> calculateCostForArray(d, targets[0]));
		} else {
			for (int i = 0; i < targets.length; i++) {
				double target = targets[i];
				costFunctions.put(i, d -> calculateCostForArray(d, target));
			}
		}

		return reduce(costFunctions);
	}

	/**
	 * Calculates the cost for an array of values via a simple cost function.
	 * 
	 * @param array  the array
	 * @param target the reference value
	 * @return the cost
	 */
	private static double calculateCostForArray(Number[] array, double target) {
		int targetExponent = Maths.getBase10Exponent(target);
		double targetMantissa = target / Math.pow(10, targetExponent);
		
		double[] costs = new double[array.length];
		for (int i = 0; i < costs.length; i++) {
			costs[i] = calculateSimpleCostForSinglePoint(array[i], targetMantissa, targetExponent);
		}
		return Maths.max(costs);
	}

	/**
	 * Calculates a cost for a single point by calculating some simple measure of
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
		double mantissa = Math.abs(targetMantissa - value.doubleValue() / Math.pow(10, exponent));
		
		return 0.1*mantissa + exponent;
	}

	/**
	 * Reduces the numerical representation(s) of the current quantity to have
	 * value(s) associated with the lowest cost as calculated by the specified cost
	 * functions by changing the prefixes of the units for each axis, if possible.
	 * 
	 * @param costFunctions the cost functions to be applied to the axes. The key of
	 *                      the map corresponds to the dimension of a axis. Use of
	 *                      {@link Axes#DEFAULT_DIMENSION} is supported. May not be
	 *                      null.
	 * @return a new quantifiable with adjusted units on the axis and
	 *         correspondingly adjusted numerical values
	 */
	@SuppressWarnings("unchecked")
	default Quantifiable<T> reduce(Map<Integer, ToDoubleFunction<Number[]>> costFunctions) {
		Objects.requireNonNull(costFunctions);

		// TODO once pattern matching is in replace this with an (exhaustive) switch
		if (this instanceof Quantity0D<?> q0d) {
			return (Quantifiable<T>) reduce0D(q0d, costFunctions.getOrDefault(0, costFunctions.get(Axes.DEFAULT_DIMENSION)));
		} else if (this instanceof Quantity1D<?> q1d) {
			return (Quantifiable<T>) reduce1D(q1d, costFunctions);
		} else if (this instanceof Quantity2D<?> q2d) {
			return (Quantifiable<T>) reduce2D(q2d, costFunctions);
		}
		throw new AssertionError("Unsupported quantifiable!"); // cannot be thrown, Quantifiable is sealed
	}

	/**
	 * Reduces the numerical representation(s) of the 0D quantity to have a value
	 * associated with the lowest cost as calculated by the specified cost function
	 * by changing the prefixes of the unit, if possible.
	 * 
	 * @param quantity      the quantity for which the numerical values should be
	 *                      modified
	 * @param costFunctions the cost function to be applied to the axis. May not be
	 *                      null.
	 * @return a new quantity with an adjusted unit on the axis and a
	 *         correspondingly adjusted numerical value
	 */
	private static <T extends Number> Quantity0D<T> reduce0D(Quantity0D<T> quantity, ToDoubleFunction<Number[]> costFunction) {
		Unit unit = quantity.unit();
		var trafos = potentialTransformations(unit);
		double cost = Double.POSITIVE_INFINITY;
		T transformedValues = quantity.value;
		
		for (var trafo : trafos.entrySet()) {
			@SuppressWarnings("unchecked")
            T proposedTransformedValues = (T) trafo.getValue().apply(quantity.value());
			
			double proposedCost = costFunction.applyAsDouble(new Number[] { proposedTransformedValues });
			if (proposedCost < cost) {
				transformedValues = proposedTransformedValues;
				unit = trafo.getKey();
				cost = proposedCost;
			}
		}
		 
		return new Quantity0D<T>(quantity.name(), transformedValues, unit);
	}

	/**
	 * Reduces the numerical representation(s) of the 1D quantity to have values
	 * associated with the lowest cost as calculated by the specified cost function
	 * by changing the prefixes of the unit for each axis, if possible.
	 * 
	 * @param quantity      the quantity for which the numerical values should be
	 *                      modified (either multiple 1D points or a single
	 *                      <i>n</i>D point, depending on the coordinate system)
	 * @param costFunctions the cost functions to be applied to the axes. The key of
	 *                      the map corresponds to the dimension of a axis. Use of
	 *                      {@link Axes#DEFAULT_DIMENSION} is supported. May not be
	 *                      null.
	 * @return a new quantity with adjusted units on the axes and correspondingly
	 *         adjusted numerical values
	 */
	private static <T extends Number> Quantity1D<T> reduce1D(Quantity1D<T> quantity, Map<Integer, ToDoubleFunction<Number[]>> costFunctions) {
		CoordinateSystem coords = quantity.coords();
		if (coords.dimension() == 1) {
			// n values for 1D
			return reduceN0DPoints(quantity, costFunctions.getOrDefault(0, costFunctions.get(Axes.DEFAULT_DIMENSION)));
		}
		// 1 value for nD
		return reduceOneNDPoint(quantity, costFunctions);
	}

	/**
	 * Reduces the numerical representations of the 1D quantity to have values
	 * associated with the lowest cost as calculated by the specified cost function
	 * by changing the prefixes of the unit, if possible.
	 * 
	 * @param quantity the quantity for which the numerical values should be
	 *                 modified (needs to have a 1D coordinate system)
	 * @param costFunction the cost function to be applied to the axis. May not be null.
	 * @return a new quantity with an adjusted unit on the axis and correspondingly
	 *         adjusted numerical values
	 */
    private static <T extends Number> Quantity1D<T> reduceN0DPoints(Quantity1D<T> quantity, ToDoubleFunction<Number[]> costFunction) {
		CoordinateSystem coords = quantity.coords();
		Unit unit = coords.axis(0).unit();
		var trafos = potentialTransformations(unit);
		double cost = Double.POSITIVE_INFINITY;
		T[] transformedValues = Maths.deepCopyPrimitiveArray(quantity.value());
		
		
		for (var trafo : trafos.entrySet()) {
			T[] proposedTransformedValues = Maths.deepCopyPrimitiveArray(quantity.value());
			for (int i = 0; i < transformedValues.length; i++) {
			    @SuppressWarnings("unchecked")
			    T transformedValue = (T) trafo.getValue().apply(quantity.value()[i]);

				proposedTransformedValues[i] = transformedValue;
			}
			double proposedCost = costFunction.applyAsDouble(proposedTransformedValues);
			if (proposedCost < cost) {
				transformedValues = proposedTransformedValues;
				unit = trafo.getKey();
				cost = proposedCost;
			}
		}
		
		var coordSymbol = coords.symbols().get(0);
		Set<Class<? extends CoordinateSystem>> coordClass = Set.of(coords.getClass());
		var coordAxes = Axes.of(new Axis(0, unit));
		
		// the coord sys record may have additional parameters that we have to respect
		Object[] args = argsWithNewAxes(coords, coordAxes);
		
		return new Quantity1D<T>(quantity.name(), transformedValues, CoordinateSystem.from(coordSymbol, coordClass, args));
	}

	/**
	 * Reduces the numerical representation of the 1D quantity to have values
	 * associated with the lowest cost as calculated by the specified cost functions
	 * by changing the prefixes of the units for each axis, if possible.
	 * 
	 * @param quantity      the quantity for which the numerical values should be
	 *                      modified (needs to have a <i>n</i>D coordinate system,
	 *                      with <i>n</i> representing the length of the numerical
	 *                      values in the quantity)
	 * @param costFunctions the cost functions to be applied to the axes. The key of
	 *                      the map corresponds to the dimension of a axis. Use of
	 *                      {@link Axes#DEFAULT_DIMENSION} is supported. May not be
	 *                      null.
	 * @return a new quantity with adjusted units on the axes and correspondingly
	 *         adjusted numerical values
	 */
	private static <T extends Number> Quantity1D<T> reduceOneNDPoint(Quantity1D<T> quantity, Map<Integer, ToDoubleFunction<Number[]>> costFunctions) {
		CoordinateSystem coords = quantity.coords();
		int dimension = coords.dimension();

		List<Axis> axes = new ArrayList<>();
		T[] vals = Maths.deepCopyPrimitiveArray(quantity.value());
		for (int i = 0; i < dimension; i++) {
			Unit unit = coords.axis(i).unit();
			double cost = Double.POSITIVE_INFINITY;

			var trafos = potentialTransformations(unit);
			for (var trafo : trafos.entrySet()) {
				@SuppressWarnings("unchecked")
                T proposedTransformedValues = (T) trafo.getValue().apply(quantity.value()[i]);
				double proposedCost = costFunctions.getOrDefault(i, costFunctions.get(Axes.DEFAULT_DIMENSION))
						.applyAsDouble(new Number[] { proposedTransformedValues });
				if (proposedCost < cost) {
					vals[i] = proposedTransformedValues;
					unit = trafo.getKey();
					cost = proposedCost;
				}
			}
			
			axes.add(new Axis(i, unit));
		}
		
		var coordSymbol = coords.symbols().get(0);
		Set<Class<? extends CoordinateSystem>> coordClass = Set.of(coords.getClass());
		var coordAxes = Axes.of(axes.toArray(Axis[]::new));
		
		// the coord sys record may have additional parameters that we have to respect
		Object[] args = argsWithNewAxes(coords, coordAxes);
		
		return new Quantity1D<T>(quantity.name(), vals, CoordinateSystem.from(coordSymbol, coordClass, args));
	}

	/**
	 * Reduces the numerical representations of the 2D quantity to have values
	 * associated with the lowest cost as calculated by the specified cost functions
	 * by changing the prefixes of the units for each axis, if possible.
	 * 
	 * @param quantity      the quantity for which the numerical values should be
	 *                      modified
	 * @param costFunctions the cost functions to be applied to the axes. The key of
	 *                      the map corresponds to the dimension of a axis. Use of
	 *                      {@link Axes#DEFAULT_DIMENSION} is supported. May not be
	 *                      null.
	 * @return a new quantity with adjusted units on the axes and correspondingly
	 *         adjusted numerical values
	 */
	private static <T extends Number> Quantity2D<T> reduce2D(Quantity2D<T> quantity, Map<Integer, ToDoubleFunction<Number[]>> costFunctions) {
		CoordinateSystem coords = quantity.coords();
		int dimension = coords.dimension();

		List<Axis> axes = new ArrayList<>();
		T[][] vals = Maths.deepCopyPrimitiveArray(quantity.value());
		for (int i = 0; i < dimension; i++) {
			Unit unit = coords.axis(i).unit();
			double cost = Double.POSITIVE_INFINITY;

			var trafos = potentialTransformations(unit);
			for (var trafo : trafos.entrySet()) {
				T[] proposedTransformedValues = Maths.deepCopyPrimitiveArray(quantity.value()[i]);
				for (int j = 0; j < vals[i].length; j++) {
				    @SuppressWarnings("unchecked")
                    T proposedTransformedValue = (T) trafo.getValue().apply(proposedTransformedValues[j]);
					proposedTransformedValues[j] = proposedTransformedValue;
				}
				double proposedCost = costFunctions.getOrDefault(i, costFunctions.get(Axes.DEFAULT_DIMENSION))
						.applyAsDouble(proposedTransformedValues);
				if (proposedCost < cost) {
					vals[i] = proposedTransformedValues;
					unit = trafo.getKey();
					cost = proposedCost;
				}
			}
			
			axes.add(new Axis(i, unit));
		}
		
		var coordSymbol = coords.symbols().get(0);
		Set<Class<? extends CoordinateSystem>> coordClass = Set.of(coords.getClass());
		var coordAxes = Axes.of(axes.toArray(Axis[]::new));
		
		// the coord sys record may have additional parameters that we have to respect
		Object[] args = argsWithNewAxes(coords, coordAxes);
		
		return new Quantity2D<T>(quantity.name(), vals, CoordinateSystem.from(coordSymbol, coordClass, args));
	}

	// TODO It would certainly be good to make the method below more robust. What if
	// no constructor that takes Object... args is defined? What if it is not a
	// record?
	/**
	 * Gets the components of the coordinate system record, except the axes, and
	 * create a new instance of the coordinate system if possible with the resolved
	 * record components, in which the axes are replaced by the given axes.
	 * 
	 * @param coords the coordinate system to take the arguments from
	 * @param axes   the axes to use for the new instance of the coordinate system
	 * @return a copy of the coordinate system except that the given axes are used
	 *         for it
	 */
	private static Object[] argsWithNewAxes(CoordinateSystem coords, NavigableSet<Axis> axes) {
		if (!coords.getClass().isRecord()) {
			throw new UnsupportedOperationException("Currently, only coordinate systems that arecords are supported.");
		}

		var components = coords.getClass().getRecordComponents();
		List<Object> args = new ArrayList<>();
		for (int j = 0; j < components.length; j++) {
			if (!"axes".equals(components[j].getName())) {
				try {
					args.add(components[j].getAccessor().invoke(coords));
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException("Cannot get " + components[j].getName(), e);
				}
			}
		}
		for (Axis axis : axes) {
			args.add(axis);
		}
		return args.toArray();
	}

    /**
	 * Finds the potential transformations of the given unit, i.e., by a simple
	 * change of the prefixes, if allowed.
	 * 
	 * @param unit the unit to check for alternative prefixes
	 * @return for each different prefix its corresponding unit and the function to
	 *         be applied to values to get to that unit. Note that the functions preserve the actual type, if possible.
	 */
	private static Map<Unit, UnaryOperator<Number>> potentialTransformations(Unit unit) {
		UnitInfo[] infos = Units.collectInfo(unit.symbols().get(0)).values().toArray(UnitInfo[]::new);

		int indexOfPrefixableUnit = -1;
		for (int i = 0; i < infos.length; i++) {
			if (mayChangePrefix(infos[i].unit())) {
				indexOfPrefixableUnit = i;
				break;
			}
		}

		if (indexOfPrefixableUnit == -1) {
			// so no prefixable unit found -> return old unit
			return Map.of(unit, UnaryOperator.identity());
		}

		Map<Unit, UnaryOperator<Number>> transformations = new HashMap<>();
		Set<UnitPrefix> allowedPrefixes = new HashSet<>(infos[indexOfPrefixableUnit].unit().prefixes());
		
		// make sure we have the identity prefix in there
		allowedPrefixes.add(Units.IDENTITY_PREFIX);
		
		for (UnitPrefix prefix : allowedPrefixes) {
			StringBuilder fullSymbol = new StringBuilder();
			List<Unit> extraUnits = new ArrayList<>();
			UnaryOperator<Number> func = null;
			for (int i = 0; i < infos.length; i++) {
				extraUnits.add(infos[i].unit());
				if (i == indexOfPrefixableUnit) {
				    Unit infoUnit = infos[i].unit();
				    UnaryOperator<Number> toBase = n -> convertNumber(n, in -> infoUnit.convertToBaseUnits(in.doubleValue()));
					if (infos[i].unit().isConversionLinear()) {
						UnitInfo ui = infos[i];
						Unit u = ui.unit();
						double factor = u.factor(ui.symbol());
						toBase = n -> convertNumber(n, in -> in.doubleValue() * factor);
					}

					// we need to find the symbol that allows to change the prefix, which might not
					// be the first (think of "kg")
					String symbol = null;
					for (String sym : infos[i].unit().symbols()) {
						if (infos[i].unit().prefixAllowed(sym)) symbol = sym;
					}
					symbol += infos[i].exponent() == 1 ? "" : "^" + infos[i].exponent();
					
					String symbolWithPrefix = prefix.symbols().get(0) + symbol;
					fullSymbol.append(symbolWithPrefix);
					Unit targetUnit = Unit.of(symbolWithPrefix, new Unit[] { infos[i].unit() });
					UnaryOperator<Number> finalToBase = toBase;
					UnaryOperator<Number> fromBase = n -> convertNumber(n, in -> targetUnit.convertFromBaseUnits(in.doubleValue()));
					func = n -> finalToBase.andThen(fromBase).apply(n);
				} else {
					String symbol = infos[i].symbol() + (infos[i].exponent() == 1 ? "" : "^" + infos[i].exponent());
					fullSymbol.append(symbol);
				}
				fullSymbol.append(" ");
			}
			
			transformations.put(Unit.of(fullSymbol.toString(), extraUnits.toArray(Unit[]::new)), func);
		}

		return Collections.unmodifiableMap(transformations);
	}

	private static <T extends Number> T convertNumber(T t, ToDoubleFunction<T> conversion) {
	    double conversionResult = conversion.applyAsDouble(t);
	    
	    if (t instanceof Double) {
            @SuppressWarnings("unchecked")
            T ret = (T) Double.valueOf(conversionResult);
            return ret;
        } else if (t instanceof Integer) {
            long roundedResult = Math.round(conversionResult);
            if (roundedResult > Integer.MAX_VALUE) {
                throw new ArithmeticException("Rounding result is not within Integer range!");
            }
            @SuppressWarnings("unchecked")
            T ret = (T) Integer.valueOf((int) roundedResult);
            return ret;
        } else if (t instanceof Long) {
            @SuppressWarnings("unchecked")
            T ret = (T) Long.valueOf(Math.round(conversionResult));
            return ret;
        } else if (t instanceof Float) {
            @SuppressWarnings("unchecked")
            T ret = (T) Float.valueOf((float) conversionResult);
            return ret;
        } else if (t instanceof Short) {
            long roundedResult = Math.round(conversionResult);
            if (roundedResult > Short.MAX_VALUE) {
                throw new ArithmeticException("Rounding result is not within Short range!");
            }
            @SuppressWarnings("unchecked")
            T ret = (T) Short.valueOf((short) conversionResult);
            return ret;
        } else if (t instanceof Byte) {
            long roundedResult = Math.round(conversionResult);
            if (roundedResult > Byte.MAX_VALUE) {
                throw new ArithmeticException("Rounding result is not within Short range!");
            }
            @SuppressWarnings("unchecked")
            T ret = (T) Byte.valueOf((byte) conversionResult);
            return ret;
        } else if (t instanceof BigDecimal) {
            @SuppressWarnings("unchecked")
            T ret = (T) new BigDecimal(String.valueOf(conversionResult));
            return ret;
        } else if (t instanceof BigInteger) {
            @SuppressWarnings("unchecked")
            T ret = (T) new BigInteger(String.valueOf(conversionResult));
            return ret;
        }
	    
	    throw new AssertionError("Unhandled Number!");
	}

	/**
	 * Checks whether any of the symbols of a unit allows prefixes. Note that this
	 * method covers the edge case of {@link eu.hoefel.unit.si.SiBaseUnit.KILOGRAM
	 * kilogram}, of which the primary reference symbol happens to be ""kg" (which
	 * contains already a prefix) and not "g".
	 * 
	 * @param unit the unit to check
	 * @return true if any of the symbols allows a prefix
	 */
	private static boolean mayChangePrefix(Unit unit) {
		return unit.symbols().stream().anyMatch(unit::prefixAllowed);
	}

	default Quantifiable<T> inContext(UnitContext context) {
		// change axes? only change name of quantity?
		return null;
	}
	
	
	
	
	public static void main(String[] args) {
        var bla = new Quantity1D<BigDecimal>(new BigDecimal[] { new BigDecimal("30.75") }, Unit.of("kg"));
        System.out.println(bla.reduce(1));
        bla.value();
        
    }
}
