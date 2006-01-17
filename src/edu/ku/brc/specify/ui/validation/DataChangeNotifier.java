/* Filename:    $RCSfile: DataChangeListener.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/16 19:59:54 $
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
package edu.ku.brc.specify.ui.validation;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements several listener interfaces and listens for the various types of notifications
 * to figure out if the component has changed.
 * 
 * @author rods
 *
 */
public class DataChangeNotifier implements FocusListener, KeyListener, ChangeListener, ListDataListener
{
    private static Log log = LogFactory.getLog(DataChangeNotifier.class);
    
    protected UIValidator                uiv;
    protected boolean                    dataChanged = false;
    protected Vector<DataChangeListener> dcListeners = new Vector<DataChangeListener>();

    protected Component   comp;
    protected String      name;
    protected String      cachedData = null;
    
    /**
     * Constructor
     * @param name the name
     * @param comp the component
     * @param uiv
     */
    public DataChangeNotifier(String name, Component comp, UIValidator uiv)
    {
        this.name = name;
        this.comp = comp;
        this.uiv  = uiv;
    }
    
    /**
     * Add a data change listener that is notified any time the data or state has change for the component.
     * @param dcl the listener
     */
    public void addDataChangeListener(DataChangeListener dcl)
    {
        dcListeners.addElement(dcl);
    }
    
    /**
     * Remove the listener
     * @param dcl the listener to be removed
     */
    public void removeDataChangeListener(DataChangeListener dcl)
    {
        dcListeners.removeElement(dcl);
    }
    
    /**
     * Notify all the listeners that the data or state was change
     */
    protected void notifyDataChangeListeners()
    {
        log.info("DataChangeNotifier - notifyDataChangeListeners");
        dataChanged = true;
        for (Enumeration e=dcListeners.elements();e.hasMoreElements();)
        {
            DataChangeListener dcl = (DataChangeListener)e.nextElement();
            dcl.dataChanged(name, comp);
        }
    }
    
    /**
     * Returns a string value for a control to compare to see if the value has changed
     * @param comp the component to get the value fromn
     * @return Returns a string value for a control to compare to see if the value has changed
     */
    public String getValueForControl(Component comp)
    {
        log.info("DataChangeNotifier - getValueForControl "+comp);
        if (comp instanceof JTextComponent)
        {
            return  ((JTextComponent)comp).getText();
            
        } else if (comp instanceof JToggleButton)
        {
            return Boolean.toString(((JToggleButton)comp).isSelected());
            
        } else if (comp instanceof JComboBox)
        {
            return ((JComboBox)comp).getSelectedItem().toString();
        } else
        {
            throw new RuntimeException("Can't get a value for component: "+comp);
        }
    }
    
    /**
     * Reset it back to not having been changed and uses the current value as the new cached value
     */
    public void reset()
    {
        dataChanged = false;
        cachedData  = getValueForControl(comp);
    }
    
    /**
     * Manual check to see if the data has changed. It doesn't call any notifications.
     * @return Returns wether the control has changed values
     */
    public boolean manualCheckForDataChanged()
    {
        if (!dataChanged)
        {
            if (cachedData != null)
            {
                if (!cachedData.equals(getValueForControl(comp)))
                {
                    dataChanged = true;
                }
            } else 
            {
                String str = getValueForControl(comp);
                if (str != null && str.length() > 0)
                {
                  dataChanged = true;
                }
            }
        }
        return dataChanged;
    }
    

    /**
     * @return Returns the dataChanged.
     */
    public boolean isDataChanged()
    {
        return dataChanged;
    }

    /**
     * @param dataChanged The dataChanged to set.
     */
    public void setDataChanged(boolean dataChanged)
    {
        this.dataChanged = dataChanged;
    }

    /**
     * @return Returns the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return Returns the uiv.
     */
    public UIValidator getUIV()
    {
        return uiv;
    }
    
    //---------------------------
    // FocusListener
    //---------------------------
    
    /* (non-Javadoc)
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    public void focusGained(FocusEvent e)
    {
        cachedData = getValueForControl(comp);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
    public void focusLost(FocusEvent e)
    {
        //log.info("["+((JTextComponent)comp).getText()+"]["+cachedData+"]");
        if (cachedData != null && !cachedData.equals(getValueForControl(comp)))
        {
            notifyDataChangeListeners();
        }
        
        if (uiv != null)
        {
            uiv.validate();
        }
    }
    
    //--------------------------------------------------------
    // ChangeListener
    //--------------------------------------------------------
    public void stateChanged(ChangeEvent e) 
    {
        notifyDataChangeListeners();
    }
    
    //--------------------------------------------------------
    // KeyListener
    //--------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent e)    
    { 
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent e)
    {
        notifyDataChangeListeners();
        if (uiv != null)
        {
            uiv.validate();
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent e)
    {
    }
    
    //--------------------------------------------------------
    // ListDataListener (JComboxBox)
    //--------------------------------------------------------
    
    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
     */
    public void contentsChanged(ListDataEvent e)
    {
        log.info("contentsChanged "+e);
        notifyDataChangeListeners();
        if (uiv != null)
        {
            uiv.validate();
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
     */
    public void intervalAdded(ListDataEvent e)
    {
        notifyDataChangeListeners();
        if (uiv != null)
        {
            uiv.validate();
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
     */
    public void intervalRemoved(ListDataEvent e)
    {
        notifyDataChangeListeners();
        if (uiv != null)
        {
            uiv.validate();
        }
    }

}
