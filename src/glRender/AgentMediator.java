package glRender;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
//import com.jogamp.opengl.glu.GLU;

import agent.Agent;
import idynomics.AgentContainer;
import idynomics.EnvironmentContainer;
import idynomics.NameRef;
import linearAlgebra.Vector;
import surface.Ball;
import surface.Surface;


/**
 * Agent mediator, draws agents and plane / cube indication the computational
 * domain
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class AgentMediator implements CommandMediator {
	protected AgentContainer agents;
	protected EnvironmentContainer environment;
	private String pigment;
	private float[] rgba;
//	private GLU glu = new GLU();
	public float kickback;
	private GL2 gl;

	/**
	 * used to set up the open gl camera
	 */
	@Override
	public float kickback() {
		return 2f * kickback;
	}
	
	/**
	 * assign agent container via the constructor
	 * @param agents
	 */
	public AgentMediator(AgentContainer agents, EnvironmentContainer environment)
	{
		this.agents = agents;
		this.environment = environment;
	}

	/**
	 * draw the the relevant objects in 3d
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void draw(GLAutoDrawable drawable) {
		gl = drawable.getGL().getGL2();

		/* get the domain lengths to draw itself and scaling */
		double[] domainLengths = agents.getShape().getDimensionLengths();
		double[] domain = new double[]{ domainLengths[0], domainLengths[1],
				(domainLengths.length > 2 ? domainLengths[2] : 0.0)};
		
		kickback = (float) Math.max(domain[0], Math.max(domain[1], domain[2]));
		
        if(domain[2] != 0.0f)
        	domainCube(drawable,domain);
        
		/* get the surfaces from the agents */
		for ( Agent a : this.agents.getAllLocatedAgents() )
		{

			for ( Surface s : (List<Surface>) (a.isAspect(
					NameRef.surfaceList) ? a.get(NameRef.surfaceList) :
					new LinkedList<Surface>()))
			{
				pigment = a.getString("pigment");
				if(s instanceof Ball)
				{
					Ball ball = (Ball) s;
	
					switch (pigment)
					{
					case "GREEN" :
						  rgba = new float[] {0.1f, 1f, 0.1f};
						  break;
					case "RED" :
						  rgba = new float[] {1f, 0.1f, 0.1f};
						  break;
					case "BLUE" :
						  rgba = new float[] {0.1f, 0.1f, 1f};
						  break;
					default :
						  rgba = new float[] {1f, 1f, 1f};
						  break;
					}
					sphere(drawable, domain, ball._point.getPosition(), 
	        		ball._radius);
				}
			}
		}
		
		/* draw the domain square */
		plane(drawable,domain);
		
	}
	
	/**
	 * draw a scaled sphere positioned relative to the domain
	 * @param drawable
	 * @param domain
	 * @param pos
	 * @param radius
	 */
	private void sphere(GLAutoDrawable drawable, double[] domain, double[] pos, 
			double radius) 
	{
		double[] p = new double[]{ pos[0], pos[1], 
				(pos.length > 2 ? pos[2] : 0.0)};
		
		int i, j;
		final int lats = 16;
		final int longs = 16;
		for(i = 0; i <= lats; i++) 
		{
	  		double lat0 = Math.PI * (-0.5 + (double) (i - 1) / lats);
	  		double z0  = Math.sin(lat0);
	  		double zr0 =  Math.cos(lat0);
	
	  		double lat1 = Math.PI * (-0.5 + (double) i / lats);
	     	double z1 = Math.sin(lat1);
	     	double zr1 = Math.cos(lat1);

	     	gl.glLoadIdentity();
			gl.glTranslated(p[0] - domain[0] * 0.5, p[1] - domain[1] * 0.5, 
					p[2] - domain[2] * 0.5);
	     	gl.glScaled(radius, radius, radius);
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
			gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.1f);
			gl.glColor3f(rgba[0], rgba[1], rgba[2]);
			gl.glBegin(GL2.GL_QUAD_STRIP);
			for(j = 0; j <= longs; j++) 
			{
				double lng = 2 * Math.PI * (double) (j - 1) / longs;
				double x = Math.cos(lng);
				double y = Math.sin(lng);
				
				gl.glNormal3d(x * zr0, y * zr0, z0);
				gl.glVertex3d(x * zr0, y * zr0, z0);
				gl.glNormal3d(x * zr1, y * zr1, z1);
				gl.glVertex3d(x * zr1, y * zr1, z1);
			}
			gl.glEnd();
		}
	
	}
	
	/**
	 * draw a domain plane
	 * @param drawable
	 * @param domain
	 */
	private void plane(GLAutoDrawable drawable, double[] domain) 
	{
		rgba = new float[] {0.3f, 0.3f, 0.3f};
		plane(drawable, domain, Vector.zeros(domain), Vector.onesDbl(domain.length), rgba, false);
	}
	
	/**
	 * draw a standard plane positioned and scaled relative to the domain
	 * @param drawable
	 * @param domain
	 * @param origin
	 * @param lengths
	 * @param color
	 * @param lighting
	 */
	private void plane(GLAutoDrawable drawable, double[] domain, double[] origin
			, double[] lengths ,float[] color, boolean lighting)
	{
		gl.glLoadIdentity();
		gl.glTranslated(origin[0], origin[1], origin[2]);
		gl.glScaled(domain[0]*0.5, domain[1]*0.5, domain[2]*0.5);
		gl.glScaled(lengths[0], lengths[1], lengths[2]);
		if (lighting)
		{
	        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, color, 0);
	        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, color, 0);
	        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.1f);
		}
		else
		{
			gl.glDisable(GL2.GL_LIGHTING);
		}
		gl.glBegin(GL2.GL_QUADS);             
		gl.glColor3f(color[0],color[1],color[2]);    
			gl.glVertex3d(-1.0, 1.0, -1.0); 
		    gl.glVertex3d( 1.0, 1.0, -1.0);
		    gl.glVertex3d( 1.0, -1.0, -1.0);  
		    gl.glVertex3d(-1.0, -1.0, -1.0); 
		gl.glEnd();
		if (lighting)
		{
			
		}
		else
		{
			gl.glEnable(GL2.GL_LIGHTING);
		}
	}
	
	/**
	 * draw a alpha blend domain cube (for 3D simulations)
	 * @param drawable
	 * @param domain
	 */
	private void domainCube(GLAutoDrawable drawable, double[] domain) 
	{
		
		gl.glLoadIdentity();
		gl.glEnable(GL2.GL_BLEND);
		gl.glDisable(GL2.GL_DEPTH_TEST);
		gl.glScaled(0.5f*domain[0], 0.5f*domain[1], 0.5f*domain[2]);
		
		rgba = new float[] {0.1f, 0.1f, 1f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.1f);
        gl.glColor3f(rgba[0],rgba[1],rgba[2]);
		gl.glBegin(GL2.GL_QUADS);                  // Start Drawing The Cube
		
		gl.glVertex3f( 1.0f, 1.0f,-1.0f);          // Top Right Of The Quad (Top)
		gl.glVertex3f(-1.0f, 1.0f,-1.0f);          // Top Left Of The Quad (Top)
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);          // Bottom Left Of The Quad (Top)
		gl.glVertex3f( 1.0f, 1.0f, 1.0f);          // Bottom Right Of The Quad (Top)
		
		gl.glVertex3f( 1.0f,-1.0f, 1.0f);          // Top Right Of The Quad (Bottom)
		gl.glVertex3f(-1.0f,-1.0f, 1.0f);          // Top Left Of The Quad (Bottom)
		gl.glVertex3f(-1.0f,-1.0f,-1.0f);          // Bottom Left Of The Quad (Bottom)
		gl.glVertex3f( 1.0f,-1.0f,-1.0f);          // Bottom Right Of The Quad (Bottom)

		gl.glVertex3f( 1.0f, 1.0f, 1.0f);          // Top Right Of The Quad (Front)
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);          // Top Left Of The Quad (Front)
		gl.glVertex3f(-1.0f,-1.0f, 1.0f);          // Bottom Left Of The Quad (Front)
		gl.glVertex3f( 1.0f,-1.0f, 1.0f);          // Bottom Right Of The Quad (Front)

		gl.glVertex3f( 1.0f,-1.0f,-1.0f);          // Bottom Left Of The Quad (Back)
		gl.glVertex3f(-1.0f,-1.0f,-1.0f);          // Bottom Right Of The Quad (Back)
		gl.glVertex3f(-1.0f, 1.0f,-1.0f);          // Top Right Of The Quad (Back)
		gl.glVertex3f( 1.0f, 1.0f,-1.0f);          // Top Left Of The Quad (Back)
		
		gl.glVertex3f(-1.0f, 1.0f, 1.0f);          // Top Right Of The Quad (Left)
		gl.glVertex3f(-1.0f, 1.0f,-1.0f);          // Top Left Of The Quad (Left)
		gl.glVertex3f(-1.0f,-1.0f,-1.0f);          // Bottom Left Of The Quad (Left)
		gl.glVertex3f(-1.0f,-1.0f, 1.0f);          // Bottom Right Of The Quad (Left)

        gl.glVertex3f( 1.0f, 1.0f,-1.0f);          // Top Right Of The Quad (Right)
        gl.glVertex3f( 1.0f, 1.0f, 1.0f);          // Top Left Of The Quad (Right)
        gl.glVertex3f( 1.0f,-1.0f, 1.0f);          // Bottom Left Of The Quad (Right)
        gl.glVertex3f( 1.0f,-1.0f,-1.0f);          // Bottom Right Of The Quad (Right)
	    gl.glEnd();                        // Done Drawing The Quad
	    
	    gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDisable(GL2.GL_BLEND);
	}

}
