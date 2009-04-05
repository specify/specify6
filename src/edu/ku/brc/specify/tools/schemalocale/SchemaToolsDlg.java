/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.schemalocale;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 3, 2007
 *
 */
public class SchemaToolsDlg extends CustomDialog
{
    protected JButton      editSchemaBtn      = createButton(getResourceString("SL_EDIT_SCHEMA"));
    protected JButton      removeLocaleBtn    = createButton(getResourceString("SL_REMOVE_SCHEMA_LOC"));
    protected JButton      exportSchemaLocBtn = createButton(getResourceString("SL_EXPORT_SCHEMA_LOC"));
    protected JButton      importSchemaLocBtn = createButton(getResourceString("SL_IMPORT_SCHEMA_LOC"));
    protected JList        localeList;
    protected Byte         schemaType;
    protected DBTableIdMgr tableMgr;

    /**
     * @param frame
     * @param schemaType
     * @param tableMgr
     * @throws HeadlessException
     */
    public SchemaToolsDlg(final Frame        frame, 
                          final Byte         schemaType,
                          final DBTableIdMgr tableMgr) throws HeadlessException
    {
        super(frame, getResourceString("SL_TOOLS_TITLE"), true, OKHELP, null);
        this.schemaType = schemaType;
        this.tableMgr   = tableMgr;
        
        helpContext = "SL_TOOLS_HELP_CONTEXT";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        setOkLabel(getResourceString("CLOSE"));
        
        super.createUI();
        

        Vector<DisplayLocale> localeDisplays = new Vector<DisplayLocale>();
        for (Locale locale : SchemaLocalizerDlg.getLocalesInUseInDB(schemaType))
        {
            localeDisplays.add(new DisplayLocale(locale));
        }
        
        localeList = new JList(localeDisplays);
        JScrollPane sp   = new JScrollPane(localeList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        CellConstraints cc = new CellConstraints();
        
        PanelBuilder builder   = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,2px,p,16px,p,4px,p,8px,p,10px"));
        builder.addSeparator(getResourceString("SL_LOCALES_IN_USE"), cc.xywh(1, 1, 3, 1));
        builder.add(sp, cc.xywh(1,3,3,1));
        
        builder.addSeparator(getResourceString("SL_TASKS"), cc.xywh(1, 5, 3, 1));
        builder.add(editSchemaBtn,      cc.xy(1,7));
        builder.add(removeLocaleBtn,    cc.xy(3,7));
        builder.add(exportSchemaLocBtn, cc.xy(1,9));
        builder.add(importSchemaLocBtn, cc.xy(3,9));
        
        builder.setDefaultDialogBorder();
        
        contentPanel = builder.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        enableBtns(false);
        
        localeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                localeSelected();
            }
        });
        localeList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me)
            {
                if (me.getClickCount() == 2)
                {
                    editSchema();
                }
            }
        });
        
        editSchemaBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0)
            {
                editSchema();
            }
        });
        
        removeLocaleBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0)
            {
                removeSchemaLocale();
            }
        });
        
        exportSchemaLocBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0)
            {
                exportSchemaLocales();
            }
        });
        
        importSchemaLocBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0)
            {
                JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), getResourceString("SL_NOT_IMPLEMENTED"));
            }
        });
        
        pack();
    }
    
    /**
     * @param enable
     */
    protected void enableBtns(final boolean enable)
    {
        
        editSchemaBtn.setEnabled(enable);
        removeLocaleBtn.setEnabled(localeList.getModel().getSize() > 1);
        exportSchemaLocBtn.setEnabled(enable);
    }

    /**
     * 
     */
    protected void localeSelected()
    {
        DisplayLocale dispLocale = (DisplayLocale)localeList.getSelectedValue();
        if (dispLocale != null)
        {
            enableBtns(true);
            
        } else
        {
            enableBtns(false);
        }
    }
    
    
    /**
     * 
     */
    protected void editSchema()
    {
        SwingUtilities.invokeLater(new Runnable() {

            public void run()
            {
                okButtonPressed();
                
                DisplayLocale dispLocale = (DisplayLocale)localeList.getSelectedValue();
                if (dispLocale != null)
                {
                    Locale currLocale = SchemaI18NService.getCurrentLocale();
                    
                    SchemaI18NService.setCurrentLocale(dispLocale.getLocale());

                    SchemaLocalizerDlg dlg = new SchemaLocalizerDlg((Frame)UIRegistry.getTopWindow(), schemaType, tableMgr); // MUST BE MODAL!
                    dlg.setVisible(true);
                    SchemaI18NService.setCurrentLocale(currLocale);
                    
                    isCancelled = true; // We need to do this here so we don't get a StatsPane we don't want

                    if (dlg.wasSaved())
                    {
                        CommandDispatcher.dispatch(new CommandAction(BaseTask.APP_CMD_TYPE, BaseTask.APP_REQ_EXIT));
                    } else
                    {
                        ContextMgr.getTaskByName("Startup").requestContext();
                    }
                }
            }
        });
    }
    
    /**
     * 
     */
    protected void removeSchemaLocale()
    {
        
    }
    
    /**
     * 
     */
    protected void exportSchemaLocales()
    {
        
    }
}
