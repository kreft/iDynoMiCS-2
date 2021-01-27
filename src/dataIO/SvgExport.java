package dataIO;

import org.w3c.dom.Element;

import idynomics.Global;
import idynomics.Idynomics;
import linearAlgebra.Vector;
import settable.Settable;
import surface.Ball;
import surface.Rod;

/**
 * \brief TODO
 * 
 * TODO sort agents in z-axis for topdown view 3d simulations
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class SvgExport implements GraphicalExporter
{
	/**
	 * TODO
	 */
	protected int _filewriterfilenr = 0;
	/**
	 * TODO
	 */
	protected FileHandler _svgFile = new FileHandler();
	/**
	 * TODO
	 */
	protected double _scalar = 10.0;
	/**
	 * TODO
	 */
	protected double _spacer = 25.0;
	
	
	private static final String[] CIRCLE_LABELS = new String[] {"cx","cy"};

	private static final String[] RECTANGLE_LABELS = new String[] {"x","y"};
	
	public void instantiate(Element xmlElem, Settable parent)
	{
		/* init something from xml? */
	}
	
	/**
	 * handles incrementing file numbering
	 * @param filenr
	 * @return
	 */
	private String DigitFilenr(int filenr) {
		String apzero = String.valueOf(filenr);
		for(int i = 0; i < Global.file_number_of_digits-String.valueOf(filenr).length(); i++)
			apzero = "0" + apzero;
		return apzero;
	}
	
	/**
	 * writes circle center position in svg format
	 * @param vector
	 * @return
	 */
	private String toSvg(double[] vector, String[] labels)
	{
		String out = " ";
		for ( int i = 0; i < labels.length; i++ )
			out += labels[i] + "=\"" + 
					Double.toString( _spacer + _scalar * vector[i] ) + "\" ";
		return out;
	}
	
	/**
	 * create a new svg file with prefix in appropriate folder
	 * @param prefix
	 */
	public void createFile(String prefix)
	{
		String fileString = Idynomics.global.outputLocation + prefix + "/" 
				+ prefix + "_" + DigitFilenr(_filewriterfilenr)  
				+ "_" + Idynomics.simulator.timer.getCurrentIteration() + ".svg";
		_svgFile.fnew(fileString);
		_svgFile.write("<svg xmlns=\"http://www.w3.org/2000/svg\" "
				+ "version=\"1.1\">\n");
	}
	
	public void createCustomFile(String fileName)
	{
		String fileString = Idynomics.global.outputLocation + "/" 
				+ fileName + ".svg";
		_svgFile.fnew(fileString);
		_svgFile.write("<svg xmlns=\"http://www.w3.org/2000/svg\" "
				+ "version=\"1.1\">\n");
	}
	
	/**
	 * close the svg file and increment file number for next file
	 */
	public void closeFile()
	{
		_svgFile.write("</svg>\n");
		_svgFile.fclose();
		_filewriterfilenr++;
	}
	
	/**
	 * Work in progress, dynamic graphical output
	 *
	 */
	
	/**
	 * 
	 */
	public String resolveColour (Object pigment)
	{
		String rgbStatement = new String();
		float[] pigmentArray = new float[3];
		if (pigment instanceof String)
		{
			return (String) pigment;
		}
		else
		{
			pigmentArray = (float[]) pigment;
		}

		
		int red = (int) Math.round(255 * pigmentArray[0]);
		int green = (int) Math.round(255 * pigmentArray[1]);
		int blue = (int) Math.round(255 * pigmentArray[2]);
		
		rgbStatement = "rgb(" + red + "," + green + "," + blue + ")";

		return rgbStatement;
	}
	
	/**
	 * draw a circle
	 * @param center
	 * @param radius
	 * @param pigment
	 */
	public void circle(double[] center, double radius, String pigment)
	{
		_svgFile.write("<circle " + toSvg(center, CIRCLE_LABELS) + "r=\"" +
				_scalar * radius + "\" fill=\"" + pigment
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
		sb.append(" transform=\"translate(" + circle_center[0] + " " 
				+ circle_center[1] + ")\"/>");
		/* write to file */
		_svgFile.write(sb.toString());
	}
	
	/**
	 * draw a rectangle without rotation
	 * @param location
	 * @param dimensions
	 * @param pigment
	 */
	public void rectangle(double[] location, double[] dimensions, String pigment)
	{
		_svgFile.write("<rect " + toSvg( location, RECTANGLE_LABELS ) + 
				"width=\"" + dimensions[0] * _scalar + 
				"\" height=\"" + dimensions[1] * _scalar + 
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

	/**
	 * 
	 */
	public void draw(Ball ball, String pigment) 
	{
		this.circle(this.to2D(ball.getCenter()), ball.getRadius(), pigment);
	}

	/**
	 * 
	 */
	public void draw(Rod rod, String pigment) 
	{
		double[] posA = this.to2D(rod._points[0].getPosition());
		double[] posB = this.to2D(rod._points[1].getPosition());
		
		this.circle(posA, rod.getRadius(), pigment);
		this.circle(posB, rod.getRadius(), pigment);
		this.rectangle(posA, posB, rod.getRadius()*2.0, pigment);
	}

	/**
	 * 
	 */
	public void sphere(double[] center, double radius, String pigment) 
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void cylinder(double[] base, double[] top, double radius, 
			String pigment) 
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void cube(double[] lowerCorner, double[] dimensions, String pigment) 
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * 
	 */
	public void rectangle(double[] base, double[] top, 
			double width, String pigment) 
	{
		_svgFile.write("<line " + 
				toSvg( base, new String[] { "x1", "y1"} ) +
				toSvg( top,  new String[] { "x2", "y2"} ) +
				"style=\"stroke:" + pigment + 
				";stroke-width:" + _scalar*width + 
				"\" />\n");
	}
}


