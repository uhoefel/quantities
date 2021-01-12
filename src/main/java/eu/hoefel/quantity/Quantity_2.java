package eu.hoefel.quantity;

import java.util.Arrays;
import java.util.stream.Stream;

import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.unit.Unit;
import eu.hoefel.unit.special.NaturalUnit;

/**
 * This record is useful for holding information about
 * 
 * <p>
 * <b>BE AWARE:</b>
 * <em>This will be reworked once project <a href="">Valhalla</a> is ready and
 * delivered specialised generics, which should enhance performance
 * substantially. The required changes might be backwards incompatible.</em>
 * <p>
 * 
 * 
 * 
 * @param <T>   the type of the stored values
 * @param name  the name associated with this record, e.g. "wavelength
 *              $\\lambda_{\\mathrm{e}}$". This is purely descriptional (and
 *              should be concise), though it <em>cannot</em> be relied upon
 *              that a specific format is used. It is intended to be used e.g.
 *              for plots.
 * @param value the numerical value of this quantity. Currently limited to
 *              double[], future releases of Java with reified generics should
 *              help here a lot. Also TODO, one can probably support integers,
 *              long, short, float by extending Unit and CoordinateSystem,
 *              although they would massively profit from reified generics
 * @param coord the coordinate system
 */
public final record Quantity_2 <T extends Number> (String name, T value, CoordinateSystem coord) {

	// TODO This should be an inline class
	
	// maybe add (sealed) combining interfaces like QuantityMax1D (not a good name)
	// that would be implemented by Quantity0D and Quantity1D. This would allow to
	// easily make dependencies that take either 0d or 1d
	
	// Quantity0D - maybe w/o coord system?
	// Quantity1D
	// Quantity2D
	// Quantity3D
	// I don't think we need more
	// 

	/**
	 * Creates a new quantity with the given name, value and units.
	 * 
	 * @param name  the name associated with this record, e.g. "wavelength
	 *              $\\lambda_{\\mathrm{e}}$". This is purely descriptional (and
	 *              should be concise), though it <em>cannot</em> be relied upon
	 *              that a specific format is used. It is intended to be used e.g.
	 *              for plots.
	 * @param value the numerical value of this quantity. Note that coordinate
	 *              transformations for single values will usually not work.
	 * @param units the unit(s) (cf {@link Unit})
	 */
	public Quantity_2(String name, double value, String units) {
		checkUnits(units);
		this.name = name;
		this.value = (T) (Double) value; // :-(
		this.units = units;
		this.coord = CoordinateSystem.emptyCoordinateSystem();
		this.extraUnits = new Unit[0];
	}

	/**
	 * Creates a new quantity with the given value and units.
	 * 
	 * @param value the numerical value of this quantity. Note that coordinate
	 *              transformations for single values will usually not work.
	 * @param units the unit(s) (cf {@link Unit})
	 */
	public Quantity_2(double value, String units) {
		checkUnits(units);
		this.name = "";
		this.value = (T) (Double) value; // :-(
		this.units = units;
		this.coord = CoordinateSystem.emptyCoordinateSystem();
		this.extraUnits = new Unit[0];
	}

	/**
	 * Creates a new quantity with the given name, value and unit.
	 * 
	 * @param name  the name associated with this record, e.g. "wavelength
	 *              $\\lambda_{\\mathrm{e}}$". This is purely descriptional (and
	 *              should be concise), though it <em>cannot</em> be relied upon
	 *              that a specific format is used. It is intended to be used e.g.
	 *              for plots.
	 * @param value the numerical value of this quantity. Note that coordinate
	 *              transformations for single values will usually not work.
	 * @param unit  the unit
	 */
	public Quantity_2(String name, double value, Unit unit) {
		this.name = name;
		this.value = (T) (Double) value; // :-(
		this.units = unit.symbols().get(0);
		this.coord = CoordinateSystem.emptyCoordinateSystem();
		this.extraUnits = new Unit[] { unit };
	}


	
	public Quantity_2 <T> to(Unit unit) {
		if (coord != CoordinateSystem.emptyCoordinateSystem()) {
			for (int i = 0; i < coord.units().length; i++) {
				if (!Unit.convertible(coord.units()[i], unit, coord.extraUnits())) {
					throw new IllegalArgumentException("Cannot convert axis " + (i + 1) + " with unit " + coord.units()[i] + " to " + unit);
				}
			}
			// TODO convert values
			
			return new Quantity_2<T>(name(), newValue, coord.inUnitsOf(unit));
		} else if (value().getClass() != double[][].class) {
			throw new UnsupportedOperationException("currently, only 2d double arrays are supported");
		} else {
			double[][] newValue = Unit.convert((double[][]) value(), units(), unit, extraUnits());
			return new Quantity_2<T>(name(), newValue, units, extraUnits);
		}
	}

	public static void main(String[] args) {
		// so in the Nodes it should look like this:
		double[][] vec = new double[][] { { 3.0 }, { 2 }, { 1 } };

		var quant = new Quantity_2<double[][]>(vec, "nm", Coords.CARTESIAN);
		
		// we need it in cylindrical natural units
		var newQuant = quant.to(NaturalUnit.OF_LENGTH, Coords.CYLINDRICAL);

		System.out.println(newQuant);
		System.out.println(Arrays.deepToString(newQuant.value()));
		
	}
}
