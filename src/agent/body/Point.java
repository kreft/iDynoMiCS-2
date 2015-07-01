package agent.body;

import utility.Vector;

public class Point
{
	
    static int UNIQUE_ID = 0;
    int uid = ++UNIQUE_ID;
	
    /**
     * 
     */
    private Double[] _position;
	
    /**
     * 
     */
    private Double[] _velocity;
	
    /**
     * 
     */
    private Double[] _force;
	
    /*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
    
	public Point(int nDim) 
	{
		this.setPosition(Vector.zeros(nDim));
		this.setVelocity(Vector.zeros(nDim));
		this.setForce(Vector.zeros(nDim));
	}
	
	/*
	 * FIXME: change this to set position random location lowerbound[]
	 * upperbound[], currently domain represents a simple spawn box with sizes
	 * "domain", this needs to be a bit more specific
	 */
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
	
	/*************************************************************************
	 * BASIC SETTERS & GETTERS
	 ************************************************************************/
	
	public int nDim() 
	{
		return this._position.length;
	}

	public Double[] getPosition() 
	{
		return this._position;
	}

	public void setPosition(Double[] position) 
	{
		this._position = position;
	}

	public Double[] getForce()
	{
		return this._force;
	}
	
	public void setForce(Double[] force)
	{
		this._force = force;
	}
	
	public Double[] getVelocity()
	{
		return this._velocity;
	}
	
	public void setVelocity(Double[] velocity)
	{
		this._velocity = velocity;
	}
	
	/*************************************************************************
	 * MOVEMENT METHODS
	 ************************************************************************/
	
	/**
	 * \brief performs one Euler step for the mechanical relaxation.
	 * 
	 * FIXME Is this Forward Euler or Backward?
	 * 
	 * The velocity is expressed as v = (sum forces) / (3 Pi diameter viscosity)
	 * Currently the viscosity of water is assumed.
	 * 
	 * @param vSquare	Highest squared velocity in the system.
	 * @param dt	Current timestep of the mechanical relaxation.
	 * @param radius	Radius of the Point.
	 * @return vSquare; if the squared velocity of this point is higher vSquare
	 * is updated.
	 */
	public Double euStep(Double vSquare, Double dt, Double radius) 
	{
		/*
		 * 
		 */
		this._velocity = Vector.scaleCopy(this._velocity, dt);
		this._position = Vector.addCopy(this._position, this._velocity );
		/*
		 * Update the velocity from the force
		 * 
		 * FIXME Where does this 0.01885 come from???
		 */
		this._velocity = Vector.scaleCopy(this._force, 1.0/(radius*0.01885));
		if ( Vector.normSquare(getVelocity()) > vSquare )
			vSquare = Vector.normSquare(this._velocity);
		/*
		 * Finally, reset 
		 */
		Vector.reset(_force);
		return vSquare;
	}
	
	/*
	 * TODO: switch from a float RTree to a Double RTree so we can
	 * consistently use Doubles in the model implementation.
	 */
	public float[] coord(Double radius) 
	{
		float[] coord = new float[_position.length];
		for (int i = 0; i < _position.length; i++) 
			coord[i] = (float) (_position[i]-radius);
		return coord;
	}
	
	public float[] dimensions(Double radius) 
	{
		float[] dimensions = new float[_position.length];
		for (int i = 0; i < _position.length; i++) 
			dimensions[i] = (float) (radius*2.0);
		return dimensions;
	}
	
	public float[] upper(Double radius) 
	{
		float[] coord = new float[_position.length];
		for (int i = 0; i < _position.length; i++) 
			coord[i] = (float) (_position[i]+radius);
		return coord;
	}
	
	
}
