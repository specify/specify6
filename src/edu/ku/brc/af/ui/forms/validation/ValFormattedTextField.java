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

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.setControlSize;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import edu.ku.brc.specify.plugins.PartialDateUI;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterField.FieldType;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * A Multiple JTextFields (wrapped inside a JPanel) that provides for "formatted" input. The format "mask" is define in XML
 * via the UIFieldFormatterMgr class. This is ideal for text fields that have a standard size and a specific format (i.e. Dates)
 * The mask enables the "fields" and separators to be specifically defined.
 * 
 * NOTE: This impl has multiple Text Field, one for each part of the format.
 * 
 * @code_status Beta
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValFormattedTextField extends JPanel implements ValFormattedTextFieldIFace
{
    //private static final Logger log  = Logger.getLogger(ValFormattedTextField.class);

    protected static ColorWrapper     valTextColor       = null;
    protected static ColorWrapper     requiredFieldColor = null;
    protected static ColorWrapper     viewFieldColor     = null;

    protected UIValidatable.ErrorType     valState       = UIValidatable.ErrorType.Valid;
    protected boolean                     isRequired     = false;
    protected boolean                     isChanged      = false;
    protected boolean                     isNew          = false;
    protected boolean                     isViewOnly     = false;
    protected boolean                     isPartialOK    = false;
    protected boolean                     isSearch       = false;
    protected Color                       bgColor        = null;
    
    protected JTextField                  viewtextField  = null;

    protected boolean                     shouldIgnoreNotifyDoc = true;
    protected boolean                     needsUpdating  = false;
    
    protected String                      currCachedValue = null;

    protected List<JFormattedDoc>         documents      = new Vector<JFormattedDoc>();
    protected String                      defaultValue   = null;

    protected UIFieldFormatterIFace       formatter;
    protected List<UIFieldFormatterField> fields              = null;
    protected boolean                     isFromUIFmtOverride = false;
    protected List<DocumentListener>      documentListeners   = null;
    
    protected boolean                     isAutoFmtOn    = true;
    
    protected Object                      origValue      = null;
    protected ChangeListener              changeListener = null;
    
    protected CardLayout                  cardLayout;
    protected JPanel                      cardPanel      = null;
    protected BGTextField                 viewTF         = null;
    protected JTextField                  editTF         = null;
    
    //---
    protected Color                       textColor = new Color(0,0,0,64);
    
    protected JComponent[]                comps = null;
    protected char                        autoNumberChar = UIFieldFormatterMgr.getAutoNumberPatternChar();
    protected KeyStroke                   pasteKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

    /**
     * Constructor
     * @param dataObjFormatterName the formatters name
     */
    protected ValFormattedTextField()
    {
        super();
        setOpaque(false);
    }
    
    /**
     * Constructor.
     * @param formatter
     * @param isViewOnly
     * @param isAllEditable
     */
    public ValFormattedTextField(final UIFieldFormatterIFace formatter, 
                                 final boolean isViewOnly, 
                                 final boolean isAllEditable)
    {
        this(formatter, isViewOnly, isAllEditable, false);
    }

    /**
     * Constructor.
     * @param formatter
     * @param isViewOnly
     * @param isAllEditable
     * @param isPartialOK
     */
    public ValFormattedTextField(final UIFieldFormatterIFace formatter, 
                                 final boolean isViewOnly, 
                                 final boolean isAllEditable, 
                                 final boolean isPartialOK)
    {
        this();
        
        this.isViewOnly  = isViewOnly;
        this.isPartialOK = isPartialOK;
        
        init(formatter, isAllEditable);
    }

    /**
     * Constructor.
     * @param formatter the formatters
     */
    public ValFormattedTextField(final UIFieldFormatterIFace formatter, final boolean isViewOnly)
    {
        this(formatter, isViewOnly, false, false);
    }

    /**
     * Constructor.
     * @param formatter the formatters
     */
    public ValFormattedTextField(final String formatterName, final boolean isViewOnly)
    {
        this(formatterName, isViewOnly, false, false);
    }

    /**
     * Constructor
     * @param formatterName the formatters name
     * @param isViewOnly
     * @param isAllEditable
     * @param isPartialOK
     */
    public ValFormattedTextField(final String formatterName, 
                                 final boolean isViewOnly, 
                                 final boolean isAllEditable, 
                                 final boolean isPartialOK)
    {
        super();

        this.isViewOnly  = isViewOnly;
        this.isPartialOK = isPartialOK;
        
        init(UIFieldFormatterMgr.getInstance().getFormatter(formatterName), isAllEditable);
    }
    
    /**
     * Constructor
     * @param formatterName the formatters name
     * @param isViewOnly
     * @param isAllEditable
     */
    public ValFormattedTextField(final String formatterName, 
                                 final boolean isViewOnly, 
                                 final boolean isAllEditable)
    {
        super();

        this.isViewOnly = isViewOnly;
        
        init(UIFieldFormatterMgr.getInstance().getFormatter(formatterName), isAllEditable);
    }
    
    /**
     * @param isPartialOK the isPartialOK to set
     */
    public void setPartialOK(final boolean isPartialOK)
    {
        boolean isDifferent = this.isPartialOK != isPartialOK;
        
        this.isPartialOK = isPartialOK;
        
        if (isDifferent)
        {
            setRequired(isRequired); // will adjust the color
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#requestFocus()
     */
    @Override
    public void requestFocus()
    {
        if (comps != null && comps[0] != null)
        {
            comps[0].requestFocus();
            //comps[0].requestFocusInWindow();
        }
    }

    /**
     * @param formatterArg
     * @param isAllEditable
     */
    protected void init(final UIFieldFormatterIFace formatterArg, final boolean isAllEditable)
    {
        setFormatterInternal(formatterArg);
        
        createUI();
        
        if (!isPartialOK && (valTextColor == null || requiredFieldColor == null ||viewFieldColor == null))
        {
            valTextColor       = AppPrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
            requiredFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
            viewFieldColor     = AppPrefsCache.getColorWrapper("ui", "formatting", "viewfieldcolor");
        }
        
        if (!isViewOnly && comps != null && !isAllEditable)
        {
            int inx = 0;
            for (UIFieldFormatterField field : fields)
            {
                if (field.isIncrementer() && !isPartialOK)
                {
                    if (comps[inx] instanceof JTextField)
                    {
                        ViewFactory.changeTextFieldUIForDisplay(((JTextField)comps[inx]), getBackground(), false);
                        
                    } else if (comps[inx] instanceof JPanel)
                    {
                        ViewFactory.changeTextFieldUIForDisplay(viewTF, getBackground(), false);
                    }
                }
                inx++;
            }
        }
    }
    
    /**
     * @return the text field
     */
    public JTextField getTextField()
    {
        return viewtextField;
    }
    
    /**
     * @return the comps
     */
    public JComponent[] getTextComps()
    {
        return comps;
    }

    /**
     * @param textField
     */
    protected void addFocusAdapter(final JTextField textField)
    {
        textField.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                ((JTextField)e.getSource()).selectAll();
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e)
            {
                isNew = false;
                validateState();
                repaint();

            }
        });
    }
    
    /**
     * Creates the various UI Components for the formatter.
     */
    protected void createUI()
    {
        CellConstraints cc = new CellConstraints();
        
        if (isViewOnly || (formatter != null && !formatter.isUserInputNeeded() && fields != null && fields.size() == 1))
        {
            viewtextField = new JTextField();
            setControlSize(viewtextField);
            
            // Remove by rods 12/5/08 this messes thihngs up
            // values don't get inserted correctly, shouldn't be needed anyway
            
            //JFormattedDoc document = new JFormattedDoc(viewtextField, formatter, formatter.getFields().get(0));
            //viewtextField.setDocument(document);
            //document.addDocumentListener(this);
            //documents.add(document);
            
            ViewFactory.changeTextFieldUIForDisplay(viewtextField, false);
            PanelBuilder    builder = new PanelBuilder(new FormLayout("1px,f:p:g,1px", "1px,f:p:g,1px"), this);
            builder.add(viewtextField, cc.xy(2, 2));
            bgColor = viewtextField.getBackground();

        } else
        {
            JTextField txt = new JTextField();
            
            Font txtFont = txt.getFont();
            
            Font font = new Font("Courier", Font.PLAIN, txtFont.getSize());
            
            BufferedImage bi = new BufferedImage(1,1, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bi.createGraphics();
            g.setFont(font);
            FontMetrics fm = g.getFontMetrics(font);
            g.dispose();
            
            Insets ins = txt.getBorder().getBorderInsets(txt);
            int baseWidth = ins.left + ins.right;
            
            bgColor = txt.getBackground();
    
            
            StringBuilder sb = new StringBuilder("1px");
            for (UIFieldFormatterField f : fields)
            {
                sb.append(",");
                if (f.getType() == FieldType.separator || f.getType() == FieldType.constant)
                {
                    sb.append('p');
                } else
                {
                    sb.append(((fm.getMaxAdvance() * f.getSize()) + baseWidth) + "px");
                }
            }
            sb.append(",1px");
            PanelBuilder builder = new PanelBuilder(new FormLayout(sb.toString(), "1px,P:G,1px"), this);
            
            comps = new JComponent[fields.size()];
            int inx = 0;
            for (UIFieldFormatterField f : fields)
            {
                JComponent comp    = null;
                JComponent tfToAdd = null;
                
                if (f.getType() == FieldType.separator || f.getType() == FieldType.constant)
                {
                    comp = createLabel(f.getValue());
                    if (f.getType() == FieldType.constant)
                    {
                        comp.setBackground(Color.WHITE);
                        comp.setOpaque(true);
                    }
                    tfToAdd = comp;
                    
                } else
                {
                    JTextField tf = new BGTextField(f.getSize(), isViewOnly ? "" : f.getValue());
                    tfToAdd = tf;
                    
                    if (inx == 0)
                    {
                        tf.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyPressed(KeyEvent e)
                            {
                                checkForPaste(e);
                            }
                        });
                    }
                    
                    JFormattedDoc document = new JFormattedDoc(tf, formatter, f);
                    tf.setDocument(document);
                    document.addDocumentListener(new DocumentAdaptor() {
                        @Override
                        protected void changed(DocumentEvent e)
                        {
                            isChanged = true;
                            if (!shouldIgnoreNotifyDoc)
                            {
                                String fldStr = getText();
                                int    len    = StringUtils.isNotEmpty(fldStr) ? fldStr.length() : 0;
                                if (formatter != null && len > 0 && formatter.isLengthOK(len))
                                {
                                    setState(formatter.isValid(fldStr) ? UIValidatable.ErrorType.Valid : UIValidatable.ErrorType.Error);
                                    repaint();
                                }
                                
                                //validateState();
                                if (changeListener != null)
                                {
                                    changeListener.stateChanged(new ChangeEvent(this));
                                }
                                
                                if (documentListeners != null)
                                {
                                    for (DocumentListener dl : documentListeners)
                                    {
                                        dl.changedUpdate(null);
                                    }
                                }
                            }
                            currCachedValue = null;
                        }
                    });
                    documents.add(document);

                    addFocusAdapter(tf);
                    
                    comp = tf;
                    comp.setFont(font);
                    
                    if (f.isIncrementer())
                    {
                        editTF     = tf;
                        cardLayout = new CardLayout();
                        cardPanel  = new JPanel(cardLayout);
                        cardPanel.add("edit", tf);
                        
                        viewTF = new BGTextField(f.getSize(), isViewOnly ? "" : f.getValue());
                        viewTF.setDocument(document);
                        cardPanel.add("view", viewTF);
                        
                        cardLayout.show(cardPanel, "view");
                        comp = cardPanel;
                        tfToAdd = cardPanel;
                    }
                }
                
                setControlSize(tfToAdd);
                builder.add(comp, cc.xy(inx+2, 2));
                comps[inx] = tfToAdd;
                inx++;
            }
        }
    }
    
    /**
     * @param e the key event
     */
    protected void checkForPaste(final KeyEvent e)
    {
        if (e.getKeyCode() == pasteKeyStroke.getKeyCode() &&
            e.getModifiers() == pasteKeyStroke.getModifiers())
        {
            String text = UIHelper.getTextFromClipboard();
            if (text != null && text.length() <= formatter.getLength())
            {
                setValue(text, null);
            }
        }
    }
    
    /**
     * @param changeListener the changeListener to set
     */
    public void setChangeListener(ChangeListener changeListener)
    {
        this.changeListener = changeListener;
    }

    /**
     * Sets the formatter.
     * @param dataObjFormatterName the formatter to use
     */
    protected void setFormatterInternal(final UIFieldFormatterIFace formatterArg)
    {
        if (formatter != formatterArg && formatterArg != null)
        {
            formatter = formatterArg;
            
            fields = formatter.getFields();
        }
    }
    
    /**
     * Sets the BG color of all the text fields.
     * @param color
     */
    protected void setBGColor(final Color color)
    {
        if (viewtextField != null)
        {
            viewtextField.setBackground(color);
            
        } else if (comps != null)
        {
            for (JComponent comp : comps)
            {
                if (comp instanceof JTextField)
                {
                    ((JTextField)comp).setBackground(color);
                } 
            }
        }
    }
    
    /**
     * Sets the formatter and reset the current value into the new format.
     * @param dataObjFormatterName the formatter to use
     */
    public void setFormatter(final UIFieldFormatterIFace formatter)
    {
        Object currentValue = isChanged ? getValue() : origValue;
        
        setFormatterInternal(formatter);
        
        setValue(currentValue, "");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isNotEmpty()
     */
    public boolean isNotEmpty()
    {
        String txt = getText();
    	return !(txt == null || txt.isEmpty());
    }
    
    /**
     * @return
     */
    public String getText()
    {
        if (viewtextField != null)
        {
            return viewtextField.getText();
        }
        StringBuilder sb      = new StringBuilder();
        int           inx     = 0;
        String        prevStr = null;
        for (JComponent c : comps)
        {
            String val = null;
            if (c instanceof JLabel)
            {
                val = ((JLabel)c).getText();
                prevStr = val;
                
            } else if (c instanceof JTextField)
            {
                val = ((JTextField)c).getText();
                
            } else if (c instanceof JPanel)
            {
                if (isAutoFmtOn)
                {
                    val = viewTF.getText();
                    if (StringUtils.isEmpty(val))
                    {
                        val = viewTF.getBgStr();
                    }
                } else 
                {
                    val = editTF.getText();
                }
            }
            
            if (StringUtils.isEmpty(val))
            {
                if (!formatter.getFields().get(inx).isIncrementer())
                {
                    if (!isPartialOK && !formatter.getName().equalsIgnoreCase("world o' pain"))
                    {
                        return null;
                    }
                    if (prevStr != null)
                    {
                        sb.setLength(sb.length() - prevStr.length());
                    }
                }
                break;
            }
            
            sb.append(val);
            if (!(c instanceof JLabel))
            {
                prevStr = null;
            }
            inx++;
        }
        currCachedValue = sb.toString();
        return currCachedValue;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        boolean isEnabled = enabled;
        if (!isViewOnly)
        {
            boolean isNeeded = formatter.isUserInputNeeded();
            if (enabled && isNeeded)
            {
                isEnabled = isNeeded;
                
            } else
            {
                isEnabled = enabled;
            }
    
            setBGColor(isRequired && isEnabled && !isViewOnly ? requiredFieldColor.getColor() : bgColor);
        }
        
        super.setEnabled(isEnabled);
        
        if (viewtextField != null)
        {
            viewtextField.setEnabled(isEnabled);
            
        } else if (comps != null)
        {
            for (JComponent comp : comps)
            {
                if (comp instanceof JTextField)
                {
                    ((JTextField)comp).setEnabled(isEnabled);
                    
                } else if (comp instanceof JPanel)
                {
                    viewTF.setEnabled(isEnabled);
                    editTF.setEnabled(isEnabled);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.text.JTextComponent#setText(java.lang.String)
     */
    public void setText(final String text, final boolean notify)
    {
        if (viewtextField != null)
        {
            viewtextField.setText(text);
            return;
        }

        shouldIgnoreNotifyDoc = !notify;
        
        boolean isTextEmpty = StringUtils.isEmpty(text);
        
        int txtLen = text.length();
        //int len    = formatter.getLength();
        int inx    = 0;
        int pos    = 0;
        for (UIFieldFormatterField field : fields)
        {
            String val;
            if (isTextEmpty)
            {
                /*if (field.isEntryField())
                {
                    if (field.getType() == FieldType.year)
                    {
                        val = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
                    } else
                    {
                        val = "";
                    }
                } else
                {
                    val = field.getValue();
                }*/
                val = "";
            } else
            {
                if (pos < txtLen)
                {
                    val = text.substring(pos, Math.min(pos+field.getSize(), txtLen));
                } else
                {
                    val = "";
                }
            }
            
            if (comps[inx] instanceof JLabel)
            {
                if (!val.equals(field.getValue()))
                {
                    valState = UIValidatable.ErrorType.Error;
                }
            }  else if (comps[inx] instanceof JPanel)
            {
                if (isAutoFmtOn)
                {
                    if (StringUtils.isNotEmpty(val))
                    {
                        viewTF.setText(val);
                    }
                } else
                {
                    editTF.setText(val);    
                }
            } else 
            {
                ((JTextField)comps[inx]).setText(val);
            }
            pos += field.getSize();
            inx++;
        }
        
        shouldIgnoreNotifyDoc = false;
        
        repaint();
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        
        if (!isViewOnly && !isPartialOK && !isNew && valState == UIValidatable.ErrorType.Error && isEnabled())
        {
            Dimension size;
            if (comps != null && comps.length > 0)
            {
                size = getSize();
                Component lastComp = comps[comps.length-1];
                size.width = lastComp.getBounds().x + lastComp.getBounds().width;
                size.height--;
                
            } else if (editTF != null)
            {
                size = editTF.getSize();
                
            } else
            {
                size = getSize();
            }
            UIHelper.drawRoundedRect((Graphics2D)g, valTextColor.getColor(), size, 0);
        }
    }

    /**
     * Returns true if the no validation errors, false if there are
     * @return true if the no validation errors, false if there are
     */
    public boolean isOK()
    {
        return valState == UIValidatable.ErrorType.Valid;
    }
    
    /**
     * Sets the text and notifies the validator of the change. This is used manually callit directly.
     * @param text the new text (must already be formatted).
     */
    public void setText(final String text)
    {
        setText(text, true);
    }
    
    /**
     * @param isViewOnly the isViewOnly to set
     */
    public void setViewOnly(boolean isViewOnly)
    {
        this.isViewOnly = isViewOnly;
    }
    
    //--------------------------------------------------
    //-- AutoNumberableIFace Interface
    //--------------------------------------------------

    public boolean needsUpdating() {
        return needsUpdating;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.AutoNumberableIFace#updateAutoNumbers()
     */
    public String updateAutoNumbers()
    {
        return updateAutoNumbers(getText(), false);
    }

    public String updateAutoNumbers(final String val) {
        return updateAutoNumbers(val, true);
    }

    public String updateAutoNumbers(final String val, final boolean doIncrement)
    {
        if (isAutoFmtOn && needsUpdating)
        {
            String nextNum = formatter.getNextNumber(val, !val.contains("#") && doIncrement);
            if (StringUtils.isNotEmpty(nextNum))
            {
                try
                {
                    setText(nextNum, false);
                    needsUpdating = false;
                    return getText();

                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ValFormattedTextField.class, ex);
                    ex.printStackTrace();
                }
            }
            needsUpdating = true;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.AutoNumberableIFace#isFormatterAutoNumber()
     */
    public boolean isFormatterAutoNumber()
    {
        return !isPartialOK && formatter != null && formatter.getAutoNumber() != null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.AutoNumberableIFace#setAutoNumberEnabled(boolean)
     */
    public void setAutoNumberEnabled(boolean turnOn)
    {
        if (formatter.isIncrementer() && cardPanel != null && isAutoFmtOn != turnOn)
        {
            cardLayout.show(cardPanel, turnOn ? "view" : "edit");
         
            isAutoFmtOn = turnOn;
            setChanged(true);
            if (changeListener != null)
            {
                changeListener.stateChanged(new ChangeEvent(this));
            }
        } else
        {
            isAutoFmtOn = turnOn;
        }
    }

    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------

    /* (non-Javadoc)
     * @see edu.kui.brc.ui.validation.UIValidatable#valState()
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
        return isRequired && !isViewOnly;
    }

    /* (non-Javadoc)
     * @see edu.kui.brc.ui.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
        setBGColor(!isPartialOK && isRequired && isEnabled() && !isViewOnly? requiredFieldColor.getColor() : bgColor);
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
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#reset()
     */
    public void reset()
    {
        origValue = null;
        if (StringUtils.isNotEmpty(defaultValue))
        {
            setText(defaultValue);
        }
        validateState();
        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    public Component getValidatableUIComp()
    {
        return this;
    }
    
    /**
     * @return if all the fields have the correct amount of characters, it skips incrementer fields
     */
    protected boolean isFilled()
    {
        try
        {
        int inx = 0;
        for (UIFieldFormatterField f : fields)
        {
            if ((!f.isIncrementer() || !isAutoFmtOn) && f.getType() != FieldType.constant && f.getType() != FieldType.separator)
            {
                JFormattedDoc doc = documents.get(inx);
                int len = f.getMinSize();
                if (doc.getLength() < len)
                {
                    return false;
                }
                inx++;
            }
        }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#validate()
     */
    public UIValidatable.ErrorType validateState()
    {
        UIValidatable.ErrorType oldState = valState;
        
        if (isPartialOK)
        {
            boolean hasText = false;
            for (int i=0;i<documents.size();i++)
            {
                JFormattedDoc doc = documents.get(i);
                
                if (!hasText && doc.getLength() > 0 && i > 0)
                {
                    return valState = UIValidatable.ErrorType.Error;
                }
                
                if (doc.getLength() > 0)
                {
                    hasText = true;
                }
            }
            return valState = UIValidatable.ErrorType.Valid;
        }
        
        if (isViewOnly)
        {
            valState = UIValidatable.ErrorType.Valid;
            
        } else if (formatter != null && formatter.isUserInputNeeded())
        {
            String data = getText();
            if (StringUtils.isEmpty(data))
            {
                valState = isRequired ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
    
            } else if (!isPartialOK && !isFilled())
            {
                valState = formatter.isLengthOK(data.length()) ? UIValidatable.ErrorType.Valid : UIValidatable.ErrorType.Error;
                // Only validate against the formatter if the it is the right length
                if (valState == UIValidatable.ErrorType.Valid)
                {
                    valState = UIFieldFormatter.isValid(formatter, data, !isAutoFmtOn) ? UIValidatable.ErrorType.Valid : UIValidatable.ErrorType.Error;
                }
            } else if (isPartialOK)
            {
                valState = StringUtils.isNotEmpty(data) ? UIValidatable.ErrorType.Valid : UIValidatable.ErrorType.Error;
            } else
            {
                valState = UIFieldFormatter.isValid(formatter, data, !isAutoFmtOn) ? UIValidatable.ErrorType.Valid : UIValidatable.ErrorType.Error;
            }
        } else
        {
            valState = UIValidatable.ErrorType.Valid;
        }
        
        if (oldState != valState)
        {
            repaint();
        }
        return valState;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#cleanUp()
     */
    public void cleanUp()
    {
        UIHelper.removeFocusListeners(this);
        UIHelper.removeKeyListeners(this);

        for (JFormattedDoc document : documents)
        {
            for (DocumentListener l : document.getDocumentListeners())
            {
                document.removeDocumentListener(l);
            }
        }
        documents.clear();
        documents = null;
        formatter = null;
        fields    = null;
        UIHelper.removeFocusListeners(this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getReason()
     */
    public String getReason()
    {
        return null;
    }

    //--------------------------------------------------------
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(final Object valueArg, final String defaultValue)
    {
        this.defaultValue = defaultValue;

        Object value = formatter.formatToUI("").equals(valueArg) ? null : valueArg;
        String data;

        if (value != null)
        {
            if (value instanceof String)
            {
                data = (String)value;

            } else if (value instanceof Date || value instanceof Calendar) {
                Date dateValue = value instanceof Calendar ? ((Calendar)value).getTime() : (Date)value;
                if (formatter.getPartialDateType().equals(UIFieldFormatterIFace.PartialDateEnum.Month)
                    || formatter.getPartialDateType().equals(UIFieldFormatterIFace.PartialDateEnum.Year)) {
                    data = PartialDateUI.formatForUI(formatter, dateValue);
                } else{
                    data = formatter.getDateWrapper().format(dateValue);
                }
            } else
            {
                data = value.toString();
            }
        } else
        {

            data = StringUtils.isNotEmpty(defaultValue) ? defaultValue : "";
        }
        
        if (origValue == null)
        {
            origValue = value;
        }
        


        setText(getFmtVal(data, value));

        validateState();

        repaint();
    }

    public String getFmtVal(final String strValue, final Object value) {
        String fmtVal;
        // Bug 9297 - Without the fix below popup forms with Auto-Incrementing
        // formatted number that have one segment will not work.
        boolean isPartialOkay = isPartialOK;
        if (formatter != null && isPartialOK) {
            isPartialOkay = formatter.isIncrementer() && formatter.getFields().size() > 1;
        }
        // Done Bug 9297
        if (formatter != null && !isPartialOkay) {
            if (formatter.isInBoundFormatter()) {
                if (strValue.length() > 0 && strValue.length() != formatter.getLength()) {
                    UIRegistry.showError(UIRegistry.getLocalizedMessage("ValFormattedTextField.WR_SIZE", formatter.getName(), strValue.length(), formatter.getLength()));
                }
                needsUpdating = (StringUtils.isEmpty(strValue) || strValue.length() != formatter.getLength() || strValue.contains("#"))
                        && formatter.getAutoNumber() != null && formatter.isIncrementer();
                
                fmtVal = (String)formatter.formatToUI(strValue);


            } else {
                if (value == null) {
                    needsUpdating = true;
                }
                fmtVal = strValue;
            }
        } else {
            fmtVal = strValue;
        }
        return fmtVal;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        if (formatter.isDate() && !isPartialOK)
        {
            return UIHelper.getCalendar(getText(), formatter.getDateWrapper());
        }
        // else
        String val = getText();
        if (formatter.isFromUIFormatter() || isFromUIFmtOverride)
        {
            if (StringUtils.isNotEmpty(val))
            {
                return formatter.formatFromUI(getText());
            }
        }
        return val != null && val.isEmpty() ? null : val;
    }
    
    /**
     * @param dl
     */
    public void addDocumentListener(final DocumentListener dl)
    {
        if (documentListeners == null)
        {
            documentListeners = new Vector<DocumentListener>();
        }
        documentListeners.add(dl);
    }

    /**
     * @param dl
     */
    public void removeDocumentListener(final DocumentListener dl)
    {
        if (documentListeners != null)
        {
            documentListeners.remove(dl);
        }
    }


    /**
     * @param isFromUIFmtOverride the isFromUIFmtOverride to set
     */
    public void setFromUIFmtOverride(boolean isFromUIFmtOverride)
    {
        this.isFromUIFmtOverride = isFromUIFmtOverride;
    }
    
    //--------------------------------------------------------
    // Individual Text Field
    //--------------------------------------------------------
    class BGTextField extends JTextField
    {
        protected String bgStr = "";
        protected Point  pnt   = null;
        protected Insets inner;

        public BGTextField(final int size, final String bgStr)
        {
            super(size);
            this.bgStr = bgStr;
            this.inner = getInsets();
        }
        
        /**
         * @return the bgStr
         */
        public String getBgStr()
        {
            return bgStr;
        }

        /* (non-Javadoc)
         * @see javax.swing.text.JTextComponent#setText(java.lang.String)
         */
        public void setText(final String text)
        {
            JFormattedDoc document = (JFormattedDoc)getDocument();
            document.setIgnoreNotify(shouldIgnoreNotifyDoc);
            super.setText(isEnabled() ? text : "");
            document.setIgnoreNotify(false);
        }
        
        /* (non-Javadoc)
         * @see java.awt.Component#paint(java.awt.Graphics)
         */
        @Override
        public void paint(Graphics g)
        {
            super.paint(g);

            String text = getText();
            int bgStrLen = bgStr == null ? 0 : bgStr.length();
            int txtLen   = text  == null ? 0 : text.length();
            if (isEnabled())
            {
                if (txtLen < bgStrLen)
                {
                    FontMetrics fm   = g.getFontMetrics();
                    int          w   = fm.stringWidth(text);
                    pnt = new Point(inner.left+w, inner.top + fm.getAscent());
    
                    g.setColor(textColor);
                    g.drawString(bgStr.substring(text.length(), bgStr.length()), pnt.x, pnt.y);
                }
                
                if (valState == UIValidatable.ErrorType.Error && isEnabled())
                {
                    UIHelper.drawRoundedRect((Graphics2D)g, isNew ? new Color(249,249,0) : valTextColor.getColor(), getSize(), 1);
                } else if (valState == UIValidatable.ErrorType.Incomplete && isEnabled())
                {
                    UIHelper.drawRoundedRect((Graphics2D)g, new Color(249,249,0), getSize(), 1);
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    //-- 
    //-------------------------------------------------------------------------
    public class JFormattedDoc extends ValPlainTextDocument
    {
        protected int                       docLimit;
        protected JTextField                textField;
        protected UIFieldFormatterIFace     docFormatter;
        protected UIFieldFormatterField     docField;

        /**
         * CReate a special formatted document
         * @param textField the textfield the document is associated with
         * @param formatter the formatter
         * @param docLenLimit the lengthof the format
         */
        public JFormattedDoc(final JTextField            textField, 
                             final UIFieldFormatterIFace formatter,
                             final UIFieldFormatterField docField)
        {
            super();
            
            this.textField    = textField;
            this.docFormatter = formatter;
            this.docField     = docField;
            this.docLimit        = docField.getSize();
        }

        /**
         * Check to see if the input was correct (doesn't check against the separator)
         * @param field the field info
         * @param str the str to be checked
         * @returntrue char matches the type of input, false it is in error
         */
        protected boolean isCharOK(final UIFieldFormatterField field, final String str)
        {
            FieldType type = field.getType();
            if (type == FieldType.alpha && !StringUtils.isAlpha(str))
            {
                return false;

            } else if (type == FieldType.alphanumeric && !StringUtils.isAlphanumeric(str))
            {
                return false;

            } else if ((type == FieldType.numeric || type == FieldType.year) && !StringUtils.isNumeric(str))
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
            String contentStr = textField.getText();
            int    newLen     = contentStr.length() + str.length();
            
            if (newLen == docLimit && str.charAt(0) == autoNumberChar)
            {
                return true;
            }
            return newLen <= docLimit && isCharOK(docField, str);
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
                
            if (okToInsertText(str))
            {
                super.insertString(offset, str, attr);
            }
            
            if (getLength() == docLimit)
            {
                int inx = 0;
                for (UIFieldFormatterField f : fields)
                {
                    int len = fields.size();
                    if (f == docField && inx < len-1)
                    {
                        for (int i=inx+1;i<len;i++)
                        {
                            UIFieldFormatterField nxtField = fields.get(i);
                            if (!nxtField.isByYear() && nxtField.isEntryField())
                            {
                                comps[i].requestFocus();
                                break;
                            }
                        }
                    }
                    inx++;
                }
            }

            if (!shouldIgnoreNotifyDoc)
            {
                validateState();
            }
        }
    }

}
