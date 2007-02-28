/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.exporters;

import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.tasks.ExportTask;

/**
 * @author jstewart
 * @code_status Alpha
 */
public class RecordSetExporterAdapter implements RecordSetExporter
{
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.RecordSetExporter#exportRecordSet(edu.ku.brc.specify.datamodel.RecordSet)
	 */
	public void exportRecordSet(RecordSet data)
    {
	    System.out.println("Exporting " + data.getIdentityTitle());
        System.out.println("Records are from table " + data.getDbTableId());
        System.out.println("Record IDs:");
        for (RecordSetItemIFace rsi: data.getItems())
        {
            System.out.println(rsi.getRecordId());
        }
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.RecordSetExporter#getHandledClasses()
     */
    public Class<?>[] getHandledClasses()
    {
        return new Class<?>[] {CollectionObject.class};
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.RecordSetExporter#getName()
     */
    public String getName()
    {
        return "TestExporter";
    }

    public String getIconName()
    {
        return ExportTask.EXPORT;
    }
}
