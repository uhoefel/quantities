package eu.hoefel.quantity;

public record QuantityImpl<T extends Number>(T value) implements Quantifiable<T> { }
