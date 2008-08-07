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

package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.UIHelper;

/**
 * @author ricardo
 *
 * @code_status Alpha
 *
 *
 */
public class DataObjFieldFormatSinglePanelBuilder extends DataObjFieldFormatPanelBuilder 
{
    
    // ui controls
    protected JTextPane          formatEditor;
    protected JButton            addFieldBtn;
    
    protected DocumentListener   formatTextDL;
    protected ActionListener     addFieldAL;
    protected MouseAdapter       addFieldMA;
    
    protected Set<String>        uniqueNameSet = new HashSet<String>();

    protected SimpleAttributeSet normalAttr;
    protected SimpleAttributeSet fieldDefAttr;
    
    /**
     * @param tableInfo
     * @param availableFieldsComp
     * @param formatContainer
     * @param okButton
     * @param uiFieldFormatterMgrCache
     */
    public DataObjFieldFormatSinglePanelBuilder(final DBTableInfo                          tableInfo,
                                                final AvailableFieldsComponent             availableFieldsComp,
                                                final DataObjSwitchFormatterContainerIface formatContainer,    
                                                final JButton                              okButton,
                                                final UIFieldFormatterMgr                  uiFieldFormatterMgrCache)
    {
        super(tableInfo, availableFieldsComp, formatContainer, okButton, uiFieldFormatterMgrCache);
        
        fillWithObjFormatter(formatContainer.getSelectedFormatter());
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatPanelBuilder#buildUI()
     */
    protected void buildUI() 
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:d:g",  
                "10px,"       +   // empty space on top of panel 
                "p,f:p:g,"    +   // Label & format text editor
                "10px,p,"     +   // separator & label
                "f:250px:g,"  +   // list box for available fields 
                "10px"  // empty space where the delete button goes for the multiple value panel
                
                ));
        
        JLabel currentFieldsLbl = createI18NFormLabel("DOF_DISPLAY_FORMAT");
        formatEditor = new JTextPane();
        // to make sure the component shrinks with the dialog
        formatEditor.setMinimumSize(new Dimension(200, 50));
        formatEditor.setPreferredSize(new Dimension(350, 100));

        PanelBuilder addFieldPB = new PanelBuilder(new FormLayout("l:m:g,r:m", "p"));  
        JLabel availableFieldsLbl = createI18NFormLabel("DOF_AVAILABLE_FIELDS");
        addFieldBtn = createButton(getResourceString("DOF_ADD_FIELD")); 
        addFieldBtn.setEnabled(true);
        addFieldPB.add(availableFieldsLbl, cc.xy(1,1));
        addFieldPB.add(addFieldBtn, cc.xy(2,1));

        // lay out components on main panel        
        int y = 2; // leave first row blank 
        pb.add(currentFieldsLbl, cc.xy(1, y)); y += 1;
        pb.add(UIHelper.createScrollPane(formatEditor), cc.xy(1, y)); y += 2;
    
        pb.add(addFieldPB.getPanel(), cc.xy(1, y)); y += 1;
        pb.add(UIHelper.createScrollPane(availableFieldsComp.getTree()), cc.xy(1, y)); y += 2;
        
        this.mainPanelBuilder = pb;

        addFormatTextListeners();
        // must be called after list of available fields has been created
        addFieldListeners();
    }
    
    /**
     * 
     */
    public void addFieldListeners()
    {
        // action listener for add field button
        if (addFieldAL == null)
        {
            addFieldAL = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    addField();
                }
            };
        }
        addFieldBtn.addActionListener(addFieldAL);
        
        // mouse adapter to detect double-click on list of available fields 
        if (addFieldMA == null)
        {
            addFieldMA = new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e)
                {
                    if(e.getClickCount() == 2)
                    {
                        addField();
                    }
                }
            };
        }
        
        availableFieldsComp.addMouseListener(addFieldMA);

    }

    /**
     * 
     */
    public void addFormatTextListeners()
    {
        // action listener for add field button
        if (formatTextDL == null)
        {
            formatTextDL = new DocumentListener()
            {
                public void removeUpdate (DocumentEvent e) { changed(e); }
                public void insertUpdate (DocumentEvent e) { changed(e); }
                public void changedUpdate(DocumentEvent e) { changed(e); }

                private void changed(@SuppressWarnings("unused") DocumentEvent e)
                { 
                    formatChanged(); 
                }
            };
        }
        formatEditor.getDocument().addDocumentListener(formatTextDL);
    }
    
    /**
     * Updates the formatter being composed whenever the text in the format text field changes.
     */
    public void formatChanged()
    {
        // for now, just create formatter from scratch, but later we can just detect changes and act accordingly
        setFormatterFromTextPane(formatContainer.getSelectedFormatter());
    }
    
    /*
     * Adds a field to the format being composed
     */
    public void addField()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) availableFieldsComp.getTree().getLastSelectedPathComponent();
        if (node == null || !node.isLeaf() || !(node.getUserObject() instanceof DataObjDataFieldWrapper) )
        {
            // not really a field that can be added, just empty or a string
            return;
        }

        Object obj = node.getUserObject();
        if (obj instanceof DataObjDataFieldWrapper)
        {
            DataObjDataFieldWrapper wrapper = (DataObjDataFieldWrapper) obj;
            insertFieldIntoTextEditor(wrapper);
            hasChanged = true;
            enableUIControls();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatPanelBuilder#fillWithObjFormatter(edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter)
     */
    public void fillWithObjFormatter(DataObjSwitchFormatter switchFormatter)
    {
        if (switchFormatter != null)
        {
            fillWithObjFormatter(switchFormatter.getSingle());
        }
        else
        {
            fillWithObjFormatter((DataObjDataFieldFormatIFace) null);
        }
    }
    
    /**
     * @param singleFormatter
     */
    protected void fillWithObjFormatter(DataObjDataFieldFormatIFace singleFormatter)
    {
        formatEditor.setText("");
        if (singleFormatter == null)
        {
            return;
        }
        
        Document doc = formatEditor.getDocument();
        DataObjDataField[] fields = singleFormatter.getFields();
        if (fields == null)
        {
            return;
        }
        
        for (DataObjDataField field : fields)
        {
            try
            {
                doc.insertString(doc.getLength(), field.getSep(), null);
                insertFieldIntoTextEditor(new DataObjDataFieldWrapper(field));
            }
            catch (BadLocationException ble) {}
        }
    }
    
    /**
     * @param wrapper
     */
    protected void insertFieldIntoTextEditor(DataObjDataFieldWrapper wrapper)
    {
        formatEditor.insertComponent(new FieldDefinitionButton(wrapper));
    }

    /**
     * Wrapper for formatters.
     * Created to modify toString() method and display item nicely on JList. 
     * 
     * @author Ricardo
     */
    protected class FieldDefinitionButton extends JButton
    {
        protected final DataObjDataFieldWrapper dataObjFieldWrapper;
        
        public FieldDefinitionButton(DataObjDataFieldWrapper dataObjFieldWrapper)
        {
            this.dataObjFieldWrapper = dataObjFieldWrapper;
            setText(dataObjFieldWrapper.toString());

            setCursor(Cursor.getDefaultCursor());
            setMargin(new Insets(0,0,0,0));
            setFont(new Font("Arial", Font.PLAIN, 11));
            setAlignmentY(0.75f);
            //button.setActionCommand(buttonString);
           
            // create popup menu
            final JPopupMenu menu = createPopupMenu();
            
            ActionListener showMenuAL = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    JButton thisBtn = FieldDefinitionButton.this;
                    menu.show(thisBtn, thisBtn.getWidth() / 2, thisBtn.getHeight());
                }
            };
            addActionListener(showMenuAL);
        }
        
        /**
         * @return
         */
        public DataObjDataField getValue()
        {
            return dataObjFieldWrapper.getFormatterField();
        }
        
        /**
         * @return
         */
        public JPopupMenu createPopupMenu()
        {
            final JPopupMenu menu = new JPopupMenu();

            ActionListener placeCursorHereAL = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    // find position of the field definition button in question
                    int pos = findFieldButtonPosition();
                    formatEditor.setCaretPosition(pos);
                }
            };
            
            JMenuItem item = new JMenuItem(getResourceString("DOF_PLACE_CURSOR_HERE"));
            item.addActionListener(placeCursorHereAL);
            menu.add(item);

            ActionListener deleteMenuAL = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    try 
                    {
                        // find position of the field definition button in question
                        int pos = findFieldButtonPosition();
                        formatEditor.getDocument().remove(pos, 1);
                    }
                    catch (BadLocationException ble) {}
                }
            };
            
            item = new JMenuItem(getResourceString("DOF_DELETE_FIELD"));
            item.addActionListener(deleteMenuAL);
            menu.add(item);
            
            createFormatterMenuItem(menu);
            
            MouseAdapter menuMA = new MouseAdapter()
            {
                public void mousePressed(MouseEvent e)
                {
                    if (e.isPopupTrigger())
                        menu.show(e.getComponent(), e.getX(), e.getY());
                }
                
                public void mouseReleased(MouseEvent e)
                {
                    if (e.isPopupTrigger())
                        menu.show(e.getComponent(), e.getX(), e.getY());
                }
            };
            
            addMouseListener(menuMA);
            
            return menu;
        }
        
        /**
         * @return
         */
        public int findFieldButtonPosition()
        {
            DefaultStyledDocument doc = (DefaultStyledDocument) formatEditor.getStyledDocument();
            int i, n = doc.getLength();
            Object obj = null;
            for (i = 0; i < n; ++i)
            {
                Element element = doc.getCharacterElement(i);
                AttributeSet attrs = element.getAttributes();
                obj = attrs.getAttribute(StyleConstants.ComponentAttribute);
                if (obj == this)
                {
                    // found button at this position
                    return i;
                }
            }
            
            // this should never happen because the button clicked must be inside text pane
            throw new RuntimeException("Button representing field in text pane was not found.");
        }
        
        /**
         * @param menu
         */
        public void createFormatterMenuItem(JPopupMenu menu)
        {
            if (!dataObjFieldWrapper.isPureField())
            {
                // can't define a formatter if it's not a real field, 
                // so let's not create the menu item for it
                return;
            }
            
            ActionListener openFormatterAL = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    throw new RuntimeException("FIX ME!");
                    
                    // open UI field formatter dialog to format this field 
                    /*DBFieldInfo    fi  = dataObjFieldWrapper.getFormatterField().getFieldInfo();
                    UIFormatterDlg dlg = new UIFormatterDlg((Frame) UIRegistry.getTopWindow(), fi, 0, uiFieldFormatterMgrCache);
                    dlg.setVisible(true);
                    
                    if (dlg.getBtnPressed() == CustomDialog.OK_BTN)
                    {
                        setFormatter(dlg.getSelectedFormat());
                    }*/
                }
            };
            
            JMenuItem item = new JMenuItem(getResourceString("DOF_CHANGE_FORMAT"));
            item.addActionListener(openFormatterAL);
            menu.add(item);
        }

        /**
         * @param fmt
         */
        protected void setFormatter(UIFieldFormatterIFace fmt)
        {
            dataObjFieldWrapper.getFormatterField().setUiFieldFormatter(fmt);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatPanelBuilder#enableUIControls()
     */
    public void enableUIControls() 
    {
        if (okButton != null)
        {
            okButton.setEnabled(hasChanged && formatContainer.getSelectedFormatter().hasFormatters());
        }
    }

    /**
     * @param formatter
     */
    protected void setFormatterFromTextPane(DataObjSwitchFormatter formatter)
    {
        // visit every character in the document text looking for fields
        // store characters not associated with components (jbutton) to make up the separator text

        DefaultStyledDocument doc = (DefaultStyledDocument) formatEditor.getStyledDocument();
        String text = formatEditor.getText(); 
        int n = doc.getLength();
        int lastFieldPos = 0;
        Vector<DataObjDataField> fields = new Vector<DataObjDataField>();
        for (int i = 0; i < n; ++i)
        {
            Element element = doc.getCharacterElement(i);
            AttributeSet attrs = element.getAttributes();
            Object obj = attrs.getAttribute(StyleConstants.ComponentAttribute);
            if (obj instanceof FieldDefinitionButton)
            {
                // found button at the current position
                // create corresponding field
                String sepStr = (lastFieldPos < i - 1)? text.substring(lastFieldPos, i) : "";

                FieldDefinitionButton fieldDefBtn = (FieldDefinitionButton) obj;
                DataObjDataField fmtField = fieldDefBtn.getValue();
                fmtField.setSep(sepStr);
                fields.add(fmtField);
                
                lastFieldPos = i;
            }
        }

        // XXX: what do we do with the remaining of the text? right now we ignore it
        // That's because we can't create an empty formatter field just to use the separator... 

        DataObjDataField[] fieldsArray = new DataObjDataField[fields.size()];
        for (int i = 0; i < fields.size(); ++i)
        {
            fieldsArray[i] = fields.elementAt(i); 
        }
        
        DataObjDataFieldFormat singleFormatter = new DataObjDataFieldFormat("", tableInfo.getClassObj(), false, "", "", fieldsArray);
        formatter.setSingle(singleFormatter);
    }
}
