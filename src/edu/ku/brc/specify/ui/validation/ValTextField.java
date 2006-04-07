/* Filename:    $RCSfile: ValTextField.java,v $
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
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.GetSetValueIFace;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.db.JAutoCompTextField;
import edu.ku.brc.specify.ui.db.PickListDBAdapter;

/**
 * A JTextControl that implements UIValidatable for participating in validation
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValTextField extends JAutoCompTextField implements UIValidatable, 
                                                                GetSetValueIFace, 
                                                                DocumentListener,
                                                                PreferenceChangeListener
{
    protected boolean isInError  = false;
    protected boolean isRequired = false;
    protected boolean isChanged  = false;
    protected Color   bgColor    = null;
    
    protected static ColorWrapper valtextcolor       = null;
    protected static ColorWrapper requiredfieldcolor = null;

    protected ValPlainTextDocument document;

    /**
     * Constructor
     */
    public ValTextField()
    {
        super();
        init();
    }

    /**
     * Constructor
     * @param arg0 initial value
     */
    public ValTextField(String arg0)
    {
        super(arg0);       
        init();
    }

    /**
     * Constructor
     * @param arg0 initial number of columns
     */
    public ValTextField(int arg0)
    {
        super(arg0);     
        init();
    }

    /**
     * Constructor
     * @param arg0 initial number of columns
     */
    public ValTextField(int arg0, PickListDBAdapter pickListDBAdapter)
    {
        super(arg0, pickListDBAdapter);     
        init();
    }

    /**
     * Constructor
     * @param arg0 initial value
     * @param arg1 initial number of columns
     */
    public ValTextField(String arg0, int arg1)
    {
        super(arg0, arg1);   
        init();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.db.JAutoCompTextField#init()
     */
    public void init()
    {
        super.init();
        
        setDocument(document = new ValPlainTextDocument());
        
        bgColor = getBackground();
        if (valtextcolor == null || requiredfieldcolor == null)
        {
            valtextcolor = PrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
            requiredfieldcolor = PrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
        }
        UICacheManager.getAppPrefs().node("ui/formatting").addPreferenceChangeListener(this);
    }
    
    /**
     * Helper method for validation sripting to see if the text field is empty
     * @return whether the text field is empty or not
     */
    public boolean isNotEmpty()
    {
        return getText().length() > 0;
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
    
    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        
        setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
    }
    

    /* (non-Javadoc)
     * @see javax.swing.text.JTextComponent#setText(java.lang.String)
     */
    public void setText(String text)
    {
        document.setIgnoreNotify(true);
        super.setText(text);
        document.setIgnoreNotify(false);
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
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#setValue(java.lang.Object)
     */
    public void setValue(Object value)
    {
        String data;
        
        if (value instanceof String)
        {
            data = (String)value;
            
        } else
        {
            data = value.toString();
        }
        setText(data);
        
        this.isInError = (isRequired && StringUtils.isEmpty(data));
        
        repaint();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return getText();
    }

    
    //--------------------------------------------------------
    // DocumentListener
    //--------------------------------------------------------


    public void changedUpdate(DocumentEvent e)
    {
        isChanged = true;
    }
    
    public void insertUpdate(DocumentEvent e)
    {
        isChanged = true;
    }
    
    public void removeUpdate(DocumentEvent e) 
    {
        isChanged = true;
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
