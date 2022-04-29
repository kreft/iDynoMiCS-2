package analysis.quantitative;

import linearAlgebra.Array;
import spatialRegistry.SpatialMap;

import java.util.Map;
import java.util.TreeMap;

public class NumericDictionary {

    int[] dims;
    TreeMap<Integer, int[][][]> dictionary = new TreeMap<Integer, int[][][]>();

    public NumericDictionary(int[] dims, int pages) {
        this.dims = dims;
        for (int i = 0; i < pages; i++) {
            if (dims.length == 2)
                dictionary.put(i, new int[dims[0]][dims[1]][1]);
            else
                dictionary.put(i, new int[dims[0]][dims[1]][dims[2]]);
        }
    }

    public int[][][] getPage(int page) {
        return dictionary.get(page);
    }

    public void setAll(Integer value) {
        for (Map.Entry<Integer, int[][][]> a : this.dictionary.entrySet())
            Array.setAll(a.getValue(), value);
    }


}