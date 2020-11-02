package dataIO;

import org.w3c.dom.Element;

import idynomics.Idynomics;
import settable.Settable;
import shape.Shape;
import surface.Ball;
import surface.Rod;

/**
 * \brief TODO class needs a rigorous update
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class PovExport implements GraphicalExporter
{
	/**
	 * 
	 */
	private FileHandler _povFile = new FileHandler();
	
	/**
	 * TODO
	 */
	protected int _filewriterfilenr = 0;
	
	/**
	 * returns file number with appending zeros as string.
	 * TODO: move to a place sensible for all file handling classes.
	 * @param filenr
	 * @return
	 */
	private String DigitFilenr(int filenr) {
		String apzero = String.valueOf(filenr);
		for(int i = 0; i < 6-String.valueOf(filenr).length(); i++)
			apzero = "0" + apzero;
		return apzero;
	}
	
	/**
	 * returns Location vector in POVray format
	 * @param vector
	 * @return
	 */
	private String toPov(double[] vector)
	{
		String out = "< ";
		for ( int i = 0; i < vector.length; i++)
		{
			out += vector[i];
			if ( i < vector.length - 1 )
				out += " , ";					
		}
		out += " >\n";
		return out;
	}

	/**
	 * 
	 * @param prefix
	 */
	public void createFile(String prefix)
	{
		String fileString = Idynomics.global.outputLocation + prefix + "/" 
				+ prefix + "_" + Idynomics.simulator.timer.getCurrentIteration()
				+ "_" + DigitFilenr(_filewriterfilenr) + ".pov";
		_povFile.fnew(fileString);

		_povFile.write("#declare Count = " + _filewriterfilenr + ";\n");
		_povFile.write("#include \"sceneheader.inc\"\n");
		
	}
	
	public void createCustomFile(String fileName)
	{
		String fileString = Idynomics.global.outputLocation + "/" 
				+ fileName + ".pov";
		_povFile.fnew(fileString);

		_povFile.write("#declare Count = " + _filewriterfilenr + ";\n");
		_povFile.write("#include \"sceneheader.inc\"\n");
		
	}
	
	
	
	/**
	 * 
	 */
	public void closeFile()
	{
		_povFile.write("#include \"scenefooter.inc\"\n");
		_povFile.fclose();
		_filewriterfilenr++;
	}
	
	public void instantiate(Element xmlElem, Settable parent)
	{
		/* init something from xml? */
	}
	
	/**
	 * adds scaled header and footer files to render folder if they are not
	 * created yet
	 * TODO clean-up
	 * @param prefix
	 * @param shape
	 */
	@Override
	public void init(String prefix, Shape shape)
	{
		String fileString = Idynomics.global.outputLocation + prefix + "/" 
				+ "sceneheader.inc";
		
		if ( ! _povFile.doesFileExist(fileString) )
		{
			_povFile.fnew(fileString);
			double[] domain = shape.getDimensionLengths();
			domain = this.to3D(domain);
			double kickback = 2.0 * Math.max( domain[0] , 
					Math.max( domain[1] , domain[2] ) );
			_povFile.write(
				"	camera { \n" +
				"		location < " + -0.5 * domain[0] + ", " + 
						-0.5 * domain[1] + ",  " + kickback + " >\n" +
				"		 up < 0.0,  1.0,  0.0 >\n" +
				"		 right < 1.0,  0.0,  0.0 >\n" +
				"		 look_at <" + -0.5 * domain[0] + ", " + 
						-0.5 * domain[1] + ",  0.0 >\n" +
				"		angle 60.0\n" +
				"	}\n" +
				"	background {\n" +
				"		color rgb < 1.0,  1.0,  1.0 >\n" +
				"	}\n" +
				"	light_source {\n" +
				"		  < " + -0.5 * domain[0] + ", " + 
						-0.5 * domain[1] + ",  " + kickback + " >\n" +
				"		color rgb < 1.0,  1.0,  1.0 >\n" +
				"	}\n" +
				"	light_source {\n" +
				"		  < " + (-0.5 * domain[0] + kickback) + ", " + 
						-0.5 * domain[1] + ",  " + kickback + " >\n" +
				"		color rgb < 0.5,  0.5,  0.5 >\n" +
				"	}\n" +
				"	union {\n" +
		
				"	#declare PURPLE = color rgb < 1.0 , 0.0 , 1.0 >;\n" +
				"	#declare ORANGE = color rgb < 1.0 , 0.6 , 0.1 >;\n" +
				"	#declare RED = color rgb < 1.0 , 0.0 , 0.0 >;\n" +
				"	#declare BLUE = color rgb < 0.0 , 0.0 , 1.0 >;\n" +
				"	#declare GREEN = color rgb < 0.0 , 1.0 , 0.0 >;\n" +
				"	#declare BLACK = color rgb < 0.0 , 0.0 , 0.0 >;\n" +
				"	#declare WHITE = color rgb < 1.0 , 1.0 , 1.0 >;\n"
				);
			_povFile.fclose();
		}
		
		fileString = Idynomics.global.outputLocation + prefix + "/" 
				+ "scenefooter.inc";
		
		if ( ! _povFile.doesFileExist(fileString) )
		{
			_povFile.fnew(fileString);
			_povFile.write(
//				"	translate < -0.5,  -0.5,  0.0 >\n" +
				"	rotate < 0.0,  0.0,  180.0 >\n" +
				"}"
				);
			_povFile.fclose();
		}
	}

	/**
	 * 
	 */
	public String resolveColour (Object pigment)
	{
		if (pigment instanceof String)
		{
			return (String) pigment;
		}
		else
		{
			float[] pigmentArray = (float[]) pigment;
			String rgbStatement = "color rgb < " + pigmentArray[0] + ", " + 
				pigmentArray[1] + ", " + pigmentArray[2] + " >";
			return rgbStatement;
		}
	}
	
	/**
	 * 
	 */
	public void draw(Ball ball, String pigment) 
	{
		this.sphere(this.to3D(ball.getCenter()), ball.getRadius(), pigment);
	}

	/**
	 * 
	 */
	public void draw(Rod rod, String pigment) 
	{
		double[] posA = this.to3D(rod._points[0].getPosition());
		double[] posB = this.to3D(rod._points[1].getPosition());
		
		this.sphere(posA, rod.getRadius(), pigment);
		this.sphere(posB, rod.getRadius(), pigment);
		this.cylinder(posA, posB, rod.getRadius(), pigment);
	}

	/**
	 * 
	 */
	public void sphere(double[] center, double radius, String pigment) 
	{
		_povFile.write("sphere { \n" + toPov(this.to3D(center)) + radius + "\n pigment { " 
				+ pigment + " } }\n" );
	}

	/**
	 * 
	 */
	public void circle(double[] center, double radius, String pigment) 
	{
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * 
	 */
	public void circleElement(double[] circle_center, double[] element_origin, 
			double[] dimension, double numPointsOnArc, String pigment) 
	{

	}

	/**
	 * draws a povray cylinder
	 * @param base: point at the center of the cylinder's base
	 * @param top: point at the center of the cylinder's top
	 * @param radius: radius of the cylinder
	 * @param pigment: povray compatible pigment String
	 */
	public void cylinder(double[] base, double[] top, double radius, 
			String pigment) 
	{
		_povFile.write("cylinder { \n" + toPov(this.to3D(base)) + 
				toPov(this.to3D(top)) + 
				radius + "\n pigment { " + pigment + " } }\n" );
		
	}

	/**
	 * 
	 */
	public void cube(double[] lowerCorner, double[] dimensions, String pigment) 
	{
		// TODO Auto-generated method stub
		
	}

	/**
	 * TODO finish 
	 *
	 */
	public void rectangle(double[] base, double[] top, double width, 
			String pigment) 
	{
		String out = "polygon { 4, ";
		out += toPov(base);
		out += toPov( new double[] {base[0], top[1]} );
		out += toPov(top);
		out += toPov( new double[] {top[0], base[1]} );
		
		// texture
		// pigment
		_povFile.write(out);
	}
	

	public void rectangle(double[] location, double[] dimensions, 
			String pigment)
	{
		
	}
}

