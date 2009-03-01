/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
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
 * Displays a list of DataObjFormatters to choose from.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 5, 2008
 *
 */
public class DataObjFieldFmtListEdtDlg extends FmtListEditorDlgBase
{
    protected DBTableInfo tableInfo;
    
    /**
     * @param frame
     * @param tableInfo
     * @param dataObjFieldFormatMgrCache
     * @param uiFieldFormatterMgrCache
     * @throws HeadlessException
     */
    public DataObjFieldFmtListEdtDlg(final Frame                 frame, 
                                     final DBTableInfo           tableInfo, 
                                     final DataObjFieldFormatMgr dataObjFieldFormatMgrCache,
                                     final UIFieldFormatterMgr   uiFieldFormatterMgrCache) throws HeadlessException
    {
        super(frame, 
                "DOF_DLG_AVAIL_TITLE",
                "DOF_LIST_EDITOR",  // HelpContext
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
                DataObjSwitchFormatter doa = (DataObjSwitchFormatter)value;
                label.setText(doa.toString() + (doa.isDefault() ? " " + UIRegistry.getResourceString("DOF_DEFAULT") : ""));
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
        DataObjSwitchFormatter newFmt = new DataObjSwitchFormatter(null, null, true, false, tableInfo.getClassObj(), "");
        editItem(newFmt, true);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.FmtListEditorDlgBase#createListDataModel()
     */
    @Override
    protected DefaultListModel createListDataModel()
    {
        // list of existing formats
        DefaultListModel model = new DefaultListModel();

        // add available data object formatters
        List<DataObjSwitchFormatter> fmtrs = dataObjFieldFormatMgrCache.getFormatterList(tableInfo.getClassObj());
        Collections.sort(fmtrs, new ComparatorByStringRepresentation<DataObjSwitchFormatter>());
        for (DataObjSwitchFormatter format : fmtrs)
        {
            model.addElement(format);
        }
        return model;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.FmtListEditorDlgBase#deleteSelectedItem()
     */
    @Override
    protected void deleteSelectedItem()
    {
        DataObjSwitchFormatter selectedFormat = (DataObjSwitchFormatter)list.getSelectedValue();
        if (selectedFormat == null)
        {
            return;
        }

        dataObjFieldFormatMgrCache.removeFormatter(selectedFormat);
        
        DefaultListModel model = (DefaultListModel) list.getModel();
        model.removeElement(selectedFormat);
        
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
            DataObjSwitchFormatter dataObjFieldFmt = (DataObjSwitchFormatter)dataObj;
            DataObjSwitchFormatter tempCopy        = isNew ? dataObjFieldFmt : (DataObjSwitchFormatter)dataObjFieldFmt.clone();
            
            DataObjFieldFormatDlg dlg = new DataObjFieldFormatDlg(this, tableInfo, dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache, tempCopy);
            dlg.setVisible(true);
            if (!dlg.isCancelled())
            {
                if (isNew)
                {
                    dataObjFieldFormatMgrCache.addFormatter(dataObjFieldFmt);
                    listModel.addElement(dataObjFieldFmt);
                    
                } else
                {
                    listModel.removeElement(dataObjFieldFmt);
                    dataObjFieldFormatMgrCache.removeFormatter(dataObjFieldFmt);
                    dataObjFieldFormatMgrCache.addFormatter(tempCopy);
                    listModel.addElement(tempCopy);
                }
                setHasChanged(true);
            }
            
        } catch (CloneNotSupportedException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DataObjFieldFmtListEdtDlg.class, ex);
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
        DataObjSwitchFormatter selected = (DataObjSwitchFormatter)list.getSelectedValue();
        DefaultListModel       model    = (DefaultListModel)list.getModel();
        for (int i=0;i<model.getSize();i++)
        {
            DataObjSwitchFormatter dof = (DataObjSwitchFormatter)model.get(i);
            dof.setDefault(dof == selected);
            setHasChanged(true);
        }
        list.repaint();
    }

}
