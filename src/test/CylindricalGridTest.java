package test;

import grid.CartesianGrid;
import grid.CylindricalGrid;
import grid.SpatialGrid.ArrayType;

public class CylindricalGridTest {

	public static void main(String[] args) {
		CylindricalGrid gridp = new CylindricalGrid(new int[]{5,359,1},1);
		ArrayType type=ArrayType.CONCN;
		gridp.newArray(type, 0);
		System.out.println(gridp.arrayAsText(type));
		CartesianGrid gridc=gridp.toCartesianGrid(type);
		System.out.println();
		System.out.println(gridc.arrayAsText(type));
	}

}
