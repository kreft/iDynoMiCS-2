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
import dataIO.Log;
import dataIO.ObjectFactory;
import grid.ArrayType;
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
import solver.PHsolver;
import solver.PKstruct;
import solver.mgFas.Domain;
import solver.mgFas.Multigrid;
import solver.mgFas.SolverGrid;
import solver.mgFas.*;
import utility.Helper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * \brief wraps and runs PDE solver
 *
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class PDEWrapper extends ProcessDiffusion {
    public static String ABS_TOLERANCE = AspectRef.solverAbsTolerance;

    public static String REL_TOLERANCE = AspectRef.solverRelTolerance;

    private Multigrid multigrid;

    public double absTol;

    public double relTol;

    public double solverResidualRatioThreshold;

//    private AgentContainer _agents;

    /**
     * Initiation from protocol file:
     * <p>
     * TODO verify and finalise
     */
    public void init(Element xmlElem, EnvironmentContainer environment,
                     AgentContainer agents, String compartmentName) {
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

        boolean autoVcycleAdjust = (boolean) this.getOr(AspectRef.autoVcycleAdjust, true);

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
        for (String soluteName : this._soluteNames) {
            SpatialGrid solute = this._environment.getSoluteGrid(soluteName);
            solute.updateDiffusivity(this._environment, this._agents);
        }

        Domain domain = new Domain(environment.getShape(), this._environment);
        this.multigrid = new Multigrid();
        multigrid.init(domain, environment, agents, this,
                vCycles, preSteps, coarseSteps, postSteps, autoVcycleAdjust);

        // TODO Let the user choose which ODEsolver to use.


    }

    public double fetchBulk(String solute) {
        for (Boundary b : this._environment.getShape().getAllBoundaries()) {
            if (b instanceof WellMixedBoundary)
                return ((WellMixedBoundary) b).getConcentration(solute);
        }
        return 0.0;
    }

    /* ***********************************************************************
     * STEPPING
     * **********************************************************************/

    @Override
    protected void internalStep() {
        /*
         * Do the generic set up and solving.
         */
//        super.internalStep();
        for (Boundary b : this._environment.getShape().getAllBoundaries()) {
            b.resetMassFlowRates();
        }
        /* gets specific solutes from process manager aspect registry if they
         * are defined, if not, solve for all solutes.
         */
        this._soluteNames = (String[]) this.getOr(SOLUTES,
                Helper.collectionToArray(this._environment.getSoluteNames()));

        prestep(this._environment.getSolutes(), 0.0);

        for (SpatialGrid var : this._environment.getSolutes()) {
            var.reset(PRODUCTIONRATE);
        }
        multigrid.initAndSolve();
        /*
         * Estimate agent growth based on the steady-state solute
         * concentrations.
         */
        for (Agent agent : this._agents.getAllLocatedAgents())
            this.applyAgentGrowth(agent);

        for (SpatialGrid var : this._environment.getSolutes()) {
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
     * <p>
     * TODO this method would benefit from renaming
     * <p>
     * {@code PRODUCTIONRATE} arrays, applies the reactions, and then tells
     * {@code Agent}s to grow.
     *
     * @return PDE updater method.
     */
    public void prestep(Collection<SpatialGrid> variables, double dt) {
        for (SpatialGrid var : variables)
            var.newArray(PRODUCTIONRATE);
        /* Note environment should be calculated simultaneously with agent reactions! */
        setupAgentDistributionMaps(this._agents.getShape());
    }

    public void applyReactions(MultigridSolute[] sols, MultigridSolute[] specials, int resorder, SolverGrid[] reacGrid, double[] resolution,
                               double voxelVolume) {
        /* pH calculation */
        if (Global.concurrent_calculation )
            applySpecialReactionsConcurrent(sols, specials, resorder);
        else
            applySpecialReactions(sols, specials, resorder);
        applyEnvReactions(sols, resorder);
        for (Agent agent : this._agents.getAllAgents())
            applyAgentReactions(agent, sols, specials, resorder, reacGrid, resolution, voxelVolume);
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
     *              altered by this method).
     */
    private void applyAgentReactions(
            Agent agent, MultigridSolute[] concGrid, MultigridSolute[] specials, int resorder, SolverGrid[] reacGrid, double[] resolution,
            double voxelVolume) {
        /*
         * Get the agent's reactions: if it has none, then there is nothing
         * more to do.
         */
        @SuppressWarnings("unchecked")
        List<Reaction> reactions = (List<Reaction>)  agent.reg().getValue(agent, XmlRef.reactions, false);
        if (reactions == null)
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
        Map<String, Double> biomass = ProcessMethods.getAgentMassMap(agent);
        /*
         * Now look at all the voxels this agent covers.
         */
        Map<String, Double> concns = new HashMap<String, Double>();
        SolverGrid solute, special;
        MultigridSolute mGrid, sGrid;
        double concn, productRate, volume, perVolume;

        double[] center = ((Body) agent.get(AspectRef.agentBody)).getCenter(shape);

        IntegerArray coord = new IntegerArray(
                shape.getCoords(center, null, resolution));


        volume = voxelVolume;
        perVolume = 1.0 / volume;
        for (Reaction r : reactions) {
            /*
             * Build the dictionary of variable values. Note that these
             * will likely overlap with the names in the reaction
             * stoichiometry (handled after the reaction rate), but will
             * not always be the same. Here we are interested in those that
             * affect the reaction, and not those that are affected by it.
             */
            concns.clear();
            for (String varName : r.getConstituentNames()) {
                mGrid = FindGrid(concGrid, varName);
                sGrid = FindGrid(specials, varName);
                if (mGrid != null) {
                    solute = mGrid._conc[resorder];
                    concn = solute.getValueAt(coord.get(), true);
                } else if (sGrid != null) {
                    special = sGrid._conc[resorder];
                    concn = special.getValueAt(coord.get(), true);
                } else if (biomass.containsKey(varName)) {
                    concn = biomass.get(varName) * perVolume;
                } else if (agent.isAspect(varName)) {
                    /*
                     * Check if the agent has other mass-like aspects
                     * (e.g. EPS).
                     */
                    concn = agent.getDouble(varName) * perVolume;
                } else {
                    // We have this option in order to facilitate components that can play a role but
                    // may not yet exist at the start of a simulation, for example microbial storage
                    // compounds.
                    Log.out(Log.Tier.CRITICAL , "Requested variable \"" + varName +
                            "\" is not (yet) defined, check the spelling, returning 0.0.");
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

            for (String productName : r.getReactantNames()) {
                mGrid = FindGrid(concGrid, productName);
                if (mGrid != null) {
                    solute = mGrid._reac[resorder];
                    productRate = r.getProductionRate(concns, productName, agent);
                    solute.addValueAt(productRate, coord.get(), true);
                }
            }
        }
    }

    /**
     * TODO this method seemed to be missing for PDE wrapper -> test it!
     *
     * @param concGrid
     * @param resorder
     */
    protected void applyEnvReactions(MultigridSolute[] concGrid, int resorder) {
        Shape shape = this._environment.getShape();
        Collection<Reaction> reactions = this._environment.getReactions();
        if (reactions.isEmpty()) {
            return;
        }
        /*
         * Construct the "concns" dictionary once, so that we don't have to
         * re-enter the solute names for every voxel coordinate.
         */
        Collection<String> soluteNames = this._environment.getSoluteNames();
        HashMap<String, Double> concns = new HashMap<String, Double>();
        for (String soluteName : soluteNames)
            concns.put(soluteName, 0.0);
        /*
         * Iterate over the spatial discretization of the environment,
         * applying extracellular reactions as required.
         */
        double productRate;
        SolverGrid solute;
        MultigridSolute mGrid;
        // TODO test
        for (IntegerArray position : concGrid[0].fetchCoords(resorder)) {
            int[] coord = position.get();
            /* Get the solute concentrations in this grid voxel. */
            for (String s : soluteNames) {
                mGrid = FindGrid(concGrid, s);
                if (mGrid != null) {
                    solute = mGrid._conc[resorder];

                    /* FIXME we may have to use unpadded here, test and correct */
                    concns.put(s, solute.getValueAt(coord, true));
                }
            }
            /* Iterate over each compartment reactions. */
            for (Reaction r : reactions) {
                /* Write rate for each product to grid. */
                for (String product : r.getReactantNames())
                    for (String s : soluteNames)
                        if (product.equals(s)) {
                            mGrid = FindGrid(concGrid, s);
                            if (mGrid != null) {
                                solute = mGrid._reac[resorder];
                                productRate = r.getProductionRate(concns, s, null);


                                /* FIXME we may have to use unpadded here, test and correct */
                                solute.addValueAt(productRate, coord, true);
//                                System.out.println(productRate);
                            }
                        }
            }
        }
    }

    protected void applySpecialReactions(MultigridSolute[] concGrid, MultigridSolute[] specialGrid, int resorder) {
        Collection<SpatialGrid> solutes = this._environment.getSolutes();
        PHsolver pHsolver = new PHsolver();

        for (IntegerArray position : concGrid[0].fetchCoords(resorder)) {
            PKstruct[] pkSolutes = createPKSolutes(concGrid, specialGrid, solutes, position, resorder);
            if (pkSolutes != null) {
                pkSolutes = pHsolver.solve(pkSolutes);
                updateSpecialGrid(specialGrid, pkSolutes, position.get(), resorder);
            }
        }
    }

    protected void applySpecialReactionsConcurrent(MultigridSolute[] concGrid, MultigridSolute[] specialGrid, int resorder) {
        Collection<SpatialGrid> solutes = this._environment.getSolutes();
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        Map<IntegerArray, Future<PKstruct[]>> positionFutureMap = new HashMap<>();

        for (IntegerArray position : concGrid[0].fetchCoords(resorder)) {
            PKstruct[] pkSolutes = createPKSolutes(concGrid, specialGrid, solutes, position, resorder);
            if (pkSolutes != null) {
                Future<PKstruct[]> future = executorService.submit(() -> new PHsolver().solve(pkSolutes));
                positionFutureMap.put(position, future);
            }
        }

        for (Map.Entry<IntegerArray, Future<PKstruct[]>> entry : positionFutureMap.entrySet()) {
            try {
                PKstruct[] resultPkStructs = entry.getValue().get();
                updateSpecialGrid(specialGrid, resultPkStructs, entry.getKey().get(), resorder);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace(); // Handle exceptions appropriately
            }
        }
        executorService.shutdown();
    }

    /* TODO reuse structs for efficiency */

    /**
     * Create PKstructs for pH solver skip solutes that are too low.
     *
     * @param concGrid
     * @param specialGrid
     * @param solutes
     * @param position
     * @param resorder
     * @return
     */
    private PKstruct[] createPKSolutes(MultigridSolute[] concGrid, MultigridSolute[] specialGrid, Collection<SpatialGrid> solutes, IntegerArray position, int resorder) {
        int numStructs = 1 + (int) solutes.stream().filter(s -> s.getpKa() != null && getConc(concGrid, s.getName(), position.get(), resorder) > Global.low_concentration_ph_solver ).count();
        if (numStructs <= 1) return null;

        PKstruct[] pkSolutes = new PKstruct[numStructs];
        int pkSol = 1;

        for (SpatialGrid s : solutes) {
            if (s.getpKa() != null && getConc(concGrid, s.getName(), position.get(), resorder) > Global.low_concentration_ph_solver ) {
                pkSolutes[pkSol] = createPKStruct(s, concGrid, specialGrid, position.get(), resorder);
                pkSol++;
            }
        }
        pkSolutes[0] = new PKstruct();
        pkSolutes[0].solute = "pH";
        pkSolutes[0].conc = getConc(specialGrid, pkSolutes[0].solute, position.get(), resorder);
        return pkSolutes;
    }

    private PKstruct createPKStruct(SpatialGrid s, MultigridSolute[] concGrid, MultigridSolute[] specialGrid, int[] coord, int resorder) {
        PKstruct pkStruct = new PKstruct();
        pkStruct.solute = s.getName();
        pkStruct.conc = getConc(concGrid, s.getName(), coord, resorder) / s.getMolarWeight();
        pkStruct.pKa = s.getpKa();
        pkStruct.maxCharge = s.getmaxCharge();
        pkStruct.pStates = new double[pkStruct.pKa.length + 1];

        for (int nPstate = 0; nPstate < pkStruct.pStates.length; nPstate++) {
            SpatialGrid spec = this._environment.getSpecialGrid(pkStruct.solute + "___" + nPstate);
            pkStruct.pStates[nPstate] = getConc(specialGrid, spec.getName(), coord, resorder);
        }
        return pkStruct;
    }

    private void updateSpecialGrid(MultigridSolute[] specialGrid, PKstruct[] pkSolutes, int[] coord, int resorder) {
        for (int i = 1; i < pkSolutes.length; i++) {
            for (int nPstate = 0; nPstate < pkSolutes[i].pStates.length; nPstate++) {
                SpatialGrid spec = this._environment.getSpecialGrid(pkSolutes[i].solute + "___" + nPstate);
                setConc(specialGrid, spec.getName(), coord, resorder, pkSolutes[i].pStates[nPstate]);
            }
        }
        setConc(specialGrid, pkSolutes[0].solute, coord, resorder, pkSolutes[0].conc);
    }


    private Double getConc(MultigridSolute[] concGrid, String s, int[] coord, int resorder) {
        MultigridSolute mGrid = FindGrid(concGrid, s);
        if(mGrid !=null)
        {
            SoluteGrid solute = mGrid._conc[resorder];
            return solute.getValueAt(coord, false);
        }
        Log.out(Log.Tier.CRITICAL, "Failed to fetch multigrid concentration in "+ this.getClass().getSimpleName());
        return 0.0;
    }

    private void setConc(MultigridSolute[] concGrid, String s, int[] coord, int resorder, Double conc) {
        MultigridSolute mGrid = FindGrid(concGrid, s);
        if(mGrid !=null)
        {
            SoluteGrid solute = mGrid._conc[resorder];
            solute.setValueAt(conc,coord, false);
        }
        else
            Log.out(Log.Tier.CRITICAL, "Failed to fetch multigrid concentration in "+ this.getClass().getSimpleName());
    }

    private void applyAgentGrowth(Agent agent)
    {
        /*
         * Get the agent's reactions: if it has none, then there is nothing
         * more to do.
         *
         * Here we are pulling the object imidiatly (even if it is null) and checking for it's
         * existense later to prevent unnescisary overhead for this frequently called segment
         */
        @SuppressWarnings("unchecked")
        List<Reaction> reactions = (List<Reaction>)  agent.reg().getValue(agent, XmlRef.reactions, false);
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
                    else if ( this._environment.isSpecialName( varName ) )
                    {
                        solute = this._environment.getSpecialGrid( varName );
                        concn = solute.getValueAt( CONCN, coord.get() );
                    }
                    else if (biomass.containsKey( varName ) )
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
                        // We have this option in order to facilitate components that can play a role but
                        // may not yet exist at the start of a simulation, for example microbial storage
                        // compounds.
                        Log.out(Log.Tier.CRITICAL , "Requested variable \"" + varName +
                                "\" is not (yet) defined, check the spelling, returning 0.0.");
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
                    productRate = r.getProductionRate(concns,productName, agent);
                    double quantity;

                    if ( this._environment.isSoluteName(productName) )
                    {
                        solute = this._environment.getSoluteGrid(productName);
                        quantity =
                                productRate * volume * this.getTimeStepSize();
                        solute.addValueAt(PRODUCTIONRATE, coord.get(), quantity
                        );
                    }
                    else if (newBiomass.containsKey(productName) )
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
