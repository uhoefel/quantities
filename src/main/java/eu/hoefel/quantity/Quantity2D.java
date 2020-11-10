package eu.hoefel.quantity;

public record Quantity2D<T extends Number>(T[][] value) implements QuantityMax2D<T> {

}
