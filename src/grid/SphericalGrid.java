package grid;

import shape.Shape;

/**
 * \brief A grid with a spherical coordinate system.
 *  
 *  <p>Here we use the {@code r, φ, θ)} convention:</p><ul><li>{@code r} is the
 *  <i>radial</i> coordinate, i.e. Euclidean distance from the origin</li><li>
 *  {@code φ (phi)} is the <i>polar</i> coordinate (also known as the
 *  <i>zenith</i> or <i>colatitude</i>) and takes values between 0 and π 
 *  radians</li></ul><p><li>
 *  {@code θ (theta)} is the <i>azimuthal</i> coordinate (also known as the
 *  <i>longitude</i>) and takes values between 0 and 2π radians</li>See 
 *  <a href="http://mathworld.wolfram.com/SphericalCoordinates.html">here</a> 
 *  for more details.</p>  
 *  
 * @author Stefan Lang, Friedrich-Schiller University Jena
 * (stefan.lang@uni-jena.de)
 * @author Robert Clegg (r.j.clegg.bham.ac.uk) University of Birmingham, U.K.
 */
public class SphericalGrid extends SpatialGrid
{	
	/*************************************************************************
	 * CONSTRUCTORS
	 ************************************************************************/
	
	public SphericalGrid(Shape shape){
		super(shape);
	}
}
