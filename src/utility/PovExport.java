package utility;

import java.io.BufferedWriter;
import java.io.File;
import java.util.List;

import agent.Agent;
import linearAlgebra.Vector;

public class PovExport {
	static BufferedWriter output;
	static int filewriterfilenr;
	static String filewriterfileprefix;
	
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
	
	public static void writepov(String prefix, List<Agent> agents) {
		
		try {
			String location = "../../" + filewriterfileprefix + "/";
			File theDir = new File(location);

			// if the directory does not exist, create it
			if (!theDir.exists()) {
			    System.out.println("creating directory: " + location);
			    boolean result = false;
			    try{
			        theDir.mkdir();
			        result = true;
			    } 
			    catch(SecurityException se){
			        //handle it
			    }        
			    if(result) {    
			        System.out.println("DIR created");  
			    }
			}
			
			// Create file 	
			File f = new File("../../" + filewriterfileprefix + "/" 
			+ filewriterfileprefix + DigitFilenr(filewriterfilenr) + ".pov");
			f.delete();
			java.io.FileWriter fstream = new java.io.FileWriter(f, true);
			PovExport.output = new BufferedWriter(fstream);
			
			output.write("#declare Count = " + filewriterfilenr + ";\n");
			output.write("#include \"sceneheader.inc\"\n");
			
			for (Agent a: agents) {	
				@SuppressWarnings("unchecked")
				List<double[]> joints = (List<double[]>) a.get("joints");
				for (int i = 0; joints.size() < i; i++)
				{
					// sphere
					output.write("sphere { \n" + toPov(joints.get(i)) + 
							a.get("radius") + "\n pigment { " + a.get("pigment") 
							+ "} }\n" );
					if (joints.size() < i-1)
					{
						//cylinder
						output.write("cylinder { \n" + toPov(joints.get(i)) + 
								", " + toPov(joints.get(i+1)) + a.get("radius") 
								+ "\n pigment { " + a.get("pigment") + "} }\n" );
					}
				}
			}
			
			output.write("#include \"scenefooter.inc\"\n");
			output.flush();
			output.close();
			
		}catch (Exception e){
			System.err.println("Error: " + e.getMessage()); 
		}
		
	}
	
}

