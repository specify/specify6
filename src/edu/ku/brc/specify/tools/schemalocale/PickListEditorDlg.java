/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
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
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.busrules.PickListBusRules;
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
    
    /**
     * @param localizableIO
     * @throws HeadlessException
     */
    public PickListEditorDlg(final LocalizableIOIFace localizableIO) throws HeadlessException
    {
        super((Frame)getTopWindow(), 
              getResourceString("PICKLIST_EDITOR"), true, OKHELP, null, OK_BTN);
        
        this.localizableIO = localizableIO;
        this.helpContext   = "PL_HELP_CONTEXT";
        this.okLabel       = getResourceString("CLOSE");
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
        edaPanel = configureList(plList, false);
        
        sysPLList   = new JList();
        sysEDAPanel = configureList(sysPLList, true);
        
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
    
    /**
     * @param list
     * @param isSystemPL
     * @return
     */
    protected EditDeleteAddPanel configureList(final JList list, final boolean isSystemPL)
    {
        
        ActionListener addAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addPL(list);
            }
            
        };
        
        ActionListener delAL = null;
        if (!isSystemPL)
        {
            delAL = new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    delPL(list);
                }
            };
        }
            
        ActionListener edtAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                editPL(list);
            }
        };
        
        final EditDeleteAddPanel arePnl = new EditDeleteAddPanel(edtAL, delAL, addAL);
        arePnl.getAddBtn().setEnabled(true);
        
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
            
            collection = AppContextMgr.getInstance().getClassObject(Collection.class);
            collection = (Collection)session.getData("FROM Collection WHERE collectionId = "+collection.getId());
            collection.getPickLists().size();
            
        }  catch (Exception ex)
        {
            //log.error(ex);
            ex.printStackTrace();
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
            //@Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    arePnl.getDelBtn().setEnabled(list.getSelectedIndex() > -1);
                    arePnl.getEditBtn().setEnabled(list.getSelectedIndex() > -1);
                }
            }
        });
        
        return arePnl;
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
                session = DataProviderFactory.getInstance().createSession();
                plBusRules.okToDelete(pickList, session, this);
                
            } catch (Exception ex)
            {
                //log.error(ex);
                ex.printStackTrace();
                pickListCache = null;
            }
        }
    }
    
    /**
     * @param pickList
     * @return
     */
    protected boolean editPL(final PickList pickList)
    {
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
        dlg.setData(pickList);
        dlg.setModal(true);
        dlg.setVisible(true);
        if (dlg.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            dlg.getMultiView().getCurrentViewAsFormViewObj().traverseToGetDataFromForms();
            return savePL(pickList);
        }
        return false;
    }
    
    /**
     * @param pickList
     */
    protected boolean savePL(final PickList pickList)
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            //collection = session.merge(collection);
            session.saveOrUpdate(pickList);
            session.commit();
            
            return true;
            
        } catch (Exception ex)
        {
            //log.error(ex);
            ex.printStackTrace();
            
        } finally 
        {
            if (session != null)
            {
                session.close();
            }
        }
        return false;
    }
    
    /**
     * @param list
     */
    protected void editPL(final JList list)
    {
        PickList selectedPL = (PickList)list.getSelectedValue();
        
        if (selectedPL != null)
        {
            PickList pickList   = null;
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                pickList = (PickList)session.getData("FROM PickList WHERE id = "+selectedPL.getId());
                
            } catch (Exception ex)
            {
                //log.error(ex);
                ex.printStackTrace();
                
            } finally 
            {
                if (session != null)
                {
                    session.close();
                }
            }
            
            if (pickList != null)
            {
                editPL(pickList);
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
                
                UIRegistry.displayLocalizedStatusBarText("PL_DELETED", pickList.getName());
                
            } catch (Exception ex)
            {
                //log.error(ex);
                ex.printStackTrace();
                
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
