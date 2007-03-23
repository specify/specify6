/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.plugins;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIPluginable;
import edu.ku.brc.ui.forms.DataObjectGettable;
import edu.ku.brc.ui.forms.DataObjectGettableFactory;
import edu.ku.brc.ui.forms.DataObjectSettable;
import edu.ku.brc.ui.forms.DataObjectSettableFactory;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatter;
import edu.ku.brc.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.ui.validation.UIValidatable;
import edu.ku.brc.ui.validation.ValFormattedTextField;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 2, 2007
 *
 */
public class PartialDateUI extends JPanel implements GetSetValueIFace, UIPluginable, UIValidatable, ChangeListener
{
    //private String[] formatterNames = {"DATE", "PARTIALDATEMONTH", "PARTIALDATEYEAR"};
    
    DataObjectGettable           getter;
    DataObjectSettable           setter;

    protected JComboBox          formatSelector;
    
    protected String             cellName       = null;
    protected boolean            isDisplayOnly  = true;
    protected ChangeListener     changeListener = null;
    
    protected Object             dataObj        = null;
    protected String             dateFieldName  = null;
    protected String             dateTypeName   = null;
    
    protected Date               date           = null;
    protected UIFieldFormatter.PartialDateEnum dateType       = UIFieldFormatter.PartialDateEnum.Full;
    protected UIFieldFormatter.PartialDateEnum origDateType   = UIFieldFormatter.PartialDateEnum.Full;
    protected boolean            dateTypeIsStr  = false;
    
    // UIValidatable && UIPluginable
    protected UIValidatable.ErrorType valState  = UIValidatable.ErrorType.Valid;
    protected boolean            isRequired     = false;
    protected boolean            isChanged      = false;
    protected boolean            isNew          = false;
    
    protected ValFormattedTextField textField;
    
    /**
     * Constrcutor.
     */
    public PartialDateUI()
    {

    }
    
    /**
     * Creates the UI.
     */
    protected void createUI()
    {
        textField = new ValFormattedTextField("Date");
        
        String[] formatKeys = {"PARTIAL_DATE_FULL", "PARTIAL_DATE_MONTH", "PARTIAL_DATE_YEAR"};
        String[] labels     = new String[formatKeys.length];
        for (int i=0;i<formatKeys.length;i++)
        {
            labels[i] = UICacheManager.getResourceString(formatKeys[i]);
        }
        formatSelector = new JComboBox(labels);
        formatSelector.setSelectedIndex(0);
        
        //setFormatter(UIFieldFormatter.PartialDateEnum.Full);
        
        formatSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                JComboBox                        cbx      = (JComboBox)ae.getSource();
                UIFieldFormatter.PartialDateEnum type     = UIFieldFormatter.PartialDateEnum.values()[cbx.getSelectedIndex()+1];
                setFormatter(type);
            }
        });
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("p, 2px, p, p:g", "p"), this);
        CellConstraints cc      = new CellConstraints();
        builder.add(formatSelector, cc.xy(1,1));
        builder.add(textField, cc.xy(3,1));
    }
    
    /**
     * Sets a nPartial Date Formatter into the TextField
     * @param type the partial date type
     */
    protected void setFormatter(final UIFieldFormatter.PartialDateEnum type)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                UIFieldFormatter formatter = UIFieldFormatterMgr.getDateFormmater(type);
                //System.out.println(type+" ["+formatter+"]");
                if (formatter != null)
                {
                    //System.out.println(formatter.getName()+"  "+formatter.getPartialDateType());
                    textField.setFormatter(formatter);
                    textField.repaint();
                    
                    /*
                    DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
                    Calendar  date = (Calendar)textField.getValue();
                    System.out.println(scrDateFormat.format(date));
                    */

                }
            }
        });

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return dataObj;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(final Object value, final String defaultValue)
    {
        dataObj = value;
        
        if (getter == null)
        {
            getter = DataObjectGettableFactory.get(dataObj.getClass().getName(), "edu.ku.brc.ui.forms.DataGetterForObj");
        }

        if (setter == null)
        {
            setter = DataObjectSettableFactory.get(dataObj.getClass().getName(), "edu.ku.brc.ui.forms.DataSetterForObj");
        }

        if (value == null)
        {
            reset();
            return;
        }
        
        Object dateObj = getter.getFieldValue(value, dateFieldName);
        if (dateObj == null && StringUtils.isNotEmpty(defaultValue) && defaultValue.equals("today"))
        {
            dateObj = new Date();
        }
        if (dateObj instanceof Date)
        {
            date = (Date)dateObj;
        }
        
        int inx = 0;
        Object dateTypeObj = getter.getFieldValue(value, dateTypeName);
        if (dateObj instanceof String)
        {
            inx = Integer.parseInt((String)dateTypeObj);
            dateTypeIsStr = true;
            
        } else if (dateTypeObj instanceof Integer)
        {
            inx = ((Integer)dateTypeObj).intValue();
            dateTypeIsStr = false;
        }
        dateType = UIFieldFormatter.PartialDateEnum.values()[inx+1];
        
        textField.setFormatter(UIFieldFormatterMgr.getDateFormmater(dateType));
        textField.setValue(date, "");
        
    }

    //--------------------------------------------------------
    //-- UIPluginable
    //--------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#initialize(java.util.Properties, boolean)
     */
    public void initialize(final Properties properties, final boolean isViewMode)
    {
        this.isDisplayOnly = isViewMode;
        
        dateFieldName = properties.getProperty("dateField");
        dateTypeName  = properties.getProperty("dateTypeField");
        
        createUI();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setCellName(java.lang.String)
     */
    public void setCellName(final String cellName)
    {
        this.cellName = cellName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
     */
    public void setChangeListener(final ChangeListener listener)
    {
        

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setIsDisplayOnly(boolean)
     */
    public void setIsDisplayOnly(final boolean isDisplayOnly)
    {
        

    }

    //--------------------------------------------------------
    // UIValidatable Interface
    //--------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#cleanUp()
     */
    public void cleanUp()
    {

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#getState()
     */
    public ErrorType getState()
    {
        return textField.getState();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#getValidatableUIComp()
     */
    public Component getValidatableUIComp()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#isChanged()
     */
    public boolean isChanged()
    {
        return isChanged || textField.isChanged();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        return textField.isInError();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#isRequired()
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#reset()
     */
    public void reset()
    {
        textField.reset();
        isChanged = false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#setAsNew(boolean)
     */
    public void setAsNew(final boolean isNew)
    {
        this.isNew = isNew;    
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#setChanged(boolean)
     */
    public void setChanged(final boolean isChanged)
    {
        this.isChanged = isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(final boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#setState(edu.ku.brc.ui.validation.UIValidatable.ErrorType)
     */
    public void setState(final ErrorType state)
    {
        textField.setState(state);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.validation.UIValidatable#validateState()
     */
    public ErrorType validateState()
    {
        
        return textField.validateState();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(final ChangeEvent arg0)
    {
        

    }

}
