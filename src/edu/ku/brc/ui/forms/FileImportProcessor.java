/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.ui.forms;

import java.io.File;

import edu.ku.brc.specify.datamodel.Attachment;

/**
 *
 * @code_status Alpha
 * @author jstewart
 */
public class FileImportProcessor
{
    protected static FileImportProcessor instance;
    
    protected FileImportProcessor()
    {
        // nothing
    }
    
    public synchronized static FileImportProcessor getInstance()
    {
        if (instance==null)
        {
            instance = new FileImportProcessor();
        }
        return instance;
    }
    
    public boolean importFileIntoRecord(FormDataObjIFace record, File file)
    {
        if (record instanceof Attachment)
        {
            Attachment a = (Attachment)record;
            a.setOrigFilename(file.getAbsolutePath());
            return true;
        }
        return false;
    }
}
