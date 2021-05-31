package solver.mgFas.boundaries;

import linearAlgebra.Vector;
import solver.mgFas.SoluteGrid;

/**
 * FIXME Should we include padding corners fx (-1, -1) ?
 */
public class CartesianPadding {

    int nI, nJ, nK;
    int[] extremes;

    public CartesianPadding(int nI, int nJ, int nK)
    {
        this.nI = nI;
        this.nJ = nJ;
        this.nK = nK;
        this.extremes = new int[] { nI, nJ, nK };
    }

    /**
     * Solid boundary
     * @param sol
     * @param dim
     * @param extreme
     */
    public void zeroFlux(SoluteGrid sol, int dim, boolean extreme)
    {
        int[] in = {0, 0, 0};
        int[] out = {0, 0, 0};
        if( extreme )
        {
            in[dim] = extremes[dim]-1;
            out[dim] = extremes[dim];
        }
        else
            out[dim] = -1;

        int[] step = new int[3];

        while( step.length > 1 )
        {
            sol.setValueAt( sol.getValueAt(
                    Vector.add( in, step ) ),
                    Vector.add( out, step ) );

            step = step(step, dim);
        }

//        int[] iterate = other(dim);
//        int[] inTemp = Vector.copy(in);
//        int[] ouTemp = Vector.copy(out);
//        for( int a = -1; a < extremes[iterate[0]]; a++)
//        {
//            for( int b = -1; (b < extremes[iterate[1]] || extremes[iterate[1]] == 0) ; b++)
//            {
//                sol.setValueAt(sol.getValueAt(inTemp), ouTemp);
//            }
//        }

    }

    /**
     * Cyclic boundary
     * @param sol
     * @param dim
     * @param extreme
     */
    public void cyclic(SoluteGrid sol, int dim, boolean extreme)
    {
        int[] in = {0, 0, 0};
        int[] out = {0, 0, 0};
        if( extreme )
        {
            in[dim] = extremes[dim]-1;
            out[dim] = -1;
        }
        else
        {
            out[dim] = extremes[dim];
        }

        int[] step = new int[3];

        while( step.length > 1 )
        {
            sol.setValueAt( sol.getValueAt(
                    Vector.add( in, step ) ),
                    Vector.add( out, step ) );

            step = step(step, dim);
        }
    }

    /**
     * Constant concentration and bulk boundary
     * @param sol
     * @param dim
     * @param extreme
     * @param value
     */
    public void constantConcentration(SoluteGrid sol, int dim, boolean extreme, double value)
    {
        int[] out = {0, 0, 0};
        if( extreme )
            out[dim] = extremes[dim];
        else
        out[dim] = -1;

        int[] step = new int[3];

        while( step.length > 1 )
        {
            int[] temp = Vector.add( out, step );
            sol.setValueAt( value, Vector.add( out, step ) );
            step = step(step, dim);
        }
    }

    /**
     * step trough the padding, start + step = current
     * @param step
     * @param dim
     * @return
     */
    public int[] step( int[] step, int dim )
    {
        int a = -1;
        int b = -1;
        int[] iterate = other(dim);

        boolean twoD = false;

        /* if 2D */
        if ( iterate[1] == 2 && extremes[iterate[1]] == 1 )
        {
            b = 0;
            twoD = true;
        }
        if ( step != null )
        {
            a = step[iterate[0]];
            b = step[iterate[1]];
        }

        if ( a < extremes[iterate[0]] )
        {
            if ( !twoD && b < extremes[iterate[1]])
            {
                step[iterate[1]]++;
            }
            else
            {
                step[iterate[0]]++;
                if( !twoD )
                {
                    step[iterate[1]] = 0;
                    b = -1;
                }
            }
            return step;
        }
        return new int[1];
    }

    /**
     * return the perpendicular dimensions
     * @param dim
     * @return
     */
    public int[] other(int dim)
    {
        if( dim == 0 )
            return new int[] { 1, 2 };
        if( dim == 1 )
            return new int[] { 0, 2 };
        else
            return new int[] { 0, 1 };
    }
}
