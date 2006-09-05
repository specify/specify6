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
package edu.ku.brc.ui.db;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
/**
 * An editable JComboBox that enables auto-completion which is supported through PickList/PickListItem. 
 * The searches in the list can be case-sensitive or insensitive. 
 * You can also set it to ask if new items should be added.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class JAutoCompComboBox extends JEditComboBox
{
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
     * Constructor with Adapter
     * @param dbAdapter the adaptor for enabling autocomplete
     */
    public JAutoCompComboBox(final PickListDBAdapter dbAdapter)
    {
        super(dbAdapter.getList());
        this.dbAdapter = dbAdapter;
    }
    
    
    /* (non-Javadoc)
     * @see javax.swing.JComboBox#setSelectedIndex(int)
     */
    public void setSelectedIndex(int index)
    {
        super.setSelectedIndex(index);
        
        if (textField != null && dbAdapter != null && index > -1)
        {
            Object item = getItemAt(index);
            if (item instanceof PickListItem)
            {
                textField.setText(((PickListItem)item).getTitle());
                textField.setSelectionEnd(caretPos + textField.getText().length());
                textField.moveCaretPosition(caretPos);
            }
        }
    }
    
    /**
     * 
     */
    protected void lookForMatch()
    {
        String s   = textField.getText();
        int    len = s.length();
        if (len == 0)
        {
            setSelectedIndex(-1);
            foundMatch = false;
            return;
        }
        
        //System.out.println(s);
        caretPos = textField.getCaretPosition();
        String text = "";
        try
        {
            text = textField.getText(0, caretPos);
            
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
            textField.setText(s.substring(0, len-1));
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
            textField = (JTextField) anEditor.getEditorComponent();
            //textField.setBackground(super.getBackground());
            textField.addFocusListener(new FocusAdapter() 
            {
                public void focusLost(FocusEvent e)
                {
                    addNewItemFromTextField();
                }
            });
            
            //System.out.println(textField.getKeyListeners());
            textField.addKeyListener(new KeyAdapter()
            {
                protected int prevCaretPos = -1;
                
                public void keyPressed(KeyEvent ev)
                {
                    prevCaretPos = textField.getCaretPosition();
                }
                
                public void keyReleased(KeyEvent ev)
                {
                    char key = ev.getKeyChar();
                    if (ev.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                    {
                        String textStr = textField.getText();
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
                                textField.setText(textStr.substring(0, len-1));
                                
                            } else if (!enableAdditions && len > 0)
                            {
                                textField.setText(textStr.substring(0, len-1));
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
                            textField.setSelectionStart(prevCaretPos);
                            textField.setSelectionEnd(textField.getText().length());
                        }
                        return;
                    }
                    lookForMatch();
                }
            });
        }
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
