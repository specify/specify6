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
    protected String instName;
    protected String chartFileName; // Transient
    
    protected int geoRefed          = 0;
    protected int hasCollNum        = 0;
    protected int hasYearOnly       = 0;
    protected int hasYearMonOnly    = 0;
    protected int hasYMDayOnly      = 0;
    protected int hasSciNameNoGenSp = 0;
    
    protected int hasLocality       = 0;
    protected int hasLatLon         = 0;
    protected int hasCollectors     = 0;
    protected int hasCountries      = 0;
    protected int hasStates         = 0;
    
    protected int totalNumRecords   = 0;
    
    protected double averagePercent = 0.0; // Transient
       
    
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
                
            case eHasLocality:
                hasLocality = value;
                break;
                
            case eHasCollectors:
                hasCollectors = value;
                break;
                
            case eHasCountries:
                hasCountries = value;
                break;
                
            case eHasStates:
                hasStates = value;
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
                
            case eHasLocality:
                return hasLocality;
                
            case eHasCollectors:
                return hasCollectors;
                
            case eHasCountries:
                return hasCountries;
                
            case eHasStates:
                return hasStates;
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
     * @return the instName
     */
    public String getInstName()
    {
        return instName;
    }

    /**
     * @param instName the instName to set
     */
    public void setInstName(String instName)
    {
        this.instName = instName;
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
     * @return the averagePercent
     */
    public double getAveragePercent()
    {
        return averagePercent;
    }

    /**
     * @param averagePercent the averagePercent to set
     */
    public void setAveragePercent(double averagePercent)
    {
        this.averagePercent = averagePercent;
    }

    /**
     * @param xstream
     */
    public static void config(final XStream xstream)
    {
        xstream.alias("colstatinfo", CollStatInfo.class); //$NON-NLS-1$
        xstream.useAttributeFor(CollStatInfo.class, "title"); //$NON-NLS-1$
        
        xstream.omitField(CollStatInfo.class, "chartFileName"); //$NON-NLS-1$
        xstream.omitField(CollStatInfo.class, "averagePercent"); //$NON-NLS-1$
    }

}
