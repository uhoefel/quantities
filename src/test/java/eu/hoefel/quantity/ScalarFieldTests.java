package eu.hoefel.quantity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.DoubleStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.quantity.function.MethodReferenceResolver.SerializableFunction;
import eu.hoefel.unit.Unit;

/** Tests for {@link ScalarField}. */
class ScalarFieldTests {
    
    @DisplayName("check input validation")
    @Test
    void testInputValidation() {
        var coords1D = new CartesianCoordinates(1);
        var coords2D = new CartesianCoordinates(2);
        assertThrows(NullPointerException.class, () -> new ScalarField<Double>(null, val -> val * 2, coords1D, coords1D));
        assertThrows(NullPointerException.class, () -> new ScalarField<Double>("name", null, coords1D, coords1D));
        assertThrows(NullPointerException.class, () -> new ScalarField<Double>("name", val -> val * 2, null, coords1D));
        assertThrows(NullPointerException.class, () -> new ScalarField<Double>("name", val -> val * 2, coords1D, null));
        
        assertThrows(IllegalArgumentException.class, () -> new ScalarField<Double>("name", val -> val * 2, coords1D, coords2D));
        
        assertThrows(IllegalArgumentException.class, () -> new ScalarField<Float>("name", val -> val * 2., coords1D, coords1D));
        assertThrows(IllegalArgumentException.class, () -> new ScalarField<Double>("name", val -> val * 2., coords2D, coords1D));
    }

    @DisplayName("approach")
    @Test
    void testApproach() {
        var scalarField = new ScalarField<Double>("magnitude of B", val -> 2 * val, new CartesianCoordinates(1), new CartesianCoordinates(1));
        
        var q0d = new Quantity0D(12, Unit.of("mm"));
        assertArrayEquals(new double[] {0.024}, scalarField.apply(q0d).value());
        
        var q1d = new Quantity1D(new double[] {1,5,7}, Unit.of("m"));
        assertArrayEquals(new double[] {2,10,14}, scalarField.apply(q1d).value());
        
        SerializableFunction<double[], Double> field2 = o -> DoubleStream.of(o).map(val -> ((double) val) * 1.5).sum();
        var scalarField2 = new ScalarField<double[]>("magnitude of B", field2, new CartesianCoordinates(2), new CartesianCoordinates(1));
        
        Quantity2D q2d = new Quantity2D(new double[][] {{1,5},{3,5}}, Unit.of("m"), Unit.of("m"));
        assertArrayEquals(new double[] {9,12}, scalarField2.apply(q2d).value());
    }
}
