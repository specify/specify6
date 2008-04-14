/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.weblink;

import static edu.ku.brc.ui.UIHelper.createLabel;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 13, 2008
 *
 */
public class WebLinkArgDlg extends CustomDialog
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
    
    // Type Format 
    protected String format = "";
    protected int    curInx = 0;
    
    /**
     * @throws HeadlessException
     */
    public WebLinkArgDlg(final WebLinkDef webLinkDef) throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), "Web Link Editor", true, OKCANCELHELP, null); // I18N
        
        this.webLinkDef = webLinkDef;
        
        for (WebLinkDefArg arg : webLinkDef.getArgs())
        {
            try
            {
                WebLinkDefArg wlda = (WebLinkDefArg)arg.clone();
                args.add(wlda);
            } catch (CloneNotSupportedException ex) {}
        }
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
        
        DocumentListener docLis = new DocumentListener() {
            public void changedUpdate(DocumentEvent e)
            {
                parseForFields();
                enableUI();
            }
            public void insertUpdate(DocumentEvent e)
            {
                parseForFields();
                enableUI(); 
            }
            public void removeUpdate(DocumentEvent e)
            {
                parseForFields();
                enableUI(); 
            }
        };
        
        nameTF.getDocument().addDocumentListener(docLis);
        baseUrlTF.getDocument().addDocumentListener(docLis);
        
        baseUrlTF.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent arg0)
            {
                parseForFields();
                super.focusLost(arg0);
            }
            
        });
        
        descTA.setRows(5);
        
        model = new WebLinkArgsTableModel();
        table = new JTable(model);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder rightPB = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,2px,p, 4px,p,2px,p, 4px,p,2px,p,4px,p,2px,p"));
        
        rightPB.add(createLabel("Name:", SwingConstants.RIGHT), cc.xy(1, 1));
        rightPB.add(nameTF, cc.xy(3, 1));
        
        rightPB.add(createLabel("Base URL:", SwingConstants.RIGHT), cc.xy(1, 3));
        rightPB.add(baseUrlTF, cc.xy(3, 3));
        
        rightPB.add(createLabel("Desc:", SwingConstants.RIGHT), cc.xy(1, 5));
        JScrollPane sp = new JScrollPane(descTA, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightPB.add(sp, cc.xy(3, 5));
        
        rightPB.add(createLabel("Fields", SwingConstants.CENTER), cc.xy(1, 7));
        sp = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        rightPB.add(sp, cc.xy(3, 7));
        
        rightPB.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        TableColumn promptCol = table.getColumnModel().getColumn(1);
        promptCol.setCellEditor(new DefaultCellEditor(new JCheckBox()));
        //promptCol.setCellRenderer(cellRenderer)
        UIHelper.makeTableHeadersCentered(table, false);
        
        contentPanel = rightPB.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        pack();
        
        setDataIntoUI();
        
        enableUI();
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
            format = "";
            
        } else if (baseStrLen == fmtLen-1)
        {
            String s = StringUtils.chomp(baseStr);
            if (!format.equals(s))
            {
                curInx = 0;
                format = "";
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
                        model.addItem(field, true);
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
        
        parseForFields();
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
    
    
    //--------------------------------------------------------
    class WebLinkArgsTableModel extends DefaultTableModel
    {
        protected String[]              colHeaders = {"Name", "Prompt"}; //I18N
        //protected Vector<WebLinkDefArg> args       = null;
        
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
        public void addItem(final String name, final boolean isPrompt)
        {
            args.add(new WebLinkDefArg(name, isPrompt)); // I18N
            fireTableDataChanged();

        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return columnIndex == 0 ? String.class : Boolean.class;
        }

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
            return column == 0 ? arg.getName() : arg.getPrompt();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return column == 1;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object value, int row, int column)
        {
            WebLinkDefArg arg = args.get(row);
            if (column == 1)
            {
                arg.setPrompt((Boolean)value);
            }
        }
    }
}
