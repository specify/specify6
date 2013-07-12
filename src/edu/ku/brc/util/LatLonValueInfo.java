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
package edu.ku.brc.util;

import java.util.ArrayList;

import edu.ku.brc.util.LatLonConverter.FORMAT;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Nov 9, 2008
 *
 */
public class LatLonValueInfo
{
    protected String  strVal;
    protected String  dirStr;
    protected String  fmtStrVal;
    protected FORMAT  format;
    protected boolean hasDegreesTxt;
    
    protected ArrayList<String> parts = new ArrayList<String>(4);
    
    /**
     * 
     */
    public LatLonValueInfo(final boolean hasDegreesTxt)
    {
        super();
        
        this.strVal        = null;
        this.fmtStrVal     = null;
        this.dirStr        = null;
        this.format        = null;
        this.hasDegreesTxt = hasDegreesTxt;
    }
    
    /**
     * @param str
     */
    public void addPart(final String str)
    {
        parts.add(str);
    }
    
    /**
     * @param inx
     * @return
     */
    public String getPart(final int inx)
    {
        return parts.get(inx);
    }
    
    /**
     * @return the strVal
     */
    public String getStrVal(final boolean inclDir)
    {
        if (strVal == null)
        {
            strVal = createValStrFromParts(false, inclDir);
        }
        return strVal;
    }
    
    public String getFormattedStrVal()
    {
        if (fmtStrVal == null)
        {
            fmtStrVal = createValStrFromParts(true, true);
        }
        return fmtStrVal;
    }

    /**
     * @param strVal the strVal to set
     */
    public void setStrVal(String strVal)
    {
        this.strVal = strVal;
    }
    /**
     * @return the dirStr
     */
    public String getDirStr()
    {
        return dirStr;
    }
    /**
     * @param dirStr the dirStr to set
     */
    public void setDirStr(String dirStr)
    {
        this.dirStr = dirStr;
    }
    /**
     * @return the format
     */
    public FORMAT getFormat()
    {
        return format;
    }
    /**
     * @param format the format to set
     */
    public void setFormat(FORMAT format)
    {
        this.format = format;
    }
    
    public String getBaseStrVal()
    {
        return strVal.substring(0, strVal.length()-2);
    }
    
    /**
     * @return the hasDegreesTxt
     */
    public boolean isHasDegreesTxt()
    {
        return hasDegreesTxt;
    }

    /**
     * @param hasDegreesTxt the hasDegreesTxt to set
     */
    public void setHasDegreesTxt(boolean hasDegreesTxt)
    {
        this.hasDegreesTxt = hasDegreesTxt;
    }
    
    /**
     * @return
     */
    public boolean isDirPositive()
    {
        return dirStr != null && (dirStr.equals(LatLonConverter.northSouth[0]) || dirStr.equals(LatLonConverter.eastWest[0]));
    }

    /**
     * @param doAddSymbols
     * @return
     */
    public String createValStrFromParts(final boolean doAddSymbols, final boolean inclDir)
    {
        if (parts.size() > 0)
        {
            StringBuilder sb = new StringBuilder(parts.get(0));
            if (doAddSymbols)
            {
                sb.append(LatLonConverter.DEGREES_SYMBOL);
            }
            
            switch (format)
            {
                case DDDDDD : 
                    if (inclDir)
                    {
                        sb.append(' ');
                        sb.append(parts.get(1));          // N/S/E/W
                    }
                    break;
                    
                case DDMMMM : 
                    sb.append(' ');
                    sb.append(parts.get(1));
                    if (doAddSymbols)
                    {
                        sb.append("'");
                    }
                    if (inclDir)
                    {
                        sb.append(' ');
                        sb.append(parts.get(2));          // N/S/E/W
                    }
                    break;
                    
                case DDMMSS :
                    sb.append(' ');
                    sb.append(parts.get(1));
                    if (doAddSymbols)
                    {
                        sb.append("'");
                    }
                    
                    sb.append(' ');
                    sb.append(parts.get(2));
                    if (doAddSymbols)
                    {
                        sb.append("\"");
                    }
                    if (inclDir)
                    {
                        sb.append(' ');
                        sb.append(parts.get(3));          // N/S/E/W
                    }
                    break;
                    
                default:
                    break;
            } // switch
            
            return sb.toString();
        }
        return null;
    }
}
