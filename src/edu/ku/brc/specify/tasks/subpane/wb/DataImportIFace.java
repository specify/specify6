/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import edu.ku.brc.specify.datamodel.Workbench;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *interface for workbench data import
 */
public interface DataImportIFace
{
    public enum Status {None, Valid, Error}
    
    /**
     * @param config
     */
    public void setConfig(ConfigureExternalDataIFace config);

    /**
     * @return
     */
    public ConfigureExternalDataIFace getConfig();

    /**
     * @param workbench
     */
    public DataImportIFace.Status getData(Workbench workbench);
    
    /**
     * @return
     */
    public Status getStatus();
}
