package eu.hoefel.quantity;

public sealed interface Quantity<T extends Number> permits Quantity0D, QuantityMax1D {

}
