/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.exporters;

import java.util.List;
import java.util.Properties;

import edu.ku.brc.specify.datamodel.RecordSet;

/**
 * @author jstewart
 * @code_status Alpha
 */
public interface RecordSetExporter
{
    public String getName();
    public String getIconName();
    public String getDescription();
    public Class<?>[] getHandledClasses();
	public void exportRecordSet(RecordSet data, Properties requestParams);
    public void exportList(List<?> data, Properties requestParams);
}
