package eu.hoefel.quantity;

import java.util.Arrays;

import eu.hoefel.coordinates.CoordinateSystem;
import eu.hoefel.unit.Unit;

public class Play2 {

    public static void main(String[] args) throws InterruptedException {
//        Quantifiable<Double> q = new Quantity0D(null, 3.0, null);
//        System.out.println(q);
        
    	int numRuns = 1;
    	
    	Thread.sleep(10_000);
    	
    	for (int i = 0; i < numRuns; i++) {
    		Quantifiable<Double> q = new Quantifiable.Quantity0D<>(3.0e-1, Unit.of("m"));
            var q2 = q.reduce(1);
//            System.out.println(q2.value());
//            System.out.println(q2.axis(0).unit().symbols());
            
            
            Quantifiable<Double[]> q1d = new Quantifiable.Quantity1D<>(new Double[] { 1e18, 1e18, 1e18, 1e18 }, Unit.of("m^-3"));
            var q1d2 = q1d.reduce(1);
            System.out.println(Arrays.toString(q1d2.value()));
            System.out.println(q1d2.axis(0).unit().symbols());
            
            
            Quantifiable<Double[][]> q2d = new Quantifiable.Quantity2D<>(new Double[][] { {1e-19}, {2e0}, {3e3}, {4e12} }, CoordinateSystem.from("cart", 4));
            var q2d2 = q2d.reduce(1);
//            System.out.println(Arrays.toString(q2d2.value()[0]));
//            System.out.println(q2d2.axis(0).unit().symbols());
//            System.out.println(Arrays.toString(q2d2.value()[1]));
//            System.out.println(q2d2.axis(1).unit().symbols());
//            System.out.println(Arrays.toString(q2d2.value()[2]));
//            System.out.println(q2d2.axis(2).unit().symbols());
//            System.out.println(Arrays.toString(q2d2.value()[3]));
//            System.out.println(q2d2.axis(3).unit().symbols());
		}
    }
}