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

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.busrules.PickListBusRules;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.AddRemoveEditPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.forms.MultiView;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jul 6, 2008
 *
 */
public class PickListEditorDlg extends CustomDialog
{

    protected LocalizableIOIFace localizableIO;
    protected JList              plList;
    protected AddRemoveEditPanel arePanel;
    
    
    /**
     * @param localizableIO
     * @throws HeadlessException
     */
    public PickListEditorDlg(LocalizableIOIFace localizableIO) throws HeadlessException
    {
        super((Frame)getTopWindow(), 
              getResourceString(""), true, OKCANCELHELP, null, OK_BTN);
        
        this.localizableIO = localizableIO;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p:g", "p,2px,p"));
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
        JScrollPane sp = new JScrollPane(plList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pb.add(sp, cc.xy(1, 1));
        pb.add(arePanel, cc.xy(1, 3));
        
        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        HelpMgr.setHelpID(getHelpBtn(), getResourceString("PL_HELP_CONTEXT"));
        
        List<PickList> list = localizableIO.getPickLists(null);
        Collections.sort(list);
        for (PickList pl : list)
        {
            model.addElement(pl);
        }
        
        pack();
    }

    /**
     * 
     */
    protected void addPL()
    {
        
    }
    
    /**
     * 
     */
    protected void delPL()
    {
        
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
            dlg.setFormAdjuster(new PickListBusRules());
            dlg.setData(pickList);
            dlg.setModal(true);
            dlg.setVisible(true);
            if (dlg.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
            {
            
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

}
