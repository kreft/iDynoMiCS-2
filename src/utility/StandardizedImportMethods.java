package utility;

import java.util.Collection;

import org.w3c.dom.Element;

import dataIO.Log;
import dataIO.XmlHandler;
import dataIO.Log.Tier;
import linearAlgebra.Vector;
import referenceLibrary.XmlRef;
import surface.Point;

public class StandardizedImportMethods {
	
	/**
	 * \brief obtain array of points
	 * TODO additional safety checks.
	 * @param xmlElem
	 * @param refObject
	 * @param requiredPoints
	 * @return
	 */
	public static Point[] pointImport(Element xmlElem, 
			Object refObject, int requiredPoints )
	{
		/* obtain all points */
		Point[] points = new Point[requiredPoints];
		Collection<Element> pointNodes =
		XmlHandler.getAllSubChild(xmlElem, XmlRef.point);
		if (pointNodes.size() != requiredPoints)
		{
			String exception = "Incorrect number of points for " + 
					refObject.getClass().getSimpleName() + ", expecting: " +
					requiredPoints;
				Log.out(Tier.CRITICAL, exception );
			throw new IllegalArgumentException(exception);
		}
		int i = 0;
		for (Element e : pointNodes) 
		{
			Element point = e;
			points[i++] = ( new Point( Vector.dblFromString(
					point.getAttribute( XmlRef.position ) )));
		}
		return points;
	}

}
