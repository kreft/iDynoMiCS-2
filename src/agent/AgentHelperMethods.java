package agent;

import dataIO.Log;
import expression.Expression;
import idynomics.Global;
import idynomics.Idynomics;
import referenceLibrary.AspectRef;
import surface.link.LinearSpring;
import surface.link.Spring;
import surface.link.TorsionSpring;
import utility.Helper;

public class AgentHelperMethods {
    public static void springInitialization(Agent a)
    {
        Body b = (Body) a.get(AspectRef.agentBody);
        for( Spring s : b.getSpringsToEvaluate())
        {
            if( s != null )
            {
                if( !s.ready())
                {
                    s.setStiffness( Helper.setIfNone( a.getDouble(AspectRef.spineStiffness),
                            1e6));
                    //TODO warn user if not set
                    if( s instanceof LinearSpring)
                    {
                        Expression spineFun;
                        if ( !Helper.isNullOrEmpty( a.getValue(
                                AspectRef.agentSpineFunction )))
                            spineFun = new Expression((String)
                                    a.getValue(AspectRef.agentSpineFunction ));
                        else
                            spineFun = Global.fallback_spinefunction;
                        s.setSpringFunction( spineFun );
                    }
                    else if( s instanceof TorsionSpring)
                    {
                        Expression torsFun = null;
                        if ( !Helper.isNullOrEmpty(
                                a.getValue(AspectRef.torsionFunction)))
                            torsFun = (Expression)
                                    a.getValue(AspectRef.torsionFunction);
                        else
                        {
                            /* TODO set default maybe? */
                            Idynomics.simulator.interupt(
                                    "missing torsion spring function in relax");
                        }
                        s.setSpringFunction( torsFun );
                    }
                }
            }
        }
    }
}
