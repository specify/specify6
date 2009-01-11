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
package edu.ku.brc.af.ui.db;

import static edu.ku.brc.ui.UIHelper.setControlSize;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * An auto-complete text field which is supported through PickList/PickListItem.
 * The searches in the list can be case-sensitive or insensitive. Does search when user presses SEARCH_KEY
 * and pops up a menu for selecting existing values.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class JAutoCompTextField extends JTextField
{
    protected int                caretPos        = 0;
    protected boolean            enableAdditions = true;
    protected boolean            caseInsensitve  = true;
    protected boolean            askBeforeSave   = true;
    protected boolean            hasChanged      = false;

    protected boolean            foundMatch      = false;
    protected boolean            ignoreFocus     = false;
    
    protected PickListDBAdapterIFace dbAdapter       = null;
    
    protected int                prevCaretPos    = -1;
    
    protected JPopupMenu         popupMenu       = null;


    /**
     * Constructor without Adaptor.
     */
    public JAutoCompTextField()
    {
        super();
        setControlSize(this);
    }
    
    /**
     * Constructor.
     * @param numCols initial value
     */    
    public JAutoCompTextField(final String numCols)
    {
        super(numCols);       
        setControlSize(this);
    }
    
    /**
     * Constructor.
     * @param numCols initial number of columns
     */
    public JAutoCompTextField(final int numCols)
    {
        super(numCols);     
        setControlSize(this);
    }
    
    /**
     * Constructor.
     * @param numCols initial number of columns
     */
    public JAutoCompTextField(final int numCols, final PickListDBAdapterIFace pickListDBAdapter)
    {
        super(numCols);
        dbAdapter = pickListDBAdapter;
        initAdapter();
        setControlSize(this);
    }
   
    /**
     * Constructor.
     * @param text initial value
     * @param numCols initial number of columns
     */
    public JAutoCompTextField(final String text,final int numCols)
    {
        super(text, numCols);   
        setControlSize(this);
    }
    
    /**
     * Constructor with Adapter.
     * @param dbAdapter the adaptor for enabling autocomplete
     */
    public JAutoCompTextField(final PickListDBAdapterIFace dbAdapter)
    {
        super();
        this.dbAdapter = dbAdapter;
        initAdapter();
        setControlSize(this);
    }
    
    /**
     * @param dbAdapter the dbAdapter to set
     */
    public void setPickListAdapter(PickListDBAdapterIFace dbAdapter)
    {
        this.dbAdapter = dbAdapter;
    }

    /**
     * @param str
     */
    protected void lookup(final String str)
    {
        foundMatch = true;
        int inx = 0;
        for (PickListItemIFace pli : dbAdapter.getList())
        {
            String title = pli.getTitle();
            int ind;
            if (caseInsensitve) 
            {
                ind = title.toLowerCase().indexOf(str.toLowerCase());
            } else
            {
                ind = title.indexOf(str);
            }
            if (ind == 0)
            {
                setSelectedIndex(inx);
                return;
            }
            inx++;
        }
        foundMatch = false;
        hasChanged = true;
    }
    
    protected void keyReleasedInternal(KeyEvent ev)
    {
        //System.out.println(ev);
        if (dbAdapter != null)
        {
            if (ev.getKeyCode() == JAutoCompComboBox.SEARCH_KEY)
            {
                /*lookup(getText());
                
                if (!foundMatch)
                {
                    setText("");
                }*/
                
                ActionListener al = new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        JMenuItem mi = (JMenuItem)ae.getSource();
                        int inx = 0;
                        for (PickListItemIFace pli : dbAdapter.getList())
                        {
                            String title = pli.getTitle();
                            if (mi.getText().equals(title))
                            {
                                setSelectedIndex(inx);
                                break;
                            }
                            inx++;
                        }
                        popupMenu = null;
                    }
                };
                
                String txt = getText();
                
                int cnt = 0;
                for (PickListItemIFace pli : dbAdapter.getList())
                {
                    String title = pli.getTitle();
                    if (title.length() >= txt.length() &&
                        title.startsWith(txt))
                    {
                        cnt++;
                    }
                }

                if (cnt > 0)
                {
                    popupMenu = new JPopupMenu();
                    for (PickListItemIFace pli : dbAdapter.getList())
                    {
                        String title = pli.getTitle();
                        if (title.length() >= txt.length() &&
                            title.startsWith(txt))
                        {
                            JMenuItem mi = new JMenuItem(title);
                            setControlSize(mi);
                            popupMenu.add(mi);
                            mi.addActionListener(al);
                        }
                    }
                    Point     location = getLocation();
                    Dimension size     = getSize();
                    popupMenu.show(this, location.x, location.y+size.height);
                }

            } else if (ev.getKeyCode() == KeyEvent.VK_ENTER) 
            {
                addNewItemFromTextField();
                
            } else if (ev.getKeyCode() == KeyEvent.VK_END)// || ev.getKeyCode() == KeyEvent.VK_SHIFT)
            {
                setSelectionStart(prevCaretPos);
                setSelectionEnd(getText().length());
            }
            
            
            
            /*else
            {
                char key = ev.getKeyChar();
                if (ev.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                {
                    String s = getText();
                    if (foundMatch)
                    {
                        //System.out.println("len ["+s.length()+"]");
                        //System.out.println(s+"["+s.substring(0, s.length()-1)+"]");
        
                        setText(s.length() == 0 ? "" : s.substring(0, s.length()-1));
                        
                    } else
                    {
                        hasChanged = true;
                    }
                    return;
                    
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
                    
                } else if(ev.getKeyCode() == KeyEvent.VK_DELETE)
                {
                    foundMatch = false;
                    hasChanged = true;
                    return;
                }
                //System.out.println("["+ev.getKeyCode()+"]["+KeyEvent.VK_DELETE+"]");
                
                caretPos = getCaretPosition();
                String text = "";
                try
                {
                    text = getText(0, caretPos);
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(JAutoCompTextField.class, ex);
                    ex.printStackTrace();
                }
                
                lookup(text);
            }*/
        }
    }

    
    /**
     * Initializes the TextField by setting up all the listeners for auto-complete.
     */
    protected void initAdapter()
    {
        if (dbAdapter != null)
        {
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e)
                {
                    addNewItemFromTextField();
                }
            });
            
            
            addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyPressed(KeyEvent ev)
                {
                    prevCaretPos = getCaretPosition();
                    
                    if (UIHelper.isMacOS())
                    {
                        keyReleasedInternal(ev);
                    }
                }
                
                /* (non-Javadoc)
                 * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
                 */
                @Override
                public void keyTyped(KeyEvent arg0)
                {
                    // TODO Auto-generated method stub
                    super.keyTyped(arg0);
                }

                @Override
                public void keyReleased(KeyEvent ev)
                {
                    keyReleasedInternal(ev);
                }
            });
        }
    }
    
    /**
     * Sets the text into the control via an index into the list of possible values.
     * @param index the index of the value
     */
    public void setSelectedIndex(int index)
    {
        if (index > -1 && dbAdapter != null)
        {
            setText(dbAdapter.getItem(index).getTitle());
            setSelectionEnd(caretPos + getText().length());
            moveCaretPosition(caretPos);
        }
    }
   
    /**
     * Sets whether new items can be added.
     * @param enableAdditions indicates items can be added
     */
    public void setEnableAdditions(final boolean enableAdditions)
    {
        this.enableAdditions = enableAdditions;
    }

    /**
     * Sets whether to ask via a dialog if a value should be added.
     * @param askBeforeSave indicates it should ask
     */
    public void setAskBeforeSave(final boolean askBeforeSave)
    {
        this.askBeforeSave = askBeforeSave;
    }
    
    /**
     * Sets wehether the searches for the items are case insensitive or not.
     * @param caseInsensitve
     */
    public void setCaseInsensitive(final boolean caseInsensitve)
    {
        this.caseInsensitve = caseInsensitve;
    }

    /**
     * It may or may not ask if it can add, and then it adds the new item.
     * @param strArg the string that is to be added
     * @return whether it was added
     */
    protected boolean askToAdd(String strArg)
    {
        
        if (ignoreFocus)
        {
            return false;
        }
        
        if (hasChanged && isNotEmpty(strArg))
        {
            ignoreFocus = true;
            String msg = UIRegistry.getLocalizedMessage("JAutoCompTextField.REMEM_VAL", strArg); //$NON-NLS-1$
	        if (!askBeforeSave || JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, msg, UIRegistry.getResourceString("JAutoCompTextField.REMEM_VAL_TITLE"),  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	                                     JOptionPane.YES_NO_OPTION))
	        {
	            if (dbAdapter != null)
	            {
	                dbAdapter.addItem(strArg, null);
	            }
                hasChanged  = false;
	            ignoreFocus = false;
	            return true;
	        }
            ignoreFocus = false;            
        }
        return false;
    }
    
    /**
     * Check to see if it can add the the item that is in the combobox'es testfield.
     */
    protected void addNewItemFromTextField()
    {

        int inx = dbAdapter.getList().indexOf(getText());
        
        if (inx != -1) // accepting value and setting the selection to null 
        {
            setSelectionStart(0);
            setSelectionEnd(0);
            moveCaretPosition(0);
            
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
                    
	            }
            }
        }        
    }

    /**
     * Creates an AutoComplete JComboBox with the name of the pick list it is to use.
     * @param name the name of the picklist to create
     * @param readOnly whether new items can be added to it or not
     * @param sizeLimit the size of list when items can be added (arg is ignored when enableAdditions is false)
     * @param createWhenNotFound indicates whether to automatically create the picklist when the name is not found,
     * or throw a runtime exception
     * @return the AutoComplete JComboBox
     */
    public static JAutoCompTextField create(final String  name, 
                                            final boolean readOnly, 
                                            final int     sizeLimit,
                                            final boolean createWhenNotFound)
    {
        PickListDBAdapterIFace adaptor = PickListDBAdapterFactory.getInstance().create(name, createWhenNotFound);
        adaptor.getPickList().setReadOnly(readOnly);
        adaptor.getPickList().setSizeLimit(sizeLimit);
        
        return new JAutoCompTextField(adaptor);
    }
}
