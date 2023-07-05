package render;

import com.jogamp.opengl.GLAutoDrawable;

/**
 * CommandMediators are passed to the openGL renderer to express the 3D scene
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public interface CommandMediator
{
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public abstract float kickback();
	
	/**
	 * \brief TODO
	 * 
	 * @return
	 */
	public default double[] orientation() 
	{
			return new double[] { 0.0, 0.0 };
	}

	/**
	 * \brief Simple drawing without zoom and tilt options.
	 * 
	 * @param drawable
	 */
	public abstract void draw(GLAutoDrawable drawable);

	public abstract void init(GLAutoDrawable drawable);
}
