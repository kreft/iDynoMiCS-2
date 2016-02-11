package reaction.term;

public class Combined implements RateTerm{
	
	final RateTerm[] _terms;
	private String[] S;
	
	public Combined(RateTerm[] terms)
	{
		this._terms = terms;
	}
	
	public double rateTerm(double[] concentrations)
	{
		double r = 1.0;
		for (int i = 0; i < _terms.length; i++)
		{
			r *= _terms[i].rateTerm(new double[]{concentrations[i]});
		}
		return r;
	}

	public double direct(double concentration, double dt) {
		System.out.println("direct method not available for combined rate");
		return 0.0;
	}
}
