/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms.validation;

import static edu.ku.brc.ui.UIHelper.setControlSize;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.af.ui.db.JAutoCompTextField;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * A JTextControl that implements UIValidatable for participating in validation
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValTextField extends JAutoCompTextField implements UIValidatable,
                                                                GetSetValueIFace,
                                                                AppPrefsChangeListener,
                                                                UIRegistry.UndoableTextIFace
{
    protected UIValidatable.ErrorType valState  = UIValidatable.ErrorType.Valid;
    
    protected boolean isRequired = false;
    protected boolean isChanged  = false;
    protected boolean isNew      = false;
    protected boolean viewOnly   = false;
    
    protected Color   bgColor;
    protected int     limit     = Integer.MAX_VALUE;

    protected static ColorWrapper valTextColor       = null;
    protected static ColorWrapper requiredFieldColor = null;

    protected boolean              doSetText         = false; 
    protected ValPlainTextDocument document;
    protected String               defaultValue      = null;
    
    protected UndoManager          undoManager       = null;

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
     * @param text initial value
     */
    public ValTextField(String text)
    {
        super(text);
        init();
    }

    /**
     * Constructor
     * @param numCols initial number of columns
     */
    public ValTextField(int numCols)
    {
        super(numCols);
        init();
    }

    /**
     * Constructor
     * @param numCols initial number of columns
     * @param pickListDBAdapter
     */
    public ValTextField(final int numCols, 
                        final PickListDBAdapterIFace pickListDBAdapter)
    {
        super(numCols, pickListDBAdapter);
        init();
    }

    /**
     * Constructor
     * @param arg0 initial value
     * @param numCols initial number of columns
     */
    public ValTextField(final String str, 
                        final int numCols)
    {
        super(str, numCols);
        init();
    }

    /**
     * 
     */
    protected void init()
    {
        setControlSize(this);

        setDocument(new ValPlainTextDocument());

        bgColor = getBackground();
        if (valTextColor == null || requiredFieldColor == null)
        {
            valTextColor       = AppPrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
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
        
        getDocument().addDocumentListener(new DocumentAdaptor() {
            @Override
            protected void changed(DocumentEvent e)
            {
                isChanged = true;
            }
        });
    }

    /**
     * @param limit
     */
    public void setLimit(final int limit)
    {
        ((ValPlainTextDocument)getDocument()).setLimit(limit);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isNotEmpty()
     */
    public boolean isNotEmpty()
    {
        return getText().length() > 0;
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

    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);

        setBackground(isRequired && isEnabled() && !viewOnly ? requiredFieldColor.getColor() : bgColor);
    }


    /* (non-Javadoc)
     * @see javax.swing.text.JTextComponent#setText(java.lang.String)
     */
    @Override
    public void setText(final String text)
    {
        if (document != null)
        {
            document.setIgnoreNotify(true);
        }
        
        doSetText = true;
        super.setText(text);
        doSetText = false;
        
        if (document != null)
        {
            document.setIgnoreNotify(false);
        }
    }
    
    /**
     * Sets the Text with Notification to the document (validators).
     * @param text the text to be set
     */
    public void setTextWithNotification(final String text)
    {
        super.setText(text);
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
        if (isRequired && isEnabled() && !viewOnly)
        {
            setBackground(requiredFieldColor.getColor());
            
        } else if (bgColor != null)
        {
            setBackground(bgColor);
        }
        this.isRequired = isRequired;
    }
    
    /**
     * @param isViewOnly whether it is view only (read only).
     */
    public void setViewOnly(final boolean isViewOnlyArg)
    {
        this.viewOnly = isViewOnlyArg;
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
        valState = isRequired && getText().length() == 0 ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#reset()
     */
    public void reset()
    {
        setText( StringUtils.isNotEmpty(defaultValue) ? defaultValue : "");
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
        UIHelper.removeKeyListeners(this);

        if (document != null)
        {
            for (DocumentListener l : document.getDocumentListeners())
            {
                document.removeDocumentListener(l);
            }
        }
        document = null;
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
    // UIRegistry.UndoableTextIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIRegistry.UndoableTextIFace#getUndoManager()
     */
    public UndoManager getUndoManager()
    {
        if (undoManager == null)
        {
            undoManager = new UndoManager();
            UIRegistry.getInstance().hookUpUndoableEditListener(this);
        }
        return undoManager;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIRegistry.UndoableTextIFace#getTextComponent()
     */
    public JTextComponent getTextComponent()
    {
        return this;
    }
    
    //--------------------------------------------------------
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        setValueWithNotification(value, defaultValue, false);
    }
    
    /**
     * Sets the value and the caller gets to says whether the document gets notified
     * which will invkde the change listeners and the validation.
     * @param value the actual value
     * @param defaultValue the default value
     * @param doNotification whether to send notifications
     */
    public void setValueWithNotification(final Object  value, 
                                         final String  defaultValue, 
                                         final boolean doNotification)
    {

        this.defaultValue = defaultValue;

        String data;

        if (value != null)
        {
            data = value.toString();
            isChanged = true;
            
        } else
        {
            isChanged = StringUtils.isNotEmpty(defaultValue);
            data      = isChanged ? defaultValue : "";
        }
        
        if (doNotification)
        {
            setTextWithNotification(data);
        } else
        {
            setText(data);
        }
        
        if (undoManager != null)
        {
            undoManager.discardAllEdits();
        }

        validateState();

        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return getText();
    }

    //-------------------------------------------------
    // AppPrefsChangeListener
    //-------------------------------------------------

    public void preferenceChange(AppPrefsChangeEvent evt)
    {
        if (evt.getKey().equals("requiredfieldcolor"))
        {
            setBackground(isRequired && isEnabled() && !viewOnly ? requiredFieldColor.getColor() : bgColor);
        }
    }
    

    //-------------------------------------------------
    // JFormattedDoc 
    //-------------------------------------------------
    public class JFormattedDoc extends ValPlainTextDocument
    {
        protected JTextField                      textField;
        protected UIFieldFormatterField.FieldType fieldType;
        protected int                             docLenLimit;
        protected int                             minVal;
        protected int                             maxVal;
        
        /**
         * Create a special formatted document
         * @param textField the textfield the document is associated with
         * @param formatter the formatter
         * @param limit the length of the format
         * @param minVal the min value for the number
         * @param maxVal the max value for the number
         */
        public JFormattedDoc(final JTextField textField, 
                             final int        limit,
                             final int        minVal,
                             final int        maxVal)
        {
            super();
            this.textField    = textField;
            this.fieldType    = UIFieldFormatterField.FieldType.numeric;
            this.docLenLimit  = limit;
            this.minVal       = minVal;
            this.maxVal       = maxVal;
        }

        /**
         * Create a special formatted document
         * @param textField the textfield the document is associated with
         * @param formatter the formatter
         * @param fieldType type of field
         * @param limit the length of the format
         */
        public JFormattedDoc(final JTextField                      textField, 
                             final UIFieldFormatterField.FieldType fieldType,
                             final int                             limit)
        {
            super();
            this.textField    = textField;
            this.fieldType    = fieldType;
            this.docLenLimit  = limit;
            
            this.minVal       = Integer.MIN_VALUE;
            this.maxVal       = Integer.MAX_VALUE;

        }

        /**
         * Check to see if the input was correct (doesn't check against the separator)
         * @param field the field info
         * @param str the str to be checked
         * @return true char matches the type of input, false it is in error
         */
        protected boolean isCharOK(final String text)
        {
            int    len = Math.min(text.length(), docLenLimit);
            String str = text.substring(0, len);

            if (fieldType == UIFieldFormatterField.FieldType.alpha && !StringUtils.isAlpha(str))
            {
                return false;

            } else if (fieldType == UIFieldFormatterField.FieldType.alphanumeric && !StringUtils.isAlphanumeric(str))
            {
                return false;

            } else if (fieldType == UIFieldFormatterField.FieldType.numeric)
            {
                if (StringUtils.isNumeric(str))
                {
                    return isNumValOK(str);
                }
                return false;
            }
            return true;
        }

        /* (non-Javadoc)
         * @see javax.swing.text.Document#remove(int, int)
         */
        @Override
        public void remove(int offset, int len) throws BadLocationException
        {
            super.remove(offset, len);
        }
        
        /**
         * @return
         */
        private boolean isNumValOK(final String str)
        {
            try
            {
                String valStr = getText(0, getLength()) + str;
                if (fieldType == UIFieldFormatterField.FieldType.numeric && StringUtils.isNumeric(valStr))
                {
                    int val = Integer.parseInt(valStr);
                    return val >= minVal && val <= maxVal;
                }
            } catch (BadLocationException ex){}
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.text.Document#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
         */
        @Override
        public void insertString(final int offset, final String strArg, final AttributeSet attr) throws BadLocationException
        {
            String str = strArg;
            if (str == null)
            {
                return;
            }

            if (str.length() > 1)
            {
                if (isCharOK(str))
                {
                    super.insertString(offset, str, attr);
                }
                validateState();
                return;
            }

            int len = getLength() + 1;
            if (len <= docLenLimit)
            {
                if (!isCharOK(str))
                {
                    validateState();
                    return;
                }

                super.insertString(offset, str, attr);
                
                /*String text = textField.getText();
                if (text != null && text.length() < limit)
                {
                    String str2 = text.substring(offset + str.length());
                    super.insertString(offset + str.length(), str2, attr);
                }*/
                
            }

            validateState();
        }
    }
}
