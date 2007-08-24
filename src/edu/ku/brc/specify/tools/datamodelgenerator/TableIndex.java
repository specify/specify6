/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.datamodelgenerator;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 24, 2007
 *
 */
public class TableIndex
{
    protected String indexName;
    protected String[] columnNames;
    /**
     * @param indexName
     * @param columnNames
     */
    public TableIndex(String indexName, String[] columnNames)
    {
        super();
        this.indexName = indexName;
        this.columnNames = columnNames;
    }
    /**
     * @return the indexName
     */
    public String getIndexName()
    {
        return indexName;
    }
    /**
     * @return the columnNames
     */
    public String[] getColumnNames()
    {
        return columnNames;
    }
    
    
}
