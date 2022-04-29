package test.junit.newTests;

import analysis.quantitative.HyperCube;
import analysis.quantitative.IntegerHyperCube;
import debugTools.Testable;
import org.junit.Test;

public class HyperCubeTest  implements Testable {

    @Test
    public void test()
    {
        test(TestMode.UNIT);
    }

    @Override
    public void test(TestMode mode) {
        HyperCube<Integer> cube = new HyperCube<Integer>( 0 , 3, 3, 3, 3);
        System.out.println( cube.get(0,1,2,2) );

        IntegerHyperCube intCube = new IntegerHyperCube( 1 , 3, 3, 2, 2);
        System.out.println( intCube.max() + "\n" );

        intCube.set(5,0,0,1,0);

        intCube.print();
    }
}
