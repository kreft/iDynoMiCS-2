package debugTools;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * SegmentTimer to make timing code sections a bit easier.
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class SegmentTimer {
	
	public static Map<String,SegmentTimer> timers = 
			new HashMap<String,SegmentTimer>();
	
	static int numberOfTimers = 0;
	
	private long _stamp;
	
	private long _netTime;
	
	private String _name = "timer";
	
	private boolean _verbose = false;
	
	/**
	 * create numbered timer
	 */
	public SegmentTimer()
	{
		SegmentTimer.numberOfTimers++;
	}
	
	/**
	 * create named timer
	 * @param name
	 */
	public SegmentTimer(String name)
	{
		this._name = name;
		SegmentTimer.numberOfTimers++;
	}
	
	/**
	 * add unnamed timer
	 * @return
	 */
	public static int add()
	{
		int temp = numberOfTimers;
		timers.put(String.valueOf( temp ), new SegmentTimer() );
		return temp;
	}
	
	/**
	 * add named timer
	 * @param name
	 * @return
	 */
	public static String add(String name)
	{
		timers.put(name , new SegmentTimer(name));
		return name;
	}
	
	/**
	 * add externally generated timer
	 * @param timer
	 * @return
	 */
	public static String add(SegmentTimer timer)
	{
		if (timer._name != "timer" && !timers.containsKey(timer._name))
		{
			timers.put(timer._name, timer);
			return timer._name;
		}
		else
		{
			timers.put(String.valueOf( numberOfTimers ), timer);
			return String.valueOf( timers.size() );
		}
	}
	
	/**
	 * get timer by index (only of unnamed)
	 * @param i
	 * @return
	 */
	public static SegmentTimer timer(int i)
	{
		return timers.get(String.valueOf(i));
	}
	
	/**
	 * get timer by name
	 * @param name
	 * @return
	 */
	public static SegmentTimer timer(String name)
	{
		return timers.get(name);
	}

	/**
	 * mark start time
	 */
	public void tick()
	{
		this._stamp = System.currentTimeMillis();
	}

	/**
	 * return duration (in millis)
	 * @return
	 */
	public long tock()
	{
		return (System.currentTimeMillis() - this._stamp);
	}
	
	/**
	 * mark start time
	 */
	public void start()
	{
		this.tick();
	}
	
	/**
	 * Start timer, create one if it did not already exist.
	 * @param name
	 */
	public static void start(String name)
	{
		if ( !timers.containsKey(name) )
		{
			add(name);
		}
		timers.get(name).start();
	}
	
	/**
	 * pause timer and return accumulated time (in millis)
	 * @return
	 */
	public long pause()
	{
		this._netTime += tock();
		if ( this._verbose)
			this.report();
		return this._netTime;
	}
	
	/**
	 * Pause named timer
	 * @param name
	 */
	public static void pause(String name)
	{
		timers.get(name).pause();
	}

	/**
	 * return end print end time to screen
	 * @return
	 */
	public long stop()
	{
		this._netTime += tock();
		long out = this.reportNet();
		this._netTime = 0;
		return out;
	}
	
	/**
	 * return end print end time of named timer to screen 
	 * @param name
	 * @return
	 */
	public static long stop(String name)
	{
		return timers.get(name).stop();
	}
	
	public static void stopAll()
	{
		for ( String name : timers.keySet() )
			stop(name);
	}
	
	/**
	 * return end print end time to screen
	 * @return
	 */
	public long report()
	{
		long out = this._netTime + tock();
		System.out.println(this._name + " timed: " + 
				( ( ( BigDecimal.valueOf( out ) ) ).multiply(
				BigDecimal.valueOf( 0.001 ) ).doubleValue() ) + " s");
		return out;
	}
	
	public long reportNet()
	{
		long out = this._netTime;
		System.out.println(this._name + " timed: " + 
				( ( ( BigDecimal.valueOf( out ) ) ).multiply(
				BigDecimal.valueOf( 0.001 ) ).doubleValue() ) + " s");
		return out;
	}
	
	/**
	 * return end print end time of named timer to screen 
	 * @return
	 */
	public static long report(String name)
	{
		return timers.get(name).report();
	}
	
	/**
	 * Set timer output to verbose (prints to screen whenever paused or tocked).
	 * @param verbose
	 */
	public void verbose(boolean verbose)
	{
		this._verbose = verbose;
	}
	
	/**
	 * Set named timer output to verbose (prints to screen whenever paused or 
	 * tocked).
	 * @param name
	 * @param verbose
	 */
	public static void verbose(String name, boolean verbose)
	{
		timers.get(name).verbose(verbose);
	}
}
