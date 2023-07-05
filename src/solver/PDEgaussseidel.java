package solver;

import static dataIO.Log.Tier.DEBUG;
import static grid.ArrayType.CONCN;
import static grid.ArrayType.PRODUCTIONRATE;

import java.util.Collection;

import dataIO.Log;
import dataIO.Log.Tier;
import grid.SpatialGrid;
import grid.WellMixedConstants;
import linearAlgebra.Vector;
import shape.Shape;

/**
 * \brief Partial Differential Equation (PDE) solver that uses the Gauss-Seidel
 * iteration approach. This PDE solver can only solve to steady-state, and 
 * should not be used where a time-dependent solution is appropriate.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class PDEgaussseidel extends PDEsolver
{
	public int maxIter = 10001;
	
	public double residualTolerance = 0.01;
	
	public PDEgaussseidel()
	{
		
	}

	@Override
	public void solve(Collection<SpatialGrid> variables,
			SpatialGrid commonGrid, double tFinal)
	{
		Shape shape = commonGrid.getShape();
		
		/*
		 * TODO
		 * The choice of strideLength should come from various dimensions of
		 * the shape. Until then, 2 (red-black) should work in most cases.
		 */
		shape.setNewIterator(2);
		
		double residual, maxResidual = 0.0;
		for ( int i = 0; i < this.maxIter; i++ )
		{
			this._updater.prestep(variables, tFinal);
			for ( SpatialGrid variable : variables )
			{
				residual = this.relax(variable, commonGrid);
				maxResidual = Math.max(residual, maxResidual);
			}
			//if ( maxResidual < this.residualTolerance )
			//	break;
			// FIXME This if clause is for debugging only, remove after
			if (i%1000==0)
			{
				Log.out(Tier.CRITICAL, "Iteration: "+i+" Max residual: "+maxResidual);
				for (SpatialGrid var : variables)
					Log.out(Tier.CRITICAL, "\t"+var.getName()+" ("+var.getMin(CONCN)+", "+var.getMax(CONCN)+")");
			}
		}
		
		// TODO relax one more time, and use only this relaxation to update the
		// flow in/out of the well-mixed region
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
	
	public double relax(SpatialGrid variable, SpatialGrid commonGrid)
	{
		/* Logging verbosity. */
		Tier level = DEBUG;
		Shape shape = variable.getShape();
		/* Coordinates of the current position. */
		int[] current;
		/* Temporary storage. */
		double currConcn, currVolume;
		double timeScale, diffusiveFlow, rateFromReactions, newConcn;
		/* 
		 * The residual gives an estimation of how close to stead-state we are.
		 */
		double residual, totalResidual = 0.0, numVoxels = 0.0;
		/*
		 * Each voxel's concentration is replaced with a weighted average of 
		 * its neighbours' concentrations and the local reaction rate. 
		 * The weight of each neighbour is proportional to the surface area 
		 * shared (more area => more weight) and to the mean diffusivity (more 
		 * diffusivity => more weight), and is inversely proportional to the
		 * distance between the two voxels (more distance => less weight).
		 * 
		 * The weights must be
		 * normalised before the concentration is replaced!
		 */
		for ( current = shape.resetIterator(); shape.isIteratorValid();
				current = shape.iteratorNext() )
		{
			if ( WellMixedConstants.isWellMixed(commonGrid, current) )
				continue;
			currConcn = variable.getValueAtCurrent(CONCN);
			currVolume = shape.getCurrVoxelVolume();
			diffusiveFlow = 0.0;
			timeScale = 0.0;
			for ( shape.resetNbhIterator(); shape.isNbhIteratorValid();
					shape.nbhIteratorNext() )
			{
				timeScale += variable.getDiffusiveTimeScaleWithNeighbor();
				diffusiveFlow += variable.getDiffusionFromNeighbor();
			}
			rateFromReactions = variable.getValueAt(PRODUCTIONRATE, current);
			// TODO norm += 1.0 / variable.getValueAt(DIFFPRODUCTIONRATE, current);
			residual = (diffusiveFlow + rateFromReactions) * 
					timeScale / currVolume;
			newConcn = currConcn + residual;
			if ( Log.shouldWrite(level) )
			{
				Log.out(level, "Coord "+Vector.toString(current)+
						": curent value "+currConcn+", new value "+newConcn);
			}
			
			// FIXME This if clause is for debugging only, remove after
			if ( rateFromReactions != 0.0  && Log.shouldWrite(Tier.DEBUG) )
			{
				Log.out(Tier.DEBUG, "Coord "+Vector.toString(current)+
						" variable "+variable.getName()+
						": curent value "+currConcn+", new value "+newConcn+"\n"
						+"\t Diffuse "+diffusiveFlow+" -> "+(diffusiveFlow*timeScale/currVolume)+"\n"
						+"\t React "+variable.getValueAt(PRODUCTIONRATE, current)+" -> "+(rateFromReactions*timeScale/currVolume));
			}
			
			if ( (! this._allowNegatives) && newConcn < 0.0 )
			{
				if ( Log.shouldWrite(Tier.EXPRESSIVE) )
				{
					Log.out(Tier.EXPRESSIVE, "Truncating concentration of "+
							variable.getName()+" to zero\n"+
							"\tVoxel at "+Vector.toString(current)+"\n"+
							"\tPrevious concn "+currConcn+
							"\tPrevented concn "+newConcn);
				}
				newConcn = 0.0;
			}
			variable.setValueAt(CONCN, current, newConcn);
			/* Calculate the residual. */
			currConcn = Math.abs(currConcn);
			newConcn = Math.abs(newConcn);
			if ( Math.min(currConcn, newConcn) > 0.0)
			{
				totalResidual = Math.abs(
						residual / Math.min(currConcn, newConcn));
			}
			numVoxels++;
		}
		return (numVoxels > 0.0) ? (totalResidual/numVoxels) : 0.0;
	}

	@Override
	public void setAbsoluteTolerance(double tol) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRelativeTolerance(double tol) {
		// TODO Auto-generated method stub
		
	}
}
