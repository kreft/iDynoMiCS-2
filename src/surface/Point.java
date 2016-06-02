package surface;

import dataIO.XmlLabel;
import generalInterfaces.Copyable;
import linearAlgebra.Vector;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.ModelNode.Requirements;
import nodeFactory.NodeConstructor;

/**
 * \brief TODO needs spring cleaning.. keep Point as a minimal object
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class Point implements Copyable, NodeConstructor
{
	/**
	 * Unique identifier for each point.
	 */
	private static int UNIQUE_ID = 0;
	protected int _uid = ++UNIQUE_ID;

	/**
	 * Location vector.
	 */
	private double[] _p;

	/**
	 * Force vector.
	 */
	private double[] _f;

	/**
	 * Used by higher-order ODE solvers.
	 */
	private double[][] _c;

	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public Point(double[] p) 
	{
		/* Copying may be slower to initiate, but is safer. */
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

	public Point(Point q)
	{
		this.setPosition(Vector.copy(q._p));
		this.setForce(Vector.zeros(_p));
	}
	
	public Object copy() 
	{
		return new Point(this._p);
	}

	/*************************************************************************
	 * BASIC GETTERS & SETTERS
	 ************************************************************************/

	public int identifier() 
	{
		return this._uid;
	}

	public int nDim()
	{
		return this._p.length;
	}
	
	public double[] getPosition()
	{
		return this._p;
	}

	public void setPosition(double[] position)
	{
		if ( Double.isNaN(position[0]))
			System.out.println(_p);
		this._p = position;
	}
	
	public double[] getForce()
	{
		return this._f;
	}

	public void setForce(double[] force)
	{
		this._f = force;
	}

	private void resetForce()
	{
		Vector.reset(this._f);
	}

	public void addToForce(double[] forceToAdd)
	{
		Vector.addEquals(this._f, forceToAdd);
	}
	
	/*************************************************************************
	 * ODE METHODS
	 ************************************************************************/

	
	public void initialiseC(int size)
	{
		this._c = new double[size][this._p.length];
	}

	
	/**
	 * \brief performs one Euler step for the mechanical relaxation.
	 * The velocity is expressed as v = (sum forces) / (3 Pi diameter viscosity)
	 * Currently the viscosity of water is assumed.
	 * 
	 * @param vSquare Highest squared velocity in the system
	 * @param dt Current timestep of the mechanical relaxation
	 * @param radius Radius of the Point
	 * @return vSquare, if the squared velocity of this point is higher vSquare
	 * is updated.
	 */
	public void euStep(double dt, double radius) 
	{
		// TODO for (longer) rod segments we cannot simply use the radius or
		// diameter but need to use the equivalent spherical diameter
		// definition by wiki: the equivalent diameter of a non-spherical 
		// particle is equal to a diameter of a spherical particle that exhibits 
		// identical properties (in this case hydrodynamic).
		// see pdf forces in microbial systems.
		double[] diff = this.dxdt(radius);
		Vector.timesEquals(diff, dt);
		Vector.addEquals(this._p, diff);
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
		double[] diff = this.dxdt(radius);
		/* Store the old position and velocity. */
		this._c[0] = Vector.copy(this._p);
		this._c[1] = Vector.copy(diff);
		/* Move the location and reset the force. */
		Vector.timesEquals(diff, dt);
		Vector.addEquals(this._p, diff);
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
		 * p = c0 + ((dxdt + c1) * dt / 2)
		 * -> c0 is the old position
		 * -> c1 is the old velocity
		 */
		Vector.addTo(this._p, this.dxdt(radius), this._c[1]);
		Vector.timesEquals(this._p, dt/2.0);
		Vector.addEquals(this._p, this._c[0]);
		this.resetForce();
	}

	/**
	 * \brief Find the velocity of this point.
	 * 
	 * <p>The drag on this point from the surrounding fluid is calculated using
	 * Stoke's Law for the drag on a sphere:</p>
	 * <p><i>v = sum(forces) / ( 3 * pi * diameter * viscosity)</i></p>
	 * 
	 * <p>See<ul>
	 * <li>Berg HC. Random walks in biology (Expanded edition). Princeton
	 * University Press; 1993. Pages 75-77</li>
	 * <li>Purcell EM. Life at low Reynolds number. <i>American Journal of
	 * Physics</i>. 1977;45: 3–11.</li>
	 * </ul></p>
	 * 
	 * <p>For the purposes of the viscosity constant, we currently assume
	 * the surrounding fluid to be water at 25 C (298.15 K). This gives us a
	 * viscosity of FIXME Rob [28May2016]: Bas, please give value and units</p>
	 * 
	 * @param radius The radius of the sphere-swept volume this point belongs
	 * to will affect the drag on it by the surrounding fluid.
	 * @return Vector describing the velocity of this point in FIXME units?
	 */
	public double[] dxdt(double radius)
	{
		
		/*
		 * 53.05 = 1/0.01885
		 * 0.01885 = 3 * pi * (viscosity of water)
		 */
		// TODO calculate from user divined viscosity
		return Vector.times(this.getForce(), 53.05/radius);
	}

	/**
	 * \brief TODO
	 * 
	 * <p>Legacy support: not identical but shoves like there is no
	 * tomorrow.</p>
	 * 
	 * @param dt
	 * @param radius
	 */
	public void shove(double dt, double radius) 
	{
		/*
		 * No point shoving if there's no force.
		 */
		if ( Vector.isZero(this.getForce()) )
			return;
		/*
		 * Scale the force.
		 */
		// TODO note that force is currently scaled may need to revise later
		//TODO explain why!
		double scalar = radius;
		if ( Vector.normEuclid(this.getForce()) < 0.2 )
		{
			/* Anti deadlock. */
			scalar *= 3.0;
		}
		else
		{
			/* Anti catapult */
			scalar *= 0.5;
		}
		Vector.times(this._f, scalar);
		/*
		 * Apply the force and reset it.
		 */
		Vector.addEquals(this._p, this._f);
		this.resetForce();
	}

	/**
	 * Retrieve the up to date model node
	 */
	public ModelNode getNode()
	{
		/* point node */
		ModelNode modelNode = new ModelNode(XmlLabel.point, this);
		modelNode.requirement = Requirements.ZERO_TO_FEW;

		/* position attribute */
		modelNode.add(new ModelAttribute(XmlLabel.position, 
				Vector.toString(this._p), null, true ));

		return modelNode;
	}

	@Override
	public void setNode(ModelNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public NodeConstructor newBlank() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addChildObject(NodeConstructor childObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String defaultXmlTag() {
		// TODO Auto-generated method stub
		return null;
	}
}
