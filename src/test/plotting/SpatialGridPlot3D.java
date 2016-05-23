package test.plotting;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.HashMap;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.View;
import javax.swing.JFrame;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;

import grid.CylindricalGrid;
import grid.PolarGrid;
import grid.SpatialGrid;
import grid.SpatialGrid.ArrayType;
import grid.SphericalGrid;
import linearAlgebra.Vector;
import test.PolarGridTest;

/**
 * @author Stefan Lang, Friedrich-Schiller University Jena 
 * (stefan.lang@uni-jena.de)
 *
 */
public class SpatialGridPlot3D{
	/* flags for mutuable voxel properties */
	public final int POLYMODE = 1 << 0, COLOR = 1 << 1, TRANSPARENCY = 1 << 2; 
	/* used to define the domain property functions should operate on */
	public enum VoxelTarget{ NBH, ALL}
	/* used to define the branch graph property functions should operate on */
	public enum Branch {Voxels, InVoxelPoints}
	
	private JFrame frame;
	Transform3D camera_pos;	
	/* the branch graph map for all objects on the frame */
	HashMap<SpatialGrid,HashMap<ArrayType,GridGraph>> grids;
	SimpleUniverse universe;
	/* visualization bounds (max 1000 meters atm)*/
	Bounds bounds;
	final Color3f RED=new Color3f(1f, 0f, 0f), 
			BLUE=new Color3f(0f, 0f, 1f), 
			GREEN=new Color3f(0f, 1f, 0f),
			GRAY=new Color3f(0.2f, 0.2f, 0.2f);
	
	public SpatialGridPlot3D(){
		Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
		frame = new JFrame("Spatial grid plot");
		frame.setSize(screen_size);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Canvas3D canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
        canvas.setDoubleBufferEnable(true);
        frame.getContentPane().add(canvas, BorderLayout.CENTER);
        
        universe = new SimpleUniverse(canvas);

		bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 1000.0);
		OrbitBehavior B = new OrbitBehavior();
		B.setReverseZoom(true);
		B.setSchedulingBounds(bounds);
		universe.getViewingPlatform().setViewPlatformBehavior(B);
		View view = universe.getViewer().getView();
		view.setBackClipDistance(1000);
		/* wow, took a time to figure out that this not default.. */
		view.setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);
		
		grids = new HashMap<SpatialGrid,HashMap<ArrayType,GridGraph>>();
		
		frame.setVisible(true);
	}
	
	/** 
	 * \brief Changes the world position of this grid. 
	 * 
	 * Adds the spatial grid if it is not already in the branch graph.
	 * 
	 * @param grid
	 * @param type
	 * @param pos
	 */
	public void setWorldPosition(SpatialGrid grid, ArrayType type, double[] pos)
	{
		if (pos == null)
			pos = Vector.zerosDbl(3);
		getGraph(grid, type).loc = pos;
	}

	/**
	 * \brief Used to focus all objects in first dimension (where the
	 * totalLength is the max radius for PolarGrids)
	 * 
	 * Adds the spatial grid if it is not already in the branch graph.
	 */
	public void autoSetCamera(){
			camera_pos = new Transform3D();
			if (grids.keySet().isEmpty()) 
				return;
			double max_length = Double.MIN_VALUE, min_start = Double.MAX_VALUE;
			for ( SpatialGrid grid : grids.keySet()){
				GridGraph graph = getGraph(grid, ArrayType.CONCN);
	
				double length = grid.getTotalLength(0);
				double start = grid instanceof PolarGrid ? 
						graph.loc[0] - length : graph.loc[0];
						length = grid instanceof PolarGrid ? 
								grid.getTotalLength(0) : grid.getTotalLength(0);
	
								min_start = Math.min(start, min_start);
								max_length = Math.max(length, max_length);
			}
	//		System.out.println(((min + max) / 2)+"  "+(2 * (max - min)));
			camera_pos.set(new Vector3d((min_start + max_length) / 2, 0, 
													2 * (max_length - min_start)));
			universe.getViewingPlatform()
						.getViewPlatformTransform().setTransform(camera_pos);
		}

	/**
	 * \brief This will create a visualization of the grid's iterator.
	 * 
	 * Steps can be increased manually or automatic, depending on user-input.
	 *
	 * Adds the spatial grid if it is not already in the branch graph.
	 *  
	 * @param grid
	 * @param type
	 */
	public void startIterator(SpatialGrid grid, ArrayType type){
		boolean auto_step = false;
		long t, min_pause = 500;
		/* get or add the grid graph */
		GridGraph graph = getGraph(grid, type);
		
		/* red opaque voxels for current */ 
		VoxelProperties cur_prop = new VoxelProperties();
		cur_prop.target = TRANSPARENCY | COLOR | POLYMODE;
		cur_prop.color = RED;
		cur_prop.alpha = 1;
		cur_prop.fillOrVec = true;
		/* blue opaque voxels for nbhs */
		VoxelProperties nbh_prop = new VoxelProperties();
		nbh_prop.target = TRANSPARENCY | COLOR | POLYMODE;
		nbh_prop.color = BLUE;
		nbh_prop.alpha = 1;
		nbh_prop.fillOrVec = true;
		/* gray transparent voxels for others */
		VoxelProperties reset_prop = new VoxelProperties();
		reset_prop.target = TRANSPARENCY | COLOR | POLYMODE;

		View view = universe.getCanvas().getView();
		int[] cur;
        for ( cur = grid.resetIterator(); grid.isIteratorValid(); 
        		cur = grid.iteratorNext())
        {
        	
        	t=System.currentTimeMillis();
        	/* set current */
        	setPerVoxelProperty(cur_prop, graph, cur);
        	/* set nbhs */
        	setCurrentNbhsProperty(nbh_prop, grid, graph);
        	/* force displaying after setting up all voxels */
        	view.startView(); 
        	auto: if (auto_step)	
        		sleepUnsave(t, min_pause);
        	else {
        		System.out.println("press enter to step iterator manually or type"
        				+ " any number in speed per second to step automatically"
        				+ ", \ni.e. type '2' for 2 steps per second");
        		String line = PolarGridTest.keyboard.nextLine();
        		double step_per_sec = 2;
        		try {
        			step_per_sec = Double.valueOf(line);
        		}catch (NumberFormatException e){break auto;}
        		System.out.println("entering automatic step mode with " 
        								+step_per_sec + " steps per second");
        		min_pause = (long) (1000L / step_per_sec);
        		auto_step = true;
        	}
        	/* do not display resetting to make visualization smoother for high
        	 * steps per second */
        	view.stopView();
        	setCurrentNbhsProperty(reset_prop, grid, graph);
        	setPerVoxelProperty(reset_prop, graph, cur);
        }
    }
	
	/** 
	 * \brief Plots the current concentrations (i.e. ArrayType CONCN) of the 
	 * given spatial grid. 
	 *  
	 * Polygon mode should be set to 'fill' (true) before calling this.
	 * 
	 * Adds the spatial grid if it is not already in the branch graph.
	 * 
	 * @param grid
	 */
	public void plotCurrentConcentrations(SpatialGrid grid){
		int [] current;
		GridGraph graph = getGraph(grid, ArrayType.CONCN);
		VoxelProperties prop = new VoxelProperties();
		prop.target = TRANSPARENCY;
		View view = universe.getCanvas().getView();
		view.stopView();
        for ( current=grid.resetIterator(); grid.isIteratorValid();
        		current=grid.iteratorNext())
        {
        	float alpha = (float)grid.getValueAtCurrent(ArrayType.CONCN);
        	prop.alpha = Math.min(Math.max(0, alpha), 1);
        	setPerVoxelProperty(prop, graph, current);
        }
        view.startView();
	}
	
	
	/** 
	 * \brief Used to set voxel properties of either all voxels or the 
	 * current neighborhood.
	 * 
	 * @param prop The voxel properties structure.
	 * @param targets The domain to be operated on (enum VoxelTarget).
	 * @param grid 
	 * @param type
	 */
	public void setProperty(VoxelProperties prop, VoxelTarget targets, SpatialGrid grid, 
							ArrayType type)
	{
		GridGraph graph = getGraph(grid, type);
		switch (targets){
		case NBH: 
			setCurrentNbhsProperty(prop, grid, graph);
			break;
		case ALL:
			setVoxelAllProperty(prop, grid, graph);
		}
	}
	
	/**
	 * @param targets The domain to be operated on (enum VoxelTarget).
	 * @param grid 
	 * @param type
	 * @param fillOrVec Boolean defining filled (true) or vector-based (false)
	 * 					polygon mode. 
	 */
	public void setPolygonMode(VoxelTarget targets, SpatialGrid grid, 
							ArrayType type, boolean fillOrVec)
	{
		VoxelProperties prop = new VoxelProperties();
		prop.fillOrVec = fillOrVec;
		setProperty(prop, targets, grid, type);
	}
	
	/**
	 * @param targets The domain to be operated on (enum VoxelTarget).
	 * @param grid
	 * @param type
	 * @param alpha The alpha value in the range 0 (transparent) and 1 (opaque)
	 */
	public void setTransparency(VoxelTarget targets, SpatialGrid grid, 
			ArrayType type, float alpha)
	{
		VoxelProperties prop = new VoxelProperties();
		prop.target = TRANSPARENCY;
		prop.alpha = alpha;
		setProperty(prop, targets, grid, type);
	}
	
	/**
	 * @param branch The branch graph (enum SpatialGridPlot3D.Branch).
	 * @param targets The domain to be operated on (enum VoxelTarget).
	 * @param grid
	 * @param type
	 * @param color 
	 */
	public void setColor(Branch branch, VoxelTarget targets, SpatialGrid grid, 
							ArrayType type, Color3f color){
		switch (branch){
		case Voxels:
			VoxelProperties prop = new VoxelProperties();
			prop.target = COLOR;
			prop.color = color;
			setProperty(prop, targets, grid, type);
			break;
		case InVoxelPoints: 
			setPointsColor(grid, type, color);
		}
	}
			
	/**
	 * \brief Adds points inside every voxel on the grid 
	 * (i.e. for voxel centers set in_voxel_loc = new double[]{0.5, 0.5, 0.5})
	 * 
	 * @param grid
	 * @param type
	 * @param in_voxel_location A relative location inside a voxel (range [0 1]).
	 */
	public void addPoints(SpatialGrid grid, ArrayType type, 
													double[] in_voxel_location){
		GridGraph graph = getGraph(grid, type);
		BranchGroup branch = graph.getLocationsBranch();
		/* only one inner location per array coord -> remove possible old ones*/
		branch.removeAllChildren();
		int[] p;
		for ( p=grid.resetIterator(); grid.isIteratorValid(); 
														p = grid.iteratorNext())
		{
			BranchGroup group = new BranchGroup();
			TransformGroup tg = new TransformGroup();
			Transform3D transform = new Transform3D();
			
			double[] loc = getCartLoc(grid, p, in_voxel_location);
			Vector3f vector = new Vector3f(
								(float) loc[0], (float) loc[1], (float) loc[2]);
			transform.setTranslation(vector);
			tg.setTransform(transform);
			Sphere sphere = new Sphere(0.05f);
			sphere.setAppearance(makeMutableColorAppearance());
			tg.addChild(sphere);
			group.addChild(tg);
			branch.addChild(group);
		}
	}
	
	/**
	 * Removes all in-voxel locations (for example center points).
	 * 
	 * @param grid
	 * @param type
	 */
	public void removePoints(SpatialGrid grid, ArrayType type){
		getGraph(grid, type).getLocationsBranch().removeAllChildren();
	}
	
	/**
	 * \brief Used to define property-targets and their respective arguments.</br>
	 * 
	 * Targets can be ORed, i.e. target = COLOR | POLYMODE will set the color and 
	 * polygon mode to the values defined by the fields color and fillOrVec, while
	 * ignoring the field alpha.</br> 
	 * 
	 * </br>
	 * Targets can be: </br>
	 *  POLYMODE (fillOrVec)
	 *   			true: filled polygons, false: lines connecting edges. </br>
	 *  COLOR (color)</br>
	 *  TRANSPARENCY (alpha) 	</br> 
	 *  </br>
	 * 
	 * Default values:</br>
	 * target = -1 (none) </br>
	 * fillOrVec = false</br>
	 * color = GRAY </br>
	 * alpha = 1 (opaque)
	 */
	private class VoxelProperties{
		int target = -1; 
		boolean fillOrVec = false; 
		Color3f color = GRAY; 
		float alpha = 1;  
	}
	
	/**
	 * \brief used to change several properties defined in prop for a single voxel.
	 * 
	 * @param prop A VoxelProperties structure.
	 * @param graph The voxel branch graph of a spatial grid.
	 * @param coord
	 */
	private void setPerVoxelProperty(VoxelProperties prop, GridGraph graph, int[] coord){
		int idx = graph.getIdx(coord);			

		Shape3D shape = (Shape3D) graph.getVoxelsBranch().getChild(idx);
		Appearance ap = shape.getAppearance();
		if ((prop.target & POLYMODE) == POLYMODE)
			ap.getPolygonAttributes().setPolygonMode(prop.fillOrVec ? 
					PolygonAttributes.POLYGON_FILL : PolygonAttributes.POLYGON_LINE);
		if ((prop.target & COLOR) == COLOR)
			ap.getMaterial().setAmbientColor(prop.color);
		if ((prop.target & TRANSPARENCY) == TRANSPARENCY)
			ap.getTransparencyAttributes().setTransparency(1 - prop.alpha);
	}

	/**
	 * \brief Used to change several properties defined in prop for 
	 *			the current neighborhood.
	 * 
	 * @param prop
	 * @param grid
	 * @param graph
	 */
	private void setCurrentNbhsProperty(VoxelProperties prop, SpatialGrid grid, GridGraph graph){
		int[] nbh;
		for ( nbh=grid.resetNbhIterator(); grid.isNbhIteratorValid();
				nbh=grid.nbhIteratorNext()){
			if (grid.nbhIteratorIsOutside() == null){
				setPerVoxelProperty(prop, graph, nbh);
			}else ;
			//TODO: what to do here?
		}
	}
	
	/**	
	 * \brief Used to change several properties defined in prop for 
	 *			all voxels on the grid.
	 *
	 * @param prop
	 * @param grid
	 * @param graph
	 */
	private void setVoxelAllProperty(VoxelProperties prop, SpatialGrid grid, GridGraph graph){
		for ( int cur[]=grid.resetIterator(); grid.isIteratorValid();
				cur=grid.iteratorNext())
			setPerVoxelProperty(prop, graph, cur);
	}
	
	/**
	 * 
	 * Used to set the color of all points on the grid (i.e. center points) 
	 * 
	 * @param grid
	 * @param type
	 * @param color
	 */
	private void setPointsColor(SpatialGrid grid, ArrayType type, Color3f color)
	{
		GridGraph graph = getGraph(grid, type);
		BranchGroup branch = graph.getLocationsBranch();
		int[] p;
		for ( p=grid.resetIterator(); grid.isIteratorValid(); 
														p = grid.iteratorNext())
		{
			int idx = graph.getIdx(p);
			TransformGroup tg = (TransformGroup) branch.getChild(idx);
			Shape3D point = (Shape3D) tg.getChild(0);
			Material m = point.getAppearance().getMaterial();
			m.setAmbientColor(color);
		}
	}
	
	/** Adds the spatial grid if it is not already in the branch graph.
	 * @param grid
	 * @param type
	 * @return
	 */
	private GridGraph getGraph(SpatialGrid grid, ArrayType type)
	{
		if (!grids.containsKey(grid)) 
			grids.put(grid, new HashMap<ArrayType, GridGraph>());
		
		if (!grids.get(grid).containsKey(type)) 
			grids.get(grid).put(type, new GridGraph(grid, type, null));
			
		return grids.get(grid).get(type);
	}

	/**
	 * Converts the coordinate and the relative location inside this coordinate 
	 * into a location, and transforms the results to a Cartesian coordinate 
	 * system if necessary (evaluated by the grid type).
	 * 
	 * @param grid
	 * @param coords
	 * @param inside
	 * @return
	 */
	private double[] getCartLoc(SpatialGrid grid, int[] coords, double[] inside)
	{
		double[] loc = grid.getLocation(coords, inside);
		if (grid instanceof SphericalGrid)
			Vector.unspherifyEquals(loc);
		if (grid instanceof CylindricalGrid)
			Vector.uncylindrifyEquals(loc);
		return loc;
	}
	
	/**
	 * Creates an appearance where only the color can be overridden.
	 * 
	 * @return
	 */
	private static Appearance makeMutableColorAppearance(){
		Appearance ap = new Appearance();
		Material m = new Material();
		m.setCapability(Material.ALLOW_COMPONENT_WRITE);
		ap.setMaterial(m);
    	return ap;
	}
	
	/** Creates an appearance where the polygon mode, the color, and the 
	 * transparency attributes can be overridden.
	 * @return
	 */
	private static Appearance makeMutableVecAppearance(){
		Appearance ap = new Appearance();
		PolygonAttributes pa = new PolygonAttributes(
				PolygonAttributes.POLYGON_LINE,
				PolygonAttributes.CULL_NONE,
				0f, true, 0f);
		pa.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
		pa.setCapabilityIsFrequent(PolygonAttributes.ALLOW_MODE_WRITE);
		ap.setPolygonAttributes(pa);
		Material m = new Material();
		m.setCapability(Material.ALLOW_COMPONENT_WRITE);
		m.setCapabilityIsFrequent(Material.ALLOW_COMPONENT_WRITE);
		ap.setMaterial(m);
		TransparencyAttributes ta = new TransparencyAttributes(
				TransparencyAttributes.NICEST, 0f);
		ta.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
		ta.setCapabilityIsFrequent(TransparencyAttributes.ALLOW_VALUE_WRITE);
		ap.setTransparencyAttributes(ta);
    	return ap;
	}
	
	/**
	 * Creates a 3D shape for a voxel. Polar voxels are approximated with 
	 * Cartesian cuboids.
	 * 
	 * @param grid
	 * @param coord
	 * @return
	 */
	private Shape3D createGridCellShape(SpatialGrid grid, int[] coord)
	{
		QuadArray qa = new QuadArray(24,QuadArray.COORDINATES);

//		Vector3f norm = new Vector3f(0f,0f,1f);
		double[] in = new double[]{0,0,0};
		
		double[] p1 = getCartLoc(grid, coord, in); in[1]++;			 // [0 0 0]
		double[] p2 = getCartLoc(grid, coord, in); in[2]++;			 // [0 1 0]
		double[] p3 = getCartLoc(grid, coord, in); in[1]--;			 // [0 1 1]
		double[] p4 = getCartLoc(grid, coord, in); in[0]++; in[1]++; // [0 0 1]
		double[] p5 = getCartLoc(grid, coord, in); in[1]--;			 // [1 1 1]
		double[] p6 = getCartLoc(grid, coord, in); in[2]--;			 // [1 0 1]
		double[] p7 = getCartLoc(grid, coord, in); in[1]++;			 // [1 0 0]
		double[] p8 = getCartLoc(grid, coord, in); 					 // [1 1 0]
		
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
		
		GeometryInfo gi = new GeometryInfo(qa);
		NormalGenerator ng = new NormalGenerator();
		ng.generateNormals(gi);
		GeometryArray ga = gi.getGeometryArray();

		Shape3D shape = new Shape3D(ga, makeMutableVecAppearance());
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		shape.setCapabilityIsFrequent(Shape3D.ALLOW_APPEARANCE_WRITE);
		return shape;
	}
	
	/**
	 * Forces the current Thread to sleep minimum 'min_millis' milliseconds.
	 * 
	 * Throws an exception if interrupted.
	 * 
	 * @param start
	 * @param min_millis
	 */
	private static void sleepUnsave(long start, long min_millis){
		try 
    	{
    		Thread.sleep(Math.max(0, min_millis-System.currentTimeMillis()+start));
    	} catch (InterruptedException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
	}
	
	private AmbientLight brightAmbientLight(){
		AmbientLight light = new AmbientLight(new Color3f(1f,1f,1f));
		light.setInfluencingBounds(bounds);
		return light;
	}
	
	/**
	 * Stores all information necessary to visualize a SpatialGrid.
	 *
	 */
	private class GridGraph{	
		/* the different branches, voxels and points inside that voxels so far */
		BranchGroup branches;
		/* the global world position of the grid */
		double[] loc;
		/* to map 3D-coordinates to indices */
		private HashMap<String,Integer> coord2idx; 
		
		GridGraph(SpatialGrid grid, ArrayType type, double[] global_pos) {
			coord2idx = new HashMap<String,Integer>();
			
			/* create transform for world position */
			if (global_pos == null) 
				global_pos = Vector.zerosDbl(3);
			loc = global_pos;
			
			Vector3f vector = new Vector3f(
					(float) loc[0], (float) loc[1], (float) loc[2]);
			
			TransformGroup world_pos = new TransformGroup();
			Transform3D trans = new Transform3D();
			trans.setTranslation(vector);
			world_pos.setTransform(trans);
			
			/* create voxel polygon BranchGraph */
			BranchGroup voxels = new BranchGroup();
			for ( int idx = 0, cur[] = grid.resetIterator(); 
					grid.isIteratorValid(); cur = grid.iteratorNext(), idx++){
				/* use string to have working hashCode() implementation */
				coord2idx.put(Arrays.toString(cur), idx);
				voxels.addChild(createGridCellShape(grid, cur));
			}
			
			/* reset, to don't disturb calling function */
			grid.resetIterator();
			/* add voxels to the branch group */
			world_pos.addChild(voxels);
			
			/* create (empty) branch graph for in-voxel-points 
			 * (i.e. origin or center) */
			BranchGroup points = new BranchGroup();
			points.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
			points.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
			world_pos.addChild(points);
			
			/* transform everything to right world location */
			branches = new BranchGroup();
			branches.addChild(world_pos);
			branches.addChild(brightAmbientLight());
			
			/* register in universe */
			universe.addBranchGraph(branches);
		}
		
		BranchGroup getVoxelsBranch(){ 
			TransformGroup tg = (TransformGroup) branches.getChild(0);
			return (BranchGroup) tg.getChild(0);
		}
		
		BranchGroup getLocationsBranch(){ 
			TransformGroup tg = (TransformGroup) branches.getChild(0);
			return (BranchGroup) tg.getChild(1);
		}
		
		int getIdx(int[] coord){ 
			return coord2idx.get(Arrays.toString(coord));
		}
	}
}
