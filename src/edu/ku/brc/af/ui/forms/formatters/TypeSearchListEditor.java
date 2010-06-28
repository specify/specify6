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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.validation.TypeSearchForQueryFactory;
import edu.ku.brc.af.ui.forms.validation.TypeSearchInfo;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jun 1, 2010
 *
 */
public class TypeSearchListEditor extends CustomDialog
{
    protected JList                  list;
    protected EditDeleteAddPanel     edaPanel;
    protected Vector<TypeSearchInfo> itemsList;
    
    /**
     * @throws HeadlessException
     */
    public TypeSearchListEditor() throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), "Query Combobox Editor", true, CustomDialog.OK_BTN, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        setOkLabel(getResourceString("CLOSE"));
        
        super.createUI();

        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,2px,p"));
         
        ActionListener addAL = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                addItem();
            }
        };
        
        ActionListener delAL = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                delItem();
            }
        };
        
        ActionListener edtAL = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                editItem();
            }
        };
        
        itemsList = TypeSearchForQueryFactory.getInstance().getList();
        list      = new JList(itemsList);
        
        edaPanel = new EditDeleteAddPanel(edtAL, delAL, addAL);
        
        CellConstraints cc = new CellConstraints();
        pb.add(UIHelper.createScrollPane(list), cc.xy(1,1));
        pb.add(edaPanel, cc.xy(1,3));
        
        pb.setDefaultDialogBorder();
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateUI();
                }
            }
        });
        list.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                if (e.getClickCount() == 2)
                {
                    editItem();
                }
            }
        });
        
        setSize(300,350);
    }
    
    /**
     * 
     */
    protected void updateUI()
    {
        int inx = list.getSelectedIndex();
        edaPanel.getAddBtn().setEnabled(true);
        if (inx > -1)
        {
            TypeSearchInfo tsi = (TypeSearchInfo)list.getSelectedValue();
            edaPanel.getDelBtn().setEnabled(!tsi.isSystem());
        }
        edaPanel.getEditBtn().setEnabled(inx > -1);
    }
    
    /**
     * 
     */
    private void editItem()
    {
        int inx = list.getSelectedIndex();
        if (inx > -1)
        {
            TypeSearchInfo tsi = (TypeSearchInfo)list.getSelectedValue();
            if (edit(tsi, false))
            {
                TypeSearchForQueryFactory.getInstance().save();
            }
        }
    }
    
    /**
     * 
     */
    private void addItem()
    {
        TypeSearchInfo tsi = new TypeSearchInfo(-1, null, null, null, null, null, null, false);
        while (true)
        {
            if (!edit(tsi, true))
            {
                break;
            }
            if (TypeSearchForQueryFactory.getInstance().getHash().get(tsi.getName()) == null)
            {
                TypeSearchForQueryFactory.getInstance().save();
                break;
            }
        }
        
    }
    
    /**
     * 
     */
    private void delItem()
    {
        int inx = list.getSelectedIndex();
        if (inx > -1)
        {
            TypeSearchInfo tsi = (TypeSearchInfo)list.getSelectedValue();
            if (!tsi.isSystem())
            {
                TypeSearchForQueryFactory.getInstance().remove(tsi);
                TypeSearchForQueryFactory.getInstance().save();
                list.remove(inx);
            }
        }
    }
    
    /**
     * @param tsi
     * @param isNewItem
     * @return
     */
    protected boolean edit(final TypeSearchInfo tsi, final boolean isNewItem)
    {
        final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Dialog)UIRegistry.getMostRecentWindow(),
                "SystemSetup",
                "TypeSearchInfo",
                null,
                getResourceString(getResourceString("EDIT")),
                "OK",
                null,
                null,
                true,
                MultiView.HIDE_SAVE_BTN | MultiView.DONT_ADD_ALL_ALTVIEWS | MultiView.USE_ONLY_CREATION_MODE |
                MultiView.IS_EDITTING);
        dlg.setHelpContext("CHANGE_PWD");
        dlg.setWhichBtns(CustomDialog.OK_BTN | CustomDialog.CANCEL_BTN);
        
        dlg.setFormAdjuster(new FormPane.FormPaneAdjusterIFace() {
            @Override
            public void adjustForm(final FormViewObj fvo)
            {
                adjustDlgForm(tsi, isNewItem, fvo);
            }
        });
        dlg.setData(tsi);
        UIHelper.centerAndShow(dlg);
        
        return !dlg.isCancelled();
    }
    
    /**
     * Configures and setup the form controls in the dialog.
     * @param tsi the current or new item
     * @param isNewItem whether it is a new item
     * @param fvo the form in the dlg
     */
    private void adjustDlgForm(final TypeSearchInfo tsi, final boolean isNewItem, final FormViewObj fvo)
    {
        final ValTextField nameTF      = fvo.getCompById("name");
        final ValTextField dispColsTF  = fvo.getCompById("displayColumns");

        //----------------------- Table List -----------------------------
        final ValComboBox         tableCBX  = fvo.getCompById("tableCBX");
        final Vector<DBTableInfo> tableList = new Vector<DBTableInfo>(DBTableIdMgr.getInstance().getTables());
        Collections.sort(tableList);
        
        tableCBX.setModel(new DefaultComboBoxModel(tableList));
        if (tableList.size() > 0)
        {
            int i   = 0;
            int inx = -1;
            for (DBTableInfo tbl : tableList)
            {
                if (tbl.getTableId() == tsi.getTableId())
                {
                    inx = i;
                    break;
                }
                i++;
            }
            tableCBX.getComboBox().setSelectedIndex(inx);
        }
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (tableList.size() == 0 || !isNewItem)
                {
                    tableCBX.getComboBox().setEnabled(false);
                    nameTF.setEditable(isNewItem);
                    
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (tableList.size() == 0 || !isNewItem)
                            {
                                dispColsTF.requestFocus();
                            }
                        }
                    });
                }
            }
        });

        //----------------------- UI Field Formatter -----------------------------
        final ValComboBox             uiFmtCbx = fvo.getCompById("uiFieldFormatterNameCBX");
        Vector<UIFieldFormatterIFace> uiffList = new Vector<UIFieldFormatterIFace>(UIFieldFormatterMgr.getInstance().getFormatters());
        Collections.sort(uiffList, new Comparator<UIFieldFormatterIFace>()
        {
            @Override
            public int compare(UIFieldFormatterIFace o1, UIFieldFormatterIFace o2)
            {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        
        uiFmtCbx.setModel(new DefaultComboBoxModel(uiffList));
        if (uiffList.size() > 0)
        {
            int i   = 0;
            int inx = -1;
            for (UIFieldFormatterIFace dof : uiffList)
            {
                if (dof.getName().equals(tsi.getDataObjFormatterName()))
                {
                    inx = i;
                    break;
                }
                i++;
            }
            uiFmtCbx.getComboBox().setSelectedIndex(inx);
        } else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    uiFmtCbx.getComboBox().setEnabled(false);
                }
            });
        }
        
        //----------------------- Data Obj Formatter -----------------------------
        Class<?> cls = DBTableIdMgr.getInstance().getInfoById(tsi.getTableId()).getClassObj();
        List<DataObjSwitchFormatter>   list          = DataObjFieldFormatMgr.getInstance().getFormatterList(cls);
        final ValComboBox              dataObjFmtCbx = fvo.getCompById("dataObjFormatterNameCBX");
        Vector<DataObjSwitchFormatter> dofList       = new Vector<DataObjSwitchFormatter>(list);
        dataObjFmtCbx.setModel(new DefaultComboBoxModel(dofList));
        
        if (dofList.size() > 0)
        {
            int i   = 0;
            int inx = -1;
            for (DataObjSwitchFormatter dof : dofList)
            {
                if (dof.getName().equals(tsi.getDataObjFormatterName()))
                {
                    inx = i;
                    break;
                }
                i++;
            }
            dataObjFmtCbx.getComboBox().setSelectedIndex(inx);
        } else
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    dataObjFmtCbx.getComboBox().setEnabled(false);
                }
            });
        }
    }
}
