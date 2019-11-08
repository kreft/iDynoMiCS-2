package debugTools;

/**
 * Testable interface is used by classes that can be run using the Tester Class
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 *
 */
public interface Testable {
	
	public enum TestMode {
		UNIT,
		CONSOLE,
		DEBUG
	}
	
	public void test(TestMode mode);

}
