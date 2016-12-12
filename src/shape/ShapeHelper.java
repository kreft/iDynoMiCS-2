/**
 * 
 */
package shape;

/**
 * \brief Static helper methods for shapes.
 * 
 * @author Stefan Lang (stefan.lang@uni-jena.de)
 *     Friedrich-Schiller University Jena, Germany 
 */
final class ShapeHelper
{
	/**
	 * \brief Converts the given resolution {@code res} .
	 * 
	 * @param shell Index of the shell.
	 * @param res Target resolution, in units of "quarter circles"
	 * @return Target resolution, in radians.
	 */
	static double scaleResolutionForShell(int shell, double res)
	{
		/* see Docs/polarShapeScalingDerivation */
		/* scale resolution to have a voxel volume of one for resolution one */
		return res * (2.0 / ( 2 * shell + 1));
	}

	/**	
	 * \brief Converts the given resolution {@code res} to account for varying 
	 * radius and polar angle.
	 * 
	 * @param shell Index of the shell.
	 * @param ring Index of the ring.
	 * @param ring_res TODO Stefan, please explain what this is.
	 * @param res Target resolution, in units of "quarter circles"
	 * @return Target resolution, in radians.
	 */
	static double scaleResolutionForRing(int shell, int ring,
												double ring_res, double res){
		/* see Docs/polarShapeScalingDerivation */
		/* scale resolution to have a voxel volume of one for resolution one */
		return res * 3.0 / ((1 + 3 * shell * (1 + shell)) 
			* (Math.cos(ring * ring_res) - Math.cos((1.0 + ring) * ring_res)));
	}
}
