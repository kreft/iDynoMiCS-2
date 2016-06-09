package glRender;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.util.gl2.GLUT;

import agent.Agent;
import aspect.AspectRef;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import linearAlgebra.Vector;
import shape.CartesianShape;
import shape.CylindricalShape;
import shape.Shape;
import surface.Ball;
import surface.Rod;
import surface.Surface;


/**
 * Agent mediator, draws agents and an indication of their computational domain.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 * @author Stefan Lang (stefan.lang@uni-jena.de)
 *     Friedrich-Schiller University Jena, Germany 
 */
public class AgentMediator implements CommandMediator {
	
	/*
	 * agent container
	 */
	protected AgentContainer _agents;
	
	/*
	 * shape
	 */
	protected Shape _shape;
	
	/*
	 * pigment declaration in string format (eg: RED, BLUE etc)
	 */
	private String _pigment;
	
	/* 
	 * rgb color code
	 */
	private float[] _rgba;
	
	/*
	 * kickback, used to move camera back to see entire render scene
	 */
	public float _kickback;
	
	/*
	 * openGL profile
	 */
	private GL2 _gl;
	
	/**
	 * OpenGL Utility Toolkit
	 */
	private GLUT _glut;
	
	/**
	 * OpenGL Utility Library
	 */
	private GLU _glu;
	
	/**
	 * Stores the length of the associated Shape in each dimension
	 */
	private double[] _domainLength;
	
	/**
	 * Default slices / stacks to subdivide polar objects.
	 */
	private int _slices = 16, _stacks = 16;
	
	private float[] _orthoX = new float[]{1,0,0}, _orthoY = new float[]{0,1,0},
					_orthoZ = new float[]{0,0,1}, _rotTemp = new float[16];;

	/**
	 * used to set up the open gl camera
	 */
	@Override
	public float kickback() {
		return 2f * _kickback;
	}
	
	/**
	 * assign agent container via the constructor
	 * @param agents
	 */
	public AgentMediator(AgentContainer agents)
	{
		this._agents = agents;
		this._shape = agents.getShape();
		this._domainLength = GLUtil.make3D(_shape.getDimensionLengths());
		/* determine kickback for camera positioning */
		_kickback = (float) Math.max(_domainLength[0],
				Math.max(_domainLength[1], _domainLength[2]));
	}
	
	/**
	 * Initializes new GLU and GLUT instances and associates the drawable's GL
	 * context with this CommandMediator.   
	 * This should be called at the end of the GLEventListener's initialization
	 * method (glRender.Render in our case). 
	 * TODO: this assumes that the AgentContainer is always associated to the
	 * same Compartment / Shape, is this satisfied?
	 */
	public void init(GLAutoDrawable drawable){
		/* set openGL profile */
		_gl = drawable.getGL().getGL2();
		_glu = GLU.createGLU(_gl);
		_glut = new GLUT();
	}

	/**
	 * draw the the relevant objects in 3d
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void draw(GLAutoDrawable drawable) {
		
		/* load identity matrix */
		_gl.glLoadIdentity();
		
		/*
		 * draw the domain Shape
		 *
		 */
		if (_shape instanceof CartesianShape){
			draw((CartesianShape) _shape);
		}
		
		/* 
     	 * Adjust positioning for the domain (prevent the scene from being
     	 * rendered in one corner). This applies to all agents.
     	 */
		_gl.glTranslated(
				 - _domainLength[0] * 0.5, 
				 - _domainLength[1] * 0.5,
				 - _domainLength[2] * 0.5);
        
		/* get the surfaces from the agents */
		for ( Agent a : this._agents.getAllLocatedAgents() )
		{

			/* cycle through the agent surfaces */
			for ( Surface s : (List<Surface>) (a.isAspect(
					AspectRef.surfaceList) ? a.get(AspectRef.surfaceList) :
					new LinkedList<Surface>()))
			{
				_pigment = a.getString("pigment");
				switch (_pigment)
				{
				case "GREEN" :
					  _rgba = new float[] {0.0f, 1.0f, 0.0f};
					  break;
				case "RED" :
					  _rgba = new float[] {1f, 0.0f, 0.0f};
					  break;
				case "BLUE" :
					  _rgba = new float[] {0.01f, 0.0f, 1.0f};
					  break;
				case "PURPLE" :
					  _rgba = new float[] {1.0f, 0.0f, 1.0f};
					  break;
				case "ORANGE" :
					  _rgba = new float[] {1f, 0.6f, 0.1f};
					  break;
				case "BLACK" :
					  _rgba = new float[] {0.0f, 0.0f, 0.0f};
					  break;
				default :
					  _rgba = new float[] {1f, 1f, 1f};
					  break;
				}
				
				/*
				 * Render the appropriate surface
				 */
				if(s instanceof Ball)
				{
					draw((Ball) s);
				} 
				else if ( s instanceof Rod )
				{
					/*
					 * A rod can be drawn with two spheres and a cylinder
					 */
					draw((Rod) s);
				}
			}
		}	
	}
	
	private void draw(Ball ball){
		_gl.glPushMatrix();
		applyCurrentColor();
		double[] loc = GLUtil.make3D(ball._point.getPosition());
		_gl.glTranslated(loc[0], loc[1], loc[2]);
		GLUquadric qobj = _glu.gluNewQuadric();
		_glu.gluQuadricDrawStyle(qobj, GLU.GLU_FILL);
		_glu.gluQuadricNormals(qobj, GLU.GLU_SMOOTH);
		_glu.gluSphere(qobj, ball._radius, _slices, _stacks);
		_glu.gluDeleteQuadric(qobj);
		_gl.glPopMatrix();
	}
	
	private void draw(Rod rod) 
	{
		Tier level = Tier.BULK;
		double[] posA = GLUtil.make3D(rod._points[0].getPosition()); /* first sphere */
		double[] posB = GLUtil.make3D(rod._points[1].getPosition()); /* second sphere*/
		
		posA = GLUtil.searchClosestCyclicShadowPoint(_shape, posA, posB);
		
		/* save the transformation matrix, so we do not disturb other drawings */
		_gl.glPushMatrix();
     	
     	applyCurrentColor();

		Log.out(level, "Constructing Rod with radius " + rod._radius + " and " 
					+ _slices + " slices, " + _stacks + " stacks" );

		GLUquadric qobj = _glu.gluNewQuadric();

		/* draw first sphere */
		_gl.glTranslated(posA[0], posA[1], posA[2]);
		_glu.gluQuadricDrawStyle(qobj, GLU.GLU_FILL);
		_glu.gluQuadricNormals(qobj, GLU.GLU_SMOOTH);
		_glu.gluSphere(qobj, rod._radius, _slices, _stacks);
		
		/* direction from posB to posA */
		double[] dp = Vector.minus(posB, posA);
		double height = Vector.normEuclid(dp);
		
		/* draw a cylinder in between */
		/* save the matrix to rotate only the cylinder */
		_gl.glPushMatrix();
		
		/* NOTE: this assumes agents are rendered one after the other! */
		Quaternion quat = new Quaternion();
		/* this will create a quaternion, so that we rotate from looking
		 * along the Z-axis (which is the default orientation of a GLU-cylinder)
		 * to look from posA to posB */
		quat.setLookAt(Vector.toFloat(dp), _orthoZ, _orthoX, _orthoY, _orthoZ);
		/* transform the quaternion into a rotation matrix and apply it */
		//TODO: is there a way to make openGL use the quaternion directly?
		_gl.glMultMatrixf(quat.toMatrix(_rotTemp, 0), 0);

		/* create and draw the cylinder */
		_glu.gluQuadricDrawStyle(qobj, GLU.GLU_FILL);
		_glu.gluQuadricNormals(qobj, GLU.GLU_SMOOTH);
		_glu.gluCylinder(qobj,
				rod._radius, 		/* base */
				rod._radius, 		/* top */
				height, 			/* height */
				_slices, _stacks);
		/* restore matrix state before rotation (so we are at point A again)*/
		_gl.glPopMatrix();

		/* draw second sphere */
		_gl.glTranslated(dp[0], dp[1], dp[2]);
		_glu.gluQuadricDrawStyle(qobj, GLU.GLU_FILL);
		_glu.gluQuadricNormals(qobj, GLU.GLU_SMOOTH);
		_glu.gluSphere(qobj, rod._radius, _slices, _stacks);
		
		/* clean up */
		_glu.gluDeleteQuadric(qobj);
		_gl.glPopMatrix();
	}
	
	private void draw(CartesianShape shape){
		/* save the current modelview matrix */
		_gl.glPushMatrix();
		
		double[] length = GLUtil.make3D(shape.getDimensionLengths());

		/* set different color / blending for 3 dimensional Cartesian shapes */
		_rgba = new float[] {0.3f, 0.3f, 0.3f};
		if (length[2] > 0){
			_rgba = new float[] {0.1f, 0.1f, 1f};
			_gl.glEnable(GL2.GL_BLEND);
			_gl.glDisable(GL2.GL_DEPTH_TEST);
		}
		applyCurrentColor();
		
		/* scale y and z relative to x (which we will choose as cube-size) */
		_gl.glScaled(1, length[1] / length[0], length[2] / length[0]);
		
		/* draw the scaled cube (rectangle).
		 * Note that a cube with length 0 in one dimension is a plane */
		_glut.glutSolidCube((float)length[0]);
		
		
		/* clean up */
		if (length[2] > 0){
			_gl.glEnable(GL2.GL_DEPTH_TEST);
			_gl.glDisable(GL2.GL_BLEND);
		}
		_gl.glPopMatrix();
	}
	
	private void draw(CylindricalShape shape){
		
	}
	
	/**
	 * Sets the current ambient and specular color to <b>this._rgba</b> 
	 * with a shininess of 0.1.
	 */
	private void applyCurrentColor(){
     	/* lighting and coloring */
		_gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, _rgba, 0);
		_gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, _rgba, 0);
		_gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.1f);
		_gl.glColor3f(_rgba[0], _rgba[1], _rgba[2]);
	}
}
