package solver;

import static dataIO.Log.Tier.DEBUG;
import static grid.ArrayType.CONCN;
import static grid.ArrayType.DIFFUSIVITY;
import static grid.ArrayType.PRODUCTIONRATE;
import static grid.ArrayType.WELLMIXED;

import java.util.Collection;

import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import linearAlgebra.Vector;
import shape.Shape;
import utility.ExtraMath;

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
		double currDiffusivity, nhbMass, tally, norm, weight;
		/*
		 * The mass of each voxel's concentration is replaced with a weighted
		 * average of its neighbours' masses (convert from concn to mass and
		 * then back to concn). The weight of each neighbour is proportional to
		 * the surface area shared (more area => more weight) and to the mean
		 * diffusivity (more diffusivity => more weight). The weights must be
		 * normalised before the concentration is replaced!
		 */
		for ( current = shape.resetIterator(); shape.isIteratorValid();
				current = shape.iteratorNext() )
		{
			// TODO this should really be > some threshold
			if ( commonGrid.getValueAt(WELLMIXED, current) == 1.0 )
				continue;
			tally = 0.0;
			norm = 0.0;
			currDiffusivity = variable.getValueAtCurrent(DIFFUSIVITY);
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
				weight = shape.nhbCurrSharedArea() *
						ExtraMath.harmonicMean(currDiffusivity, 
						variable.getValueAt(DIFFUSIVITY, nhb));
				tally += nhbMass * weight;
				norm += weight;
			}
			/*
			 * Flux is in units of mass/mole per unit time. Divide by the voxel
			 * volume to convert this to a rate of change in concentration.
			 */
			double massFromDiffusion = tally / norm;
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
