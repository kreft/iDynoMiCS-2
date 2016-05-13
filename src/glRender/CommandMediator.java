package glRender;

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
	 * \brief Simple drawing without zoom and tilt options.
	 * 
	 * @param drawable
	 */
	public abstract void draw(GLAutoDrawable drawable);

}
