package eu.hoefel.quantity;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.coordinates.axes.Axes;
import eu.hoefel.coordinates.axes.Axis;
import eu.hoefel.unit.Unit;
import eu.hoefel.unit.UnitInfo;
import eu.hoefel.unit.UnitPrefix;
import eu.hoefel.unit.Units;
import eu.hoefel.utils.Maths;

public sealed interface Quantifiable<T> {
	
	public record Quantity0D(String name, Double value, Unit unit) implements Quantifiable<Double> {
		
		public Quantity0D {
			Objects.requireNonNull(name);
			Objects.requireNonNull(value);
			Objects.requireNonNull(unit);
		}

		public Quantity0D(double value, Unit unit) {
			this("", value, unit);
		}

		@Override
		public CoordinateSystem coords() {
			return new CartesianCoordinates(new Axis(unit));
		}

		@Override
		public Unit axis(int dimension) {
			return unit();
		}
	}

	public record Quantity1D(String name, double[] value, CoordinateSystem coords) implements Quantifiable<double[]> {

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

		public Quantity1D(double[] value, Unit unit) {
			this("", value, new CartesianCoordinates(1, new Axis(unit)));
		}
	}
	
	public record Quantity2D(String name, double[][] value, CoordinateSystem coords) implements Quantifiable<double[][]> {

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

		public Quantity2D(double[][] value, CoordinateSystem coords) {
			this("", value, coords);
		}

		public Quantity2D(double[][] value, Unit... units) {
			this(value, new CartesianCoordinates(value.length, Axes.withUnits(units)));
		}
	}
	
	public T value();

	public CoordinateSystem coords();
	
	default Unit axis(int dimension) {
		return coords().axis(dimension).unit();
	}
	
	// TODO:
	// - reduce : ~6h
	// - add/subtract/mul/divide/pow/root? : ~10h
	// - putInContext(UnitContext);/where to put names? axis? : ~6h
	
	default Quantifiable<T> reduce(double... targets) {
		Objects.requireNonNull(targets);
		
		if (targets.length != coords().dimension() && targets.length != 1) {
			// if targets.length == 1 we assume it is meant to be the default dimension
			throw new IllegalArgumentException(
					"The number of targets (%d) does not fit to the necessarily needed dimensions (%d)"
							.formatted(targets.length, coords().dimension()));
		}

		Map<Integer, Function<double[],Double>> costFunctions = new HashMap<>();
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

	private static Double calculateCostForArray(double[] t, double target) {
		int targetExponent = Maths.getBase10Exponent(target);
		double targetMantissa = target / Math.pow(10, targetExponent);
		
		double[] costs = new double[t.length];
		for (int i = 0; i < costs.length; i++) {
			costs[i] = calculateSimpleCostForSinglePoint(t[i], targetMantissa, targetExponent);
		}
		return Maths.max(costs);
	}
	
	
	private static double calculateSimpleCostForSinglePoint(double value, double targetMantissa, int targetExponent) {
		// always between 0 and 308
		int exponent = Math.abs(targetExponent - Maths.getBase10Exponent(value));
		
		// always between 1.0 and 9.99...
		double mantissa = Math.abs(targetMantissa - value / Math.pow(10, exponent));
		
		return 0.1*mantissa + exponent;
	}

	@SuppressWarnings("unchecked")
	private Quantifiable<T> reduce(Map<Integer, Function<double[],Double>> costFunctions) {
		// TODO once pattern matching is in replace this with an (exhaustive) switch
		if (this instanceof Quantity0D q0d) {
			return (Quantifiable<T>) reduce0D(q0d, costFunctions);
		} else if (this instanceof Quantity1D q1d) {
			return (Quantifiable<T>) reduce1D(q1d, costFunctions);
		} else if (this instanceof Quantity2D q2d) {
			return (Quantifiable<T>) reduce2D(q2d, costFunctions);
		}
		throw new AssertionError("Unsupported quantifiable!");
	}

	private static Quantity0D reduce0D(Quantity0D quantity, Map<Integer, Function<double[],Double>> costFunctions) {
		Unit unit = quantity.unit();
		var trafos = potentialTransformations(unit);
		double cost = Double.POSITIVE_INFINITY;
		Double transformedValues = quantity.value;
		
		for (var trafo : trafos.entrySet()) {
			Double proposedTransformedValues = trafo.getValue().apply(quantity.value());
			double proposedCost = costFunctions.getOrDefault(0, costFunctions.get(Axes.DEFAULT_DIMENSION))
					.apply(new double[] { proposedTransformedValues });
			if (proposedCost < cost) {
				transformedValues = proposedTransformedValues;
				unit = trafo.getKey();
				cost = proposedCost;
			}
		}
		
		return new Quantity0D(quantity.name(), transformedValues, unit);
	}

	private static Quantity1D reduce1D(Quantity1D quantity, Map<Integer, Function<double[],Double>> costFunctions) {
		CoordinateSystem coords = quantity.coords();
		int dimension = coords.dimension();
		if (dimension == 1) {
			// n values for 1D
			return reduceN0DPoints(quantity, costFunctions);
		}
		return reduceOneNDPoint(quantity, costFunctions);
	}

	public static Quantity1D reduceN0DPoints(Quantity1D quantity, Map<Integer, Function<double[],Double>> costFunctions) {
		CoordinateSystem coords = quantity.coords();
		Unit unit = coords.axis(0).unit();
		var trafos = potentialTransformations(unit);
		double cost = Double.POSITIVE_INFINITY;
		double[] transformedValues = quantity.value().clone();
		
		for (var trafo : trafos.entrySet()) {
			double[] proposedTransformedValues = quantity.value().clone();
			for (int i = 0; i < transformedValues.length; i++) {
				proposedTransformedValues[i] = trafo.getValue().apply(quantity.value()[i]);
			}
			double proposedCost = costFunctions.getOrDefault(0, costFunctions.get(Axes.DEFAULT_DIMENSION))
					.apply(proposedTransformedValues);
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
		
		return new Quantity1D(quantity.name(), transformedValues, CoordinateSystem.from(coordSymbol, coordClass, args));
	}

	private static Quantity1D reduceOneNDPoint(Quantity1D quantity, Map<Integer, Function<double[],Double>> costFunctions) {
		CoordinateSystem coords = quantity.coords();
		int dimension = coords.dimension();

		List<Axis> axes = new ArrayList<>();
		double[] vals = quantity.value().clone();
		for (int i = 0; i < dimension; i++) {
			Unit unit = coords.axis(i).unit();
			double cost = Double.POSITIVE_INFINITY;

			var trafos = potentialTransformations(unit);
			for (var trafo : trafos.entrySet()) {
				Double proposedTransformedValues = trafo.getValue().apply(quantity.value()[i]);
				double proposedCost = costFunctions.getOrDefault(i, costFunctions.get(Axes.DEFAULT_DIMENSION))
						.apply(new double[] { proposedTransformedValues });
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
		
		return new Quantity1D(quantity.name(), vals, CoordinateSystem.from(coordSymbol, coordClass, args));
	}

	private static Quantity2D reduce2D(Quantity2D quantity, Map<Integer, Function<double[],Double>> costFunctions) {
		CoordinateSystem coords = quantity.coords();
		int dimension = coords.dimension();

		List<Axis> axes = new ArrayList<>();
		double[][] vals = Maths.deepCopyPrimitiveArray(quantity.value());
		for (int i = 0; i < dimension; i++) {
			Unit unit = coords.axis(i).unit();
			double cost = Double.POSITIVE_INFINITY;

			var trafos = potentialTransformations(unit);
			for (var trafo : trafos.entrySet()) {
				double[] proposedTransformedValues = quantity.value()[i].clone();
				for (int j = 0; j < vals[i].length; j++) {
					proposedTransformedValues[j] = trafo.getValue().apply(proposedTransformedValues[j]);
				}
				double proposedCost = costFunctions.getOrDefault(i, costFunctions.get(Axes.DEFAULT_DIMENSION))
						.apply(proposedTransformedValues);
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
		
		return new Quantity2D(quantity.name(), vals, CoordinateSystem.from(coordSymbol, coordClass, args));
	}

	private static Object[] argsWithNewAxes(CoordinateSystem coords, NavigableSet<Axis> axes) {
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

	private static Map<Unit, Function<Double,Double>> potentialTransformations(Unit unit) {
		UnitInfo[] infos = Units.collectInfo(unit.symbols().get(0));

		int indexOfPrefixableUnit = -1;
		for (int i = 0; i < infos.length; i++) {
			if (mayChangePrefix(infos[i])) {
				indexOfPrefixableUnit = i;
				break;
			}
		}

		if (indexOfPrefixableUnit == -1) {
			// so no prefixable unit found -> return old unit
			return Map.of(unit, Function.identity());
		}

		Map<Unit, Function<Double,Double>> transformations = new HashMap<>();
		Set<UnitPrefix> allowedPrefixes = new HashSet<>(infos[indexOfPrefixableUnit].unit().prefixes());
		
		// make sure we have the identity prefix in there
		allowedPrefixes.add(Units.IDENTITY_PREFIX);
		
		for (UnitPrefix prefix : allowedPrefixes) {
			StringBuilder fullSymbol = new StringBuilder();
			List<Unit> extraUnits = new ArrayList<>();
			Function<Double,Double> func = null;
			for (int i = 0; i < infos.length; i++) {
				extraUnits.add(infos[i].unit());
				if (i == indexOfPrefixableUnit) {
					Function<Double,Double> toBase = infos[i].unit()::convertToBaseUnits;
					if (infos[i].unit().canUseFactor()) {
						UnitInfo ui = infos[i];
						Unit u = ui.unit();
						toBase = value -> value * u.factor(ui.symbol());
					} else {
						toBase = infos[i].unit()::convertToBaseUnits;
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
					func = toBase.andThen(targetUnit::convertFromBaseUnits);
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

	private static boolean mayChangePrefix(UnitInfo info) {
		Unit unit = info.unit();
		for (String symbol : unit.symbols()) {
			if (unit.prefixAllowed(symbol)) {
				return true;
			}
		}
		return false;
	}
}
