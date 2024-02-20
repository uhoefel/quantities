package eu.hoefel.quantity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.DoubleStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.quantity.function.MethodReferenceResolver.SerializableFunction;
import eu.hoefel.unit.Unit;

/** Tests for {@link VectorField}. */
@SuppressWarnings("javadoc")
class VectorFieldTests {

    @DisplayName("check input validation")
    @Test
    void testInputValidation() {
        SerializableFunction<Double, double[]> func = val -> new double[] { val * 2 };
        var coords1D = new CartesianCoordinates(1);
        var coords2D = new CartesianCoordinates(2);
        assertThrows(NullPointerException.class, () -> new VectorField<>(null, func, coords1D, coords2D));
        assertThrows(NullPointerException.class, () -> new VectorField<Double>("name", null, coords1D, coords2D));
        assertThrows(NullPointerException.class, () -> new VectorField<>("name", func, null, coords2D));
        assertThrows(NullPointerException.class, () -> new VectorField<>("name", func, coords2D, null));

        assertThrows(IllegalArgumentException.class, () -> new VectorField<>("name", func, coords1D, coords1D));

        assertThrows(IllegalArgumentException.class, () -> new VectorField<Float>("name", val -> new double[] { val * 2 }, coords1D, coords2D));
        assertThrows(IllegalArgumentException.class, () -> new VectorField<>("name", func, coords2D, coords2D));
    }

    @DisplayName("approach")
    @Test
    void testApproach() {
        var vectorField = new VectorField<Double>("magnitude of B", val -> new double[] { val, val * 2 }, new CartesianCoordinates(1), new CartesianCoordinates(2));

        var q0d = new Quantity0D(12, Unit.of("mm"));
        assertArrayEquals(new double[][] {{0.012,0.024}}, vectorField.apply(q0d).value());

        var q1d = new Quantity1D(new double[] {1,5,7}, Unit.of("m"));
        assertArrayEquals(new double[][] {{1,2},{5,10},{7,14}}, vectorField.apply(q1d).value());

        SerializableFunction<double[], double[]> field2 = o -> new double[] { 1, DoubleStream.of(o).map(val -> 1.5*val).sum() };
        var vectorField2 = new VectorField<>("magnitude of B", field2, new CartesianCoordinates(2), new CartesianCoordinates(2));

        Quantity2D q2d = new Quantity2D(new double[][] {{1,5},{3,5}}, Unit.of("m"), Unit.of("m"));
        assertArrayEquals(new double[][] {{1,9},{1,12}}, vectorField2.apply(q2d).value());
    }
}
