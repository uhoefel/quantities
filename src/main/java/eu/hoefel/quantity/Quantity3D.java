package eu.hoefel.quantity;

public record Quantity3D<T extends Number>(T[][][] value) implements Quantity<T> {

}
