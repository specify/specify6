/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.plugins.latlon;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.validation.DataChangeListener;
import edu.ku.brc.ui.validation.DataChangeNotifier;
import edu.ku.brc.ui.validation.UIValidatable;
import edu.ku.brc.ui.validation.ValTextField;
import edu.ku.brc.ui.validation.UIValidatable.ErrorType;

/**
 * 
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 8, 2007
 *
 */
public class DDDDPanel extends JPanel implements LatLonUIIFace, DataChangeListener, ItemListener
{
    protected BigDecimal     minusOne      = new BigDecimal("-1.0");

    protected ValTextField   latitudeDD; 
    protected ValTextField   longitudeDD;
    
    protected BigDecimal     latitude;
    protected BigDecimal     longitude;
    
    protected JComboBox      latitudeDir;
    protected JComboBox      longitudeDir;
    
    protected boolean        hasChanged = false;
    protected boolean        isRequired = false;
    protected ChangeListener changeListener = null;
    
    protected Vector<ValTextField>       textFields  = new Vector<ValTextField>();
    protected Vector<DataChangeNotifier> dcNotifiers = new Vector<DataChangeNotifier>();


    /**
     * Constrcutor. 
     */
    public DDDDPanel()
    {
        createUI("p, 2px, p, 2px, p", 10, 10, 5);
    }
    
    /**
     * Creates the UI for the panel.
     * @param colDef the JGoodies column definition
     * @param latCols the number of columns for the latitude control
     * @param lonCols the number of columns for the longitude control
     * @param cbxIndex the column index of the combobox
     * @return return the builder
     */
    protected PanelBuilder createUI(final String colDef, 
                                    final int latCols,
                                    final int lonCols,
                                    final int cbxIndex)
    {
        latitudeDD    = createTextField(latCols);
        longitudeDD   = createTextField(lonCols);

        PanelBuilder    builder    = new PanelBuilder(new FormLayout(colDef, "p, 1px, p, c:p:g"), this);
        CellConstraints cc         = new CellConstraints();

        builder.add(new JLabel("Latitude:", JLabel.RIGHT), cc.xy(1, 1));
        builder.add(latitudeDD, cc.xy(3, 1));
        builder.add(latitudeDir = createDirComboxbox(true), cc.xy(cbxIndex, 1));
        
        builder.add(new JLabel("Longitude:", JLabel.RIGHT), cc.xy(1, 3));
        builder.add(longitudeDD, cc.xy(3, 3));
        builder.add(longitudeDir = createDirComboxbox(false), cc.xy(cbxIndex, 3));
        
     
        return builder;
    }
    
    /**
     * Creates a combox for selecting direction
     * @param forNorthSouth true N,S, false E,W
     * @return cbx
     */
    public JComboBox createDirComboxbox(final boolean forNorthSouth)
    {
        JComboBox cbx =  new JComboBox(forNorthSouth ? new String[] {"N", "S"} : new String[] {"E", "W"} ); // I18N
        cbx.addItemListener(this);
        return cbx;
    }
    
    /**
     * set the data into the UI 
     */
    protected void setDataIntoUI()
    {

        if (latitude != null)
        {
            latitudeDir.setSelectedIndex(latitude.doubleValue() >= 0 ? 0 : 1);
            latitudeDD.setText(LatLonConverter.format(latitude.abs()));   
        }
        
        if (longitude != null)
        {
            longitudeDir.setSelectedIndex(longitude.doubleValue() >= 0 ? 0 : 1);
            longitudeDD.setText(LatLonConverter.format(longitude.abs()));
        }
 
    }
    
    /**
     * Helper method for setting the data into the UI.
     * @param doLatitude true does latitude, false does longitude
     */
    protected void getDataFromUI(final boolean doLatitude)
    {
        if (doLatitude)
        {
            String str = latitudeDD.getText();
            if (StringUtils.isNotEmpty(StringUtils.deleteWhitespace(str)))
            {
                latitude = new BigDecimal(str).abs();
                if (latitudeDir.getSelectedIndex() == 0)
                {
                    latitude = latitude.multiply(minusOne);
                }
            }
        } else
        {
            String str = longitudeDD.getText();
            if (StringUtils.isNotEmpty(StringUtils.deleteWhitespace(str)))
            {
                longitude = new BigDecimal(str).abs();
                if (longitudeDir.getSelectedIndex() == 0)
                {
                    longitude = longitude.multiply(minusOne);
                }
            }
        }
    }
    
    /**
     * Helper to create a validated text field and hook up a datachange listener.
     * @param columns the number of columns
     * @return the textfield
     */
    protected ValTextField createTextField(final int columns)
    {
        ValTextField textField = new ValTextField(columns);
        textField.setRequired(isRequired);
        
        DataChangeNotifier dcn = new DataChangeNotifier(null, textField, null);
        dcn.addDataChangeListener(this);
        dcNotifiers.add(dcn);

        textField.getDocument().addDocumentListener(dcn);
        
        textFields.add(textField);

        return textField;
    }
    
    /**
     *  Helper to notify data has changed.
     */
    protected void doDataChanged()
    {
        hasChanged = true;
        
        if (changeListener != null)
        {
            changeListener.stateChanged(null);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#getDataFromUI()
     */
    public void getDataFromUI()
    {
        getDataFromUI(true);
        getDataFromUI(false);
    }
    
    /* (non-Javadoc)
     * @see LatLonUIIFace#set(java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal, java.math.BigDecimal)
     */
    public void set(final BigDecimal latitude1, 
                    final BigDecimal longitude1)
    {
        this.latitude  = latitude1;
        this.longitude = longitude1;
        
        setDataIntoUI();
    }
    
    /* (non-Javadoc)
     * @see LatLonUIIFace#getLatitude()
     */
    public BigDecimal getLatitude()
    {
        getDataFromUI(true);
        return latitude;
    }
    
    /* (non-Javadoc)
     * @see LatLonUIIFace#getLongitude()
     */
    public BigDecimal getLongitude()
    {
        getDataFromUI(false);
        return longitude;
    }
    
    /* (non-Javadoc)
     * @see LatLonUIIFace#getLatitude()
     */
    public String getLatitudeDir()
    {
        getDataFromUI(true);
        return latitude.doubleValue() > 0 ? "N" : "S"; // I18N
    }
    
    /* (non-Javadoc)
     * @see LatLonUIIFace#getLongitude()
     */
    public String getLongitudeDir()
    {
        getDataFromUI(false);
        return longitude.doubleValue() > 0 ? "W" : "E"; // I18N
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#hasChanged()
     */
    public boolean hasChanged()
    {
        return hasChanged;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#setHasChanged(boolean)
     */
    public void setHasChanged(boolean hasChanged)
    {
        this.hasChanged = hasChanged;
        
        for (DataChangeNotifier dcn : dcNotifiers)
        {
            dcn.setDataChanged(hasChanged);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#setIsRequired(boolean)
     */
    public void setIsRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#cleanUp()
     */
    public void cleanUp()
    {
        for (DataChangeNotifier dcn : dcNotifiers)
        {
            dcn.cleanUp();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#setChangeListener(javax.swing.event.ChangeListener)
     */
    public void setChangeListener(final ChangeListener changeListener)
    {
        this.changeListener = changeListener;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#validateState()
     */
    public ErrorType validateState()
    {
        
        UIValidatable.ErrorType valState = UIValidatable.ErrorType.Valid;
        for (ValTextField vtf : textFields)
        {
            UIValidatable.ErrorType errType = vtf.validateState();
            if (errType.ordinal() > valState.ordinal())
            {
                valState = errType;
            }
        }
        return valState;
    }
    
    //--------------------------------------------------------
    // DataChangedListener Interface
    //--------------------------------------------------------
    
    public void dataChanged(String name, Component comp, DataChangeNotifier dcn)
    {
        doDataChanged();
    }
    
    //--------------------------------------------------------
    // ItemListener Interface
    //--------------------------------------------------------
    public void itemStateChanged(ItemEvent e)
    {
        doDataChanged();
    }
}
