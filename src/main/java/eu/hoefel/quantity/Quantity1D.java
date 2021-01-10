package eu.hoefel.quantity;

import java.util.Objects;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.coordinates.axes.Axis;
import eu.hoefel.unit.Unit;

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
