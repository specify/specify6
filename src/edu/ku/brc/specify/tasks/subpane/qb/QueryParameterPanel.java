/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
@SuppressWarnings("serial")
public class QueryParameterPanel extends JPanel implements QueryFieldPanelContainerIFace
{
    protected boolean hasPrompts = false;
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getFields()
     */
    //@Override
    public int getFields()
    {
        return queryFields.size();
    }

    /**
     * JGoodies format string used by contained QueryFieldPanels
     */
    protected String columnDefStr;
    protected Vector<QueryFieldPanel> queryFields = null;
  
    public void setQuery(final SpQuery query, final TableTree tblTree, final Hashtable<String, TableTree> ttHash)
    {
    	setQuery(query, tblTree, ttHash, true);
    }
    public void setQuery(final SpQuery query, final TableTree tblTree, final Hashtable<String, TableTree> ttHash, boolean loadQueryFields)
    {
        removeAll();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if (loadQueryFields)
        {
        	queryFields = QueryBldrPane.getQueryFieldPanels(query, this, tblTree, ttHash);
        	add(queryFields.get(0)); //add header panel to ui
        	queryFields.remove(0); //remove it from params list.
        	for (QueryFieldPanel qfp : queryFields)
        	{
        		if (qfp.getQueryField().getIsPrompt())
        		{
        			add(qfp);
        			hasPrompts = true;
        		}
        	}
        }
   }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getAddBtn()
     */
    //@Override
    public JButton getAddBtn()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getColumnDefStr()
     */
    //@Override
    public String getColumnDefStr()
    {
        return columnDefStr;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#getField(int)
     */
    //@Override
    public QueryFieldPanel getField(int index)
    {
        return queryFields.get(index);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#removeQueryFieldItem(edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanel)
     */
    //@Override
    public void removeQueryFieldItem(QueryFieldPanel qfp)
    {
        //Shouldn't ever need to do this.
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#selectQFP(edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanel)
     */
    //@Override
    public void selectQFP(QueryFieldPanel qfp)
    {
        //Shouldn't ever need to do this.
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#setColumnDefStr(java.lang.String)
     */
    //@Override
    public void setColumnDefStr(String columnDefStr)
    {
        this.columnDefStr = columnDefStr;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#isPromptMode()
     */
    //@Override
    public boolean isPromptMode()
    {
        return true;
    }

    /**
     * @return the hasPrompts
     */
    public boolean getHasPrompts()
    {
        return hasPrompts;
    }

    
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#updateAvailableConcepts()
	 */
	@Override
	public void updateAvailableConcepts() 
	{
		//nada
	}
	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#isUpdatingAvailableConcepts()
	 */
	@Override
	public boolean isUpdatingAvailableConcepts() 
	{
		return false;
	}
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#doSearch()
	 */
	@Override
	public void doSearch() 
	{
		//do nothing
	}
	/* (non-Javadoc)
	 * @see edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanelContainerIFace#isAvailableExportFieldName(edu.ku.brc.specify.tasks.subpane.qb.QueryFieldPanel, java.lang.String)
	 */
	@Override
	public boolean isAvailableExportFieldName(QueryFieldPanel qfp, String name) 
	{
		return false;
	}

	
}
