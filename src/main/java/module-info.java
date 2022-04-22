/**
 * This module provides support for quantities built upon {@link eu.hoefel.unit}
 * and {@link eu.hoefel.coordinates}. Note that there is a conceptual difference
 * to (almost?) all other quantity libraries not only in Java: usually only the
 * unit information is used in the quantity, the quantities defined in here also
 * convey information about the coordinate system, providing richer metadata
 * while still being reasonably easy to use and being able to handle gracefully
 * missing coordinate system information for the most part.
 * <p>
 * The {@link eu.hoefel.quantity} is the package containing the base classes
 * needed for the generation of quantities. It depends on the
 * {@link eu.hoefel.quantity.function} package.
 * <p>
 * The {@link eu.hoefel.quantity.function} is the package containing helper
 * classes for dealing automatically with the serialization of the functions
 * used for the scalar and vector fields.
 * 
 * @author Udo Hoefel
 */
module eu.hoefel.quantity {
    exports eu.hoefel.quantity;
    exports eu.hoefel.quantity.function;

    requires transitive eu.hoefel.coordinates;
    requires transitive eu.hoefel.unit;
    requires transitive eu.hoefel.jatex;

    requires eu.hoefel.utils;
}