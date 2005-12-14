/* Filename:    $RCSfile: SubPaneMgr.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
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
package edu.ku.brc.specify.ui;

import edu.ku.brc.specify.core.ContextMgr;
import edu.ku.brc.specify.core.NavBoxMgr;
import edu.ku.brc.specify.core.subpane.ExpressSearchResultsPane;
import edu.ku.brc.specify.exceptions.UIException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.*;

public class SubPaneMgr extends JTabbedPane implements ChangeListener
{
    // Static Data Members
    private static Log log = LogFactory.getLog(SubPaneMgr.class);
    
    // Data Members
    protected Hashtable<String, SubPaneIFace> panes = new Hashtable<String, SubPaneIFace>();   
    protected SubPaneIFace currentPane = null;
    
    /**
     * 
     * 
     */
    public SubPaneMgr()
    {
        // This way we notifications that the tabs have changed
        addChangeListener(this);
    }
    
    /**
     * Counts up all the same kind of windows
     * @param name the name of the SubPanel
     * @return the count of the same kind of panes
     */
    protected int countSameType(final String name)
    {
        int count = 0;
        for (Enumeration<SubPaneIFace> e=panes.elements();e.hasMoreElements();)
        {
            SubPaneIFace sp = e.nextElement();
            if (sp.getName().equals(name))
            {
                count++;
            }
            
        }
        return count;
    }
    
    /**
     * Adds the sub pane and return the same one it added
     * @param pane the pane to be added
     * @return the same pane
     */
    public SubPaneIFace addPane(SubPaneIFace pane)
    {
        if (pane == null)
        {
            throw new NullPointerException("Null name or pane when adding to SubPaneMgr");
        }
        
        int cnt = countSameType(pane.getName());
        
        // Add this pane to the tabs
        String title = pane.getName() + (cnt > 0 ? ":" + Integer.toString(cnt+1) : "");
        
        //log.info("addPane: adding pane "+pane.getTitle());
        // When the first the pane is added there is no notification via the listener so we nedd to do it here
        // when items are added and there is already items then the listener gets notified.
        /*if (currentPane != null)
        {
            currentPane.showingPane(false);
        }
        pane.showingPane(true);
        currentPane = pane;
        */
        
        panes.put(title, pane); // this must be done before adding it
        addTab(title, pane.getIcon(), pane.getUIComponent());
        
        this.setSelectedIndex(this.getComponentCount()-1);
        
        // XXX Are these needed??
        pane.getUIComponent().invalidate();
        invalidate();
        doLayout();
        
        return pane;
    }
    
    /**
     * Removes a pane 
     * @param pane the pane to be remoped
     * @return the same pane as the one removed
     */
    public SubPaneIFace removePane(SubPaneIFace pane)
    {
        if (currentPane == pane)
        {
            pane.showingPane(false);
            currentPane = null;
        }
        this.remove(pane.getUIComponent());
        return pane;
    }
    
    /**
     * Show (makes visible) the pane by name
     * @param name the name of the pane to be shown
     * @return the pane that is now shown
      */
    public SubPaneIFace showPane(String name) 
    {
        SubPaneIFace pane = panes.get(name);
        if (pane != null)
        {
            this.setSelectedComponent(pane.getUIComponent());
        } else
        {
            throw new NullPointerException("Could not find pane["+name+"]");
        }
        return pane;
    }
    
    /**
     * Returns a SubPane for the UI component that it represents
     * @param comp the component to be looked up
     * @return Returns a SubPane for the UI component that it represents
     */
    public SubPaneIFace getSubPaneForComponent(Component comp)
    {
        for (Enumeration<SubPaneIFace> e=panes.elements();e.hasMoreElements();)
        {
            SubPaneIFace sp = e.nextElement();
            if (sp.getUIComponent() == comp)
            {
                return sp;
            }
        }
        return null;
    }
    
    /**
     * 
     * @return Return the current sub pane
     */
    public SubPaneIFace getCurrentSubPane()
    {
        return getSubPaneForComponent(getComponentAt(this.getSelectedIndex()));
    }
    
    /**
     * Returns a sub pane at an index
     * @param index the indes of the sub pane
     * @return Returns a sub pane at an index
     */
    public SubPaneIFace getSubPaneAt(int index)
    {
        return getSubPaneForComponent(getComponentAt(index));
    }
    
    /**
     * Removes all the Tabs
     *
     */
    public void closeAll()
    {
        SubPaneIFace subPane = this.getCurrentSubPane();
        if (subPane != null)
        {
            panes.clear();
        }
        this.removeAll();
    }
    
    /*
     * Removes the current tab
     */
    public void closeCurrent()
    {
        SubPaneIFace subPane = this.getCurrentSubPane();
        this.remove(subPane.getUIComponent());
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
            SubPaneIFace subPane = getSubPaneAt(index);
            // might be null when it is the very first one
            if (subPane != null)
            {
                //log.info("stateChanged: new pane ["+subPane.getTitle()+"]");
                // When the first the pane is added there is no notification via the listener so we nedd to do it here
                // when items are added and there is already items then the listener gets notified.
                if (currentPane != subPane)
                {
                    if (currentPane != null)
                    {
                        currentPane.showingPane(false);
                    }
                    ContextMgr.getInstance().requestContext(subPane.getTask()); // XXX not sure if this need to be moved up into the if above
                    subPane.showingPane(true);
                }
             }
             currentPane = subPane;
       } else 
        {
            ContextMgr.getInstance().requestContext(null);
        }
        
    }
    
    

}
