/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.List;

import edu.ku.brc.dbsupport.DataProviderSessionIFace;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public interface DataExport
{
    /**
     * @param config
     */
    public void setConfig(ConfigureExternalData config);

    /**
     * @return
     */
    public ConfigureExternalData getConfig();
    
    /**
     * @param data list of workbench rows to export
     * @param session - a session for the workbench
     * @param closeSession - if true session is closed after writing
     */
    public void writeData(final List<?> data, final DataProviderSessionIFace session, final boolean closeSession) throws Exception;
}
