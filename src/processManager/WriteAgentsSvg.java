package processManager;

import java.util.List;

import agent.Agent;
import dataIO.SvgExport;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import utility.Helper;

/**
 * 
 * @author baco
 *
 */
public class WriteAgentsSvg extends ProcessManager
{
	/**
	 * the svg exporter
	 */
	protected SvgExport svg = new SvgExport();
	
	/**
	 * the concentration value, this will be the max value of the color gradient
	 * when indicating concentration
	 */
	protected double _maxConcn = 2.0;
	
	/**
	 * if any, the solute that is used to draw the solute gradient
	 */
	protected String _solute = "solute1";
	
	@Override
	protected void internalStep(EnvironmentContainer environment,
														AgentContainer agents)
	{
		
		/* initiate new file */
		svg.newSvg(Helper.obtainInput((String) reg().getValue(this, 
				"comparmentName"), "svg writer misses compartment name"));
		
		/* draw computational domain rectangle */		
		svg.rectangle(Vector.zerosDbl(agents.getNumDims()),
				agents.getShape().getDimensionLengths(), "GRAY");
		
		/* draw solute grid for specified solute  */
		SpatialGrid solute = environment.getSoluteGrid(_solute);
		int[] coord = solute.resetIterator();
		double[] origin;
		double[] dimension = new double[3];
		while ( solute.isIteratorValid() )
		{
			/* identify exact voxel location and size */
			origin = solute.getVoxelOrigin(coord);
			solute.getVoxelSideLengthsTo(dimension, coord);
			
			/* scale the solute concentration for coloring */
			double conc = solute.getValueAtCurrent(ArrayType.CONCN) * 255.0/_maxConcn;
			conc = Math.min(conc, 255.0);
			conc = Math.max(conc, 0.0);
			int c = 255 - Math.round((float) conc);
			
			/* write the solute square */
			svg.rectangle(Vector.subset(origin,agents.getNumDims()), 
					Vector.subset(dimension,agents.getNumDims()), "rgb(" + c 
					+ "," + c + "," + c + ")");
			
			/* go to next voxel */
			solute.iteratorNext();
		}

		/* draw agents NOTE currently only coccoid */
		for (Agent a: agents.getAllLocatedAgents()) {	
			@SuppressWarnings("unchecked")
			List<double[]> joints = (List<double[]>) a.get("joints");
			for (int i = 0; joints.size() > i; i++)
				svg.circle(joints.get(i), a.getDouble("radius"), a.getString("pigment"));
		}
		
		/* close svg file */
		svg.closeSvg();

	}
}