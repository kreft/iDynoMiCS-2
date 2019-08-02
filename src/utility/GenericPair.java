package utility;

/**
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 * @param <E>
 * @param <S>
 */
public class GenericPair<E,S> 
{

    private final E first;
    private final S second;

    public GenericPair(E e, S s) 
    {
    	first = e;
    	second = s;
    }

    public E getFirst() 
    {
        return first;
    }

    public S getSecond() 
    {
        return second;
    }
}
