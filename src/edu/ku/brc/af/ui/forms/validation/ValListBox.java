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
package edu.ku.brc.af.ui.forms.validation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.hibernate.collection.PersistentSet;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIHelper;

/**
 * A JList that implements UIValidatable for participating in validation
 
 * @code_status Beta
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValListBox extends JList implements UIValidatable, ListSelectionListener, GetSetValueIFace, AppPrefsChangeListener
{
    protected UIValidatable.ErrorType valState  = UIValidatable.ErrorType.Valid;
    protected boolean isRequired = false;
    protected boolean isChanged  = false;
    protected boolean isNew      = false;
    protected Color   bgColor    = null;

    protected static ColorWrapper valTextColor       = null;
    protected static ColorWrapper requiredFieldColor = null;

    public ValListBox(ListModel arg0)
    {
        super(arg0);
        init();
    }

    public ValListBox(Object[] arg0)
    {
        super(arg0);
        init();
    }

    public ValListBox(Vector<?> arg0)
    {
        super(arg0);
        init();
    }

    public ValListBox()
    {
        super();
        init();
    }


    /**
     * Initizes colors and listeners
     */
    public void init()
    {

        addListSelectionListener(this);

        bgColor = getBackground();
        if (valTextColor == null || requiredFieldColor == null)
        {
            valTextColor = AppPrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
            requiredFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
        }
        AppPrefsCache.addChangeListener("ui.formatting.requiredfieldcolor", this);
        
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e)
            {
                isNew = false;
                repaint();
            }
        });
    }

    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);

        if (!isNew && valState == UIValidatable.ErrorType.Error && isEnabled())
        {
            UIHelper.drawRoundedRect((Graphics2D)g, valTextColor.getColor(), getSize(), 1);
        }
    }

    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------

    /* (non-Javadoc)
     * @see edu.kui.brc.ui.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        return valState != UIValidatable.ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getState()
     */
    public ErrorType getState()
    {
        return valState;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        return getSelectedIndex() != -1;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setState(edu.ku.brc.ui.forms.validation.UIValidatable.ErrorType)
     */
    public void setState(ErrorType state)
    {
        this.valState = state;
    }

    /* (non-Javadoc)
     * @see edu.kui.brc.ui.validation.UIValidatable#isRequired()
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.kui.brc.ui.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        setBackground(isRequired && isEnabled() ? requiredFieldColor.getColor() : bgColor);
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isChanged()
     */
    public boolean isChanged()
    {
        return isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    public void setChanged(boolean isChanged)
    {
        this.isChanged = isChanged;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    public void setAsNew(boolean isNew)
    {
        this.isNew = isRequired ? isNew : false;
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#validate()
     */
    public UIValidatable.ErrorType validateState()
    {
        valState = isRequired && getSelectedIndex() == -1 ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
        return valState;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#reset()
     */
    public void reset()
    {
        setSelectedIndex(-1);
        valState = isRequired ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
        repaint();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    public Component getValidatableUIComp()
    {
        return this;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#cleanUp()
     */
    public void cleanUp()
    {
        UIHelper.removeFocusListeners(this);
        UIHelper.removeListSelectionListeners(this);
        UIHelper.removeKeyListeners(this);        
        AppPrefsCache.removeChangeListener("ui.formatting.requiredfieldcolor", this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getReason()
     */
    public String getReason()
    {
        return null;
    }

    //--------------------------------------------------------
    // ListSelectionListener
    //--------------------------------------------------------
    public void valueChanged(ListSelectionEvent e)
    {
        isChanged = true;
    }


    //--------------------------------------------------------
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {

        if (value == null)
        {
            setSelectedIndex(-1);
            if (getModel() instanceof DefaultListModel)
            {
            	DefaultListModel defModel = (DefaultListModel)getModel();
            	defModel.clear();
            }
            return;
        }
        
        Iterator<?> iter = null;
        if (value instanceof Set)
        {
            iter = ((Set<?>)value).iterator();
            
        } else if (value instanceof PersistentSet)
        {
            iter = ((PersistentSet)value).iterator();
        }
        
        
        if (iter != null)
        {        
            DefaultListModel defModel = new DefaultListModel(); 
            while (iter.hasNext())
            {
                defModel.addElement(iter.next());
            }
            setModel(defModel);
            setSelectedIndex(-1);
        } else 
        {
            boolean fnd = false;
            ListModel  model = getModel();
            for (int i=0;i<model.getSize();i++)
            {
                Object item = model.getElementAt(i);
                if (item instanceof String)
                {
                    if (((String)item).equals(value))
                    {
                        setSelectedIndex(i);
                        fnd = true;
                        break;
                    } 
                } else if (item.equals(value))
                {
                    setSelectedIndex(i);
                    fnd = true;
                    break;
                }
            }
            
            if (!fnd)
            {
                setSelectedIndex(-1);
                valState = UIValidatable.ErrorType.Error;
                
            } else
            {
                valState = UIValidatable.ErrorType.Valid;
            }
        }

        repaint();
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return getSelectedValue();
    }
    
    //-------------------------------------------------
    // AppPrefsChangeListener
    //-------------------------------------------------

    public void preferenceChange(AppPrefsChangeEvent evt)
    {
        if (evt.getKey().equals("requiredfieldcolor"))
        {
            setBackground(isRequired && isEnabled() ? requiredFieldColor.getColor() : bgColor);
        }
    }
}
