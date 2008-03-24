/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.awt.Color;
import java.util.List;

import edu.ku.brc.af.core.ServiceInfo;
import edu.ku.brc.af.core.ServiceProviderIFace;
import edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL;
import edu.ku.brc.ui.db.ERTICaptionInfo;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 18, 2007
 *
 */
public class QBQueryForIdResultsHQL extends QueryForIdResultsHQL implements ServiceProviderIFace
{
    protected String title;
    protected int    tableId;
    protected String iconName;

    /**
     * @param bannerColor
     * @param title
     * @param iconName
     * @param tableId
     * @param searchTerm
     * @param listOfIds
     */
    public QBQueryForIdResultsHQL(final Color     bannerColor,
                                  final String    title,
                                  final String    iconName,
                                  final int       tableId,
                                  final String    searchTerm,
                                  final List<?>   listOfIds)
    {
        super(null, bannerColor, searchTerm, listOfIds);
        this.title    = title;
        this.tableId  = tableId;
        this.iconName = iconName;
    }
    
    /**
     * @param list
     */
    public void setCaptions(List<ERTICaptionInfo> list)
    {
        this.captions = list;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getDisplayOrder()
     */
    @Override
    public Integer getDisplayOrder()
    {
        return 0;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getIconName()
     */
    @Override
    public String getIconName()
    {
        return iconName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#buildCaptions()
     */
    @Override
    protected void buildCaptions()
    {
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.expresssearch.QueryForIdResultsHQL#getTableId()
     */
    @Override
    public int getTableId()
    {
        return tableId;
    }

    //--------------------------------------------------------------------
    // ServiceProviderIFace Interface
    //--------------------------------------------------------------------
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.ServiceProviderIFace#getServices()
     */
    public List<ServiceInfo> getServices()
    {
        return null;
    }
    
}