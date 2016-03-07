package glRender;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

/**
 * Test case command mediator that draws a new random sphere every second
 * @author baco
 *
 */
public class CommandMediatorExample implements CommandMediator
{
	protected double tic = 0.0;
	protected double x = 0, y = 0, z = 0;
	public float kickback;
	

	@Override
	public float kickback() {
		return kickback;
	}
   /**
    * Draws the object (sphere)
    * 
    * @param glDrawable The GLDrawable to draw objects on.
    */
    public void draw(GLAutoDrawable drawable, float zoom) {
    	if(tic == 0.0)
    		 tic = System.currentTimeMillis();
    	else if (System.currentTimeMillis() - tic > 1000)
    	{
    		x= (Math.random() -0.5) * 3;
    		y= (Math.random() -0.5) * 3;
    		z= (Math.random() -0.5) * 3;
    		tic = 0.0;
    	}
    	final GL2 gl = drawable.getGL().getGL2();
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
           gl.glTranslated(x,y, - 10.0 + z + zoom);
           gl.glRotatef(0.0f,1.0f,0.0f,0.0f); 
	        float[] rgba = {0.3f, 0.5f, 1f};
	        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
	        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
	        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.1f);
           gl.glColor3f(0.5f,0.5f,0.2f);
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

	public void draw(GLAutoDrawable drawable) {
		draw(drawable, 0f);
		
	}
}