/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
package edu.ku.brc.specify.plugins;

import java.util.Properties;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.ui.GetSetValueIFace;

/**
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Jul 18, 2007
 *
 */
public abstract class UIPluginBase extends JPanel implements GetSetValueIFace, UIPluginable
{
    protected Object         dataObj           = null;
    protected String         cellName          = null;
    protected String         title             = null;  // suppose to be localized
    protected boolean        isViewMode        = true;
    protected boolean        isNewObj          = true;
    protected Properties     properties        = null;
    protected FormViewObj    fvo               = null;
    protected Vector<ChangeListener> listeners = null;
    
    /**
     * Constructor.
     */
    public UIPluginBase()
    {
        title = getClass().getSimpleName();
    }

    //-----------------------------------------
    // GetSetValueIFace
    //-----------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    @Override
    public Object getValue()
    {
        return dataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(Object value, String defaultValue)
    {
        dataObj = value == null ? defaultValue : value;
    }
    
    //-----------------------------------------
    // UIPluginable
    //-----------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(final Properties propertiesArg, final boolean isViewModeArg)
    {
        this.properties = propertiesArg;
        this.isViewMode = isViewModeArg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setCellName(java.lang.String)
     */
    @Override
    public void setCellName(String cellName)
    {
        this.cellName = cellName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener(final ChangeListener listener)
    {
        if (this.listeners == null)
        {
            this.listeners = new Vector<ChangeListener>();
        }
        this.listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setViewable(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void setParent(FormViewObj parent)
    {
        fvo = parent;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public abstract boolean isNotEmpty();
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#shutdown()
     */
    @Override
    public void shutdown()
    {
        if (listeners != null)
        {
            listeners.clear();
            listeners  = null;
        }
        
        if (properties != null)
        {
            properties.clear();
            properties = null;
        }
        dataObj = null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#carryForwardStateChange()
     */
    @Override
    public void carryForwardStateChange()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setNewObj(boolean)
     */
    @Override
    public void setNewObj(boolean isNewObj)        // TODO Auto-generated method stub
    {
        this.isNewObj = isNewObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#canCarryForward()
     */
    @Override
    public boolean canCarryForward()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getCarryForwardFields()
     */
    @Override
    public String[] getCarryForwardFields()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }

    /**
     * Notify all the change listeners.
     * @param e the change event or null
     */
    protected void notifyChangeListeners(final ChangeEvent e)
    {
        if (listeners != null)
        {
            for (ChangeListener l : listeners)
            {
                l.stateChanged(e);
            }
        }
    }
}
