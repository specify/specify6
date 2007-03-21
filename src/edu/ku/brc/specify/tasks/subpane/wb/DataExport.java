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
     */
    public void writeData(List<?> data) throws Exception;
}
