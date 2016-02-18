package processManager;

import java.util.HashMap;
import java.util.List;

import agent.Agent;
import dataIO.SvgExport;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import linearAlgebra.Vector;
import utility.Helper;

public class WriteAgentsSvg extends ProcessManager
{
	
	//FIXME: very quick and dirty, this ProcessManager really needs more
	// information than it can get from the environment and agents...
	protected SvgExport svg = new SvgExport();
	
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
		
		/*  */
		SpatialGrid solute = environment.getSoluteGrid("solute2");
		int[] coord = solute.resetIterator();
		double[] origin;
		double[] dimension = new double[3];
		while ( solute.isIteratorValid() )
		{
			origin = solute.getVoxelOrigin(coord);
			solute.getVoxelSideLengthsTo(dimension, coord);
			double conc = solute.getValueAtCurrent(ArrayType.CONCN) * 255.0/2.0;
			conc = Math.min(conc, 255.0);
			conc = Math.max(conc, 0.0);
			int c = 255 - Math.round((float) conc);
			svg.rectangle(Vector.subset(origin,agents.getNumDims()), 
					Vector.subset(dimension,agents.getNumDims()), "rgb(" + c 
					+ "," + c + "," + c + ")");
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