package glRender;

import java.awt.EventQueue;

/**
 * Example of how to invoke a Render object giving it a CommandMediator
 * @author baco
 *
 */
public class Caller {

	public static void main(String[] args) {
		
		CommandMediatorExample mediator = new CommandMediatorExample();
		Render myRender = new Render(mediator);
		EventQueue.invokeLater(myRender);	
		
	}

}
