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
public interface DataImport
{
    /**
     * @param config
     */
    public void setConfig(ConfigureDataImport config);

    /**
     * @return
     */
    public ConfigureDataImport getConfig();

    /**
     * @param workbench
     */
    public void getData(Workbench workbench);
}
