package dataIO;

import generalInterfaces.Quizable;
import java.util.List;
import agent.Agent;
import linearAlgebra.Vector;

public class SvgExport {
	int filewriterfilenr = 0;
	public List<Task> tasks;
	
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
		return " cx=\"" + Double.toString(25+50.0*v[0]) + "\" cy=\"" + Double.toString(25+50.0*v[1]) + "\" ";
	}
	
	public void writepov(String prefix, List<Agent> agents) 
	{
		FileHandler svgFile = new FileHandler();
		
		svgFile.fnew("../../Simulations/" + prefix + "/" 
		+ prefix + DigitFilenr(filewriterfilenr) + ".svg");

		svgFile.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");
		svgFile.write("<rect x=\"25\" y=\"25\" width=\"450\" height=\"450\" fill=\"gray\" />\n");
		
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
						50.0* (double) a.get("radius") + "\" fill=\"" + a.get("pigment") 
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
						50.0* (double) q.get(radius) + "\" fill=\"" + q.get(pigment) 
						+ "\" />\n" );
			}
		}
		
		private void line(FileHandler File, Quizable q)
		{
			
		}
	}
}

