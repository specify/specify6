/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.weblink;

import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.ui.BiColorTableCellRenderer;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

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
    protected DBTableInfo               tableInfo;
    protected JTextField                nameTF;
    protected JTextField                baseUrlTF;
    protected JTextArea                 descTA;
    protected JTable                    table;
    protected WebLinkArgsTableModel     model;
    protected JTable                    availableFields;
    protected AvailFieldsTableModel     avModel         = null;
    protected Vector<String>            avFieldList     = new Vector<String>();
    
    protected Vector<String>            availFieldNames = null;
    
    protected boolean                   isEdit     = false;
    protected WebLinkDef                webLinkDef = null;
    protected Vector<WebLinkDefArg>     args       = new Vector<WebLinkDefArg>();
    protected Hashtable<String, String> fields     = new Hashtable<String, String>();
    protected Hashtable<String, String> verifyHash = new Hashtable<String, String>();
    protected Hashtable<String, String> titleHash  = new Hashtable<String, String>();
    
    protected Hashtable<String, DBFieldInfo> fieldInfoHash = new Hashtable<String, DBFieldInfo>();
    
    protected boolean                   hasChanged = false; 
    
    // Type Format 
    protected String  format  = ""; //$NON-NLS-1$
    protected int     curInx  = 0;
    protected boolean isParsingError      = false;
    protected boolean isParsingIncomplete = false;
    protected Color   txtFGColor;
    protected Color   txtBGColor;
    protected Color   errColor            = new Color(255, 220, 220);
    
    /**
     * @param webLinkDef the WebLink to be edited
     * @throws HeadlessException
     */
    public WebLinkEditorDlg(final WebLinkDef webLinkDef,
                            final DBTableInfo tableInfo) throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), getResourceString("WebLinkArgDlg.WEB_LNK_EDTR"), true, OKCANCELHELP, null); //$NON-NLS-1$
        
        this.webLinkDef = webLinkDef;
        this.tableInfo  = tableInfo;
        
        availFieldNames = new Vector<String>();
        if (tableInfo != null)
        {
            for (DBFieldInfo fi : tableInfo.getFields())
            {
                if (fi.getType().equals("java.lang.String"))
                {
                    fieldInfoHash.put(fi.getName(), fi);
                    availFieldNames.add(fi.getName());
                }
            }
            
            List<String> treeFieldNames = DBTableIdMgr.getInstance().getTreeFieldNames(tableInfo);
            if (treeFieldNames != null)
            {
                for (String fName : treeFieldNames)
                {
                    availFieldNames.add(fName);
                }
            }
            avModel = new AvailFieldsTableModel();
            Collections.sort(availFieldNames);
            availableFields = new JTable(avModel);
        }
        
        for (WebLinkDefArg arg : webLinkDef.getArgs())
        {
            try
            {
                WebLinkDefArg wlda  = (WebLinkDefArg)arg.clone();
                String        fName = wlda.getName();
                if (tableInfo != null)
                {
                    boolean isField = fieldInfoHash.get(fName) != null;
                    wlda.setField(isField);
                    wlda.setPrompt(!isField);
                }
                if ( wlda.getTitle() != null)
                {
                    titleHash.put(fName, wlda.getTitle());
                }
                args.add(wlda);
                
            } catch (CloneNotSupportedException ex) {}
        }
        
        helpContext = "WEBLNK_ARGS_EDITOR";
    }
    
    /**
     * 
     */
    protected void adjustAvailableJList()
    {
        if (avModel != null)
        {
            avFieldList.clear();
            verifyHash.clear();
            for (int i=0;i<model.getRowCount();i++)
            {
                verifyHash.put((String)model.getValueAt(i, 0), "X");
            }
            
            for (String fName : availFieldNames)
            {
                if (verifyHash.get(fName) == null)
                {
                    avFieldList.addElement(fName);
                }
            }
            avModel.fire();
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
        baseUrlTF = UIHelper.createTextField(25);
        descTA    = UIHelper.createTextArea();
        
        descTA.setLineWrap(true);
        descTA.setWrapStyleWord(true);
        
        txtFGColor = nameTF.getForeground();
        txtBGColor = nameTF.getBackground();
        
        if (isEdit)
        {
            ViewFactory.changeTextFieldUIForDisplay(nameTF, true);
        }
        
        descTA.setRows(5);
        
        model = new WebLinkArgsTableModel();
        table = new JTable(model);
        table.setDefaultRenderer(String.class, new BiColorTableCellRenderer(true));
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder rightPB = new PanelBuilder(new FormLayout("p,2px,f:p:g",  //$NON-NLS-1$
                      "p,2px,p, 4px,p,2px,200px" + (tableInfo != null ? ",2px,200px" : ""))); //$NON-NLS-1$
        
        rightPB.add(createI18NFormLabel("WebLinkArgDlg.NAME"), cc.xy(1, 1)); //$NON-NLS-1$ //$NON-NLS-2$
        rightPB.add(nameTF, cc.xy(3, 1));
        
        rightPB.add(createI18NFormLabel("WebLinkArgDlg.URL"), cc.xy(1, 3)); //$NON-NLS-1$ //$NON-NLS-2$
        rightPB.add(baseUrlTF, cc.xy(3, 3));
        
        rightPB.add(createI18NFormLabel("WebLinkArgDlg.DESC"), cc.xy(1, 5)); //$NON-NLS-1$ //$NON-NLS-2$
        rightPB.add(UIHelper.createScrollPane(descTA), cc.xy(3, 5));
        
        rightPB.add(createI18NFormLabel("WebLinkArgDlg.FIELDS"), cc.xy(1, 7)); //$NON-NLS-1$ //$NON-NLS-2$
        rightPB.add(UIHelper.createScrollPane(table), cc.xy(3, 7));
        
        if (tableInfo != null)
        {
            rightPB.add(UIHelper.createScrollPane(availableFields), cc.xy(3, 9));
        }
        
        rightPB.setDefaultDialogBorder();
        
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
        
        adjustAvailableJList();
        
        enableUI();
        
        pack();
    }
    
    /**
     * 
     */
    protected void setURLToError()
    {
        Toolkit.getDefaultToolkit().beep();
        //baseUrlTF.setForeground(txtBGColor);
        baseUrlTF.setBackground(errColor);
        isParsingError = true;
        baseUrlTF.repaint();
        enableUI();
    }
    
    /**
     * 
     */
    protected void parseForFields()
    {
        if (isParsingError)
        {
            //baseUrlTF.setForeground(txtFGColor);
            baseUrlTF.setBackground(txtBGColor);
            isParsingError = false;
            enableUI();
        } 
        
        boolean wasIncomplete = isParsingIncomplete;
        
        
        fields.clear();
        args.clear();
        model.fire();
        
        String baseStr = baseUrlTF.getText();
        
        Pattern pattern = Pattern.compile("<.*?>");
        Matcher matcher = pattern.matcher(baseStr);
        while (matcher.find())
        {
            String token = matcher.group(0);
            if (token.length() > 2)
            {
                token = token.substring(1, token.length()-1);
                if (StringUtils.contains(token, ">") || StringUtils.contains(token, "<"))
                {
                    setURLToError();
                    return;
                }
                if (tableInfo == null || fieldInfoHash.get(token) != null)
                {
                    if (tableInfo != null && token.equals("this"))
                    {
                        setURLToError();
                        return;
                    }
                    
                    fields.put(token, token);
                    String titleStr = titleHash.get(token);
                    if (StringUtils.isEmpty(titleStr) && fieldInfoHash == null)
                    {
                        titleStr = StringUtils.capitalize(token);
                    }
                    model.addItem(token, titleStr, !token.equals("this"));
                    
                } else
                {
                    String titleStr = StringUtils.capitalize(token);
                    model.addItem(token, titleStr, !token.equals("this"));
                }
            }
        }
        
        if (wasIncomplete != isParsingIncomplete)
        {
            enableUI();
        }
        
        adjustAvailableJList();
        format = baseStr;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cancelButtonPressed()
     */
    @Override
    protected void cancelButtonPressed()
    {
        hasChanged = false;
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
        final String buStr = webLinkDef.getBaseURLStr();
        
        nameTF.setText(webLinkDef.getName());
        baseUrlTF.setText(buStr);
        descTA.setText(webLinkDef.getDesc());
        
        // Can't get the Field to be unselected on MAc OS X
        // AND positioned correctly
        //baseUrlTF.select(buStr.length()-1, buStr.length()-1);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                baseUrlTF.select(0, 0);
                if (StringUtils.isNotEmpty(buStr) && buStr.length() > 0)
                {
                    baseUrlTF.setCaretPosition(0);
                }
            }
        });
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
        
        okBtn.setEnabled(okEnable && !isParsingError && !isParsingIncomplete);
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
            WebLinkDefArg arg = new WebLinkDefArg(name, title, isPrompt);
            if (tableInfo != null)
            {
                DBFieldInfo fi = fieldInfoHash.get(arg.getName());
                if (fi != null)
                {
                    arg.setField(true);
                    arg.setPrompt(false);
                    arg.setTitle(fi.getTitle());
                } else
                {
                    arg.setPrompt(true);
                }
            }
            args.add(arg);
            
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
                case 2 : return arg.isPrompt() && !arg.isField();
            }
            return ""; //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            WebLinkDefArg arg = args.get(row);
            boolean canPrompt = arg.isPrompt() && !arg.isField();
            
            // Turns out, I don't think the prompt should ever be editable.
            switch (column)
            {
                case 0 : return false;
                case 1 : return canPrompt;
                case 2 : return false;//!arg.isField() && tableInfo == null && !arg.getName().equals("this");
            }
            throw new RuntimeException("Missing case!");
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
                    titleHash.put(arg.getName(), (String)value);
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
    
    //--------------------------------------------------------
    //-- Table Model
    //--------------------------------------------------------
    class AvailFieldsTableModel extends DefaultTableModel
    {
        protected String[] colHeaders = null;
        
        /**
         * 
         */
        public AvailFieldsTableModel()
        {
            super();
            colHeaders = new String[] {
                    getResourceString("WebLinkArgDlg.COLNAME"),  //$NON-NLS-1$
                    getResourceString("WebLinkArgDlg.COLTITLE")}; //$NON-NLS-1$
        }
        
        public void fire()
        {
            fireTableDataChanged();
        }
        
        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
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
            return avFieldList == null ? 0 : avFieldList.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            switch (column)
            {
                case 0 : return avFieldList.get(row);
                case 1 : return tableInfo.getFieldByName(avFieldList.get(row)).getTitle();
            }
            return ""; //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object value, int row, int column)
        {
        }
    }
}
