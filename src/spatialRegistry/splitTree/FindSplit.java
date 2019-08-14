package spatialRegistry.splitTree;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import spatialRegistry.Area;
import spatialRegistry.Entry;

/**
 * Testing potential further speed ups
 * 
 * NOTE do not use this method, it is not thread safe, just for testing purposes
 * 
 * @author Bastiaan Cockx @BastiaanCockx (baco@env.dtu.dk), DTU, Denmark.
 *
 */
@SuppressWarnings("rawtypes")
public class FindSplit extends RecursiveTask<List<List<Entry>>> 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -767857163097668802L;
	
	SplitTree<?> tree;
	
	List<Area> objectives;
	
	public FindSplit(SplitTree<?> tree, List<Area> objectives)
	{
		this.tree = tree;
		this.objectives = objectives;
	}

	@Override
	protected List<List<Entry>> compute()
	{
		List<List<Entry>> listr = new LinkedList<List<Entry>>();
//		if (objectives.size() > 100)
//		{
//			FindSplit p1 = new FindSplit(this.tree, this.objectives.subList(0, objectives.size()/2));
//			p1.fork();
//			listr.addAll(new FindSplit(this.tree, this.objectives.subList(objectives.size()/2+1, objectives.size())).compute());
//			listr.addAll(p1.join());
//		}
//		else
//		{
//			for ( Area a : objectives )
//				listr.add(tree.find(a));
//		}
		return listr;
	}
}
