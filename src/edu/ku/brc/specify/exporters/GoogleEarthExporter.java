/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.exporters;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.io.File;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.dbsupport.RecordSetLoader;
import edu.ku.brc.specify.tasks.services.KeyholeMarkupGenerator;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.services.GenericKmlGenerator;

/**
 * @author jstewart
 * @code_status Alpha
 */
public class GoogleEarthExporter implements RecordSetExporter
{
    /** Logger for all log messages emitted from this class. */
    private static final Logger log = Logger.getLogger(GoogleEarthExporter.class);
            
    protected KeyholeMarkupGenerator kmlGen = new KeyholeMarkupGenerator();
    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.RecordSetExporter#exportRecordSet(edu.ku.brc.specify.datamodel.RecordSet)
	 */
	public void exportRecordSet(RecordSet data)
    {
        int collObjTableId = DBTableIdMgr.getIdByClassName(CollectionObject.class.getName());
        int collEvtTableId = DBTableIdMgr.getIdByClassName(CollectingEvent.class.getName());
        int dataTableId = data.getDbTableId();

        if (dataTableId == collObjTableId)
        {
            exportCollectionObjectRecordSet(data);
        }
        else if (dataTableId == collEvtTableId)
        {
            exportCollectingEventRecordSet(data);
        }
        else
        {
            throw new RuntimeException("Unsupported data type");
        }
	}
    
    @SuppressWarnings("unchecked")
    public void exportList(List<?> data)
    {
        if (data==null || data.size()==0)
        {
            return;
        }
        
        if (data.get(0).getClass().equals(WorkbenchRow.class))
        {
            exportWorkbenchRowList((List<WorkbenchRow>)data);
        }
    }
    
    protected void exportCollectionObjectRecordSet(@SuppressWarnings("unused") RecordSet data)
    {
        JFrame topFrame = (JFrame)UICacheManager.get(UICacheManager.TOPFRAME);
        Icon icon = IconManager.getIcon(getIconName());
        JOptionPane.showMessageDialog(topFrame, "Not yet implemented", getName() + " data export", JOptionPane.ERROR_MESSAGE, icon);
    }
    
    protected void exportCollectingEventRecordSet(RecordSet data)
    {
        List<Object> records = RecordSetLoader.loadRecordSet(data);
        for (Object o: records)
        {
            CollectingEvent ce = (CollectingEvent)o;
            kmlGen.addCollectingEvent(ce, null);
        }
        File tmpFile;
        try
        {
            tmpFile = File.createTempFile("sp6export", ".kml");
            kmlGen.outputToFile(tmpFile.getAbsolutePath());
            AttachmentUtils.openFile(tmpFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected void exportWorkbenchRowList(List<WorkbenchRow> rows)
    {
        boolean checkForGeoFields = true;
        GenericKmlGenerator kmlGenerator = new GenericKmlGenerator();
        for (WorkbenchRow row: rows)
        {
            // check for geo refs the first time through
            if (checkForGeoFields)
            {
                if (!row.getWorkbench().containsGeoRefData())
                {
                    log.error("Provided record set does not contain geo referenced data");
                    return;
                }
                checkForGeoFields=false;
            }
            
            String name = getNameOfWorkbenchRow(row);
            Pair<Double,Double> geoRef = getGeoRefFromWorkbenchRow(row);
            String htmlDesc = buildHtmlDescFromWorkbenchRow(row);
            kmlGenerator.addPlacemark(geoRef, name, htmlDesc);
        }
        
        File tmpFile;
        try
        {
            tmpFile = File.createTempFile("sp6export", ".kml");
            kmlGenerator.generateKML(tmpFile);
            AttachmentUtils.openFile(tmpFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected String getNameOfWorkbenchRow(WorkbenchRow row)
    {
        return row.getData(0);
    }
    
    protected Pair<Double,Double> getGeoRefFromWorkbenchRow(WorkbenchRow row)
    {
        Workbench wb = row.getWorkbench();
        int localityTableId = DBTableIdMgr.getIdByClassName(Locality.class.getName());
        int lat1Index = wb.getColumnIndex(localityTableId, "latitude1");
        int lon1Index = wb.getColumnIndex(localityTableId, "longitude1");
        //int lat2Index = wb.getColumnIndex(localityTableId, "latitude2");
        //int lon2Index = wb.getColumnIndex(localityTableId, "longitude2");
        String lat1 = row.getData(lat1Index);
        String lon1 = row.getData(lon1Index);
        double lat = Double.parseDouble(lat1);
        double lon = Double.parseDouble(lon1);
        return new Pair<Double,Double>(lat,lon);
    }
    
    protected String buildHtmlDescFromWorkbenchRow(WorkbenchRow row)
    {
        return row.toString() + "<br><a href=\"http://www.google.com/\">Google</a>";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.RecordSetExporter#getHandledClasses()
     */
    public Class<?>[] getHandledClasses()
    {
        return new Class<?>[] {CollectionObject.class, CollectingEvent.class, WorkbenchRow.class};
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.RecordSetExporter#getName()
     */
    public String getName()
    {
        return getResourceString("GoogleEarth");
    }

    public String getIconName()
    {
        return "GoogleEarth";
    }
    
    public String getDescription()
    {
        return getResourceString("GoogleEarth_Description");
    }
}
