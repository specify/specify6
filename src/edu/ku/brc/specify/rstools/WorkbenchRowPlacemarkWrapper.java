/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.rstools;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.GeoRefConverter.GeoRefFormat;

/**
 * This class implements the {@link LatLonPlacemarkIFace} interface
 * using the data in a {@link WorkbenchRow} object.
 * 
 * @author jstewart
 * @code_status Beta
 */
public class WorkbenchRowPlacemarkWrapper implements LatLonPlacemarkIFace
{
    /** The {@link WorkbenchRow} containing the actual data. */
    protected WorkbenchRow wbRow;
    
    /** A text label used to identify this placemark. */
    protected String label;
    
    /** The {@link WorkbenchTemplateMappingItem}s describing the row. */
    protected Hashtable<Short, WorkbenchTemplateMappingItem> mappings = null;
    
    /** The data items contained in the row. */
    protected Vector<WorkbenchDataItem> dataList = null;
    
    protected List<WorkbenchTemplateMappingItem>               visibleColumns;
    protected Hashtable<WorkbenchTemplateMappingItem, Boolean> visibleMap = new Hashtable<WorkbenchTemplateMappingItem, Boolean>();

    /**
     * Constructor.
     * 
     * @param row the {@link WorkbenchRow} to represent as a {@link LatLonPlacemarkIFace} object
     * @param label the text label of the resulting placemark
     * @param visibleColumns the columns to use for the HTML
     */
    public WorkbenchRowPlacemarkWrapper(final WorkbenchRow row, 
                                        final String label,
                                        final List<WorkbenchTemplateMappingItem> visibleColumns)
    {
        this.wbRow = row;
        this.label = label;
        this.visibleColumns = visibleColumns;
        
        for (WorkbenchTemplateMappingItem wbtmi : visibleColumns)
        {
            visibleMap.put(wbtmi, true);
        }
    }
    
    /**
     * Initializes all data needed by the {@link LatLonPlacemarkIFace} methods.
     */
    protected void initExportData()
    {
        if (mappings == null)
        {
            mappings = new Hashtable<Short, WorkbenchTemplateMappingItem>();
            for (WorkbenchTemplateMappingItem wbtmi : wbRow.getWorkbench().getWorkbenchTemplate().getWorkbenchTemplateMappingItems())
            {
                mappings.put(wbtmi.getViewOrder(), wbtmi);
            }
        }
        
        if (dataList == null)
        {
            dataList = new Vector<WorkbenchDataItem>(wbRow.getWorkbenchDataItems());
            Collections.sort(dataList);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#cleanup()
     */
    public void cleanup()
    {
        mappings.clear();
        mappings = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.rstools.GoogleEarthPlacemarkIFace#getHtmlContent(java.lang.String)
     */
    public String getHtmlContent(final String textColorArg)
    {
        String textColor = UIHelper.fixColorForHTML(textColorArg);
        
        boolean useCaptions = AppPreferences.getRemote().getBoolean("google.earth.useorigheaders", true);
        
        // XXX In the Future this should call a delegate
        // so it isn't hard code in the class
        
        initExportData();
        
        StringBuilder sb = new StringBuilder("<table>");
        for (WorkbenchDataItem wbdi : dataList)
        {
            WorkbenchTemplateMappingItem wbtmi = mappings.get(wbdi.getColumnNumber());
            if (visibleMap.get(wbtmi) != null && wbtmi.getIsExportableToContent())
            {
                sb.append("<tr><td align=\"right\"><font color=\"");
                sb.append(textColor);
                sb.append("\">");
            
                sb.append(useCaptions ? wbtmi.getCaption() : wbtmi.getTitle());
                sb.append(":</font></td><td align=\"left\" valign=\"middle\"><font color=\"");
                sb.append(textColor);
                sb.append("\">");
            
                sb.append(wbdi.getCellData());
                sb.append("</font></td></tr>\n");
            }
        } 
        sb.append("</table>\n");
        
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getLatLon()
     */
    public Pair<Double, Double> getLatLon()
    {
        GeoRefConverter converter = new GeoRefConverter();
        
        initExportData();
        
        Double latitude  = null;
        Double longitude = null;
        
        for (WorkbenchDataItem wbdi : dataList)
        {
            WorkbenchTemplateMappingItem wbtmi = mappings.get(wbdi.getColumnNumber());
            if (wbtmi.getFieldName().equals("latitude1"))
            {
                String valStr = wbdi.getCellData();
                
                if (StringUtils.isNotEmpty(valStr))
                {
                    String latStr;
                    try
                    {
                        latStr   = converter.convert(StringUtils.stripToNull(valStr), GeoRefFormat.D_PLUS_MINUS.name());
                        latitude = UIHelper.parseDouble(latStr);
                    }
                    catch (Exception e)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchRowPlacemarkWrapper.class, e);
                        latitude = null;
                    }
                }
            } else if (wbtmi.getFieldName().equals("longitude1"))
            {
                String valStr = wbdi.getCellData();
                if (StringUtils.isNotEmpty(valStr))
                {
                    String lonStr;
                    try
                    {
                        lonStr = converter.convert(StringUtils.stripToNull(valStr), GeoRefFormat.D_PLUS_MINUS.name());
                        longitude = UIHelper.parseDouble(lonStr);
                    }
                    catch (Exception e)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchRowPlacemarkWrapper.class, e);
                        longitude = null;
                    }
                }
            }
        }
        
        if (latitude != null && longitude != null)
        {
            return new Pair<Double, Double>(latitude, longitude);
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getTitle()
     */
    public String getTitle()
    {
        initExportData();
        return label;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.rstools.GoogleEarthPlacemarkIFace#getImageIcon()
     */
    public ImageIcon getImageIcon()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#getAltitude()
     */
    @Override
    public Double getAltitude()
    {
        return null;
    }


}
