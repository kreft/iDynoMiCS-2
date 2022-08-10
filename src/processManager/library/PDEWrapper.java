package processManager.library;

import static grid.ArrayType.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agent.Agent;
import agent.Body;
import bookkeeper.KeeperEntry;
import boundary.Boundary;
import boundary.WellMixedBoundary;
import dataIO.ObjectFactory;
import idynomics.Global;
import org.w3c.dom.Element;

import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import grid.SpatialGrid;
import processManager.ProcessDiffusion;
import processManager.ProcessMethods;
import reaction.Reaction;
import reaction.RegularReaction;
import referenceLibrary.AspectRef;
import referenceLibrary.XmlRef;
import shape.Shape;
import shape.subvoxel.IntegerArray;
import solver.mgFas.Domain;
import solver.mgFas.Multigrid;
import solver.mgFas.SolverGrid;
import solver.mgFas.*;
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
    
    private Multigrid multigrid;

    public double absTol;
    
    public double relTol;

    public double solverResidualRatioThreshold;

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

        /* TODO move values to global defaults */
        this.absTol = (double) this.getOr(ABS_TOLERANCE, 1.0e-12);
        this.relTol = (double) this.getOr(REL_TOLERANCE, 1.0e-6);

        this.solverResidualRatioThreshold = (double) this.getOr(
                AspectRef.solverResidualRatioThreshold, 1.0e-4);

        int vCycles = (int) this.getOr(AspectRef.vCycles, 15);
        int preSteps = (int) this.getOr(AspectRef.preSteps, 5);
        int coarseSteps = (int) this.getOr(AspectRef.coarseSteps, 5);
        int postSteps = (int) this.getOr(AspectRef.postSteps, 5);

        boolean autoVcycleAdjust = (boolean) this.getOr(AspectRef.autoVcycleAdjust, false);

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

        Domain domain = new Domain(environment.getShape(), this._environment);
        this.multigrid = new Multigrid();
        multigrid.init(domain, environment, agents, this,
                vCycles, preSteps, coarseSteps, postSteps, autoVcycleAdjust);

        // TODO Let the user choose which ODEsolver to use.


    }

    public double fetchBulk(String solute)
    {
        for( Boundary b : this._environment.getShape().getAllBoundaries() )
        {
            if (b instanceof WellMixedBoundary )
                return ((WellMixedBoundary) b).getConcentration(solute);
        }
        return 0.0;
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
        for ( Boundary b : this._environment.getShape().getAllBoundaries() )
        {
            b.resetMassFlowRates();
        }
        /* gets specific solutes from process manager aspect registry if they
         * are defined, if not, solve for all solutes.
         */
        this._soluteNames = (String[]) this.getOr(SOLUTES,
                Helper.collectionToArray(this._environment.getSoluteNames()));

        prestep(this._environment.getSolutes(), 0.0);

        for ( SpatialGrid var : this._environment.getSolutes() )
        {
            var.reset(PRODUCTIONRATE);
        }
        multigrid.initAndSolve();
        /*
         * Estimate agent growth based on the steady-state solute
         * concentrations.
         */
        for ( Agent agent : this._agents.getAllLocatedAgents() )
            this.applyAgentGrowth(agent);

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
    /* TODO should env reactions be aplied here? */
        for ( SpatialGrid var : variables )
            var.newArray(PRODUCTIONRATE);
        applyEnvReactions(variables);

        setupAgentDistributionMaps(this._agents.getShape());
    }

    public void applyReactions(MultigridSolute[] sols, int resorder, SolverGrid[] reacGrid, double[] resolution,
                               double voxelVolume)
    {
        for( Agent agent : this._agents.getAllAgents() )
            applyAgentReactions(agent, sols, resorder, reacGrid, resolution, voxelVolume);
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
     */
    private void applyAgentReactions(
            Agent agent, MultigridSolute[] concGrid, int resorder, SolverGrid[] reacGrid, double[] resolution,
            double voxelVolume)
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
        Shape shape = this._agents.getShape();

        /*
         * Get the agent biomass kinds as a map. Copy it now so that we can
         * use this copy to store the changes.
         */
        Map<String,Double> biomass = ProcessMethods.getAgentMassMap(agent);
        /*
         * Now look at all the voxels this agent covers.
         */
        Map<String,Double> concns = new HashMap<String,Double>();
        SolverGrid solute;
        MultigridSolute mGrid;
        double concn, productRate, volume, perVolume;

        double[] center = ((Body) agent.get(AspectRef.agentBody)).getCenter(shape);

        IntegerArray coord = new IntegerArray(
                shape.getCoords( center, null, resolution ));



        volume = voxelVolume;
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
                mGrid = FindGrid(concGrid, varName);
                if ( mGrid != null ) {
                    solute = mGrid._conc[resorder];
                    concn = solute.getValueAt(coord.get(), true);
                }
                else if ( biomass.containsKey(varName) )
                {
                    concn = biomass.get(varName) * perVolume;
                }
                else if ( agent.isAspect(varName) )
                {
                    /*
                     * Check if the agent has other mass-like aspects
                     * (e.g. EPS).
                     */
                    concn = agent.getDouble(varName) * perVolume;
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
                mGrid = FindGrid(concGrid, productName);
                if ( mGrid != null )
                {
                    solute = mGrid._reac[resorder];
                    productRate = r.getProductionRate(concns, productName);
                    solute.addValueAt( productRate, coord.get() , true );
                }
            }
        }
    }

    private void applyAgentGrowth(Agent agent)
    {
        /*
         * Get the agent's reactions: if it has none, then there is nothing
         * more to do.
         */
        @SuppressWarnings("unchecked")
        List<RegularReaction> reactions =
                (List<RegularReaction>) agent.getValue(XmlRef.reactions);
        if ( reactions == null )
            return;

        Shape shape = this._agents.getShape();
        double[] center = ((Body) agent.get(AspectRef.agentBody)).getCenter(shape);

        IntegerArray coord = new IntegerArray(
                shape.getCoords( center ));
        /*
         * Get the agent biomass kinds as a map. Copy it now so that we can
         * use this copy to store the changes.
         */
        Map<String,Double> biomass = ProcessMethods.getAgentMassMap(agent);
        @SuppressWarnings("unchecked")
        Map<String,Double> newBiomass = (HashMap<String,Double>)
                ObjectFactory.copy(biomass);
        /*
         * Now look at all the voxels this agent covers.
         */
        Map<String,Double> concns = new HashMap<String,Double>();
        SpatialGrid solute;
        double concn, productRate, volume, perVolume;

            volume = this._agents.getShape().getVoxelVolume( coord.get() );
            perVolume = 1.0 / volume;
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
                    if ( this._environment.isSoluteName( varName ) )
                    {
                        solute = this._environment.getSoluteGrid( varName );
                        concn = solute.getValueAt( CONCN, coord.get() );
                    }
                    else if ( biomass.containsKey( varName ) )
                    {
                        concn = biomass.get( varName ) * perVolume;

                    }
                    else if ( agent.isAspect( varName ) )
                    {
                        /*
                         * Check if the agent has other mass-like aspects
                         * (e.g. EPS).
                         */
                        concn = agent.getDouble( varName ) * perVolume;
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
                    /* FIXME: it is probably faster if we get the reaction rate
                     * once and then calculate the rate per product from that
                     * for each individual product
                     */
                    productRate = r.getProductionRate(concns,productName);
                    double quantity;

                    if ( this._environment.isSoluteName(productName) )
                    {
                        solute = this._environment.getSoluteGrid(productName);
                        quantity =
                                productRate * volume * this.getTimeStepSize();
                        solute.addValueAt(PRODUCTIONRATE, coord.get(), quantity
                        );
                    }
                    else if ( newBiomass.containsKey(productName) )
                    {
                        quantity =
                                productRate * this.getTimeStepSize() * volume;
                        newBiomass.put(productName, newBiomass.get(productName)
                                + quantity );
                    }
                    /* FIXME this can create conflicts if users try to mix mass-
                     * maps and simple mass aspects	 */
                    else if ( agent.isAspect(productName) )
                    {
                        /*
                         * Check if the agent has other mass-like aspects
                         * (e.g. EPS).
                         */
                        quantity =
                                productRate * this.getTimeStepSize() * volume;
                        newBiomass.put(productName, agent.getDouble(productName)
                                + quantity);
                    }
                    else
                    {
                        quantity =
                                productRate * this.getTimeStepSize() * volume;
                        //TODO quick fix If not defined elsewhere add it to the map
                        newBiomass.put(productName, quantity);
                        System.out.println("agent reaction catched " +
                                productName);
                        // TODO safety?

                    }
                    if( Global.bookkeeping )
                        agent.getCompartment().registerBook(
                                KeeperEntry.EventType.REACTION,
                                productName,
                                String.valueOf( agent.identity() ),
                                String.valueOf( quantity ), null );
                }
            }
        ProcessMethods.updateAgentMass(agent, newBiomass);
    }

    private MultigridSolute FindGrid(MultigridSolute[] grids, String name)
    {
        for ( MultigridSolute grid : grids )
            if ( grid.soluteName.equals(name) ) {
//                Quick debug check to see which grid (coarse/fine) we are handling
//                System.out.println( grid.getVoxelVolume() );
                return grid;
            }
        return null;
    }
}
