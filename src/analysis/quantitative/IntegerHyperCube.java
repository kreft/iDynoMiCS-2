package analysis.quantitative;

import java.util.Iterator;

public class IntegerHyperCube extends HyperCube<Integer>
{
    public IntegerHyperCube(Integer value, int... dims) {
        super(value, dims);
    }

    public Integer max()
    {
        Integer out = 0;
        for( Integer a : this )
            out = Math.max(a, out);
        return out;
    }
}
