package eu.hoefel.quantity;

import java.util.Objects;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.coordinates.axes.Axis;
import eu.hoefel.unit.Unit;

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
