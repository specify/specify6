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
 * An interface specifying the minimal capabilities of an object capable of exporting a {@link RecordSet} or {@link List}
 * of records in some external format.
 * 
 * @author jstewart
 * @code_status Beta
 */
public interface RecordSetExporter
{
    /**
     * Returns the name of the exporter.
     * 
     * @return the exporter's name
     */
    public String getName();
    
    /**
     * Returns the icon name of the exporter.
     * 
     * @return the exporter's icon's name
     */
    public String getIconName();
    
    /**
     * Returns a short text description of the exporter.
     * 
     * @return a short text description of the exporter.
     */
    public String getDescription();
    
    /**
     * Returns an array of datatypes handled by the exporter.
     * 
     * @return an array of datatypes handled by the exporter.
     */
    public Class<?>[] getHandledClasses();
	
    /**
     * Exports the given {@link RecordSet}.
     * 
	 * @param data the {@link RecordSet} to export
	 * @param requestParams configuration parameters used by the exporter
	 * @throws Exception an error occurrred while exporting the {@link RecordSet}
	 */
	public void exportRecordSet(RecordSet data, Properties requestParams) throws Exception;
    
    /**
     * Exports the given {@link List}.
     * 
     * @param data the {@link List} to export
     * @param requestParams configuration parameters used by the exporter
     * @throws Exception an error occurrred while exporting the {@link List}
     */
    public void exportList(List<?> data, Properties requestParams) throws Exception;
}
