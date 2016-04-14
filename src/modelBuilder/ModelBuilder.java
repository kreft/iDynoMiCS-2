package modelBuilder;

import java.util.List;

import dataIO.Log;
import dataIO.Log.Tier;

/**
 * \brief Class for building up a model, setting all required inputs as 
 * appropriate.
 * 
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
// TODO move to IsSubmodel? Separated temporarily for development purposes
public class ModelBuilder
{
	public static void buildModel(IsSubmodel submodel)
	{
		List<InputSetter> inputs = submodel.getRequiredInputs();
		Object input = null;
		String name;
		for ( InputSetter setter : inputs )
		{
			name = setter.getName();
			Log.out(Tier.EXPRESSIVE, "Setting input: "+name);
			if ( setter instanceof SubmodelMaker )
			{
				SubmodelMaker smMaker = (SubmodelMaker) setter;
			}
			else if ( setter instanceof ParameterSetter )
			{
				ParameterSetter pSetter = (ParameterSetter) setter;
				String classType = pSetter.getClassType();
				// TODO
				
			}
			else
			{
				// TODO safety
			}
			submodel.acceptInput(name, input);
		}
	}
}
