/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.conversion;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 4, 2011
 *
 */
public class PartialDateConv
{
    private String dateStr;
    private String partial;
    private String verbatim;
    
    /**
     * 
     */
    public PartialDateConv()
    {
        super();
        this.dateStr  = null;
        this.partial  = null;
        this.verbatim = null;
    }
    /**
     * @param dateStr
     * @param partial
     */
    public PartialDateConv(final String dateStr, final String partial)
    {
        super();
        this.dateStr = dateStr;
        this.partial = partial;
        this.verbatim = "NULL";
    }
    
    public void setAllNullStrs()
    {
        dateStr  = "NULL";
        partial  = "NULL";
        verbatim = "NULL";

    }
    
    /**
     * @return the dateStr
     */
    public String getDateStr()
    {
        return dateStr;
    }
    
    /**
     * @param dateStr the dateStr to set
     */
    public void set(String dateStrArg, final String partialArg, final String verbatimArg)
    {
        dateStr = dateStrArg;
        partial = partialArg;
        verbatim = verbatimArg;
    }
    
    public void nullAll()
    {
        dateStr = null;
        partial = null;
        verbatim = null;
    }
    
    /**
     * @param dateStr the dateStr to set
     */
    public void setDateStr(String dateStr)
    {
        this.dateStr = dateStr;
    }
    /**
     * @return the partial
     */
    public String getPartial()
    {
        return partial;
    }
    /**
     * @param partial the partial to set
     */
    public void setPartial(String partial)
    {
        this.partial = partial;
    }
    /**
     * @return the verbatim
     */
    public String getVerbatim()
    {
        return verbatim;
    }
    /**
     * @param verbatim the verbatim to set
     */
    public void setVerbatim(String verbatim)
    {
        this.verbatim = verbatim;
    }
    
    public boolean isNull()
    {
        return dateStr == null || dateStr.equalsIgnoreCase("NULL");
    }
}
