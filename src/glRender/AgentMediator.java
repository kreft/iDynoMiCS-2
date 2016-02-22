package glRender;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import agent.Agent;
import idynomics.AgentContainer;
import idynomics.NameRef;
import surface.Ball;
import surface.Surface;


/**
 * Very quick and dirty initial attempt at live simulation rendering, this
 * object construct a openGL 3D scene when it is called by the Render object
 * to prevent concurrent operation it suspends the simulator while it is writing
 * the 3d scene.
 * TODO proper radius scaling, other shapes
 * @author baco
 *
 */
public class AgentMediator implements CommandMediator {
	protected AgentContainer agents;
	private String pigment;
	private float[] rgba;
	
	/**
	 * assign agent container via the constructor
	 * @param agents
	 */
	public AgentMediator(AgentContainer agents)
	{
		this.agents = agents;
	}

	/**
	 * call without tilt and zoom options
	 */
	@Override
	public void draw(GLAutoDrawable drawable) {
		draw(drawable, 0.0f, 0.0f);
	}

	/**
	 * draw the the relevant objects in 3d
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void draw(GLAutoDrawable drawable, float zoom, float tilt) {
		final GL2 gl = drawable.getGL().getGL2();

		/* get the domain lengths to draw itself and scaling */
		double[] domainLengths = agents.getShape().getDimensionLengths();
		
		/* draw the domain square */
		gl.glLoadIdentity();
		gl.glTranslated(-domainLengths[0]*1.5,-domainLengths[0]*1.5, zoom);
		rgba = new float[] {0.2f, 0.2f, 0.2f};
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.1f);
		gl.glRotatef(tilt,1.0f,0f,0f);            // Rotate The Cube On X, Y & Z
		gl.glBegin(GL2.GL_QUADS);                  // Start Drawing The Cube
		gl.glColor3f(rgba[0],rgba[1],rgba[2]);    
			gl.glVertex3d(0.0, domainLengths[1]*3, -5.0*domainLengths[0]);              // Top Left
		    gl.glVertex3d( domainLengths[0]*3, domainLengths[1]*3,  -5.0*domainLengths[0]);              // Top Right
		    gl.glVertex3d( domainLengths[0]*3,0.0,  -5.0*domainLengths[0]);              // Bottom Right
		    gl.glVertex3d(0.0,0.0,  -5.0*domainLengths[0]);              // Bottom Left
		gl.glEnd();
		
		/* get the surfaces from the agents */
			for ( Agent a : this.agents.getAllLocatedAgents() )
				for ( Surface s : (List<Surface>) (a.isAspect(
						NameRef.surfaceList) ? a.get(NameRef.surfaceList) :
						new LinkedList<Surface>()))
				{
				pigment = a.getString("pigment");
				if(s instanceof Ball)
				{
					Ball ball = (Ball) s;
				 int i, j;
				 final int lats = 32;
			       final int longs = 32;
		       for(i = 0; i <= lats; i++) {
		       	double lat0 = Math.PI * (-0.5 + (double) (i - 1) / lats);
		       	double z0  = Math.sin(lat0);
		       	double zr0 =  Math.cos(lat0);
		
		          double lat1 = Math.PI * (-0.5 + (double) i / lats);
		          double z1 = Math.sin(lat1);
		          double zr1 = Math.cos(lat1);
		
		          gl.glLoadIdentity();
		          double[] p = ball._point.getPosition();
		          /** currently hard coded domain, scaling, no radius scaling */
		          gl.glTranslated(3.0* p[0] -domainLengths[0]*1.5, 3.0* p[1] -domainLengths[1]*1.5, -5.0*domainLengths[0] + (p.length == 3 ? p[2] : 0)
		        		  + zoom);
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
			        
			        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
			        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
			        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.1f);
		          gl.glColor3f(rgba[0],rgba[1],rgba[2]);
		          gl.glBegin(gl.GL_QUAD_STRIP);
		          for(j = 0; j <= longs; j++) {
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
			
		}
		
	}

}
