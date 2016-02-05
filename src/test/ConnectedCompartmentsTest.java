package test;

import boundary.BoundaryConnected;
import boundary.BulkBLBoundary;
import idynomics.Compartment;

public class ConnectedCompartmentsTest
{
	public static void main(String[] args)
	{
		Compartment c1 = new Compartment("dimensionless");
		Compartment c2 = new Compartment("dimensionless");
		BoundaryConnected b1 = new BulkBLBoundary();
		BoundaryConnected b2 = new BulkBLBoundary();
		b1.setPartnerBoundary(b2);
		b2.setPartnerBoundary(b1);
		c1.addBoundary(null, 0, b1);
		c2.addBoundary(null, 0, b2);
	}
}