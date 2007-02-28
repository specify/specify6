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

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.dbsupport.RecordSetLoader;
import edu.ku.brc.specify.tasks.services.KeyholeMarkupGenerator;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author jstewart
 * @code_status Alpha
 */
public class GoogleEarthExporter implements RecordSetExporter
{
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.RecordSetExporter#getHandledClasses()
     */
    public Class<?>[] getHandledClasses()
    {
        return new Class<?>[] {CollectionObject.class, CollectingEvent.class};
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
