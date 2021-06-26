package eu.hoefel.quantity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.coordinates.CylindricalCoordinates;
import eu.hoefel.coordinates.axes.Axes;
import eu.hoefel.unit.Unit;

/** Tests for {@link Quantity1D}. */
class Quantity1DTests {
    
    @DisplayName("check input validation")
    @Test
    void testInputValidation() {
        assertThrows(NullPointerException.class, () -> new Quantity1D(null, new double[] { 12. }, new CartesianCoordinates(1)));
        assertThrows(NullPointerException.class, () -> new Quantity1D("name", null, new CartesianCoordinates(1)));
        assertThrows(NullPointerException.class, () -> new Quantity1D("name", new double[] { 12. }, (CoordinateSystem) null));
    }
    
    @DisplayName("apply")
    @Test
    void testApply() {
        // order 0
        double[] array         = {-3, 2, -5};
        double[] arrayAbsolute = { 3, 2,  5};
        var quantity = new Quantity1D(array, Unit.of("m"));
        var quantityWithAppliedFunction = quantity.apply(Math::abs);
        assertEquals(quantity.name(), quantityWithAppliedFunction.name());
        assertEquals(quantity.coords(), quantityWithAppliedFunction.coords());
        assertArrayEquals(array, quantity.value());
        assertArrayEquals(arrayAbsolute, quantityWithAppliedFunction.value());
        
        // order 1
        quantity = new Quantity1D(array, Unit.of("m"), Unit.of("mm"), Unit.of("pm"));
        quantityWithAppliedFunction = quantity.apply(Math::abs);
        assertEquals(quantity.name(), quantityWithAppliedFunction.name());
        assertEquals(quantity.coords(), quantityWithAppliedFunction.coords());
        assertArrayEquals(array, quantity.value());
        assertArrayEquals(arrayAbsolute, quantityWithAppliedFunction.value());
    }

    @DisplayName("to")
    @Test
    void testTo() {
        CoordinateSystem cart = new CartesianCoordinates(3, Axes.withUnits(Unit.of("mm")));
        CoordinateSystem cyl = new CylindricalCoordinates();
        
        var xyz = new Quantity1D("cartesian coords", new double[] {1,1,1}, cart);
        var rphiz = xyz.to("cylinder coords", cyl);
        
        // Note that the 0.001 on the z-axis is due to the original coordinate system
        // having mm as its unit there, while the cylindrical coordinates have m.
        assertArrayEquals(new double[] {0.001414213562373095, 0.7853981633974483, 0.001}, rphiz.value());
    }

    @DisplayName("approach")
    @Test
    void testApproach() {
        var quantity = new Quantity1D(new double[] { 1e18, 1e18, 1e18, 1e18 }, Unit.of("m^-3"));
        var quantityApproaching1 = quantity.approach(1);
        assertArrayEquals(new double[] {1,1,1,1}, quantityApproaching1.value(), 1e-15);
        assertEquals("μm^-3", quantityApproaching1.axis(0).unit().symbols().get(0));
        
        
        var quantityApproaching1e18 = quantityApproaching1.approach(1e18);
        assertArrayEquals(new double[] { 1e18, 1e18, 1e18, 1e18 }, quantityApproaching1e18.value(), 1e-15);
        assertEquals("m^-3", quantityApproaching1e18.axis(0).unit().symbols().get(0));
    }
}
