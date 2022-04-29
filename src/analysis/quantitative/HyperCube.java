package analysis.quantitative;

import java.util.Iterator;

/**
 * General class to store objects in a matrix with more than 3 dimensions.
 *
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <base>
 */
public class HyperCube<base> implements Iterable<base> {

    private Lattice root;
    private int[] size;

    @Override
    public Iterator<base> iterator() {
        return new CubeIterator(this);
    }

    private class CubeIterator<base> implements Iterator<base> {

        private int[] position;
        private HyperCube<base> _cube;

        public CubeIterator(HyperCube<base> cube) {
            this._cube = cube;
            this.position = new int[cube._dims];
            position[0] = -1;
        }

        @Override
        public boolean hasNext() {
            for (int i = 0; i < position.length; i++) {
                if (_cube.size[i]-1 > position[i])
                    return true;
            }
            return false;
        }

        @Override
        public base next() {
            for (int i = 0; i < position.length; i++) {
                if (_cube.size[i]-1 > position[i]) {
                    position[i]++;
                    return this._cube.get(position);
                } else {
                    position[i] = 0;
                }
            }
            return null;
        }
    }

    private class Lattice<base> {

        /* notice next == null means leaf */
        private Lattice[] next;
        private base entry;

        public Lattice()
        {

        }
        public Lattice(base entry )
        {
            this.entry = entry;
        }
    }

    private int _dims;

    public HyperCube( base value, int ... dims )
    {
        this.size = dims;
        this._dims = dims.length;
        root = new Lattice<base>();
        settice(0, root, value, dims);
    }

    /** used to initialize the hypercube */
    private void settice(int active, Lattice lat, base value, int ... dims) {
        lat.next = new Lattice[dims[active]];
        for( int i = 0; i < lat.next.length; i++ ) {
            lat.next[i] = new Lattice<base>();
            if( active < dims.length-1) {
                settice(active+1, lat.next[i], value, dims);
            } else {
                setAll(value, lat.next[i]);
            }
        }
    }

    public void setAll( base value )
    {
        setAll( value, root);
    }

    public void setAll( base value, int ... index )
    {
        Lattice<base> lat = sellectice(index);
        setAll(value, lat);
    }

    private void setAll( base value, Lattice lat )
    {
        if( lat.next == null )
            lat.entry = value;
        else {
            for( Lattice l : lat.next )
                setAll( value, l );
        }
    }

    public void set( base value, int ... index )
    {
        sellectice(index).entry = value;
    }

    public base get( int ... index )
    {
        return sellectice(index).entry;
    }

    /* select specified lattice */
    private Lattice<base> sellectice(int ... index ) {
        /* invalid amount of coordinates */
        if( index.length > _dims )
            return null;

        /* go down until we arrive at destination */
        Lattice<base> active = root;
        for (int i = 0; i < _dims; i++) {
            active = active.next[index[i]];
            if(i == index.length-1)
                return active;
        }

        /* did not arrive at leaf? */
        return null;
    }

    public void print()
    {
        int i = 0;
        for( base b : this )
        {
            System.out.print( b.toString() + " " );
            i++;
            if( i % size[0] == 0 )
                System.out.print("\n");
            if( size.length > 1 && i % (size[0] * size[1]) == 0 )
                System.out.print("\n");
        }
    }
}
