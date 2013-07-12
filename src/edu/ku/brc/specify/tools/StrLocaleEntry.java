/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.tools;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 15, 2009
 *
 */
public class StrLocaleEntry
{
    public enum STATUS {IsNew, HasChanged, IsOK, IsComment, IsBlank}
    
    protected String key;
    protected String srcStr;
    protected String dstStr;
    
    protected STATUS status;
    
    protected boolean edited = false;
    
    public StrLocaleEntry(String key, 
                          String srcStr, 
                          String dstStr,
                          STATUS status)
    {
        super();
        this.key = key;
        this.srcStr = srcStr;
        this.dstStr = dstStr;
        this.status = status;
    }

    /**
     * @return
     */
    public boolean isValue()
    {
        return status != STATUS.IsComment && status != STATUS.IsBlank;
    }
    
    /**
     * @return the key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * @return the srcStr
     */
    public String getSrcStr()
    {
        return srcStr;
    }

    /**
     * @return the dstStr
     */
    public String getDstStr()
    {
        return dstStr;
    }

    /**
     * @param srcStr the srcStr to set
     */
    public void setSrcStr(String srcStr)
    {
        this.srcStr = srcStr;
    }

    /**
     * @param dstStr the dstStr to set
     */
    public void setDstStr(String dstStr)
    {
        if (this.dstStr == null || !this.dstStr.equals(dstStr))
        {
        	edited = true;
        	this.dstStr = dstStr;
        }
    }

    /**
     * @return the status
     */
    public STATUS getStatus()
    {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(STATUS status)
    {
        this.status = status;
    }

    
	/**
	 * @return the edited
	 */
	public boolean isEdited()
	{
		return edited;
	}

	/**
	 * @param edited the edited to set
	 */
	public void setEdited(boolean edited)
	{
		this.edited = edited;
	}
    
	
}