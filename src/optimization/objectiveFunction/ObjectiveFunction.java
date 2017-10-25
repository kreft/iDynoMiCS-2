package optimization.objectiveFunction;

public interface ObjectiveFunction {

	public void setData( double[] data );
	
	public double loss( double[] x );
}
