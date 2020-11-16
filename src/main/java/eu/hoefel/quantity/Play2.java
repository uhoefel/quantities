package eu.hoefel.quantity;

public class Play2 {
    public static void main(String[] args) {
        Quantifiable<Float> q = new QuantityImpl<>(3.0f);
        System.out.println(q instanceof Quantifiable);
    }
}