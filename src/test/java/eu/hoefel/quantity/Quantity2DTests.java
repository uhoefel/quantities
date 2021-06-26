package eu.hoefel.quantity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;

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
}
