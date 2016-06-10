package glRender;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

/**
 * TODO clean-up commenting
 * 
 * TODO culling?
 * 
 * openGL Render class, manages openGL settings, output frame and it's own
 * key bindings, requires a command mediator to draw up the 3D scene
 * 
 * based on:
 * http://nehe.gamedev.net/tutorial/creating_an_opengl_window_win32/13001/
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Render implements GLEventListener, Runnable {
	
	/**
	 * screen device
	 */
	private static GraphicsEnvironment _graphicsEnvironment;
	
	/*
	 * full screen toggle
	 */
	private static boolean _isFullScreen = false;
	
	/*
	 * display modes
	 */
	public static DisplayMode _dm, _dm_old;
	
	/*
	 * Display dimensions
	 */
	private static Dimension _displayDimensions;
	
	/*
	 * display position
	 */
	private static Point _point = new  Point(0,0);

	/*
	 * frame icon
	 */
	private final static String ICON_PATH = "icons/iDynoMiCS_logo_icon.png";
	
	/*
	 * glu library
	 */
	private GLU _glu = new GLU();
	
	/*
	 * screen and lighting properties
	 */
	private boolean _light;
	private boolean _blend;
	private float _aspectRatio;
	
	/*
	 * Camera positioning
	 */
	private float _tilt = 0.005f, _zoom = 0.0f, _angle = 0.5f*(float) Math.PI;
	private float _x = 0f, _y = 0f /* , z = 0f */;

	
	/* 
	 * Light sources 
	 */
    private float[] lightPosition = {-40.0f, -40.0f, 80.0f, 1f};
    private float[] lightAmbient = {0.25f, 0.25f, 0.25f, 1f};
    private float[] LightDiffuse = {0.5f, 0.5f, 0.5f, 1f};
    
    /*
     * mediator
     */
    private CommandMediator _commandMediator;

    /*
     * this is what refreshes what is rendered on the screen
     */
	@Override
	public void display(GLAutoDrawable drawable) {
		
		/*
		 * the open GL2 drawable
		 */
		final GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
			
		/*
		 * start new identity
		 */
		gl.glLoadIdentity();

		/*
		 * switch lighting and alpha blending
		 */
		if(_light)
			gl.glEnable(GL2.GL_LIGHTING);
		else
			gl.glDisable(GL2.GL_LIGHTING);
		if(_blend)
		{
			gl.glEnable(GL2.GL_BLEND);
			gl.glDisable(GL2.GL_DEPTH_TEST);
		}
		else
		{
			gl.glEnable(GL2.GL_DEPTH_TEST);
			gl.glDisable(GL2.GL_BLEND);
		}
		
		/*
		 * ask commandMediator to draw what it draws, tilt and zoom only work
		 * if implemented by commandMediator
		 */
		this._commandMediator.draw(drawable);
		
		/*
		 * adjust the camera settings to the size of the drawable and the user
		 * defined camera setting adjustments (zoom, tilt, x, y)
		 */
		double dist = _commandMediator.kickback() - _zoom;
		double hDist = Math.sin(_tilt+0.0001) * dist;
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		
		/* camera perspective */
		_glu.gluPerspective( 45.0f, _aspectRatio, 
				1.0, _commandMediator.kickback() + 50.0 );
		
		/* 
		 * camera position, direction and rotation 
		 */
		_glu.gluLookAt( 
				_x + hDist * Math.cos(_angle), 		// eyeX
				_y + hDist * Math.sin(_angle), 		// eyeY
				Math.cos(_tilt) * dist, 			// eyeZ
				_x, 								// centerX
				_y, 								// centerY
				0, 									// centerZ
				Math.cos(_angle), 					// upX
				Math.sin(_angle),					// upY
				dist * 10000 * ( Math.sin(_tilt) )	// upZ
				);	
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		
		gl.glFlush();
		
		/*
		 * this is recursive!
		 */
	}

	/**
	 * currently unused interface method
	 */
	@Override
	public void dispose(GLAutoDrawable arg0) {
	
	}

	/*
	 * Initiate the open GL environment, set shader model, lighting, smoothing
	 * etc
	 */
	@Override
	public void init(GLAutoDrawable drawable) {
		final GL2 gl = drawable.getGL().getGL2();
		
		/* vertical sync */
		gl.setSwapInterval(1);
		
		/* shading and rendering settings */
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.5f, 0.5f, 0.5f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		
		/* light */
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, this.lightAmbient, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, this.LightDiffuse, 0);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, this.lightPosition, 0);
		gl.glEnable(GL2.GL_LIGHT1);
		gl.glEnable(GL2.GL_LIGHTING);
		
		this._light = true;
		
		/* alpha blend */
		gl.glColor4f(1f, 1f, 1f, 0.5f); // 50% alpha
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE);	
		
		_commandMediator.init(drawable);
	}

	/**
	 * act upon reshaping of the render window
	 */
	@Override
	public void reshape(GLAutoDrawable drawable , int x, int y, int width, 
			int height) {
		final GL2 gl = drawable.getGL().getGL2();
		
		/* determine the aspect ratio */
		if(height <= 0 )
			height = 1;
		_aspectRatio = (float) width / (float) height;
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		
		/* adjust the camera perspective to the screen resizing */
		_glu.gluPerspective(45.0f, _aspectRatio, 1.0, 500.0);
		_glu.gluLookAt(0, 0, 0, 0, 0, -80, 0, 1, 0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	/**
	 * Create a new Render object associated with it's mediator
	 * @param mediator
	 */
	public Render(CommandMediator mediator) {
		this._commandMediator = mediator;
	}

	/**
	 * Render is runnable to prevent the gui to become unresponsive
	 */
	@Override
	public void run() {
		/* openGL profile */
		final GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile);
		
		/* Canvas */
		final GLCanvas glcanvas = new GLCanvas(capabilities);
		Render r = new Render(_commandMediator);
		glcanvas.addGLEventListener(r);
		
		/*
		 * dimensions of the initial window
		 */
		Dimension myDim = new Dimension();
		myDim.setSize(500, 500);
		glcanvas.setSize(myDim);
		
		/*
		 * set the animator and its Frames per second
		 */
		final FPSAnimator animator = new FPSAnimator(glcanvas, 15, true);
		
		/* window name */
		final JFrame frame = new JFrame ("Live render");
		
		/* add the canvas to the JFrame and set the window close action to stop
		 * rendering
		 */
		frame.getContentPane().add(glcanvas);
		frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e) {
				if(animator.isStarted())
					animator.stop();
			}
		});
		
		/* size the window */
		frame.setSize(frame.getContentPane().getPreferredSize());
		
		/* detect and set graphics dephices */
		_graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = _graphicsEnvironment.getScreenDevices();
		_dm = devices[0].getDisplayMode();

		/* set the frame's initial position and make it visable */
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		/* add aditional 0 by 0 JPanel with key bindings */
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(0,0));
		frame.add(p, BorderLayout.SOUTH);
		keyBindings(p, frame, r);
		
		/* set the icon */
		ImageIcon img = new ImageIcon(ICON_PATH);
		frame.setIconImage(img.getImage());
		
		/* start the animator */
		animator.start();
	}

	/**
	 *  switch between fullScreen and windowed 
	 */
	protected static void fullScreen(JFrame f) {
		/*
		 * switch to full screen
		 */
		if(!_isFullScreen)
		{
			/* settings for full screen */
			f.dispose();
			f.setUndecorated(true);
			f.setVisible(true);
			f.setResizable(false);
			
			/* store current window position and size */
			_displayDimensions = f.getSize();
			_point = f.getLocation();
			
			/* switch to full screen */
			f.setLocation(0, 0);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			f.setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
			
			_isFullScreen = true;
		}
		/*
		 * switch to windowed
		 */
		else
		{
			/* settings for windowed */
			f.dispose();
			f.setUndecorated(false);
			f.setResizable(true);
			
			/* restore old window position and dimensions */
			f.setLocation(_point);
			f.setSize(_displayDimensions);
			f.setVisible(true);
			
			_isFullScreen = false;	
		}
	}
	
	/*
	 * The key bindings available for the JFrame
	 */
	private static void keyBindings(JPanel p, JFrame frame, Render r) 
	{
		/*
		 * store available actions and keys 
		 */
		ActionMap actionMap = p.getActionMap();
		InputMap inputMap = p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		/* 
		 * full screen 
		 */
		actionMap.put("fullscreen", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;

			/* toggle */
			@Override
			public void actionPerformed(ActionEvent a) {
				System.out.println("fullscreen");
				fullScreen(frame);
			}
		});
		
		/* ALT ENTER full screen */
		inputMap.put(KeyStroke.getKeyStroke(
				KeyEvent.VK_ENTER, ActionEvent.ALT_MASK), "fullscreen");
		
		/* F11 full screen */
		inputMap.put(KeyStroke.getKeyStroke(
				KeyEvent.VK_F11, 0), "fullscreen");

		/* escape full screen */
		inputMap.put(KeyStroke.getKeyStroke(
				KeyEvent.VK_ESCAPE, 0), "escapeFullscreen");
		actionMap.put("escapeFullscreen", new AbstractAction(){
			private static final long serialVersionUID = 370697371995950359L;
			
			/* always go to windowed */
			@Override
			public void actionPerformed(ActionEvent a) {
				System.out.println("escapeFullscreen");
				_isFullScreen = true;
				fullScreen(frame);
			}
		});

		
		/* up */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "UP");
		actionMap.put("UP", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			@Override
			public void actionPerformed(ActionEvent b) {
				System.out.println("up");
				r._x -= 1f;
			}
		});
		
		/* down */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "DOWN");
		actionMap.put("DOWN", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			@Override
			public void actionPerformed(ActionEvent c) {
				System.out.println("down");
				r._x += 1f;
			}
		});
		
		/* left */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "LEFT");
		actionMap.put("LEFT", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			
			@Override
			public void actionPerformed(ActionEvent d) {
				System.out.println("left");
				r._y -= 1f;
			}
		});
		
		/* right */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "RIGHT") ;
		actionMap.put("RIGHT", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("right");
				r._y += 1f;
			}
		});
		
		/* filter */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "filter") ;
		actionMap.put("filter", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			
			@Override
			public void actionPerformed(ActionEvent f) {
				System.out.println("filter");

			}
		});
		
		/* light */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), "lights") ;
		actionMap.put("lights", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			
			@Override
			public void actionPerformed(ActionEvent g) {
				System.out.println("lights");
				r._light = r._light ? false : true;

			}
		});
		
		/* alpha */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "blend") ;
		actionMap.put("blend", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			
			@Override
			public void actionPerformed(ActionEvent g) {
				System.out.println("blend");
				r._blend = r._blend ? false : true;
			}
		});
		
		/* tilt down */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "tiltdown") ;
		actionMap.put("tiltdown", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			
			@Override
			public void actionPerformed(ActionEvent g) {
				System.out.println("tiltdown");
					r._tilt -= 0.1f*(float) Math.PI;
			}
		});
		
		/* tilt up */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "tiltup") ;
		actionMap.put("tiltup", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			
			@Override
			public void actionPerformed(ActionEvent g) {
				System.out.println("tiltup");
					r._tilt += 0.1f*(float) Math.PI;
			}
		});
		
		/* zoom out */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0), "out") ;
		actionMap.put("out", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			
			@Override
			public void actionPerformed(ActionEvent g) {
				System.out.println("out");
				r._zoom -= 0.3f;
			}
		});
		
		/* zoom in */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "in") ;
		actionMap.put("in", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			
			@Override
			public void actionPerformed(ActionEvent g) {
				System.out.println("in");
				r._zoom += 0.3f;
			}
		});
		
		/* clockwise */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "clockwise") ;
		actionMap.put("clockwise", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			
			@Override
			public void actionPerformed(ActionEvent g) {
				System.out.println("clockwise");
				r._angle -= 0.1f*(float) Math.PI;
			}
		});
		
		/* counterclockwise */
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "counterclockwise") ;
		actionMap.put("counterclockwise", new AbstractAction(){
			private static final long serialVersionUID = 346448974654345823L;
			
			@Override
			public void actionPerformed(ActionEvent g) {
				System.out.println("counterclockwise");
				r._angle += 0.1f*(float) Math.PI;
			}
		});	
	}
}
