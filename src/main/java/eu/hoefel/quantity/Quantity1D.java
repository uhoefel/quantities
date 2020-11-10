package eu.hoefel.quantity;

public record Quantity1D<T extends Number>(T[] value) implements QuantityMax1D<T> {

}
