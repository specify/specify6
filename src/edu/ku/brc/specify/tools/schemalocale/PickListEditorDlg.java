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
package edu.ku.brc.specify.tools.schemalocale;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.af.ui.forms.validation.ValPlainTextDocument;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.busrules.PickListBusRules;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jul 6, 2008
 *
 */
public class PickListEditorDlg extends CustomDialog implements BusinessRulesOkDeleteIFace
{

    protected LocalizableIOIFace localizableIO;
    protected JList              sysPLList;
    protected EditDeleteAddPanel sysEDAPanel;
    
    protected JList              plList;
    protected EditDeleteAddPanel edaPanel;
    
    protected PickListBusRules   plBusRules = new PickListBusRules();
    protected Collection         collection = null;
    
    // Transient
    protected JList              pickListCache = null; // needed when deleting a PL
    protected boolean            doImportExport;
    
    protected DBTableInfo        tableInfo    = null;
    protected DBFieldInfo        fieldInfo    = null;
    
    protected boolean            isChanged    = false;
    protected Vector<PickList>   newPickLists = new Vector<PickList>();
    
    /**
     * @param localizableIO
     * @throws HeadlessException
     */
    public PickListEditorDlg(final LocalizableIOIFace localizableIO,
                             final boolean doImportExport) throws HeadlessException
    {
        super((Frame)getTopWindow(), 
              getResourceString("PICKLIST_EDITOR"), true, OKHELP, null, OK_BTN);
        
        this.localizableIO = localizableIO;
        this.helpContext   = "PL_HELP_CONTEXT";
        this.okLabel       = getResourceString("CLOSE");
        this.doImportExport = doImportExport;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p:g", "p,2px,f:p:g,2px,p, 10px, p,2px,f:p:g,2px,p,10px"));
        CellConstraints cc = new CellConstraints();
        
        plList   = new JList();
        edaPanel = configureList(plList, false, doImportExport);
        
        sysPLList   = new JList();
        sysEDAPanel = configureList(sysPLList, true, false);
        
        int y = 1;
        pb.add(UIHelper.createI18NLabel("PL_PICKLISTS_SYS", SwingConstants.CENTER), cc.xy(1, y)); y+= 2;
        pb.add(UIHelper.createScrollPane(sysPLList), cc.xy(1, y)); y+= 2;
        pb.add(sysEDAPanel, cc.xy(1, y)); y+= 2;
        
        pb.add(UIHelper.createI18NLabel("PL_PICKLISTS_USR", SwingConstants.CENTER), cc.xy(1, y)); y+= 2;
        pb.add(UIHelper.createScrollPane(plList), cc.xy(1, y)); y+= 2;
        pb.add(edaPanel, cc.xy(1, y)); y+= 2;
        
        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        pack();
    }
    
    public boolean hasChanged()
    {
        return isChanged;
    }
    
    /**
     * @param tableInfo the tableInfo to set
     */
    public void setTableInfo(DBTableInfo tableInfo)
    {
        this.tableInfo = tableInfo;
    }

    /**
     * @param fieldInfo the fieldInfo to set
     */
    public void setFieldInfo(DBFieldInfo fieldInfo)
    {
        this.fieldInfo = fieldInfo;
    }

    /**
     * @return any newly created picklists
     */
    public Vector<PickList> getNewPickLists()
    {
        return newPickLists;
    }
    
    /**
     * @param list
     * @param isSystemPL
     * @return
     */
    protected EditDeleteAddPanel configureList(final JList   list, 
                                               final boolean isSystemPL,
                                               final boolean doImportExportArg)
    {
        
        ActionListener addAL = null;
        ActionListener delAL = null;
        
        if (!isSystemPL)
        {
            if (doImportExportArg)
            {
                /*addAL = new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        importPL(list);
                    }
                    
                };
                
                delAL = new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        exportPL(list);
                    }
                }; */
            } else
            {
                addAL = new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        addPL(list);
                    }
                };
                
                delAL = new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        delPL(list);
                    }
                };
            }
        }
        
        ActionListener edtAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                editPL(list);
            }
        };
        
        final EditDeleteAddPanel edaPnl = new EditDeleteAddPanel(edtAL, delAL, addAL);
        if (edaPnl.getAddBtn() != null)
        {
            edaPnl.getAddBtn().setEnabled(true);
        }
        
        /*if (doImportExport && addAL != null && delAL != null)
        {
            edaPnl.getAddBtn().setIcon(IconManager.getIcon("Import16"));
            edaPnl.getDelBtn().setIcon(IconManager.getIcon("Export16"));
        }*/
        
        List<PickList> items = null;
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            if (localizableIO != null)
            {
                items = localizableIO.getPickLists(null);
                
            } else
            {
                Vector<PickList> plItems = new Vector<PickList>();
                
                String sqlStr = "FROM PickList WHERE collectionId = COLLID AND isSystem = " + (isSystemPL ? 1 : 0);
                String sql = QueryAdjusterForDomain.getInstance().adjustSQL(sqlStr);
                List<?> pickLists = session.getDataList(sql);
    
                for (Object obj : pickLists)
                {
                    plItems.add((PickList)obj);
                }
                items = plItems;
            }
            
            java.util.Collections.sort(items);
            
            collection = AppContextMgr.getInstance().getClassObject(Collection.class);
            collection = (Collection)session.getData("FROM Collection WHERE collectionId = "+collection.getId());
            collection.getPickLists().size();
            
        }  catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PickListEditorDlg.class, ex);
            //log.error(ex);
            // XXX error dialog
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        if (items == null)
        {
            // catastrophic error
            // need error dlg
            return null;
        }
        
        DefaultListModel model = new DefaultListModel();
        for (PickList pl : items)
        {
            model.addElement(pl);
        }
        list.setModel(model);
        
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                if (e.getClickCount() == 2)
                {
                    editPL(list);
                }
            }
        });
        
        list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    if (edaPnl.getDelBtn() != null)
                    {
                        edaPnl.getDelBtn().setEnabled(list.getSelectedIndex() > -1);
                    }
                    edaPnl.getEditBtn().setEnabled(list.getSelectedIndex() > -1);
                }
            }
        });
        
        return edaPnl;
    }
    
    /**
     * @param list
     */
    protected void addPL(final JList list)
    {
        PickList pickList = new PickList();
        pickList.initialize();
        
        collection.addReference(pickList, "pickLists");
        
        if (editPL(pickList))
        {
            ((DefaultListModel)list.getModel()).addElement(pickList);
            newPickLists.add(pickList);
        }
    }
    
    /**
     * @param list
     */
    protected void delPL(final JList list)
    {
        PickList pickList = (PickList)list.getSelectedValue();
        if (pickList != null)
        {
            DataProviderSessionIFace session = null;
            try
            {
                pickListCache = list;
                session       = DataProviderFactory.getInstance().createSession();
                plBusRules.okToDelete(pickList, session, this);
                isChanged     = true;
                newPickLists.remove(pickList);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PickListEditorDlg.class, ex);
                //log.error(ex);
                pickListCache = null;
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
    }
    
    /**
     * @return
     */
    protected boolean importPL(@SuppressWarnings("unused")
    final JList listArg)
    {
        isChanged = true;
        return true;
    }
    
    /**
     * @return
     */
    protected boolean exportPL(@SuppressWarnings("unused") final JList listArg)
    {
        return true;
    }
    
    /**
     * Edits and saves a PickList 
     * @param pickList the PickList to be edited
     * @return true if it was saved ok
     */
    protected boolean editPL(final PickList pickList)
    {
        String tableName = pickList.getTableName();
        String fieldName = pickList.getFieldName();
        if (StringUtils.isNotEmpty(tableName) && StringUtils.isNotEmpty(fieldName))
        {
            tableInfo = DBTableIdMgr.getInstance().getInfoByTableName(tableName);
            if (tableInfo != null)
            {
                fieldInfo = tableInfo.getFieldByName(fieldName);
            }
        }
        plBusRules.setTableInfo(tableInfo);
        plBusRules.setFieldInfo(fieldInfo);
        
        ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                "SystemSetup",
                "PickList",
                null,
                getResourceString("PL_EDT_TITLE"),
                getResourceString("SAVE"),
                null, // className,
                null, // idFieldName,
                true, // isEdit,
                MultiView.HIDE_SAVE_BTN);
        
        dlg.setHelpContext("PL_ITEM_EDITOR");
        dlg.setFormAdjuster(plBusRules);
        
        MultiView    multiView = dlg.getMultiView();
        ValTextField tf        = multiView.getKids().get(0).getCurrentViewAsFormViewObj().getCompById("value");
        ArrayList<DocumentListener> listeners = new ArrayList<DocumentListener>();
        for (DocumentListener dl : ((ValPlainTextDocument)tf.getDocument()).getDocumentListeners())
        {
            listeners.add(dl);
        }
        
        ValPlainTextDocument doc;
        if (fieldInfo != null && fieldInfo.getType().equals("java.lang.Byte"))
        {
            doc = tf.new JFormattedDoc(tf, 3, 0, 255);
        } else
        {
            doc = tf.new JFormattedDoc(tf, UIFieldFormatterField.FieldType.anychar, 64);
        }
        tf.setDocument(doc);
        for (DocumentListener dl : listeners)
        {
            doc.addDocumentListener(dl);
        }
        
        dlg.setData(pickList);
        dlg.setModal(true);
        dlg.setVisible(true);
        
        if (dlg.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            dlg.getMultiView().getCurrentViewAsFormViewObj().traverseToGetDataFromForms();
            //boolean isOK = PickList.save(true, pickList);
            boolean isOK = multiView.getCurrentViewAsFormViewObj().saveObject();
            if (isOK)
            {
                isChanged = true;
                dispatchChangeNotification(pickList);
            }
            return isOK;
        }
        return false;
    }
    
    /**
     * Notifies the PickList Cache (Factory) that the PickList has changed.
     * @param pickList the pickList that has changed
     */
    private void dispatchChangeNotification(final PickList pickList)
    {
        CommandDispatcher.dispatch(new CommandAction("PICKLIST", "CLEAR", pickList.getName()));
    }
    
    /**
     * @param list
     */
    protected void editPL(final JList list)
    {
        PickList selectedPL = (PickList)list.getSelectedValue();
        
        if (selectedPL != null)
        {
            PickList pickList = PickList.getDataObj(PickList.class, selectedPL.getId());
            if (pickList != null)
            {
                if (editPL(pickList))
                {
                    DefaultListModel model = ((DefaultListModel)list.getModel());
                    int inx = model.indexOf(selectedPL);
                    ((DefaultListModel)list.getModel()).removeElement(selectedPL);
                    model.insertElementAt(pickList, inx);
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#applyButtonPressed()
     */
    @Override
    protected void applyButtonPressed()
    {
        super.applyButtonPressed();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cancelButtonPressed()
     */
    @Override
    protected void cancelButtonPressed()
    {
        super.cancelButtonPressed();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace#doDeleteDataObj(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, boolean)
     */
    @Override
    public void doDeleteDataObj(final Object dataObj, 
                                final DataProviderSessionIFace session, 
                                final boolean doDelete)
    {
        if (doDelete)
        {
            try
            {
                PickList pickList = (PickList)dataObj;
                
                pickList = session.merge(pickList);
                
                pickList.getCollection().removeReference(pickList, "pickLists");
                
                session.beginTransaction();
                session.delete(pickList);
                session.commit();
                
                if (pickListCache != null) // should never be null
                {
                    ((DefaultListModel)pickListCache.getModel()).remove(pickListCache.getSelectedIndex());
                    pickListCache = null;
                }
                
                dispatchChangeNotification(pickList);
                
                UIRegistry.displayLocalizedStatusBarText("PL_DELETED", pickList.getName());
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PickListEditorDlg.class, ex);
                //log.error(ex);
                
            } finally 
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
    }
}
