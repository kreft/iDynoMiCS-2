package dataIO;

import generalInterfaces.Quizable;
import idynomics.AgentContainer;
import idynomics.Param;

import java.util.List;
import agent.Agent;
import dataIO.Feedback.LogLevel;
import linearAlgebra.Vector;

/**
 * 
 * @author baco
 *
 */
public class SvgExport {
	int filewriterfilenr = 0;
	public List<Task> tasks;
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
	
	public void writepov(String prefix, AgentContainer agentContainer) 
	{
		List<Agent> agents = agentContainer.getAllLocatedAgents();
		FileHandler svgFile = new FileHandler();
		String fileString = Param.outputLocation + prefix + "/" 
				+ prefix + DigitFilenr(filewriterfilenr) + ".svg";
		svgFile.fnew(fileString);
		Feedback.out(LogLevel.EXPRESSIVE, "Writing new file: " + fileString);

		svgFile.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");
		
		double[] compDomain = agentContainer.getShape().getDimensionLengths();
		svgFile.write("<rect x=\"" + spacer + "\" y=\"" + spacer + "\" width=\"" 
				+ compDomain[0] * scalar + "\" height=\"" + compDomain[1] * 
				scalar + "\" fill=\"gray\" />\n");
		
		/**
		 *  the original
		 */
		
		for (Agent a: agents) {	
			@SuppressWarnings("unchecked")
			List<double[]> joints = (List<double[]>) a.get("joints");
			for (int i = 0; joints.size() > i; i++)
			{
				// sphere
				svgFile.write("<circle " + toSvg(joints.get(i)) + "r=\"" +
						scalar * (double) a.get("radius") + "\" fill=\"" + a.get("pigment") 
						+ "\" />\n" );
//				if (joints.size() > i+1)
//				{
//					//cylinder
//
//				}
			}
		}
		
		
		/**
		 * ifAgent task .. work in progress
		 */
//		for (Task t: tasks)
//			t.draw(svgFile);

		
		svgFile.write("</svg>\n");
		svgFile.fclose();
		filewriterfilenr++;
	}
	
	/**
	 * Work in progress, dynamic graphical output
	 *
	 */
	public class Task
	{
		public List<Quizable> quizables;
		public String bool = "#isLocated";
		public String draw = "circle";
		public String centers = "joints";
		public String pigment = "pigment";
		public String radius = "radius";
		
		void draw(FileHandler File)
		{
			for (Quizable q: quizables) {
				if ((boolean) q.get(bool)) {
					switch(draw)
					{
					case "circle": circle(File,q);
					case "line": line(File,q);
					}
				}
			}
		}
		
		private void circle(FileHandler File, Quizable q)
		{
			@SuppressWarnings("unchecked")
			List<double[]> cs = (List<double[]>) q.get(centers);
			for (double[] c: cs)
			{
				// sphere
				File.write("<circle " + toSvg(c) + "r=\"" +
						scalar * (double) q.get(radius) + "\" fill=\"" + q.get(pigment) 
						+ "\" />\n" );
			}
		}
		
		private void line(FileHandler File, Quizable q)
		{
			
		}
	}
}

