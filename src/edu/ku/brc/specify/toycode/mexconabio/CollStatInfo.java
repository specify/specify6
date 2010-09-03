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
package edu.ku.brc.specify.toycode.mexconabio;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.specify.toycode.mexconabio.CollStatSQLDefs.StatType;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 2, 2010
 *
 */
public class CollStatInfo
{
    
    protected String title;
    protected String chartFileName;
    
    protected int geoRefed          = 0;
    protected int hasCollNum        = 0;
    protected int hasYearOnly       = 0;
    protected int hasYearMonOnly    = 0;
    protected int hasYMDayOnly      = 0;
    protected int hasSciNameNoGenSp = 0;
    
    protected int missingDate       = 0;
    protected int missingLocality   = 0;
    protected int missingLatLon     = 0;
    protected int missingCollectors = 0;
    protected int missingCountries  = 0;
    protected int totalNumRecords   = 0;
       
    
    /**
     * 
     */
    public CollStatInfo()
    {
        super();
    }

    /**
     * 
     */
    public CollStatInfo(final String title)
    {
        super();
        this.title = title;
    }

    public void setValue(final StatType statType, final int value)
    {
        switch (statType)
        {
            case eTotalNumRecords:
                totalNumRecords = value;
                break;
                
            case eGeoRefed:
                geoRefed = value;
                break;

            case eHasCollNum:
                hasCollNum = value;
                break;

            case eHasYearOnly:
                hasYearOnly = value;
                break;
                
            case eHasYearMonOnly:
                hasYearMonOnly = value;
                break;
                
            case eHasYMDayOnly:
                hasYMDayOnly = value;
                break;
                
            case eHasSciNameNoGenSp:
                hasSciNameNoGenSp = value;
                break;
                
            case eMissingLocality:
                missingLocality = value;
                break;
                
            case eMissingCollectors:
                missingCollectors = value;
                break;
                
            case eMissingCountries:
                missingCountries = value;
                break;
        }
    }

    public int getValue(final StatType statType)
    {
        switch (statType)
        {
            case eTotalNumRecords:
                return totalNumRecords;
                
            case eGeoRefed:
                return geoRefed;

            case eHasCollNum:
                return hasCollNum;

            case eHasYearOnly:
                return hasYearOnly;
                
            case eHasYearMonOnly:
                return hasYearMonOnly;
                
            case eHasYMDayOnly:
                return hasYMDayOnly;
                
            case eHasSciNameNoGenSp:
                return hasSciNameNoGenSp;
                
            case eMissingLocality:
                return missingLocality;
                
            case eMissingCollectors:
                return missingCollectors;
                
            case eMissingCountries:
                return missingCollectors;
        }
        return 0;
    }

    /**
     * @return the totalNumRecords
     */
    public int getTotalNumRecords()
    {
        return totalNumRecords;
    }

    /**
     * @param totalNumRecords the totalNumRecords to set
     */
    public void setTotalNumRecords(int totalNumRecords)
    {
        this.totalNumRecords = totalNumRecords;
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
     * @return the chartFileName
     */
    public String getChartFileName()
    {
        return chartFileName;
    }

    /**
     * @param chartFileName the chartFileName to set
     */
    public void setChartFileName(String chartFileName)
    {
        this.chartFileName = chartFileName;
    }

    /**
     * @param xstream
     */
    public static void config(final XStream xstream)
    {
        xstream.alias("colstatinfo", CollStatInfo.class); //$NON-NLS-1$
        xstream.useAttributeFor(CollStatInfo.class, "title"); //$NON-NLS-1$
        
        xstream.omitField(CollStatInfo.class, "chartFileName"); //$NON-NLS-1$
    }

}
