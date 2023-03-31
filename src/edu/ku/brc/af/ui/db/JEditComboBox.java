/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.af.ui.db;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Timestamp;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
/**
 * An editable JComboBox that enables the user to edit the values in the list which actually creates a new value
 * in the picklist.
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class JEditComboBox extends JComboBox
{
    protected int                caretPos        = 0;
    protected boolean            enableAdditions = true;
    protected boolean            caseInsensitve  = true;

    protected JTextField         textField       = null;
    protected boolean            foundMatch      = false;
    protected boolean            ignoreFocus     = false;
    protected boolean            askBeforeSave   = false;
    
    protected PickListDBAdapterIFace  dbAdapter  = null;
    
    public static final KeyStroke clearKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);//, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());


    /**
     * Constructor
     */
    public JEditComboBox()
    {
        super();
    }
    
    /**
     * Constructor
     * @param arg0 with a model
     */
    public JEditComboBox(ComboBoxModel arg0)
    {
        super(arg0);
    }

    /**
     * Constructor
     * @param arg0 object array of items
     */
    public JEditComboBox(Object[] arg0)
    {
        super(arg0);
    }

    /**
     * Constructor
     * @param arg0 vector of items
     */
    public JEditComboBox(Vector<?> arg0)
    {
        super(arg0);
    }

    /**
     * Constructor with Adapter
     * @param dbAdapter the adaptor for enabling autocomplete
     */
    public JEditComboBox(final PickListDBAdapterIFace dbAdapter)
    {
        super(dbAdapter.getList());
        
        this.dbAdapter = dbAdapter;
        init(true);
    }
    
    /**
     * An initializer so a PickListAdaptor can be set after the control is created, and automatically makes it editable
     * @param dbAdapterArg the PickListAdaptor
     * @param makeEditable indicates whether it is editable
     */
    public void init(final PickListDBAdapterIFace dbAdapterArg, final boolean makeEditable)
    {
        setModel(new DefaultComboBoxModel(dbAdapterArg.getList()));
        
        this.dbAdapter = dbAdapterArg;
        init(makeEditable);  
    }
    
    /**
     * Initializes the combobox to enable the typing of values 
     * @param makeEditable indicates to make it an editable combobox
     */
    public void init(final boolean makeEditable)
    {
        if (makeEditable && !this.isEditable)
        {
            this.setEditor(new BasicComboBoxEditor());
            this.setEditable(true);
            setSelectedIndex(-1);  
        }
    }
    
    /**
     * Returns the text field when it is editable
     * @return the text field when it is editable
     */
    public JTextField getTextField()
    {
        return textField;
    }
    
    /**
     * Sets whether new items can be added
     * @param enableAdditions indicates items can be added
     */
    public void setEnableAdditions(final boolean enableAdditions)
    {
        this.enableAdditions = enableAdditions;
    }

    /**
     * Sets whether to ask via a dialog if a value should be added
     * @param askBeforeSave indicates it should ask
     */
    public void setAskBeforeSave(final boolean askBeforeSave)
    {
        this.askBeforeSave = askBeforeSave;
    }
    
    /**
     * Sets wehether the searches for the items are case insensitive or not
     * @param caseInsensitve
     */
    public void setCaseInsensitive(final boolean caseInsensitve)
    {
        this.caseInsensitve = caseInsensitve;
    }
    
    /**
     * Return the PickListAdaptor
     * @return the PickListAdaptor
     */
    public PickListDBAdapterIFace getDBAdapter()
    {
        return dbAdapter;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setBackground(java.awt.Color)
     */
    @Override
    public void setBackground(Color bgColor)
    {
        super.setBackground(bgColor);
        if (textField != null)
        {
            textField.setBackground(bgColor);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.JComboBox#setSelectedIndex(int)
     */
    @Override
    public void setSelectedIndex(int index)
    {
        super.setSelectedIndex(index);
        
        if (textField != null && dbAdapter != null && index > -1)
        {
            Object item = getItemAt(index);
            if (item instanceof PickListItem)
            {
                textField.setText(((PickListItem)item).getTitle());
                //textField.setSelectionEnd(caretPos + textField.getText().length());
                //textField.moveCaretPosition(caretPos);
            }
        }
    }
    
    /**
     * It may or may not ask if it can add, and then it adds the new item
     * @param strArg the string that is to be added
     * @return whether it was added
     */
    protected boolean askToAdd(String strArg)
    {
        if (ignoreFocus)
        {
            return false;
        }
        
        if (isNotEmpty(strArg))
        {
            ignoreFocus = true;
            
            String msg   = UIRegistry.getLocalizedMessage("JEditComboBox.ADD_NEW_VALUE", strArg); //$NON-NLS-1$
            String title = UIRegistry.getResourceString("JEditComboBox.ADD_NEW_ITEM_TITLE"); //$NON-NLS-1$
            if (!askBeforeSave || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION)) 
            {
                PickListItemIFace pli = null;
                if (dbAdapter != null)
                {
                    if (!dbAdapter.isReadOnly())
                    {
                        pli = dbAdapter.addItem(strArg, strArg);
                    }
                } else
                {
                    pli = new PickListItem(strArg, strArg, new Timestamp(System.currentTimeMillis())); // this is ok because the items will not be saved.
                }
                
                if (pli != null)
                {
                    this.addItem(pli);
                    this.setSelectedItem(pli);
                }
                
                ignoreFocus = false;
                
                return true;
            }
            ignoreFocus = false;            
        }
        return false;
    }
    
    /**
     * Check to see if it can add the the item that is in the combobox'es testfield
     */
    public void addNewItemFromTextField()
    {
        if (getSelectedIndex() != -1) // accepting value and setting the selection to null 
        {
            if (textField != null)
            {
                textField.setSelectionStart(0);
                textField.setSelectionEnd(0);
                textField.moveCaretPosition(0);
            }
            
        } else
        {
            // Need to add a new value
            if (enableAdditions && textField != null)
            {
                String str = textField.getText();
                if (StringUtils.isNotEmpty(str) && askToAdd(str))
                {
                    textField.setSelectionStart(0);
                    textField.setSelectionEnd(0);
                    textField.moveCaretPosition(0);                                     
                } else 
                {
                    textField.setText(""); //$NON-NLS-1$
                }
            }
        }        
    }
    
    protected KeyAdapter createKeyAdapter()
    {
        return new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent ev)
            {
                char key = ev.getKeyChar();
                
                if (ev.getKeyCode() == clearKeyStroke.getKeyCode())
                {
                    int selectedIndex = getSelectedIndex();
                    if (selectedIndex > -1 && dbAdapter != null && 
                        textField != null && 
                        textField.getText().length() == 0 &&
                        !dbAdapter.isReadOnly())
                    {
                        // delete item
                        PickListItem item = (PickListItem)getSelectedItem();
                        dbAdapter.getList().remove(item);
                    }
                    
                } else if (!(Character.isLetterOrDigit(key) || Character.isSpaceChar(key)))
                {
                    if (ev.getKeyCode() == KeyEvent.VK_ENTER) 
                    {
                        addNewItemFromTextField();
                    }
                } else
                {
                    if (textField != null)
                    {
                        if (getSelectedIndex() > -1)
                        {
                            int    pos         = textField.getCaretPosition();
                            String currentText = textField.getText();
                            setSelectedIndex(-1);    
                            textField.setText(currentText);
                            textField.moveCaretPosition(pos);
                            textField.setSelectionStart(pos);
                            textField.setSelectionEnd(pos);
                        }
                        
                    } else
                    {
                        setSelectedIndex(-1);
                    }
                }
            }
        };
    }
    
    protected FocusListener createFocusListener()
    {
        return new FocusAdapter() 
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                addNewItemFromTextField();
            }
        };
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComboBox#setEditor(javax.swing.ComboBoxEditor)
     */
    @Override
    public void setEditor(ComboBoxEditor anEditor)
    {
        super.setEditor(anEditor);
        
        if (anEditor != null && anEditor.getEditorComponent() instanceof JTextField)
        {
            textField = (JTextField) anEditor.getEditorComponent();
            
            FocusListener fl = createFocusListener();
            if (fl != null)
            {
                textField.addFocusListener(fl);
            }
            
            KeyAdapter ka = createKeyAdapter();
            if (ka != null)
            {
                textField.addKeyListener(ka);
            }
        }
    }
    
    /**
     * Returns whether the ComboBox has a PickList Adapter
     * @return true if it has a dbAdapter
     */
    public boolean hasAdapter()
    {
        return this.dbAdapter != null;
    }
    
    /**
     * Creates an AutoComplete JComboBox with a name of the pick list it is to use.
     * @param name the name of the picklist to create
     * @param readOnly whether new items can be added to it
     * @param sizeLimit the size of list when items can be added (arg is ignored when enableAdditions is false)
     * @param createWhenNotFound indicates whether to automatically create the picklist when the name is not found,
     * or throw a runtime exception
     * @return the new autocomplete JComboBox
     */
    public static JEditComboBox create(final String  name, 
                                           final boolean readOnly, 
                                           final int     sizeLimit,
                                           final boolean createWhenNotFound)
    {
        PickListDBAdapterIFace adaptor = PickListDBAdapterFactory.getInstance().create(name, createWhenNotFound);
        adaptor.getPickList().setReadOnly(readOnly);
        adaptor.getPickList().setSizeLimit(sizeLimit);
        
        return new JEditComboBox(adaptor);
    }


    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        // TODO Cross Platform MAC ONLY
        if (UIHelper.getOSType() == UIHelper.OSTYPE.MacOSX)
        {
            Dimension d = super.getPreferredSize();
            if (this.getEditor() != null)
            {
                d.height += 6;
            }
           return d;
        }
        return super.getPreferredSize();
    }
}
