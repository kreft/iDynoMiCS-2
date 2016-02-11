package reaction.term;

public class Catalyst implements RateTerm {
	
	private String[] S;

	public double rateTerm(double[] concentration) {
		return concentration[0];
	}

	public double direct(double concentration, double dt) {
		System.out.println("direct method not available for catalyst");
		return 0.0;
	}
}
