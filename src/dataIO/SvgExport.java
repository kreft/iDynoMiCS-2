package dataIO;

import java.util.List;

import agent.Agent;
import linearAlgebra.Vector;

public class SvgExport {
	int filewriterfilenr = 0;
	
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
		return " cx=\"" + Double.toString(500+50.0*v[0]) + "\" cy=\"" + Double.toString(500+50.0*v[1]) + "\" ";
	}
	
	public void writepov(String prefix, List<Agent> agents) 
	{
		FileHandler svgFile = new FileHandler();
		
		svgFile.fnew("../../Simulations/" + prefix + "/" 
		+ prefix + DigitFilenr(filewriterfilenr) + ".svg");

		svgFile.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");
		
		for (Agent a: agents) {	
			@SuppressWarnings("unchecked")
			List<double[]> joints = (List<double[]>) a.get("joints");
			for (int i = 0; joints.size() > i; i++)
			{
				// sphere
				svgFile.write("<circle " + toSvg(joints.get(i)) + "r=\"" +
						50.0* (double) a.get("radius") + "\" fill=\"" + a.get("pigment") 
						+ "\" />\n" );
				if (joints.size() > i+1)
				{
					//cylinder

				}
			}
		}
		svgFile.write("</svg>\n");
		svgFile.fclose();
		filewriterfilenr++;
	}
	
}

