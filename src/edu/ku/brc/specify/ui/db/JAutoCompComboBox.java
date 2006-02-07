/* Filename:    $RCSfile: JAutoCompComboBox.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/27 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui.db;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.BadLocationException;
/**
 * An editable JComboBox that enables auto-completion which is supported through PickList/PickListItem. 
 * The searches in the list can be case-sensitive or insensitive. 
 * You can also set it to ask if new items should be added.
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class JAutoCompComboBox extends JComboBox
{
    protected int                caretPos        = 0;
    protected boolean            enableAdditions = true;
    protected boolean            caseInsensitve  = true;

    protected JTextField         tf              = null;
    protected boolean            foundMatch      = false;
    protected boolean            ignoreFocus     = false;
    protected boolean            askBeforeSave   = false;
    
    protected PickListDBAdapter  dbAdapter       = null;

    /**
     * Constructor
     */
    public JAutoCompComboBox()
    {
        super();
    }
    
    /**
     * Constructor
     * @param arg0 with a model
     */
    public JAutoCompComboBox(ComboBoxModel arg0)
    {
        super(arg0);
    }

    /**
     * Constructor
     * @param arg0 object array of items
     */
    public JAutoCompComboBox(Object[] arg0)
    {
        super(arg0);
    }

    /**
     * Constructor
     * @param arg0 vector of items
     */
    public JAutoCompComboBox(Vector<?> arg0)
    {
        super(arg0);
    }

    /**
     * Constructor with Adaptor
     * @param dBAdaptor the adaptor for enabling autocomplete
     */
    public JAutoCompComboBox(PickListDBAdapter dbAdapter)
    {
        super(dbAdapter.getList());
        
        this.dbAdapter = dbAdapter;
        init(true);
    }
    
    /**
     * Initializes the combobox to enable the typing of values 
     * @param makeEditable indicates to make it an editable combobox
     */
    protected void init(final boolean makeEditable)
    {
        if (makeEditable)
        {
            this.setEditor(new BasicComboBoxEditor());
            this.setEditable(true);
            setSelectedIndex(-1);  
        }
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

    /* (non-Javadoc)
     * @see javax.swing.JComboBox#setSelectedIndex(int)
     */
    public void setSelectedIndex(int index)
    {
        super.setSelectedIndex(index);
        if (dbAdapter != null && index > -1)
        {
            Object item = getItemAt(index);
            if (item instanceof PickListItem)
            {
    	        tf.setText(((PickListItem)item).getTitle());
    	        tf.setSelectionEnd(caretPos + tf.getText().length());
    	        tf.moveCaretPosition(caretPos);
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
            
            // XXX I18N
	        if (!askBeforeSave || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, 
                                                            "Add new value `"+strArg+"` to the list?", "Add New Item", JOptionPane.YES_NO_OPTION))
	        {
                PickListItem pli;
	            if (dbAdapter != null)
	            {
                    pli = dbAdapter.addItem(strArg, strArg);
	            } else
                {
                    pli = new PickListItem(strArg, strArg, null); // this is ok because the items will not be saved.
                }
                this.addItem(pli);
                this.setSelectedItem(pli);
                
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
    protected void addNewItemFromTextField()
    {
        if (getSelectedIndex() != -1) // accepting value and setting the selection to null 
        {
            tf.setSelectionStart(0);
            tf.setSelectionEnd(0);
            tf.moveCaretPosition(0);
            
        } else
        {
            // Need to add a new value
            if (enableAdditions)
            {
	            if (askToAdd(tf.getText()))
	            {
	                tf.setSelectionStart(0);
	                tf.setSelectionEnd(0);
	                tf.moveCaretPosition(0);	                                    
	            } else 
	            {
	                tf.setText("");
	            }
            }
        }        
    }
    
    /**
     * 
     */
    protected void lookForMatch()
    {
        String s   = tf.getText();
        int    len = s.length();
        if (len == 0)
        {
            setSelectedIndex(-1);
            foundMatch = false;
            return;
        }
        
        //System.out.println(s);
        caretPos = tf.getCaretPosition();
        String text = "";
        try
        {
            text = tf.getText(0, caretPos);
            
        } catch (BadLocationException ex)
        {
            ex.printStackTrace();
        }
        
        String textLowerCase = text.toLowerCase();
        
        foundMatch = true;
        int n = getItemCount();
        for (int i = 0; i < n; i++)
        {
            int ind;
            if (caseInsensitve) 
            {
                String item = ((PickListItem)getItemAt(i)).getTitle().toLowerCase();
                ind = item.indexOf(textLowerCase);
            } else
            {
                ind = ((PickListItem)getItemAt(i)).getTitle().indexOf(text);
            }
            
            if (ind == 0)
            {
                setSelectedIndex(i);
                return;
            }
        }
        
        // When not doing "additions" ...
        // At this point there was no match so "if" there had been one before there isn't now
        // so remove the last character typed and check to see if there is a match again.
        if (!enableAdditions && len > 0)
        {
            tf.setText(s.substring(0, len-1));
            lookForMatch();
            return;
        }
        foundMatch = false;        
    }

    /* (non-Javadoc)
     * @see javax.swing.JComboBox#setEditor(javax.swing.ComboBoxEditor)
     */
    public void setEditor(ComboBoxEditor anEditor)
    {
        super.setEditor(anEditor);
        if (anEditor.getEditorComponent() instanceof JTextField)
        {
            tf = (JTextField) anEditor.getEditorComponent();
            tf.addFocusListener(new FocusAdapter() 
            {
                public void focusLost(FocusEvent e)
                {
                    addNewItemFromTextField();
                }
            });
            
            //System.out.println(tf.getKeyListeners());
            tf.addKeyListener(new KeyAdapter()
            {
                protected int prevCaretPos = -1;
                
                public void keyPressed(KeyEvent ev)
                {
                    prevCaretPos = tf.getCaretPosition();
                }
                
                public void keyReleased(KeyEvent ev)
                {
                    char key = ev.getKeyChar();
                    if (ev.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                    {
                        String textStr = tf.getText();
                        int    len     = textStr.length();
                        if (len == 0)
                        {
                            foundMatch = false;
                            setSelectedIndex(-1);
                            return;
                            
                        } else
                        {
                            if (foundMatch)
                            {
                                tf.setText(textStr.substring(0, len-1));
                                
                            } else if (!enableAdditions && len > 0)
                            {
                                tf.setText(textStr.substring(0, len-1));
                                lookForMatch();
                                return;
                            }
                        }
                        
                    } else if ((!(Character.isLetterOrDigit(key) || Character.isSpaceChar(key))) && 
                                 ev.getKeyCode() != KeyEvent.VK_DELETE)
                    {
                        if (ev.getKeyCode() == KeyEvent.VK_ENTER) 
                        {
                            addNewItemFromTextField();
                            
                        } else if (ev.getKeyCode() == KeyEvent.VK_END)
                        {
                            tf.setSelectionStart(prevCaretPos);
                            tf.setSelectionEnd(tf.getText().length());
                        }
                        return;
                    }
                    lookForMatch();
                }
            });
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
    public static JAutoCompComboBox create(final String  name, 
                                           final boolean readOnly, 
                                           final int     sizeLimit,
                                           final boolean createWhenNotFound)
    {
        PickListDBAdapter adaptor = new PickListDBAdapter(name, createWhenNotFound);
        adaptor.getPickList().setReadOnly(readOnly);
        adaptor.getPickList().setSizeLimit(sizeLimit);
        
        return new JAutoCompComboBox(adaptor);
    }
  

}
