package dataIO;
import java.util.List;

import agent.Agent;
import linearAlgebra.Vector;

public class PovExport {
	static int filewriterfilenr;
	
	private static String DigitFilenr(int filenr) {
		String apzero = String.valueOf(filenr);
		for(int i = 0; i < 4-String.valueOf(filenr).length(); i++)
			apzero = "0" + apzero;
		return apzero;
	}
	
	private static String toPov(double[] vector)
	{
		double[] v = Vector.zerosDbl(3);
		int nDim = Math.min(vector.length, 3);
		for ( int i = 0; i < nDim; i++ )
			v[i] = vector[i];
		return "< " + Double.toString(v[1]) + " , " + Double.toString(v[0]) + " , " + Double.toString(v[2]) + " >\n";
	}
	
	public static void writepov(String prefix, List<Agent> agents) 
	{
		FileHandler povFile = new FileHandler();
		
		povFile.fnew("../../Simulations/" + prefix + "/" 
		+ prefix + DigitFilenr(filewriterfilenr) + ".pov");

		povFile.write("#declare Count = " + filewriterfilenr + ";\n");
		povFile.write("#include \"../sceneheader.inc\"\n");
		
		for (Agent a: agents) {	
			@SuppressWarnings("unchecked")
			List<double[]> joints = (List<double[]>) a.get("joints");
			for (int i = 0; joints.size() > i; i++)
			{
				// sphere
				povFile.write("sphere { \n" + toPov(joints.get(i)) + 
						a.get("radius") + "\n pigment { " + a.get("pigment") 
						+ " } }\n" );
				if (joints.size() > i+1)
				{
					//cylinder
					povFile.write("cylinder { \n" + toPov(joints.get(i)) + 
							", " + toPov(joints.get(i+1)) + a.get("radius") 
							+ "\n pigment { " + a.get("pigment") + " } }\n" );
				}
			}
		}
		povFile.write("#include \"../scenefooter.inc\"\n");
		povFile.fclose();
	}
	
}

