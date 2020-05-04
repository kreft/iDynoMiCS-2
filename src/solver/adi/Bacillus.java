package solver.adi;

import agent.Agent;
import solver.adi.Quad;
import agent.Body;

public class Bacillus extends Quad {
	
	  private String species; 	    
	  private Agent agent;
	  public double x, y, z;		  // center - z usually ignored
	  public double radius;	      // current size

	  public double getX() {return x;}
	  public double getY() {return y;}
	  public double getZ() {return z;}
	  
	  public Bacillus(Agent agent)
	  {
		  this.agent = agent;
		  Body aBody = (Body) agent.get("agentBody");
		  double[] agentPos = aBody.getCenter();
		  this.x = agentPos[0];
		  this.y = agentPos[1];
		  this.z = 0.0;
		  this.radius = agent.getDouble("radius");
	  }
	  
	 public void setMcr() 
	 {
		    lx = x - radius;
		    ly = y - radius;
		    lz = z - radius;
		    ux = x + radius;
		    uy = y + radius;
		    uz = z + radius;
		    growMcr(); // grow sorting quads above me as needed
	    }
	  
	public double getDryMass() {
		// TODO Auto-generated method stub
		return 0;
	}
	public int getProcess() {
		// TODO Auto-generated method stub
		return 0;
	}
	public double getGrowthRate() {
		// TODO Auto-generated method stub
		return 0;
	}
	public double getRateForReaction(int i, double d, double e) {
		// TODO Auto-generated method stub
		return 0;
	}
}
