package eu.hoefel.quantity;

public class Play {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		Quantity<Float> q = new Quantity0D<>(3.0f);
		
//		Quantity<Double[]> q = new Quantity1D<Double[]>(new Double[] {3.0});
		
		// pattern matching not yet implemented
//		switch (q) {
//		}
		
		Quantifiable<Float> q = new Quantity0D<>(3.0f);
		System.out.println(q instanceof Quantity0D);
		System.out.println(q instanceof QuantityMax3D);
		System.out.println(q instanceof QuantityMax2D);
		System.out.println(q instanceof QuantityMax1D);
		
		Quantifiable<Float> q2 = new Quantity1D<>(new Float[] { 3.0f });
		
		System.out.println(q2 instanceof Quantifiable);
		System.out.println(q2 instanceof QuantityMax3D);
		System.out.println(q2 instanceof QuantityMax2D);
		System.out.println(q2 instanceof QuantityMax1D);
	}

}
