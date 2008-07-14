/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.weblink;

import com.thoughtworks.xstream.XStream;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 13, 2008
 *
 */
public class WebLinkUsedBy
{
    protected String tableName;
    protected String fieldName;
    
    /**
     * @param tableName
     * @param fieldName
     */
    public WebLinkUsedBy(String tableName, String fieldName)
    {
        this.tableName = tableName;
        this.fieldName = fieldName;
    }
    
    public WebLinkUsedBy(String tableName)
    {
        this.tableName = tableName;
        this.fieldName = null;
    }
    /**
     * @return the tableName
     */
    public String getTableName()
    {
        return tableName;
    }
    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }
    /**
     * @return the fieldName
     */
    public String getFieldName()
    {
        return fieldName;
    }
    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
    }
    
    /**
     * @param xstream
     */
    public static void configXStream(final XStream xstream)
    {
        xstream.alias("usedby", WebLinkUsedBy.class); //$NON-NLS-1$

    }  
}
