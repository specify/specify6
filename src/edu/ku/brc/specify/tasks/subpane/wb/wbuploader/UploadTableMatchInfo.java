/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.Vector;

import edu.ku.brc.ui.UIRegistry;

/**
 * @author timo
 * 
 * Information about the matching for a particular Upload table for a row.
 * ColIdxs are the workbench indexes for the table.
 * isMatch is true if there is a matching record in the db for the current row values.
 * Each 'sequence' (e.g. Collector 1, Collector 2, ...) for an upload table will
 * generate its own UploadTableMatchInfo object.
 *
 */
public class UploadTableMatchInfo
{
	protected final Vector<Integer> colIdxs;
	protected final int numberOfMatches;
	
	
	/**
	 * @param match
	 * @param uploadTable
	 */
	public UploadTableMatchInfo(int numberOfMatches, Vector<Integer> colIdxs)
	{
		super();
		this.numberOfMatches = numberOfMatches;
		this.colIdxs = colIdxs;
	}
	
	/**
	 * @return the colIdxs
	 */
	public Vector<Integer> getColIdxs() 
	{
		return colIdxs;
	}
	/**
	 * @return the match
	 */
	public int getNumberOfMatches() 
	{
		return numberOfMatches;
	}
	
	/**
	 * @return text description of the match situation.
	 */
	public String getDescription()
	{
		if (numberOfMatches == 0)
		{
			return UIRegistry.getResourceString("UploadTableMatchInfo.NoMatch");
		}
		
		if (numberOfMatches == 1)
		{
			return UIRegistry.getResourceString("UploadTableMatchInfo.Matched");
		}
		
		return String.format(UIRegistry.getResourceString("UploadTableMatchInfo.MultipleMatches"), numberOfMatches);
	}
}
