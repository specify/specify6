/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.lang.reflect.Method;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 * Stores default values for (probably only required) fields not in uploading dataset.
 */
public class DefaultFieldEntry
{
    /**
     * The upload table that created this entry.
     */
    protected final UploadTable uploadTbl;
    /**
     * The Java class of the field being uploaded to.
     */
    protected Class<?>          fldClass;
    /**
     * The method in tblClass that is used set values to the field being uploaded to.
     */
    protected Method            setter;
    /**
     * Default arg for setter member.
     */
    protected Object[]          defaultValue;
    /**
     * The name of the field being uploaded to.
     */
    protected String            fldName;

    /**
     * @param fldClass
     * @param setter
     * @param defaultValue
     * @param fldName
     */
    public DefaultFieldEntry(final UploadTable uploadTbl, Class<?> fldClass, Method setter,
            String fldName)
    {
        super();
        this.uploadTbl = uploadTbl;
        this.fldClass = fldClass;
        this.setter = setter;
        this.defaultValue = new Object[1];
        defaultValue[0] = null;
        this.fldName = fldName;
    }

    /**
     * @param params
     * @return the default value Object
     */
    protected Object getDefaultValue(Object... params)
    {
        return defaultValue[0];
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public final void setDefaultValue(Object defaultValue)
    {
        this.defaultValue[0] = defaultValue;
    }

    /**
     * @return the fldClass
     */
    public final Class<?> getFldClass()
    {
        return fldClass;
    }

    /**
     * @return the fldName
     */
    public final String getFldName()
    {
        return fldName;
    }

    /**
     * @return the setter
     */
    public final Method getSetter()
    {
        return setter;
    }

    public boolean isDefined()
    {
        return defaultValue[0] != null;
    }

    /**
     * @return the uploadTbl
     */
    public final UploadTable getUploadTbl()
    {
        return uploadTbl;
    }
}
