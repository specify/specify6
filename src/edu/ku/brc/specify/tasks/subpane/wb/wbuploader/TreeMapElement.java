/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

/**
 * @author timo
 *
 * Used to construct workbench an upload mapping for a given rank of a Treeable class. 
 */
public class TreeMapElement
{
	/**
	 * The rank being mapped
	 */
	protected int rank;
	/**
	 * The index in the workbench being uploaded.
	 */
	protected int index;
	/**
	 * The 1-to-many 'order' of the column (currently used only for determinations - genus1, genus2 ...)
	 */
	protected Integer sequence = null;
	/**
	 * True if there must be data for this rank.
	 */
	protected boolean required;
	/**
	 * @param index
	 * @param rank
	 * @param sequence
	 * @param required
	 */
	public TreeMapElement(int index, int rank, Integer sequence, boolean required)
	{
		super();
		this.index = index;
		this.rank = rank;
		this.sequence = sequence;
		this.required = required;
	}
	/**
	 * @param rank
	 * @param index
	 * @param required
	 */
	public TreeMapElement(int index, int rank, boolean required)
	{
		super();
		this.index = index;
		this.rank = rank;
		this.required = required;
	}
	/**
	 * @return the index
	 */
	public int getIndex()
	{
		return index;
	}
	/**
	 * @return the rank
	 */
	public int getRank()
	{
		return rank;
	}
	/**
	 * @return the required
	 */
	public boolean isRequired()
	{
		return required;
	}
	/**
	 * @return the sequence
	 */
	public Integer getSequence()
	{
		return sequence;
	}
    /**
     * @param sequence the sequence to set
     */
    public void setSequence(Integer sequence)
    {
        this.sequence = sequence;
    }			
}
