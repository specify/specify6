/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

/**
 * @author Administrator
 *
 */
public interface QBJRDataSourceListenerIFace
{
	/**
	 * @param progressPerCent
	 * 
	 * Sent when the current row changes.
	 */
	public void currentRow(final int currentRow);	
	
	/**
	 * @param rowCount
	 * 
	 * Sent when/if the total row count is determined. 
	 */
	public void rowCount(final int rowCount);
	
	/**
	 * @param rows the number of rows processed.
	 * 
	 * Sent when processing stops.
	 */
	public void done(final int rows);
	
	/**
	 * Sent when/if data needs to be pre-processed before JR report can be filled.
	 */
	public void loading();
	
	/**
	 * Sent when data is loaded.
	 */
	public void loaded();
	
	/**
	 * Sent when fill begins.
	 */
	public void filling();
	
}
