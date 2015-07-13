package array;

public final class LinearAlgebra
{
	/*************************************************************************
	 * USEFUL ARRAYS
	 ************************************************************************/
	
	public static NDimDouble newVector(int length)
	{
		return new NDimDouble(new int[] {length}, 0.0);
	}
	
	public static NDimDouble newMatrix(int nRowCol)
	{
		return new NDimDouble(new int[] {nRowCol, nRowCol}, 0.0);
	}
	
	public static NDimDouble identityMatrix(int nRowCol)
	{
		NDimDouble out = new NDimDouble(new int[] {nRowCol, nRowCol}, 0.0);
		for ( int i = 0; i < nRowCol; i++ )
			out.setValueAt(new int[] {i, i}, 1.0);
		return out;
	}
	
	/*************************************************************************
	 * 
	 ************************************************************************/
	
	public static NDimDouble times(NDimDouble left, NDimDouble right)
	{
		if ( left.getNDim() != 2 || right.getNDim() > 2 )
			throw new IllegalArgumentException("Must be vectors or matrices");
		if ( left.getDims()[1] != right.getDims()[0] )
			throw new IllegalArgumentException("Inner dimensions must agree");
		int mL = left.getDims()[0];
		int nL = left.getDims()[1];
		int nR = (right.getNDim() > 1) ? right.getDims()[1] : 1;
		NDimDouble out = new NDimDouble(new int[] {mL, nR});
		Double temp;
		for ( int i = 0; i < mL; i++ )
			for ( int j = 0; j < nR; j++ )
			{
				temp = 0.0;
				for ( int k = 0; k < nL; k++ )
				{
					temp += left.getValueAt(new int[] {i, k}) * 
										right.getValueAt(new int[] {k, j});
				}
				out.setValueAt(new int[] {i, j}, temp);
			}
		return out;
	}
	
	
	
}