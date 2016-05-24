package shape.resolution;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.DoubleFunction;

import org.jdom.IllegalAddException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dataIO.Log;
import dataIO.Log.Tier;
import dataIO.XmlHandler;
import dataIO.XmlLabel;
import generalInterfaces.XMLable;
import grid.PolarGrid;
import shape.resolution.ResolutionCalculator.ResCalc;
import shape.resolution.ResolutionCalculator.UniformResolution;
import shape.ShapeConventions.DimName;

/**
 * \brief A factory class for different resolution calculators and different 
 * spatial grids.
 * 
 * @author Stefan Lang (stefan.lang@uni-jena.de)
 *     Friedrich-Schiller University Jena, Germany
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public final class ResCalcFactory implements XMLable
{
	/**
	 * 
	 */
	private DimName[] _dimNames;
	/**
	 * 
	 */
	private Class<?>[] _resClasses = new Class<?>[3];
	/**
	 * 
	 */
	private Object[] _resObjects = new Object[3];
	
	public ResCalcFactory(DimName[] dimNames)
	{
		this._dimNames = dimNames;
	}
	
	@Override
	public void init(Element xmlElem)
	{
		String resolution_val = "";
		/* Try to fetch targetResolution from solutes element. */
		if ( xmlElem != null )
		{
			resolution_val = XmlHandler.gatherAttribute(
							xmlElem, XmlLabel.targetResolutionAttribute);
		}
		/* 
		 * If no solutes node is given, or targetResolutionAttribute undefined
		 * then create a uniform resolution with res=1 in all dimensions.
		 */
		if ( xmlElem == null || resolution_val == "" )
			resolution_val = "1";
		/* 
		 * Set up default resolution objects and values (these can actually be
		 * overwritten by succeeding operations).
		 */
		for ( int dim = 0; dim < 3; dim++ )
		{
			this._resObjects[dim] =  Double.valueOf(resolution_val);
			this._resClasses[dim] = UniformResolution.class;
		}
		/* Get all references to resolution. */
		NodeList resList = XmlHandler.getAll(xmlElem, XmlLabel.resolution);
		/* If no resolution elements are defined at all, we are done. */
		if ( resList.getLength() == 0 )
			return;
		/* A list of dimensions that have already been set. */
		HashSet<DimName> dimNames = new HashSet<DimName>();
		/* Loop through all resolution elements and overwrite resolutions. */
		Element resE;
		for ( int i = 0; i < resList.getLength(); i++ )
		{
			resE = (Element) resList.item(i);
			/* Determine the dimensions to set this resolution element for. */
			String[] dimensions = XmlHandler.gatherAttribute(resE,
					XmlLabel.dimensionNamesAttribute).split(",");
			
			if ( (dimensions.length == 1) && (dimensions[0].equals("")) )
			{
				throw new IllegalArgumentException("A resolution element needs"
						+" to be connected to at least one dimension.");
			}
			/* Loop over the dimensions. */
			for ( int j = 0; j < dimensions.length; j++ )
			{
				DimName dimName = DimName.valueOf(dimensions[j]);
				/* Test if dimension was already set. */
				if ( dimNames.contains(dimName) )
				{
					Log.out(Tier.CRITICAL,
									"Tried to set resolution of dimension "
									+dimName+" twice, ignoring last value.");
					continue;
				}
				/* Test if created resolutions fit the shape's dimNames. */
				for ( DimName aDim : this._dimNames )
					if ( ! aDim.equals(dimName) )
					{
						Log.out(Tier.CRITICAL, "Tried to set resolution of "
								+"dimension "+dimName+", but the dimension is "
								+"not present in the current shape, ignoring");	
						continue;
					}
				dimNames.add(dimName);
				/* try to fetch class. */
				String class_name = XmlHandler.gatherAttribute(resE,
													XmlLabel.classAttribute);
				if ( ! class_name.isEmpty() )
				{
					try
					{
						this._resClasses[j] = Class.forName(class_name);
					}
					catch (ClassNotFoundException e)
					{
						 /* remember that res_classes is already initialized 
						  * with uniform resolutions by default*/
						Log.out(Tier.CRITICAL, "Resolution calculator class "
								+ class_name + " not found. continuing with "
								+"uniform resolution calculator in dimension "
								+ dimensions[j] + " for now");
						continue;
					}
				}					
				/* Try to fetch targetResolution attribute. */
				resolution_val = XmlHandler.gatherAttribute(
									resE, XmlLabel.targetResolutionAttribute);
				if ( resolution_val.equals("") )
				{
					/* try to fetch resolution object (not used at the moment)*/
					Element exE = 
							XmlHandler.loadUnique(resE, XmlLabel.expression);
					if ( exE == null )
					{
						// ... more resolution objects can be added here
					}
					else
					{
						//TODO: move from DoubleFunction to expression.
						//ExpressionB ex = new ExpressionB(resE);
						// ... not used at the moment.
					}
					Log.out(Tier.CRITICAL, "Tried to set a resolution that is"
							+ "not parsable as double - using default value");
				}
				else
					this._resObjects[j] = Double.valueOf(resolution_val);
			}
		}
		
		/* make some sanity tests */
		//TODO: ensure right order of res objects and classes from this.dimNames
		for ( DimName dimName : dimNames )
		{
			/* Test if created resolutions fit the shape's dimNames. */
			for ( int i = 0; i < this._dimNames.length; i++ )
				if ( ! this._dimNames[i].equals(dimName) )
				{
					throw new IllegalAddException("Tried to set resolution of "
							+"dimension "+dimName+", but the dimension is not "
							+"present in the current shape");	
				}	
		}
	}
	
	@Override
	public String getXml() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public DimName[] getDimNames()
	{
		return _dimNames;
	}
	
	// NOTE Could move these 2 methods to DimName
	public static boolean isAngular(DimName dimName)
	{
		return (dimName == DimName.THETA) || (dimName == DimName.PHI);
	}
	
	public static boolean isLinear(DimName dimName)
	{
		return (dimName == DimName.X) || (dimName == DimName.Y) || 
				(dimName == DimName.Z) || (dimName == DimName.R);
	}
	
	/**********************************************************************/
	/************************* DIMENSION LEVEL ****************************/
	/**********************************************************************/

	/* generic for all currently implemented grids */

	//TODO: determine resCalc class automatically if null argument.

	/**
	 * \brief Creates a resolution calculator object for a given array 
	 * of dimensions.
	 * 
	 * The result will be one of {@code ResCalc[]}, {@code ResCalc[][]} 
	 * or {@code ResCalc[][][]}, 
	 * depending on the dimensions themselves and their combinations. 
	 * 
	 * @param dims An array of dimensions.
	 * @param totalLength The totalLength of the grid in each dimension.
	 * @param res A valid resolution object for each dimension.
	 * @param resCalcClasses The desired ResCalc class in each dimension.
	 * @return
	 */
	public Object[] createResCalcForDimensions(double[] totalLength)
	{
		Object[] out = new Object[3];
		ArrayList<Object> dimArgs = new ArrayList<>();
		for ( int dim = 0; dim < 3; dim++ )
		{
			/* 
			 * Add the last resCalc to arguments for polar dimensions. 
			 * This has to be done because in polar grids resolutions in one
			 * dimension depend on their preceding dimension(s).
			 */
			if ( isAngular(this._dimNames[dim]) && (dim > 0) )
				dimArgs.add(out[dim-1]);
			/* 
			 * Create the appropriate ResCalc object for the current 
			 * dimension. 
			 */ 
			out[dim] = createResCalcForDimension(
							this._dimNames[dim], dimArgs,
							(dim < totalLength.length ?  totalLength[dim] : 1),
							this._resObjects[dim], this._resClasses[dim]);
		}
		return out;
	}

	/**
	 * \brief Creates a resolution calculator array for a given dimension 
	 * with given arguments. 
	 * 
	 * The result will be one of {@code ResCalc}, {@code ResCalc[]} or
	 * {@code ResCalc[][]} depending on the dimension and the number of 
	 * arguments for that dimension. The arguments need to be a resolution 
	 * calculator for shell-only-dependent dimensions (θ for the 
	 * cylinder and φ for the sphere) or a resolution calculator and a 
	 * resolution calculator array for shell- and ring- dependent dimensions 
	 * (θ in the sphere).  
	 * 
	 * 
	 * @param dim A dimension name.
	 * @param dimArgs The required resolution calculators for that dimension.
	 * @param totalLength The totalLength of the grid.
	 * @param res A valid resolution object.
	 * @param resCalcClass The desired ResCalc class.
	 * @return
	 */
	private static Object createResCalcForDimension( 
				DimName dim, ArrayList<Object> dimArgs, double totalLength,
				Object res, Class<?> resCalcClass)
	{
		try
		{
			/* Get new ResCalc instance. */
			Object rC = resCalcClass.newInstance(); 
			/* Fetch class of resolution object. */
			Class<?> resClass = res.getClass();
			/* Cast Integer to double. */
			if ( resClass.equals(Integer.class) )
			{
				res = Double.valueOf((Integer) res);
				resClass = double.class;
			}
			/* Cast Double to double. */
			if ( resClass.equals(Double.class) )
				resClass = double.class;
			/* 
			 * getClass() will not determine the functional interface,
			 * so do it manually here.
			 */
			if ( res instanceof DoubleFunction )
				resClass = DoubleFunction.class;
			/* Get suitable initialising method. */
			Method init = 
						resCalcClass.getMethod("init", resClass, double.class);
			/* For cartesian(-like) dimensions just call init method. */
			if ( isLinear(dim) )
			{
				init.invoke(rC, res, totalLength);
				return rC;
			}
			/* For polar dimensions we have to do some more stuff... */
			if ( isAngular(dim) )
			{
				/* Determine the number of shells we have from the dimArgs. */
				int nShells = ((ResCalc) dimArgs.get(0)).getNVoxel();
				/* 
				 * init shell-only-dependent dimension 
				 * this is theta for the cylinder and phi for the sphere.
				 */
				Object[] rC_oneDim = new ResCalc[nShells];
				for ( int shell = 0; shell < nShells; shell++ )
				{
					rC = resCalcClass.newInstance();
					/* scale resolution for shell 
					 * (using the static method provided by PolarGrid)
					 */
					Object scaled_res = manipulateResolutionObject(res, shell);
					/* initialize ResCalc appropriately and save in array */
					init.invoke(rC, scaled_res, totalLength);
					rC_oneDim[shell] = rC;
				}
				/* 
				 * used to discriminate whether this is 
				 * THETA of cylinder (shell-only-dependent) or 
				 * PHI of sphere (shell-only-dependent) or
				 * THETA of sphere (shell and ring dependent).
				 * TODO: some safety would be more user-friendly I guess
				 * 
				 * If this is to be computed for a shell-only-dependent 
				 * dimension, then we are done.
				 */
				if ( dimArgs.size() == 1 || dim==DimName.PHI )
					return rC_oneDim;
				/* 
				 * Only needed for theta dimension of sphere.
				 */
				Object[][] rC_twoDim = new ResCalc[nShells][];
				/* Get resolution calculator of phi dimension from dimArgs. */
				ResCalc[] rC_phi = ((ResCalc[]) dimArgs.get(1));
				for ( int shell = 0; shell < nShells; shell++ )
				{
					/* Fetch number of rings. */
					int nRings = rC_phi[shell].getNVoxel();
					/* Set up ring. */
					rC_twoDim[shell] = new ResCalc[nRings];
					for ( int ring = 0; ring < nRings; ring++ )
					{
						rC = resCalcClass.newInstance(); 
						/* 
						 * Scale resolution for shell and ring
						 * (using the static method provided by PolarGrid)
						 */
						Object scaled_res = 
								manipulateResolutionObject(res, shell, ring);
						init.invoke(rC, scaled_res, totalLength);
						/* Initialize ResCalc appropriately and save in array. */
						rC_twoDim[shell][ring] = (ResCalc) rC;
					}
				}
				return rC_twoDim;
			}
		} catch (InvocationTargetException e){
			/* lets only report the causing error here */
			e.getCause().printStackTrace();
		} catch (NoSuchMethodException | SecurityException 
				| IllegalAccessException | IllegalArgumentException
				| InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private static Object manipulateResolutionObject(Object res, int shell)
	{
		if ( res instanceof DoubleFunction )
		{
			//TODO safety
			@SuppressWarnings("unchecked")
			DoubleFunction<Double> r = (DoubleFunction<Double>)res;
			DoubleFunction<Double> fun = 
					x -> PolarGrid.scaleResolutionForShell(shell, r.apply(x));
					return fun;
		}
		else if ( res instanceof double[] )
		{
			double[] r = (double[]) res;
			for ( int i = 0; i < r.length; i++ )
				r[i] = PolarGrid.scaleResolutionForShell(shell, r[i]);
			return r;
		}
		else if (res instanceof Double)
		{
			double r = (double) res;
			return PolarGrid.scaleResolutionForShell(shell, r);
		} 
		return null;
	}
	
	/**
	 * \brief Change a resolution-specifying object according to the 
	 * 
	 * @param res
	 * @param shell
	 * @param ring
	 * @return Object with the same class as <b>res</b>.
	 */
	private static Object manipulateResolutionObject(Object res, int shell, int ring)
	{
		if ( res instanceof DoubleFunction )
		{
			//TODO safety
			@SuppressWarnings("unchecked")
			DoubleFunction<Double> r = (DoubleFunction<Double>)res;
			DoubleFunction<Double> fun = 
					x -> PolarGrid.scaleResolutionForRing(
							shell, ring, r.apply(x));
					return fun;
		}
		else if ( res instanceof double[] )
		{
			double[] r = (double[]) res;
			for ( int i = 0; i < r.length; i++ )
				r[i] = PolarGrid.scaleResolutionForRing(shell, ring, r[i]);
			return r;
		}
		else if (res instanceof Double)
		{
			double r = (double) res;
			return PolarGrid.scaleResolutionForRing(shell, ring, r);
		}
		return null;
	}
}