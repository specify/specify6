/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.web;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace;
import edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 22, 2008
 *
 */
public class ResultsCollector implements ESResultsTablePanelIFace
{

    public ResultsCollector()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#cleanUp()
     */
    public void cleanUp()
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#expandView()
     */
    public void expandView()
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getListOfIds(boolean)
     */
    public List<Integer> getListOfIds(boolean returnAll)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getResults()
     */
    public QueryForIdResultsIFace getResults()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getUIComponent()
     */
    public Component getUIComponent()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#hasResults()
     */
    public boolean hasResults()
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#initialize(edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace, edu.ku.brc.ui.db.QueryForIdResultsIFace)
     */
    public void initialize(final ExpressSearchResultsPaneIFace esrPane, 
                           final QueryForIdResultsIFace results)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#setPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void setPropertyChangeListener(PropertyChangeListener pcl)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
}