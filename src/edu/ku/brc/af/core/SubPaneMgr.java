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
package edu.ku.brc.af.core;

import static edu.ku.brc.ui.UIRegistry.getAction;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getStatusBar;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.registerAction;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.ui.ExtendedTabPanel;
import edu.ku.brc.ui.ExtendedTabbedPane;
import edu.ku.brc.ui.UIHelper;

/**
 * Manages all the SubPanes that are in the main Tabbed pane. It notifies listeners when SubPanes are added, removed or Shown.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class SubPaneMgr extends ExtendedTabbedPane implements ChangeListener
{
    private static final Logger log = Logger.getLogger(SubPaneMgr.class);
    
    private static final SubPaneMgr instance = new SubPaneMgr();

    protected enum NotificationType {Added, Removed, Shown}

    // Data Members
    protected Hashtable<String, SubPaneIFace> panes = new Hashtable<String, SubPaneIFace>();
    protected SubPaneIFace             currentPane = null;

    protected List<SubPaneMgrListener> listeners = new ArrayList<SubPaneMgrListener>();
    protected boolean                  globalShutdown = false;
    
    protected JPopupMenu               popupMenu         = null;
    protected MouseAdapter             popupMouseAdapter = null;

    /**
     * Singleton Constructor.
     *
     */
    protected SubPaneMgr()
    {
        // This way we notifications that the tabs have changed
        addChangeListener(this);

        setOpaque(true); // this is so the tabs are painted correctly against the BG color of the TabbedPane
        
        createTabActions();
        createContextMenu();
       
        popupMouseAdapter = new MouseAdapter() 
        {
            private void showPopup(MouseEvent e) 
            {
                if (e.isPopupTrigger()) 
                {
                    int y = e.getY() - popupMenu.getSize().height;
                    popupMenu.show(e.getComponent(), e.getX(), y < 0 ? e.getY() : y);
                }
             }
            
            @Override
            public void mousePressed(MouseEvent e)
            {
                super.mousePressed(e);
                showPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                super.mouseReleased(e);
                showPopup(e);
            }
        };
        
        addMouseListener(popupMouseAdapter);
    }

    /**
     * Returns the reference to the singleton.
     * @return the reference to the singleton
     */
    public static SubPaneMgr getInstance()
    {
        return instance;
    }
    
    /**
     * Adds a Close Btn to the Tab.
     * @param title the title of the tab
     * @param icon the icon for the tab (can be null)
     * @param comp the component tab
     * @param index the index of the tab to be fixed
     */
    protected ExtendedTabPanel adjustTab(final String    title, 
                                         final Icon      icon, 
                                         final Component comp, 
                                         final int       index)
    {
        ExtendedTabPanel tabUI = super.adjustTab(title, icon, comp, index);
        /*if (comp instanceof SubPaneIFace)
        {
            //  just making sure we don't add it twice
            boolean fnd = false;
            for (MouseListener ml : tabUI.getMouseListeners())
            {
                if (ml == popupMouseAdapter)
                {
                    fnd = true;
                    break;
                }
            }
            if (!fnd)
            {
                tabUI.addMouseListener(popupMouseAdapter); 
            }
        }*/
        return tabUI;
    }
    
    /**
     * Returns a unique tab name based on the generic tab name.
     * @param paneName the "generic" name of the panel (without the "()")
     * @return the new unique name with possibly parenthesis
     */
    protected String buildUniqueName(final String paneName)
    {
        String title = paneName;
        boolean nameInUse = (panes.get(paneName) != null) ? true : false;
        int     index     = 2;
        while (nameInUse)
        {
            title = paneName + "("+index+")"; //$NON-NLS-1$ //$NON-NLS-2$
            nameInUse = (panes.get(title) != null) ? true : false;
            index++;
        }
        return title;
    }

    /**
     * Adds the sub pane and return the same one it added. It's ok to add the same
     * pane in twice, it will detect it, show it, and then return.
     * @param pane the pane to be added
     * @return the same pane
     */
    public SubPaneIFace addPane(final SubPaneIFace pane)
    {
        if (pane == null)
        {
            throw new NullPointerException("Null name or pane when adding to SubPaneMgr"); //$NON-NLS-1$
        }
        
        int maxNumPanes = AppPreferences.getRemote().getInt("SubPaneMgr.MaxPanes", 10); //$NON-NLS-1$
        if (getComponentCount() >= maxNumPanes)
        {
            boolean doAsk = AppPreferences.getRemote().getBoolean("tabs.askb4close", false); //$NON-NLS-1$
            if (doAsk)
            {
                SwingUtilities.invokeLater(new Runnable() {
    
                    /* (non-Javadoc)
                     * @see java.lang.Runnable#run()
                     */
                    public void run()
                    {
                        Object[] options = { getResourceString("SubPaneMgr.SUBPANE_OPTIONS_CLOSE_ALLBUT"),  //$NON-NLS-1$
                                getResourceString("SubPaneMgr.SUBPANE_OPTIONS_CLOSE_OLDEST")  //$NON-NLS-1$
                              };
                        int userChoice = JOptionPane.showOptionDialog(getTopWindow(), 
                                                                     getResourceString("SubPaneMgr.SUBPANE_OPTIONS_MAX_TABS"),  //$NON-NLS-1$
                                                                     getResourceString("SubPaneMgr.SUBPANE_OPTIONS_TOO_MANY"),  //$NON-NLS-1$
                                                                     JOptionPane.YES_NO_CANCEL_OPTION,
                                                                     JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                        if (userChoice == JOptionPane.YES_OPTION)
                        {
                            closeAll(false); // false means don't close current tab
                           
                        } else
                        {
                            closeOldestPane();
                        }                    
                    }
                });
            } else
            {
                closeOldestPane();
            }
        }
        
        Component firstFocusable = pane.getFirstFocusable();
        if (firstFocusable != null)
        {
            //log.error("firstFocusable: "+firstFocusable);
            pane.getUIComponent().addComponentListener(new TabSelectionFocusGainListener(firstFocusable));
        }
        
        getStatusBar().setText(""); //$NON-NLS-1$
        
        if (instance.panes.contains(pane))
        {
            showPane(pane);
            adjustCloseAllMenu();
            return pane;
        }

        // Add this pane to the tabs
        pane.setPaneName(buildUniqueName(pane.getPaneName()));

        //log.debug("Putting SubPane ["+pane.getPaneName()+"] ");
        panes.put(pane.getPaneName(), pane); // this must be done before adding it
        addTab(pane.getPaneName(), pane.getIcon(), pane.getUIComponent());
        
        notifyListeners(NotificationType.Added, pane);

        if (getTabCount() > 0) // should never happen  that it is zero
        {
            this.setSelectedIndex(getTabCount()-1);
        }
        
        adjustCloseAllMenu();

        return pane;
    }
    
    /**
     * Finds the Oldest 'best' pane and closes it. First anything without a form, then anything with a form that is in view mode,
     * then a form in edit mode that isn't changed, then whatever is the oldest.
     */
    protected void closeOldestPane()
    {
        SubPaneIFace pane = getOldestPane();
        if (pane != null)
        {
            removePane(pane);
        }
    }
    
    /**
     * @return the old pane see {@link #closeOldestPane()}
     */
    protected SubPaneIFace getOldestPane()
    {
        ArrayList<SubPaneIFace> subPanes = new ArrayList<SubPaneIFace>(instance.panes.values());
        Collections.sort(subPanes, new Comparator<SubPaneIFace>() {
            @Override
            public int compare(SubPaneIFace o1, SubPaneIFace o2)
            {
                return o1.getCreateTime().compareTo(o2.getCreateTime());
            }
        });
        
        // Anything without a form
        for (SubPaneIFace sp : subPanes)
        {
            if (sp.getMultiView() == null)
            {
                return sp;
            }
            
        }
        
        // Check viewable forms
        for (SubPaneIFace sp : subPanes)
        {
            if (sp.getMultiView() != null && !sp.getMultiView().isEditable())
            {
                return sp;
            }
            
        }
        
        // Check editable forms that haven't changed
        for (SubPaneIFace sp : subPanes)
        {
            if (sp.getMultiView() != null && sp.getMultiView().isEditable() && !sp.getMultiView().hasChanged())
            {
                return sp;
            }
        }
        
        // take the oldest subpane
        return subPanes.get(0);
    }
    

    /**
     * Enables the Close All action as to whether there state is right in the UI. 
     */
    protected void adjustCloseAllMenu()
    {
        Action closeCurrent = getAction("CloseCurrent"); //$NON-NLS-1$
        if (closeCurrent != null)
        {
            closeCurrent.setEnabled(panes.size() > 0);//  || TaskMgr.getToolbarTaskCount() > 1);
        }        
        
        Action closeAll = getAction("CloseAll"); //$NON-NLS-1$
        if (closeAll != null)
        {
            closeAll.setEnabled(panes.size() > 0);//  || TaskMgr.getToolbarTaskCount() > 1);
        }        
        
        Action closeAllBut = getAction("CloseAllBut"); //$NON-NLS-1$
        if (closeAllBut != null)
        {
            closeAllBut.setEnabled(panes.size() > 1);//  || TaskMgr.getToolbarTaskCount() > 1);
        }
        
        checkForTaskableConfig();
    }
    
    /**
     * Renames the pane, internally and the tab.
     * @param pane the pane to be renamed
     * @param newName the new name for the tab
     * @return the same pane as the one renamed
     */
    public SubPaneIFace renamePane(final SubPaneIFace pane, final String newName)
    {
        // Makes we have a unique name, this shouldn't happen
        int    cnt        = 1;
        String newNameStr = newName;
        while (panes.get(newNameStr) != null)
        {
            newNameStr = newName + " " + Integer.toString(cnt); //$NON-NLS-1$
            cnt++;
        }
        
        getStatusBar().setText(""); //$NON-NLS-1$
        
        if (panes.get(pane.getPaneName()) != null)
        {
            panes.remove(pane.getPaneName());
            
            pane.setPaneName(newNameStr);
            
            int       index   = indexOfComponent(pane.getUIComponent());
            Component tabComp = getTabComponentAt(index);
            
            if (tabComp instanceof ExtendedTabPanel)
            {
                ((ExtendedTabPanel)tabComp).setTitle(pane.getPaneName());
            } else
            {
                this.setTitleAt(index, pane.getPaneName());
            }
            
            //log.debug("Putting SubPane ["+newName+"] ");
            panes.put(pane.getPaneName(), pane);
            
        } else
        {
            log.error("Couldn't find pane named["+pane.getPaneName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        return pane;
    }

    /**
     * Replaces  the "old pane" with a new pane, the main point here that the new gets inserted into the same position.
     * NOTE: aboutToShutdown() is not called for the "old pane".
     * @param oldPane the old pane to be replaced
     * @param newPane the new pane
     * @return the same pane as the one renamed
     */
    public synchronized SubPaneIFace replacePane(final SubPaneIFace oldPane, final SubPaneIFace newPane)
    {
        getStatusBar().setText(""); //$NON-NLS-1$
        
        //System.err.println("SubPaneMgr::replacePane ************************************************");

        // The caller is assuming that the pane is there to be replaced, but the SubPaneMgr can't
        // be responsible for that. So if the pane to be replaced is not found then it just adds 
        // SubPane straight away.
        SubPaneIFace fndPane = panes.get(oldPane.getPaneName());
        if (oldPane != fndPane)
        {
            //log.warn("Couldn't find Pane ["+oldPane.getPaneName()+"]");
            addPane(newPane);
            notifyListeners(NotificationType.Added, newPane);
            return newPane;
        }
        
        final int index = this.indexOfComponent(oldPane.getUIComponent());
        if (index < 0 || index >= this.getComponentCount())
        {
            log.error("Couldn't find index for panel ["+oldPane.getPaneName()+"] index was["+index+"] number tab["+this.getComponentCount()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }
        
        if (panes.get(oldPane.getPaneName()) != null)
        {
            notifyListeners(NotificationType.Added, newPane);
            
            this.insertTab(newPane.getPaneName(), newPane.getIcon(), newPane.getUIComponent(), null, index);
            
            String title = buildUniqueName(newPane.getPaneName());
            newPane.setPaneName(title);

            removePane(oldPane, false);
            
            //log.debug("Putting SubPane ["+newPane.getPaneName()+"] ");
            panes.put(newPane.getPaneName(), newPane);
            
            adjustCloseAllMenu();
            
            checkForTaskableConfig();
            
               SwingUtilities.invokeLater(() -> {
                   if (index > -1 && index < SubPaneMgr.this.getComponentCount()) {
                       setSelectedIndex(index);
                   }
               });
            
            return newPane;
            
        } 
        // else
        log.error("Couldn't find pane named["+oldPane.getPaneName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        
        return null;
    }

    /**
     * Checks for and enables the Configure Menu item for the current tab.
     */
    protected void checkForTaskableConfig()
    {
        if (currentPane != null)
        {
            Action configTaskAction = getAction("ConfigureTask"); //$NON-NLS-1$
            if (configTaskAction != null)
            {
                configTaskAction.setEnabled(currentPane.getTask().isConfigurable());
            }
        }
    }
    
    /**
     * Asks the pane if it can be closed via a call to aboutToShutdown and it it returns true
     * then the pane is closed and removed.
     * @param pane the pane to be removed
     * @return true if the pane was removed false if the user decided it shouldn't via a call to aboutToShutdown.
     */
    public boolean removePane(final SubPaneIFace pane)
    {
        return removePane(pane, true); // true means ask for save
    }


    /**
     * Asks the pane if it can be closed via a call to aboutToShutdown and it it returns true
     * then the pane is closed and removed.
     * @param pane the pane to be removed
     * @param askForSave can override asking whether it should be saved
     * @return true if the pane was removed false if the user decided it shouldn't via a call to aboutToShutdown.
     */
    public boolean removePane(final SubPaneIFace pane, final boolean askForSave)
    {
        return removePane(pane, askForSave, askForSave);
    }


    /**
     * Asks the pane if it can be closed via a call to aboutToShutdown and it it returns true
     * then the pane is closed and removed.
     * @param pane the pane to be removed
     * @param askForSave can override asking whether it should be saved
     * @param doAboutToShutdown should it call aboutToShutdown
     * @return true if the pane was removed false if the user decided it shouldn't via a call to aboutToShutdown.
     */
    protected synchronized boolean removePane(final SubPaneIFace pane, 
                                              final boolean      askForSave,
                                              final boolean      doAboutToShutdown)
    {
        getStatusBar().setText(""); //$NON-NLS-1$
        
        if (currentPane == pane)
        {
            pane.showingPane(false);
            currentPane = null;
        }
        
        boolean okToShutdown = !doAboutToShutdown || pane.aboutToShutdown();
        
        if (askForSave && !okToShutdown)
        {
            return false;
        }
        
        if (panes.get(pane.getPaneName()) != null)
        {
            notifyListeners(NotificationType.Removed, pane);
            
            // Sometimes things get out of wack, so catch the error
            // this usually only happens during testing when other
            // things have happened.
            try
            {
                
                //int inx = indexOfTab(pane.getTitle());
                //using pane.getTitle() leads to evil when removing tabs when tabs with duplicate titles exist. 
                //(BaseSubPane.getTitle() returns name which is not always the same as the JTabbedPane tab title for the pane)
                //so using indexOfComponent instead.
                int inx = indexOfComponent(pane.getUIComponent());
                
                //System.err.println(getTabCount()+"  "+inx+" - "+ getComponentCount()+"  " + indexOfComponent(pane.getUIComponent()));
                if (inx != -1 && inx < getTabCount())
                {
                    this.removeTabAt(inx);
                }
                pane.getUIComponent().setVisible(false);
                
            } catch (ArrayIndexOutOfBoundsException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SubPaneMgr.class, ex);
                System.err.println(getTabCount()+"   - "+ getComponentCount());
                
                log.error(ex);
                ex.printStackTrace();
            }
            
            panes.remove(pane.getPaneName());
            
            if (!globalShutdown)
            {
                // This let's focus get taken away.
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        pane.shutdown();
                    }
                });
            } else
            {
                pane.shutdown();
            }
            
        } else
        {
            log.error("Couldn't find pane named["+pane.getPaneName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
            
            // Note: We should never get here, but if we do...
            //
            // At this point we should bail but remove the current tab manually
            // it wasn't found in the panes list
            //
            SubPaneIFace subPane = this.getCurrentSubPane();
            if(subPane != null)
            {
                if (!subPane.aboutToShutdown())
                {
                    return false;
                }
                
                subPane.shutdown();
                try
                {
                    int inx = indexOfTab(pane.getTitle());
                    if (inx != -1 && inx < getTabCount())
                    {
                        this.removeTabAt(inx);
                    }
                    
                } catch (ArrayIndexOutOfBoundsException ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SubPaneMgr.class, ex);
                    log.error(ex);
                    ex.printStackTrace();
                }
            }
        }
        
        adjustCloseAllMenu();
        
        return true;
    }
    
    /**
     * Looks for the first SubPane it finds that is has the same task  and the UI is of SimpleDescPane and then replaces it.
     * If there are none then it adds it.
     * @param subPane the new subpane
     */
    public static void replaceSimplePaneForTask(final SubPaneIFace subPane)
    {
        for (SubPaneIFace sp : instance.panes.values())
        {
            if (sp.getTask() == subPane.getTask() && sp.getUIComponent() instanceof SimpleDescPane)
            {
                instance.replacePane(sp, subPane);
                return;
            }
        }
        instance.addPane(subPane);
    }

    /**
     * Returns a SubPaneIFace that currently contains the same RecordSet as the one in question.
     * @param rs the RecordSet in question
     * @return the subpane containing the RecordSet or null.
     */
    public static SubPaneIFace getSubPaneWithRecordSet(final RecordSetIFace rs)
    {
        SubPaneIFace subPane = null;
        if (rs != null)
        {
            for (SubPaneIFace sp : instance.panes.values())
            {
                RecordSetIFace sprs = sp.getRecordSet();
                if (sprs != null)
                {
                    if (sprs == rs)
                    {
                        return sp;
                    }

                    if (sprs.getRecordSetId() != null && 
                        rs.getRecordSetId() != null &&
                        sprs.getRecordSetId().equals(rs.getRecordSetId()))
                    {
                        return sp;
                    }
                    
                    if (sprs.getDbTableId().equals(rs.getDbTableId()) &&
                        sprs.getNumItems() == rs.getNumItems())
                    {
                        Hashtable<Integer, Boolean> hash = new Hashtable<Integer, Boolean>();
                        for (RecordSetItemIFace rsi : rs.getItems())
                        {
                            hash.put(rsi.getRecordId(), true);
                        }
                        
                        boolean allTheSame = true;
                        for (RecordSetItemIFace rsi : sprs.getItems())
                        {
                            if (hash.get(rsi.getRecordId()) == null)
                            {
                                allTheSame = false;
                                break;
                            }
                        }
                        if (allTheSame)
                        {
                            return sp;
                        }
                    }
                }
            }
        }
        return subPane;
    }
    
    /**
     * Finds a SubPane by name.
     * @param subPaneName the name of the SubPaneIFace
     * @return the SubPaneIFace with the name in question or null if not found
     */
    public SubPaneIFace getSubPaneByName(final String subPaneName)
    {
        for (SubPaneIFace sp : instance.panes.values())
        {
            if (subPaneName.equals(sp.getPaneName()))
            {
                return sp;
            }
        }
        return null;
    }
    
    /**
     * Returns a list of the SubPaneIFaces.
     * @return a list of the SubPaneIFaces.
     */
    public Collection<SubPaneIFace> getSubPanes()
    {
        return panes.values();
    }

    /**
     * Tell Each SubPane that it is about to be shutdown, if the SubPane passes back false then the shutdown stops.
     */
    public boolean aboutToShutdown()
    {
        globalShutdown = true;
        // Move all the elements to a List so the iterator on the Hashtable works correctly
        // if the a SubPane wants to remove itself
        List<SubPaneIFace> list = new ArrayList<SubPaneIFace>(panes.values());
        for (SubPaneIFace sp : list)
        {
            boolean ok = sp.aboutToShutdown();
            if (!ok)
            {
                globalShutdown = false;
                return false;
            }
            removePane(sp, false); // overrides asking to be saved.
        }
        
        globalShutdown = false;
        return true;
    }
    
    /**
     * @return whether there is a global shutdown.
     */
    public boolean isGlobalShutdown()
    {
        return globalShutdown;
    }

    /**
     * Show (makes visible) the pane by name.
     * @param name the name of the pane to be shown
     * @return the pane that is now shown
      */
    public SubPaneIFace showPane(final String name)
    {
        getStatusBar().setText(""); //$NON-NLS-1$
        
        // Look the the desired pane
        SubPaneIFace pane = panes.get(name);
        return showPane(pane);
    }
    
    /**
     * Shows a SubPane
     * @param pane the SubPane to be shown
     * @return the SubPane that was passed in
     */
    public SubPaneIFace showPane(final SubPaneIFace pane)
    {
        if (pane != null)
        {
            // Notify the current pane it is about to be hidden
            SubPaneIFace oldPane = getCurrentSubPane();
            if (oldPane != null && oldPane != pane)
            {
                oldPane.showingPane(false);
            }

            // Notify the new pane it is about to be show
            pane.showingPane(true);
            this.setSelectedComponent(pane.getUIComponent());
            
            checkForTaskableConfig();
            
        } else
        {
            throw new NullPointerException();
        }
        return pane;
    }

    /**
     * Returns a SubPane for the UI component that it represents.
     * @param comp the component to be looked up
     * @return Returns a SubPane for the UI component that it represents
     */
    public SubPaneIFace getSubPaneForComponent(final Component comp)
    {
        if (comp instanceof SubPaneIFace)
        {
            return (SubPaneIFace)comp;
        }
        
        for (Enumeration<SubPaneIFace> e=panes.elements();e.hasMoreElements();)
        {
            SubPaneIFace sp = e.nextElement();
            if (sp.getUIComponent() == comp)
            {
                return sp;
            }
        }
        log.error("Couldn't find SubPane for Component ["+comp.hashCode()+"]["+comp+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        return null;
    }

    /**
     * Returns the current sub pane.
     * @return the current sub pane.
     */
    public SubPaneIFace getCurrentSubPane()
    {
        int index = this.getSelectedIndex();
        return index > -1 ? getSubPaneForComponent(getComponentAt(index)) : null;
    }

    /**
     * Returns a sub pane at an index.
     * @param index the indes of the sub pane
     * @return Returns a sub pane at an index
     */
    public SubPaneIFace getSubPaneAt(final int index)
    {
        return getSubPaneForComponent(getComponentAt(index));
    }

    /**
     * Asks each pane if it can close (aboutToShutdown) and then closes the pane.
     * If any one pane return false from aboutToShutdown then the process stops.
     * Also, if there is only one visible and then it will always leave at least one pane.
     * @return true is all were closed
     */
    public boolean closeAll()
    {
        return closeAll(true);
    }
    
    /**
     * Asks each pane if it can close (aboutToShutdown) and then closes the pane.
     * If any one pane return false from aboutToShutdown then the process stops.
     * Also, if there is only one visible and then it will always leave at least one pane.
     * @return true is all were closed
     */
    public boolean closeAllExceptCurrent()
    {
        return closeAll(false);
    }

    /**
     * Asks each pane if it can close (aboutToShutdown) and then closes the pane.
     * If any one pane return false from aboutToShutdown then the process stops.
     * Also, if there is only one visible and then it will always leave at least one pane.
     */
    protected boolean closeAll(final boolean includeCurrent)
    {
        if (panes.values().size() > 0)
        {
            SubPaneIFace subPane = this.getCurrentSubPane();
            if (subPane != null)
            {
                Vector<SubPaneIFace> paneList = new Vector<SubPaneIFace>(panes.values()); // Not sure we need to create a new list
                for (SubPaneIFace pane : paneList)
                {
                    showPane(pane);
                    if ((includeCurrent || (!includeCurrent && pane != subPane)) && !removePane(pane))
                    {
                        return false;
                    }
                }
                if (includeCurrent)
                {
                    panes.clear(); // insurance
                }
            }
            
            if (includeCurrent)
            {
                this.removeAll(); // insurance
            }
            
            if (TaskMgr.getToolbarTaskCount() == 1)
            {
                Taskable defaultTask = TaskMgr.getDefaultTaskable();
                if (defaultTask != null)
                {
                    addPane(defaultTask.getStarterPane());
                }
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.ExtendedTabbedPane#closeCurrent()
     */
    @Override
    public synchronized void closeCurrent()
    {
        Action closeCurrent = getAction("CloseCurrent");
        if (!closeCurrent.isEnabled())
        {
            return;
        }
        
        closeCurrent.setEnabled(false);
        
        try
        {
            SubPaneIFace subPane = this.getCurrentSubPane();
            if (subPane != null)
            {
                if (!subPane.aboutToShutdown())
                {
                    return;
                }
                
                //subPane.shutdown();
                
                boolean isCurrentPaneStarter = subPane.getTask().isStarterPane();
                
                // If there is only one pane left and there is only one tasks that provide UI
                // then we cannot let it close.
                boolean wasLastSingleTaskPane = panes.size() == 1  && TaskMgr.getToolbarTaskCount() == 1;//) || subPane.getTask().isSingletonPane();
    
                Taskable task = subPane.getTask();
                if (task != null)
                {
                    //System.out.println("wasLastSingleTaskPane: "+wasLastSingleTaskPane+"  Count: "+countPanesByTask(task)+"   isStarterPane "+task.isStarterPane()+"   isShowDefault "+task.isShowDefault());
                    if (!wasLastSingleTaskPane && !task.isStarterPane() && task.isShowDefault() && countPanesByTask(task) == 1)
                    {
                        int index = this.indexOfComponent(subPane.getUIComponent());
                        if (index < 0)
                        {
                            log.error("Couldn't find index for panel ["+subPane.getPaneName()+"] "); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        
                        replacePane(subPane, task.getStarterPane());
                        
                    } else if (!this.removePane(subPane, true, false) && panes.size() > 0)
                    {
                        return;
                    }
                    
                    if (wasLastSingleTaskPane && !isCurrentPaneStarter)
                    {
                        subPane = task.getStarterPane();
                        addPane(subPane);
                    }
                }
            }
        } finally
        {
            closeCurrent.setEnabled(true);
        }
    }
    
    /**
     * Counts up the number of SubPanes with the same Task.
     * @param task the task in question
     * @return the number of SubPanes sharing that task
     */
    protected int countPanesByTask(final Taskable task)
    {
        int cnt = 0;
        for (SubPaneIFace sp : panes.values())
        {
            if (sp.getTask() == task)
            {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * Adds listener of changes (adds and removes) of SubPaneIFaces to the manager.
     * @param l the listener
     */
    public void addListener(final SubPaneMgrListener l)
    {
        listeners.add(l);
    }

    /**
     * Removes listener.
     * @param l the listener
     */
    public void removeListener(final SubPaneMgrListener l)
    {
        listeners.remove(l);
    }

    /**
     * Notifies listeners when something happens to a subpane.
     * @param type the type of notification
     * @param subPane the subpane it happened to
     */
    protected void notifyListeners(final NotificationType type, final SubPaneIFace subPane)
    {
        for (SubPaneMgrListener l : listeners)
        {
            if (type == NotificationType.Added)
            {
                l.subPaneAdded(subPane);
                
            } else if (type == NotificationType.Removed)
            {
                l.subPaneRemoved(subPane);
            } else if (type == NotificationType.Shown)
            {
                l.subPaneShown(subPane);
            }
        }
    }
    
    /**
     * 
     */
    public static void createTabActions()
    {
        if (getAction("CloseCurrent") != null)
        {
            return;
        }
        
        Action closeCurrent = new AbstractAction()
        {
            public void actionPerformed(ActionEvent ae)
            {
                SubPaneMgr.getInstance().closeCurrent();
            }
        };
        registerAction("CloseCurrent", closeCurrent); //$NON-NLS-1$

        Action closeAll = new AbstractAction() {
            public void actionPerformed(ActionEvent ae)
            {
                SubPaneMgr.getInstance().closeAll();
            }
        };
        registerAction("CloseAll", closeAll); //$NON-NLS-1$
        
        Action closeAllBut = new AbstractAction() {
            public void actionPerformed(ActionEvent ae)
            {
                SubPaneMgr.getInstance().closeAllExceptCurrent();
            }
        };
        registerAction("CloseAllBut", closeAllBut); //$NON-NLS-1$
        
        // Configure Task
        Action configureToolAction = new AbstractAction(getResourceString("Specify.CONFIG_TASK_MENU")) { //$NON-NLS-1$
            public void actionPerformed(ActionEvent e)
            {
                SubPaneIFace sp = SubPaneMgr.getInstance().getCurrentSubPane();
                if (sp != null)
                {
                    Taskable task = sp.getTask();
                    if (task != null && task.isConfigurable())
                    {
                        task.doConfigure();
                    }
                }
            }
        };
        registerAction("ConfigureTask", configureToolAction); //$NON-NLS-1$
        
        configureToolAction.setEnabled(false);
    }
    
    /**
     * @param pane
     */
    protected void createContextMenu()
    {
        if (popupMenu == null)
        {
            popupMenu = new JPopupMenu();
            
            String ttl = getResourceString("Specify.SBP_CLOSE_CUR_MENU"); 
            String mnu = getResourceString("Specify.SBP_CLOSE_CUR_MNEU"); 
            UIHelper.createMenuItemWithAction(popupMenu, ttl, mnu, ttl, true, getAction("CloseCurrent")); 
    
            ttl = getResourceString("Specify.SBP_CLOSE_ALL_MENU"); 
            mnu = getResourceString("Specify.SBP_CLOSE_ALL_MNEU"); 
            UIHelper.createMenuItemWithAction(popupMenu, ttl, mnu, ttl, true, getAction("CloseAll")); 
            
            ttl = getResourceString("Specify.SBP_CLOSE_ALLBUT_MENU"); 
            mnu = getResourceString("Specify.SBP_CLOSE_ALLBUT_MNEU"); 
            UIHelper.createMenuItemWithAction(popupMenu, ttl, mnu, ttl, true, getAction("CloseAllBut")); 
            
            popupMenu.addSeparator();
            
            JMenuItem configTaskMI = new JMenuItem(getAction("ConfigureTask"));
            popupMenu.add(configTaskMI);
            //register("ConfigureTask", configTaskMI); //$NON-NLS-1$
        }
    }


    //--------------------------------------------------------
    // ChangeListener
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        int index = getSelectedIndex();
        if (index > -1)
        {
            if (UIHelper.isMacOS_10_7_X())
            {
                for (int i=0;i<getTabCount();i++)
                {
                    Component comp = getTabComponentAt(i);
                    if (comp instanceof ExtendedTabPanel)
                    {
                        ((ExtendedTabPanel)comp).getTextLabel().setForeground(i == index ? Color.white : Color.BLACK);
                    }
                }
            }
            SubPaneIFace subPane = getSubPaneAt(index);
            // might be null when it is the very first one
            if (subPane != null)
            {
                //log.debug("stateChanged: new pane ["+subPane.getTitle()+"]");
                // When the first the pane is added there is no notification via the listener so we need to do it here
                // when items are added and there is already items then the listener gets notified.
                if (currentPane != subPane)
                {
                    if (currentPane != null)
                    {
                        currentPane.showingPane(false);
                    }
                    ContextMgr.requestContext(subPane.getTask()); // XXX not sure if this need to be moved up into the if above
                    subPane.showingPane(true);
                    notifyListeners(NotificationType.Shown, subPane);
                }
             }
             currentPane = subPane;
        } else
        {
            ContextMgr.requestContext(null);
        }
        checkForTaskableConfig();
    }

    //------------------------------------------------------------------------
    public class TabSelectionFocusGainListener implements ComponentListener
    {
        protected Component focusable;

        public TabSelectionFocusGainListener(final Component focusable)
        {
            super();
            this.focusable = focusable;
        }

        public void componentResized(ComponentEvent e)
        {
        }

        public void componentMoved(ComponentEvent e)
        {
        }

        public void componentShown(ComponentEvent e)
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    if (focusable != null)
                    {
                        focusable.requestFocusInWindow();
                        focusable = null;
                    }
                }
            });
        }

        public void componentHidden(ComponentEvent e)
        {
        }
    }
}
