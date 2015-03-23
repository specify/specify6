/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
	 * The name of the field mapped to.
	 */
	protected String fldName;
    /**
     * The caption for the rank in the workbench.
     */
    protected String wbFldName;
	/**
	 * The 1-to-many 'order' of the column (currently used only for determinations - genus1, genus2 ...)
	 */
	protected Integer sequence = null;
	/**
	 * True if there must be data for this rank.
	 */
	protected boolean required;
	/**
	 * True for taxa levels defined in determination table
	 */
	protected boolean isLowerSubTree;
	/**
	 * @param index
	 * @param rank
	 * @param sequence
	 * @param required
	 */
	public TreeMapElement(int index, String fldName, String wbFldName, int rank, Integer sequence, boolean required, boolean isLowerSubTree)
	{
		super();
		this.index = index;
		this.fldName = fldName;
        this.wbFldName = wbFldName;
		this.rank = rank;
		this.sequence = sequence;
		this.required = required;
		this.isLowerSubTree = isLowerSubTree;
	}
	/**
	 * @param rank
	 * @param index
	 * @param required
	 */
	public TreeMapElement(int index, String wbFldName, int rank, boolean required)
	{
		super();
		this.index = index;
        this.wbFldName = wbFldName;
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
		return sequence != null ? sequence : 0;
	}
    /**
     * @param sequence the sequence to set
     */
    public void setSequence(Integer sequence)
    {
        this.sequence = sequence;
    }
    /**
     * @return the wbFldName
     */
    public final String getWbFldName()
    {
        return wbFldName;
    }	
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return wbFldName + "(" + String.valueOf(index) + ") " + String.valueOf(rank) + ", " + String.valueOf(sequence);
    }
 
    /**
     * @return the fldName
     */
    public String getFldName()
    {
        return fldName;
    }
    /**
     * @param fldName the fldName to set
     */
    public void setFldName(String fldName)
    {
        this.fldName = fldName;
    }
	/**
	 * @return the isLowerSubTree situation
	 */
	public boolean isLowerSubTree() {
		return isLowerSubTree;
	}
	/**
	 * @param isLowerSubTree the isLowerSubTreeness to establish
	 */
	public void setLowerSubTree(boolean isLowerSubTree) {
		this.isLowerSubTree = isLowerSubTree;
	}
    
    
}
