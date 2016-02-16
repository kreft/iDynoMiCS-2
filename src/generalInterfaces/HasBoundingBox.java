package generalInterfaces;

import surface.BoundingBox;

public interface HasBoundingBox {

	public BoundingBox boundingBox();
	public BoundingBox boundingBox(double margin);
}
