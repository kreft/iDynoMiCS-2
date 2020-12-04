package processManager.library;

import static grid.ArrayType.*;

import java.util.Collection;

import org.w3c.dom.Element;

import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import grid.SpatialGrid;
import processManager.ProcessDiffusion;
import referenceLibrary.AspectRef;
import solver.PDEmultigrid;
import solver.mgFas.Domain;
import solver.mgFas.Multigrid;

/**
 * \brief wraps and runs PDE solver
 *
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class PDEWrapper extends ProcessDiffusion
{
    public static String ABS_TOLERANCE = AspectRef.solverAbsTolerance;

    public static String REL_TOLERANCE = AspectRef.solverRelTolerance;

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

        Domain domain = new Domain(environment.getShape());
        Multigrid multigrid = new Multigrid();
        multigrid.init(domain, environment, vCycles, preSteps, coarseSteps, postSteps);

        this._solver.setUpdater(this);

        this._solver.setAbsoluteTolerance(absTol);

        this._solver.setRelativeTolerance(relTol);

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
        super.internalStep();

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
     * {@code PRODUCTIONRATE} arrays, applies the reactions, and then tells
     * {@code Agent}s to grow.
     *
     * @return PDE updater method.
     */
    public void prestep(Collection<SpatialGrid> variables, double dt)
    {
//        for ( SpatialGrid var : variables )
//            var.newArray(PRODUCTIONRATE);
//        applyEnvReactions(variables);
//        for ( Agent agent : _agents.getAllLocatedAgents() )
//            applyAgentReactions(agent, variables);
    }
}
