package glRender;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;

import agent.Agent;
import idynomics.AgentContainer;
import idynomics.NameRef;
import surface.Ball;
import surface.Surface;
import utility.Helper;


/**
 * Very quick and dirty initial attempt at live simulation rendering, this
 * object construct a openGL 3D scene when it is called by the Render object
 * to prevent concurrent agentTree access, the agentTree now implements
 * synchronized public methods
 * TODO proper radius scaling, other shapes
 * @author baco
 *
 */
public class AgentMediator implements CommandMediator {
	protected AgentContainer agents;
	private String pigment;
	private float[] rgba;
	private GLU glu = new GLU();
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
	public AgentMediator(AgentContainer agents)
	{
		this.agents = agents;
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
		
		/* draw the domain square */
		domainPlane(drawable,domain);
		
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
		
	}
	
	
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
			gl.glBegin(gl.GL_QUAD_STRIP);
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
	
	private void domainPlane(GLAutoDrawable drawable, double[] domain) 
	{
		gl.glLoadIdentity();
		gl.glScaled(domain[0]*0.5, domain[1]*0.5, domain[2]*0.5);
		rgba = new float[] {0.2f, 0.2f, 0.2f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.1f);
		gl.glBegin(GL2.GL_QUADS);             
		gl.glColor3f(rgba[0],rgba[1],rgba[2]);    
			gl.glVertex3d(-1.0, 1.0, -1.0); 
		    gl.glVertex3d( 1.0, 1.0, -1.0);
		    gl.glVertex3d( 1.0, -1.0, -1.0);  
		    gl.glVertex3d(-1.0, -1.0, -1.0); 
		gl.glEnd();
	}
	
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
