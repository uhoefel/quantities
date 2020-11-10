package eu.hoefel.quantity;

//public interface QuantityMax1D<T> extends Quantity<T>{
public sealed interface QuantityMax1D<T> extends Quantity<T> permits Quantity1D<T>, QuantityMax2D<T> {

}
