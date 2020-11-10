///**
// * This module provides support for programmatic (generic) LaTeX document
// * creation, such as used for lab reports or letters.
// * <p>
// * The {@link eu.hoefel.jatex} is the package containing the base classes needed
// * for the document generation, as well as some convenience classes wrapping
// * often used LaTeX features, like e.g. {@link eu.hoefel.jatex.Table tables} and
// * {@link eu.hoefel.jatex.Equation equations}. It depends on the
// * {@link eu.hoefel.jatex.utility} package.
// * <p>
// * The {@link eu.hoefel.jatex.letter} is the package containing convenience
// * classes for the generation of letters. It depends on the
// * {@link eu.hoefel.jatex} package.
// * <p>
// * The {@link eu.hoefel.jatex.utility} is the package containing some methods
// * often used in the {@link eu.hoefel.jatex} package, which is the only package
// * it is exported to.
// * 
// * @author udoh
// */
module eu.hoefel.quantity {

	exports eu.hoefel.quantity;

	// ugh...
	opens eu.hoefel.quantity to org.junit.platform.commons;

	requires java.logging;

	requires org.junit.jupiter.api;
	
	requires eu.hoefel.utils;
	requires transitive eu.hoefel.jatex;
	requires transitive eu.hoefel.unit;
	requires transitive eu.hoefel.coordinates;
}