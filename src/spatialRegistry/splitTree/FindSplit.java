package spatialRegistry.splitTree;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import spatialRegistry.splitTree.SplitTree.Area;
import spatialRegistry.splitTree.SplitTree.Entry;

/**
 * NOTE do not use this method, it is not thread safe, just for testing purposes
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
public class FindSplit extends RecursiveTask<List<List<Entry>>> 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -767857163097668802L;
	
	SplitTree tree;
	
	LinkedList<Area> objectives;
	
	public FindSplit(SplitTree tree, LinkedList<Area> objectives)
	{
		this.tree = tree;
		this.objectives = objectives;
	}

	@Override
	protected List<List<Entry>> compute()
	{
		List<List<Entry>> listr;
		FindSplit buddy = null;
		
		Area mine = objectives.getFirst();
		objectives.remove(mine);
		
		if (! objectives.isEmpty() )
		{
			buddy = new FindSplit(this.tree, this.objectives);
			buddy.fork();
		}
		List<Entry> result = tree.find(mine);
		if ( objectives.isEmpty() )
			listr = new LinkedList<List<Entry>>();
		else
			listr = buddy.join();
		listr.add(result);
		return listr;
	}
}
