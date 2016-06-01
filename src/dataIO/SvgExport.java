package dataIO;

import dataIO.Log.Tier;
import idynomics.Idynomics;
import linearAlgebra.Vector;

/**
 * \brief TODO
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class SvgExport
{
	/**
	 * TODO
	 */
	int filewriterfilenr = 0;
	/**
	 * TODO
	 */
	FileHandler svgFile = new FileHandler();
	/**
	 * TODO
	 */
	public double scalar = 25.0;
	/**
	 * TODO
	 */
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
		String fileString = Idynomics.global.outputLocation + prefix + "/" 
				+ prefix + "_" + DigitFilenr(filewriterfilenr) + ".svg";
		svgFile.fnew(fileString);
		Log.out(Tier.EXPRESSIVE, "Writing new file: " + fileString);

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
	 * draw a non-full circle.
	 * @param center
	 * @param dimension The length in each dimension.
	 * @param pigment
	 */
	public void circle(double[] center, double[] dimension, String pigment)
	{
		circleElement(center, Vector.zeros(center), dimension, 100, pigment);
	}
	
	/**
	 * converts the (polar) rectangle defined by <b>origin</b> and 
	 * <b>dimension</b> into a cartesian polygon.
	 * Approximates curves with numPointsOnArc - 1 lines.
	 * 
	 * TODO find better names
	 * @param r0
	 * @param r1
	 * @param theta0
	 * @param theta1
	 * @param pigment
	 */
	public void circleElement(double[] circle_center, double[] element_origin, 
			double[] dimension ,double numPointsOnArc, String pigment)
	{
		/* create new polygon */
		StringBuilder sb = new StringBuilder("<polygon points=\"");
		/* first point is the origin */
		double[] cur_polar = new double[]{element_origin[0], element_origin[1]};
		double[] cur_cartesian = Vector.uncylindrify(cur_polar);
		/* append first point (origin)*/
		sb.append(cur_cartesian[0] + "," + cur_cartesian[1]);
		/* step size in degree: arc length divided by number of points */
		double step = element_origin[0] * dimension[1] / numPointsOnArc;
		/* append arc points along r0*/
		for (int i=0; i<numPointsOnArc; ++i){
			cur_polar[1] += step;
			Vector.uncylindrifyTo(cur_cartesian, cur_polar);
			sb.append(" " + cur_cartesian[0] + "," + cur_cartesian[1]);
		}
		/* cur_polar[1] should be theta1 here, so just switch to r1 shell */
		cur_polar[0] = element_origin[0] + dimension[0];
		/* append theta max point */
		cur_cartesian = Vector.uncylindrify(cur_polar);
		sb.append(cur_cartesian[0] + "," + cur_cartesian[1]);
		/* step size in degree: arc length divided by number of points */
		step = cur_polar[0] * dimension[1] / numPointsOnArc;
		/* append arc points along r1*/
		for (int i=0; i<numPointsOnArc; ++i){
			cur_polar[1] += step;
			Vector.uncylindrifyTo(cur_cartesian, cur_polar);
			sb.append(" " + cur_cartesian[0] + "," + cur_cartesian[1]);
		}
		
		/* finish points list */
		sb.append("\"");
		/* transform to 'real' location and end xml tag*/
		sb.append(" transform=\"translate(" + circle_center[0] + " " + circle_center[1]
																	+ ")\"/>");
		/* write to file */
		svgFile.write(sb.toString());
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


