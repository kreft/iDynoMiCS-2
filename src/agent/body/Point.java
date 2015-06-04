package agent.body;

import util.Vect;

public class Point implements Cloneable {
    static int UNIQUE_ID = 0;
    int uid = ++UNIQUE_ID;
	private Double[] position;
	private Double[] velocity;
	private Double[] force;
	
	public Point(int nDim) 
	{
		this.setPosition(Vect.zeros(nDim));
		this.setVelocity(Vect.zeros(nDim));
		this.setForce(Vect.zeros(nDim));
	}
	
	public Point(int nDim, double domain) 
	{
		this.setPosition(Vect.randomDirection(nDim,domain));
		this.setVelocity(Vect.zeros(nDim));
		this.setForce(Vect.zeros(nDim));
	}
	
	public Point(Double[] p) 
	{
		this.setPosition(p);
		this.setVelocity(Vect.zeros(p.length));
		this.setForce(Vect.zeros(p.length));
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
		position = Vect.sum(position, Vect.product(getVelocity(),dt));
		setVelocity(Vect.product(getForce(), 1.0/(radius*0.01885)));
		if (Vect.normSquare(getVelocity()) > vSquare)
			vSquare = Vect.normSquare(getVelocity());
		Vect.reset(getForce());
		return vSquare;
	}
	
	float[] coord(Double radius) 
	{
		float[] coord = new float[position.length];
		for (int i = 0; i < position.length; i++) 
			coord[i] = (float) (position[i]-radius);
		return coord;
	}
	
	float[] dimensions(Double radius) 
	{
		float[] dimensions = new float[position.length];
		for (int i = 0; i < position.length; i++) 
			dimensions[i] = (float) (radius*2.0);
		return dimensions;
	}
	
	float[] upper(Double radius) 
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

	Double[] getForce() {
		return force;
	}

	void setForce(Double[] force) {
		this.force = force;
	}

	Double[] getVelocity() {
		return velocity;
	}

	void setVelocity(Double[] velocity) {
		this.velocity = velocity;
	}

}
