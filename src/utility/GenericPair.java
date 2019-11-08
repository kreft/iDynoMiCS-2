package utility;

/**
 * Generic data class used to either pass or store a pair of associated objects.
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
