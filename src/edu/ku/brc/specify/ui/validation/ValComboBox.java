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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.GetSetValueIFace;
import edu.ku.brc.specify.ui.UICacheManager;
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
public class ValComboBox extends JPanel implements UIValidatable, ListDataListener, GetSetValueIFace, PreferenceChangeListener
{
    protected static Color        defaultTextBGColor = null;
    protected static ColorWrapper valtextcolor       = null;
    protected static ColorWrapper requiredfieldcolor = null;


    protected UIValidatable.ErrorType valState  = UIValidatable.ErrorType.Valid;
    protected boolean isRequired = false;
    protected boolean isChanged  = false;
    protected boolean isNew      = false;

    protected JAutoCompComboBox comboBox;
    protected String            defaultValue = null;


    /**
     * Constructor
     */
    public ValComboBox(boolean editable)
    {
        comboBox = new JAutoCompComboBox();
        init(editable);
    }

    /**
     * Constructor
     * @param arg0 with a model
     */
    public ValComboBox(ComboBoxModel arg0, boolean editable)
    {
        comboBox = new JAutoCompComboBox(arg0);
        init(editable);
    }

    /**
     * Constructor
     * @param arg0 object array of items
     */
    public ValComboBox(Object[] arg0, boolean editable)
    {
        comboBox = new JAutoCompComboBox(arg0);
        init(editable);
    }

    /**
     * Constructor
     * @param arg0 vector of items
     */
    public ValComboBox(Vector<?> arg0, boolean editable)
    {
        comboBox = new JAutoCompComboBox(arg0);
        init(editable);
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
    
    public void init(final PickListDBAdapter dbAdapter)
    {
        comboBox = new JAutoCompComboBox(dbAdapter);
        init(true);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.db.JAutoCompComboBox#init(boolean)
     */
    public void init(final boolean makeEditable)
    {
        comboBox.init(makeEditable);
        
        if (defaultTextBGColor == null)
        {
            defaultTextBGColor = (new JTextField()).getBackground();
        }

        //setLayout(new BorderLayout());
        //add(comboBox, BorderLayout.CENTER);

        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p:g", "p:g"), this);
        CellConstraints cc         = new CellConstraints();
        builder.add(comboBox, cc.xy(1,1));

        comboBox.getModel().addListDataListener(this);

        if (valtextcolor == null || requiredfieldcolor == null)
        {
            valtextcolor = PrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
            requiredfieldcolor = PrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
        }
        UICacheManager.getAppPrefs().node("ui/formatting").addPreferenceChangeListener(this);

        FocusAdapter focusAdapter = new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                //valState = isRequired && comboBox.getSelectedIndex() == -1;
                isNew = false;
                repaint();
            }
        };
        if (comboBox.getTextField() != null)
        {
            comboBox.getTextField().addFocusListener(focusAdapter);
        } else
        {
            comboBox.addFocusListener(focusAdapter);
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        comboBox.setEnabled(enabled);
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

        //System.err.println(this.getLocation().x+","+this.getLocation().y+" "+comboBox.getSelectedIndex()+" ["+isNew+"]["+valState()+"]["+isEnabled()+"]");
        if (!isNew && valState == UIValidatable.ErrorType.Error && isEnabled())
        {
            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
            Dimension dim = getSize();
            g2d.setColor(valtextcolor.getColor());
            g2d.drawRect(0, 0, dim.width-1, dim.height-1);
        }
    }

    /**
     * Returns the combobox
     * @return the combobox
     */
    public JAutoCompComboBox getComboBox()
    {
        return comboBox;
    }
    
    /**
     * Helper function that indicates whether the control has text or not
     * @return whether the control has text or not
     */
    public boolean hasText()
    {
        return comboBox.getTextField() != null ? comboBox.getTextField().getText().length() > 0 : comboBox.getSelectedIndex() > -1;
    }
    
    /**
     * Helper function that returns true if an item is selected or the text field is not empty
     * @return true if an item is selected or the text field is not empty
     */
    public boolean isNotEmpty()
    {
        System.out.println((comboBox.getSelectedIndex() > -1) +"  "+ hasText()+"  "+comboBox.getSelectedItem());
        return comboBox.getSelectedIndex() > -1 || hasText();
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#addFocusListener(java.awt.event.FocusListener)
     */
    public void addFocusListener(FocusListener l)
    {
        comboBox.addFocusListener(l);
        if (comboBox.getTextField() != null)
        {
            comboBox.getTextField().addFocusListener(l);
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#removeFocusListener(java.awt.event.FocusListener)
     */
    public void removeFocusListener(FocusListener l)
    {
        comboBox.removeFocusListener(l);
        if (comboBox.getTextField() != null)
        {
            comboBox.getTextField().removeFocusListener(l);
        }
    }
    
    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------


    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#valState()
     */
    public boolean isInError()
    {
        return valState != UIValidatable.ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#getState()
     */
    public ErrorType getState()
    {
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#setState(edu.ku.brc.specify.ui.validation.UIValidatable.ErrorType)
     */
    public void setState(ErrorType state)
    {
        this.valState = state;
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
        comboBox.setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : defaultTextBGColor);
        setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : defaultTextBGColor);
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#setAsNew(boolean)
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

        valState = isRequired && comboBox.getSelectedIndex() == -1 ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
        return valState;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#reset()
     */
    public void reset()
    {
        comboBox.setSelectedIndex(-1);
        if (comboBox.getTextField() != null)
        {
            comboBox.getTextField().setText(StringUtils.isNotEmpty(defaultValue) ? defaultValue : "");
        }
        valState = isRequired ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
        repaint();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#getValidatableUIComp()
     */
    public Component getValidatableUIComp()
    {
        return this;
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
        validateState();
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
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        boolean fnd = false;

        if (value != null)
        {
            ComboBoxModel  model = comboBox.getModel();

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
            
            if (fnd)
            {
                this.valState = UIValidatable.ErrorType.Valid;
            } else
            {
                comboBox.setSelectedIndex(-1);
                this.valState = (comboBox.hasAdapter() && isRequired) || isRequired ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
            }
        } else
        {
            valState = isRequired && comboBox.getSelectedIndex() == -1 ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
            if (comboBox.getTextField() != null)
            {
                comboBox.getTextField().setText("");
            }
            comboBox.setSelectedIndex(-1);
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
                return selectedObj instanceof PickListItem? ((PickListItem)selectedObj).getValue() : selectedObj;
            } else
            {
                //selectedObj.toString();
            }
        }
        return selectedObj;
    }

    //-------------------------------------------------
    // PreferenceChangeListener
    //-------------------------------------------------

    public void preferenceChange(PreferenceChangeEvent evt)
    {
        if (evt.getKey().equals("requiredfieldcolor"))
        {
            comboBox.setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : defaultTextBGColor);
            setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : defaultTextBGColor);
        }
    }
}
