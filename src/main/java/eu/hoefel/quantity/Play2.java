package eu.hoefel.quantity;

import java.util.Arrays;

import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.unit.Unit;

public class Play2 {
    public static void main(String[] args) {
//        Quantifiable<Double> q = new Quantity0D(null, 3.0, null);
//        System.out.println(q);
        
        Quantifiable<Double> q = new Quantifiable.Quantity0D(3.0e-1, Unit.of("m"));
        var q2 = q.reduce(1);
        System.out.println(q2.value());
        System.out.println(q2.axis(0).symbols());
        
        
        Quantifiable<double[]> q1d = new Quantifiable.Quantity1D(new double[] { 1e19, 2e19, 3e19, 4e19 }, Unit.of("kg m^-3"));
        var q1d2 = q1d.reduce(1);
        System.out.println(Arrays.toString(q1d2.value()));
        System.out.println(q1d2.axis(0).symbols());
        
        
        Quantifiable<double[][]> q2d = new Quantifiable.Quantity2D(new double[][] { {1e-19}, {2e0}, {3e3}, {4e12} }, CoordinateSystem.from("cart", 4));
        var q2d2 = q2d.reduce(1);
        System.out.println(Arrays.toString(q2d2.value()[0]));
        System.out.println(q2d2.axis(0).symbols());
        System.out.println(Arrays.toString(q2d2.value()[1]));
        System.out.println(q2d2.axis(1).symbols());
        System.out.println(Arrays.toString(q2d2.value()[2]));
        System.out.println(q2d2.axis(2).symbols());
        System.out.println(Arrays.toString(q2d2.value()[3]));
        System.out.println(q2d2.axis(3).symbols());
    }
}