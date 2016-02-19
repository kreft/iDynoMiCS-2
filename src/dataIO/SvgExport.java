package dataIO;

import idynomics.Param;
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
	
	/**
	 * handles incrementing file numbering
	 * @param filenr
	 * @return
	 */
	private String DigitFilenr(int filenr) {
		String apzero = String.valueOf(filenr);
		for(int i = 0; i < 4-String.valueOf(filenr).length(); i++)
			apzero = "0" + apzero;
		return apzero;
	}
	
	/**
	 * writes circle center position in svg format
	 * @param vector
	 * @return
	 */
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
	
	/**
	 * create a new svg file with prefix in appropriate folder
	 * @param prefix
	 */
	public void newSvg(String prefix)
	{
		String fileString = Param.outputLocation + prefix + "/" 
				+ prefix + DigitFilenr(filewriterfilenr) + ".svg";
		svgFile.fnew(fileString);
		Log.out(tier.EXPRESSIVE, "Writing new file: " + fileString);

		svgFile.write("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">\n");
	}
	
	/**
	 * close the svg file and increment file number for next file
	 */
	public void closeSvg()
	{
		svgFile.write("</svg>\n");
		svgFile.fclose();
		filewriterfilenr++;
	}
	
	/**
	 * Work in progress, dynamic graphical output
	 *
	 */
	
	/**
	 * draw a circle
	 * @param center
	 * @param radius
	 * @param pigment
	 */
	public void circle(double[] center, double radius, String pigment)
	{
		svgFile.write("<circle " + toSvg(center) + "r=\"" +
				scalar * radius + "\" fill=\"" + pigment
				+ "\" />\n" );
	}
	
	/**
	 * draw a rectangle
	 * @param location
	 * @param dimensions
	 * @param pigment
	 */
	public void rectangle(double[] location, double[] dimensions, String pigment)
	{
		svgFile.write("<rect x=\"" + (spacer + scalar*location[0]) + "\" y=\"" + 
				(spacer + scalar*location[1]) + "\" width=\"" + dimensions[0] * 
				scalar + "\" height=\"" + dimensions[1] * scalar + 
				"\" fill=\"" + pigment + "\" />\n");
	}
	
	/**
	 * draw a line
	 * @param File
	 * @param q
	 */
	public void line(double[] positionA, double[] positionB, String pigment)
	{
		// TODO
	}
}


