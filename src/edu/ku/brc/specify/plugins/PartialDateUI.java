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

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.lowagie.text.Font;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.DataObjectGettable;
import edu.ku.brc.af.ui.forms.DataObjectGettableFactory;
import edu.ku.brc.af.ui.forms.DataObjectSettable;
import edu.ku.brc.af.ui.forms.DataObjectSettableFactory;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextField;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 2, 2007
 *
 */
public class PartialDateUI extends JPanel implements GetSetValueIFace, 
                                                     UIPluginable, 
                                                     UIValidatable, 
                                                     ChangeListener
{
    private static final Logger log  = Logger.getLogger(PartialDateUI.class);
    
    protected DataObjectGettable      getter;
    protected DataObjectSettable      setter;

    protected FormViewObj             parent;
    protected JComboBox               formatSelector;
    
    protected String                  cellName         = null;
    protected boolean                 isDisplayOnly    = true;
    protected ChangeListener          changeListener   = null;
    protected String                  title;
    
    protected Object                  dataObj          = null;
    protected String                  dateFieldName    = null;
    protected String                  dateTypeName     = null;
    
    protected UIFieldFormatter.PartialDateEnum dateType       = UIFieldFormatter.PartialDateEnum.Full;
    protected UIFieldFormatter.PartialDateEnum origDateType   = UIFieldFormatter.PartialDateEnum.Full;
    protected boolean                 dateTypeIsStr    = false;
    
    protected UIValidatable[]         uivs        = new UIValidatable[3];
    protected JTextField[]            textFields  = new JTextField[3];

    protected JPanel[]                panels      = new JPanel[3];
    protected CardLayout              cardLayout  = new CardLayout();
    protected JPanel                  cardPanel;
    protected UIValidatable           currentUIV  = null;
    protected ActionListener          comboBoxAL  = null;
    
    // UIValidatable && UIPluginable
    protected UIValidatable.ErrorType valState    = UIValidatable.ErrorType.Valid;
    protected boolean            isRequired       = false;
    protected boolean            isChanged        = false;
    protected boolean            isNew            = false;
    protected boolean            ignoreDocChanges = false;
    protected boolean            isDateChanged    = false;

    
    /**
     * Constructor.
     */
    public PartialDateUI()
    {
        loadAndPushResourceBundle("specify_plugins");
        title = UIRegistry.getResourceString("PartialDateUI");
        popResourceBundle();
    }
    
    /**
     * Creates the UI.
     */
    protected void createUI()
    {
        DocumentAdaptor docListener = null;
        if (!isDisplayOnly)
        {
            docListener = new DocumentAdaptor()
            {
                @Override
                protected void changed(DocumentEvent e)
                {
                    super.changed(e);
                    
                    if (!ignoreDocChanges)
                    {
                        isChanged     = true;
                        isDateChanged = true;
                        if (changeListener != null)
                        {
                            changeListener.stateChanged(new ChangeEvent(PartialDateUI. this));
                        }
                    }
                }
            };
        }
        
        List<UIFieldFormatterIFace> partialDateList = UIFieldFormatterMgr.getInstance().getDateFormatterList(true);
        for (UIFieldFormatterIFace uiff : partialDateList)
        {
            if (uiff.getName().equals("PartialDateMonth"))
            {
                ValFormattedTextField tf = new ValFormattedTextField(uiff, isDisplayOnly, !isDisplayOnly);
                tf.setRequired(isRequired);
                if (docListener != null) tf.addDocumentListener(docListener);
                uivs[1]       = tf;
                textFields[1] = tf.getTextField();
                if (isDisplayOnly)
                {
                    ViewFactory.changeTextFieldUIForDisplay(textFields[1], false);
                }
                
            } else if (uiff.getName().equals("PartialDateYear"))
            {
                ValFormattedTextField tf = new ValFormattedTextField(uiff, isDisplayOnly, !isDisplayOnly);
                tf.setRequired(isRequired);
                if (docListener != null) tf.addDocumentListener(docListener);
                uivs[2]       = tf;
                textFields[2] = tf.getTextField();
                if (isDisplayOnly)
                {
                    ViewFactory.changeTextFieldUIForDisplay(textFields[2], false);
                }
            }
        }
        
        List<UIFieldFormatterIFace> dateList = UIFieldFormatterMgr.getInstance().getDateFormatterList(false);
        for (UIFieldFormatterIFace uiff : dateList)
        {
            if (uiff.getName().equals("Date"))
            {
                ValFormattedTextFieldSingle tf = new ValFormattedTextFieldSingle(uiff, isDisplayOnly, false);
                tf.setRequired(isRequired);
                if (docListener != null) tf.addDocumentListener(docListener);
                uivs[0]       = tf;
                textFields[0] = tf;
                if (isDisplayOnly)
                {
                    ViewFactory.changeTextFieldUIForDisplay(textFields[0], false);
                }

            }
        }
        
        cardPanel = new JPanel(cardLayout);
        
        String[] formatKeys = {"PARTIAL_DATE_FULL", "PARTIAL_DATE_MONTH", "PARTIAL_DATE_YEAR"};
        String[] labels     = new String[formatKeys.length];
        for (int i=0;i<formatKeys.length;i++)
        {
            labels[i]  = UIRegistry.getResourceString(formatKeys[i]);
            cardPanel.add(labels[i], (JComponent)uivs[i]);
        }
        
        formatSelector = createComboBox(labels);
        formatSelector.setSelectedIndex(0);
        
        JComponent typDisplayComp = null;
        if (!isDisplayOnly)
        {
            comboBoxAL = new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    JComboBox cbx       = (JComboBox)ae.getSource();
                    Object    dataValue = ((GetSetValueIFace)currentUIV).getValue();//isDateChanged ? ((GetSetValueIFace)currentUIV).getValue() : Calendar.getInstance();
                    currentUIV = uivs[cbx.getSelectedIndex()];
                    
                    ignoreDocChanges = true;
                    ((GetSetValueIFace)currentUIV).setValue(dataValue, null);
                    ignoreDocChanges = false;
                    
                    cardLayout.show(cardPanel, formatSelector.getSelectedItem().toString());
                    
                    isChanged = true;
                    if (changeListener != null)
                    {
                        changeListener.stateChanged(new ChangeEvent(PartialDateUI. this));
                    }
                }
            };
            typDisplayComp = formatSelector;
            formatSelector.addActionListener(comboBoxAL);
        }
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout((typDisplayComp != null ? "p, 2px, f:p:g" : "f:p:g"), "p"), this);
        CellConstraints cc      = new CellConstraints();
        if (typDisplayComp != null)
        {
            builder.add(typDisplayComp, cc.xy(1,1));
            builder.add(cardPanel,      cc.xy(3,1));
        } else
        {
            builder.add(cardPanel,      cc.xy(1,1));
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#canCarryForward()
     */
    @Override
    public boolean canCarryForward()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getCarryForwardFields()
     */
    @Override
    public String[] getCarryForwardFields()
    {
        return new String[] { dateFieldName, dateTypeName};
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        if (currentUIV != null)
        {
            return ((GetSetValueIFace)currentUIV).getValue() != null;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        Object fieldVal = null;
        if (currentUIV != null)
        {
            fieldVal = ((GetSetValueIFace)currentUIV).getValue();
        }
        if (!isDisplayOnly &&
            dataObj != null && 
            StringUtils.isNotEmpty(dateFieldName) &&
            StringUtils.isNotEmpty(dateTypeName) && 
            isChanged)
        {
            verifyGetterSetters();
            
            setter.setFieldValue(dataObj, dateFieldName, fieldVal != null && StringUtils.isNotEmpty(fieldVal.toString()) ? fieldVal : null);
            setter.setFieldValue(dataObj, dateTypeName, formatSelector.getSelectedIndex()+1); // Need to add one because the first value is None
        }
        return dataObj;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        for (UIValidatable uiv : uivs)
        {
            ((JComponent)uiv).setEnabled(enabled);
        }
        formatSelector.setEnabled(enabled);
    }
    
    /**
     * 
     */
    private void verifyGetterSetters()
    {
        if (getter == null)
        {
            getter = DataObjectGettableFactory.get(dataObj.getClass().getName(), FormHelper.DATA_OBJ_GETTER);
        }

        if (setter == null)
        {
            setter = DataObjectSettableFactory.get(dataObj.getClass().getName(), FormHelper.DATA_OBJ_SETTER);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(final Object value, final String defaultValue)
    {
        dataObj = value;
        
        if (dataObj != null)
        {
            verifyGetterSetters();

        } else
        {
            for (UIValidatable uiv : uivs)
            {
                ((GetSetValueIFace)uiv).setValue(null, "");
            }
            currentUIV = uivs[0];
            return;
        }
        
        Calendar date = null;

        Object dateObj = getter.getFieldValue(value, dateFieldName);
        if (dateObj == null && StringUtils.isNotEmpty(defaultValue) && defaultValue.equals("today"))
        {
            date = Calendar.getInstance();
        }
        
        if (dateObj instanceof Calendar)
        {
            date = (Calendar)dateObj;
        }
        
        int inx = 0;
        Object dateTypeObj = getter.getFieldValue(value, dateTypeName);
        if (dateTypeObj instanceof String)
        {
            inx = Integer.parseInt((String)dateTypeObj);
            dateTypeIsStr = true;
            
        } else if (dateTypeObj instanceof Short)
        {
            inx = ((Short)dateTypeObj).intValue();
            dateTypeIsStr = false;
            
        } else if (dateTypeObj instanceof Byte)
        {
            inx = ((Byte)dateTypeObj).intValue();
            dateTypeIsStr = false;
            
        } else 
        {
            inx = 1;
        }
        
        if (inx > 0)
        {
            inx--; // need to subtract one because the first item is "None"
        } else
        {
            log.error(dateTypeName+" was zero and shouldn't have been!");
        }
        
        currentUIV = uivs[inx];
        if (currentUIV != null)
        {
            ignoreDocChanges = true;
            ((GetSetValueIFace)currentUIV).setValue(date, "");
            ignoreDocChanges = false;
        }
        
        dateType = UIFieldFormatter.PartialDateEnum.values()[inx+1];
        formatSelector.removeActionListener(comboBoxAL);
        formatSelector.setSelectedIndex(inx);
        formatSelector.addActionListener(comboBoxAL);
        cardLayout.show(cardPanel, formatSelector.getModel().getElementAt(inx).toString());
    }

    //--------------------------------------------------------
    //-- UIPluginable
    //--------------------------------------------------------
    
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
    public void initialize(final Properties properties, final boolean isViewMode)
    {
        this.isDisplayOnly = isViewMode;
        
        dateFieldName = properties.getProperty("df");
        dateTypeName  = properties.getProperty("tp");

        createUI();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setCellName(java.lang.String)
     */
    public void setCellName(final String cellName)
    {
        this.cellName = cellName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener(final ChangeListener listener)
    {
        this.changeListener = listener;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setViewable(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void setParent(final FormViewObj parent)
    {
        this.parent = parent;
        
        JLabel lbl = parent.getLabelFor(this);
        if (lbl != null && StringUtils.isNotEmpty(dateFieldName))
        {
            DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByClassName(parent.getView().getClassName());
            if (tblInfo != null)
            {
                final DBFieldInfo fi = tblInfo.getFieldByName(dateFieldName);
                if (fi != null)
                {
                    title      = fi.getTitle();
                    isRequired = fi.isRequired();
                    if (uivs[0] instanceof ValFormattedTextFieldSingle)
                    {
                        ((ValFormattedTextFieldSingle)uivs[0]).setRequired(isRequired);
                    } else
                    {
                        for (UIValidatable uiv : uivs)
                        {
                            ((ValFormattedTextField)uiv).setRequired(isRequired);
                        }
                    }
                    
                    if (StringUtils.isNotEmpty(fi.getTitle()))
                    {
                        lbl.setText(fi.getTitle()+":");
                        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                    }
                    
                    if (lbl != null)
                    {
                        lbl.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e)
                            {
                                super.mouseClicked(e);
                                if (e.getClickCount() == 2)
                                {
                                    JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow(),
                                            "<html>"+fi.getDescription(), 
                                            UIRegistry.getResourceString("FormViewObj.UNOTES"), 
                                            JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        });
                    }
                } else
                {
                    log.error("PartialDateUI - Couldn't find date field ["+dateFieldName+"] in data obj View: "+parent.getView().getName());
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#shutdown()
     */
    public void shutdown()
    {
        changeListener = null;
        dataObj        = null;
    }
    
    //--------------------------------------------------------
    // UIValidatable Interface
    //--------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#cleanUp()
     */
    public void cleanUp()
    {

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getState()
     */
    public ErrorType getState()
    {
        return validateState();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    public Component getValidatableUIComp()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isChanged()
     */
    public boolean isChanged()
    {
        return isChanged || (currentUIV != null && currentUIV.isChanged());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        return currentUIV != null ? currentUIV.isInError() : true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isRequired()
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#reset()
     */
    public void reset()
    {
        for (UIValidatable uiv : uivs)
        {
            uiv.reset();
        }
        isChanged     = false;
        isDateChanged = false;
        dataObj       = null;
        
        formatSelector.setSelectedIndex(0); // None is zero, Full is 1
        
        for (JTextField tf : textFields)
        {
            if (tf != null)
            {
                tf.setText("");
            }
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    public void setAsNew(final boolean isNew)
    {
        this.isNew = isNew;    
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    public void setChanged(final boolean isChanged)
    {
        this.isChanged = isChanged;
        if (!isChanged)
        {
            isDateChanged = false;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(final boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setState(edu.ku.brc.ui.forms.validation.UIValidatable.ErrorType)
     */
    public void setState(final ErrorType state)
    {
        for (UIValidatable uiv : uivs)
        {
            uiv.setState(state);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#validateState()
     */
    public ErrorType validateState()
    {
        ErrorType errType = ErrorType.Valid;
        if (currentUIV != null)
        {
            errType = currentUIV.validateState();
            if (errType == ErrorType.Incomplete && !isRequired)
            {
                errType = ErrorType.Valid;
            }
        } else
        {
            errType = ErrorType.Error;
        }
        return errType;
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(final ChangeEvent e)
    {
        System.err.println(e);
        if (changeListener != null)
        {
            changeListener.stateChanged(e);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getReason()
     */
    public String getReason()
    {
        return null;
    }

}
