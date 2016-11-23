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
	public int maxIter = 100;
	
	public double residualTolerance = 0.01;
	
	public PDEgaussseidel()
	{
		
	}

	@Override
	public void solve(Collection<SpatialGrid> variables,
			SpatialGrid commonGrid, double tFinal)
	{
		double residual, maxResidual = 0.0;
		for ( int i = 0; i < this.maxIter; i++ )
		{
			this._updater.prestep(variables, tFinal);
			for ( SpatialGrid variable : variables )
			{
				residual = this.relax(variable, commonGrid, tFinal);
				maxResidual = Math.max(residual, maxResidual);
			}
			if ( maxResidual < this.residualTolerance )
				break;
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
	
	private double relax(SpatialGrid variable,
			SpatialGrid commonGrid, double tFinal)
	{
		/* Logging verbosity. */
		Tier level = DEBUG;
		Shape shape = variable.getShape();
		/* Coordinates of the current position. */
		int[] current, nhb;
		/* Temporary storage. */
		double currConcn, currDiffusivity, nhbMass, tally, norm, weight;
		/* 
		 * The residual gives an estimation of how close to stead-state we are.
		 */
		double residual, totalResidual = 0.0, numVoxels = 0.0;
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
			currConcn = variable.getValueAtCurrent(CONCN);
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
			if ( ! this._allowNegatives )
			{
				newConcn = 0.0;
				if ( Log.shouldWrite(level) )
					Log.out(level, "\t\t\tTruncating to zero.");
			}
			variable.setValueAt(CONCN, current, newConcn);
			/* Calculate the residual. */
			currConcn = Math.abs(currConcn);
			newConcn = Math.abs(newConcn);
			if ( Math.max(currConcn, newConcn) > 0.0)
			{
				residual = Math.abs((currConcn - newConcn) /
									Math.max(currConcn, newConcn));
			}
			else
				residual = 0.0;
			totalResidual += residual;
			numVoxels++;
		}
		return (numVoxels > 0.0) ? (totalResidual/numVoxels) : 0.0;
	}
}
