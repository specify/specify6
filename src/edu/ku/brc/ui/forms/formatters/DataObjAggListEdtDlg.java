package edu.ku.brc.ui.forms.formatters;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;

import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.util.ComparatorByStringRepresentation;

/**
 * @author Ricardo
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class DataObjAggListEdtDlg extends FmtListEditorDlgBase 
{
    protected DBTableInfo tableInfo;
    
    /**
     * @throws HeadlessException
     */
    public DataObjAggListEdtDlg(final Frame                 frame, 
                                final DBTableInfo           tableInfo, 
                                final DataObjFieldFormatMgr dataObjFieldFormatMgrCache,
                                final UIFieldFormatterMgr   uiFieldFormatterMgrCache) 
        throws HeadlessException
    {
        super(frame, 
              "DOA_DLG_TITLE",
              "DOA_LIST_EDITOR", 
              tableInfo, 
              dataObjFieldFormatMgrCache, 
              uiFieldFormatterMgrCache);
        
        this.tableInfo = tableInfo;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.FmtListEditorDlgBase#addItem()
     */
    @Override
    protected void addItem()
    {
        DataObjAggregator agg = new DataObjAggregator();
        agg.setDataClass(tableInfo.getClassObj());
        
        editItem(agg, true);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.FmtListEditorDlgBase#createListDataModel()
     */
    @Override
    protected DefaultListModel createListDataModel()
    {
        DefaultListModel model = new DefaultListModel();
        
        List<DataObjAggregator> aggs = dataObjFieldFormatMgrCache.getAggregatorList(tableInfo.getClassObj());
        Collections.sort(aggs, new ComparatorByStringRepresentation<DataObjAggregator>()); 
        for (DataObjAggregator agg : aggs)
        {
            model.addElement(agg);
        };
        return model;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.FmtListEditorDlgBase#deleteSelectedItem()
     */
    @Override
    protected void deleteSelectedItem()
    {
        DataObjAggregator agg = (DataObjAggregator)list.getSelectedValue();
        listModel.removeElement(agg);
        dataObjFieldFormatMgrCache.removeAggregator(agg);
        setHasChanged(true);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.FmtListEditorDlgBase#editItem(java.lang.Object, boolean)
     */
    @Override
    protected void editItem(Object dataObj, boolean isNew)
    {
        try
        {
            DataObjAggregator agg      = (DataObjAggregator)dataObj;
            DataObjAggregator tempCopy = isNew ? agg : (DataObjAggregator)agg.clone();
            
            DataObjAggregatorDlg dlg = new DataObjAggregatorDlg(this, tableInfo, dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache, tempCopy);
            dlg.setVisible(true);
            
            if (!dlg.isCancelled() && dlg.hasChanged())
            {
                if (isNew)
                {
                    dataObjFieldFormatMgrCache.addAggregator(agg);
                    listModel.addElement(agg);
                    
                } else
                {
                    listModel.removeElement(agg);
                    dataObjFieldFormatMgrCache.removeAggregator(agg);
                    dataObjFieldFormatMgrCache.addAggregator(tempCopy);
                    listModel.addElement(tempCopy);
                }
                setHasChanged(true);
            }
            
        } catch (CloneNotSupportedException ex)
        {
            ex.printStackTrace();
        }

    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.FmtListEditorDlgBase#getDEDAToolTipKeys()
     */
    @Override
    protected String[] getDEDAToolTipKeys()
    {
        return new String[]  { "", "", "", ""};
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.FmtListEditorDlgBase#setDefaultItem()
     */
    @Override
    protected void setDefaultItem()
    {
        DataObjAggregator selected = (DataObjAggregator)list.getSelectedValue();
        DefaultListModel model    = (DefaultListModel)list.getModel();
        for (int i=0;i<model.getSize();i++)
        {
            DataObjAggregator agg = (DataObjAggregator)model.get(i);
            agg.setDefault(agg == selected);
        }
        setHasChanged(true);
        list.repaint();
    }
}
