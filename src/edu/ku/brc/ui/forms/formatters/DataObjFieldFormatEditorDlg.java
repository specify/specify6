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
package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createList;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.ComparatorByStringRepresentation;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Aug 5, 2008
 *
 */
public class DataObjFieldFormatEditorDlg extends CustomDialog
{
   
    protected DBTableInfo                               tableInfo;
    protected DataObjFieldFormatMgr                     dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr                       uiFieldFormatterMgrCache;
    
    // UI
    protected JList                                     formatList;
    protected DefaultListModel                          listModel;
    protected EditDeleteAddPanel                        edaPanel;
        
    /**
     * @param parentDlg
     * @param tableInfo
     * @param dataObjFieldFormatMgrCache
     * @param uiFieldFormatterMgrCache
     * @throws HeadlessException
     */
    public DataObjFieldFormatEditorDlg(final Frame                 parentDlg, 
                                       final DBTableInfo           tableInfo, 
                                       final DataObjFieldFormatMgr dataObjFieldFormatMgrCache,
                                       final UIFieldFormatterMgr   uiFieldFormatterMgrCache) throws HeadlessException
    {
        super(parentDlg, getResourceString("DOF_DLG_TITLE"), true, OK_BTN, null);
        
        this.tableInfo                   = tableInfo;
        this.dataObjFieldFormatMgrCache  = dataObjFieldFormatMgrCache;
        this.uiFieldFormatterMgrCache    = uiFieldFormatterMgrCache;
        
        this.helpContext                 = "DOF_LIST_EDITOR"; 

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        okLabel = getResourceString("CLOSE");
        
        // TODO Auto-generated method stub
        super.createUI();
        
        populateFormatterList();

        ActionListener addAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addNewFormatter();
            }
        };

        ActionListener delAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                deleteSelectedFormatter();
            }
        };

        ActionListener editAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                editFormatter((DataObjSwitchFormatter)formatList.getSelectedValue(), false);
            }
        };

        // control panel
        edaPanel = new EditDeleteAddPanel(addAL, delAL, editAL);
        edaPanel.getAddBtn().setEnabled(true);
        
        JLabel tableTitleLbl = createLabel(getResourceString("FFE_TABLE") + ": ");
        JLabel tableTitleValueLbl = createLabel(tableInfo.getTitle());
        tableTitleValueLbl.setBackground(Color.WHITE);
        tableTitleValueLbl.setOpaque(true);

        CellConstraints cc = new CellConstraints();
        
        PanelBuilder tblInfoPB = new PanelBuilder(new FormLayout("p,p", "p"));
        tblInfoPB.add(tableTitleLbl, cc.xy(1, 1));
        tblInfoPB.add(tableTitleValueLbl, cc.xy(2, 1));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:max(200px;p):g", "p,2px,f:max(200px;p):g,5px,p"));
        pb.add(tblInfoPB.getPanel(), cc.xy(1, 1));
        pb.add(UIHelper.createScrollPane(formatList), cc.xy(1, 3));
        pb.add(edaPanel, cc.xy(1, 5));
        
        pb.setDefaultDialogBorder();
        
        contentPanel   = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        pack();
    }
    
    /**
     * @return
     */
    protected List<DataObjSwitchFormatter> populateFormatterList()
    {
        // list of existing formats
        listModel = new DefaultListModel();

        // add available data object formatters
        List<DataObjSwitchFormatter> fmtrs = dataObjFieldFormatMgrCache.getFormatterList(tableInfo.getClassObj());
        Collections.sort(fmtrs, new ComparatorByStringRepresentation<DataObjSwitchFormatter>());
        for (DataObjSwitchFormatter format : fmtrs)
        {
            listModel.addElement(format);
        }

        formatList = createList(listModel);
        formatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        addFormatListSelectionListener();
        addFormatListMouseListener();

        return fmtrs;
    }
    
    /**
     * @param dataObjFieldFmt
     */
    protected void editFormatter(final DataObjSwitchFormatter dataObjFieldFmt, final boolean isNew)
    {
        try
        {
            DataObjSwitchFormatter tempCopy = isNew ? dataObjFieldFmt : (DataObjSwitchFormatter)dataObjFieldFmt.clone();
            
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
            }
            
        } catch (CloneNotSupportedException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    protected void addNewFormatter()
    {
        DataObjSwitchFormatter newFmt = new DataObjSwitchFormatter(null, null, true, true, tableInfo.getClassObj(), "");
        editFormatter(newFmt, true);
    }

    /**
     * 
     */
    protected void deleteSelectedFormatter()
    {
        DataObjSwitchFormatter selectedFormat = getSelectedFormatter();
        if (selectedFormat == null)
        {
            return;
        }

        dataObjFieldFormatMgrCache.removeFormatter(selectedFormat);
        
        int              index = formatList.getSelectedIndex();
        DefaultListModel model = (DefaultListModel) formatList.getModel();
        model.removeElement(selectedFormat);

        if (model.getSize() == 0)
        {
            return;
        }

        // else
        index = (index >= model.getSize()) ? index - 1 : index;
        formatList.setSelectedIndex(index);
    }

    /**
     * @return
     */
    public DataObjSwitchFormatter getSelectedFormatter()
    {
        // get current formatter
        Object value = formatList.getSelectedValue();
        if (!(value instanceof DataObjSwitchFormatter))
        {
            return null;
        }

        DataObjSwitchFormatter fmt = (DataObjSwitchFormatter) value;
        return fmt;
    }

    /**
     * 
     */
    private void addFormatListMouseListener()
    {
        MouseAdapter mAdp = new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    int index = formatList.locationToIndex(e.getPoint());
                    formatList.ensureIndexIsVisible(index);
                    editFormatter((DataObjSwitchFormatter)formatList.getSelectedValue(), false);
                }
            }
        };

        formatList.addMouseListener(mAdp);
    }

    /**
     * 
     */
    private void addFormatListSelectionListener()
    {
        ListSelectionListener formatListSL = new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (e.getValueIsAdjusting())
                {
                    return;
                }
                updateUIEnabled();
            }
        };

        formatList.addListSelectionListener(formatListSL);
    }
    
    /**
     * 
     */
    protected void updateUIEnabled()
    {
        edaPanel.getDelBtn().setEnabled(formatList.getSelectedIndex() != -1);
        edaPanel.getEditBtn().setEnabled(formatList.getSelectedIndex() != -1);
    }
}
