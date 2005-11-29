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
import edu.ku.brc.specify.exceptions.UIException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;

public class SubPaneMgr extends JTabbedPane implements ChangeListener
{
    protected Hashtable<String, SubPaneIFace> panes = new Hashtable<String, SubPaneIFace>();
       
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
     * 
     * @param aName
     * @param aPanel
     * @return
     * @throws UIException
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
        addTab(title, pane.getIcon(), pane.getUIComponent());
        panes.put(title, pane);
        
        this.setSelectedIndex(this.getComponentCount()-1);
        
        // XXX Are these needed??
        pane.getUIComponent().invalidate();
        invalidate();
        doLayout();
        
        return pane;
    }
    
    /**
     * 
     * @param aName
     * @param aPanel
     * @return
     * @throws UIException
     */
    public SubPaneIFace removePane(SubPaneIFace pane)
    {
        this.remove(pane.getUIComponent());
        return pane;
    }
    
    /**
     * 
     * @param aName
     * @return
     * @throws UIException
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
     * 
     * @param comp
     * @return
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
     * @param comp
     * @return
     */
    public SubPaneIFace getCurrentSubPane()
    {
        return getSubPaneForComponent(getComponentAt(this.getSelectedIndex()));
    }
    
    /**
     * 
     * @param comp
     * @return
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
            ContextMgr.getInstance().unregister(subPane.getTask());
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
                ContextMgr.getInstance().requestContext(subPane.getTask());
            }
        } else 
        {
            ContextMgr.getInstance().requestContext(null);
        }
        
    }
    
    

}
