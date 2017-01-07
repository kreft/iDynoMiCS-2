package grid;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.ObjectFactory;
import dataIO.XmlHandler;
import idynomics.EnvironmentContainer;
import instantiable.Instantiable;
import dataIO.Log.Tier;
import linearAlgebra.Array;
import linearAlgebra.Matrix;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import settable.Attribute;
import settable.Module;
import settable.Settable;
import settable.Module.Requirements;
import shape.Shape;
import utility.ExtraMath;

/**
 * \brief A SpatialGrid stores information about a variable over space.
 * 
 * <p>A typical example is the concentration of a dissolved compound, often
 * referred to as a solute. A spatial grid describing the concentration of a
 * solute may also describe the diffusivity of this solute, as well as any
 * other necessary information about it that varies in space. Each type of
 * information, including concentration, is kept in a separate array. All
 * these arrays have identical dimensions and resolutions, so that voxels
 * overlap exactly.</p>
 * 
 * <p>Since all the arrays in a SpatialGrid line up, it is possible to iterate
 * over all voxels in a straightforward manner. On top of this, we can also
 * iterate over all neighbouring voxels of the voxel the main iterator is
 * currently focused on.</p>
 * 
 * <p>On the boundaries of the grid, </p>
 * 
 * @author Robert Clegg (r.j.clegg@bham.ac.uk) University of Birmingham, U.K.
 * @author Stefan Lang (stefan.lang@uni-jena.de)
 * 								Friedrich-Schiller University Jena, Germany 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 */

public class SpatialGrid implements Settable, Instantiable
{
	/**
	 * The name of the variable which this grid represents.
	 */
	protected String _name;
	/**
	 * 
	 */
	protected Shape _shape;
	/**
	 * Dictionary of arrays according to their type. Note that not all types
	 * may be occupied.
	 */
	protected Map<ArrayType, double[][][]> _array = 
			new HashMap<ArrayType, double[][][]>();
	/**
	 * TODO
	 */
	protected double _wellmixedFlow = 0.0;
	
	/**
	 * identifies what compartment hosts this grid
	 */
	protected Settable _parentNode;
	
	/**
	 * \brief Log file verbosity level used for debugging the getting of
	 * values.
	 * 
	 * <ul><li>Set to {@code BULK} for normal simulations</li>
	 * <li>Set to {@code DEBUG} when trying to debug an issue</li></ul>
	 */
	protected static final Tier GET_VALUE_LEVEL = Tier.BULK;
	/**
	 * \brief Log file verbosity level used for debugging the setting of
	 * values.
	 * 
	 * <ul><li>Set to {@code BULK} for normal simulations</li>
	 * <li>Set to {@code DEBUG} when trying to debug an issue</li></ul>
	 */
	protected static final Tier SET_VALUE_LEVEL = Tier.BULK;
	/**
	 * \brief Log file verbosity level used for debugging the flux from the
	 * neighbor iterator voxel into the current iterator voxel.
	 * 
	 * <ul><li>Set to {@code BULK} for normal simulations</li>
	 * <li>Set to {@code DEBUG} when trying to debug an issue</li></ul>
	 */
	protected static final Tier GET_FLUX_WITH_NHB_LEVEL = Tier.BULK;
	
	/* ***********************************************************************
	 * CONSTRUCTORS
	 * **********************************************************************/

	/**
	 * \brief Construct a new grid.
	 * NOTE only used by dummy grid
	 * 
	 * @param shape Shape of the grid.
	 * @param name Name of the variable this represents.
	 */
	public SpatialGrid(Shape shape, String name, Settable parent)
	{
		this._shape = shape;
		this._name = name;
		this._parentNode = parent;
	}
	
	/**
	 * NOTE Only used by unit tests, consider restructuring tests
	 * @param shape
	 * @param name
	 * @param environment
	 */
	public SpatialGrid(String name, double concentration, Settable parent)
	{
		this._shape = ((EnvironmentContainer) parent).getShape();
		this._name = name;
		this._parentNode = parent;
		this.newArray(ArrayType.CONCN, concentration);
	}
	
	public SpatialGrid(Element xmlElem, Settable parent)
	{
		this.instantiate(xmlElem, parent);
	}
	
	public SpatialGrid() { 
		//NOTE only used for ClassRef
	}

	public void instantiate(Element xmlElem, Settable parent)
	{
		this._shape = ((EnvironmentContainer) parent).getShape();
		this._parentNode = parent;
		this._name = XmlHandler.obtainAttribute(xmlElem, 
				XmlRef.nameAttribute, this.defaultXmlTag());
		this.newArray(ArrayType.CONCN, 0.0);
		String conc = XmlHandler.obtainAttribute((Element) xmlElem, 
				XmlRef.concentration, this.defaultXmlTag());
		this.setTo(ArrayType.CONCN, conc);
	}

	/* ***********************************************************************
	 * 							BASIC GETTERS & SETTERS
	 * ***********************************************************************/
	
	/**
	 * @return The name of the variable this grid represents.
	 */
	public String getName()
	{
		return this._name;
	}
	
	/**
	 * @return The shape of this grid.
	 */
	public Shape getShape()
	{
		return this._shape;
	}
	
	/**
	 * @return Types of all arrays present in this grid.
	 */
	public Set<ArrayType> getAllArrayTypes()
	{
		return this._array.keySet();
	}
	
	/* ***********************************************************************
	 * 							ARRAY INITIALISATION
	 * ***********************************************************************/
	
	/**
	 * \brief Initialise an array of the given <b>type</b> and fill all voxels
	 * with <b>initialValues</b>.
	 * 
	 * <p>If the array is already initialised, this simply fills it with
	 * <b>initialValues</b>.</p>
	 * 
	 * @param type {@code ArrayType} for the new array.
	 * @param initialValues {@code double} for every voxel to take.
	 */
	public void newArray(ArrayType type, double initialValues)
	{
		/*
		 * Try resetting all values of this array. If it doesn't exist yet,
		 * make it.
		 */
		if ( this.hasArray(type) )
			Array.setAll(this._array.get(type), initialValues);
		else
			this._array.put(type, this._shape.getNewArray(initialValues));
	}
	
	/**
	 * \brief Initialise an array of the given <b>type</b> and fill it with
	 * zeros.
	 * 
	 * <p>If the array is already initialised, this simply resets it.</p>
	 * 
	 * @param type {@code ArrayType} for the new array.
	 */
	public void newArray(ArrayType type)
	{
		this.newArray(type, 0.0);
	}
	
	/**
	 * \brief Whether this grid has an array of the type specified.
	 * 
	 * @param type Type of array sought.
	 * @return {@code true} if this array is already initialised in this grid,
	 * {@code false} otherwise.
	 */
	public boolean hasArray(ArrayType type)
	{
		return this._array.containsKey(type);
	}

	/**
	 * \brief Get a copy of an array held in this grid.
	 * 
	 * @param type The type of array required.
	 * @return Copy of this array.
	 */
	public double[][][] getArray(ArrayType type)
	{
		return Array.copy(this._array.get(type));
	}

	/* ***********************************************************************
	 * 							ARRAY SETTERS
	 * ***********************************************************************/
	
	/**
	 * \brief Set all values in the array specified to the <b>value</b> given.
	 * 
	 * @param type Type of the array to set.
	 * @param value New value for all elements of this array.
	 */
	public void setAllTo(ArrayType type, double value)
	{
		Array.setAll(this._array.get(type), value);
	}
	
	/**
	 * \brief Overwrite all values in the array specified to those of the array
	 * given, discarding all old values.
	 * 
	 * @param type Type of the array to set.
	 * @param array Array of values to use.
	 */
	public void setTo(ArrayType type, double[][][] array)
	{
		Array.copyTo(this._array.get(type), array);
	}
	
	/**
	 * \brief Set an array from a string.
	 * 
	 * @param type Type of the array to set.
	 * @param array String representation of the values in this array: either
	 * a single value, or conforming to the approach used in the
	 * {@code linearAlgebra} package.
	 */
	public void setTo(ArrayType type, String array)
	{
		if ( array.contains(Vector.DELIMITER) || 
				array.contains(Matrix.DELIMITER) ||
				array.contains(Array.DELIMITER) )
		{
			this.setTo( type, Array.dblFromString(array) );
		}
		else
			this.setAllTo( type, Double.valueOf(array) );
	}
	/**
	 * \brief Force all values in the given array type to be at least zero.
	 * 
	 * @param type Type of the array to use.
	 */
	public void makeNonnegative(ArrayType type)
	{
		Array.makeNonnegative(this._array.get(type));
	}
	
	/**
	 * \brief Increase all values in the array specified by the <b>value</b>
	 * given.
	 * 
	 * <p>To decrease all elements of this array (i.e. subtract), simply use
	 * {@code addToAll(type, -value)}.</p>
	 * 
	 * @param type Type of the array to use.
	 * @param value New value to add to all elements of this array.
	 */
	public void addToAll(ArrayType type, double value)
	{
		Array.addEquals(this._array.get(type), value);
	}
	
	/**
	 * \brief Multiply all values in the array specified by the <b>value</b>
	 * given.
	 * 
	 * <p>To divide all elements of this array, simply use
	 * {@code timesAll(type, 1.0/value)}.</p>
	 * 
	 * @param type Type of the array to use.
	 * @param value New value with which to multiply all elements of this array.
	 */
	public void timesAll(ArrayType type, double value)
	{
		Array.timesEquals(this._array.get(type), value);
	}
	
	/* ***********************************************************************
	 * 							ARRAY GETTERS
	 * ***********************************************************************/
	
	/**
	 * \brief Get the greatest value in the given array.
	 * 
	 * @param type Type of the array to use.
	 * @return Greatest value of all the elements of the array <b>type</b>.
	 */
	public double getMax(ArrayType type)
	{
		return Array.max(this._array.get(type));
	}
	
	/**
	 * \brief Get the least value in the given array.
	 * 
	 * @param type Type of the array to use.
	 * @return Least value of all the elements of the array <b>type</b>.
	 */
	public double getMin(ArrayType type)
	{
		return Array.min(this._array.get(type));
	}
	
	/**
	 * \brief Get the arithmetic mean value in the given array.
	 * 
	 * @param type Type of the array to use.
	 * @return Average value of all the elements of the array <b>type</b>.
	 */
	// FIXME this currently ignores voxel volumes.
	public double getAverage(ArrayType type)
	{
		return Array.meanArith(this._array.get(type));
	}
	
	/**
	 * \brief Get the sum of the values in the array of given <b>type</b>.
	 * 
	 * @param type Type of the array to use.
	 * @return Total value of all the elements of the array <b>type</b>.
	 */
	// FIXME this currently ignores voxel volumes.
	public double getTotal(ArrayType type)
	{
		return Array.sum(this._array.get(type));
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param array
	 * @param type
	 * @return
	 */
	public double getTotalAbsDiffWith(double[][][] array, ArrayType type)
	{
		return Array.totalAbsDifference(array, this.getArray(type));
	}
	
	/* ***********************************************************************
	 * 							TWO ARRAY METHODS
	 * ***********************************************************************/
	
	/**
	 * \brief Add all elements of one array to those of another,
	 * element-by-element.
	 * 
	 * @param destination Type of array to be overwritten with its own values
	 * plus those of <b>source</b>.
	 * @param source Type of array to use in incrementing <b>destination</b>.
	 * The values of this array are preserved in this method.
	 */
	public void addArrayToArray(ArrayType destination, ArrayType source)
	{
		Array.addEquals(this._array.get(destination), this._array.get(source));
	}

	/* ***********************************************************************
	 * 							VOXEL GETTERS & SETTERS
	 * ***********************************************************************/
	
	/**
	 * \brief Gets the value of one coordinate on the given array type.
	 * 
	 * @param type Type of array to get from.
	 * @param coord Coordinate on this array to get.
	 * @return double value at this coordinate on this array.
	 */
	public double getValueAt(ArrayType type, int[] coord)
	{
		if ( Log.shouldWrite(GET_VALUE_LEVEL) )
		{
			Log.out(GET_VALUE_LEVEL, "Trying to get value at coordinate "
					+ Vector.toString(coord) + " in "+ type);
		}
		if ( this._array.containsKey(type) )
		{
			if ( Log.shouldWrite(GET_VALUE_LEVEL) )
			{
				Log.out(GET_VALUE_LEVEL, "   returning " 
						+ this._array.get(type)[coord[0]][coord[1]][coord[2]]);
			}
			return this._array.get(type)[coord[0]][coord[1]][coord[2]];
		}
		else
		{
			//TODO: safety?
			if ( Log.shouldWrite(GET_VALUE_LEVEL) )
				Log.out(GET_VALUE_LEVEL, "   returning " + Double.NaN);
			return Double.NaN;
		}
	}
	
	/**
	 * \brief Sets the value of one voxel on the given array type.
	 * 
	 * @param type Type of array to set in.
	 * @param coord Coordinate of the voxel.
	 * @param value New value for the voxel.
	 */
	public void setValueAt(ArrayType type, int[] coord, double value)
	{
		if ( Log.shouldWrite(SET_VALUE_LEVEL) )
		{
			Log.out(SET_VALUE_LEVEL, "Trying to set value at coordinate "
					+ Vector.toString(coord) + " in "+ type + " to "+value);
		}
		this._array.get(type)[coord[0]][coord[1]][coord[2]] = value;
	}
	
	/**
	 * \brief Increases the value of one voxel on the given array type.
	 * 
	 * @param type Type of array to set in.
	 * @param coord Coordinate of the voxel.
	 * @param value Value to increase the voxel's current value by.
	 */
	public void addValueAt(ArrayType type, int[] coord, double value)
	{
		if ( Log.shouldWrite(SET_VALUE_LEVEL) )
		{
			Log.out(SET_VALUE_LEVEL, "Trying to add "+value+" at coordinate "
					+Vector.toString(coord)+" in "+type);
		}
		this._array.get(type)[coord[0]][coord[1]][coord[2]] += value;
	}
	
	/**
	 * \brief Multiplies the value of one voxel on the given array type.
	 * 
	 * @param type Type of array to set in.
	 * @param coord Coordinate of the voxel.
	 * @param value Value to multiply the voxel's current value by.
	 */
	public void timesValueAt(ArrayType type, int[] coord, double value)
	{
		if ( Log.shouldWrite(SET_VALUE_LEVEL) )
		{
			Log.out(SET_VALUE_LEVEL, "Trying to multiply with " + value 
					+ " at coordinate "+ Vector.toString(coord) + " in "+ type);
		}
		this._array.get(type)[coord[0]][coord[1]][coord[2]] *= value;
	}
	
	/* ***********************************************************************
	 * 							ITERATION
	 * ***********************************************************************/
	
	/**
	 * \brief Get the value of the given array where the current iterator voxel
	 * is.
	 * 
	 * @param type Type of array to get from.
	 * @return {@code double} value voxel.
	 */
	public double getValueAtCurrent(ArrayType type)
	{
		return this.getValueAt(type, this._shape.iteratorCurrent());
	}

	/**
	 * \brief Set the value at the voxel where the iterator is currently
	 * located.
	 * 
	 * @param type Type of array to set the value for.
	 * @param value 
	 */
	public void setValueAtCurrent(ArrayType type, double value)
	{
		this.setValueAt(type, this._shape.iteratorCurrent(), value);
	}

	/**
	 * \brief Get the value of the given array where the neighbor iterator
	 * voxel is.
	 * 
	 * @param type Type of array to get from.
	 * @return {@code double} value voxel.
	 */
	public double getValueAtNhb(ArrayType type)
	{
		if ( this._shape.isNbhIteratorInside() )
			return this.getValueAt(type, this._shape.nbhIteratorCurrent());
		else
		{
			throw new IndexOutOfBoundsException(
					"Tried to get grid value at neighbor"
							+ Vector.toString(this._shape.nbhIteratorCurrent())
							+ " of current coordinate "
							+ Vector.toString(this._shape.iteratorCurrent())
							+ ", but the neighbour is not inside the grid.");
		}
	}
	
	/**
	 * \brief Calculate the mass flow from the neighbor voxel into the current
	 * iterator voxel (may be negative).
	 * 
	 * <p>The flux from the neighboring voxel into the current one is given by
	 * the formula <br><i>(c<sub>nhb</sub> - c<sub>itr</sub>) *
	 * (D<sub>nhb</sub><sup>-1</sup> + D<sub>itr</sub><sup>-1</sup>)<sup>-1</sup>
	 *  * d<sub>nhb,itr</sub><sup>-1</sup></i><br>
	 * where subscript <i>itr</i> denotes the current iterator voxel and
	 * <i>nhb</i> the current neighbor voxel, and
	 * <ul>
	 * <li><i>c</i> is voxel concentration</li>
	 * <li><i>D</i> is voxel diffusivity</li>
	 * <li><i>d</i> is distance between centres of two voxels</li>
	 * </ul>
	 * The flux has units of mass or mole per area per unit time.</p>
	 * 
	 * <p>Note that we use the harmonic mean diffusivity, rather than the
	 * arithmetic or geometric.</p>
	 * 
	 * <p>The flow from the neighboring voxel into the current one is then
	 * the flux multiplied by the shared surface area, i.e.
	 * <i>flux * SA<sub>nhb,itr</sub></i> where <i>SA</i> is shared surface
	 * area of two voxels. Flow has units of mass/mole per unit time, and so
	 * should be divided by the volume of the current iterator voxel to give
	 * the rate of change of concentration to this voxel due to diffusive 
	 * flow.</p>
	 * 
	 * TODO Rob [8June2016]: I need to find the reference for this.
	 * 
	 * @return Diffusive flow from the neighbor voxel into the current iterator
	 * voxel, in units of mass (or mole) per time.
	 */
	// TODO safety if neighbor iterator or arrays are not initialised.
	public double getDiffusionFromNeighbor()
	{
		Tier level = Tier.BULK;
		if ( Log.shouldWrite(level) )
		{
			Log.out(level, " finding flow from nhb "+
					Vector.toString(this._shape.nbhIteratorCurrent())+
					" to curr "+Vector.toString(this._shape.iteratorCurrent()));
		}
		if ( this._shape.isNbhIteratorInside() )
		{
			/* Difference in concentration. */
			double concnDiff = this.getValueAtNhb(ArrayType.CONCN)
					- this.getValueAtCurrent(ArrayType.CONCN);
			/* Average diffusivity. */
			double diffusivity = ExtraMath.harmonicMean(
					this.getValueAtCurrent(ArrayType.DIFFUSIVITY),
					this.getValueAtNhb(ArrayType.DIFFUSIVITY));
			/* Surface are the two voxels share (in square microns). */
			double sArea = this._shape.nhbCurrSharedArea();
			/* Centre-centre distance. */
			double dist = this._shape.nhbCurrDistance();
			/* Calculate the the flux from these values. */
			double flux = concnDiff * diffusivity / dist ;
			double flow = flux * sArea;
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "    concnDiff is "+concnDiff);
				Log.out(level, "    diffusivity is "+diffusivity);
				Log.out(level, "    distance is "+dist);
				Log.out(level, "  => flux is "+flux);
				Log.out(level, "    surface area is "+sArea);
				Log.out(level, "  => flow is "+flow);
			}
			return flow;
		}
		else if ( this._shape.isIteratorValid() )
		{
			double flow = 
					this._shape.nbhIteratorOutside().getDiffusiveFlow(this);
			if ( Log.shouldWrite(level) )
				Log.out(level, "  got flow from boundary: "+flow);
			return flow;
		}
		else
		{
			Log.out(Tier.CRITICAL,
					"Trying to get flux with an invalid neighbor: current "+
					Vector.toString(this._shape.iteratorCurrent())+
					", neighbor "+
					Vector.toString(this._shape.nbhIteratorCurrent()));
			return Double.NaN;
		}
	}
	
	/**
	 * \brief Calculate the time-scale of the diffusion from the neighbor voxel
	 * into the current iterator voxel (always positive).
	 * 
	 * <p>The time-scale from the neighboring voxel into the current one is 
	 * given by the formula <i>SA<sub>nhb,itr</sub> * V<sub>itr</sub> *
	 * (D<sub>nhb</sub><sup>-1</sup> + D<sub>itr</sub><sup>-1</sup>)
	 *  * d<sub>nhb,itr</sub><sup>-1</sup></i>
	 * where subscript <i>itr</i> denotes the current iterator voxel and
	 * <i>nhb</i> the current neighbor voxel, and
	 * <ul>
	 * <li><i>SA</i> is shared surface area of two voxels</li>
	 * <li><i>V</i> is voxel volume</li>
	 * <li><i>D</i> is voxel diffusivity</li>
	 * <li><i>d</i> is distance between centres of two voxels</li>
	 * </ul>
	 * 
	 * @return Time-scale of the diffusive flow from the neighbor voxel into
	 * the current iterator voxel, in units of time.
	 */
	public double getDiffusiveTimeScaleWithNeighbor()
	{
		if ( this._shape.isNbhIteratorInside() )
		{
			/* Average diffusivity. */
			double diffusivity = ExtraMath.harmonicMean(
					this.getValueAtCurrent(ArrayType.DIFFUSIVITY),
					this.getValueAtNhb(ArrayType.DIFFUSIVITY));
			/* Surface are the two voxels share (in square microns). */
			double sArea = this._shape.nhbCurrSharedArea();
			/* Centre-centre distance. */
			double dist = this._shape.nhbCurrDistance();
			/* Current voxel volume. */
			double volume = this._shape.getCurrVoxelVolume();
			/* Calculate the the timescale from these values. */
			return dist * volume / (diffusivity * sArea);
		}
		else if ( this._shape.isIteratorValid() )
		{
			/* Average diffusivity. */
			double diffusivity = this.getValueAtCurrent(ArrayType.DIFFUSIVITY);
			/* Surface are the two voxels share (in square microns). */
			double sArea = this._shape.nhbCurrSharedArea();
			/* Centre-centre distance. */
			double dist = this._shape.nhbCurrDistance();
			/* Current voxel volume. */
			double volume = this._shape.getCurrVoxelVolume();
			/* Calculate the the timescale from these values. */
			return dist * volume / (diffusivity * sArea);
		}
		else
		{
			return Double.NaN;
		}
	}
	
	/**
	 * \brief Increase the grid's tally of mass flow into a well-mixed region.
	 * 
	 * @param flow Flow in units of mass (or moles) per time.
	 */
	public void increaseWellMixedMassFlow(double flow)
	{
		this._wellmixedFlow += flow;
	}
	
	/**
	 * @return This grid's tally of mass flow into a well-mixed region, in
	 * units of mass (or moles) per time.
	 */
	public double getWellMixedMassFlow()
	{
		return this._wellmixedFlow;
	}

	/**
	 * Reset this grid's tally of flow into a well-mixed region.
	 */
	public void resetWellMixedMassFlow()
	{
		this._wellmixedFlow = 0.0;
	}
	
	/* ***********************************************************************
	 * 							LOCATION GETTERS
	 * ***********************************************************************/
	
	/**
	 * \brief  Gets the value of one voxel on the given array type.
	 * 
	 * @param type Type of array required.
	 * @param location Vector of position in continuous space.
	 * @return Value of the given <b>type</b> in the voxel containing
	 * <b>location</b>.
	 */
	public double getValueAt(ArrayType type, double[] location)
	{
		return this.getValueAt(type, this._shape.getCoords(location));
	}
	
	/* ***********************************************************************
	 * 							REPORTING
	 * ***********************************************************************/
	
	/**
	 * \brief TODO
	 * 
	 * @param row
	 * @param buffer
	 */
	public void rowToBuffer(double[] row, StringBuffer buffer)
	{
		for ( int i = 0; i < row.length - 1; i++ )
			buffer.append(row[i]+", ");
		buffer.append(row[row.length-1]);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param matrix
	 * @param buffer
	 */
	public void matrixToBuffer(double[][] matrix, StringBuffer buffer)
	{
		for ( int i = 0; i < matrix.length - 1; i++ )
		{
			if ( matrix[i].length == 1 )
				buffer.append(matrix[i][0]+", ");
			else
			{
				rowToBuffer(matrix[i], buffer);
				buffer.append(";\n");
			}
		}
		rowToBuffer(matrix[matrix.length - 1], buffer);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @return
	 */
	public StringBuffer arrayAsBuffer(ArrayType type)
	{
		StringBuffer out = new StringBuffer();
		double[][][] array = this._array.get(type);
		for ( int i = 0; i < array.length - 1; i++ )
		{
			matrixToBuffer(array[i], out);
			// NOTE Rob [16June2016]: Consider always appending \n, as this can
			// be confusing in polar shapes.
			if ( array[i].length == 1 )
				out.append(", ");
			else
				out.append("\n");
		}
		matrixToBuffer(array[array.length - 1], out);
		return out;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @return
	 */
	// TODO explain why this is different to linearAlgebra.Array.toString()
	public String arrayAsText(ArrayType type)
	{
		return this.arrayAsBuffer(type).toString();
	}
	
	/* ***********************************************************************
	 * 							MODEL NODES
	 * ***********************************************************************/

	@Override
	public Module getModule()
	{
		Module modelNode = new Module(XmlRef.solute, this);
		modelNode.setRequirements(Requirements.ZERO_TO_MANY);
		
		modelNode.setTitle(this._name);
		
		modelNode.add(new Attribute(XmlRef.nameAttribute, 
				this._name, null, true ));
		
		modelNode.add(new Attribute(XmlRef.concentration, 
				ObjectFactory.stringRepresentation(
				this.getArray( ArrayType.CONCN )), null, true ));
		
		return modelNode;
	}

	@Override
	public void setModule(Module node)
	{
		this._name = node.getAttribute( XmlRef.nameAttribute ).getValue();
		this.setTo(ArrayType.CONCN, 
				node.getAttribute(XmlRef.concentration).getValue());
	}

	public void removeModule(String specifier) 
	{
		((EnvironmentContainer) this._parentNode).removeSolute(this);
	}

	@Override
	public String defaultXmlTag()
	{
		return XmlRef.solute;
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
