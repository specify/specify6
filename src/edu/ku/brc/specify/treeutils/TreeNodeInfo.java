/**
 * 
 */
package edu.ku.brc.specify.treeutils;

/**
 * @author timo
 *
 *Used by TreeTraversalWorkers
 */
public class TreeNodeInfo
{
	protected final int id;
	protected final int rank;
	protected final String name;
	
	public TreeNodeInfo(final int id, final int rank, final String name)
	{
		this.id = id;
		this.rank = rank;
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public int getId() 
	{
		return id;
	}

	/**
	 * @return the rank
	 */
	public int getRank() 
	{
		return rank;
	}

	/**
	 * @return the name
	 */
	public String getName() 
	{
		return name;
	}

}    	
