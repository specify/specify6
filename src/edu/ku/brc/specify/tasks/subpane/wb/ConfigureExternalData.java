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
public interface ConfigureExternalData
{
    /**
     * configures import/export settings for file.
     * 
     * @param file
     */
    void getConfig(final File file);

     /**
     * does the first row of data contain column names?
     * 
     * @return
     */
    boolean getFirstRowHasHeaders();
    
    /**
     * @param value
     */
    void setFirstRowHasHeaders(final boolean value);

    // 
    /**
     * the columns in the file.
     * 
     * @return
     */
    Vector<ImportColumnInfo> getColInfo();

    /**
     * the file containing the data to be imported.
     * 
     * @return
     */
    File getFile();
    
    /**
     * @return 
     */
    String getFileName();

    /** 
     * if interactive then column headers, separators, etc are obtained from user. else
     * prefs/defaults are used.
     * 
     * @param arg to be or not to be interactive
     */
    void setInteractive(final boolean arg);

    /**
     * @return the properties for the configuration
     */
    Properties getProperties();
    
    /**
     * @param headers captions for the columns headings. 
     */
    void setHeaders(final String[] headers);
}
