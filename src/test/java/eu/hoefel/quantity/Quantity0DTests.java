package eu.hoefel.quantity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.hoefel.coordinates.CartesianCoordinates;
import eu.hoefel.unit.Unit;

/**
 * Tests for Quantity0D.
 * 
 * @author Udo
 */
class Quantity0DTests {

    @DisplayName("check input validation")
    @Test
    void testInputValidation() {
        assertThrows(NullPointerException.class, () -> new Quantity0D(null, 12., new CartesianCoordinates(1)));
        assertThrows(NullPointerException.class, () -> new Quantity0D("name", null, new CartesianCoordinates(1)));
        assertThrows(NullPointerException.class, () -> new Quantity0D("name", 12., null));
    }
    
    @DisplayName("apply")
    @Test
    void testApply() {
        var quantity = new Quantity0D(-3.0, Unit.of("m"));
        var quantityWithAppliedFunction = quantity.apply(Math::abs);
        assertEquals(quantity.name(), quantityWithAppliedFunction.name());
        assertEquals(quantity.coords(), quantityWithAppliedFunction.coords());
        assertEquals(-3.0, quantity.value());
        assertEquals(3.0, Math.abs(quantity.value()));
        assertEquals(3.0, quantityWithAppliedFunction.value());
    }

    @DisplayName("approach")
    @Test
    void testApproach() {
        var quantity = new Quantity0D(1024, Unit.of("kg"));
        var quantityApproaching1 = quantity.approach(1);
        assertEquals(1.024, quantityApproaching1.value());
        assertEquals("Mg", quantityApproaching1.axis(0).unit().symbols().get(0));
        
        var quantityApproaching1024 = quantityApproaching1.approach(1024);
        assertEquals(1024, quantityApproaching1024.value());
        assertEquals("kg", quantityApproaching1024.axis(0).unit().symbols().get(0));
        
        quantity = new Quantity0D(3.0e-2, Unit.of("m^2"));
        quantityApproaching1 = quantity.approach(1);
        assertEquals(3, quantityApproaching1.value(), 1e-15);
        assertEquals("dm^2", quantityApproaching1.axis(0).unit().symbols().get(0));
        
        quantity = new Quantity0D(3.0e2, Unit.of("m^-2"));
        quantityApproaching1 = quantity.approach(1);
        assertEquals(3, quantityApproaching1.value(), 1e-15);
        assertEquals("dm^-2", quantityApproaching1.axis(0).unit().symbols().get(0));
    }
}
