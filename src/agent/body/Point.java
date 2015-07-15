package agent.body;

import utility.Vector;

public class Point {
    static int UNIQUE_ID = 0;
    int uid = ++UNIQUE_ID;
	private Double[] position;
	private Double[] velocity;
	private Double[] force;
	
	public Point(int nDim) 
	{
		this.setPosition(Vector.zeros(nDim));
		this.setVelocity(Vector.zeros(nDim));
		this.setForce(Vector.zeros(nDim));
	}
	
	//FIXME: change this to set position random location lowerbound[] 
	//upperbound[], currently domain represents a simple spawn box with sizes
	// "domain", this needs to be a bit more specific
	public Point(int nDim, double domain) 
	{
		this.setPosition(Vector.randomDirection(nDim,domain));
		this.setVelocity(Vector.zeros(nDim));
		this.setForce(Vector.zeros(nDim));
	}
	
	public Point(Double[] p) 
	{
		this.setPosition(p);
		this.setVelocity(Vector.zeros(p.length));
		this.setForce(Vector.zeros(p.length));
	}

	public int identifier() 
	{
        return uid;
    }
	
	/**
	 * \brief performs one euler step for the mechanical relaxation.
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
	public Double euStep(Double vSquare, double dt, Double radius) 
	{
		position = Vector.sum(position, Vector.product(getVelocity(),dt));
		setVelocity(Vector.product(getForce(), 1.0/(radius*0.01885)));
		if (Vector.normSquare(getVelocity()) > vSquare)
			vSquare = Vector.normSquare(getVelocity());
		Vector.reset(getForce());
		return vSquare;
	}
	
	//TODO: switch from a float RTree to a Double RTree so we can consistantly 
	// use Doubles in the model implementation.
	public float[] coord(Double radius) 
	{
		float[] coord = new float[position.length];
		for (int i = 0; i < position.length; i++) 
			coord[i] = (float) (position[i]-radius);
		return coord;
	}
	
	public float[] dimensions(Double radius) 
	{
		float[] dimensions = new float[position.length];
		for (int i = 0; i < position.length; i++) 
			dimensions[i] = (float) (radius*2.0);
		return dimensions;
	}
	
	public float[] upper(Double radius) 
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

	public Double[] getPosition() 
	{
		return position;
	}

	public void setPosition(Double[] position) 
	{
		this.position = position;
	}

	public Double[] getForce() {
		return force;
	}

	public void setForce(Double[] force) {
		this.force = force;
	}

	public Double[] getVelocity() {
		return velocity;
	}

	public void setVelocity(Double[] velocity) {
		this.velocity = velocity;
	}

}
