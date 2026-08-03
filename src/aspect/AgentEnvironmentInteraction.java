package aspect;

import agent.Agent;
import agent.Body;
import compartment.Compartment;
import dataIO.Log;
import dataIO.ObjectFactory;
import grid.ArrayType;
import grid.SpatialGrid;
import referenceLibrary.AspectRef;
import shape.Shape;
import shape.subvoxel.CoordinateMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AgentEnvironmentInteraction {


    static String VD_TAG = AspectRef.agentVolumeDistributionMap;

    public static Map<String,Double> getLocalConcentrations(AspectInterface aspectOwner) {
        HashMap<String,Double> out = new HashMap<String,Double>();
        Log.Tier level = Log.Tier.NORMAL;

        Agent anAgent = (Agent) aspectOwner;
        Compartment comp = anAgent.getCompartment();

        /*
         * Dimensionless compartment (chemostat)
         */
        if (comp.isDimensionless())
        {
            @SuppressWarnings("unchecked")
            Map<String,Double> myMap = (Map<String,Double>) ObjectFactory.copy(
                    comp.environment.getAverageConcentrations() );
            return myMap;
        }

        /*
         * Dimensional compartment
         */
        Collection<SpatialGrid> solutes = comp.environment.getSolutes();

        CoordinateMap distribMap;
        if ( ! anAgent.isAspect(VD_TAG) )
        {
            distribMap = new CoordinateMap();
            Shape shape = anAgent.getCompartment().getShape();
            int[] coords = shape.getCoords( ((Body) anAgent.get(AspectRef.agentBody)).getCenter(shape));
            distribMap.put(coords,1.0);
        } else
        {
            @SuppressWarnings("unchecked")
            Map<Shape, CoordinateMap> mapOfMaps = (Map<Shape, CoordinateMap>) anAgent.getValue(VD_TAG);
            distribMap = mapOfMaps.get(comp.getShape());
        }

        /*
         * Loop over the coordinates, storing the solute concentrations.
         */
        for ( SpatialGrid solute: solutes )
        {
            double concn = 0;
            for ( int[] coord : distribMap.keySet() )
            {
                concn += solute.getValueAt(ArrayType.CONCN, coord);
            }
            /* store averaged local concentration, assuming equal distribution
             * for a more correct implementation consider Shape getVoxelVolume
             */
            out.put(solute.getName(), concn / distribMap.keySet().size() );
        }
        return out;
    }
}
