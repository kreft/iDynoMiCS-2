package grid;

import java.util.HashMap;

import linearAlgebra.Array;
import shape.Shape;

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
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class SpatialGrid
{	
	/**
	 * Label for an array. 
	 */
	public enum ArrayType
	{
		/**
		 * The concentration of, e.g., a solute.
		 */
		CONCN,
		/**
		 * The diffusion coefficient of a solute. For example, this may be
		 * lower inside a biofilm than in the surrounding water.
		 */
		DIFFUSIVITY,
		/**
		 * A measure of how well-mixed a solute is. A diffusion-reaction should
		 * ignore where this is above a certain threshold.
		 */
		WELLMIXED,
		/**
		 * The rate of production of this solute. Consumption is described by
		 * negative production.
		 */
		PRODUCTIONRATE,
		/**
		 * The differential of production rate with respect to its
		 * concentration.
		 */
		DIFFPRODUCTIONRATE,
		/**
		 * Laplacian operator.
		 */
		LOPERATOR;
	}
	
	/**
	 * Dictionary of arrays according to their type. Note that not all types
	 * may be occupied.
	 */
	protected HashMap<ArrayType, double[][][]> _array
									= new HashMap<ArrayType, double[][][]>();
									
	protected Shape _shape;
	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public SpatialGrid(Shape shape)
	{
		this._shape = shape;
	}
	
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
	 * @param type Type of array sought (e.g. CONCN).
	 * @return {@code true} if this array is already initialised in this grid,
	 * {@code false} otherwise.
	 */
	public boolean hasArray(ArrayType type)
	{
		return this._array.containsKey(type);
	}

	public Shape getShape()
	{
		return this._shape;
	}

	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public double[][][] getArray(ArrayType type)
	{
		return Array.copy(this._array.get(type));
	}

	
	/*************************************************************************
	 * VOXEL GETTERS & SETTERS
	 ************************************************************************/
	
	/**
	 * \brief Gets the value of one coordinate on the given array type.
	 * 
	 * @param type Type of array to get from.
	 * @param coord Coordinate on this array to get.
	 * @return double value at this coordinate on this array.
	 */
	public double getValueAt(ArrayType type, int[] coord)
	{
		if ( this._array.containsKey(type) )
			return this._array.get(type)[coord[0]][coord[1]][coord[2]];
		else
			return Double.NaN;
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @param coord
	 * @param value
	 */
	public void setValueAt(ArrayType type, int[] coord, double value)
	{
		if ( this._array.containsKey(type) )
			this._array.get(type)[coord[0]][coord[1]][coord[2]] = value;
		// TODO safety?
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @param coord
	 * @param value
	 */
	public void addValueAt(ArrayType type, int[] coord, double value)
	{
		if ( this._array.containsKey(type) )
			this._array.get(type)[coord[0]][coord[1]][coord[2]] += value;
		// TODO safety?
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @param coord
	 * @param value
	 */
	public void timesValueAt(ArrayType type, int[] coord, double value)
	{
		if ( this._array.containsKey(type) )
			this._array.get(type)[coord[0]][coord[1]][coord[2]] *= value;
		// TODO safety?
	}
	
	/*************************************************************************
	 * ARRAY SETTERS
	 ************************************************************************/
	
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
	 * \brief TODO
	 * 
	 * @param type
	 * @param array
	 */
	public void setTo(ArrayType type, double[][][] array)
	{
		Array.copyTo(this._array.get(type), array);
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
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
	
	/*************************************************************************
	 * ARRAY GETTERS
	 ************************************************************************/
	
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
	
	/*************************************************************************
	 * TWO-ARRAY METHODS
	 ************************************************************************/
	
	public void addArrayToArray(ArrayType destination, ArrayType source)
	{
		Array.add(this._array.get(destination), this._array.get(source));
	}
	
	/*************************************************************************
	 * LOCATION GETTERS
	 ************************************************************************/
	
	/**
	 * 
	 * @param name
	 * @param location
	 * @return
	 */
	public double getValueAt(ArrayType type, double[] location)
	{
		return this.getValueAt(type, this._shape.getCoords(location));
	}
		
	/**
	 * \brief Get the value of the given array in the 
	 * 
	 * @param type
	 * @return
	 */
	public double getValueAtCurrent(ArrayType type)
	{
		return this.getValueAt(type, this._shape.iteratorCurrent());
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @return
	 */
	public double getValueAtNhb(ArrayType type)
	{
		return this.getValueAt(type, this._shape.nbhIteratorCurrent());
	}
	
	/**
	 * \brief TODO
	 * 
	 * @param type
	 * @param value
	 */
	public void setValueAtCurrent(ArrayType type, double value)
	{
		this.setValueAt(type, this._shape.iteratorCurrent(), value);
	}
			
	/**
	 * 
	 * TODO safety if neighbor iterator or arrays are not initialised.
	 * 
	 * @return
	 */
	public double getFluxWithNeighbor(String soluteName)
	{
		Shape shape = this._shape;
		// FIXME an invalid neighbor is not the same as one on the boundary!!!
		if ( shape.isNhbIteratorInside() )
		{
			/*
			 * First find the difference in concentration.
			 */
			double out = this.getValueAtNhb(ArrayType.CONCN)
					- this.getValueAtCurrent(ArrayType.CONCN);
			/*
			 * Then multiply this by the average diffusivity.
			 */
			out *= meanDiffusivity(
				this.getValueAtCurrent(ArrayType.DIFFUSIVITY),
				this.getValueAtNhb(ArrayType.DIFFUSIVITY));
			/*
			 * Finally, multiply by the surface are the two voxels share (in
			 * square microns).
			 */
			// TODO Rob: I need to change this
			out /= shape.nbhCurrSharedArea();
			return out;
		}
		else
		{
			double flux = shape.nbhIteratorOutside()
							.getGridMethod(soluteName).getBoundaryFlux(this);
			//System.out.println("method: "+flux); //bughunt
			return flux;
		}
	}
	
	private static double meanDiffusivity(double a, double b)
	{
		if ( a == 0.0 || b == 0.0 )
			return 0.0;
		if ( a == Double.POSITIVE_INFINITY )
			return b;
		if ( b == Double.POSITIVE_INFINITY )
			return a;
		/*
		 * This is a computationally nicer way of getting the harmonic mean:
		 * 2 / ( (1/a) + (1/b))
		 */
		return 2.0 * ( a * b ) / ( a + b );
	}
	
	/*************************************************************************
	 * REPORTING
	 ************************************************************************/
	
	public void rowToBuffer(double[] row, StringBuffer buffer)
	{
		for ( int i = 0; i < row.length - 1; i++ )
			buffer.append(row[i]+", ");
		buffer.append(row[row.length-1]);
	}
	
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
	
	public StringBuffer arrayAsBuffer(ArrayType type)
	{
		StringBuffer out = new StringBuffer();
		double[][][] array = this._array.get(type);
		for ( int i = 0; i < array.length - 1; i++ )
		{
			matrixToBuffer(array[i], out);
			if ( array[i].length == 1 )
				out.append(", ");
			else
				out.append("\n");
		}
		matrixToBuffer(array[array.length - 1], out);
		return out;
	}
	
	public String arrayAsText(ArrayType type)
	{
		return this.arrayAsBuffer(type).toString();
	}
}
