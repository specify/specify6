/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.plugins.latlon;

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.util.LatLonConverter.convert;
import static edu.ku.brc.util.LatLonConverter.convertDDDDToDDDD;
import static edu.ku.brc.util.LatLonConverter.getFormat;
import static edu.ku.brc.util.LatLonConverter.parseLatLonStr;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.formatters.NumberMinMaxFormatter;
import edu.ku.brc.af.ui.forms.validation.DataChangeListener;
import edu.ku.brc.af.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.af.ui.forms.validation.UIValidatable.ErrorType;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.LatLonConverter.FORMAT;
import edu.ku.brc.util.LatLonConverter.Part;


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
    protected static final   String[] NORTH_SOUTH = {"N", "S"};
    protected static final   String[] EAST_WEST   = {"E", "W"};
    
    protected static String[] northSouth  = null;
    protected static String[] eastWest    = null;
          
    protected BigDecimal     minusOne      = new BigDecimal("-1.0");
    protected int            decimalFmtLen = 7;

    protected boolean        isViewMode = false;
    protected ValFormattedTextFieldSingle latitudeDD; 
    protected ValFormattedTextFieldSingle longitudeDD;
    
    protected BigDecimal     latitude;
    protected BigDecimal     longitude;
    
    protected String         latitudeStr;
    protected String         longitudeStr;
    protected FORMAT         defaultFormat;
    
    protected JComboBox      latitudeDir;
    protected JComboBox      longitudeDir;
    protected JTextField     latitudeDirTxt;
    protected JTextField     longitudeDirTxt;
    protected JLabel         latLabel;
    protected JLabel         lonLabel;
    
    protected JTextField     latitudeTF;
    protected JTextField     longitudeTF;
    
    protected boolean        hasChanged = false;
    protected boolean        isRequired = false;
    protected ChangeListener changeListener = null;
    protected String         reason         = null;
    
    protected Vector<ValFormattedTextFieldSingle> textFields = new Vector<ValFormattedTextFieldSingle>();
    protected Vector<ValFormattedTextFieldSingle> latTFs      = new Vector<ValFormattedTextFieldSingle>();
    protected Vector<ValFormattedTextFieldSingle> lonTFs      = new Vector<ValFormattedTextFieldSingle>();
    protected Vector<DataChangeNotifier>          dcNotifiers = new Vector<DataChangeNotifier>();

    // For helper methods
    protected StringBuilder sb = new StringBuilder();
    protected enum ValState {Empty, Valid, Error}

    /**
     * Constructor. 
     */
    public DDDDPanel()
    {
        this.defaultFormat = FORMAT.DDDDDD;
        
        if (northSouth == null)
        {
            northSouth = new String[] {getResourceString(NORTH_SOUTH[0]), getResourceString(NORTH_SOUTH[1])};
        }

        if (eastWest == null)
        {
            eastWest = new String[] {getResourceString(EAST_WEST[0]), getResourceString(EAST_WEST[1])};
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        
        if (latitudeDir != null)
        {
            latitudeDir.setEnabled(enabled);
            longitudeDir.setEnabled(enabled);
        }
        if (latitudeDirTxt != null)
        {
            latitudeDirTxt.setEnabled(enabled);
            longitudeDirTxt.setEnabled(enabled);
        }
        
        for (ValFormattedTextFieldSingle vtfs : textFields)
        {
            vtfs.setEnabled(enabled);
        }
        
        latLabel.setEnabled(enabled);
        lonLabel.setEnabled(enabled);
    }
    
    /**
     * Creates and initializes the UI.
     */
    public void init()
    {
        createUI("p, 2px, p, 2px, p", 10, 10, 5, false);
    }
    
    /**
     * Creates the UI for the panel.
     * @param colDef the JGoodies column definition
     * @param latCols the number of columns for the latitude control
     * @param lonCols the number of columns for the longitude control
     * @param cbxIndex the column index of the combobox
     * @return return the builder
     */
    protected PanelBuilder createUI(final String  colDef, 
                                    final int     latCols,
                                    final int     lonCols,
                                    final int     cbxIndex,
                                    final boolean asDDIntegers)
    {
        latitudeDD    = asDDIntegers ? createTextField(Integer.class, latCols, 0, 90, latTFs) : createTextField(Double.class, latCols, 0.0, 90.0, latTFs);
        longitudeDD   = asDDIntegers? createTextField(Integer.class, lonCols, 0, 180, lonTFs) : createTextField(Double.class, lonCols, 0.0, 180.0, lonTFs);
        textFields.addAll(latTFs);
        textFields.addAll(lonTFs);
        
        PanelBuilder    builder    = new PanelBuilder(new FormLayout(colDef, "p, 1px, p, c:p:g"));
        CellConstraints cc         = new CellConstraints();
        
        latitudeDir  = createDirComboxbox(true);
        longitudeDir = createDirComboxbox(false);
        
        JComponent latDir;
        JComponent lonDir;
        if (isViewMode)
        {
            latitudeDirTxt  = new JTextField(2);
            longitudeDirTxt = new JTextField(2);
            
            setControlSize(latitudeDirTxt);
            setControlSize(longitudeDirTxt);

            ViewFactory.changeTextFieldUIForDisplay(latitudeDirTxt, false);
            ViewFactory.changeTextFieldUIForDisplay(longitudeDirTxt, false);
            latDir = latitudeDirTxt;
            lonDir = longitudeDirTxt;   
            
        } else
        {
            latDir = latitudeDir;
            lonDir = longitudeDir;
        }

        builder.add(latLabel = createI18NFormLabel("Latitude", SwingConstants.RIGHT), cc.xy(1, 1));
        builder.add(latitudeDD, cc.xy(3, 1));
        builder.add(latDir, cc.xy(cbxIndex, 1));
        
        builder.add(lonLabel = createI18NFormLabel("Longitude", SwingConstants.RIGHT), cc.xy(1, 3));
        builder.add(longitudeDD, cc.xy(3, 3));
        builder.add(lonDir, cc.xy(cbxIndex, 3));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout(colDef, "p, 1px, p, p"), this);

        PanelBuilder txtPanelPB = new PanelBuilder(new FormLayout("p", "p, 4px, p"));
        
        latitudeTF  = new JTextField(15);
        longitudeTF = new JTextField(15);
        //final JLabel     lat1Lbl     = createI18NFormLabel("LAT1", SwingConstants.RIGHT);
        //final JLabel     lon1Lbl     = createI18NFormLabel("LON1", SwingConstants.RIGHT);
        
        //txtPanelPB.add(lat1Lbl, cc.xy (1, 1)); 
        txtPanelPB.add(latitudeTF,  cc.xy (1, 1)); 
        //txtPanelPB.add(lon1Lbl, cc.xy (1, 3)); 
        txtPanelPB.add(longitudeTF,  cc.xy (1, 3));
        
        latitudeTF.setEditable(false);
        longitudeTF.setEditable(false);
        
        pb.add(builder.getPanel(), cc.xy(1, 1));
        pb.add(txtPanelPB.getPanel(),  cc.xy(3, 1));
     
        textFields.addAll(latTFs);
        textFields.addAll(lonTFs);
        
        return builder;
    }
    
    /**
     * Creates a combox for selecting direction.
     * @param forNorthSouth true N,S, false E,W
     * @return cbx
     */
    public JComboBox createDirComboxbox(final boolean forNorthSouth)
    {
        JComboBox cbx =  createComboBox(forNorthSouth ? northSouth : eastWest );
        cbx.addItemListener(this);
        return cbx;
    }
    
    /**
     * set the data into the U.
     */
    protected void setDataIntoUI()
    {
        if (latitude != null)
        {
            Part[] parts = parseLatLonStr(latitudeStr);
            latitudeDir.setSelectedIndex(latitude.doubleValue() >= 0 ? 0 : 1);
            latitudeDD.setText(parts[0].getPart());
            
            if (latitudeDirTxt != null)
            {
                latitudeDirTxt.setText(latitudeDir.getSelectedItem().toString());
            }
            latitudeTF.setText(latitudeStr);
            
        } else
        {
            latitudeDD.setText("");
            if (latitudeDirTxt != null)
            {
                latitudeDirTxt.setText("");
            }
            latitudeTF.setText("");
        }
        
        if (longitude != null)
        {
            Part[] parts = parseLatLonStr(longitudeStr);
            longitudeDir.setSelectedIndex(longitude.doubleValue() >= 0 ? 0 : 1);
            longitudeDD.setText(parts[0].getPart());
            if (longitudeDirTxt != null)
            {
                longitudeDirTxt.setText(longitudeDir.getSelectedItem().toString());
            }
            longitudeTF.setText(longitudeStr);
        } else
        {
            longitudeDD.setText("");
            if (latitudeDirTxt != null)
            {
                longitudeDirTxt.setText("");
            }    
            longitudeTF.setText("");
        }
    }
    
    /**
     * @param txtFld
     * @param cbx
     * @param val
     * @param dir
     * @param isLat
     */
    protected void set(final ValFormattedTextFieldSingle txtFld, 
                       final JComboBox cbx,
                       final String    val, 
                       final String    dir,
                       final boolean   isLat)
    {
        if (StringUtils.isNotEmpty(val) && StringUtils.isNotEmpty(dir))
        {
            //if ()
        }
    }
    
    /**
     * @param lat1DDDDDD
     * @param lat1Dir
     * @param lon1DDDDDD
     * @param lon1Dir
     * @param lat2DDDDDD
     * @param lat2Dir
     * @param lon2DDDDDD
     * @param lon2Dir
     */
    public void set(final String lat1DDDDDD, final String lat1Dir,
                    final String lon1DDDDDD, final String lon1Dir,
                    final String lat2DDDDDD, final String lat2Dir,
                    final String lon2DDDDDD, final String lon2Dir)
    {
        if (StringUtils.isNotEmpty(lat1DDDDDD))
        latitudeDD.setText(lat1DDDDDD);
        latitudeDD.setText(lon1DDDDDD);
        
        if (lat1Dir.equals(NORTH_SOUTH[0]))
        {
            latitudeDD.setText(lat1DDDDDD);
            latitudeDD.setText(lon1DDDDDD);
        }
    }
    
    /**
     * @param txtFields
     * @return
     */
    protected String getStringFromFields(final ValFormattedTextFieldSingle... txtFields)
    {
        return getStringFromFields(false, txtFields);
    }
    
    /**
     * @param doDisplay
     * @param txtFields
     * @return
     */
    protected String getStringFromFields(final boolean doDisplay, final ValFormattedTextFieldSingle... txtFields)
    {
        String degrees = "\u00b0";
        
        sb.setLength(0);
        
        int cnt = 0;
        for (ValFormattedTextFieldSingle tf : txtFields)
        {
            String str = StringUtils.deleteWhitespace(tf.getText());
            if (StringUtils.isEmpty(str))
            {
                str = "0";
            }
            
            if (cnt > 0) sb.append(' ');
            
            double val = Double.parseDouble(str);
            if (doDisplay && cnt > 0 && val < 10.0)
            {
                //sb.append('0');
            }
            sb.append(str);
            if (doDisplay)
            {
                if (cnt == 0)
                {
                    sb.append(degrees);
                } else if (txtFields.length == 2)
                {
                    sb.append("'");
                } else if (txtFields.length == 3)
                {
                    sb.append(cnt == 1 ? "'" : "\"");
                }
            }
            cnt++;
        }
        return sb.toString();
    }

    /**
     * @param txtFields
     * @return
     */
    protected ValState evalState(final ValFormattedTextFieldSingle... txtFields)
    {
        for (ValFormattedTextFieldSingle tf : txtFields)
        {
            tf.setState(UIValidatable.ErrorType.Valid);
            tf.repaint();
        }
        ValState                    state   = ValState.Empty;
        boolean                     isEmpty = false;
        ValFormattedTextFieldSingle prevTF  = null;
        for (ValFormattedTextFieldSingle tf : txtFields)
        {
            String str = StringUtils.deleteWhitespace(tf.getText());
            if (StringUtils.isEmpty(str))
            {
                isEmpty = true;
                
            } else if (tf.getState() == UIValidatable.ErrorType.Valid && tf.getText().length() > 0)
            {
                if (isEmpty)
                {
                    if (prevTF != null)
                    {
                        prevTF.setState(UIValidatable.ErrorType.Error);
                        prevTF.repaint();
                    }
                    return ValState.Error;
                }
                state =  ValState.Valid;
                
            } else
            {
                return ValState.Error;
            }
            prevTF = tf;
        }
        return state;
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
                latitude = convertDDDDToDDDD(str, NORTH_SOUTH[latitudeDir.getSelectedIndex()]);
                latitudeStr = latitudeTF.getText();
                
            } else
            {
                latitude    = null;
                latitudeStr = null;
            }
            
            
        } else
        {
            String str = longitudeDD.getText();
            if (StringUtils.isNotEmpty(StringUtils.deleteWhitespace(str)))
            {
                longitude = convertDDDDToDDDD(str, EAST_WEST[longitudeDir.getSelectedIndex()]);
                longitudeStr = latitudeTF.getText();
                
            } else
            {
                longitude    = null;
                longitudeStr = null;
            }
        }
    }
    
    /**
     * @return
     */
    public String getLatitudeStr()
    {
        return latitudeDD.getText() + " " + NORTH_SOUTH[latitudeDir.getSelectedIndex()];
    }
    
    /**
     * @return
     */
    public String getLongitudeStr()
    {
        return longitudeDD.getText() + " " + EAST_WEST[longitudeDir.getSelectedIndex()];
    }
    
    /**
     * Helper to create a validated text field and hook up a datachange listener.
     * @param columns the number of columns
     * @return the textfield
     */
    protected ValFormattedTextFieldSingle createTextField(final Class<?> dataCls,
                                                          final int      columns,
                                                          final Number   minValue,
                                                          final Number   maxValue,
                                                          final Vector<ValFormattedTextFieldSingle> txtList)
    {
        NumberMinMaxFormatter       fmt       = new NumberMinMaxFormatter(dataCls, columns, minValue, maxValue);
        ValFormattedTextFieldSingle textField = new ValFormattedTextFieldSingle(fmt, false, false, false);
        
        /*textField.addFocusListener(new FocusAdapter()
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
                ((ValFormattedTextFieldSingle)e.getSource()).setNew(false);
                doDataChanged();
                repaint();
            }
        });*/
        
        if (isViewMode)
        {
            ViewFactory.changeTextFieldUIForDisplay(textField, false);

        } else
        {
            textField.setRequired(isRequired);
            
            DataChangeNotifier dcn = new DataChangeNotifier(null, textField, null);
            dcn.addDataChangeListener(this);
            dcNotifiers.add(dcn);
    
            textField.getDocument().addDocumentListener(dcn);
        }
        
        txtList.add(textField);

        return textField;
    }
    
    /**
     * Notify the changeListeners.
     */
    protected void doDataChangeNotify()
    {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                hasChanged = true;
                if (changeListener != null)
                {
                    changeListener.stateChanged(null);
                }
            }
        });
    }
    
    /**
     *  Helper to notify data has changed.
     */
    protected void doDataChanged()
    {
        doDataChangeNotify();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#setViewMode(boolean)
     */
    public void setViewMode(boolean isViewModeArg)
    {
        this.isViewMode = isViewModeArg;
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
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#set(java.math.BigDecimal, java.math.BigDecimal, java.lang.String, java.lang.String)
     */
    public void set(final BigDecimal latitudeArg, 
                    final BigDecimal longitudeArg,
                    final String     latitudeStrArg, 
                    final String     longitudeStrArg)
    {
        this.latitude     = latitudeArg;
        this.longitude    = longitudeArg;
        
        this.latitudeStr  = convert(latitudeStrArg, getFormat(latitudeStrArg, defaultFormat), defaultFormat, LatLonConverter.LATLON.Latitude);
        this.longitudeStr = convert(longitudeStrArg, getFormat(latitudeStrArg, defaultFormat), defaultFormat, LatLonConverter.LATLON.Longitude);
        
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
        return latitude.doubleValue() >= 0 ? northSouth[0] : northSouth[1];
    }
    
    /* (non-Javadoc)
     * @see LatLonUIIFace#getLongitude()
     */
    public String getLongitudeDir()
    {
        getDataFromUI(false);
        return longitude.doubleValue() >= 0 ? eastWest[0] : eastWest[1];
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
        reason = null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#setChangeListener(javax.swing.event.ChangeListener)
     */
    public void setChangeListener(final ChangeListener changeListener)
    {
        this.changeListener = changeListener;
    }
    
    protected ErrorType validateState(final boolean                             includeEmptyCheck,
                                      final Vector<ValFormattedTextFieldSingle> txtList,
                                      final JTextField                          textTF,
                                      final String                              formattedStr)
    {
        ErrorType state = validateStateTexFields(includeEmptyCheck, txtList);
        textTF.setText(state == ErrorType.Valid || state == ErrorType.Incomplete ? formattedStr : "");
        return state;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#validateState(boolean)
     */
    public ErrorType validateState(final boolean includeEmptyCheck)
    {
        reason = null;
        
        ErrorType latState = validateState(includeEmptyCheck, latTFs, latitudeTF, getLatitudeStr());
        ErrorType lonState = validateState(includeEmptyCheck, lonTFs, longitudeTF, getLongitudeStr());
        
        return latState.ordinal() > lonState.ordinal() ? latState : lonState;
    }
    
    /**
     * @param includeEmptyCheck indicates that empty is invalid
     * @return whether the field is valid
     */
    protected ErrorType validateStateTexFields(final boolean includeEmptyCheck,
                                               final Vector<ValFormattedTextFieldSingle> txtList)
    {
        int completeCnt = 0;
        UIValidatable.ErrorType valState = UIValidatable.ErrorType.Valid;
        for (ValFormattedTextFieldSingle vtf : txtList)
        {
            if (vtf.getText().length() == 0)
            {
                if (includeEmptyCheck)
                {
                    if (valState != UIValidatable.ErrorType.Error)
                    {
                        valState = UIValidatable.ErrorType.Incomplete;
                    }
                } else
                {
                    completeCnt++;
                }
                
            } else
            {
                UIValidatable.ErrorType errType = vtf.validateState();
                if (errType.ordinal() > valState.ordinal())
                {
                    valState = errType;
                }
            }
        }
        if (completeCnt > 0 && txtList.size() != completeCnt)
        {
            return UIValidatable.ErrorType.Incomplete;
        }
        return valState;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#clear()
     */
    public void clear()
    {
        latitudeDD.setText("");
        longitudeDD.setText("");
        
        latitude  = null;
        longitude = null;
        reason    = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#getReason()
     */
    public String getReason()
    {
        return reason;
    }
    
    //--------------------------------------------------------
    // DataChangedListener Interface
    //--------------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.DataChangeListener#dataChanged(java.lang.String, java.awt.Component, edu.ku.brc.ui.forms.validation.DataChangeNotifier)
     */
    public void dataChanged(final String name, final Component comp, final DataChangeNotifier dcn)
    {
        doDataChanged();

    }
    
    //--------------------------------------------------------
    // ItemListener Interface
    //--------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent e)
    {
        doDataChanged();
    }
    
}
