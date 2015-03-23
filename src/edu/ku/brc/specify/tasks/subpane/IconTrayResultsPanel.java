/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.QueryForIdResultsIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.ui.IconTray;

public class IconTrayResultsPanel extends IconTray implements ESResultsTablePanelIFace
{
    protected ExpressSearchResultsPaneIFace esrPane;
    protected QueryForIdResultsIFace        results;
    protected PropertyChangeListener        propChangeListener;
    protected FormDataObjIFace              prevSelection;
    
    public IconTrayResultsPanel(final int defWidth, final int defHeight)
    {
        super(IconTray.SINGLE_ROW, defWidth, defHeight);
        
        iconListWidget.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @SuppressWarnings("synthetic-access")
            public void valueChanged(ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting())
                {
                    return;
                }
                
                FormDataObjIFace selectedRecord = getSelection();
                
                if (propChangeListener != null)
                {
                    PropertyChangeEvent pce = new PropertyChangeEvent(IconTrayResultsPanel.this, "selection", prevSelection, selectedRecord);
                    propChangeListener.propertyChange(pce);
                }
                
                prevSelection = selectedRecord;
            }
        });
    }
    
    public void cleanUp()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#hasResults()
     */
    public boolean hasResults()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getListOfIds(boolean)
     */
    public List<Integer> getListOfIds(boolean returnAll)
    {
        return Collections.singletonList(prevSelection.getId());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getResults()
     */
    public QueryForIdResultsIFace getResults()
    {
        return results;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getUIComponent()
     */
    public Component getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#initialize(edu.ku.brc.specify.tasks.subpane.ExpressSearchResultsPaneIFace, edu.ku.brc.ui.db.QueryForIdResultsIFace)
     */
    public void initialize(ExpressSearchResultsPaneIFace esResPane, QueryForIdResultsIFace esResults)
    {
        this.esrPane = esResPane;
        this.results = esResults;
        
        int tableID = results.getTableId();
        Vector<Integer> resultsIDs = results.getRecIds();
        
        if (resultsIDs.size() > 0)
        {
            // build a query to get all of these records
            DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(tableID);
            StringBuilder queryString = new StringBuilder("SELECT o FROM ");
            queryString.append(tableInfo.getClassName());
            queryString.append(" o WHERE o.id IN (");
            
            for (Integer id: resultsIDs)
            {
                queryString.append(id);
                queryString.append(",");
            }
            // remove the last comma
            queryString.deleteCharAt(queryString.length()-1);
            queryString.append(")");
            
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            QueryIFace query = session.createQuery(queryString.toString(), false);
            List<?> resultRecords = query.list();
            
            for (Object o: resultRecords)
            {
                FormDataObjIFace formObj = (FormDataObjIFace)o;
                addItem(formObj);
            }
        }
    }

    public void setPropertyChangeListener(PropertyChangeListener pcl)
    {
        propChangeListener = pcl;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
    }

    public void expandView()
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.ESResultsTablePanelIFace#getSelectedRows()
     */
    @Override
    public int[] getSelectedRows()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    
}
