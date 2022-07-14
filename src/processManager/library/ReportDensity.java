package processManager.library;

import analysis.simple.Density;
import compartment.AgentContainer;
import compartment.EnvironmentContainer;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import org.w3c.dom.Element;
import processManager.ProcessManager;
import referenceLibrary.AspectRef;
import utility.Helper;

public class ReportDensity extends ProcessManager
{

    Density density;

    @Override
    public void init(Element xmlElem, EnvironmentContainer environment,
                     AgentContainer agents, String compartmentName)
    {
        super.init(xmlElem, environment, agents, compartmentName);

        double[] domain = getDoubleA( AspectRef.domain );
        double[] voxel = getDoubleA( AspectRef.voxel );

        if( Helper.isNullOrEmpty( domain ) || Helper.isNullOrEmpty( voxel ) ) {
            Idynomics.simulator.interupt(this.getClass().getSimpleName() + " initialized with invalid" +
                    "domain or voxel specification, stopping");
        }
        else {
            density = new Density( Vector.convert(domain), Vector.convert(voxel) );
//            density = new Density(new Double[]{202.0, 202.0, 4.0}, new Double[]{2.0, 2.0, 2.0});
        }

        /**
         * include bacsim agent file interpretation
         */
        String bacSimfile = getString(AspectRef.fileName);
        if( ! Helper.isNullOrEmpty(bacSimfile) ) {
            Double agentDensity = getDouble( AspectRef.agentDensity );
            density.read(bacSimfile, agentDensity); // 0.29
            density.writeCSV("bacSim");
        }

    }

    @Override
    protected void internalStep() {

        density.read(this._agents.getAllAgents(), this._agents.getShape());
        density.writeCSV("density_iDyno");
    }
}
