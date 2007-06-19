/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.util.Vector;

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
    public enum Status {None, Valid, Error, Modified}
    
    /**
     * @param config
     */
    public void setConfig(final ConfigureExternalDataIFace config);

    /**
     * @return
     */
    public ConfigureExternalDataIFace getConfig();

    /**
     * @param workbench
     */
    public DataImportIFace.Status getData(final Workbench workbench);
    
    /**
     * @return
     */
    public Status getStatus();
    
    /**
     * @return info on cells truncated during last import
     */
    public Vector<DataImportTruncation> getTruncations();
    
    /**
     * @return messages generated during last import
     */
    public Vector<String> getMessages();
}
