package eu.hoefel.quantity;

import java.util.Objects;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.coordinates.axes.Axis;
import eu.hoefel.unit.Unit;

public record Quantity3D(String name, double[][][] value, Unit unit) implements Quantifiable<double[][][]> {

	public Quantity3D {
		Objects.requireNonNull(name);
		Objects.requireNonNull(value);
		Objects.requireNonNull(unit);
	}

	public Quantity3D(double[][][] value, Unit unit) {
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
