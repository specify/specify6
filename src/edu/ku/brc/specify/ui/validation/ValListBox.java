/* Filename:    $RCSfile: ValListBox.java,v $
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.GetSetValueIFace;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * A JList that implements UIValidatable for participating in validation
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValListBox extends JList implements UIValidatable, ListSelectionListener, GetSetValueIFace, PreferenceChangeListener
{
    protected boolean isInError  = false;
    protected boolean isRequired = false;
    protected boolean isChanged  = false;
    protected Color   bgColor    = null;

    protected static ColorWrapper valtextcolor       = null;
    protected static ColorWrapper requiredfieldcolor = null;

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
        if (valtextcolor == null || requiredfieldcolor == null)
        {
            valtextcolor = PrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
            requiredfieldcolor = PrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
        }
        UICacheManager.getAppPrefs().node("ui/formatting").addPreferenceChangeListener(this);

    }

    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g)
    {
        super.paint(g);

        if (isInError() && isEnabled())
        {
            Dimension dim = getSize();
            g.setColor(valtextcolor.getColor());
            g.drawRect(1, 1, dim.width-2, dim.height-2);
        }
    }

    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------

    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        return isInError;
    }

    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#setInError(boolean)
     */
    public void setInError(boolean isInError)
    {
        this.isInError = isInError;
        repaint();

    }

    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#isRequired()
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#isChanged()
     */
    public boolean isChanged()
    {
        return isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#setChanged(boolean)
     */
    public void setChanged(boolean isChanged)
    {
        this.isChanged = isChanged;
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
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#setValue(java.lang.Object)
     */
    public void setValue(Object value)
    {

        Iterator iter = null;
        if (value instanceof Set)
        {
            iter = ((Set)value).iterator();
            
        } else if (value instanceof org.hibernate.collection.PersistentSet)
        {
            iter = ((org.hibernate.collection.PersistentSet)value).iterator();
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
                this.isInError = isRequired;
                
            } else
            {
                this.isInError = false;
            }
        }

        repaint();
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return getSelectedValue();
    }
    
    //-------------------------------------------------
    // PreferenceChangeListener
    //-------------------------------------------------

    public void preferenceChange(PreferenceChangeEvent evt)
    {
        if (evt.getKey().equals("requiredfieldcolor"))
        {
            setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
        }
    }
}
