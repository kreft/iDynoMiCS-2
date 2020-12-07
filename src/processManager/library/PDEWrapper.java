package processManager.library;

import static grid.ArrayType.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agent.Agent;
import org.w3c.dom.Element;

import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import grid.SpatialGrid;
import processManager.ProcessDiffusion;
import processManager.ProcessMethods;
import reaction.Reaction;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.Shape;
import shape.subvoxel.IntegerArray;
import solver.PDEexplicit;
import solver.PDEmultigrid;
import solver.mgFas.Domain;
import solver.mgFas.Multigrid;
import utility.Helper;

/**
 * \brief wraps and runs PDE solver
 *
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class PDEWrapper extends ProcessDiffusion
{
    public static String ABS_TOLERANCE = AspectRef.solverAbsTolerance;

    public static String REL_TOLERANCE = AspectRef.solverRelTolerance;

//    private AgentContainer _agents;
    /**
     *
     * Initiation from protocol file:
     *
     * TODO verify and finalise
     */
    public void init(Element xmlElem, EnvironmentContainer environment,
                     AgentContainer agents, String compartmentName)
    {
        super.init(xmlElem, environment, agents, compartmentName);

        double absTol = (double) this.getOr(ABS_TOLERANCE, 1.0e-9);
        double relTol = (double) this.getOr(REL_TOLERANCE, 1.0e-3);

        int vCycles = (int) this.getOr(AspectRef.vCycles, 5);
        int preSteps = (int) this.getOr(AspectRef.preSteps, 100);
        int coarseSteps = (int) this.getOr(AspectRef.coarseSteps, 100);
        int postSteps = (int) this.getOr(AspectRef.postSteps, 100);

        /* TODO initial diffusivity */

        /* gets specific solutes from process manager aspect registry if they
         * are defined, if not, solve for all solutes.
         */
        this._soluteNames = (String[]) this.getOr(SOLUTES,
                Helper.collectionToArray(this._environment.getSoluteNames()));
        /*
         * Set up the relevant arrays in each of our solute grids: diffusivity
         * & well-mixed need only be done once each process manager time step,
         * but production rate must be reset every time the PDE updater method
         * is called.
         */
        for ( String soluteName : this._soluteNames )
        {
            SpatialGrid solute = this._environment.getSoluteGrid(soluteName);
            solute.updateDiffusivity(this._environment, this._agents);
        }

        Domain domain = new Domain(environment.getShape());
        Multigrid multigrid = new Multigrid();
        multigrid.init(domain, environment, agents, this,
                vCycles, preSteps, coarseSteps, postSteps);

        super.init(xmlElem, environment, agents, compartmentName);

        // TODO Let the user choose which ODEsolver to use.


    }

    /* ***********************************************************************
     * STEPPING
     * **********************************************************************/

    @Override
    protected void internalStep()
    {
        /*
         * Do the generic set up and solving.
         */
//        super.internalStep();

        prestep(this._environment.getSolutes(), 0.0);

        for ( SpatialGrid var : this._environment.getSolutes() )
        {
            var.reset(PRODUCTIONRATE);
        }
        /*
         * Estimate agent growth based on the steady-state solute
         * concentrations.
         */
//        for ( Agent agent : this._agents.getAllLocatedAgents() )
//            this.applyAgentGrowth(agent);

        for ( SpatialGrid var : this._environment.getSolutes() )
        {
            double massMove = var.getTotal(PRODUCTIONRATE);
            var.increaseWellMixedMassFlow(massMove);
        }
        /*
         * Estimate the steady-state mass flows in or out of the well-mixed
         * region, and distribute it among the relevant boundaries.
         */
        this._environment.distributeWellMixedFlows(this._timeStepSize);


        /* perform final clean-up and update agents to represent updated
         * situation. */
        this.postStep();
    }

    /**
     * \brief The standard PDE updater method resets the solute
     *
     * TODO this method would benefit from renaming
     *
     * {@code PRODUCTIONRATE} arrays, applies the reactions, and then tells
     * {@code Agent}s to grow.
     *
     * @return PDE updater method.
     */
    public void prestep(Collection<SpatialGrid> variables, double dt)
    {
        for ( SpatialGrid var : variables )
            var.newArray(PRODUCTIONRATE);
        applyEnvReactions(variables);

        setupAgentDistributionMaps(this._agents.getShape());

        for ( Agent agent : _agents.getAllLocatedAgents() )
            applyAgentReactions(agent, variables);
    }

    /**
     * \brief Apply the reactions for a single agent.
     *
     * <p><b>Note</b>: this method assumes that the volume distribution map
     * of this agent has already been calculated. This is typically done just
     * once per process manager step, rather than at every PDE solver
     * relaxation.</p>
     *
     * @param agent Agent assumed to have reactions (biomass will not be
     * altered by this method).
     * @param variables Collection of spatial grids assumed to be the solutes.
     */
    private void applyAgentReactions(
            Agent agent, Collection<SpatialGrid> variables)
    {
        /*
         * Get the agent's reactions: if it has none, then there is nothing
         * more to do.
         */
        @SuppressWarnings("unchecked")
        List<Reaction> reactions =
                (List<Reaction>) agent.getValue(XmlRef.reactions);
        if ( reactions == null )
            return;
        /*
         * Get the distribution map and scale it so that its contents sum up to
         * one.
         */
        Shape shape = variables.iterator().next().getShape();
        @SuppressWarnings("unchecked")
        Map<Shape, HashMap<IntegerArray,Double>> mapOfMaps =
                (Map<Shape, HashMap<IntegerArray,Double>>)
                        agent.getValue(VOLUME_DISTRIBUTION_MAP);

        HashMap<IntegerArray,Double> distributionMap = mapOfMaps.get(shape);
        /*
         * Get the agent biomass kinds as a map. Copy it now so that we can
         * use this copy to store the changes.
         */
        Map<String,Double> biomass = ProcessMethods.getAgentMassMap(agent);
        /*
         * Now look at all the voxels this agent covers.
         */
        Map<String,Double> concns = new HashMap<String,Double>();
        SpatialGrid solute;
        double concn, productRate, volume, perVolume;
        for ( IntegerArray coord : distributionMap.keySet() )
        {
            volume = shape.getVoxelVolume(coord.get());
            perVolume = 1.0/volume;
            for ( Reaction r : reactions )
            {
                /*
                 * Build the dictionary of variable values. Note that these
                 * will likely overlap with the names in the reaction
                 * stoichiometry (handled after the reaction rate), but will
                 * not always be the same. Here we are interested in those that
                 * affect the reaction, and not those that are affected by it.
                 */
                concns.clear();
                for ( String varName : r.getConstituentNames() )
                {
                    solute = FindGrid(variables, varName);
                    if ( solute != null )
                        concn = solute.getValueAt(CONCN, coord.get());
                    else if ( biomass.containsKey(varName) )
                    {
                        concn = biomass.get(varName) *
                                distributionMap.get(coord) * perVolume;
                    }
                    else if ( agent.isAspect(varName) )
                    {
                        /*
                         * Check if the agent has other mass-like aspects
                         * (e.g. EPS).
                         */
                        concn = agent.getDouble(varName) *
                                distributionMap.get(coord) * perVolume;
                    }
                    else
                    {
                        // TODO safety?
                        concn = 0.0;
                    }
                    concns.put(varName, concn);
                }
                /*
                 * Now that we have the reaction rate, we can distribute the
                 * effects of the reaction. Note again that the names in the
                 * stoichiometry may not be the same as those in the reaction
                 * variables (although there is likely to be a large overlap).
                 */

                for ( String productName : r.getReactantNames() )
                {
                    solute = FindGrid(variables, productName);
                    if ( solute != null )
                    {
                        productRate = r.getProductionRate(concns, productName);
                        solute.addValueAt( PRODUCTIONRATE, coord.get(), volume *
                                productRate );
                    }
                    /*
                     * Unlike in a transient solver, we do not update the agent
                     * mass here.
                     */
                }
            }
        }
        /* debugging */
//		Log.out(Tier.NORMAL , " -- " +
//		this._environment.getSoluteGrid("glucose").getAverage(PRODUCTIONRATE));
    }

    private SpatialGrid FindGrid(Collection<SpatialGrid> grids, String name)
    {
        for ( SpatialGrid grid : grids )
            if ( grid.getName().equals(name) )
                return grid;
        return null;
    }
}
