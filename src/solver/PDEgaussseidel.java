package solver;

import static dataIO.Log.Tier.DEBUG;
import static grid.ArrayType.CONCN;
import static grid.ArrayType.PRODUCTIONRATE;
import static grid.ArrayType.WELLMIXED;

import java.util.Collection;

import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import linearAlgebra.Vector;
import shape.Shape;

public class PDEgaussseidel extends PDEsolver
{
	public PDEgaussseidel()
	{
		
	}

	@Override
	public void solve(Collection<SpatialGrid> variables,
			SpatialGrid commonGrid, double tFinal)
	{
		for ( int i = 0; i < 1; i++ )
		{
			this._updater.prestep(variables, tFinal);
			for ( SpatialGrid variable : variables )
			{
				this.relax(variable, commonGrid, tFinal);
			}
		}
		
	}

	@Override
	protected double getWellMixedFlow(String name)
	{
		return 0.0;
	}

	@Override
	protected void increaseWellMixedFlow(String name, double flow)
	{
		
	}
	
	/* ***********************************************************************
	 * PRIVATE METHODS
	 * **********************************************************************/
	
	private void relax(SpatialGrid variable,
			SpatialGrid commonGrid, double tFinal)
	{
		/* Logging verbosity. */
		Tier level = DEBUG;
		Shape shape = variable.getShape();
		/* Coordinates of the current position. */
		int[] current, nhb;
		/* Temporary storage. */
		double tallyTotal, nhbMass, nhbSArea, totalSArea;
		
		for ( current = shape.resetIterator(); shape.isIteratorValid();
				current = shape.iteratorNext() )
		{
			// TODO this should really be > some threshold
			if ( commonGrid.getValueAt(WELLMIXED, current) == 1.0 )
				continue;
			tallyTotal = 0.0;
			totalSArea = 0.0;
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, 
						"Coord "+Vector.toString(shape.iteratorCurrent())+
						" (curent value "+variable.getValueAtCurrent(CONCN)+
						"): calculating flux...");
			}
			for ( nhb = shape.resetNbhIterator(); shape.isNbhIteratorValid();
					nhb = shape.nbhIteratorNext() )
			{
				nhbMass = variable.getValueAtNhb(CONCN) *
						shape.getVoxelVolume(nhb);
				nhbSArea = shape.nhbCurrSharedArea();
				tallyTotal += nhbMass * nhbSArea;
				totalSArea += nhbSArea;
			}
			/*
			 * Flux is in units of mass/mole per unit time. Divide by the voxel
			 * volume to convert this to a rate of change in concentration.
			 */
			double massFromDiffusion = tallyTotal / totalSArea;
			double volume = shape.getCurrVoxelVolume();
			double concnFromDiffusion = massFromDiffusion / volume;
			double concnFromReactions = tFinal *
					variable.getValueAt(PRODUCTIONRATE, current);
			double newConcn = concnFromDiffusion + concnFromReactions;
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, 
						"Coord "+Vector.toString(shape.iteratorCurrent())+
						" (curent value "+variable.getValueAtCurrent(CONCN)+
						"): change from diffusion = "+concnFromDiffusion+
						", change from reactions = "+concnFromReactions+
						": new value "+newConcn);
			}
			variable.setValueAt(CONCN, current, newConcn);
		}
	}
}
