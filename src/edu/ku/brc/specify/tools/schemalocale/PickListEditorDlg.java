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
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.busrules.PickListBusRules;
import edu.ku.brc.ui.AddRemoveEditPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.ui.forms.MultiView;

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
    protected JList              plList;
    protected AddRemoveEditPanel arePanel;
    protected List<PickList>     list = null;
    protected PickListBusRules   plBusRules = new PickListBusRules();
    protected Collection         collection = null;
    
    
    /**
     * @param localizableIO
     * @throws HeadlessException
     */
    public PickListEditorDlg(final LocalizableIOIFace localizableIO) throws HeadlessException
    {
        super((Frame)getTopWindow(), 
              getResourceString("PICKLIST_EDITOR"), true, OKCANCELHELP, null, OK_BTN);
        
        this.localizableIO = localizableIO;
        helpContext = "PL_HELP_CONTEXT";
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p:g", "p,2px,f:p:g,2px,p"));
        CellConstraints cc = new CellConstraints();
        
        DefaultListModel model = new DefaultListModel();
        plList = new JList(model);
        
        plList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                if (e.getClickCount() == 2)
                {
                    editPL();
                }
            }
        });
        
        plList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            //@Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    arePanel.setEnabled(plList.getSelectedIndex() > -1);
                }
            }
        });
        
        ActionListener addAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addPL();
            }
            
        };
        ActionListener delAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                delPL();
            }
            
        };
        ActionListener edtAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                editPL();
            }
            
        };
        arePanel = new AddRemoveEditPanel(addAL, delAL, edtAL);
        arePanel.getAddBtn().setEnabled(true);
        
        pb.add(UIHelper.createI18NLabel("PL_PICKLISTS", SwingConstants.CENTER), cc.xy(1, 1));
        JScrollPane sp = new JScrollPane(plList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pb.add(sp, cc.xy(1, 3));
        pb.add(arePanel, cc.xy(1, 5));
        
        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            if (localizableIO != null)
            {
                list = localizableIO.getPickLists(null);
                
            } else
            {
                list = new Vector<PickList>();
                
                String sql = QueryAdjusterForDomain.getInstance().adjustSQL("FROM PickList WHERE collectionId = COLLID");
                List<?> pickLists = session.getDataList(sql);
    
                for (Object obj : pickLists)
                {
                    list.add((PickList)obj);
                }
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
        
        fillModel();
        
        pack();
    }
    
    /**
     * 
     */
    protected void fillModel()
    {
        if (list != null)
        {
            DefaultListModel model = (DefaultListModel)plList.getModel();
            model.removeAllElements();
            
            Collections.sort(list);
            for (PickList pl : list)
            {
                model.addElement(pl);
            }
        }
    }

    /**
     * 
     */
    protected void addPL()
    {
        PickList pickList = new PickList();
        pickList.initialize();
        
        collection.addReference(pickList, "pickLists");
        
        if (editPL(pickList))
        {
            list.add(pickList);
            fillModel();
        }
    }
    
    /**
     * 
     */
    protected void delPL()
    {
        PickList pickList = (PickList)plList.getSelectedValue();
        if (pickList != null)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                plBusRules.okToDelete(pickList, session, this);
                
            } catch (Exception ex)
            {
                //log.error(ex);
                ex.printStackTrace();
                
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
        dlg.setHelpLabel("PL_ITEM_EDITOR");
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
     * 
     */
    protected void editPL()
    {
        PickList selectedPL = (PickList)plList.getSelectedValue();
        
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
    public void doDeleteDataObj(Object dataObj, DataProviderSessionIFace session, boolean doDelete)
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
                
                list.remove(pickList);
                fillModel();
                
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
