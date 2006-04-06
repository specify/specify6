/* Filename:    $RCSfile: ValFormattedTextField.java,v $
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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.GetSetValueIFace;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.forms.UIFieldFormatterMgr;

/**
 * A JTextField (wrapped inside a JPanel) that provides for "formatted" input. The format "mask" is define in XML
 * via the UIFieldFormatterMgr class. This is idea for text fields that have a standard size and a specific format (i.e. Dates)
 * The mask enables the "fields" and separators to be specifically defined.
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValFormattedTextField extends JPanel implements UIValidatable,
                                                             GetSetValueIFace,
                                                             DocumentListener,
                                                             PreferenceChangeListener
{
    protected boolean  isInError  = false;
    protected boolean  isRequired = false;
    protected boolean  isChanged  = false;
    protected Color    bgColor    = null;

    protected int      inputLen   = 0;
    protected Object[] formatObj  = null;

    protected JSpecialTextField   textField;

    protected static ColorWrapper valtextcolor       = null;
    protected static ColorWrapper requiredfieldcolor = null;

    protected UIFieldFormatterMgr.Formatter            formatter;
    protected List<UIFieldFormatterMgr.FormatterField> fields = null;


    /**
     * Constructor
     * @param arg0 the formatters name
     */
    public ValFormattedTextField(final String formatterName)
    {

        init();

        formatter = UIFieldFormatterMgr.getFormatter(formatterName);
        fields = formatter.getFields();

        PanelBuilder    builder = new PanelBuilder(new FormLayout("p", "p"), this);
        CellConstraints cc      = new CellConstraints();

        StringBuilder strBuf = new StringBuilder();
        int x = 1;
        for (UIFieldFormatterMgr.FormatterField field : fields)
        {
            inputLen += field.getSize();
            strBuf.append(field.getValue());
        }

        textField = new JSpecialTextField(strBuf.toString(), inputLen);
        textField.setDocument(new JFormattedDoc(textField, formatter, inputLen));
        builder.add(textField, cc.xy(x, 1));
        textField.addFocusListener(new FocusAdapter()
                {
                    public void focusGained(FocusEvent e)
                    {
                        ((JTextField)e.getSource()).selectAll();
                        repaint();
                    }

                    public void focusLost(FocusEvent e)
                    {
                        JTextField tf = (JTextField)e.getSource();
                        String data = tf.getText();

                        isInError = (isRequired && (StringUtils.isEmpty(data) || data.length() != inputLen));
                        repaint();

                    }
                });

    }


    /**
     * Inits the control
     */
    public void init()
    {

        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

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
        return textField.getText().length() > 0;
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
            Dimension tfDim = textField.getSize();
            g.setColor(valtextcolor.getColor());
            g.drawRect(1, 1, tfDim.width-1, tfDim.height-1);
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

        textField.setText(data);

        this.isInError = (isRequired && (StringUtils.isEmpty(data) || data.length() != inputLen));

        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return "";//getText();
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

    //--------------------------------------------------
    //-- Inner Class
    //--------------------------------------------------
    public class JSpecialTextField extends JTextField
    {
        protected String bgStr = null;
        protected Point  pnt   = null;
        protected Color textColor = new Color(0,0,0,64);
        protected Insets inner;

        public JSpecialTextField(final String bgStr, final int columns)
        {
            super(columns);
            this.bgStr = bgStr;
            inner = this.getInsets();
        }

        public void setBGStr(final String bgStr)
        {
            this.bgStr = bgStr;
        }

        public void paint(Graphics g)
        {
            super.paint(g);

            String text = getText();
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(text);
            pnt = new Point(inner.left+w, inner.top + fm.getAscent());

            g.setColor(textColor);
            g.drawString(bgStr.substring(text.length(), bgStr.length()), pnt.x, pnt.y);

        }

    }

    public class JFormattedDoc extends PlainDocument
    {
        protected int limit;
        protected JSpecialTextField textField;
        protected UIFieldFormatterMgr.Formatter formatter;
        protected UIFieldFormatterMgr.FormatterField[] fields;

        /**
         * CReate a special formatted document
         * @param textField the textfield the document is associated with
         * @param formatter the formatter
         * @param limit the lengthof the format
         */
        public JFormattedDoc(JSpecialTextField textField, UIFieldFormatterMgr.Formatter formatter, int limit)
        {
            super();
            this.textField   = textField;
            this.formatter  = formatter;
            this.limit      = limit;
            fields = new UIFieldFormatterMgr.FormatterField[limit];
            int inx = 0;
            for (UIFieldFormatterMgr.FormatterField f : formatter.getFields())
            {
                for (int i=0;i<f.getSize();i++)
                {
                    fields[inx++] = f;
                }
            }
        }

        /**
         * Check to see if the input was correct (doesn't check against the separator)
         * @param field the field info
         * @param str the str to be checked
         * @returntrue char matches the type of input, false it is in error
         */
        protected boolean isCharOK(final UIFieldFormatterMgr.FormatterField field, final String str)
        {
            if (field.getType() == UIFieldFormatterMgr.FieldType.alpha && !StringUtils.isAlpha(str))
            {
                return false;

            } else if (field.getType() == UIFieldFormatterMgr.FieldType.alphanumeric && !StringUtils.isAlphanumeric(str))
            {
                return false;

            } else if (field.getType() == UIFieldFormatterMgr.FieldType.numeric && !StringUtils.isNumeric(str))
            {
                return false;

            }
            return true;
        }

        /**
         * Checks to see if the icoming string maps correctly to the format and ll the chars match the appropriate type
         * @param str the string
         * @return true - ok, false there was an error
         */
        protected boolean okToInsertText(final String str)
        {
            int len = Math.min(str.length(), limit);
            for (int i=0;i<len;i++)
            {
                char c = str.charAt(i);
                if (fields[i].getType() == UIFieldFormatterMgr.FieldType.separator)
                {
                    if (c != fields[i].getValue().charAt(0))
                    {
                        return false;
                    }
                }
                String s = "";
                c += c;
                if (!isCharOK(fields[i], s))
                {
                    return false;
                }
            }
            return true;
        }

        /* (non-Javadoc)
         * @see javax.swing.text.Document#insertString(int, java.lang.String, javax.swing.text.AttributeSet)
         */
        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
        {
            if (str == null)
                return;


            if (str.length() > 1)
            {
                if (okToInsertText(str))
                {
                    super.insertString(offset, str.substring(0, Math.min(str.length(), limit)), attr);

                } else
                {
                    getToolkit().beep();
                }
                return;
            }

            int len = getLength() + str.length();
            if (len <= limit)
            {
                UIFieldFormatterMgr.FormatterField field =  fields[offset];
                if (!isCharOK(field, str))
                {
                    getToolkit().beep();
                    return;
                }

                if (field.getType() == UIFieldFormatterMgr.FieldType.separator)
                {
                    if (str.charAt(0) != field.getValue().charAt(0))
                    {
                        if (!isCharOK(fields[offset + 1], str))
                        {
                            getToolkit().beep();
                            return;
                        }
                        str = field.getValue() + str;
                    }
                }

                super.insertString(offset, str, attr);
            }
        }
    }
}
