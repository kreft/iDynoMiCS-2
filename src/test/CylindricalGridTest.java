package test;

import grid.CylindricalGrid;
import grid.SpatialGrid.ArrayType;

public class CylindricalGridTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CylindricalGrid grid=new CylindricalGrid(new int[]{10,359,1},1);
		grid.newArray(ArrayType.CONCN);
		System.out.println(grid.arrayAsText(ArrayType.CONCN));
		System.out.println();
		grid.toCartesianGrid();
	}

}
