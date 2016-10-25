package glRender;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.math.Quaternion;
import com.jogamp.opengl.util.gl2.GLUT;

import agent.Agent;
import dataIO.Log;
import dataIO.Log.Tier;
import idynomics.AgentContainer;
import linearAlgebra.Vector;
import referenceLibrary.AspectRef;
import shape.CartesianShape;
import shape.CylindricalShape;
import shape.Dimension.DimName;
import shape.Shape;
import shape.ShapeIterator;
import shape.SphericalShape;
import surface.Ball;
import surface.Rod;
import surface.Surface;
import utility.Helper;


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
	private float _kickback;
	
	/*
	 * openGL profile
	 */
	private GL2 _gl;
	
	/**
	 * OpenGL Utility Toolkit
	 */
	private GLUT _glut;
	
	/**
	 * toggle grid view or domain view
	 */
	public boolean grid = false;
	
	/**
	 * OpenGL Utility Library
	 */
	private GLU _glu;
	
	/**
	 * Stores the length of the associated Shape in each dimension
	 */
	private double[] _domainMaxima;
	
	/**
	 * 
	 */
	public int definition = 8;
	/**
	 * Default slices / stacks to subdivide polar objects.
	 * TODO we really want to have this separate for small and large objects
	 */
	private int _slices = definition*2, _stacks = definition;
	
	
	private float[] _orthoX = new float[]{1,0,0}, _orthoY = new float[]{0,1,0},
					_orthoZ = new float[]{0,0,1}, _rotTemp = new float[16];

	/**
	 * used to set up the open gl camera
	 */
	@Override
	public float kickback() {
		return _kickback;
	}
	
	public double[] orientation() 
	{
		if (this._shape instanceof CartesianShape)
			return Vector.times( this._domainMaxima, 0.5);
		else
			return new double[] { 0.0, 0.0 };
	}
	
	/**
	 * assign agent container via the constructor
	 * @param agents
	 */
	public AgentMediator(AgentContainer agents)
	{
		this._agents = agents;
		this._shape = agents.getShape();
		this._domainMaxima = new double[3];
		/* determine kickback for camera positioning */
		_kickback = 0.0f;
		for (DimName dn : _shape.getDimensionNames()){
			float max = (float)_shape.getDimension(dn).getExtreme(1);
			_kickback  = (float) Math.max(_kickback, max);
			_domainMaxima[_shape.getDimensionIndex(dn)] = max;
		}
		if (this._shape instanceof CartesianShape)
			this._kickback = 1.5f * this._kickback;
		else
			this._kickback = 3.0f * this._kickback;		
	}
	
	/**
	 * Initializes new GLU and GLUT instances and associates the drawable's GL
	 * context with this CommandMediator.   
	 * This should be called at the end of the GLEventListener's initialization
	 * method (glRender.Render in our case). 
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
		
		_slices = definition*2;
		_stacks = definition;
		
		/* load identity matrix */
		_gl.glLoadIdentity();
		

		
		/*
		 * when we want to disable depth test we draw the domain here
		 */
//		draw(_shape);
        
		/* get the surfaces from the agents */
		for ( Agent a : this._agents.getAllLocatedAgents() )
		{

			/* cycle through the agent surfaces */
			for ( Surface s : (List<Surface>) (a.isAspect(
					AspectRef.surfaceList) ? a.get(AspectRef.surfaceList) :
					new LinkedList<Surface>()))
			{
				_pigment = a.getString("pigment");
				_pigment = Helper.setIfNone(_pigment, "WHITE");
				switch (_pigment)
				{
				case "GREEN" :
					  _rgba = new float[] {0.0f, 1.0f, 0.0f};
					  break;
				case "RED" :
					  _rgba = new float[] {1.0f, 0.0f, 0.0f};
					  break;
				case "BLUE" :
					  _rgba = new float[] {0.01f, 0.0f, 1.0f};
					  break;
				case "PURPLE" :
					  _rgba = new float[] {1.0f, 0.0f, 1.0f};
					  break;
				case "ORANGE" :
					  _rgba = new float[] {1.0f, 0.6f, 0.1f};
					  break;
				case "BLACK" :
					  _rgba = new float[] {0.0f, 0.0f, 0.0f};
					  break;
				case "WHITE" :
				default :
					  _rgba = new float[] {1.0f, 1.0f, 1.0f};
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
		/* 
     	 * Adjust positioning for the domain (prevent the scene from being
     	 * rendered in one corner). This applies to all agents and shapes.
     	 */
//		_gl.glTranslated(
//				 - _domainMaxima[0] * 0.5, 
//				 - _domainMaxima[1] * 0.5,
//				 - _domainMaxima[2] * 0.5);
		
		/*
		 * when we want to blend we draw the domain here
		 */
		draw(_shape);
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
		 /* first sphere */
		double[] posA = GLUtil.make3D(rod._points[0].getPosition());
		 /* second sphere*/
		double[] posB = GLUtil.make3D(rod._points[1].getPosition());
		
		posA = GLUtil.searchClosestCyclicShadowPoint(_shape, posA, posB);
		
		/* save the transformation matrix, so we do not disturb other drawings */
		_gl.glPushMatrix();

		applyCurrentColor();

		if ( Log.shouldWrite(level) )
		{
			Log.out(level, "Constructing Rod with radius " + rod._radius + " and " 
					+ _slices + " slices, " + _stacks + " stacks" );
		}
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
		
		/* set rotation to look from posA to posB */
		glRotated(dp);

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
	
	private void draw(Shape shape){
		/* save the current modelview matrix */
		_gl.glPushMatrix();
		double[] length = GLUtil.make3D(shape.getDimensionLengths());

		/* set different color / blending for 3 dimensional Cartesian shapes */
		if (length[2] > 0)
		{
			_rgba = new float[] {0.1f, 0.1f, 1.0f};
			_gl.glEnable(GL2.GL_BLEND);
		}
		else
		{
			_rgba = new float[] {0.3f, 0.3f, 0.3f};
		}
		/**
		 * NOTE moved this here since it seems to resolve black lines in domain 
		 * square, as long as the domain is drawn first this should not cause
		 * any problems.
		 */
//		_gl.glDisable(GL2.GL_DEPTH_TEST); 
		applyCurrentColor();
		
		_gl.glDisable(GL2.GL_LIGHTING);
		_gl.glColor3f(_rgba[0], _rgba[1], _rgba[2]);
		_gl.glEnable(GL2.GL_BLEND); 
		_gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		_gl.glColor4f(0.9f,0.9f,1.0f,0.1f);
		
		if (this.grid)
		{
			ShapeIterator it = _shape.getNewIterator();
			for (int[] cur = it.resetIterator(); it.isIteratorValid(); cur = it.iteratorNext())
			{			
				_gl.glPushMatrix();
				drawVoxel(_shape, cur);
				_gl.glPopMatrix();
			}
		}
		else
		{
			/* apply different functions for different types */
			if (shape instanceof CartesianShape){
				
				
				/* polar shapes are already centered around the origin after calling 
				 * getGlobalLocation() , so undo global translation */
				_gl.glTranslated(
						 _domainMaxima[0] * 0.5, 
						 _domainMaxima[1] * 0.5,
						 _domainMaxima[2] * 0.5);
				
				/* scale y and z relative to x (which we will choose as cube-size)*/
				_gl.glScaled(1, length[1] / length[0], length[2] / length[0]);
	
				/* draw the scaled cube (rectangle).
				 * Note that a cube with length 0 in one dimension is a plane 
				 */
				_glut.glutSolidCube((float)length[0]);
				
			}else if (shape instanceof CylindricalShape){
	
				/* draw the cylinder.
				 * Note that a cylinder with height 0 is a circle and only full 
				 * circles can be drawn at the moment. 
				 */
				_glut.glutSolidCylinder(length[0], length[2], _slices*8, 
						(int)Math.ceil(length[2])); 
				
			}else if (shape instanceof SphericalShape){
				
				/* draw the sphere.
				 * Note that only full spheres can be drawn at the moment. 
				 * NOTE we can allow the domain to have a bit better definition than the agents
				 */
				_glut.glutSolidSphere(length[0], _slices*8, _stacks*8);
				
			}
		}

		/* make sure Depth test is re-enabled and blend is disabled before
		 * drawing other objects.
		 */
		_gl.glEnable(GL2.GL_LIGHTING);
		_gl.glEnable(GL2.GL_DEPTH_TEST);
		_gl.glDisable(GL2.GL_BLEND);

		_gl.glPopMatrix();
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
	
	/**
	 * The default orientation of the rotated objects is assumed to look at the 
	 * positive Z axis. 
	 * 
	 * @param direc
	 */
	private void glRotated(double[] direc){
		/* NOTE: this assumes agents are rendered one after the other! */
		Quaternion quat = new Quaternion();
		/* this will create a quaternion, so that we rotate from looking
		 * along the Z-axis (which is the default orientation) */
		quat.setLookAt(Vector.toFloat(direc), _orthoZ, _orthoX, _orthoY, _orthoZ);
		/* transform the quaternion into a rotation matrix and apply it */
		//TODO: is there a way to make openGL use the quaternion directly?
		_gl.glMultMatrixf(quat.toMatrix(_rotTemp, 0), 0);
	}
	
	private void drawVoxel(Shape shape, int[] coord)
	{
//		Vector3f norm = new Vector3f(0f,0f,1f);
		double[] in = new double[]{0,0,0};
		
		double[] p1 = shape.getGlobalLocation(shape.getLocation(coord, in)); in[1]++;			 // [0 0 0]
		double[] p2 = shape.getGlobalLocation(shape.getLocation(coord, in)); in[2]++;			 // [0 1 0]
		double[] p3 = shape.getGlobalLocation(shape.getLocation(coord, in)); in[1]--;			 // [0 1 1]
		double[] p4 = shape.getGlobalLocation(shape.getLocation(coord, in)); in[0]++; in[1]++; 	 // [0 0 1]
		double[] p5 = shape.getGlobalLocation(shape.getLocation(coord, in)); in[1]--;			 // [1 1 1]
		double[] p6 = shape.getGlobalLocation(shape.getLocation(coord, in)); in[2]--;			 // [1 0 1]
		double[] p7 = shape.getGlobalLocation(shape.getLocation(coord, in)); in[1]++;			 // [1 0 0]
		double[] p8 = shape.getGlobalLocation(shape.getLocation(coord, in)); 					 // [1 1 0]
		
		_gl.glBegin(GL2.GL_QUADS);
		// r==0
		_gl.glVertex3fv(Vector.toFloat(p1), 0);
		_gl.glVertex3fv(Vector.toFloat(p2), 0);
		_gl.glVertex3fv(Vector.toFloat(p3), 0);
		_gl.glVertex3fv(Vector.toFloat(p4), 0);
		
		// r==1
		_gl.glVertex3fv(Vector.toFloat(p5), 0);
		_gl.glVertex3fv(Vector.toFloat(p6), 0);
		_gl.glVertex3fv(Vector.toFloat(p7), 0);
		_gl.glVertex3fv(Vector.toFloat(p8), 0);
		
		// p==0
		_gl.glVertex3fv(Vector.toFloat(p1), 0);
		_gl.glVertex3fv(Vector.toFloat(p2), 0);
		_gl.glVertex3fv(Vector.toFloat(p8), 0);
		_gl.glVertex3fv(Vector.toFloat(p7), 0);
		
		// p==1
		_gl.glVertex3fv(Vector.toFloat(p3), 0);
		_gl.glVertex3fv(Vector.toFloat(p4), 0);
		_gl.glVertex3fv(Vector.toFloat(p6), 0);
		_gl.glVertex3fv(Vector.toFloat(p5), 0);
		
		// t==0
		_gl.glVertex3fv(Vector.toFloat(p1), 0);
		_gl.glVertex3fv(Vector.toFloat(p4), 0);
		_gl.glVertex3fv(Vector.toFloat(p6), 0);
		_gl.glVertex3fv(Vector.toFloat(p7), 0);

		//t==1
		_gl.glVertex3fv(Vector.toFloat(p2), 0);
		_gl.glVertex3fv(Vector.toFloat(p3), 0);
		_gl.glVertex3fv(Vector.toFloat(p5), 0);
		_gl.glVertex3fv(Vector.toFloat(p8), 0);
		
		_gl.glEnd();

	}
}
