package utility;

public class GenericTrio<E,S,U>  {

    private final E first;
    private final S second;
    private final U third;

    public GenericTrio(E e, S s, U u) 
    {
    	first = e;
    	second = s;
    	third = u;
    }

    public E getFirst() 
    {
        return first;
    }

    public S getSecond() 
    {
        return second;
    }
    
    public U getThird() 
    {
        return third;
    }
	
}
