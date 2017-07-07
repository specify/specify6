/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.util.ArrayList;
import java.util.List;

import edu.ku.brc.specify.datamodel.DataModelObjBase;
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
	protected final String tblTitle;
	protected final List<Integer> colIdxs;
	protected final int numberOfMatches;
	protected final boolean isBlank;
	protected final boolean isSkipped;
	protected final List<Integer> matchIds = new ArrayList<Integer>();
	
	
	/**
	 * @param tblTitle
	 * @param numberOfMatches
	 * @param colIdxs
	 * @param isBlank
	 * @param isSkipped
	 */
	public UploadTableMatchInfo(String tblTitle, int numberOfMatches, List<Integer> colIdxs, boolean isBlank, boolean isSkipped)
	{
		super();
		this.tblTitle = tblTitle;
		this.numberOfMatches = numberOfMatches;
		this.colIdxs = colIdxs;
		this.isBlank = isBlank;
		this.isSkipped = isSkipped;
	}

	/**
	 * @param tblTitle
	 * @param matches
	 * @param colIdxs
	 * @param isBlank
	 * @param isSkipped
	 */
	public UploadTableMatchInfo(String tblTitle, List<DataModelObjBase> matches, List<Integer> colIdxs, boolean isBlank, boolean isSkipped)
	{
		super();
		this.tblTitle = tblTitle;
		this.numberOfMatches = matches.size();
		for (DataModelObjBase obj : matches) {
			this.matchIds.add(obj.getId());
		}
		this.colIdxs = colIdxs;
		this.isBlank = isBlank;
		this.isSkipped = isSkipped;
	}

	/**
	 * @return the matchIds
	 */
	public List<Integer> getMatchIds() {
		return new ArrayList<Integer>(matchIds);
	}
	/**
	 * @return the tblTitle
	 */
	public String getTblTitle()
	{
		return tblTitle;
	}
	/**
	 * @return the colIdxs
	 */
	public List<Integer> getColIdxs() 
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
	 * @return the isBlank
	 */
	public boolean isBlank() 
	{
		return isBlank;
	}

	/**
	 * @return the isSkipped
	 */
	public boolean isSkipped() 
	{
		return isSkipped;
	}

	/**
	 * @return text description of the match situation.
	 */
	public String getDescription()
	{
		if (numberOfMatches == 0)
		{
			return String.format(UIRegistry.getResourceString("UploadTableMatchInfo.NoMatch"), tblTitle);
		}
		
		if (numberOfMatches == 1)
		{
			return String.format(UIRegistry.getResourceString("UploadTableMatchInfo.Matched"), 
					tblTitle);
		}
		
		return String.format(UIRegistry.getResourceString("UploadTableMatchInfo.MultipleMatches"), 
				numberOfMatches, tblTitle);
	}
}
