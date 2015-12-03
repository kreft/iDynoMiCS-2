package agent.body;

import java.util.Random;

import linearAlgebra.Vector;
import utility.MTRandom;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx, DTU (baco@env.dtu.dk)
 */
public class Point
{
    static int UNIQUE_ID = 0;
    protected int uid = ++UNIQUE_ID;
    Random random = new MTRandom();
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
		setVelocity(dxdt(radius));
		Vector.addEquals(position, Vector.times(getVelocity(), dt));
		this.resetForce();
	}

	private double[] dxdt(double radius)
	{
		return Vector.times(getForce(), 53.05/radius);
	}
	
	public void shove(double dt, double radius)
	{
		//////////////////////////////////////////////
		// Legacy support (^_^ )
		/////////////////////////////////////////////
		// not identical but shoves like there is no tomorrow 
		// TODO note that force is currently scaled may need to revise later
		
		if (!Vector.isZero(getForce()))	{
			velocity = Vector.onesDbl(velocity.length);
			
			// anti deadlock
			if (Vector.normEuclid(getForce())  < 0.2)
				Vector.addEquals(position, Vector.times(getForce(), 5.0* radius)); 
			// anti catapult
			else
				Vector.addEquals(position, Vector.times(getForce(), 0.7* radius)); 
		}
		else {
			velocity = Vector.zerosDbl(velocity.length);
		}
		this.resetForce();
		
		/////////////////////////////////////////////
		// sort of replacing the following old code
		/////////////////////////////////////////////
		// ContinuousVector diff = computeDifferenceVector(aNeighbor);
		// Double delta = diff.norm() - getInteractDistance(aNeighbor);
		// Math.exp(-delta * 5 / _totalRadius)
		// diff.normalizeVector(delta);
		// if ( isMutual )
		// {
		// 	diff.times(0.5);
		//	aNeighbor._movement.add(diff);
		// } 
		// this._movement.subtract(diff);
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
		Vector.addEquals(this.force, forceToAdd);
	}
	
	public void subtractFromForce(double[] forceToSubtract)
	{
		Vector.minusEquals(this.force, forceToSubtract);
	}

	public double[] getVelocity() {
		return velocity;
	}

	public void setVelocity(double[] velocity) {
		this.velocity = velocity;
	}

}
