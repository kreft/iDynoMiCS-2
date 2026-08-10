package processManager.library;

import agent.Agent;
import agent.Body;
import dataIO.Log;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import surface.Surface;
import utility.ExtraMath;
import utility.Helper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
                /*
                 * Vector params
                 */
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
                if (!vector.isAspect(AspectRef.derepressionTime ))
                {
                    Log.out(Log.Tier.EXPRESSIVE, this.getClass().getSimpleName() + " not derepressed " +
                                AspectRef.derepressionTime + ", Setting to 0.0");
                    vector.set(AspectRef.derepressionTime, 0.0);
                }
                /*
                 * donor params
                 */
                if (!donor.isAspect(AspectRef.growthTone ))
                {
                    if(Log.shouldWrite(Log.Tier.EXPRESSIVE)) { // this is not critical, probably ok to assume to be 1 if unset.
                        Log.out(Log.Tier.EXPRESSIVE, this.getClass().getSimpleName() + " missing " +
                                AspectRef.growthTone + ", Setting placeholder 1.");
                    }
                    donor.set(AspectRef.growthTone, 1.0);
                }

            }
            vectors.removeIf(vector -> (this.getTimeForNextStep() < vector.getDouble(AspectRef.readyToDonate)) ); //JG: so vector is not removed when ready to donate before end of timestep
            // hence we assume there is a separate cooldown for each plasmid

            if ( !vectors.isEmpty() ) {
                for (Agent vector : vectors) {
                    List<Agent> neighbors =
                            this._agents.treeSearch(donor, vector.getDouble(AspectRef.pilusLength));
                    neighbors.removeIf(agent ->
                            !this.agentCompatibility(agent,vector.getString(AspectRef.vectorIncompatibility)));
                    // note we can replace this with only checking for potentialHost to have failed attempts too
                    // did it like this for now as it simpler and faster for initial implementation.

                    for( Surface s : ((Body) donor.getValue(AspectRef.agentBody)).getSurfaces()) {
                        this._agents.filterAgentCollision(s,neighbors,vector.getDouble(AspectRef.pilusLength)); // FIXME this will need updating if we get multi surface agents
                    }

                    if( neighbors.isEmpty() )
                        break;
                    
                    //Check whether plasmid is derepressed and set scan speed accordingly, correct for growth tone
                    double effectiveScanSpeed;
                    if( vector.isAspect(AspectRef.transitoryDerepression) && vector.getDouble(AspectRef.derepressionTime) > this.getTimeForNextStep() - this.getTimeStepSize() ){
                    //Case I: Plasmid is derepressed, but will become repressed before the end of the current timestep
                        if( vector.getDouble(AspectRef.derepressionTime) < this.getTimeForNextStep() ) {
                            double derepressionTimeFactor = ( this.getTimeForNextStep() - vector.getDouble(AspectRef.derepressionTime) ) / this.getTimeStepSize();
                            double partDerepressedScanSpeed = vector.getDouble(AspectRef.derepressedScanSpeed) * derepressionTimeFactor +  vector.getDouble(AspectRef.scanSpeed) * (1 - derepressionTimeFactor);
                            effectiveScanSpeed = partDerepressedScanSpeed * donor.getDouble(AspectRef.growthTone);
                            Log.out(Log.Tier.NORMAL, "Case I: Plasmid (ID:" + vector.identity() + ") is derepressed, but will become repressed before the end of the current timestep");
                            Log.out(Log.Tier.NORMAL, "Plasmid (ID:" + vector.identity() + ") is derepressed untill " + vector.getDouble(AspectRef.derepressionTime));
                        } 
                    // Case II: Plasmid is derepressed for the entire duration of the current timestep
                        else {
                            effectiveScanSpeed = vector.getDouble(AspectRef.derepressedScanSpeed) * donor.getDouble(AspectRef.growthTone);
                            Log.out(Log.Tier.NORMAL, "Case II: Plasmid (ID:" + vector.identity() + ") is derepressed for the entire duration of the current timestep");
                            Log.out(Log.Tier.NORMAL, "Plasmid (ID:" + vector.identity() + ") is derepressed untill " + vector.getDouble(AspectRef.derepressionTime));
                        }
                    } 
                    // Case III: Plasmid is repressed, regular scan speed is applied
                    else {
                        effectiveScanSpeed = vector.getDouble(AspectRef.scanSpeed) * donor.getDouble(AspectRef.growthTone);
                        Log.out(Log.Tier.NORMAL, "Case III: Plasmid (ID:" + vector.identity() + ") is repressed, regular scan speed is applied");
                        Log.out(Log.Tier.NORMAL, "Plasmid (ID:" + vector.identity() + ") was derepressed untill " + vector.getDouble(AspectRef.derepressionTime));
                    }
                    // Set the number of transfer attempts (testTally) by multiplying the effective scan speed (number of tries per unit of time) with the timestep duration (unit of time)
                    double testTally = effectiveScanSpeed * this.getTimeStepSize();
                    double maxTallyPerTimeStep = testTally; // NOTE: max number of tally that would fit in a full timestep! actual tally number can be lower if not ready from the start.
                    
                    /* TestTally is adjusted for the time window in which the vector is ready to donate
                    (if not ready at start of timestep) */
                    if( (this.getTimeForNextStep() - this.getTimeStepSize()) <  vector.getDouble(AspectRef.readyToDonate))
                        testTally = effectiveScanSpeed * (this.getTimeForNextStep() - vector.getDouble(AspectRef.readyToDonate));
                    Log.out(Log.Tier.NORMAL, "Vector (ID:" + vector.identity() + ") has maximum of " + maxTallyPerTimeStep + " attempts to contact neighbour");
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

                            double now = this.getTimeForNextStep() - (testTally/maxTallyPerTimeStep) * this.getTimeStepSize();
                            Agent receiverVector = new Agent(vector);
                            // cooldown for transconjugant // currently defined at vector level
                            double transconjugentCooldown = ( vector.isAspect( AspectRef.transconjugentCooldown ) ?
                                    vector.getDouble(AspectRef.transconjugentCooldown) : vector.getDouble(AspectRef.transferCooldown));
                            receiverVector.set(AspectRef.readyToDonate, now + transconjugentCooldown);
                            receiverVector.set(AspectRef.vectorReceivedTime, now );
                            
                            // set transitory derepression time
                            if (vector.isAspect(AspectRef.transitoryDerepression)) {
                                double transitoryDerepression = vector.getDouble(AspectRef.transitoryDerepression);
                                receiverVector.set(AspectRef.derepressionTime, now + transitoryDerepression);
                            }
                            
                            // transfer copy of the vector to selected neighbouring agent
                            selected.addVector(receiverVector);

                            if (Log.shouldWrite(Log.Tier.NORMAL)) {
                                Log.out(Log.Tier.NORMAL, "Vector (ID:" + vector.identity() + ") transferred from Donor (ID:" + donor.identity() + ") to Recipient (ID:" + selected.identity() +
                                        ") at time " + now);
                            }
                            
                            // Overwrite aspects to copy of recipient with that of donor
                            if (vector.isAspect(AspectRef.aspectsToTransfer)) {
                                Set<String> aspectsToCopy = new HashSet<String>(); // Make empty hashmap for listing aspects to copy
		                        String[] aspects_transfer = (String[]) vector.get(AspectRef.aspectsToTransfer); // Retrieve names of aspects to copy
                                aspectsToCopy.addAll(Arrays.asList(aspects_transfer)); // Write aspects to transfer in hashmap
                            
                                for (String aspect : aspectsToCopy) {
                                   selected.set(aspect, donor.get(aspect)); 
                                }
                            }
                            // cooldown for donor
                            vector.set(AspectRef.readyToDonate, now + vector.getDouble(AspectRef.transferCooldown));

                            // this currently assumes transfer cooldown is always lower than transconjugent cooldown
                            if ( (vector.getDouble(AspectRef.transferCooldown) / this.getTimeStepSize()) < 1.0 )
                                Log.out(Log.Tier.CRITICAL, this.getClass().getSimpleName() + " timestep exceeds " + AspectRef.transferCooldown );
                            break; //donor will be on cooldown and won't transfer again during this cycle.
                        }
                        testTally -= 1.0; // JG: this should remain, even when successful transfer, as the duration of 1 transfer is assumed to correspond with 1 tally (discuss whether that's correct)
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
                if (a.isAspect(AspectRef.hostIncompatibilities)) { //NOTE renamed to hostIncompatibilities
                    for (String incGroup : a.getStringA(AspectRef.hostIncompatibilities))
                        if (incGroup.equals(incompatibilityGroup))
                            return false;
                }
                for (Agent vector : a.getVectors()) {
                    if (vector.isAspect(AspectRef.vectorIncompatibility)) //NOTE renamed from incGroup to vectorIncompatibility
                        if (vector.getString(AspectRef.vectorIncompatibility).equals(incompatibilityGroup))
                            return false;
                }
                return true; //return true of inc group is not encountered.
            }
        return false; //return false if not a potential host.
    }

}
