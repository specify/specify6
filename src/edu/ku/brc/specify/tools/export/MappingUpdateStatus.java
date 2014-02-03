/**
 * 
 */
package edu.ku.brc.specify.tools.export;

/**
 * @author timo
 *
 */
public class MappingUpdateStatus
{
	protected final long recsToDelete;
	protected final long recsUpdated;
	protected final long recsAdded;
	protected final long totalRecsChanged;
	/**
	 * @param recsToDelete
	 * @param recsUpdated
	 * @param recsAdded
	 * @param totalRecsChanged
	 */
	public MappingUpdateStatus(long recsToDelete, long recsUpdated,
			long recsAdded, long totalRecsChanged)
	{
		super();
		this.recsToDelete = recsToDelete;
		this.recsUpdated = recsUpdated;
		this.recsAdded = recsAdded;
		this.totalRecsChanged = totalRecsChanged;
	}
	/**
	 * @return the recsToDelete
	 */
	public long getRecsToDelete()
	{
		return recsToDelete;
	}
	/**
	 * @return the recsUpdated
	 */
	public long getRecsUpdated()
	{
		return recsUpdated;
	}
	/**
	 * @return the recsAdded
	 */
	public long getRecsAdded()
	{
		return recsAdded;
	}
	/**
	 * @return the totalRecsChanged
	 */
	public long getTotalRecsChanged()
	{
		return totalRecsChanged;
	}
	
	
}
