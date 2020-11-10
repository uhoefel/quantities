package eu.hoefel.TODO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import eu.hoefel.science.units.Unit;
import eu.hoefel.science.units.Units;
import eu.hoefel.science.units.si.SiBaseUnit;
import eu.hoefel.science.units.si.SiCommonUnit;
import eu.hoefel.science.units.si.SiDerivedUnit;
import eu.hoefel.science.units.special.AtomicUnit;
import eu.hoefel.science.units.special.NaturalUnit;
import eu.hoefel.science.units.special.PlanckUnit;

/** 
 * Tests for everything related to the SI unit implementations.
 * 
 * @author Udo Hoefel
 */
@DisplayName("Generic SI tests")
@SuppressWarnings("javadoc")
class UnitTests {

	@DisplayName("Testing factor")
	@Test
	void testFactor() {
		assertEquals(0.001, Units.factor("m", "km"));
		assertEquals(0.001, Units.factor(SiBaseUnit.METER, Unit.of("km")));
		assertEquals(1000, Units.factor(Unit.of("km"), SiBaseUnit.METER));
	}

	@DisplayName("Testing convertibility")
	@Test
	void testConvertibility() {
		assertTrue(Units.convertible(SiBaseUnit.METER, SiCommonUnit.ANGSTROM));
		assertTrue(Units.convertible(Unit.of("m"), SiCommonUnit.ANGSTROM));
		assertTrue(Units.convertible(SiCommonUnit.ANGSTROM, Unit.of("m")));
		assertTrue(Units.convertible("Angstrom", "m"));
		assertTrue(Units.convertible("N", "N"));
		assertFalse(Units.convertible(null, "N"));
		assertFalse(Units.convertible(null, Unit.of("N")));

		// unknown units are fine, if they are identical on origin and target
		assertTrue(Units.convertible("s^-1 NoKnownUnit", "Hz NoKnownUnit"));
		assertTrue(Units.convertible("ADCU Angstrom", "ADCU m"));

		// not if they differ though
		assertFalse(Units.convertible("s^-1 NoKnownUnit", "Hz AnotherUnknownUnit"));
		assertFalse(Units.convertible("ADCU Angstrom", "AnotherADCU m"));
	}

	@DisplayName("Testing equivalence")
	@Test
	void testEquivalence() {
		assertTrue(Units.equivalent("kg m s^-2", "N"));
	}

	@DisplayName("Testing simplification")
	@Test
	void testSimplification() {
		assertEquals("N", Units.simplify("kg m s^-2"));
		assertEquals("J", Units.simplify("kg m^2 s^-2"));
		
		assertEquals("J m", Units.simplify("kg m^3 s^-2"));

		// complicated case I
		assertEquals("N Wb", Units.simplify("kg^2 m^3 s^-4 A^-1"));

		// complicated case II
		assertEquals("J^2 T", Units.simplify("kg^3 m^4 s^-6 A^-1"));

		// no simplification possible -> return input
		assertEquals("nm", Units.simplify("nm"));
		
		// check canceling
		long bla = System.nanoTime();
		assertEquals("N Wb", Units.simplify("kg^2 m^3 s^-4 A^-1 A^-2 A^2"));
		System.out.println("took: " + (System.nanoTime() - bla));
		assertEquals("", Units.simplify("A^-2 A^2"));
		assertEquals("", Units.simplify("t t^-1"));
		assertEquals("Sv", Units.simplify("Sv A^-2 A^2"));
		
		bla = System.nanoTime();
		assertEquals("N Wb", Units.simplify("kg^2 m^3 s^-4 A^-1 A^-2 A^2"));
		System.out.println("took: " + (System.nanoTime() - bla));
	}

	@DisplayName("Testing SI conversions")
	@Test
	void testConversion() {
		assertEquals(3e9, Units.convert(3.0, "°C MN m", "°C mJ"));
		assertEquals(3e9, Units.convert(3, "MN m", "mJ"));
		assertEquals(3e12, Units.convert(3, "MN m^2 mm^-1", "mJ"));
		assertEquals(9.2570327444741e26, Units.convert(3, "pc", "Angstrom"));
		assertEquals(3.261563777167433, Units.convert(1, "pc", "ly"));
		assertEquals(5.566789756301004e9, Units.convert(0, "°C^4", "K^4"));
		assertEquals(273.15, Units.convert(0, "°C", "K"));
		assertEquals(890.8961457332842, Units.convert(0, "m°C pc", "K ly"));
		assertEquals(276.15, Units.convert(3, "°C^4", "K °C^3"));
		assertEquals(30, Units.convert(3, "nm", "Angstrom"));
		assertEquals(3e-6, Units.convert(3, "g", "Mg"));
		assertEquals(3e-3, Units.convert(3, "mg", "g"));
		assertEquals(2.7615e8, Units.convert(3, "°C g kg", "mK g^2"));
		assertEquals(273.15, Units.convert(0, "°C", "K"));
		assertEquals(1, Units.convert(8, "bit V^-1", "byte V^-1"));
		assertEquals(2.0, Units.convert(2.0, SiBaseUnit.METER, SiBaseUnit.METER));
		assertEquals(2.0, Units.convert(2.0, SiDerivedUnit.DEGREE_CELSIUS, SiDerivedUnit.DEGREE_CELSIUS));
		assertEquals(274.15, Units.convert(1.0, SiDerivedUnit.DEGREE_CELSIUS, SiBaseUnit.KELVIN));
		assertEquals(1.0, Units.convert(274.15, SiBaseUnit.KELVIN, SiDerivedUnit.DEGREE_CELSIUS));

		assertEquals(0.007297352569281204, Units.convert(1.0, NaturalUnit.OF_LENGTH, AtomicUnit.OF_LENGTH));
	}

	@DisplayName("Testing illegal arguments")
	@Test
	void testIllegalArguments() {
		// TODO asser error msg
		assertThrows(IllegalArgumentException.class, () -> Units.convert(3.0, "mm^-1 °C m MAngstrom YBq Wb^-123", "km^2"), "Could convert to a noncompatible unit.");
		assertThrows(IllegalArgumentException.class, () -> Units.convert(0.0, SiBaseUnit.METER, Unit.of("not_a_real_unit")), "Could convert to a nonexisting unit.");
		assertThrows(IllegalArgumentException.class, () -> Units.convert(0.0, SiBaseUnit.METER, SiBaseUnit.AMPERE), "Could convert to a noncompatible unit.");
		assertThrows(IllegalArgumentException.class, () -> Units.factor(SiBaseUnit.METER, Unit.of("not_a_real_unit")), "Could convert to a nonexisting unit.");
		assertThrows(IllegalArgumentException.class, () -> Units.factor(SiBaseUnit.KELVIN, SiDerivedUnit.DEGREE_CELSIUS), "Could determine a factor to a unit that requires a shift.");
		assertThrows(IllegalArgumentException.class, () -> Units.factor(SiBaseUnit.KELVIN, SiBaseUnit.AMPERE), "Could determine a factor to a noncompatible unit.");
	}

	@DisplayName("Testing conversion (Unit -> String)")
	@Test
	void testUnitStringConversion() {
		assertEquals(1.6162e-35, Units.convert(1.0, PlanckUnit.PLANCK_LENGTH, Unit.of("m")), 1e-39);
	}

	@DisplayName("Testing conversion (String -> Unit)")
	@Test
	void testStringUnitConversion() {
		assertEquals(1/1.6162e-35, Units.convert(1.0, Unit.of("m"), PlanckUnit.PLANCK_LENGTH), 1e31);
	}

	@DisplayName("Testing composite units")
	@Test
	void testCompositeUnits() {
		assertFalse(Units.isValid("not_a_unit"));
		assertTrue(Units.isValid("kg^2"));
		assertTrue(Units.isValid("kg^2 s^-1"));
		assertEquals(273.151, Units.convert(1.0, Unit.of("m°C"), SiBaseUnit.KELVIN), 1e-13);
		assertEquals(-273.149, Units.convert(1.0, SiBaseUnit.KELVIN, Unit.of("m°C")));
		assertEquals(-270.15, Units.convert(3.0, Unit.of("K"), SiDerivedUnit.DEGREE_CELSIUS));
		assertEquals(-273.147, Units.convert(3.0, Unit.of("mK"), SiDerivedUnit.DEGREE_CELSIUS));
		assertEquals(3.0, Units.convert(3.0, Unit.of("A s"), SiDerivedUnit.COULOMB));
		assertEquals(0.003, Units.convert(3.0, Unit.of("A ms"), SiDerivedUnit.COULOMB));
		assertNotEquals(Unit.of("kg", SiBaseUnit.values()), Unit.of("kg", SiDerivedUnit.values()));
		assertNotEquals(Unit.of("kg", SiBaseUnit.values()), Unit.of("kg"));
		assertEquals(Unit.of("kg"), Unit.of("kg"));
		assertEquals(Unit.of("kg", Units.DEFAULT_UNITS.stream().toArray(Unit[]::new)), Unit.of("kg"));
	}
}
