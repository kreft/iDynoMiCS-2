package aspect.calculated;

import agent.Agent;
import aspect.AspectInterface;
import aspect.Calculated;

/** quick and dirty for rendering
 *
 */
public class HasVector extends Calculated  {

    public Object get(AspectInterface aspectOwner)
    {
        return String.valueOf(!((Agent) aspectOwner).getVectors().isEmpty());
    }
}
