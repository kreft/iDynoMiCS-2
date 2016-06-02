package shape;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import boundary.Boundary;
import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.XMLRef;
import dataIO.Log.Tier;
import generalInterfaces.CanPrelaunchCheck;
import nodeFactory.ModelAttribute;
import nodeFactory.ModelNode;
import nodeFactory.NodeConstructor;
import nodeFactory.ModelNode.Requirements;
import shape.ShapeConventions.BoundaryCyclic;
import utility.ExtraMath;
import utility.Helper;

/**
 * \brief Dimension of a {@code Shape}.
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk), University of Birmingham, UK.
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Dimension implements CanPrelaunchCheck, NodeConstructor, 
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
	 * Boundary objects at the minimum (0) and maximum (1).
	 */
	private Boundary[] _boundary = new Boundary[2];
	
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
	
	/**************************************************************************
	 * CONSTRUCTORS
	 *************************************************************************/
	
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
	
	public void init(Node xmlNode)
	{
		Element elem = (Element) xmlNode;
		String str;
		NodeList extNodes, bndNodes;
		Element extElem, bndElem;
		Boundary aBoundary;
		int index = -1;
		/*
		 * See if this is cyclic. Assume not if unspecified.
		 * 
		 * TODO check that str is "false" and not a typo of "true" 
		 * (e.g. "truw")
		 */
		str = XmlHandler.gatherAttribute(elem, XMLRef.IS_CYCLIC);
		if ( Boolean.valueOf(str) )
			this.setCyclic();
		
		/* calculate length from dimension extremes */
		double length = getLength();
		
		/* fetch target resolution (or use length as default) */
		str = XmlHandler.gatherAttribute(elem,
				XMLRef.targetResolutionAttribute);
		this._targetRes = length; 
		if ( str != "" )
			this._targetRes = Double.valueOf(str);
		
		/* 
		 * Boundaries at the extremes.
		 */
		str = XmlHandler.gatherAttribute(elem, XMLRef.min);
		if ( str != null && str != "")
			this.setExtreme(Double.valueOf(str), 0);
		
		str = XmlHandler.gatherAttribute(elem, XMLRef.max);
		if ( str != null && str != "")
			this.setExtreme(Double.valueOf(str), 1);

		/* Set the boundary, if given (not always necessary). */
		bndNodes = XmlHandler.getAll(elem, XMLRef.dimensionBoundary);
		for ( int i = 0; i < bndNodes.getLength(); i++ )
		{
			bndElem = (Element) bndNodes.item(i);
			str = XmlHandler.gatherAttribute(elem, XMLRef.nameAttribute);
			str = Helper.obtainInput(str, "dimension extreme (min/max)");
			str = str.toLowerCase();
			if ( str.equals("min") )
				index = 0;
			else if ( str.equals("max") )
				index = 1;
			else
			{
				Log.out(Tier.CRITICAL, 
						"Warning! Dimension extreme must be min or max: "+str);
			}
			
			str = bndElem.getAttribute(XMLRef.classAttribute);
			aBoundary = (Boundary) Boundary.getNewInstance(str);
			aBoundary.init(bndElem);
			this.setBoundary(aBoundary, index);	
		}
	}
	
	/**************************************************************************
	 * BASIC SETTERS AND GETTERS
	 *************************************************************************/
	
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
	 * \brief Confirm that the maximum extreme is greater than the minimum.
	 */
	protected void checkExtremes()
	{
		if ( this._extreme[1] < this._extreme[0] )
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
		this.checkExtremes();
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
		this.checkExtremes();
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
	 * \brief Set the length of this dimension.
	 * 
	 * @param length Positive {@code double}.
	 */
	public void setLength(double length)
	{
		this._extreme[1] = this._extreme[0] + length;
		this.checkExtremes();
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
		Boundary b1 = new BoundaryCyclic();
		Boundary b2 = b1.makePartnerBoundary();
		this.setBoundaries(b1, b2);
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
	
	/**************************************************************************
	 * BOUNDARIES
	 *************************************************************************/
	
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
	public void setBoundary(Boundary aBoundary, int index)
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
	public void setBoundaries(Boundary minBndry, Boundary maxBndry)
	{
		this._boundary[0] = minBndry;
		this._boundary[1] = maxBndry;
	}
	
	/**
	 * \brief Get an array of boundaries. 
	 * 
	 * @return 2-array of {@code Boundary} objects.
	 */
	public Boundary[] getBoundaries()
	{
		return this._boundary;
	}
	
	/**
	 * \brief Get the {@code Boundary} at the required extreme.
	 * 
	 * @param extreme Which extreme to check: 0 for minimum, 1 for maximum.
	 * @return The {@code Boundary} at the required extreme.
	 */
	public Boundary getBoundary(int extreme)
	{
		return this._boundary[extreme];
	}
	
	/**
	 * \brief Report whether the boundary on the given extreme is defined.
	 * 
	 * @param extreme Which extreme to check: 0 for minimum, 1 for maximum.
	 * @return True if the boundary is defined, null if it is not.
	 */
	public boolean isBoundaryDefined(int extreme)
	{
		return this._boundary[extreme] != null;
	}
	
	/**************************************************************************
	 * USEFUL METHODS
	 *************************************************************************/
	
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
		// TODO check that a and b are inside?
		double out = a - b;
		if ( this._isCyclic &&  (Math.abs(out) > 0.5 * this.getLength()) )
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
	
	/**************************************************************************
	 * PRE-LAUNCH CHECK
	 *************************************************************************/
	
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
				if ( ! this._boundary[i].isReadyForLaunch() )
					return false;
			}
		}
		return true;
	}

	@Override
	public ModelNode getNode() {
		
		ModelNode modelNode = new ModelNode(this.defaultXmlTag(), this);
		modelNode.requirement = Requirements.ZERO_TO_MANY;
		modelNode.add(new ModelAttribute(XMLRef.nameAttribute, 
										this._dimName.name(), null, false ));
		modelNode.add(new ModelAttribute(XMLRef.IS_CYCLIC, 
				String.valueOf(this._isCyclic), null, false ));
		modelNode.add(new ModelAttribute(XMLRef.targetResolutionAttribute, 
				String.valueOf(this._targetRes), null, false ));
		modelNode.add(new ModelAttribute(XMLRef.min, 
				String.valueOf(this._extreme[0]), null, false ));
		modelNode.add(new ModelAttribute(XMLRef.max, 
				String.valueOf(this._extreme[1]), null, false ));


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
		return XMLRef.shapeDimension;
	}

	@Override
	public int compareTo(Dimension o) {
		return this._dimName.compareTo(o._dimName);
	}
}