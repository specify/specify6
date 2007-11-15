package edu.ku.brc.specify.tasks.subpane;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.af.core.expresssearch.QueryForIdResultsIFace;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace.QueryIFace;
import edu.ku.brc.ui.IconTray;
import edu.ku.brc.ui.forms.FormDataObjIFace;

public class IconTrayResultsPanel extends IconTray implements ESResultsTablePanelIFace
{
    protected ExpressSearchResultsPaneIFace esrPane;
    protected QueryForIdResultsIFace        results;
    protected PropertyChangeListener        propChangeListener;
    protected FormDataObjIFace              prevSelection;
    
    public IconTrayResultsPanel()
    {
        super(IconTray.SINGLE_ROW);
        
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

    public List<Integer> getListOfIds(boolean returnAll)
    {
        return Collections.singletonList(prevSelection.getId());
    }

    public QueryForIdResultsIFace getResults()
    {
        return results;
    }

    public Component getUIComponent()
    {
        return this;
    }

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
            QueryIFace query = session.createQuery(queryString.toString());
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
}
