package shape;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import boundary.SpatialBoundary;
import boundary.spatialLibrary.SolidBoundary;
import dataIO.XmlHandler;
import generalInterfaces.CanPrelaunchCheck;
import instantiable.Instance;
import referenceLibrary.ClassRef;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Module.Requirements;
import settable.Settable;
import shape.iterator.PolarShapeIterator;
import surface.Surface;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief Dimension of a {@code Shape}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Dimension implements CanPrelaunchCheck, Settable, 
		Comparable<Dimension>
{
	/**
	 * 
	 * 
	 */
	public enum DimName
	{
		X(false),
		Y(false),
		Z(false),
		R(false),
		THETA(true),
		PHI(true);
		
		private boolean _isAngular;
		
		DimName(boolean isAngular)
		{
			this._isAngular = isAngular;
		}
		
		public boolean isAngular()
		{
			return this._isAngular;
		}
	}
	

	/**
	 * If we need to put a point just inside the maximum extreme, use this
	 * number multiplied by the dimension length as the small amount less than
	 * this._extreme[1] to use.
	 * 
	 * <p>NOTE: Rob [8Feb2016] We used to use Math.ulp(this.getLength()) but
	 * that didn't seem to work very well.</p>
	 */
	private final static double INSIDE_MAX_SCALAR = 1.0E-6;
	
	/**
	 * Minimum and maximum values for this dimension. Must be finite and have
	 * {@code this._extreme[0]} < {@code this._extreme[1]}.
	 */
	protected double[] _extreme = new double[]{0.0, Double.MIN_VALUE};
	
	/**
	 * Actual values of min and max for this dimension. This will determine the
	 * multiplicative factor, if not provided.
	 */
	protected double[] _realExtreme = new double[]{0.0, Double.MIN_VALUE};
	
	/**
	 * Boundary objects at the minimum (0) and maximum (1).
	 */
	private SpatialBoundary[] _boundary = new SpatialBoundary[2];
	
	/**
	 * Surface objects at the minimum (0) and maximum (1).
	 */
	private Surface[] _surface = new Surface[2];
	
	/**
	 * Whether boundaries are required (true) or optional (false) at the
	 * minimum (0) and maximum (1) of this dimension. Meaningless in
	 * cyclic dimensions.
	 */
	protected boolean[] _required = new boolean[]{true, true};
	
	/**
	 * If this is a cyclic dimension, different rules apply.
	 */
	protected boolean _isCyclic = false;
	/**
	 * TODO
	 */
	protected boolean _isSignificant = true;
	
	/**
	 * 
	 */
	protected DimName _dimName;

	/**
	 * target resolution
	 */
	protected Double _targetRes;

	private Settable _parentNode;
	
	/* ************************************************************************
	 * CONSTRUCTORS
	 * ***********************************************************************/
	
	public Dimension()
	{
		
	}
	
	public Dimension(boolean isSignificant, DimName dimName)
	{
		this._dimName = dimName;
		if ( isSignificant )
			this.setSignificant();
		else
			this.setInsignificant();
	}
	

	public void instantiate(Element xmlElement, Settable parent)
	{
		Element elem = (Element) xmlElement;
		String str;
		Double dbl;
		double val;
		NodeList bndNodes;
		Element bndElem;
		SpatialBoundary aBoundary;

		this._parentNode = parent;
		int index = -1;
		
		/*
		 * See if this is cyclic. Assume not if unspecified.
		 */
		if ( XmlHandler.obtainBoolean(elem, XmlRef.isCyclic, 
				this.defaultXmlTag() + " " + this._dimName.name()) )
			this.setCyclic();
		
		/* 
		 * Boundaries at the extremes.
		 */
		
		/* The maximum has to be set. */
		str = this.defaultXmlTag() + " " + this._dimName.name();
		val = XmlHandler.obtainDouble(elem, XmlRef.max, str);
		/* Convert from degrees to radians for angular dimensions */
		if ( this.isAngular() )
			val = Math.toRadians(val);
		this.setExtreme(val, 1);
		
		/* Set the real max from xml or equal to the compartment size. 
		 * NOTE please add comments, what is the difference between real max 
		 * and max? arent they both real? */
		dbl = XmlHandler.gatherDouble(elem, XmlRef.realMax);
		if ( dbl != null )
		{
			val = dbl;
			if ( this.isAngular() )
				val = Math.toRadians(val);
		}
		this.setRealExtreme(val, 1);
		
		/* By default the minimum is 0.0 */
		val = 0.0;
		dbl = XmlHandler.gatherDouble(elem, XmlRef.min);
		if (dbl != null)
		{
			val = dbl;
		/* Convert from degrees to radians for angular dimensions */
			if ( this.isAngular() )
				val = Math.toRadians(val);
		}
		this.setExtreme(val, 0);
		
		/* Set the real min from xml or equal to the compartment size. */
		dbl = XmlHandler.gatherDouble(elem, XmlRef.realMin);
		if ( dbl != null )
		{
			val = dbl;
			if ( this.isAngular() )
				val = Math.toRadians(val);
		}
		this.setRealExtreme(val, 0);
		
		/* Calculate length from dimension extremes. */
		double length = this.getLength();
		
		/* Fetch target resolution (or use length as default). */
		dbl = XmlHandler.obtainDouble(elem, XmlRef.targetResolutionAttribute,
				this.defaultXmlTag() + " " + this._dimName.name());
		this._targetRes = length; 
		if ( dbl != null )
			this._targetRes = dbl;
		
		/* Set theta dimension cyclic for a full circle, no matter what 
		 * the user specified */
		if ( this._dimName == DimName.THETA && ExtraMath.areEqual(length, 
				2 * Math.PI, PolarShapeIterator.POLAR_ANGLE_EQ_TOL))
		{
			this.setCyclic();
		}
		
		// FIXME investigate and clean
		/* Set the boundary, if given (not always necessary). */
		bndNodes = XmlHandler.getAll(elem, XmlRef.dimensionBoundary);
		if ( ! Helper.isNullOrEmpty(bndNodes) )
		{
			for ( int i = 0; i < bndNodes.getLength(); i++ )
			{
				bndElem = (Element) bndNodes.item(i);
				aBoundary = (SpatialBoundary)Instance.getNew(bndElem, this);
				index = aBoundary.getExtreme();
				this.setBoundary(aBoundary, index);	
			}
		}
		
		/*
		 * Create default solid boundaries if dimension is not cyclic
		 */
		if (!this._isCyclic)
		{
			for (int extreme = 0 ; extreme < 2; extreme++)
			{
				if (!this.isBoundaryDefined(extreme))
				{
					SolidBoundary solidBoundary = (SolidBoundary) 
							Instance.getNew(ClassRef.solidBoundary , null);
					this.setBoundary(solidBoundary, extreme);
				}
			}
		}
		
	}
	
	/* ************************************************************************
	 * BASIC SETTERS AND GETTERS
	 * ***********************************************************************/
	
	public DimName getName()
	{
		return this._dimName;
	}
	
	public boolean isAngular()
	{
		return this._dimName.isAngular();
	}
	
	/**
	 * \brief Get the length of this dimension.
	 * 
	 * @return A positive {@code double}.
	 */
	public double getLength()
	{
		return this._extreme[1] - this._extreme[0];
	}
	
	/**
	 * \brief Get the real length of this dimension.
	 * 
	 * @return A positive {@code double}.
	 */
	public double getRealLength()
	{
		return this._realExtreme[1] - this._realExtreme[0];
	}
	
	/**
	 * \brief Confirm that the maximum extreme is greater than the minimum.
	 */
	protected void checkExtremes(double[] extremeVal)
	{
		if ( extremeVal[1] < extremeVal[0] )
		{
			throw new 
					IllegalArgumentException("Dimension length must be > 0");
		}
	}
	
	/**
	 * \brief Set the value for a specified extreme to take.
	 * 
	 * @param value Value for the specified extreme to take.
	 * @param index Which extreme to set: 0 for minimum, 1 for maximum.
	 */
	public void setExtreme(double value, int index)
	{
		this._extreme[index] = value;
		this.checkExtremes(this._extreme);
	}
	
	/**
	 * \brief Set the values of both the minimum and maximum extremes.
	 * 
	 * <p>Note that <b>minValue</b> must be less than <b>maxValue</b>.</p>
	 * 
	 * @param minValue Value for the minimum extreme to take.
	 * @param maxValue Value for the maximum extreme to take.
	 */
	public void setExtremes(double minValue, double maxValue)
	{
		this._extreme[0] = minValue;
		this._extreme[1] = maxValue;
		this.checkExtremes(this._extreme);
	}
	
	/**
	 * \brief Set the value for real extreme to take.
	 * 
	 * @param value Value for the specified extreme to take.
	 * @param index Which extreme to set: 0 for minimum, 1 for maximum.
	 */
	public void setRealExtreme(double value, int index)
	{
		this._realExtreme[index] = value;
		this.checkExtremes(this._realExtreme);
	}
	
	/**
	 * @param index Index of the extreme: must be 0 or 1.
	 * @return Position in space of the required extreme.
	 */
	public double getExtreme(int index)
	{
		return this._extreme[index];
	}
	
	/**
	 * @param index Index of the extreme: must be 0 or 1.
	 * @return Position in space of the required extreme.
	 */
	public double getRealExtreme(int index)
	{
		return this._realExtreme[index];
	}
	
	/**
	 * \brief Set the length of this dimension.
	 * 
	 * @param length Positive {@code double}.
	 */
	public void setLength(double length)
	{
		this._extreme[1] = this._extreme[0] + length;
		this.checkExtremes(this._extreme);
	}
	
	/**
	 * \brief Set this dimension to be cyclic.
	 * 
	 * <p>Anything crossing one extreme of this dimension will re-emerge on the
	 * other extreme.</p>
	 */
	public void setCyclic()
	{
		this._isCyclic = true;
	}
	
	/**
	 * \brief Whether this dimension is cyclic or not.
	 * 
	 * @return {@code boolean} specifying whether this dimension is cyclic
	 * (true) or bounded (false).
	 */
	public boolean isCyclic()
	{
		return this._isCyclic;
	}
	
	/**
	 * TODO
	 */
	public void setSignificant()
	{
		this._isSignificant = true;
	}
	
	/**
	 * TODO
	 */
	public void setInsignificant()
	{
		this._isSignificant = false;
		this.setBoundariesOptional();
	}
	
	/**
	 * TODO
	 * @return
	 */
	public boolean isSignificant()
	{
		return this._isSignificant;
	}
	
	/* ************************************************************************
	 * BOUNDARIES
	 * ***********************************************************************/
	
	/**
	 * \brief Tell this dimension that the boundary at the given extreme may
	 * not be specified.
	 * 
	 * <p>Meaningless in cyclic dimensions.</p>
	 * 
	 * @param index Which extreme to set: 0 for minimum, 1 for maximum.
	 * @see #setBoundariesOptional()
	 * @see #setBoundaryRequired(int)
	 */
	public void setBoundaryOptional(int index)
	{
		this._required[index] = false;
	}
	
	/**
	 * \brief Tell this dimension that both boundaries may not be specified.
	 * 
	 * <p>Meaningless in cyclic dimensions.</p>
	 * 
	 * @see #setBoundaryOptional(int)
	 * @see #setBoundariesRequired()
	 */
	public void setBoundariesOptional()
	{
		this.setBoundaryOptional(0);
		this.setBoundaryOptional(1);
	}
	
	/**
	 * \brief Tell this dimension that the boundary at the given extreme must
	 * be specified.
	 * 
	 * <p>Meaningless in cyclic dimensions.</p>
	 * 
	 * @param index Which extreme to set: 0 for minimum, 1 for maximum.
	 * @see #setBoundariesRequired()
	 * @see #setBoundaryOptional(int)
	 */
	public void setBoundaryRequired(int index)
	{
		this._required[index] = true;
	}
	
	/**
	 * \brief Tell this dimension that both boundaries must be specified.
	 * 
	 * <p>Meaningless in cyclic dimensions.</p>
	 * 
	 * @see #setBoundaryRequired(int)
	 * @see #setBoundariesOptional()
	 */
	public void setBoundariesRequired()
	{
		this.setBoundaryRequired(0);
		this.setBoundaryRequired(1);
	}
	
	/**
	 * \brief Set both the minimum and maximum boundaries.
	 * 
	 * @param minBndry {@code Boundary} to set at the minimum extreme.
	 * @param maxBndry {@code Boundary} to set at the maximum extreme.
	 */
	public void setBoundary(SpatialBoundary aBoundary, int index)
	{
		if ( this._isCyclic )
		{
			throw new IllegalArgumentException(
							"Cannot set the boundary of a cyclic dimension!");
		}
		else
			this._boundary[index] = aBoundary;
	}
	
	/**
	 * \brief Set both the minimum and maximum boundaries.
	 * 
	 * @param minBndry {@code Boundary} to set at the minimum extreme.
	 * @param maxBndry {@code Boundary} to set at the maximum extreme.
	 */
	public void setBoundaries(SpatialBoundary minBndry, SpatialBoundary maxBndry)
	{
		this._boundary[0] = minBndry;
		this._boundary[1] = maxBndry;
	}
	
	/**
	 * \brief Get an array of boundaries. 
	 * 
	 * @return 2-array of {@code Boundary} objects.
	 */
	public SpatialBoundary[] getBoundaries()
	{
		return this._boundary;
	}
	
	/**
	 * \brief Get the {@code Boundary} at the required extreme.
	 * 
	 * @param extreme Which extreme to check: 0 for minimum, 1 for maximum.
	 * @return The {@code Boundary} at the required extreme.
	 */
	public SpatialBoundary getBoundary(int extreme)
	{
		return this._boundary[extreme];
	}
	
	/**
	 * \brief Report whether the boundary on the given extreme is defined.
	 * 
	 * @param extreme Which extreme to check: 0 for minimum, 1 for maximum.
	 * @return True if the boundary is defined, false if it is not.
	 */
	public boolean isBoundaryDefined(int extreme)
	{
		return this._boundary[extreme] != null;
	}
	
	/* ************************************************************************
	 * SURFACES
	 * ***********************************************************************/
	
	/**
	 * \brief Set the surface at one extreme.
	 * 
	 * @param aSurface Surface object to use.
	 * @param extreme Which extreme to set: 0 for minimum, 1 for maximum.
	 */
	public void setSurface(Surface aSurface, int extreme)
	{
		this._surface[extreme] = aSurface;
	}
	
	/**
	 * \brief Get the {@code Surface} at the required extreme.
	 * 
	 * @param extreme Which extreme to check: 0 for minimum, 1 for maximum.
	 * @return The {@code Surface} at the required extreme.
	 */
	public Surface getSurface(int extreme)
	{
		return this._surface[extreme];
	}
	
	/**
	 * \brief Report whether the surface on the given extreme is defined.
	 * 
	 * @param extreme Which extreme to check: 0 for minimum, 1 for maximum.
	 * @return True if the surface is defined, false if it is not.
	 */
	public boolean isSurfaceDefined(int extreme)
	{
		return this._surface[extreme] != null;
	}
	
	/* ************************************************************************
	 * USEFUL METHODS
	 * ***********************************************************************/
	
	/**
	 * \brief Get the shortest distance between two positions along this
	 * dimension.
	 * 
	 * <p>Note that this may be negative if <b>b</b> > <b>a</b>.</p> 
	 * 
	 * @param a Position in this dimension.
	 * @param b Position in this dimension.
	 * @return Shortest distance between <b>a</b> and <b>b</b>, accounting for
	 * cyclic dimension if necessary.
	 */
	public double getShortest(double a, double b)
	{
		double out = a - b;
		if ( this._isCyclic && (Math.abs(out) > 0.5 * this.getLength()) )
			out -= this.getLength() * Math.signum(out);
		return out;
	}
	
	/**
	 * \brief Checks if the position given is within the extremes.
	 * 
	 * <p>Always inside a cyclic dimension.</p>
	 * 
	 * @param a Position in this dimension.
	 * @return Whether <b>a</b> is inside (true) or outside (false).
	 */
	public boolean isInside(double a)
	{
		return this._isCyclic ||
					(( a >= this._extreme[0] ) && ( a < this._extreme[1] ));
	}
	
	public boolean isLocalInside(double a)
	{
		return (( a >= this._extreme[0] ) && ( a < this._extreme[1] ));
	}
	
	/**
	 * @param a Any point along this dimension, whether inside or outside.
	 * @return <b>a</b> if it is inside the extremes, or the corresponding 
	 * point inside the extremes if <b>a</b> is outside.
	 */
	private double periodicLocation(double a)
	{
		return this._extreme[0] + 
				ExtraMath.floorMod(a - this._extreme[0], this.getLength());
	}
	
	/**
	 * \brief Given a position on in this dimension, finds the closest point
	 * within the extremes.
	 * 
	 * @param a Position in this dimension.
	 * @return Closest point to <b>a</b> within the extremes.
	 */
	public double getInside(double a)
	{
		if ( this._isCyclic )
			return this.periodicLocation(a);
		else
		{
			/*
			 * this._extreme[1] is an exclusive limit, so take a value just
			 * below if necessary.
			 */
			if ( a >= this._extreme[1] )
				return this._extreme[1] - (INSIDE_MAX_SCALAR*this.getLength());
			if ( a < this._extreme[0] )
				return this._extreme[0];
			return a;
		}
	}
	
	/**
	 * @return Position between the two extremes, chosen with a uniform
	 * random distribution.
	 */
	public double getRandomInside()
	{
		return ExtraMath.getUniRand(this._extreme[0], this._extreme[1]);
	}
	
	/* ************************************************************************
	 * HELPER METHODS
	 * ***********************************************************************/
	
	/**
	 * TODO
	 * @param minMax
	 * @return
	 */
	public static String extremeToString(int minMax)
	{
		return minMax == 0 ? "minimum" : "maximum";
	}
	
	/**
	 * TODO
	 * @param minMax
	 * @return
	 */
	public static int extremeToInt(String minMax)
	{
		return ( minMax.equals("minimum") ) ? 0 : 1;
			
	}
	
	/* ************************************************************************
	 * PRE-LAUNCH CHECK
	 * ***********************************************************************/
	
	public boolean isReadyForLaunch()
	{
		for ( int i = 0; i < 2; i++ )
		{
			if ( this._boundary[i] == null )
			{
				if ( this._required[i] )
					return false;
			}
			else
			{
				// TODO
				//if ( ! this._boundary[i].isReadyForLaunch() )
				//	return false;
			}
		}
		return true;
	}
	
	/* ************************************************************************
	 * MODEL NODE
	 * ***********************************************************************/

	@Override
	public Module getModule()
	{
		Module modelNode = new Module(this.defaultXmlTag(), this);
		modelNode.setTitle( this._dimName.toString() );
		modelNode.setRequirements(Requirements.IMMUTABLE);
		modelNode.add(new Attribute(XmlRef.nameAttribute, 
										this._dimName.name(), null, false ));
		modelNode.add(new Attribute(XmlRef.isCyclic, 
				String.valueOf(this._isCyclic), null, false ));
		modelNode.add(new Attribute(XmlRef.targetResolutionAttribute, 
				String.valueOf(this._targetRes), null, false ));
		/* Extremes */
		modelNode.add(new Attribute(XmlRef.min, 
				String.valueOf(this._extreme[0]), null, false ));
		modelNode.add(new Attribute(XmlRef.max, 
				String.valueOf(this._extreme[1]), null, false ));
		/* Real Extremes */
		modelNode.add( new Attribute(XmlRef.realMin,
                String.valueOf(this._realExtreme[0]), null, false ) );
		modelNode.add( new Attribute(XmlRef.realMax,
                String.valueOf(this._realExtreme[1]), null, false ) );
		/* Boundaries */
		if ( ! this._isCyclic )
		{
			if ( this.isBoundaryDefined(0) )
				modelNode.add(this._boundary[0].getModule());
			if ( this.isBoundaryDefined(1) )
				modelNode.add(this._boundary[1].getModule());
		}
		return modelNode;
	}

	@Override
	public void setModule(Module node)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String defaultXmlTag()
	{
		return XmlRef.shapeDimension;
	}

	@Override
	public int compareTo(Dimension o)
	{
		return this._dimName.compareTo(o._dimName);
	}

	@Override
	public void setParent(Settable parent) 
	{
		this._parentNode = parent;
	}
	
	@Override
	public Settable getParent() 
	{
		return this._parentNode;
	}

}