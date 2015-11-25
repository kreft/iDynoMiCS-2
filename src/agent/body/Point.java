package agent.body;

import linearAlgebra.Vector;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx, DTU (baco@env.dtu.dk)
 */
public class Point
{
    static int UNIQUE_ID = 0;
    int uid = ++UNIQUE_ID;
	private double[] position;
	private double[] velocity;
	private double[] force;
	
	public Point(double[] p) 
	{
		this.setPosition(p);
		this.setVelocity(Vector.zerosDbl(p.length));
		this.setForce(Vector.zerosDbl(p.length));
	}
	
	public Point(int nDim)
	{
		this(Vector.zerosDbl(nDim));
	}
	
	//FIXME: change this to set position random location lowerbound[] 
	//upperbound[], currently domain represents a simple spawn box with sizes
	// "domain", this needs to be a bit more specific
	public Point(int nDim, double domain) 
	{
		this(Vector.randomPlusMinus(nDim, domain));
	}
	
	public Point(String vectorString)
	{
		this(Vector.dblFromString(vectorString));
	}
	
	public int identifier() 
	{
        return uid;
    }
	
	/**
	 * \brief performs one Euler step for the mechanical relaxation.
	 * The velocity is expressed as v = (sum forces) / (3 Pi diameter viscosity)
	 * Currently the viscosity of water is assumed.
	 * @param vSquare
	 * 			Highest squared velocity in the system
	 * @param dt
	 * 			Current timestep of the mechanical relaxation
	 * @param radius
	 * 			Radius of the Point
	 * @return vSquare, if the squared velocity of this point is higher vSquare
	 * is updated.
	 */
	public Double euStep(double vSquare, double dt, double radius) 
	{
		Vector.add(position, Vector.timesEquals(getVelocity(),dt));
		// TODO Rob [19Nov2015]: Where does this 0.01885 comes from?
		// TODO Bas [24Nov2015]: this method still needs to be updated,
		// currently the velocity is: (sum forces) / (3 Pi diameter viscosity)
		// here the viscosity of water is assumed, that is where 0.01885 comes
		// from.
		setVelocity(Vector.timesEquals(getForce(), 1.0/(radius*0.01885)));
		if ( Vector.normSquare(getVelocity()) > vSquare )
			vSquare = Vector.normSquare(getVelocity());
		this.resetForce();
		return vSquare;
	}
	
	//TODO: switch from a float RTree to a Double RTree so we can consistantly 
	// use Doubles in the model implementation.
	public float[] coord(double radius) 
	{
		float[] coord = new float[position.length];
		for (int i = 0; i < position.length; i++) 
			coord[i] = (float) (position[i]-radius);
		return coord;
	}
	
	public float[] dimensions(double radius) 
	{
		float[] dimensions = new float[position.length];
		for (int i = 0; i < position.length; i++) 
			dimensions[i] = (float) (radius*2.0);
		return dimensions;
	}
	
	public float[] upper(double radius) 
	{
		float[] coord = new float[position.length];
		for (int i = 0; i < position.length; i++) 
			coord[i] = (float) (position[i]+radius);
		return coord;
	}
	
	public int nDim() 
	{
		return getPosition().length;
	}

	public double[] getPosition() 
	{
		return position;
	}

	public void setPosition(double[] position) 
	{
		this.position = position;
	}

	public double[] getForce() {
		return force;
	}

	public void setForce(double[] force) {
		this.force = force;
	}
	
	public void resetForce()
	{
		Vector.reset(this.force);
	}
	
	public void addToForce(double[] forceToAdd)
	{
		Vector.add(this.force, forceToAdd);
	}
	
	public void subtractFromForce(double[] forceToSubtract)
	{
		Vector.minus(this.force, forceToSubtract);
	}

	public double[] getVelocity() {
		return velocity;
	}

	public void setVelocity(double[] velocity) {
		this.velocity = velocity;
	}

}
