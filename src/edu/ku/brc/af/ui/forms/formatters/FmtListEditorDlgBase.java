/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBInfoBase;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 11, 2008
 *
 */

public abstract class FmtListEditorDlgBase extends CustomDialog 
{
    protected DBInfoBase                baseInfo;
    protected DataObjFieldFormatMgr     dataObjFieldFormatMgrCache;
    protected UIFieldFormatterMgr       uiFieldFormatterMgrCache;
    
    protected boolean                   hasChanged = false; 
    
    // UI controls
    protected JList                     list;
    protected DefaultListModel          listModel;
    protected DefEditDeleteAddPanel     dedaPanel;
    
    /**
     * @throws HeadlessException
     */
    public FmtListEditorDlgBase(final java.awt.Dialog                owner,
                                final String                titleKey,
                                final String                helpContext,
                                final DBInfoBase            baseInfo, 
                                final DataObjFieldFormatMgr dataObjFieldFormatMgrCache,
                                final UIFieldFormatterMgr   uiFieldFormatterMgrCache) 
        throws HeadlessException
    {
        super(owner, getResourceString(titleKey), true, OK_BTN | HELP_BTN, null);
        this.baseInfo                  = baseInfo;
        this.dataObjFieldFormatMgrCache = dataObjFieldFormatMgrCache;
        this.uiFieldFormatterMgrCache   = uiFieldFormatterMgrCache;
        this.helpContext                = helpContext;
        
        okLabel = getResourceString("CLOSE");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        CellConstraints cc = new CellConstraints();
       
        // table info
        PanelBuilder tblInfoPB          = new PanelBuilder(new FormLayout("p,p:g", "p")/*, new FormDebugPanel()*/);
        JLabel       tableTitleLbl      = createI18NFormLabel("FmtListEditorBase." + (baseInfo instanceof DBTableInfo ? "TABLE" : "FIELD"));
        JLabel       tableTitleValueLbl = createLabel(baseInfo.getTitle());
        tableTitleValueLbl.setBackground(Color.WHITE);
        tableTitleValueLbl.setOpaque(true);
        
        tblInfoPB.add(tableTitleLbl,      cc.xy(1, 1));
        tblInfoPB.add(tableTitleValueLbl, cc.xy(2, 1));
        
        // add available data object formatters
        createList();
        
        ActionListener addAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {   
                addItem();
            }
        };
        
        ActionListener delAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                deleteSelectedItem();
            }
        };
        
        ActionListener edtAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                editItem(list.getSelectedValue(), false);
            }
        };
        
        ActionListener defAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e) 
            {
                setDefaultItem();
            }
        };
        
        String[] toolTipKeys = getDEDAToolTipKeys();
        
        // delete button
        dedaPanel = new DefEditDeleteAddPanel(defAL, edtAL, delAL, addAL, 
                                              toolTipKeys[0], toolTipKeys[1], toolTipKeys[2], toolTipKeys[3]);
        dedaPanel.getAddBtn().setEnabled(true);
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:max(250px;p):g", "p,6px,f:max(250px;p):g,2px,p"));
        
        // lay out components on main panel        
        int y = 1; // leave first row blank 
        pb.add(tblInfoPB.getPanel(), cc.xy(1, y)); y += 2;
        
        pb.add(UIHelper.createScrollPane(list), cc.xy(1,y)); y += 2;

        pb.add(dedaPanel, cc.xy(1,y)); y += 2;
        
        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        updateUIEnabled();

        pack();
    }
    
    /**
     * @return
     */
    protected void createList() 
    {
        listModel = createListDataModel();
        
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        ListCellRenderer cellRenderer = getListCellRenderer();
        if (cellRenderer != null)
        {
            list.setCellRenderer(cellRenderer);
        }
        
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateUIEnabled();
                }
            }
        });
        
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                if (e.getClickCount() == 2)
                {
                    editItem(list.getSelectedValue(), false);
                }
            }
        });
    }
    
    protected ListCellRenderer getListCellRenderer()
    {
        return null;
    }
    
    /**
     * @return
     */
    protected abstract String[] getDEDAToolTipKeys();
    
    /**
     * @return
     */
    protected abstract DefaultListModel createListDataModel();
    
    /**
     * 
     */
    protected abstract void deleteSelectedItem();
    
    /**
     * 
     */
    protected abstract void editItem(final Object dataObj, final boolean isNew);
    
    /**
     * 
     */
    protected abstract void setDefaultItem();
    
    /**
     * 
     */
    protected abstract void addItem();
    
    /**
     * 
     */
    protected void updateUIEnabled()
    {
        Object item = list.getSelectedValue();
        
        dedaPanel.getDelBtn().setEnabled(item != null);
        dedaPanel.getDefBtn().setEnabled(item != null);
        dedaPanel.getEditBtn().setEnabled(item != null);
    }

    /**
     * @return the hasChanged
     */
    public boolean hasChanged()
    {
        return hasChanged;
    }

    /**
     * @param hasChanged the hasChanged to set
     */
    public void setHasChanged(boolean hasChanged)
    {
        if (this.hasChanged != hasChanged)
        {
            setWindowModified(hasChanged);
        }
        this.hasChanged = hasChanged;
        updateUIEnabled();
    }
    
}
