package eu.hoefel.quantity;

//public interface QuantityMax2D<T> extends QuantityMax1D<T> {
public sealed interface QuantityMax2D<T> extends QuantityMax1D<T> permits Quantity2D<T>, QuantityMax3D<T> {

}
