/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Color;
import java.util.List;
import java.util.Vector;

import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.db.ERTICaptionInfo;
import edu.ku.brc.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Feb 19, 2008
 *
 */
public class TableSearchResults implements QueryForIdResultsIFace
{
    protected List<ERTICaptionInfo> cols;
    protected String                sql;
    protected DBTableInfo           tableInfo;
    protected boolean               isMultipleSelection = false;

    /**
     * @param tableId
     */
    public TableSearchResults(final DBTableInfo tableInfo, 
                              final List<ERTICaptionInfo> cols)
    {
        this.tableInfo = tableInfo;
        this.cols      = cols;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#cleanUp()
     */
    public void cleanUp()
    {
        cols = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getBannerColor()
     */
    public Color getBannerColor()
    {
        return new Color(30, 144, 255);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getDescription()
     */
    public String getDescription()
    {
        return tableInfo.getDescription();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getDisplayOrder()
     */
    public Integer getDisplayOrder()
    {
        return 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getIconName()
     */
    public String getIconName()
    {
        return tableInfo.getName();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getRecIds()
     */
    public Vector<Integer> getRecIds()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getSearchTerm()
     */
    public String getSearchTerm()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getSQL(java.lang.String, java.util.Vector)
     */
    public String getSQL(String searchTerm, Vector<Integer> ids)
    {
        return sql;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getTableId()
     */
    public int getTableId()
    {
        return tableInfo.getTableId();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getTitle()
     */
    public String getTitle()
    {
        return tableInfo.getTitle();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getVisibleCaptionInfo()
     */
    public List<ERTICaptionInfo> getVisibleCaptionInfo()
    {
        return cols;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isExpanded()
     */
    public boolean isExpanded()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isHQL()
     */
    public boolean isHQL()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setSQL(java.lang.String)
     */
    public void setSQL(String sql)
    {
        this.sql = sql;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#shouldInstallServices()
     */
    public boolean shouldInstallServices()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#enableEditing()
     */
    public boolean enableEditing()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#removeIds(java.util.List)
     */
    public void removeIds(List<Integer> ids)
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#isMultipleSelection()
     */
    public boolean isMultipleSelection()
    {
        return isMultipleSelection;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#setMultipleSelection(boolean)
     */
    public void setMultipleSelection(boolean isMultiple)
    {
        isMultipleSelection = isMultiple;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#getParams()
     */
    //@Override
    public List<Pair<String, Object>> getParams()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.QueryForIdResultsIFace#complete()
     */
    //@Override
    public void complete()
    {
        // nothing to do
    }

    
    
}
