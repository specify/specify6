/* Filename:    $RCSfile: JAutoCompTextField.java,v $
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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import edu.ku.brc.specify.ui.db.PickListItem;

/**
 * An auto-complete text field which is supported through PickList/PickListItem.
 * The searches in the list can be case-sensitive or insensitive
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class JAutoCompTextField extends JTextField
{
    protected int                caretPos        = 0;
    protected boolean            enableAdditions = true;
    protected boolean            caseInsensitve  = true;
    protected boolean            askBeforeSave   = true;

    protected boolean            foundMatch      = false;
    protected boolean            ignoreFocus     = false;
    
    protected PickListDBAdapter  dbAdapter       = null;

    /**
     * Constructor with Adaptor
     * @param dBAdaptor the adaptor for enabling autocomplete
     */
    public JAutoCompTextField(PickListDBAdapter dbAdapter)
    {
        super();
        this.dbAdapter = dbAdapter;
        init();
    }

    
    /**
     * Initializes the TextField by setting up all the listeners for auto-complete
     */
    protected void init()
    {
        addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                addNewItemFromTextField();
            }
        });
        
        
        addKeyListener(new KeyAdapter()
        {
            protected int prevCaretPos = -1;
            
            public void keyPressed(KeyEvent ev)
            {
                prevCaretPos = getCaretPosition();
            }
            
            public void keyReleased(KeyEvent ev)
            {
                char key = ev.getKeyChar();
                if (ev.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                {
                    String s = getText();
                    if (foundMatch)
                    {
                        //System.out.println("len ["+s.length()+"]");
                        //System.out.println(s+"["+s.substring(0, s.length()-1)+"]");
                        setText(s.substring(0, s.length()-1));
                        
                    }
                    
                } else if ((!(Character.isLetterOrDigit(key) || Character.isSpaceChar(key))) && 
                             ev.getKeyCode() != KeyEvent.VK_DELETE)
                {
                    if (ev.getKeyCode() == KeyEvent.VK_ENTER) 
                    {
                        addNewItemFromTextField();
                    }
                    //System.out.println("Key Code "+ev.getKeyCode()+"  Pos: "+getCaretPosition()+"  Del: "+KeyEvent.VK_DELETE);
                    
                    if (ev.getKeyCode() == KeyEvent.VK_END)// || ev.getKeyCode() == KeyEvent.VK_SHIFT)
                    {
                        setSelectionStart(prevCaretPos);
                        setSelectionEnd(getText().length());
                    }
                    return;
                }
                
                caretPos = getCaretPosition();
                String text = "";
                try
                {
                    text = getText(0, caretPos);
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                
                String lowerCaseText = text.toLowerCase();
                
                foundMatch = true;
                int inx = 0;
                for (PickListItem pli : dbAdapter.getList())
                {
                    //System.out.println("str: "+str);
                    String title = pli.getTitle();
                    int ind;
                    if (caseInsensitve) 
                    {
                        ind = title.toLowerCase().indexOf(lowerCaseText);
                    } else
                    {
                        ind = title.indexOf(text);
                    }
                    if (ind == 0)
                    {
                        setSelectedIndex(inx);
                        return;
                    }
                    inx++;
                }
                foundMatch = false;
            }
        });
    }
    
    /**
     * Sets the text into the control via an index into the list of possible values
     * @param index the index of the value
     */
    public void setSelectedIndex(int index)
    {
        if (index > -1)
        {
            setText(dbAdapter.getItem(index).getTitle());
            setSelectionEnd(caretPos + getText().length());
            moveCaretPosition(caretPos);
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
        
        if (strArg != null && strArg.length() > 0)
        {
            ignoreFocus = true;
	        if (!askBeforeSave || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, "Remember value `"+strArg+"`?", "Remember Value", 
	                                     JOptionPane.YES_NO_OPTION))
	        {
	            if (dbAdapter != null)
	            {
	                dbAdapter.addItem(strArg, null);
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
    protected void addNewItemFromTextField()
    {

        int inx = dbAdapter.getList().indexOf(getText());
        
        if (inx != -1) // accepting value and setting the selection to null 
        {
            setSelectionStart(0);
            setSelectionEnd(0);
            moveCaretPosition(0);
            //System.out.println("Clearing Selection.");
            
        } else 
        {
            // Need to add a new value
            if (enableAdditions)
            {
	            if (askToAdd(getText()))
	            {
	                setSelectionStart(0);
	                setSelectionEnd(0);
	                moveCaretPosition(0);	   
                    
	            } else 
	            {
	                //setText("");
	            }
            }
        }        
    }

    /**
     * Creates an AutoComplete JComboBox with the "ID" of the pick list it is to use.
     * @param id the id of the pick list
     * @return the AutoComplete JComboBox
     */
    public static JAutoCompTextField create(final int id)
    {
        return new JAutoCompTextField(new PickListDBAdapter(id));
    }
}
