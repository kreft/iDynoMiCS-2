package processManager.library;

import agent.Agent;
import agent.Body;
import dataIO.Log;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import surface.Surface;
import utility.ExtraMath;
import utility.Helper;

import java.util.List;

public class PlasmidDynamics2 extends ProcessManager {

    @Override
    protected void internalStep() {
        agentLoop();
    }

    protected void agentLoop() {


        for (Agent donor: Helper.shuffledCopy(this._agents.getAllAgents()))
        {
            /* check vectors, set a readyToDonate time if none is set, remove vectors that are not ready from list */
            List<Agent> vectors = Helper.shuffledCopy( donor.getVectors() );
            for (Agent vector : vectors ) {
                if ( !vector.isAspect(AspectRef.readyToDonate ))
                    vector.set(AspectRef.readyToDonate, this.getTimeForNextStep()-this.getTimeStepSize());
                if (!vector.isAspect(AspectRef.transferCooldown ))
                {
                    Log.out(Log.Tier.CRITICAL, this.getClass().getSimpleName() + " missing " +
                            AspectRef.transferCooldown + "! Setting placeholder 30 minutes.");
                    vector.set(AspectRef.transferCooldown, 30.0);
                }
                if (!vector.isAspect(AspectRef.scanSpeed ))
                {
                    Log.out(Log.Tier.CRITICAL, this.getClass().getSimpleName() + " missing " +
                            AspectRef.scanSpeed + "! Setting placeholder 1 minute.");
                    vector.set(AspectRef.scanSpeed, 1.0);
                }
                if (!vector.isAspect(AspectRef.transferProbability ))
                {
                    if(Log.shouldWrite(Log.Tier.EXPRESSIVE)) { // this is not critical, probably ok to assume to be 1 if unset.
                        Log.out(Log.Tier.EXPRESSIVE, this.getClass().getSimpleName() + " missing " +
                                AspectRef.transferProbability + ", Setting placeholder 1.");
                    }
                    vector.set(AspectRef.transferProbability, 1.0);
                }
            }
            vectors.removeIf(vector -> (this.getTimeForNextStep() < vector.getDouble(AspectRef.readyToDonate)) );
            // hence we assume there is a separate cooldown for each plasmid

            if ( !vectors.isEmpty() ) {
                for (Agent vector : vectors) {
                    List<Agent> neighbors =
                            this._agents.treeSearch(donor, vector.getDouble(AspectRef.pilusLength));
                    neighbors.removeIf(agent ->
                            !this.agentCompatibility(agent,vector.getString(AspectRef.incGroup)));
                    // note we can replace this with only checking for potentialHost to have failed attempts too
                    // did it like this for now as it simpler and faster for initial implementation.

                    for( Surface s : ((Body) donor.getValue(AspectRef.agentBody)).getSurfaces()) {
                        this._agents.filterAgentCollision(s,neighbors,vector.getDouble(AspectRef.pilusLength)); // FIXME this will need updating if we get multi surface agents
                    }

                    if( neighbors.isEmpty() )
                        break;

                    double testTally = vector.getDouble(AspectRef.scanSpeed) * this.getTimeStepSize();
                    double totalTally = testTally;
                    /* we loop instead of calculating complement rule probability
                    such that we can calculate cooldown time more accurately */
                    while ( testTally > 0.0) {
                        if( testTally < 1.0 && testTally < ExtraMath.getUniRandDbl() ) // decide if we try one more time on partial testTally
                            break;
                        if( ExtraMath.getUniRandDbl() < vector.getDouble(AspectRef.transferProbability))
                            // we could additionally scale the prob to account for density of receptible agents around.
                        {
                            /* method that scales contact prob equally, can provide custom weights for future implementations */
                            Agent selected = Helper.selectByWeightedProbability(neighbors,
                                    Helper.uniformWeights( neighbors.size() ), ExtraMath.getUniRandDbl());

                            double now = this.getTimeForNextStep() - (testTally/totalTally) * this.getTimeStepSize();
                            Agent receiverVector = new Agent(vector);
                            // cooldown for transconjugant, FIXME we'll want to make this a separate receiver cooldown probably
                            receiverVector.set(AspectRef.readyToDonate, now + vector.getDouble(AspectRef.transferCooldown));
                            selected.addVector(receiverVector);
                            // cooldown for donor
                            vector.set(AspectRef.readyToDonate, now + vector.getDouble(AspectRef.transferCooldown));
                        }
                        testTally -= 1.0;
                    }

                }
            }

        }
    }


    /**
     * returns false if the incGroup is found
     *
     * @param a
     * @param incompatibilityGroup
     * @return
     */
    protected boolean agentCompatibility(Agent a, String incompatibilityGroup) {
        if (a.isAspect(AspectRef.potentialHost ))
            if( a.getBoolean(AspectRef.potentialHost)) {
                if (a.isAspect(AspectRef.incompatibilityGroups)) {
                    for (String incGroup : a.getStringA(AspectRef.incompatibilityGroups))
                        if (incGroup.equals(incompatibilityGroup))
                            return false;
                }
                for (Agent vector : a.getVectors()) {
                    if (vector.isAspect(AspectRef.incGroup))
                        if (vector.getString(AspectRef.incGroup).equals(incompatibilityGroup))
                            return false;
                }
                return true; //return true of inc group is not encountered.
            }
        return false; //return false if not a potential host.
    }

}
