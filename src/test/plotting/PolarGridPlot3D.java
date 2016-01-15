package test.plotting;

import java.awt.Color;
import java.util.Arrays;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;

import grid.PolarGrid;
import grid.SphericalGrid;
import linearAlgebra.Vector;
import test.PolarGridTest;

public class PolarGridPlot3D {
	BranchGroup group, polyGroup;
	PolarGrid grid;
	SimpleUniverse universe;
	final Color3f red=new Color3f(1f, 0f, 0f), 
			blue=new Color3f(0f, 0f, 1f), 
			green=new Color3f(0f, 1f, 0f);
	
	public PolarGridPlot3D(PolarGrid grid, boolean centre, boolean print_grid){
		universe = new SimpleUniverse();
		group = new BranchGroup();
		polyGroup = new BranchGroup();
		this.grid=grid;
		
		OrbitBehavior B = new OrbitBehavior();
		B.setReverseZoom(true);
		B.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0) );
		universe.getViewingPlatform().setViewPlatformBehavior(B);
		Viewer viewer = universe.getViewer();
		View view = viewer.getView();
		view.setFrontClipDistance(0);
		view.setBackClipDistance(3000);
			
		int[] current;
		double[] p;
		for ( current = grid.resetIterator(); grid.isIteratorValid();
				current = grid.iteratorNext())
		{
//			System.out.println(Arrays.toString(current));
			if (centre) p = grid.getVoxelCentre(Vector.copy(current));
			else p = grid.getVoxelOrigin(Vector.copy(current));
//			System.out.println(Arrays.toString(p));
//			System.out.println();
			
			Sphere sphere = new Sphere(0.05f);
			sphere.setAppearance(makeShinyAppearance(green));
			TransformGroup tg = toCartesian(p);
			tg.addChild(sphere);
			group.addChild(tg);
			
			QuadArray qa = getGridCell(Vector.copy(current));
			GeometryInfo gi = new GeometryInfo(qa);
	        NormalGenerator ng = new NormalGenerator();
	        ng.generateNormals(gi);
	        GeometryArray result = gi.getGeometryArray();

			Shape3D poly = new Shape3D(
					result, makeNoCullVecAppearance(red));
			polyGroup.addChild(poly);
		}
//		addLights(polyGroup);
		universe.getViewingPlatform().setNominalViewingTransform();
		universe.addBranchGraph(group);
		if (print_grid) universe.addBranchGraph(polyGroup);
	}
	
	public void runIterator(){
//    	int[] current;
    	long t;
        for ( grid.resetIterator(); grid.isIteratorValid();
        		grid.iteratorNext())
        {
        	t=System.currentTimeMillis();
//        	int[] state = linearAlgebra.Vector.copy(current);
        	setColorAll(false);
//        	grid.setCurrent(state);
        	try {
        		Thread.sleep(Math.max(0, 500-System.currentTimeMillis()+t));
        	} catch (InterruptedException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        	setColorAll(true);
        }
    }
	
	public void startIterator(){
//		int[] current;
        for ( grid.resetIterator(); grid.isIteratorValid();
        		grid.iteratorNext())
        {
//        	int[] state = linearAlgebra.Vector.copy(current);
        	setColorAll(false);
//        	grid.setCurrent(state);
        	universe.getViewer().getView().repaint();
        	System.out.println("press enter to step iterator");
        	PolarGridTest.keyboard.nextLine();
        	setColorAll(true);
        	universe.getViewer().getView().repaint();
        }
    }
	
	private static Appearance makeShinyAppearance(Color3f color){
		Appearance ap = new Appearance();
		Material m = new Material();
		m.setCapability(Material.ALLOW_COMPONENT_READ);
		m.setCapability(Material.ALLOW_COMPONENT_WRITE);
		m.setEmissiveColor(color); 
		m.setShininess(128);
		ap.setMaterial(m);
    	return ap;
	}
	
	private static Appearance makeNoCullVecAppearance(Color3f color){
		Appearance ap = new Appearance();
		PolygonAttributes myPA = new PolygonAttributes( );
		myPA.setCullFace( PolygonAttributes.CULL_NONE );
		myPA.setBackFaceNormalFlip(true);
		myPA.setPolygonMode( PolygonAttributes.POLYGON_LINE);
		myPA.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
		myPA.setCapability(PolygonAttributes.ALLOW_MODE_READ);
		ap.setPolygonAttributes(myPA);
		Material m = new Material();
		m.setCapability(Material.ALLOW_COMPONENT_READ);
		m.setCapability(Material.ALLOW_COMPONENT_WRITE);
		m.setEmissiveColor(color); 
		m.setShininess(128);
		ap.setMaterial(m);
    	return ap;
	}
	
	private void setColorAll(boolean reset){
		setColor(grid.coord2idx(grid.iteratorCurrent())-1,reset,true);
    	for (int[] nbh = grid.resetNbhIterator(); 
				grid.isNbhIteratorValid(); nbh=grid.nbhIteratorNext() )
		{
//    		System.out.println(grid.nbhIteratorIsOutside());
    		if (grid.nbhIteratorIsOutside()==null){
    			int idx=grid.coord2idx(nbh)-1;
    			if (idx>=0 && idx<grid.length()){  // ignore boundaries for the moment
    				setColor(idx,reset,false);
    				//        			System.out.println(Arrays.toString(grid.iteratorCurrent())+"  "
    				//        					+Arrays.toString(nbh_i)+"  "+idx);
    				universe.getViewer().getView().repaint();
    			}
    		}
		}
        
	}
	
	private void setColor(int idx, boolean reset, boolean isCurrent){
		int b = 10;  // pause to get changes updated (else strange errors happen) 
		Sphere c = (Sphere)((TransformGroup)group.getChild(idx)).getChild(0);
    	Material cm = c.getAppearance().getMaterial();
    	Shape3D p = (Shape3D)polyGroup.getChild(idx);
    	PolygonAttributes pa = p.getAppearance().getPolygonAttributes();
    	Material pm = p.getAppearance().getMaterial();
		if (reset){
			pa.setPolygonMode( PolygonAttributes.POLYGON_LINE);
			sleepUnsave(b);
			cm.setEmissiveColor(green);
			sleepUnsave(b);
			pm.setEmissiveColor(red);
			sleepUnsave(b);
		}
		else{
			pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
			sleepUnsave(b);
			cm.setEmissiveColor(isCurrent ? red : blue);
			sleepUnsave(b);
			pm.setEmissiveColor(isCurrent ? red : blue);
			sleepUnsave(b);
		}
	}
	
	private void addLights(BranchGroup b) {
		//Create a bounding sphere to act as the active bounds
		//of the lights
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
				100.0);
		//Create the colours and directions
		Color3f lightColour = new Color3f(1.0f, 1.0f, 1.0f);
		Vector3f lightDir = new Vector3f(-1.0f, -1.0f, -1.0f);
		Color3f ambientColour = new Color3f(0.2f, 0.2f, 0.2f);
		//Create the lights
		AmbientLight ambientLight = new AmbientLight(ambientColour);
		ambientLight.setInfluencingBounds(bounds);
		DirectionalLight directionalLight = new DirectionalLight(lightColour,
				lightDir);
		directionalLight.setInfluencingBounds(bounds);
		//Add the lights to the branch
		b.addChild(ambientLight);
		b.addChild(directionalLight);
	}	

	private TransformGroup toCartesian(double[] p){
		TransformGroup tg = new TransformGroup();
		Transform3D transform = new Transform3D();
		
		Vector3f vector;
		if (grid instanceof SphericalGrid){
			vector = new Vector3f(
					(float)(p[0]*Math.cos(p[1])*Math.sin(p[2])), 
					(float)(p[0]*Math.sin(p[1])*Math.sin(p[2])),
					(float)(p[0]*Math.cos(p[2])));
		}
		else vector = new Vector3f((float)(p[0]*Math.sin(p[1])), 
				(float)(p[0]*Math.cos(p[1])), (float)(p[2]));
//		System.out.println(Arrays.toString(p)+"  "+vector);
		transform.setTranslation(vector);
		tg.setTransform(transform);
		return tg;
	}
	
	private double[] getCartLoc(int[] coord, double[] in){
		in = grid.getLocation(Vector.copy(coord), in);
		double[] p;
		if (grid instanceof SphericalGrid){
			p = new double[]{
					(float)(in[0]*Math.cos(in[1])*Math.sin(in[2])), 
					(float)(in[0]*Math.sin(in[1])*Math.sin(in[2])),
					(float)(in[0]*Math.cos(in[2]))};
		}
		else p = new double[]{(float)(in[0]*Math.sin(in[1])), 
				(float)(in[0]*Math.cos(in[1])), (float)(in[2])};
		return p;
	}
	
	private QuadArray getGridCell(int[] coord){
		QuadArray qa = new QuadArray(24,QuadArray.COORDINATES | QuadArray.COLOR_4);

//		Vector3f norm = new Vector3f(0f,0f,1f);
		double[] in = new double[]{0,0,0};
		
		double[] p1 = getCartLoc(coord,in); in[1]++;			 // [0 0 0]
		double[] p2 = getCartLoc(coord,in); in[2]++;			 // [0 1 0]
		double[] p3 = getCartLoc(coord,in); in[1]--;			 // [0 1 1]
		double[] p4 = getCartLoc(coord,in); in[0]++; in[1]++; 	 // [0 0 1]
		double[] p5 = getCartLoc(coord,in); in[1]--;			 // [1 1 1]
		double[] p6 = getCartLoc(coord,in); in[2]--;			 // [1 0 1]
		double[] p7 = getCartLoc(coord,in); in[1]++;			 // [1 0 0]
		double[] p8 = getCartLoc(coord,in); 					 // [1 1 0]
		
//		System.out.println(Arrays.toString(p1));
		
		// r==0
		qa.setCoordinates(0,p1);
		qa.setCoordinates(1,p2);
		qa.setCoordinates(2,p3);
		qa.setCoordinates(3,p4);
		
		// r==1
		qa.setCoordinates(4,p5);
		qa.setCoordinates(5,p6);
		qa.setCoordinates(6,p7);
		qa.setCoordinates(7,p8);
		// p==0
		qa.setCoordinates(8,p1);
		qa.setCoordinates(9,p2);
		qa.setCoordinates(10,p8);
		qa.setCoordinates(11,p7);
		// p==1
		qa.setCoordinates(12,p3);
		qa.setCoordinates(13,p4);
		qa.setCoordinates(14,p6);
		qa.setCoordinates(15,p5);
		// t==0
		qa.setCoordinates(16,p1);
		qa.setCoordinates(17,p4);
		qa.setCoordinates(18,p6);
		qa.setCoordinates(19,p7);
		//t==1
		qa.setCoordinates(20,p2);
		qa.setCoordinates(21,p3);
		qa.setCoordinates(22,p5);
		qa.setCoordinates(23,p8);
				
		Color4f c = new Color4f(new Color(255, 0, 0, 50));
		for (int i=0; i<24; ++i){
			qa.setColor(i, c);
		}
		
		return qa;
	}
	
	private void sleepUnsave(int millis){
		try {
			Thread.sleep(millis); 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
