package agent.body;

import linearAlgebra.Vector;

/**
 * \brief TODO needs spring cleaning.. keep Point as a minimal object
 * 
 * @author Bastiaan Cockx, DTU (baco@env.dtu.dk)
 */
public class Point
{
    static int UNIQUE_ID = 0;
    protected int uid = ++UNIQUE_ID;
    
    /**
     * Position vector.
     */
	private double[] p;
	
	/**
	 * Force vector.
	 */
	private double[] f;
	
	/**
	 * Used by higher-order ODE solvers.
	 */
	private double[][] c;
	
	public Point(double[] p) 
	{
		// Copying may be slower to initiate, but is safer.
		this.setPosition(Vector.copy(p));
		this.setForce(Vector.zeros(p));
	}
	
	public Point(int nDim)
	{
		this(Vector.zerosDbl(nDim));
	}
	
	//FIXME: change this to set position random location lowerbound[] 
	// upperbound[], currently domain represents a simple spawn box with sizes
	// "domain", this needs to be a bit more specific
	public Point(int nDim, double domain) 
	{
		this(Vector.randomPlusMinus(nDim, domain));
	}
	
	public Point(String vectorString)
	{
		this(Vector.dblFromString(vectorString));
	}
	
	public void setC(int size)
	{
		c = new double[size][p.length];
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
	public void euStep(double dt, double radius) 
	{
		// TODO Rob [19Nov2015]: Where does this 0.01885 comes from?
		// TODO Bas [24Nov2015]: this method still needs to be updated,
		// currently the velocity is: (sum forces) / (3 Pi diameter viscosity)
		// here the viscosity of water is assumed, that is where 0.01885 comes
		// from.
		// TODO Rob [26Nov2015]: OK, let's just sure this is clearly
		// documented before release. For now, I've replaced the 1/0.01885
		// with 53.05, as this is slightly less work for the CPU! 
		// TODO for (longer) rod segments we cannot simply use the radius or
		// diameter but need to use the equivalent spherical diameter
		// definition by wiki: the equivalent diameter of a non-spherical 
		// particle is equal to a diameter of a spherical particle that exhibits 
		// identical properties (in this case hydrodynamic).
		// see pdf forces in microbial systems.
		Vector.addEquals(p, Vector.times(dxdt(radius), dt));
		this.resetForce();
	}
	
	/**
	 * \brief First stage of Heun's method.
	 * 
	 * @param dt
	 * @param radius
	 */
	public void heun1(double dt, double radius)
	{
		// h position = Vector.copy(p);
		c[0] = Vector.copy(p);									
		Vector.addEquals(p, Vector.times(dxdt(radius), dt));
		// h velocity = dxdt(radius);
		c[1] = dxdt(radius);
		this.resetForce();
	}
	
	/**
	 * \brief Second stage of Heun's method.
	 * 
	 * @param dt
	 * @param radius
	 */
	public void heun2(double dt, double radius)
	{
		/*
		 * p = c0 + (dxdt * c1 * dt / 2)
		 */
		p = Vector.add(dxdt(radius),c[1]);
		Vector.timesEquals(p, dt/2.0);
		Vector.addEquals(p, c[0]);
		this.resetForce();
	}
	
	/**
	 * 
	 * @param radius
	 * @return
	 */
	public double[] dxdt(double radius)
	{
		/*
		 * 53.05 = 1/0.01885
		 * 0.01885 = 3 * pi * (viscosity of water)
		 */
		return Vector.times(getForce(), 53.05/radius);
	}
	
	public void shove(double dt, double radius) 
	{
		// Legacy support
		// not identical but shoves like there is no tomorrow 
		// TODO note that force is currently scaled may need to revise later
		
		if ( ! Vector.isZero(getForce()) )
		{
			// anti deadlock
			if ( Vector.normEuclid(getForce()) < 0.2 )
				Vector.addEquals(p, Vector.times(getForce(), 5.0* radius)); 
			// anti catapult
			else
				Vector.addEquals(p, Vector.times(getForce(), 0.7* radius));
		}
		this.resetForce();
	}
	
	public double[] coord(double radius) 
	{
		double[] coord = new double[p.length];
		for (int i = 0; i < p.length; i++) 
			coord[i] = p[i] - radius;
		return coord;
	}
	
	public double[] dimensions(double radius) 
	{
		double[] dimensions = new double[p.length];
		for (int i = 0; i < p.length; i++) 
			dimensions[i] = radius * 2.0;
		return dimensions;
	}
	
	public double[] upper(double radius) 
	{
		double[] coord = new double[p.length];
		for (int i = 0; i < p.length; i++) 
			coord[i] = p[i] + radius;
		return coord;
	}
	
	public int nDim()
	{
		return p.length;
	}

	public double[] getPosition()
	{
		return p;
	}

	public void setPosition(double[] position)
	{
		this.p = position;
	}

	public double[] getForce()
	{
		return f;
	}

	public void setForce(double[] force)
	{
		this.f = force;
	}
	
	private void resetForce()
	{
		Vector.reset(f);
	}
	
	public void addToForce(double[] forceToAdd)
	{
		Vector.addEquals(this.f, forceToAdd);
	}
	
	public void subtractFromForce(double[] forceToSubtract)
	{
		Vector.minusEquals(this.f, forceToSubtract);
	}
}