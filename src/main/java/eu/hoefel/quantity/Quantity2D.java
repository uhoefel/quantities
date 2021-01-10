package eu.hoefel.quantity;

import java.util.Objects;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.coordinates.axes.Axes;
import eu.hoefel.unit.Unit;

public record Quantity2D(String name, double[][] value, CoordinateSystem coords) implements Quantifiable<double[][]> {

	public Quantity2D {
		Objects.requireNonNull(name);
		Objects.requireNonNull(value);
		Objects.requireNonNull(coords);
	}

	public Quantity2D(double[][] value, CoordinateSystem coords) {
		this("", value, coords);
	}

	public Quantity2D(double[][] value, Unit... units) {
		this(value, new CartesianCoordinates(value.length, Axes.withUnits(units)));
	}
}
