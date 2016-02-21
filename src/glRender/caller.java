package glRender;

import java.awt.EventQueue;


public class caller {

	public static void main(String[] args) {
		
		CommandMediatorExample mediator = new CommandMediatorExample();
		Render myRender = new Render(mediator);
		EventQueue.invokeLater(myRender);
		
		
	}
	
	

}
