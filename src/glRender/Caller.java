package glRender;

import java.awt.EventQueue;

/**
 * Example of how to invoke a Render object giving it a CommandMediator
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark
 */
public class Caller {

	public static void main(String[] args) {
		
		/* create the mediator */
		CommandMediatorExample mediator = new CommandMediatorExample();
		
		/* Assign the mediator to a new Render window */
		Render myRender = new Render(mediator);
		
		/* invoke the renderer (on its own thread) */
		EventQueue.invokeLater(myRender);	
	}
}
