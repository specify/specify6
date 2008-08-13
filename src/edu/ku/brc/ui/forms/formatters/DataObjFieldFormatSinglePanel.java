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
import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.UIHelper;

/**
 * @author ricardo
 *
 * @code_status Alpha
 *
 *
 */
public class DataObjFieldFormatSinglePanel extends DataObjFieldFormatPanel 
{
    // ui controls
    protected JTextPane          formatEditor;
    protected JButton            addFieldBtn;
    protected JTextField         sepText;
    protected JLabel             sepLbl;
    
    protected Set<String>        uniqueNameSet = new HashSet<String>();

    protected SimpleAttributeSet normalAttr;
    protected SimpleAttributeSet fieldDefAttr;
    
    protected boolean            ignoreFmtChange = false;
    
    
    /**
     * @param tableInfo
     * @param availableFieldsComp
     * @param formatContainer
     * @param okButton
     * @param uiFieldFormatterMgrCache
     */
    public DataObjFieldFormatSinglePanel(final DBTableInfo                          tableInfo,
                                         final AvailableFieldsComponent             availableFieldsComp,
                                         final DataObjSwitchFormatterContainerIface formatContainer,
                                         final UIFieldFormatterMgr                  uiFieldFormatterMgrCache,    
                                         final ChangeListener                       listener)
    {
        super(tableInfo, availableFieldsComp, formatContainer, uiFieldFormatterMgrCache, listener);
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatPanelBuilder#buildUI()
     */
    protected void buildUI() 
    {
        CellConstraints cc = new CellConstraints();
        
        JLabel currentFieldsLbl = createI18NLabel("DOF_DISPLAY_FORMAT");
        formatEditor = new JTextPane();
        // to make sure the component shrinks with the dialog
        formatEditor.setMinimumSize(new Dimension(200, 50));
        formatEditor.setPreferredSize(new Dimension(350, 100));

        PanelBuilder addFieldPB         = new PanelBuilder(new FormLayout("p,2px,p,f:p:g,r:m", "p,2px,p"));  
        sepText     = createTextField(4);
        addFieldBtn = createButton(getResourceString("DOF_ADD_FIELD")); 
        sepLbl      = createI18NFormLabel("DOF_SEP_TXT");
        
        addFieldPB.add(sepLbl, cc.xy(1,1));
        addFieldPB.add(sepText, cc.xy(3,1));
        addFieldPB.add(addFieldBtn, cc.xy(5,1));
        
        addFieldBtn.setEnabled(false);
        sepLbl.setEnabled(false);
        sepText.setEnabled(false);
        
        // For when it is standalone
        if (AppPreferences.hasRemotePrefs())
        {
            sepText.setText(AppPreferences.getRemote().get("DOF_SEP", ", "));
            
        } else
        {
            sepText.setText(", ");
        }

        PanelBuilder pb = new PanelBuilder(new FormLayout("f:d:g",  
                "10px,"       +   // empty space on top of panel 
                "p,f:p:g,"    +   // Label & format text editor
                "2px,p,"      +   // separator & add field
                "10px,p,"     +   // separator & label
                "f:250px:g,"      // list box for available fields 
                ), this);
        
        // layout components on main panel        
        int y = 2; // leave first row blank
        
        pb.add(currentFieldsLbl, cc.xy(1, y)); y += 1;
        pb.add(UIHelper.createScrollPane(formatEditor), cc.xy(1, y)); y += 2;
        
        pb.add(addFieldPB.getPanel(), cc.xy(1, y)); y += 2;
        
        JLabel availableFieldsLbl = createI18NFormLabel("DOF_AVAILABLE_FIELDS", SwingConstants.LEFT);
        pb.add(availableFieldsLbl, cc.xy(1,y)); y += 1;
        
        pb.add(UIHelper.createScrollPane(availableFieldsComp.getTree()), cc.xy(1, y)); y += 2;
        
        availableFieldsComp.getTree().addTreeSelectionListener(new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent e)
            {
                boolean enable = availableFieldsComp.getTree().getSelectionCount() > 0;
                addFieldBtn.setEnabled(enable);
                sepText.setEnabled(enable);
                sepLbl.setEnabled(enable);
            }
        });
        
        this.mainPanelBuilder = pb;
        
        fillWithObjFormatter(formatContainer.getSelectedFormatter());

        addFormatTextListeners();
        
        // must be called after list of available fields has been created
        addFieldListeners();
    }
    
    
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean flag)
    {
        super.setVisible(flag);
        if (!flag)
        {
            formatContainer.getSelectedFormatter().setSingle(null);
        }
    }

    /**
     * 
     */
    public void addFieldListeners()
    {
        // action listener for add field button
        addFieldBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addField();
            }
        });
        
        // mouse adapter to detect double-click on list of available fields 
        availableFieldsComp.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    addField();
                }
            }
        });
    }

    /**
     * 
     */
    public void addFormatTextListeners()
    {
        formatEditor.getDocument().addDocumentListener(new DocumentListener()
        {
            public void removeUpdate (DocumentEvent e) { changed(e); }
            public void insertUpdate (DocumentEvent e) { changed(e); }
            public void changedUpdate(DocumentEvent e) { changed(e); }

            private void changed(@SuppressWarnings("unused") DocumentEvent e)
            { 
                if (!ignoreFmtChange)
                {
                    formatChanged();
                }
            }
        });
        
        sepText.getDocument().addDocumentListener(new DocumentListener()
        {
            public void removeUpdate (DocumentEvent e) { changed(e); }
            public void insertUpdate (DocumentEvent e) { changed(e); }
            public void changedUpdate(DocumentEvent e) { changed(e); }

            private void changed(@SuppressWarnings("unused") DocumentEvent e)
            { 
                if (AppPreferences.hasRemotePrefs())
                {
                    AppPreferences.getRemote().put("DOF_SEP", sepText.getText()); 
                } 
            }
        });
    }
    
    /**
     * Updates the formatter being composed whenever the text in the format text field changes.
     */
    public void formatChanged()
    {
        // for now, just create formatter from scratch, but later we can just detect changes and act accordingly
        setFormatterFromTextPane(formatContainer.getSelectedFormatter());
        setHasChanged(true);
    }
    
    /*
     * Adds a field to the format being composed
     */
    public void addField()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) availableFieldsComp.getTree().getLastSelectedPathComponent();
        if (node == null || !node.isLeaf() || !(node.getUserObject() instanceof DataObjDataFieldWrapper) )
        {
            return; // not really a field that can be added, just empty or a string
        }

        Object obj = node.getUserObject();
        if (obj instanceof DataObjDataFieldWrapper)
        {
            DataObjDataFieldWrapper wrapper = (DataObjDataFieldWrapper) obj;
            String sep = sepText.getText();
            if (StringUtils.isNotEmpty(sep))
            {
                try
                {
                    DefaultStyledDocument doc = (DefaultStyledDocument) formatEditor.getStyledDocument();
                    if (doc.getLength() > 0)
                    {
                        doc.insertString(doc.getLength(), sep, null);
                    }
                }
                catch (BadLocationException ble) {}
                
            }
            insertFieldIntoTextEditor(wrapper);
            setHasChanged(true);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatPanelBuilder#fillWithObjFormatter(edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter)
     */
    public void fillWithObjFormatter(final DataObjSwitchFormatter switchFormatter)
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
    protected void fillWithObjFormatter(final DataObjDataFieldFormatIFace singleFormatter)
    {
        formatEditor.setText("");
        if (singleFormatter == null)
        {
            return;
        }
        
        ignoreFmtChange = true;
        
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
        ignoreFmtChange = false;
    }
    
    /**
     * @param wrapper
     */
    protected void insertFieldIntoTextEditor(final DataObjDataFieldWrapper wrapper)
    {
        formatEditor.insertComponent(new FieldDefinitionComp(wrapper));
    }

    /**
     * Wrapper for formatters.
     * Created to modify toString() method and display item nicely on JList. 
     * 
     * @author Ricardo
     */
    protected class FieldDefinitionComp extends JLabel
    {
        protected final DataObjDataFieldWrapper dataObjFieldWrapper;
        
        public FieldDefinitionComp(DataObjDataFieldWrapper dataObjFieldWrapper)
        {
            this.dataObjFieldWrapper = dataObjFieldWrapper;
            
            setText(dataObjFieldWrapper.toString());
            setCursor(Cursor.getDefaultCursor());
            setFont(new Font("Arial", Font.PLAIN, 11));
            setAlignmentY(0.75f);
            setBackground(new Color(0,0,0,30));
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
           
            // create popup menu
            final JPopupMenu menu = createPopupMenu();
            
            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    JLabel thisBtn = FieldDefinitionComp.this;
                    menu.show(thisBtn, thisBtn.getWidth() / 2, thisBtn.getHeight());
                }
            });
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
                        formatChanged();
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
     * @see edu.ku.brc.ui.forms.formatters.DataObjFieldFormatPanelBuilder#isInError()
     */
    @Override
    public boolean isInError()
    {
        //System.err.println("Has: "+formatContainer.getSelectedFormatter().hasFormatters());
        return super.isInError() || !formatContainer.getSelectedFormatter().hasFormatters();
    }

    /**
     * @param formatter
     */
    protected void setFormatterFromTextPane(final DataObjSwitchFormatter formatter)
    {
        // visit every character in the document text looking for fields
        // store characters not associated with components (jbutton) to make up the separator text

        DefaultStyledDocument doc = (DefaultStyledDocument) formatEditor.getStyledDocument();
        String text         = formatEditor.getText(); 
        int    docLen       = doc.getLength();
        int    lastFieldPos = 0;
        
        Vector<DataObjDataField> fields = new Vector<DataObjDataField>();
        for (int i = 0; i < docLen; ++i)
        {
            Element      element = doc.getCharacterElement(i);
            AttributeSet attrs   = element.getAttributes();
            Object       obj     = attrs.getAttribute(StyleConstants.ComponentAttribute);
            if (obj instanceof FieldDefinitionComp)
            {
                // found button at the current position
                // create corresponding field
                String sepStr = (lastFieldPos < i - 1) ? text.substring(lastFieldPos, i) : "";

                FieldDefinitionComp fieldDefBtn = (FieldDefinitionComp) obj;
                DataObjDataField    fmtField    = fieldDefBtn.getValue();
                fmtField.setSep(sepStr);
                fields.add(fmtField);
                
                lastFieldPos = i+1;
            }
        }

        // XXX: what do we do with the remaining of the text? right now we ignore it
        // That's because we can't create an empty formatter field just to use the separator... 

        DataObjDataField[] fieldsArray = new DataObjDataField[fields.size()];
        for (int i = 0; i < fields.size(); ++i)
        {
            fieldsArray[i] = fields.elementAt(i); 
        }
        
        DataObjDataFieldFormat singleFormatter = fieldsArray.length == 0 ? null : new DataObjDataFieldFormat("", tableInfo.getClassObj(), false, "", "", fieldsArray);
        formatter.setSingle(singleFormatter);
    }
}
