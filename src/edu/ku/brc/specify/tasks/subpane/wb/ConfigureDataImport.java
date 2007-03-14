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

import java.util.*;

import java.io.File;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public interface ConfigureDataImport
{
   //configures import settings for file
    void getConfig(File  file);
   
   //does the first row of data contain column names?
   boolean getFirstRowHasHeaders();
   
   //the columns in the file
   Vector<ImportColumnInfo> getColInfo();   
   
   //the file containing the data to be imported
   File getFile();
   
   //if interactive then column headers, separators, etc are obtained from user. else prefs/defaults are used
   void setInteractive(final boolean arg); 
}
