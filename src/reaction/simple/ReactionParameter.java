package reaction.simple;

public class ReactionParameter implements ReactionRate {

	public double rateTerm(double[] concentration) {
		return concentration[0];
	}

	public double direct(double concentration, double dt) {
		System.out.println("direct method not available for reaction param");
		return 0.0;
	}
}
