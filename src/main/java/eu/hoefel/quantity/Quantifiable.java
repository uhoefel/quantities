package eu.hoefel.quantity;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.coordinates.axes.Axis;
import eu.hoefel.unit.Unit;
import eu.hoefel.unit.UnitInfo;
import eu.hoefel.unit.UnitPrefix;
import eu.hoefel.unit.Units;
import eu.hoefel.utils.Maths;

public sealed interface Quantifiable<T> permits Quantity0D, Quantity1D, Quantity2D, Quantity3D {
	
	public T value();

	public CoordinateSystem coords();
	
	default Unit axis(int dimension) {
		return coords().axis(dimension).unit();
	}
	
	// TODO:
	// - reduce : ~6h
	// - add/subtract/mul/divide/pow/root? : ~10h
	// - putInContext(UnitContext); : ~6h

	default Quantifiable<T> reduce(BiFunction<double[],Double,Double> costFunction, double... target) {
		Objects.requireNonNull(target);
		
		double val = costFunction.apply(value()[i], target[0]);
		
		int dim = coords().dimension();
		for (int i = 0; i < dim; i++) {
			Unit u = axis(i);
			
		}
		
	}
	
	public static void main(String[] args) {
		double[] vals = {1,2,3,4};
		Quantifiable<double[]> bla = new Quantity1D(vals, Unit.of("m s^-2"));
		bla.reduce(null, null);
		bla.coords().axis(0).unit().baseUnits();
		
//		bla.coords().christoffelSymbol2ndKind(bla.value(), 1,1,1);

		System.out.println(bla);
	}
	
//	private Quantifiable<T> reduce(double value, Unit unit, double target) {
//		int targetExponent = Maths.getBase10Exponent(target);
//		double targetMantissa = target / Math.pow(10, targetExponent);
//		
//		int exponent = Maths.getBase10Exponent(value);
//		double mantissa = Math.abs(targetMantissa - value / Math.pow(10, exponent));
//
//		exponent = Math.abs(targetExponent - exponent);
//		
//		String fullSymbol = unit.symbols().get(0);
//		
//		UnitInfo[] infos = Units.collectInfo(fullSymbol);
//		if (infos.length > 1) throw new RuntimeException();
//		
//		Unit baseUnit = infos[0].unit();
//		String symbol = infos[0].symbol() + (infos[0].exponent() == 0 ? "" : "^" + infos[0].exponent());
//		
//		Unit unitWithoutPrefix = Unit.of(symbol, new Unit[] { baseUnit });
//		value = unit.convertToBaseUnits(value);
//
//		Unit returnUnit = unit;
//		Set<UnitPrefix> allowedPrefixes = unit.prefixes();
//		for (UnitPrefix prefix : allowedPrefixes) {
//			String prefixSymbol = prefix.symbols().get(0);
//			if (unitWithoutPrefix.prefixAllowed(prefixSymbol)) {
//				Unit potentialUnit = Unit.of(prefixSymbol + symbol, new Unit[] { unitWithoutPrefix });
//				double potentialValue = Units.convert(value, unitWithoutPrefix, potentialUnit);
//				int potentialExponent = Maths.getBase10Exponent(potentialValue);
//				double potentialMantissa =  Math.abs(targetMantissa - potentialValue / Math.pow(10, potentialExponent));
//				potentialExponent = Math.abs(targetExponent - potentialExponent);
//				if (potentialExponent < exponent || (potentialExponent == exponent && potentialMantissa < mantissa)) {
//					exponent = potentialExponent;
//					mantissa = potentialMantissa;
//					returnUnit = potentialUnit;
//				}
//			}
//		}
//		return returnUnit;
//	}
}
