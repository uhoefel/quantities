package eu.hoefel.quantity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.unit.Unit;

/** Tests for {@link Quantity2D}. */
class Quantity2DTests {
    
    @DisplayName("check input validation")
    @Test
    void testInputValidation() {
        assertThrows(NullPointerException.class, () -> new Quantity2D(null, new double[][] {{ 12. }}, new CartesianCoordinates(1)));
        assertThrows(NullPointerException.class, () -> new Quantity2D("name", null, new CartesianCoordinates(1)));
        assertThrows(NullPointerException.class, () -> new Quantity2D("name", new double[][] {{ 12. }}, (CoordinateSystem) null));
    }

    @DisplayName("approach")
    @Test
    void testApproach() {
        var quantity = new Quantity2D("", new double[][] {{1e-19, 2e0, 3e3, 4e12}}, CoordinateSystem.from("cart", 4));
        var quantityApproaching1 = quantity.approach(1);
        assertArrayEquals(new double[][] {{0.09999999999999999,2.0,2.9296875,3.637978807091713}}, quantityApproaching1.value());
        assertEquals(List.of("am"),  quantityApproaching1.axis(0).unit().symbols());
        assertEquals(List.of("m"),   quantityApproaching1.axis(1).unit().symbols());
        assertEquals(List.of("kim"), quantityApproaching1.axis(2).unit().symbols());
        assertEquals(List.of("Tim"), quantityApproaching1.axis(3).unit().symbols());
    }

    @DisplayName("from")
    @Test
    void testFrom() {
        var q0 = new Quantity1D("length of ruler", new double[] { 12 }, Unit.of("m"));
        var q1 = new Quantity1D(new double[] { 22 }, Unit.of("m"));
        var q2 = new Quantity1D(new double[] { 45122 }, Unit.of("nm"));
        var q3 = new Quantity1D(new double[] { 0.0002 }, Unit.of("Gm"));
        
        var result = assertDoesNotThrow(() -> Quantity2D.from(q0, q1, q2, q3));
        assertEquals("length of ruler", result.name());
        assertArrayEquals(new double[][] { { 12 }, { 22 }, { 4.5122000000000006e-5 }, { 2e5 } }, result.value());
        assertEquals(Unit.of("m"), result.coords().axis(0).unit());

        assertThrows(NullPointerException.class, () -> Quantity2D.from((Quantity1D[]) null));
        
        assertThrows(NoSuchElementException.class, () -> Quantity2D.from(new Quantity1D[] { null }));
        assertThrows(NoSuchElementException.class, () -> Quantity2D.from());
        
        assertDoesNotThrow(() -> Quantity2D.from(null, q0, q1, q2, q3));
    }
}
