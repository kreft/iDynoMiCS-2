package analysis.filter;

import aspect.AspectInterface;
import grid.ArrayType;
import idynomics.Compartment;

public class SoluteFilter implements Filter
{

	private Compartment _compartment;
	private String _solute;

	public SoluteFilter( String filter, Compartment compartment )
	{
		this._compartment = compartment;
		this._solute = filter.split("%")[1];
	}
	@Override
	public String stringValue(AspectInterface subject) {
		return String.valueOf( this._compartment.getSolute( 
				this._solute ).getAverage( ArrayType.CONCN ) );
	}

	@Override
	public String header() {
		return this._solute;
	}

	@Override
	public boolean match(AspectInterface subject) {
		return false;
	}
	
}