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

import static edu.ku.brc.ui.UIHelper.createComboBox;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.DataObjectGettable;
import edu.ku.brc.af.ui.forms.DataObjectGettableFactory;
import edu.ku.brc.af.ui.forms.DataObjectSettable;
import edu.ku.brc.af.ui.forms.DataObjectSettableFactory;
import edu.ku.brc.af.ui.forms.FormHelper;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatter;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextField;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
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
    
    protected UIValidatable[]         textFields = new UIValidatable[3];
    protected UIFieldFormatterIFace[] fmts       = new UIFieldFormatterIFace[3];
    protected JPanel[]                panels     = new JPanel[3];
    protected CardLayout              cardLayout = new CardLayout();
    protected JPanel                  cardPanel;
    protected UIValidatable           currentUIV = null;
    
    // UIValidatable && UIPluginable
    protected UIValidatable.ErrorType valState  = UIValidatable.ErrorType.Valid;
    protected boolean            isRequired     = false;
    protected boolean            isChanged      = false;
    protected boolean            isNew          = false;
    
    /**
     * Constructor.
     */
    public PartialDateUI()
    {

    }
    
    /**
     * Creates the UI.
     */
    protected void createUI()
    {
        List<UIFieldFormatterIFace> partialDateList = UIFieldFormatterMgr.getInstance().getDateFormatterList(true);
        for (UIFieldFormatterIFace uiff : partialDateList)
        {
            if (uiff.getName().equals("PartialDateMonth"))
            {
                fmts[1] = uiff;
                textFields[1] = new ValFormattedTextField(uiff, isDisplayOnly, !isDisplayOnly);
                
            } else if (uiff.getName().equals("PartialDateYear"))
            {
                fmts[2] = uiff;
                textFields[2] = new ValFormattedTextField(uiff, isDisplayOnly, !isDisplayOnly);
            }
        }
        
        List<UIFieldFormatterIFace> dateList = UIFieldFormatterMgr.getInstance().getDateFormatterList(false);
        for (UIFieldFormatterIFace uiff : dateList)
        {
            if (uiff.getName().equals("Date"))
            {
                fmts[0] = uiff;
                textFields[0] = new ValFormattedTextFieldSingle(uiff, isDisplayOnly, false);
                currentUIV = textFields[0];
            }
        }
        
        cardPanel = new JPanel(cardLayout);
        
        String[] formatKeys = {"PARTIAL_DATE_FULL", "PARTIAL_DATE_MONTH", "PARTIAL_DATE_YEAR"};
        String[] labels     = new String[formatKeys.length];
        for (int i=0;i<formatKeys.length;i++)
        {
            labels[i]  = UIRegistry.getResourceString(formatKeys[i]);
            cardPanel.add(labels[i], (JComponent)textFields[i]);
        }
        formatSelector = createComboBox(labels);
        formatSelector.setSelectedIndex(0);
        
        //setFormatter(UIFieldFormatter.PartialDateEnum.Full);
        
        formatSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                JComboBox                        cbx  = (JComboBox)ae.getSource();
                UIFieldFormatter.PartialDateEnum type = UIFieldFormatter.PartialDateEnum.values()[cbx.getSelectedIndex()+1];
                Object dataValue = ((GetSetValueIFace)currentUIV).getValue();
                
                currentUIV = textFields[cbx.getSelectedIndex()];
                ((GetSetValueIFace)currentUIV).setValue(currentUIV, null);
                
                setFormatter(type);
                
                cardLayout.show(cardPanel, formatSelector.getSelectedItem().toString());
               
            }
        });
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("p, 2px, p, p:g", "p"), this);
        CellConstraints cc      = new CellConstraints();
        builder.add(formatSelector, cc.xy(1,1));
        builder.add(cardPanel,      cc.xy(3,1));
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
                UIFieldFormatterIFace formatter = UIFieldFormatterMgr.getInstance().getDateFormmater(type);
                //System.out.println(type+" ["+formatter+"]");
                if (formatter != null)
                {
                    //System.out.println(formatter.getName()+"  "+formatter.getPartialDateType());
                    //textField.setFormatter(formatter);
                    //textField.repaint();
                    
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
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        throw new NotImplementedException("isNotEmpty not implement!");
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
        if (dataObj != null)
        {
            if (getter == null)
            {
                getter = DataObjectGettableFactory.get(dataObj.getClass().getName(), FormHelper.DATA_OBJ_GETTER);
            }
    
            if (setter == null)
            {
                setter = DataObjectSettableFactory.get(dataObj.getClass().getName(), FormHelper.DATA_OBJ_SETTER);
            }

        } else if (currentUIV != null)
        {
            for (UIValidatable uiv : textFields)
            {
                ((GetSetValueIFace)uiv).setValue(null, "");
            }
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
        
        if (currentUIV != null)
        {
            ((GetSetValueIFace)currentUIV).setValue(date, "");
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
        
        //textField.setFormatter(UIFieldFormatterMgr.getInstance().getDateFormmater(dateType));
        //textField.setValue(date, "");
        
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
        
        dateFieldName = properties.getProperty("dateField");
        dateTypeName  = properties.getProperty("dateTypeField");
        
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
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setViewable(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void setParent(FormViewObj parent)
    {
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
        return currentUIV.getState();
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
        return currentUIV.isInError();
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
        for (UIValidatable uiv : textFields)
        {
            uiv.reset();
        }
        isChanged = false;
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
        for (UIValidatable uiv : textFields)
        {
            uiv.setState(state);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#validateState()
     */
    public ErrorType validateState()
    {
        return currentUIV.validateState();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(final ChangeEvent arg0)
    {
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getReason()
     */
    public String getReason()
    {
        return null;
    }

}
