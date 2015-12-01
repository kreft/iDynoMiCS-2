package test.plotting;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Material;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;

import grid.CylindricalGrid;
import grid.SpatialGrid.ArrayType;
import grid.SphericalGrid;

public class PolarGridPlot3D {
	BranchGroup group;

	public PolarGridPlot3D(CylindricalGrid grid){
		SimpleUniverse universe = new SimpleUniverse();
		group = new BranchGroup();
		
		OrbitBehavior B = new OrbitBehavior();
		B.setReverseZoom(true);
		B.setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0) );
		universe.getViewingPlatform().setViewPlatformBehavior(B);
			
		Color3f light1Color = new Color3f(0f, 1f, 0f); // green light
		int[] current;
		double[] p;
		for ( current = grid.resetIterator(); grid.isIteratorValid();
				current = grid.iteratorNext())
		{
			p = grid.getVoxelOrigin(current);
			
			Appearance ap = new Appearance();
		
			Material m = new Material();
			m.setEmissiveColor(light1Color);
			m.setShininess(128);
			ap.setMaterial(m);
			
			Sphere sphere = new Sphere(0.05f);
			sphere.setAppearance(ap);
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
		
		
		BoundingSphere bounds =
				new BoundingSphere(new Point3d(0.0,0.0,0.0), Double.POSITIVE_INFINITY);
		Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
		AmbientLight light	= new AmbientLight(light1Color);
		light.setInfluencingBounds(bounds);

		group.addChild(light);
		
		universe.getViewingPlatform().setNominalViewingTransform();
		universe.addBranchGraph(group);
	}
	

	public static void main( String[] args ) {
		// TODO Auto-generated method stub
		SphericalGrid grid = new SphericalGrid(new int[]{3,360,360},1);
//		CylindricalGrid grid = new CylindricalGrid(new int[]{10,360,20},1);
		ArrayType type=ArrayType.CONCN;
		grid.newArray(type, 0);
		new PolarGridPlot3D(grid);
	}
}
