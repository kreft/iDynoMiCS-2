package solver.mgFas;

import java.util.LinkedList;

import org.w3c.dom.Element;

import aspect.AspectInterface;
import aspect.AspectReg;
import dataIO.XmlHandler;
import debugTools.QuickCSV;
import idynomics.Idynomics;
import instantiable.Instantiable;
import instantiable.object.InstantiableList;
import linearAlgebra.Array;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import settable.Settable;

public class RecordKeeper implements AspectInterface, Instantiable {

	public enum RecordType
	{
		CONCENTRATION,
		
		DIFFERENCENORM,
		
		DIFFERENCEMAX,
		
		DIFFERENCE,
	}
	private AspectReg _aspectRegistry = new AspectReg();
	
	protected RecordType _recordType;
	
	protected Integer _interval;
	
	protected Integer _counter;
	
	protected SoluteGrid _solute;
	
	public String _soluteName;
	
	protected String _difference;
	
	protected double[][][] _savedGrid;
	
	protected Integer _order;
	
	protected LinkedList<Double> _series = new LinkedList<Double>();
	
	public RecordType readRecordType (String recordType)
	{
		if (recordType.contentEquals("concentration"))
			return RecordType.CONCENTRATION;
		else if (recordType.contentEquals("differenceNorm"))
			return RecordType.DIFFERENCENORM;
		else if (recordType.contentEquals("differenceMax"))
			return RecordType.DIFFERENCEMAX;
		else if (recordType.contentEquals("difference"))
			return RecordType.DIFFERENCE;
		else
			Idynomics.simulator.interupt("No record type set. "
					+ "Returning concentration.");
		return RecordType.CONCENTRATION;
	}
	
	@Override
	public void instantiate(Element xmlElement, Settable parent) 
	{
		if (xmlElement != null)
			this.loadAspects(xmlElement);
		this._soluteName =  (String) this.getValue(AspectRef.solute);
		this._order = (Integer) this.getValue(AspectRef.order);
		this._recordType = readRecordType((String) this.getValue(AspectRef.recordType));
		this._interval = (Integer) this.getValue(AspectRef.interval);
		this._difference = (String) this.getOr(AspectRef.difference, "real");
		this._counter = 0;
	}
	
	public void step(double[][][] grid, Integer order, String soluteName)
	{
		double[][][] trimmedGrid = MultigridUtils.translateOut(grid);
		if (this._order == order && this._soluteName.contentEquals(soluteName))
		{
			if (this._savedGrid == null)
				this._savedGrid = new double[trimmedGrid.length]
						[trimmedGrid[0].length][trimmedGrid[0][0].length];
			
			this._counter++;
			
			if (this._counter == this._interval)
			{
				if (this._recordType == RecordType.CONCENTRATION)
				{
					QuickCSV.write( "solute_" + this._soluteName + "_concentration_order_" + 
							this._order, Array.slice( trimmedGrid, 2, 0 ));
				}
				
				else if (this._recordType == RecordType.DIFFERENCE)
				{
					double[][][] difference = new double[trimmedGrid.length]
							[trimmedGrid[0].length][trimmedGrid[0][0].length];
					for (int i = 0; i < trimmedGrid.length; i++)
					{
						for (int j = 0; j < trimmedGrid[0].length; j++)
						{
							for (int k = 0; k < trimmedGrid[0][0].length; k++)
							{
								if (this._difference.contentEquals("absolute"))
								{
									difference[i][j][k] = 
										Math.abs(trimmedGrid[i][j][k] - this._savedGrid[i][j][k]);
								}
								else
								{
									difference[i][j][k] = 
											trimmedGrid[i][j][k] - this._savedGrid[i][j][k];
								}
							}
						}
					}
					QuickCSV.write( "solute_" + this._soluteName + "_difference_order_" + 
							this._order, Array.slice( difference, 2, 0 ));
				}
				
				else if (this._recordType == RecordType.DIFFERENCENORM)
				{
					double[][][] difference = new double[trimmedGrid.length]
							[trimmedGrid[0].length][trimmedGrid[0][0].length];
					for (int i = 0; i < trimmedGrid.length; i++)
					{
						for (int j = 0; j < trimmedGrid[0].length; j++)
						{
							for (int k = 0; k < trimmedGrid[0][0].length; k++)
							{
								if (this._difference.contentEquals("absolute"))
								{
									difference[i][j][k] = 
										Math.abs(trimmedGrid[i][j][k] - this._savedGrid[i][j][k]);
								}
								else
								{
									difference[i][j][k] = 
											trimmedGrid[i][j][k] - this._savedGrid[i][j][k];
								}
							}
						}
					}
					double norm = MultigridUtils.computeNormUnpaddedMatrix(difference);
					this._series.add(norm);
					this._savedGrid = Vector.copy(trimmedGrid);
				}
				
				else if (this._recordType == RecordType.DIFFERENCEMAX)
				{
					float[][][] difference = new float[trimmedGrid.length]
							[trimmedGrid[0].length][trimmedGrid[0][0].length];
					for (int i = 0; i < trimmedGrid.length; i++)
					{
						for (int j = 0; j < trimmedGrid[0].length; j++)
						{
							for (int k = 0; k < trimmedGrid[0][0].length; k++)
							{
								if (this._difference.contentEquals("absolute"))
								{
									difference[i][j][k] = 
										(float) Math.abs(trimmedGrid[i][j][k] - this._savedGrid[i][j][k]);
								}
								else
								{
									difference[i][j][k] = 
											(float) (trimmedGrid[i][j][k] - this._savedGrid[i][j][k]);
								}
							}
						}
					}
					float max = MultigridUtils.max(difference);
					this._series.add((double) max);
					this._savedGrid = Vector.copy(trimmedGrid);
				}
				
				this._counter = 0;
			}
		}
	}
	
	public void flush()
	{
		if (this._recordType == RecordType.DIFFERENCENORM)
		{
			double[][] series = new double[this._series.size()][1];
			for (int i = 0; i < this._series.size(); i++)
			{
				series[i][0] = this._series.get(i);
			}
			QuickCSV.write( "solute_" + this._soluteName + "_differenceNorm_order_" + 
					this._order, series);
		}
		
		else if (this._recordType == RecordType.DIFFERENCEMAX)
		{
			double[][] series = new double[this._series.size()][1];
			for (int i = 0; i < this._series.size(); i++)
			{
				series[i][0] = this._series.get(i);
			}
			QuickCSV.write( "solute_" + this._soluteName + "_differenceMax_order_" + 
					this._order, series);
		}
	}

	@Override
	public AspectReg reg() 
	{
		return _aspectRegistry;
	}
	
	public String getSoluteName()
	{
		return this._soluteName;
	}
	
	public Integer getOrder()
	{
		return this._order;
	}

	public String defaultXmlTag() 
	{
		return XmlRef.record;
	}
	
}
