package test.plotting;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Material;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.Viewer;

import grid.PolarGrid;
import grid.SphericalGrid;

public class PolarGridPlot3D {
	BranchGroup group;
	PolarGrid grid;
	final Color3f red=new Color3f(1f, 0f, 0f), blue=new Color3f(0f, 0f, 1f), green=new Color3f(0f, 1f, 0f);
	
	public PolarGridPlot3D(PolarGrid grid, boolean centre){
		SimpleUniverse universe = new SimpleUniverse();
		group = new BranchGroup();
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
			if (centre) p = grid.getVoxelCentre(current);
			else p = grid.getVoxelOrigin(current);
//			System.out.println(Arrays.toString(p));
//			System.out.println();
			
			Sphere sphere = new Sphere(0.05f);
			sphere.setAppearance(makeShinyAppearance(green));
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
			transform.setTranslation(vector);
			tg.setTransform(transform);
			tg.addChild(sphere);
			group.addChild(tg);
		}	
		
		
//		BoundingSphere bounds =
//				new BoundingSphere(new Point3d(0.0,0.0,0.0), Double.POSITIVE_INFINITY);
//		AmbientLight light	= new AmbientLight(green);
//		light.setInfluencingBounds(bounds);
//		group.addChild(light);
		
		universe.getViewingPlatform().setNominalViewingTransform();
		universe.addBranchGraph(group);
	}
	
	public void start(){
    	int[] current;
    	long t;
        for ( current = grid.resetIterator(); grid.isIteratorValid();
        		current = grid.iteratorNext())
        {
        	t=System.currentTimeMillis();
        	int[] state = linearAlgebra.Vector.copy(current);
        	setColor(false);
        	grid.setCurrent(state);
        	try {
        		Thread.sleep(Math.max(0, 500-System.currentTimeMillis()+t));
        	} catch (InterruptedException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	}
        	setColor(true);
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
	
	private void setColor(boolean reset){
		Sphere c = (Sphere)((TransformGroup)group.getChild(grid.iteratorCurrentIdx()-1)).getChild(0);
    	Material m = c.getAppearance().getMaterial();
		if (reset) m.setEmissiveColor(green);
		else m.setEmissiveColor(red);
    	
    	int[] nbh;
    	for ( nbh = grid.resetNbhIterator(); 
				grid.isNbhIteratorValid(); nbh = grid.nbhIteratorNext() )
		{
//    		System.out.println(Arrays.toString(nbh)+"  "+(grid.coord2idx(nbh)-1));
    		int idx=grid.coord2idx(nbh)-1;
    		if (idx>0 && idx<grid.length()){  // ignore boundaries for the moment
    			c = (Sphere)((TransformGroup)group.getChild(idx)).getChild(0);
    			m = c.getAppearance().getMaterial();
    			if (reset) m.setEmissiveColor(green);
    			else m.setEmissiveColor(blue);
    		}
		}
	}

//	public static void main( String[] args ) {
//		// TODO Auto-generated method stub
//		SphericalGrid grid = new SphericalGrid(new int[]{3,360,360},1);
////		CylindricalGrid grid = new CylindricalGrid(new int[]{10,360,20},1);
//		ArrayType type=ArrayType.CONCN;
//		grid.newArray(type, 0);
//		new PolarGridPlot3D(grid);
//	}
}
