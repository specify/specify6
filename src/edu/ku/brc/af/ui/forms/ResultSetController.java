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
package edu.ku.brc.af.ui.forms;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValidationListener;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.UIHelper.CommandType;
import org.apache.log4j.Logger;

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
    private static final Logger log = Logger.getLogger(ResultSetController.class);

    protected static Border enabledBorder  = BorderFactory.createLineBorder(new Color(64,64,64));
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
    
    protected boolean isNewObj   = false;
    protected boolean doLayoutBtns;
    
    // Global Key Actions
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
                               final int     len,
                               final boolean doLayoutBtns)
    {
        this.formValidator = formValidator;
        this.doLayoutBtns  = doLayoutBtns;
        
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
     * 
     */
    public void setupGotoListener()
    {
        KeyStroke gotoKS     = KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        String    ACTION_KEY = "GOTO";
        InputMap  inputMap   = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap  = panel.getActionMap();
        
        inputMap.put(gotoKS, ACTION_KEY);
        actionMap.put(ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showGotoRecDlg();
            }
        });
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

        recDisp  = createLabel("  ");
        recDisp.setHorizontalAlignment(SwingConstants.CENTER);
        recDisp.setOpaque(true);
        recDisp.setBackground(Color.WHITE);
        recDisp.setBorder(enabledBorder);
        recDisp.setFont(recDisp.getFont().deriveFont(recDisp.getFont().getSize2D()-2));
        
        MouseListener mouseListener = new MouseAdapter() 
        {
              private boolean showIfPopupTrigger(MouseEvent mouseEvent) 
              {
                  if (mouseEvent.isPopupTrigger())
                  {
                      JPopupMenu popupMenu = createPopupMenu();
                      if (popupMenu != null && popupMenu.getComponentCount() > 0) 
                      {
                          popupMenu.show(mouseEvent.getComponent(),
                                  mouseEvent.getX(),
                                  mouseEvent.getY());
                          return true;
                      }
                  }
                  return false;
              }
              @Override
              public void mousePressed(MouseEvent mouseEvent) 
              {
                  showIfPopupTrigger(mouseEvent);
              }
              @Override
              public void mouseReleased(MouseEvent mouseEvent) 
              {
                  showIfPopupTrigger(mouseEvent);
              }
            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    if (numRecords == 1)
                    {
                        UIRegistry.writeTimedSimpleGlassPaneMsg(getResourceString("OnlyOneRrecordInCon"));
                    } else
                    {
                        showGotoRecDlg();
                    }
                }
            }
        };
        recDisp.addMouseListener(mouseListener);

        
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
        int col = 11;
        
        if (addNewBtn)
        {
            if (false)
            {
                newRecBtn = new JButton("+")
                {
                    public void setEnabled(boolean enable)
                    {
                        //log.debug("newRecBtn - RS: "+ formValidator.getName() + " " + newRecBtn.hashCode() + " "+enable+"  isNewObj: "+isNewObj);
                        if (formValidator != null && formValidator.getName() != null && formValidator.getName().equals("Collection Object"))
                        {
                            int x = 0;
                            x++;
                            if (enable)
                            {
                                int y = 0;
                                y++;
                            }
                        }
                        if (enable)
                        {
                            int x = 0;
                            x++;
                        }
                        super.setEnabled(enable);
                    }
                };
                setControlSize(newRecBtn);

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

            if (doLayoutBtns)
            {
                rowBuilder.add(newRecBtn, cc.xy(col,1));
                col += 2;
            }
        }
        
        if (addDelBtn)
        {
            if (false)
            {
                delRecBtn = new JButton("-")
                {
                    public void setEnabled(boolean enable)
                    {
                        //log.debug("delRecBtn - RS: "+formValidator.getName() + " " + hashCode() + " "+enable);
                        if (formValidator != null && formValidator.getName() != null && formValidator.getName().equals("Permit"))
                        {
                            int x = 0;
                            x++;
                        }
                        if (!enable)
                        {
                            int x = 0;
                            x++;
                        }
                        super.setEnabled(enable);
                    }
                };
            } else
            {
                delRecBtn = UIHelper.createIconBtn("DeleteRecord", null, null);    
            }
            delRecBtn.setToolTipText(createTooltip("DeleteRecordTT", objTitle));
            delRecBtn.setMargin(insets);
            btnsHash.put(CommandType.DelItem, delRecBtn);
            
            if (doLayoutBtns)
            {
                rowBuilder.add(delRecBtn, cc.xy(col,1));
                col += 2;
            }
        }
        
        if (addSearchBtn)
        {
            searchRecBtn = UIHelper.createIconBtn("Search", IconManager.IconSize.Std16, null, null);
            searchRecBtn.setToolTipText(createTooltip("SearchForRecordTT", objTitle));
            searchRecBtn.setMargin(insets);
            
            if (doLayoutBtns)
            {
                rowBuilder.add(searchRecBtn, cc.xy(col,1));
                col += 2;
            }
        }
        
 
        firstBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                firstRecord();
            }
        });
        prevBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                prevRecord();
            }
        });
        nextBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                nextRecord();
            }
        });
        lastBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                lastRecord();
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
     * 
     */
    protected JPopupMenu createPopupMenu()
    {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem  mi = UIHelper.createLocalizedMenuItem("Go to Record", null, null, true, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showGotoRecDlg();
            }
        });
        popupMenu.add(mi);
        return popupMenu;
    }
    
    /**
     * 
     */
    private void firstRecord()
    {
        if (notifyListenersAboutToChangeIndex(currentInx, 0))
        {
            currentInx = 0;
            updateUI();
            notifyListeners();
        }

    }
    
    /**
     * 
     */
    private void lastRecord()
    {
        if (notifyListenersAboutToChangeIndex(currentInx, lastInx))
        {
            currentInx = lastInx;
            updateUI();
            notifyListeners();
        }

    }
    
    /**
     * 
     */
    public void prevRecord()
    {
        if (currentInx < 1) return;
        
        setUIEnabled(false);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (notifyListenersAboutToChangeIndex(currentInx, currentInx-1))
                {
                    // Note: notifyListenersAboutToChangeIndex sometimes can call a method
                    // that ends up setting the currentInx and therefore we should make
                    // sure that by decrementing it will still have a good value
                    if (currentInx > 0)
                    {
                        currentInx--;
                        updateUI();
                        notifyListeners();
                    }
                }
            }
        });
    }
    
    public void nextRecord()
    {
        if (currentInx > numRecords-2) return;
        
        setUIEnabled(false);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (notifyListenersAboutToChangeIndex(currentInx, currentInx+1))
                {
                    currentInx++;
                    updateUI();
                    notifyListeners();
                }
            }
        });
    }
    
    /**
     * 
     */
    protected void showGotoRecDlg()
    {
        JTextField tf = UIHelper.createTextField(5);
        
        CellConstraints    cc = new CellConstraints();
        DefaultFormBuilder p  = new DefaultFormBuilder(new FormLayout("p,2px,f:p:g", "p"));
        p.add(UIHelper.createI18NLabel("RS_NUM_LBL"), cc.xy(1,1));
        p.add(tf, cc.xy(3,1));
        p.setDefaultDialogBorder();
        
        CustomDialog dlg = CustomDialog.create("", true, CustomDialog.OKCANCEL, p.getPanel());
        dlg.setCustomTitleBar(getResourceString("RS_JUMP_TITLE"));
        
        UIHelper.centerAndShow(dlg);
        if (!dlg.isCancelled())
        {
            String recNumStr = tf.getText();
            if (!recNumStr.isEmpty() && StringUtils.isNumeric(recNumStr))
            {
                try
                {
                    int recNum = Integer.parseInt(recNumStr);
                    if (recNum > 0 && recNum <= lastInx)
                    {
                        setIndex(recNum-1);
                    }
                } catch (Exception ex) {}
            }
        }
    }
    
    /**
     * Resets the current index to zero.
     * @param enabled true enabled, false not enabled
     */
    public void reset()
    {
        currentInx = 0;
            
        updateUI();
    }

    /**
     * Set the the controller to have no items and the number of records to zero.
     * @param enabled true enabled, false not enabled
     */
    public void clear()
    {
        currentInx = -1;
        numRecords = 0;
        lastInx    = -1;
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
    public void updateUI()
    {
        if (panel == null) return;
        
        // This needs to be done first because the enabling of the other buttons
        // may rely on the 'New Btn'
        if (newRecBtn != null)
        {
            //log.debug("updateUI - RS - formValidator.isTopLevel() "+formValidator.isTopLevel()+" isEnabled() "+formValidator.isEnabled());
            boolean enable = formValidator == null ? false : (formValidator.isTopLevel() || 
                    (formValidator.getParent() != null ? formValidator.getParent().isEnabled() : formValidator.isEnabled()));
            //this is a debugging aid for still open bug #247
            if (formValidator != null && formValidator.getName().equals("PreparationProperty")) {
                if (!enable && newRecBtn.isEnabled()) {
                    log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!DISABLING PP + !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    new Exception().printStackTrace();
                    log.debug("------------------------- END DISABLING PP + STACK ---------------------------------------------------------");
                }
            }
            newRecBtn.setEnabled(enable);
        }
                
        boolean isNewAndValid = newRecBtn == null ? true : newRecBtn.isEnabled();
        
        /*boolean isNewAndValid = true;
        if (formValidator != null)
        {
            isNewAndValid = !isNewObj || (isNewObj && formValidator.isFormValid());
        }*/
        //System.err.println("isNewObj "+isNewObj+" isNewAndValid "+isNewAndValid+" currentInx "+currentInx);
        
        
        firstBtn.setEnabled(currentInx > 0 && isNewAndValid);
        prevBtn.setEnabled(currentInx > 0 && isNewAndValid);
        nextBtn.setEnabled(currentInx < lastInx && isNewAndValid);
        lastBtn.setEnabled(currentInx < lastInx && isNewAndValid);
        
        boolean enabled = numRecords > 0;
        
        recDisp.setEnabled(enabled);
        recDisp.setBorder(enabled ? enabledBorder : disabledBorder);
        //recDisp.setBackground(enabled ? enabledTxtBG : disabledTxtBG);
        String lbl = UIRegistry.getLocalizedMessage("RecordControllerDisplay", (currentInx+1), numRecords);
        recDisp.setText(numRecords > 0 ? lbl : " "); // XXX Move to I18N properties file formatted
        
        if (delRecBtn != null)
        {
            delRecBtn.setEnabled(numRecords > 0);
        }
 
        panel.validate();
    }
    
    /**
     * @return the recDisp
     */
    public JLabel getRecDisp()
    {
        return recDisp;
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
     * Notifies all the listeners that the index has changed. But any one listener can return
     * false that it isn't OK and then it stops and returns false.
     * @param oldIndex the old index
     * @param newIndex the new index
     * @return returns whether it was ok to change indexes
     */
    protected boolean notifyListenersAboutToChangeIndex(final int oldIndex, final int newIndex)
    {
        for (ResultSetControllerListener rscl : listeners)
        {
            boolean isOK = rscl.indexAboutToChange(oldIndex, newIndex);
            if (!isOK)
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Sets all the UI Enabled/Disabled
     * @param enabled true/false
     */
    public void setUIEnabled(final boolean enabled)
    {
        if (!enabled)
        {
            firstBtn.setEnabled(false);
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            lastBtn.setEnabled(false);
            recDisp.setEnabled(false);
            
            if (newRecBtn != null)
            {
                if (formValidator.getName().equals("PreparationProperty")) {
                    if (!enabled && newRecBtn.isEnabled()) {
                        log.debug("!!!!!!!!!!!!!!!!!!!!!!!!!DISABLING PP + !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        new Exception().printStackTrace();
                        log.debug("------------------------- END DISABLING PP + STACK ---------------------------------------------------------");
                    }
                }
                newRecBtn.setEnabled(false);
            }
            if (delRecBtn != null)
            {
                delRecBtn.setEnabled(false);
            }
        } else
        {
           updateUI(); 
        }
    }

    /**
     * @return the isNewObj
     */
    public boolean isNewObj()
    {
        return isNewObj;
    }

    /**
     * @param isNewObj the isNewObj to set
     */
    public void setNewObj(boolean isNewObj)
    {
        //System.err.println("this.isNewObj "+this.isNewObj+ " isNewObj "+isNewObj);
        this.isNewObj = isNewObj;
        updateUI();
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
        if (commandsHash == null && rsc != null)
        {
            rsc.createRSActions();
        }
        
        if (commandsHash != null)
        {
            for (RSAction<CommandType> rsca : commandsHash.values())
            {
                rsca.setRs(rsc);
                
                if (rsc != null)
                {
                    JButton btn = rsc.btnsHash.get(rsca.getType());
                    if (btn != null)
                    {
                        KeyStroke ks         = UIHelper.getKeyStroke(rsca.getType());
                        String    ACTION_KEY = rsca.getType().toString();
                        InputMap  inputMap   = btn.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                        ActionMap actionMap  = btn.getActionMap();
                        
                        inputMap.put(ks, ACTION_KEY);
                        actionMap.put(ACTION_KEY, rsca);
                        rsca.setBtn(btn);
                    } else
                    {
                        //System.err.println("Btn for ["+rsca.getType()+"] is null");
                    }
                }
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
    protected static void createMenuItem(final JMenuItem   menuItem,
                                         final String      titleKey, 
                                         final CommandType cmdType)
    {
        KeyStroke ks = UIHelper.getKeyStroke(cmdType);
        JMenuItem mi = new JMenuItem(UIRegistry.getInstance().makeAction(commandsHash.get(cmdType), 
                                            getResourceString(titleKey), null, "", 
                                            ks.getKeyCode(),
                                            ks));
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
        
        createMenuItem(mi, "DATA_FIRST",    CommandType.First);
        createMenuItem(mi, "DATA_PREVIOUS", CommandType.Previous);
        createMenuItem(mi, "DATA_NEXT",     CommandType.Next);
        createMenuItem(mi, "DATA_LAST",     CommandType.Last);
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
        protected CommandType         type;
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
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (rs != null && btn != null)
            {
                // rods (04/26/11) NOTE: the 'doClick' on the button does not work.
                // I think it has to do with the event being dispatched on the UI thread.
                // With this change the call to change the index gets call directly
                //btn.doClick();
                switch (type)
                {
                    case First:
                        rs.firstRecord();
                        break;
                    case Previous:
                        rs.prevRecord();
                        break;
                    case Next:
                        rs.nextRecord();
                        break;
                    case Last:
                        rs.lastRecord();
                        break;
                    case Save:
                        break;
                    case NewItem:
                        break;
                    case DelItem:
                        break;
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
