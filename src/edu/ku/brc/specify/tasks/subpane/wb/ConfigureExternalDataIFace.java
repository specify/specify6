/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import java.io.File;
import java.util.Properties;
import java.util.Vector;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 */
public interface ConfigureExternalDataIFace
{
    public enum Status {None, Valid, Error, Cancel}
    
    /**
     * Returns whether the configuration was valid.
     * @return the status.
     */
    public Status getStatus();
    
    /**
     * configures import/export settings for file.
     * 
     * @param file
     */
    public void readConfig(final File file);

     /**
     * does the first row of data contain column names?
     * 
     * @return
     */
    public boolean getFirstRowHasHeaders();
    
    /**
     * @param value
     */
    public void setFirstRowHasHeaders(boolean value);

    // 
    /**
     * the columns in the file.
     * 
     * @return
     */
    public Vector<ImportColumnInfo> getColInfo();

    /**
     * the file containing the data to be imported.
     * 
     * @return
     */
    public File getFile();
    
    /**
     * @return 
     */
    public String getFileName();

    /** 
     * if interactive then column headers, separators, etc are obtained from user. else
     * prefs/defaults are used.
     * 
     * @param arg to be or not to be interactive
     */
    public void setInteractive(boolean arg);

    /**
     * @return the properties for the configuration
     */
    public Properties getProperties();
    
    /**
     * @param headers captions for the columns headings. 
     */
    public void setHeaders(String[] headers);
}
