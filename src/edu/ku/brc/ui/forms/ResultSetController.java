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

package edu.ku.brc.ui.forms;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.validation.FormValidator;
import edu.ku.brc.ui.forms.validation.UIValidator;
import edu.ku.brc.ui.forms.validation.ValidationListener;

/*
 * Creates a ResultSetController with First, Last, Previous, Next, New and Delete buttons.
 * When the current index is -1 then the controller is disabled. Setting the length to zero will
 * automatically set the current index to -1 and disable the controller.
 * 
 * @code_status Complete
 **
 * @author rods
 *
 */
public class ResultSetController implements ValidationListener
{
    protected static Border enabledBorder  = BorderFactory.createLineBorder(Color.BLACK);
    protected static Border disabledBorder = BorderFactory.createLineBorder(Color.GRAY.brighter());
    
    protected static Color  enabledTxtBG   = Color.WHITE;
    protected static Color  disabledTxtBG  = new Color(225,225,225);//Color.WHITE.darker();
    
    protected List<ResultSetControllerListener> listeners = new ArrayList<ResultSetControllerListener>();
    
    protected FormValidator formValidator = null;
    
    
    protected JPanel  panel    = null;
    protected JButton firstBtn = null;
    protected JButton prevBtn  = null;
    protected JLabel  recDisp  = null;
    protected JButton nextBtn  = null;
    protected JButton lastBtn  = null;  
    protected JButton newRecBtn = null;  
    protected JButton delRecBtn = null;
    protected JButton searchRecBtn = null;
    
    protected int     currentInx = 0;
    protected int     lastInx    = 0;
    protected int     numRecords = 0;
    
    // Global Key Actions
    private enum CommandType { First, Previous, Next, Last, Save, NewItem, DelItem}
    private Hashtable<CommandType, JButton> btnsHash = new Hashtable<CommandType, JButton>();
    
    // Static Members
    private static Hashtable<CommandType, RSAction<CommandType>> commandsHash     = null;
    private static ResultSetController  currentFocusedRS = null;
    
    private static ResultSetController  backStopRS = null;
    
    static 
    {
        registerFocusListener();
    }
    
    /**
     * 
     */
    private ResultSetController()
    {
        
    }
    
    /**
     * Constructor.
     * @param formValidator the form validator that listens for when the index changes to a new object
     * @param addNewBtn indicates it should include the "Add" (New Object) button
     * @param addDelBtn indicates it should include Delete button
     * @param objTitle the title of a single object in the controller (used for building tooltips)
     * @param len the intial length of the List of Objects (can be zero)
     */
    public ResultSetController(final FormValidator formValidator, 
                               final boolean addNewBtn,  
                               final boolean addDelBtn, 
                               final boolean addSearchBtn, 
                               final String  objTitle,
                               final int     len)
    {
        this.formValidator = formValidator;
       
        if (formValidator != null)
        {
            formValidator.addValidationListener(this);
        }
       
        String objectTitle = objTitle;
        if (StringUtils.isEmpty(objectTitle))
        {
            objectTitle = getResourceString("Item");
            if (StringUtils.isEmpty(objectTitle))
            {
                objectTitle = "Item"; // use English if nothing can be found.
            }
            
        }
        buildRecordNavBar(addNewBtn, addDelBtn, addSearchBtn, objectTitle);
        
        setLength(len);
    }
    
    /**
     * Creates a TooTip using the proper Object
     * @param ttKey the ToolTip key
     * @param objTitle the arg for the tooltip
     * @return the full tooltip
     */
    public static String createTooltip(final String ttKey, final String objTitle)
    {
        return String.format(getResourceString(ttKey), new Object[] {objTitle});
    }
    
    /**
     * Creates the UI for the controller.
     * @param addNewBtn indicates it should include the "Add" (New Object) button
     * @param addDelBtn indicates it should include Delete button
     * @param objTitle the title of a single object in the controller (used for building tooltips)
     */
    protected void buildRecordNavBar(final boolean addNewBtn, 
                                     final boolean addDelBtn, 
                                     final boolean addSearchBtn, 
                                     final String  objTitle)
    {
        String             colDef     = "p,2dlu,p,2dlu,max(50dlu;p):grow,2dlu,p,2dlu,p" + 
                                        (addNewBtn ? ",12px,p" : "") + 
                                        (addDelBtn ? ",2dlu,p" : "") + 
                                        (addSearchBtn ? ",2dlu,p" : "");
        
        Insets             insets     = new Insets(1,1,1,1);
        DefaultFormBuilder rowBuilder = new DefaultFormBuilder(new FormLayout(colDef, "p"));
        
        firstBtn = UIHelper.createIconBtn("FirstRec", null, null);
        prevBtn  = UIHelper.createIconBtn("PrevRec", null, null);
        btnsHash.put(CommandType.Previous, prevBtn);
        btnsHash.put(CommandType.First, firstBtn);

        recDisp  = new JLabel("  ");
        recDisp.setHorizontalAlignment(SwingConstants.CENTER);
        recDisp.setOpaque(true);
        recDisp.setBackground(Color.WHITE);
        recDisp.setBorder(enabledBorder);

        
        nextBtn  = UIHelper.createIconBtn("NextRec", null, null);
        lastBtn  = UIHelper.createIconBtn("LastRec", null, null);
        btnsHash.put(CommandType.Next, nextBtn);
        btnsHash.put(CommandType.Last, lastBtn);
        
        firstBtn.setToolTipText(createTooltip("GotoFirstRecordTT", objTitle));
        prevBtn.setToolTipText(createTooltip("GotoPreviousRecordTT", objTitle));
        nextBtn.setToolTipText(createTooltip("GotoNextRecordTT", objTitle));
        lastBtn.setToolTipText(createTooltip("GotoLastRecordTT", objTitle));
        
        firstBtn.setOpaque(false);
        prevBtn.setOpaque(false);
        nextBtn.setOpaque(false);
        lastBtn.setOpaque(false);
        
        CellConstraints cc = new CellConstraints();
        rowBuilder.add(firstBtn, cc.xy(1,1));
        rowBuilder.add(prevBtn, cc.xy(3,1));
        rowBuilder.add(recDisp, cc.xy(5,1));
        rowBuilder.add(nextBtn, cc.xy(7,1));
        rowBuilder.add(lastBtn, cc.xy(9,1));
        int row = 11;
        
        if (addNewBtn)
        {
            if (false)
            {
                newRecBtn = new JButton("+")
                {
                    public void setEnabled(boolean enable)
                    {
                        System.err.println(formValidator.getName() + " " + hashCode() + " "+enable);
                        if (!enable)
                        {
                            int x = 0;
                            x++;
                        }
                        super.setEnabled(enable);
                    }
                };
                ActionListener l = new ActionListener() {
                    public void actionPerformed(ActionEvent ae)
                    {
                        for (ResultSetControllerListener rscl : listeners)
                        {
                            rscl.newRecordAdded();
                        }
                    }
                 };
                newRecBtn.addActionListener(l);

            } else
            {
                newRecBtn = UIHelper.createIconBtn("NewRecord", null, new ActionListener() {
                    public void actionPerformed(ActionEvent ae)
                    {
                        for (ResultSetControllerListener rscl : listeners)
                        {
                            rscl.newRecordAdded();
                        }
                    }
                });
            }
            
            newRecBtn.setToolTipText(createTooltip("NewRecordTT", objTitle));
            newRecBtn.setEnabled(true);
            newRecBtn.setMargin(insets);
            btnsHash.put(CommandType.NewItem, newRecBtn);

            rowBuilder.add(newRecBtn, cc.xy(row,1));
            row += 2;
        }
        
        if (addDelBtn)
        {
            delRecBtn = UIHelper.createIconBtn("DeleteRecord", null, null);
            delRecBtn.setToolTipText(createTooltip("RemoveRecordTT", objTitle));
            delRecBtn.setMargin(insets);
            btnsHash.put(CommandType.DelItem, delRecBtn);
            
            rowBuilder.add(delRecBtn, cc.xy(row,1));
            row += 2;
        }
        
        if (addSearchBtn)
        {
            searchRecBtn = UIHelper.createIconBtn("Search", IconManager.IconSize.Std16, null, null);
            searchRecBtn.setToolTipText(createTooltip("SearchForRecordTT", objTitle));
            searchRecBtn.setMargin(insets);
            rowBuilder.add(searchRecBtn, cc.xy(row,1));
            row += 2;
        }
        
 
        firstBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                notifyListenersAboutToChangeIndex(currentInx, 0);
                currentInx = 0;
                updateUI();
                notifyListeners();
            }
        });
        prevBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                notifyListenersAboutToChangeIndex(currentInx, currentInx-1);
                currentInx--;
                updateUI();
                notifyListeners();
            }
        });
        nextBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                notifyListenersAboutToChangeIndex(currentInx, currentInx+1);
                currentInx++;
                updateUI();
                notifyListeners();
            }
        });
        lastBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                notifyListenersAboutToChangeIndex(currentInx, lastInx);
                currentInx = lastInx;
                updateUI();
                notifyListeners();
            }
        });
        
        // Make sure it gets centered
        rowBuilder.getPanel().setOpaque(false);
        DefaultFormBuilder outerCenteredPanel = new DefaultFormBuilder(new FormLayout("c:p:g", "p"));
        outerCenteredPanel.add(rowBuilder.getPanel(), cc.xy(1,1));
        panel = outerCenteredPanel.getPanel();    
        panel.setOpaque(false);
    }
    
    /**
     * Sets whether the record controller should be enabled
     * @param enabled true enabled, false not enabled
     */
    public void setEnabled(final boolean enabled)
    {
        if (enabled)
        {
            currentInx = 0;
            
        } else
        {
            currentInx = -1;
            numRecords = 0;
            lastInx    = -1;
        }
        updateUI();
    }

    /**
     * Sets a new length or size of the controller and adjust the current index to zero or -1.
     * @param len the new length
     */
    public void setLength(final int len)
    {
        currentInx = len > 0 ? 0 : -1;
        numRecords = len;
        lastInx    = numRecords - 1;
        updateUI(); 
    }
    
    /**
     * Sets the controller to a new index
     * @param index the new index
     */
    public void setIndex(final int index, final boolean doIndexNotify)
    {
        if (index < numRecords)
        {
            int oldInx = currentInx;
            currentInx = index;
            updateUI(); 
            if (doIndexNotify)
            {
                notifyListenersAboutToChangeIndex(oldInx, currentInx);
            }
            notifyListeners();
        }
    }
    
    
    /**
     * Sets the controller to a new index
     * @param index the new index
     */
    public void setIndex(final int index)
    {
        setIndex(index, true);
    }
    
    
    /**
     * 
     * @return the length (the number of records)
     */
    public int getLength()
    {
        return numRecords;
    }
    
    /**
     * @return the current index
     */
    public int getCurrentIndex()
    {
        return currentInx;
    }
    

    /**
     * @return the panel
     */
    public JPanel getPanel()
    {
        return panel;
    }
    
    
    /**
     * Enables/Disables the UI according to where we are in the resultset
     */
    protected void updateUI()
    {
        if (panel == null) return;
        
        firstBtn.setEnabled(currentInx > 0);
        prevBtn.setEnabled(currentInx > 0);
        nextBtn.setEnabled(currentInx < lastInx);
        lastBtn.setEnabled(currentInx < lastInx);
        
        boolean enabled = numRecords > 0;
        
        recDisp.setEnabled(enabled);
        recDisp.setBorder(enabled ? enabledBorder : disabledBorder);
        //recDisp.setBackground(enabled ? enabledTxtBG : disabledTxtBG);
        recDisp.setText(numRecords > 0 ? ((currentInx+1) + " of " + numRecords) : " "); // XXX Move to I18N properties file formatted
        
        if (delRecBtn != null)
        {
            delRecBtn.setEnabled(numRecords > 0);
        }
        
        panel.validate();
    }
    
    /**
     * Returns the JButton that is used to create new records.
     * @return the JButton that is used to create new records
     */
    public JButton getNewRecBtn()
    {
        return newRecBtn;
    }

    /**
     * Returns the JButton that is used to create new records.
     * @return the JButton that is used to create new records
     */
    public JButton getDelRecBtn()
    {
        return delRecBtn;
    }

    /**
     * Returns the JButton that is used to search for existing records.
     * @return the JButton that is used to search for existing records.
     */
    public JButton getSearchRecBtn()
    {
        return searchRecBtn;
    }

    /**
     * Adds a listener.
     * @param l the listener
     */
    public void addListener(ResultSetControllerListener l)
    {
        listeners.add(l);
    }
    
    /**
     * Remove a listener.
     * @param l the listener
     */
    public void removeListener(ResultSetControllerListener l)
    {
        listeners.remove(l);
    }
    
    /**
     * Remove all the listeners.
     */
    public void removeAllListeners()
    {
        listeners.clear();
    }
    
    /**
     * Notifies all the listeners that the index has changed.
     */
    protected void notifyListeners()
    {
        for (ResultSetControllerListener rscl : listeners)
        {
            rscl.indexChanged(currentInx);
        }
    }
    
    /**
     * Notifies all the listeners that the index has changed.
     * @param oldIndex the old index
     * @param newIndex the new index
     */
    protected void notifyListenersAboutToChangeIndex(final int oldIndex, final int newIndex)
    {
        for (ResultSetControllerListener rscl : listeners)
        {
            rscl.indexAboutToChange(oldIndex, newIndex);
        }
    }
    
    /**
     * Sets all the UI Enabled/Disabled
     * @param enabled true/false
     */
    protected void setUIEnabled(final boolean enabled)
    {
        if (!enabled)
        {
            firstBtn.setEnabled(enabled);
            prevBtn.setEnabled(enabled);
            nextBtn.setEnabled(enabled);
            lastBtn.setEnabled(enabled);
            recDisp.setEnabled(enabled);
            
            if (newRecBtn != null)
            {
                newRecBtn.setEnabled(enabled);
            }
            if (delRecBtn != null)
            {
                delRecBtn.setEnabled(enabled);
            }
        } else
        {
           updateUI(); 
        }
    }

    /**
     * This is not static because it makes it easier to create the RSAction objects.
     */
    protected void createRSActions()
    {
        if (commandsHash == null)
        {
            commandsHash = new Hashtable<CommandType, RSAction<CommandType>>();
            for (CommandType cmdType : CommandType.values())
            {
                RSAction<CommandType> action = new RSAction<CommandType>(cmdType);
                commandsHash.put(cmdType, action);
            }
        }
    }
    
    /**
     * Sets the current (or focused ResultSetController) into the action btns.
     * @param rsc ResultSetController
     */
    protected static void installRS(final ResultSetController rsc)
    {
        if (commandsHash == null)
        {
            rsc.createRSActions();
        }
        
        for (RSAction<CommandType> rsca : commandsHash.values())
        {
            rsca.setRs(rsc);
            
            if (rsc != null)
            {
                rsca.setBtn(rsc.btnsHash.get(rsca.getType()));
            }
        }
    }
    
    /**
     * 
     */
    private static void registerFocusListener()
    {
        final KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager(); 
        focusManager.addPropertyChangeListener( 
            new PropertyChangeListener() { 
                public void propertyChange(PropertyChangeEvent e) 
                {
                    Component permanentFocusOwner = null;
                    String propName = e.getPropertyName(); 
                    if (propName.equals("permanentFocusOwner"))
                    {
                        permanentFocusOwner = focusManager.getFocusOwner();
                    }
                    Component comp = permanentFocusOwner;
                    while (comp != null && !(comp instanceof MultiView))
                    {
                        comp = comp.getParent();
                    }
                    
                    ResultSetController rsc = null;
                    boolean             fnd = false;
                    if (comp instanceof MultiView)
                    {
                        FormViewObj fvo = ((MultiView)comp).getCurrentViewAsFormViewObj();
                        if (fvo != null && fvo.getRsController() != null)
                        {
                            rsc = fvo.getRsController();
                            if (currentFocusedRS == null || currentFocusedRS != rsc)
                            {
                                currentFocusedRS = rsc;
                                fnd              = true;
                            }
                        } 
                    }
                    
                    if (!fnd)
                    {
                        currentFocusedRS = backStopRS;
                    }
                    
                    installRS(currentFocusedRS);
                    
                } 
            } 
        );
    }
    
    /**
     * Gets the Mneu Integer from a Key to I18N
     * @param key the Mneu key
     * @return the Integer
     */
    protected static Integer getMneuInt(final String key)
    {
        String str = getResourceString(key);
        if (StringUtils.isNotEmpty(str))
        {
            return new Integer(str.charAt(0));
        }
        return null;
    }
    
    /**
     * Helper method for creating the menu items.
     * @param menuItem the parent menu it for the menu items
     * @param titleKey the I18N key for the button title
     * @param menuKey the I18N key for the mneumonic 
     * @param cmdType the command
     */
    protected static void createMenuItem(final JMenuItem menuItem,
                                         final String titleKey, 
                                         final String menuKey, 
                                         final CommandType cmdType)
    {
        Integer menuInt = getMneuInt(menuKey);
        JMenuItem mi = new JMenuItem(UIRegistry.getInstance().makeAction(commandsHash.get(cmdType), 
                getResourceString(titleKey), null, "", 
                menuInt,
                KeyStroke.getKeyStroke(menuInt, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())));
        menuItem.add(mi);
    }
    
    /**
     * Add special MenuItems to the data menu.
     * @param mi the data menu
     */
    public static void addMenuItems(final JMenuItem mi)
    {
        ResultSetController rsc = new ResultSetController();
        rsc.createRSActions();
        
        createMenuItem(mi, "DATA_FIRST",    "DATA_FIRST_MNEU",    CommandType.First);
        createMenuItem(mi, "DATA_PREVIOUS", "DATA_PREVIOUS_MNEU", CommandType.Previous);
        createMenuItem(mi, "DATA_NEXT",     "DATA_NEXT_MNEU",     CommandType.Next);
        createMenuItem(mi, "DATA_LAST",     "DATA_LAST_MNEU",     CommandType.Last);
    }        

    /**
     * @param backStopRS the backStopRS to set
     */
    public static void setBackStopRS(ResultSetController backStopRS)
    {
        ResultSetController.backStopRS = backStopRS;
        installRS(backStopRS);
    }
    
    //----------------------------------------------------------------------------
    //--
    //----------------------------------------------------------------------------
    class RSAction<T> extends AbstractAction implements PropertyChangeListener
    {
        protected CommandType        type;
        protected ResultSetController rs  = null;
        protected JButton             btn = null;
        
        public RSAction(final CommandType type)
        {
            super();
            this.type = type;
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            if (rs != null)
            {
                if (btn != null)
                {
                    btn.doClick();
                }
            }
        }
        
        public void setEnabled(final boolean enabled)
        {
            if (!enabled || rs == null)
            {
                super.setEnabled(false);
                
            } else if (btn != null)
            {
                super.setEnabled(btn.isEnabled());
            } else
            {
                super.setEnabled(false);
            }
        }

        /**
         * @return the btn
         */
        public JButton getBtn()
        {
            return btn;
        }

        /**
         * @param btn the btn to set
         */
        public void setBtn(JButton btn)
        {
            if (this.btn != null)
            {
                this.btn.removePropertyChangeListener(this);
            }
            
            this.btn = btn;
            
            if (this.btn != null)
            {
                super.setEnabled(this.btn.isEnabled());
                this.btn.addPropertyChangeListener(this);
            }
        }

        /**
         * @return the type
         */
        public CommandType getType()
        {
            return type;
        }

        /**
         * @return the rs
         */
        public ResultSetController getRs()
        {
            return rs;
        }

        /**
         * @param rs the rs to set
         */
        public void setRs(ResultSetController rs)
        {
            this.rs = rs;
            setEnabled(rs != null);
        }

        /* (non-Javadoc)
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt.getPropertyName().equals("enabled"))
            {
                super.setEnabled((Boolean)evt.getNewValue());
            }
        }
        
    }
    
    //-----------------------------------------------------
    // ValidationListener
    //-----------------------------------------------------

   /* (non-Javadoc)
     * @see ValidationListener#wasValidated(UIValidator)
     */
    public void wasValidated(final UIValidator validator)
    {
        if (formValidator != null && firstBtn.isEnabled() != formValidator.isFormValid())
        {
            setUIEnabled(formValidator.isFormValid());
        }

    }

}
