/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import edu.ku.brc.specify.datamodel.SpQuery;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class QueryParameterPanel extends JPanel implements QueryFieldPanelContainerIFace
{
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getFields()
     */
    @Override
    public int getFields()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * JGoodies format string used by contained QueryFieldPanels
     */
    protected String columnDefStr;
    protected Vector<QueryFieldPanel> queryFields = null;
    
    public void setQuery(final SpQuery query, final TableTree tblTree, final Hashtable<String, TableTree> ttHash)
    {
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        queryFields = QueryBldrPane.getQueryFieldPanels(query, this, tblTree, ttHash);
        add(queryFields.get(0)); //add header panel to ui
        queryFields.remove(0); //remove it from params list.
        for (QueryFieldPanel qfp : queryFields)
        {
            if (qfp.getQueryField().getIsPrompt())
            {
                add(qfp);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getAddBtn()
     */
    @Override
    public JButton getAddBtn()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getColumnDefStr()
     */
    @Override
    public String getColumnDefStr()
    {
        return columnDefStr;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getField(int)
     */
    @Override
    public QueryFieldPanel getField(int index)
    {
        return queryFields.get(index);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#removeQueryFieldItem(edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanel)
     */
    @Override
    public void removeQueryFieldItem(QueryFieldPanel qfp)
    {
        //Shouldn't ever need to do this.
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#selectQFP(edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanel)
     */
    @Override
    public void selectQFP(QueryFieldPanel qfp)
    {
        //Shouldn't ever need to do this.
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#setColumnDefStr(java.lang.String)
     */
    @Override
    public void setColumnDefStr(String columnDefStr)
    {
        this.columnDefStr = columnDefStr;
    }
}
