/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.exporters;

import java.util.List;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;

/**
 * @author jstewart
 * @code_status Alpha
 *
 */
public class WebPageExporter implements RecordSetExporter
{
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#exportRecordSet(edu.ku.brc.specify.datamodel.RecordSet)
     */
    public void exportRecordSet(RecordSet data)
    {
        int taxonTableId = DBTableIdMgr.getIdByClassName(Taxon.class.getName());
        int dataTableId = data.getDbTableId();

        if (dataTableId == taxonTableId)
        {
            exportTaxonRecordSet(data);
        }
        else
        {
            throw new RuntimeException("Unsupported data type");
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getDescription()
     */
    public String getDescription()
    {
        return "Creates web pages to represent the holdings of a collection in a taxonomy tree-based layout.";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getHandledClasses()
     */
    public Class<?>[] getHandledClasses()
    {
        return new Class<?>[] {Taxon.class};
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getIconName()
     */
    public String getIconName()
    {
        return "WebPage";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporter#getName()
     */
    public String getName()
    {
        return "Web Page";
    }
    
    protected void exportTaxonRecordSet(@SuppressWarnings("unused") RecordSet taxonRecordSet)
    {
        JFrame topFrame = (JFrame)UICacheManager.get(UICacheManager.TOPFRAME);
        Icon icon = IconManager.getIcon(getIconName());
        JOptionPane.showMessageDialog(topFrame, "Not yet implemented", getName() + " data export", JOptionPane.ERROR_MESSAGE, icon);
    }

    public void exportList(List<?> data)
    {
        // TODO Auto-generated method stub
        
    }
}
