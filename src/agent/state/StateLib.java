package agent.state;

import agent.state.secondary.*;

public class StateLib {

	/**
	 * Allows assignment of secondary states from the xml file
	 * @param state
	 * @return
	 */
	public static State get(String state)
	{
		switch (state) 
		{
			case "joints" : return new JointsState(); // currently used by povExport
			case "simpleVolume" : return new SimpleVolumeState();
			case "coccoidRadius" : return new CoccoidRadius();
			case "lowerBoundingBox" : return new LowerBoundingBox();
			case "dimensionsBoundingBox" : return new DimensionsBoundingBox();
		}
		return null;
	}
}

