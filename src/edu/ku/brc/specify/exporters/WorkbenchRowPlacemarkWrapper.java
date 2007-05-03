package edu.ku.brc.specify.exporters;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.GeoRefConverter.GeoRefFormat;

/**
 * This class implements the {@link GoogleEarthPlacemarkIFace} interface
 * using the data in a {@link WorkbenchRow} object.
 * 
 * @author jstewart
 * @code_status Alpha
 */
public class WorkbenchRowPlacemarkWrapper implements GoogleEarthPlacemarkIFace
{
    protected WorkbenchRow wbRow;
    protected String label;
    protected Hashtable<Short, WorkbenchTemplateMappingItem> mappings = null;
    protected Vector<WorkbenchDataItem>                      dataList = null;

    public WorkbenchRowPlacemarkWrapper( WorkbenchRow row, String label )
    {
        this.wbRow = row;
        this.label = label;
    }
    
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
    
    public void cleanup()
    {
        mappings.clear();
        mappings = null;
    }

    public String getHtmlContent()
    {
        // XXX In the Future this should call a delegate
        // so it isn't hard code in the class
        
        initExportData();
        
        StringBuilder sb = new StringBuilder("<table>");
        for (WorkbenchDataItem wbdi : dataList)
        {
            WorkbenchTemplateMappingItem wbtmi = mappings.get(wbdi.getColumnNumber());
            if (wbtmi.getIsExportableToContent())
            {
                sb.append("<tr><td align=\"right\">");
                sb.append(wbtmi.getCaption());
                sb.append("</td><td align=\"left\">");
                sb.append(wbdi.getCellData());
                sb.append("</td></tr>\n");
            }
        } 
        sb.append("</table>\n");
        
        return sb.toString();
    }

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
                        latStr = converter.convert(valStr, GeoRefFormat.D_PLUS_MINUS.name());
                        latitude = Double.parseDouble(latStr);
                    }
                    catch (Exception e)
                    {
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
                        lonStr = converter.convert(valStr, GeoRefFormat.D_PLUS_MINUS.name());
                        longitude = Double.parseDouble(lonStr);
                    }
                    catch (Exception e)
                    {
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

    public String getTitle()
    {
        initExportData();
        return label;
    }

}
