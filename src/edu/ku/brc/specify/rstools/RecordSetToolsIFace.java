/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.rstools;

import java.util.List;
import java.util.Properties;

import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;

/**
 * An interface specifying the minimal capabilities of an object capable of 
 * processing a RecordSet to do something a {@link RecordSet} or {@link List}
 * of records in some external format.
 * 
 * @author jstewart
 * 
 * @code_status Beta
 */
public interface RecordSetToolsIFace
{
    /**
     * Returns the name of the tool.
     * 
     * @return the tool's name
     */
    public abstract String getName();
    
    /**
     * Returns the icon name of the tool.
     * 
     * @return the tool's icon's name
     */
    public abstract String getIconName();
    
    /**
     * Returns a short text description of the tool. (This is used in to build the pane display).
     * 
     * @return a short text description of the tool.
     */
    public abstract String getDescription();
    
    /**
     * Returns an array of datatypes handled by the tool.
     * 
     * @return an array of datatypes handled by the tool.
     */
    public abstract Class<?>[] getHandledClasses();
	
    /**
     * Exports the given {@link RecordSet}.
     * 
	 * @param recordSet the {@link RecordSet} to process
	 * @param requestParams configuration parameters used by the tool
	 * @throws Exception an error occurrred while exporting the {@link RecordSet}
	 */
	public abstract void processRecordSet(RecordSetIFace recordSet, Properties requestParams) throws Exception;
    
    /**
     * Exports the given {@link List}.
     * 
     * @param data the {@link List} to process
     * @param requestParams configuration parameters used by the tool
     * @throws Exception an error occurrred while exporting the {@link List}
     */
    public abstract void processDataList(List<?> data, Properties requestParams) throws Exception;
    
    /**
     * @return whether the export should be placed in the UI (the sidebar).
     */
    public abstract boolean isVisible();
    
    /**
     * @return returns a list of table ids that this tool can work on.
     */
    public int[] getTableIds();
}
