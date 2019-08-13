package test.other;

import spatialRegistry.slitRegistry.Slit;

public class SlitsTest {

	public static void main(String[] args) {
		
		Slit<Object> mySlit = new Slit<Object>(
				3, // dimensions
				2.0, // minimum slit width
				new double[]{2.0, 0.0, 0.0}, // lower corner
				new double[]{20.0, 20.0, 20.0}, // higher corner
				new boolean[] { false, true, true}); // dimension periodic
		
		mySlit.insert(new double[]{2.1, 1.0, 1.0}, new double[]{1.0, 1.0, 1.0}, "hello");
		
		
		mySlit.getLow();
	}
	

}
