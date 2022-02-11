package analysis.quantitative;

import linearAlgebra.Vector;

public class DistanceVector {

    int[] vector;

    public DistanceVector(int[] vector)
    {
        this.vector = Vector.copy(vector);
    }

    public void set(int[] vector) {
        this.vector = Vector.copy(vector);
    }

    public int[] get() {
        return this.vector;
    }
}
