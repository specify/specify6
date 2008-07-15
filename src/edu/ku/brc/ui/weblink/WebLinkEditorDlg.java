/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.ui.weblink;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.ViewFactory;

/**
 * This class is used for editing a single WebLink Object and all of it's arguments. 
 * The arguments can be references to fields in a data object or they can be values
 * that are prompted for with a popup dialog.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Apr 13, 2008
 *
 */
public class WebLinkEditorDlg extends CustomDialog
{
    protected JTextField                nameTF;
    protected JTextField                baseUrlTF;
    protected JTextArea                 descTA;
    protected JTable                    table;
    protected WebLinkArgsTableModel     model;

    protected boolean                   isEdit     = false;
    protected WebLinkDef                webLinkDef = null;
    protected Vector<WebLinkDefArg>     args       = new Vector<WebLinkDefArg>();
    protected Hashtable<String, String> fields     = new Hashtable<String, String>();
    
    protected boolean                   hasChanged = false; 
    
    // Type Format 
    protected String format = ""; //$NON-NLS-1$
    protected int    curInx = 0;
    
    /**
     * @param webLinkDef the WebLink to be edited
     * @throws HeadlessException
     */
    public WebLinkEditorDlg(final WebLinkDef webLinkDef) throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), getResourceString("WebLinkArgDlg.WEB_LNK_EDTR"), true, OKCANCELHELP, null); // I18N //$NON-NLS-1$
        
        this.webLinkDef = webLinkDef;
        
        for (WebLinkDefArg arg : webLinkDef.getArgs())
        {
            try
            {
                WebLinkDefArg wlda = (WebLinkDefArg)arg.clone();
                args.add(wlda);
            } catch (CloneNotSupportedException ex) {}
        }
        
        helpContext = "WEBLNK_ARGS_EDITOR";
    }

    /**
     * @param isEdit the isEdit to set
     */
    public void setEdit(boolean isEdit)
    {
        this.isEdit = isEdit;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        nameTF    = UIHelper.createTextField();
        baseUrlTF = UIHelper.createTextField();
        descTA    = UIHelper.createTextArea();
        
        if (isEdit)
        {
            ViewFactory.changeTextFieldUIForDisplay(nameTF, true);
        }
        
        descTA.setRows(5);
        
        model = new WebLinkArgsTableModel();
        table = new JTable(model);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder rightPB = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,2px,p, 4px,p,2px,p, 4px,p,2px,p,4px,p,2px,p")); //$NON-NLS-1$ //$NON-NLS-2$
        
        rightPB.add(createLabel(getResourceString("WebLinkArgDlg.NAME")+":", SwingConstants.RIGHT), cc.xy(1, 1)); //$NON-NLS-1$ //$NON-NLS-2$
        rightPB.add(nameTF, cc.xy(3, 1));
        
        rightPB.add(createLabel(getResourceString("WebLinkArgDlg.URL")+":", SwingConstants.RIGHT), cc.xy(1, 3)); //$NON-NLS-1$ //$NON-NLS-2$
        rightPB.add(baseUrlTF, cc.xy(3, 3));
        
        rightPB.add(createLabel(getResourceString("WebLinkArgDlg.DESC")+":", SwingConstants.RIGHT), cc.xy(1, 5)); //$NON-NLS-1$ //$NON-NLS-2$
        JScrollPane sp = new JScrollPane(descTA, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightPB.add(sp, cc.xy(3, 5));
        
        rightPB.add(createLabel(getResourceString("WebLinkArgDlg.FIELDS")+":", SwingConstants.CENTER), cc.xy(1, 7)); //$NON-NLS-1$ //$NON-NLS-2$
        sp = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightPB.add(sp, cc.xy(3, 7));
        
        rightPB.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        TableColumn promptCol = table.getColumnModel().getColumn(2);
        promptCol.setCellEditor(new DefaultCellEditor(new JCheckBox()));
        //promptCol.setCellRenderer(cellRenderer)
        UIHelper.makeTableHeadersCentered(table, false);
        
        contentPanel = rightPB.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        pack();
        
        setDataIntoUI();
        
        DocumentListener docLis = new DocumentListener() {
            protected void changed()
            {
                hasChanged = true;
                parseForFields();
                enableUI();
            }
            public void changedUpdate(DocumentEvent e)
            {
                changed();
            }
            public void insertUpdate(DocumentEvent e)
            {
                changed();
            }
            public void removeUpdate(DocumentEvent e)
            {
                changed();
            }
        };
        
        nameTF.getDocument().addDocumentListener(docLis);
        baseUrlTF.getDocument().addDocumentListener(docLis);
        
        enableUI();
        
        pack();
    }
    
    /**
     * 
     */
    protected void parseForFields()
    {
        String baseStr    = baseUrlTF.getText();
        int    baseStrLen = baseStr.length();
        int    fmtLen     = format.length();
        if (baseStrLen == 0)
        {
            curInx = 0;
            format = ""; //$NON-NLS-1$
            
        } else if (baseStrLen == fmtLen-1)
        {
            String s = StringUtils.chomp(baseStr);
            if (!format.equals(s))
            {
                curInx = 0;
                format = ""; //$NON-NLS-1$
            }
        }
        
        if (curInx == 0)
        {
            fields.clear();
            args.clear();
            model.fire();
        }
        
        int inx = 0;
        do
        {
            int nxtInx = baseStr.indexOf('[', inx);
            if (nxtInx == -1)
            {
                // error 
                break;
            }
            nxtInx++;
            int endInx = baseStr.indexOf(']', nxtInx);
            if (endInx > -1)
            {
                //endInx--;
                String field = baseStr.substring(nxtInx, endInx);
                if (field.length() > 0)
                {
                    if (fields.get(field) == null)
                    {
                        fields.put(field, field);
                        model.addItem(field, StringUtils.capitalize(field), true);
                        inx = endInx+1;
                        
                    } else
                    {
                        // error 
                        break;
                    }
                } else
                {
                 // error 
                    break;
                }
                
            } else
            {
                // error 
                break;
            }
        }    
        while (true);
        
        format = baseStr;
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
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        getDataFromUI();
        
        webLinkDef.getArgs().clear();
        webLinkDef.getArgs().addAll(args);
        
        super.okButtonPressed();
    }

    /**
     * 
     */
    protected void setDataIntoUI()
    {
        nameTF.setText(webLinkDef.getName());
        baseUrlTF.setText(webLinkDef.getBaseURLStr());
        descTA.setText(webLinkDef.getDesc());
        
        //parseForFields();
    }
    
    /**
     * 
     */
    protected void getDataFromUI()
    {
        webLinkDef.setName(nameTF.getText());
        webLinkDef.setBaseURLStr(baseUrlTF.getText());
        webLinkDef.setDesc(descTA.getText());
    }
    
    /**
     * 
     */
    protected void enableUI()
    {
        boolean okEnable = true;
        if (nameTF.getText().length() == 0 || baseUrlTF.getText().length() == 0)
        {
            okEnable = false;
        }
        
        okBtn.setEnabled(okEnable);
    }
    

    /**
     * @return the hasChanged
     */
    public boolean hasChanged()
    {
        return hasChanged;
    }
    
    //--------------------------------------------------------
    //-- Table Model
    //--------------------------------------------------------
    class WebLinkArgsTableModel extends DefaultTableModel
    {
        protected String[] colHeaders = null;
        
        /**
         * 
         */
        public WebLinkArgsTableModel()
        {
            super();
        }
        
        public void fire()
        {
            fireTableDataChanged();
        }
        
        /**
         * @param name
         * @param isPrompt
         */
        public void addItem(final String name, final String title, final boolean isPrompt)
        {
            args.add(new WebLinkDefArg(name, title, isPrompt));
            fireTableDataChanged();
            hasChanged = true;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return columnIndex < 2 ? String.class : Boolean.class;
        }

        /**
         * 
         */
        public void removeItem()
        {
            int index = table.getSelectedRow();
            if (index > -1)
            {
                args.remove(index);
            }
            fireTableDataChanged();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            if (colHeaders == null)
            {
                colHeaders = new String[] {
                        getResourceString("WebLinkArgDlg.COLNAME"),  //$NON-NLS-1$
                        getResourceString("WebLinkArgDlg.COLTITLE"),  //$NON-NLS-1$
                        getResourceString("WebLinkArgDlg.COLPROMPT")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            return colHeaders.length;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return colHeaders[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return args == null ? 0 : args.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            WebLinkDefArg arg = args.get(row);
            switch (column)
            {
                case 0 : return arg.getName();
                case 1 : return arg.getTitle();
                case 2 : return arg.isPrompt();
            }
            return ""; //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return column > 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object value, int row, int column)
        {
            WebLinkDefArg arg = args.get(row);
            switch (column)
            {
                case 0 : 
                    break;
                case 1 : 
                    arg.setTitle((String)value);
                    hasChanged = true;
                    break;
                case 2 : 
                    arg.setPrompt((Boolean)value);
                    hasChanged = true;
                    break;
            }
        }
    }
}
