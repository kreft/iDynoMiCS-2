package dataIO;

import generalInterfaces.Quizable;
import grid.SpatialGrid;
import idynomics.AgentContainer;
import idynomics.Param;

import java.util.List;
import agent.Agent;
import dataIO.Log.tier;
import linearAlgebra.Vector;

/**
 * 
 * @author baco
 *
 */
public class SvgExport {
	int filewriterfilenr = 0;
	FileHandler svgFile = new FileHandler();
	public double scalar = 25.0;
	public double spacer = 25.0;
	
	private String DigitFilenr(int filenr) {
		String apzero = String.valueOf(filenr);
		for(int i = 0; i < 4-String.valueOf(filenr).length(); i++)
			apzero = "0" + apzero;
		return apzero;
	}
	
	private String toSvg(double[] vector)
	{
		double[] v = Vector.zerosDbl(2);
		int nDim = Math.min(vector.length, 2);
		for ( int i = 0; i < nDim; i++ )
			v[i] = vector[i];
		/**
		 * work out how to do scaling and domain properly and consistently
		 */
		return " cx=\"" + Double.toString(spacer+scalar*v[0]) + "\" cy=\"" + Double.toString(spacer+scalar*v[1]) + "\" ";
	}
	
	public void newSvg(String prefix)
	{
		String fileString = Param.outputLocation + prefix + "/" 
				+ prefix + DigitFilenr(filewriterfilenr) + ".svg";
		svgFile.fnew(fileString);
		Log.out(tier.EXPRESSIVE, "Writing new file: " + fileString);

		svgFile.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");
	}
	
	public void closeSvg()
	{
		svgFile.write("</svg>\n");
		svgFile.fclose();
		filewriterfilenr++;
	}
	
	public void writepov(String prefix, AgentContainer agentContainer) 
	{
		/* initiate new file */
		newSvg(prefix);
		
		/* draw computational domain rectangle */
		rectangle(Vector.zerosDbl(agentContainer.getNumDims()),
				agentContainer.getShape().getDimensionLengths(), "GRAY");
		
		/*  */
		SpatialGrid solute;
		
		/* draw agents NOTE currently only coccoid */
		for (Agent a: agentContainer.getAllLocatedAgents()) {	
			@SuppressWarnings("unchecked")
			List<double[]> joints = (List<double[]>) a.get("joints");
			for (int i = 0; joints.size() > i; i++)
			{
				circle(joints.get(i), a.getDouble("radius"), a.getString("pigment"));
			}
		}
		
		/* close svg file */
		closeSvg();
	}
	
	/**
	 * Work in progress, dynamic graphical output
	 *
	 */
		
	public void circle(double[] center, double radius, String pigment)
	{

		svgFile.write("<circle " + toSvg(center) + "r=\"" +
				scalar * radius + "\" fill=\"" + pigment
				+ "\" />\n" );
	
	}
	
	public void rectangle(double[] location, double[] dimensions, String pigment)
	{
		svgFile.write("<rect x=\"" + (spacer + scalar*location[0]) + "\" y=\"" + 
				(spacer + scalar*location[1]) + "\" width=\"" + dimensions[0] * 
				scalar + "\" height=\"" + dimensions[1] * scalar + 
				"\" fill=\"" + pigment + "\" />\n");
	}
	
	private void line(FileHandler File, Quizable q)
	{
		
	}
}


