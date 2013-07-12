/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms.formatters;

import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.UIRegistry;
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
              "DOA_DLG_AVAIL_TITLE",
              "DOA_LIST_EDITOR", 
              tableInfo, 
              dataObjFieldFormatMgrCache, 
              uiFieldFormatterMgrCache);
        
        this.tableInfo = tableInfo;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.FmtListEditorDlgBase#getListCellRenderer()
     */
    @Override
    protected ListCellRenderer getListCellRenderer()
    {
        return new DefaultListCellRenderer()
        {
            @Override
            public Component getListCellRendererComponent(JList listArg,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus)
            {
                JLabel label = (JLabel)super.getListCellRendererComponent(listArg, value, index, isSelected, cellHasFocus);
                DataObjAggregator agg = (DataObjAggregator)value;
                label.setText(agg.toString() + (agg.isDefault() ? " " + UIRegistry.getResourceString("DOA_DEFAULT") : ""));
                return label;
            }
            
        };
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
        }
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
                    if (listModel.size() == 0)
                    {
                        tempCopy.setDefault(true);
                    }
                    dataObjFieldFormatMgrCache.addAggregator(tempCopy);
                    listModel.addElement(tempCopy);
                    
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjAggListEdtDlg.class, ex);
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
