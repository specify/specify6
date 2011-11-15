/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.config.init;

import com.thoughtworks.xstream.XStream;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Mar 1, 2009
 *
 */
public class TreeDefRow 
{
    protected String  defName;
    protected String  title;
    protected int     rank;
    protected boolean isIncluded;
    protected boolean isEnforced;
    protected boolean isInFullName;
    protected boolean isRequired;
    protected String  separator;
    
    /**
     * Default no arg constructor
     */
    public TreeDefRow()
    {
    	//nothing to do
    }
    
    /**
     * @param defName
     * @param rank
     * @param isEnforced
     * @param isInFullName
     */
    public TreeDefRow(String  defName, 
                      String  title,
                      int     rank, 
                      boolean isIncluded, 
                      boolean isEnforced, 
                      boolean isInFullName, 
                      boolean isRequired, 
                      String separator)
    {
        super();
        this.defName    = defName;
        this.title      = title;
        this.rank       = rank;
        this.isIncluded = isIncluded;
        this.isEnforced = isEnforced;
        this.isInFullName = isInFullName;
        this.isRequired   = isRequired;
        this.separator   = separator;
    }
    /**
     * @return the defName
     */
    public String getDefName()
    {
        return defName;
    }
    /**
     * @return the rank
     */
    public int getRank()
    {
        return rank;
    }
    /**
     * @return the isEnforced
     */
    public boolean isEnforced()
    {
        return isEnforced;
    }
    /**
     * @return the isInFullName
     */
    public boolean isInFullName()
    {
        return isInFullName;
    }
    /**
     * @return the isEditable
     */
    public boolean isRequired()
    {
        return isRequired;
    }
    /**
     * @param isEnforced the isEnforced to set
     */
    public void setEnforced(boolean isEnforced)
    {
        this.isEnforced = isEnforced;
    }
    /**
     * @param isInFullName the isInFullName to set
     */
    public void setInFullName(boolean isInFullName)
    {
        this.isInFullName = isInFullName;
    }
    /**
     * @return the separator
     */
    public String getSeparator()
    {
        return separator;
    }
    /**
     * @param separator the separator to set
     */
    public void setSeparator(String separator)
    {
        this.separator = separator;
    }
    
    /**
     * @return the isIncluded
     */
    public boolean isIncluded()
    {
        return isIncluded;
    }
    
    /**
     * @param isIncluded the isIncluded to set
     */
    public void setIncluded(boolean isIncluded)
    {
        this.isIncluded = isIncluded;
    }
    
    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title)
    {
        this.title = title;
    }
    /**
     * Configures the XStream for I/O.
     * @param xstream the stream
     */
    public static void configXStream(final XStream xstream)
    {
        xstream.useAttributeFor(TreeDefRow.class, "isIncluded");
        xstream.useAttributeFor(TreeDefRow.class, "defName");
        xstream.useAttributeFor(TreeDefRow.class, "rank");
        xstream.useAttributeFor(TreeDefRow.class, "isEnforced");
        xstream.useAttributeFor(TreeDefRow.class, "isInFullName");
        xstream.useAttributeFor(TreeDefRow.class, "isRequired");
        xstream.useAttributeFor(TreeDefRow.class, "separator");
        xstream.useAttributeFor(TreeDefRow.class, "title");
    }
}
