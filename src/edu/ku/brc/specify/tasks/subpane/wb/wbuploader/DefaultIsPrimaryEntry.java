/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.lang.reflect.Method;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 *A "DefaultFieldEntry" for the Collector.isPrimary field.
 */
public class DefaultIsPrimaryEntry extends DefaultFieldEntry
{

    public DefaultIsPrimaryEntry(final UploadTable uploadTbl, Class<?> fldClass, Method setter,
            String fldName)
    {
        super(uploadTbl, fldClass, setter, fldName);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.wb.wbuploader.DefaultFieldEntry#getDefaultValue(java.lang.Object[])
     */
    @Override
    protected Object getDefaultValue(Object... params)
    {
        return params[0].equals(0);
    }

}
