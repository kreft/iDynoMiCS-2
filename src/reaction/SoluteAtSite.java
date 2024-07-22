package reaction;

import agent.Body.Morphology;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.Idynomics;
import utility.Helper;

public class SoluteAtSite {
	
	public final static String separator = "@";
	
	public String soluteName;
	
	public String siteName;
	
	public Object site;
	
	public SoluteAtSite (String string)
	{
		if (string.contains(separator))
    	{
    		String[] splitString = string.split(separator);
    		this.soluteName = splitString[0];
    		this.siteName = splitString[1];
    	}
    	else
    	{
    		this.soluteName = string;
    		this.siteName = "agent";
    	}
    	
    	this.site = Idynomics.
				simulator.getCompartment(this.siteName);
    	
    	if (Helper.isNullOrEmpty(this.site))
		{
			if (this.siteName.equalsIgnoreCase("agent"))
			{
				this.site = "agent";
			}
			
			else if (this.siteName.equalsIgnoreCase("compartment"))
			{
				this.site = "compartment";
			}
			
			else
			{
				if (Log.shouldWrite(Tier.CRITICAL))
					Log.out(Tier.CRITICAL, "Site " + this.site + " not found by "
					+ "SoluteAtSite class.");
			}
		}
	}
	
	public void setSite(String siteName)
	{
		this.siteName = siteName;
		this.site = Idynomics.
				simulator.getCompartment(this.siteName);
	}
	
}
