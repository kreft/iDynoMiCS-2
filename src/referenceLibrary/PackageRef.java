package referenceLibrary;

public class PackageRef {

	
	/* ************************************************************************
	 * default packages
	 */
	
	public final static String eventPackage =
			new aspect.event.CoccoidDivision().getClass().getPackage().getName();
	
	public final static String calculatedPackage =
			new aspect.calculated.StateExpression().getClass().getPackage().getName();
	
	public final static String processManagerPackage =
			new processManager.library.GraphicalOutput().getClass().getPackage().getName();
	
}
