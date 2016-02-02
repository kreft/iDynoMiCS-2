package test.plotting;

import java.util.Arrays;
import java.util.HashMap;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.View;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;

import grid.PolarGrid;
import grid.SpatialGrid.ArrayType;
import grid.SphericalGrid;
import linearAlgebra.Vector;
import test.PolarGridTest;

public class PolarGridPlot3D {
	BranchGroup group, polyGroup;
	PolarGrid grid;
	SimpleUniverse universe;
	HashMap<Integer,Integer> coord2idx; 
	boolean print_grid_lines, plot_locations;
	
	final Color3f RED=new Color3f(1f, 0f, 0f), 
			BLUE=new Color3f(0f, 0f, 1f), 
			GREEN=new Color3f(0f, 1f, 0f);
	
	public PolarGridPlot3D(PolarGrid grid, int plot_location, boolean print_grid){
		print_grid_lines = print_grid;
		universe = new SimpleUniverse();
		group = plot_location > 0 ? new BranchGroup(): null;
		polyGroup = new BranchGroup();
		coord2idx = new HashMap<Integer,Integer>();
		this.grid=grid;
		Bounds bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0);
		OrbitBehavior B = new OrbitBehavior();
		B.setReverseZoom(true);
		B.setSchedulingBounds(bounds);
		universe.getViewingPlatform().setViewPlatformBehavior(B);
		Viewer viewer = universe.getViewer();
		View view = viewer.getView();
		view.setFrontClipDistance(0);
		view.setBackClipDistance(3000);
			
		int[] current;
		double[] p;
		int idx=0;
		for ( current = grid.resetIterator(); grid.isIteratorValid();
				current = grid.iteratorNext())
		{
			coord2idx.put(Arrays.hashCode(current), idx);
			if (plot_location>0){
				//			System.out.println(Arrays.toString(current));
				if (plot_location==2) 
					p = grid.getVoxelCentre(Vector.copy(current));
				else if (plot_location==1)
					p = grid.getVoxelOrigin(Vector.copy(current));
				else throw new IllegalArgumentException();
				//			System.out.println(Arrays.toString(p));
				//			System.out.println();

				Sphere sphere = new Sphere(0.05f);
				sphere.setAppearance(makeShinyAppearance(GREEN));
				TransformGroup tg = toCartesian(p);
				tg.addChild(sphere);
				group.addChild(tg);
			}
			QuadArray qa = getGridCell(Vector.copy(current),0);
			GeometryInfo gi = new GeometryInfo(qa);
			NormalGenerator ng = new NormalGenerator();
			ng.generateNormals(gi);
			GeometryArray result = gi.getGeometryArray();

			/*
			 * alpha = [0, 1] with 0 beeing opaque
			 * shininess = [1, 128] with 1 beeing not shiny at all 
			 */
			Shape3D poly = new Shape3D(
					result, makeNoCullVecAppearance(RED, print_grid));
			polyGroup.addChild(poly);
			idx++;
		}
//		addLights(polyGroup);
		AmbientLight ambientLight = new AmbientLight(new Color3f(1f,1f,1f));
		ambientLight.setInfluencingBounds(bounds);
		polyGroup.addChild(ambientLight);
		universe.getViewingPlatform().setNominalViewingTransform();
		if (plot_location>0) universe.addBranchGraph(group);
		universe.addBranchGraph(polyGroup);
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
        		Thread.sleep(Math.max(0, 1000-System.currentTimeMillis()+t));
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
	
	public void plotCurrentConcentrations(){
		int idx;
		int [] current;
//		double max_conc = grid.getMax(ArrayType.CONCN);
//		System.out.println(max_conc);
        for ( current=grid.resetIterator(); grid.isIteratorValid();
        		current=grid.iteratorNext())
        {
        	idx = coord2idx.get(Arrays.hashCode(current));
        	float alpha = (float)(
        			1-grid.getValueAtCurrent(ArrayType.CONCN));
        	System.out.println(grid.getValueAtCurrent(ArrayType.CONCN));
        	setTransparency(idx, alpha);
        }
	}
	
	private static Appearance makeShinyAppearance(Color3f color){
		Appearance ap = new Appearance();
		Material m = new Material();
		m.setCapability(Material.ALLOW_COMPONENT_READ);
		m.setCapability(Material.ALLOW_COMPONENT_WRITE);
		m.setAmbientColor(color); 
		ap.setMaterial(m);
    	return ap;
	}
	
	private static Appearance makeNoCullVecAppearance(
											Color3f color, boolean print_grid){
		Appearance ap = new Appearance();
		ap.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_READ);
		ap.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
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
		m.setAmbientColor(color);
		ap.setMaterial(m);
		TransparencyAttributes ta = new TransparencyAttributes(
				TransparencyAttributes.NICEST, 0f);
		ta.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
		ap.setTransparencyAttributes(ta);
    	return ap;
	}
	
	private void setColorAll(boolean reset){
		setColor(coord2idx.get(Arrays.hashCode(grid.iteratorCurrent())),
				reset,RED);
    	for (int[] nbh = grid.resetNbhIterator(); 
				grid.isNbhIteratorValid(); nbh=grid.nbhIteratorNext() )
		{
    		//    		System.out.println(grid.nbhIteratorIsOutside());
    		if (grid.nbhIteratorIsOutside()==null){
    			int idx=coord2idx.get(Arrays.hashCode(nbh));
    			setColor(idx,reset,BLUE);
    			//        			System.out.println(Arrays.toString(grid.iteratorCurrent())+"  "
    			//        					+Arrays.toString(nbh_i)+"  "+idx);
    			universe.getViewer().getView().repaint();
    		}
		}
	}
	
	private void setColor(int idx, boolean reset, Color3f color){
//		int b = 10;  // pause to get changes updated (else sometimes strange graphics) 
		Shape3D c, p;
		Material cm = null, pm;
		if (plot_locations){
			c = (Shape3D)((TransformGroup)group.getChild(idx)).getChild(0);
			cm = c.getAppearance().getMaterial();
		}
    	p = (Shape3D)polyGroup.getChild(idx);
    	PolygonAttributes pa = p.getAppearance().getPolygonAttributes();
    	pm = p.getAppearance().getMaterial();
		if (reset){
			pa.setPolygonMode( PolygonAttributes.POLYGON_LINE);
//			sleepUnsave(b);
			if (cm != null){
				cm.setAmbientColor(GREEN);
//				sleepUnsave(b);
			}
			pm.setAmbientColor(RED);
//			sleepUnsave(b);
			
		}
		else{
			pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
//			sleepUnsave(b);
			if (cm != null){
				cm.setAmbientColor(color);
//				sleepUnsave(b);
			}
			pm.setAmbientColor(color);
			// has to be set again
			p.getAppearance().setPolygonAttributes(pa);
//			sleepUnsave(b);
		}
	}
	
	private void setTransparency(int idx, float alpha){
//		int b = 10;  // pause to get changes updated (else sometimes strange graphics) 
		Shape3D p = (Shape3D)polyGroup.getChild(idx);
		Appearance ap = p.getAppearance();
		PolygonAttributes pa = ap.getPolygonAttributes();
		pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
		ap.getTransparencyAttributes().setTransparency(alpha);
	}

	private TransformGroup toCartesian(double[] p){
		TransformGroup tg = new TransformGroup();
		Transform3D transform = new Transform3D();
		
		Vector3f vector;
		if (grid instanceof SphericalGrid){
			vector = new Vector3f(
					(float)(p[0]*Math.cos(p[2])*Math.sin(p[1])), 
					(float)(p[0]*Math.sin(p[2])*Math.sin(p[1])),
					(float)(p[0]*Math.cos(p[1])));
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
					(float)(in[0]*Math.cos(in[2])*Math.sin(in[1])), 
					(float)(in[0]*Math.sin(in[2])*Math.sin(in[1])),
					(float)(in[0]*Math.cos(in[1]))};
		}
		else p = new double[]{(float)(in[0]*Math.sin(in[1])), 
				(float)(in[0]*Math.cos(in[1])), (float)(in[2])};
		return p;
	}
	
	private QuadArray getGridCell(int[] coord, int alpha){
		QuadArray qa = new QuadArray(24,QuadArray.COORDINATES);

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
