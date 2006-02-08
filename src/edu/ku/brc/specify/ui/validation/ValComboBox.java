/* Filename:    $RCSfile: ValComboBox.java,v $
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.GetSetValueIFace;
import edu.ku.brc.specify.ui.db.JAutoCompComboBox;
import edu.ku.brc.specify.ui.db.PickListDBAdapter;
import edu.ku.brc.specify.ui.db.PickListItem;


/**
 * A JComboBox that implements UIValidatable for participating in validation
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValComboBox extends JPanel implements UIValidatable, ListDataListener, GetSetValueIFace
{
    protected boolean isInError  = false;
    protected boolean isRequired = false;
    protected boolean isChanged  = false;
    protected Color   bgColor    = null;
    
    protected JAutoCompComboBox comboBox;

    protected static ColorWrapper valtextcolor       = null;
    protected static ColorWrapper requiredfieldcolor = null;

    /**
     * Constructor
     */
    public ValComboBox()
    {
        comboBox = new JAutoCompComboBox();
        init(false);
    }

    /**
     * Constructor
     * @param arg0 with a model
     */
    public ValComboBox(ComboBoxModel arg0)
    {
        comboBox = new JAutoCompComboBox(arg0);
        init(false);
    }

    /**
     * Constructor
     * @param arg0 object array of items
     */
    public ValComboBox(Object[] arg0)
    {
        comboBox = new JAutoCompComboBox(arg0);
        init(false);
    }

    /**
     * Constructor
     * @param arg0 vector of items
     */
    public ValComboBox(Vector<?> arg0)
    {
        comboBox = new JAutoCompComboBox(arg0);
        init(false);
    }

    /**
     * Constructor with dbAdapter
     * @param dbAdapter the adaptor for enabling autocomplete
     */
    public ValComboBox(PickListDBAdapter dbAdapter)
    {
        comboBox = new JAutoCompComboBox(dbAdapter);
        init(true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.db.JAutoCompComboBox#init(boolean)
     */
    public void init(final boolean makeEditable)
    {
        setLayout(new BorderLayout());
        add(comboBox, BorderLayout.CENTER);
        
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        
        comboBox.getModel().addListDataListener(this);

        bgColor = getBackground();
        if (valtextcolor == null || requiredfieldcolor == null)
        {
            valtextcolor = PrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
            requiredfieldcolor = PrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
        }
        
    }
    
    /**
     * Returns the model for the combo box
     * @return the model for the combo box
     */
    public ComboBoxModel getModel()
    {
        return comboBox.getModel();
    }
    

    /* (non-Javadoc)
     * @see javax.swing.JComboBox#setModel(javax.swing.ComboBoxModel)
     */
    public void setModel(ComboBoxModel model)
    {
        comboBox.setModel(model);
        model.addListDataListener(this);
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
            g.drawRect(0, 0, dim.width-1, dim.height-1);
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
    // ListDataListener (JComboxBox)
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
     */
    public void contentsChanged(ListDataEvent e)
    {
        isChanged = true;
        isInError = isRequired && comboBox.getSelectedIndex() != -1;
        repaint();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
     */
    public void intervalAdded(ListDataEvent e)
    {
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
     */
    public void intervalRemoved(ListDataEvent e)
    {
    }
    

    //--------------------------------------------------------
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#setValue(java.lang.Object)
     */
    public void setValue(Object value)
    {
        ComboBoxModel  model = comboBox.getModel();
        
        boolean fnd = false;
        
        if (comboBox.hasAdapter())
        {
            for (int i=0;i<comboBox.getItemCount();i++)
            {
                PickListItem pli = (PickListItem)model.getElementAt(i);
                if (pli.getValue().equals(value.toString()))
                {
                    comboBox.setSelectedIndex(i);
                    fnd = true;
                    break;
                }
            }   
            
        
        } else
        {
            for (int i=0;i<comboBox.getItemCount();i++)
            {
                Object item = model.getElementAt(i);
                if (item instanceof String)
                {
                    if (((String)item).equals(value))
                    {       
                        comboBox.setSelectedIndex(i);
                        fnd = true;
                        break;
                    } 
                } else if (item.equals(value))
                {
                    comboBox.setSelectedIndex(i);
                    fnd = true;
                    break;
                }
            } 
        } 

        if (!fnd)
        {
            comboBox.setSelectedIndex(-1);
            this.isInError = isRequired || comboBox.hasAdapter();
            
        } else
        {
            this.isInError = false;
        }
        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        Object selectedObj = comboBox.getSelectedItem();
        if (selectedObj != null)
        {
            if (comboBox.hasAdapter())
            {
                ((PickListItem)selectedObj).getValue();
            } else
            {
                //selectedObj.toString();
            }
        }
        return selectedObj;
    }
}
