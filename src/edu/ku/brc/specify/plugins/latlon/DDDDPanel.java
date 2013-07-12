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
package edu.ku.brc.specify.plugins.latlon;

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.util.LatLonConverter.adjustLatLonStr;
import static edu.ku.brc.util.LatLonConverter.convert;
import static edu.ku.brc.util.LatLonConverter.convertDDDDStrToDDDDBD;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.util.Vector;

import javax.swing.BorderFactory;
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

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.formatters.NumberMinMaxFormatter;
import edu.ku.brc.af.ui.forms.validation.DataChangeListener;
import edu.ku.brc.af.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.af.ui.forms.validation.UIValidatable.ErrorType;
import edu.ku.brc.ui.VerticalSeparator;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.LatLonValueInfo;
import edu.ku.brc.util.LatLonConverter.FORMAT;
import edu.ku.brc.util.LatLonConverter.LATLON;

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
    //private static final Logger log = Logger.getLogger(DDDDPanel.class);
            
    protected static final   String[] NORTH_SOUTH = {"N", "S"};
    protected static final   String[] EAST_WEST   = {"E", "W"};
    
    protected static String[] northSouth  = null;
    protected static String[] eastWest    = null;
          
    protected BigDecimal     minusOne      = new BigDecimal("-1.0");
    protected int            decimalFmtLen = 7;

    protected boolean        isViewMode = false;
    protected ValFormattedTextFieldSingle latitudeDD; 
    protected ValFormattedTextFieldSingle longitudeDD;
    
    protected LatLonValueInfo latInfoOrig = null;
    protected LatLonValueInfo lonInfoOrig = null;

    protected LatLonValueInfo latInfoCnvrt = null;
    protected LatLonValueInfo lonInfoCnvrt = null;

    
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
        
        builder.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 2));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout(colDef, "p, 1px, p, p"), this);

        PanelBuilder txtPanelPB = new PanelBuilder(new FormLayout("5px,p,5px,p,2px,p", "p, 4px, p"));
        
        latitudeTF  = new JTextField(15);
        longitudeTF = new JTextField(15);
        
        if (!isViewMode)
        {
            Color bg = getBackground();
            txtPanelPB.add(new VerticalSeparator(bg.darker(), bg.brighter()),  cc.xywh(1, 1, 1, 3));
            txtPanelPB.add(latitudeTF,                  cc.xy (4, 1)); 
            txtPanelPB.add(createI18NLabel("SOURCE"),   cc.xy (6, 1)); 
            txtPanelPB.add(longitudeTF,                 cc.xy (4, 3));
            txtPanelPB.add(createI18NLabel("SOURCE"),   cc.xy (6, 3)); 
            txtPanelPB.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 2));
        }
        
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
        JComboBox cbx = createComboBox(forNorthSouth ? northSouth : eastWest );
        cbx.addItemListener(this);
        return cbx;
    }
    
    /**
     * set the data into the U.
     */
    protected void setDataIntoUI()
    {
        if (StringUtils.isNotEmpty(latitudeStr))
        {
            latitudeDir.removeItemListener(this);
            latitudeDir.setSelectedIndex(latInfoCnvrt.isDirPositive() ? 0 : 1);
            latitudeDir.addItemListener(this);
            
            latitudeDD.setText(latInfoCnvrt.getPart(0));
            
            if (latitudeDirTxt != null)
            {
                latitudeDirTxt.setText(latInfoCnvrt.getDirStr());
            }
            
        } else
        {
            latitudeDir.removeItemListener(this);
            boolean isDefNorth = AppPreferences.getRemote().getBoolean(LatLonUI.LAT_PREF, true);
            latitudeDir.setSelectedIndex(isDefNorth ? 0 : 1);
            latitudeDir.addItemListener(this);
            
            latitudeDD.setText("");
            if (latitudeDirTxt != null)
            {
                latitudeDirTxt.setText("");
            }
            latitudeTF.setText("");
        }
        
        if (StringUtils.isNotEmpty(longitudeStr))
        {
            longitudeDir.removeItemListener(this);
            longitudeDir.setSelectedIndex(lonInfoCnvrt.isDirPositive() ? 0 : 1);
            longitudeDir.addItemListener(this);
            
            longitudeDD.setText(lonInfoCnvrt.getPart(0));
            if (longitudeDirTxt != null)
            {
                longitudeDirTxt.setText(lonInfoCnvrt.getDirStr());
            }
            
        } else
        {
            longitudeDir.removeItemListener(this);
            boolean isDefWest = AppPreferences.getRemote().getBoolean(LatLonUI.LON_PREF, true);
            longitudeDir.setSelectedIndex(isDefWest ? 1 : 0);
            longitudeDir.addItemListener(this);
            
            longitudeDD.setText("");
            if (latitudeDirTxt != null)
            {
                longitudeDirTxt.setText("");
            }    
            longitudeTF.setText("");
        }
    }
    
    /**
     * @param txtFields
     * @return
     */
    protected String getStringFromFields(final ValFormattedTextFieldSingle... txtFields)
    {
        return getStringFromFields(false, true, txtFields);
    }
    
    /**
     * @return the defaultFormat
     */
    public FORMAT getDefaultFormat()
    {
        return defaultFormat;
    }

    /**
     * @param doDisplay
     * @param txtFields
     * @return
     */
    protected String getStringFromFields(final boolean doDisplay,
                                         final boolean inclZeroes,
                                         final ValFormattedTextFieldSingle... txtFields)
    {
        String degrees = "\u00b0";
        
        sb.setLength(0);
        
        int cnt = 0;
        for (ValFormattedTextFieldSingle tf : txtFields)
        {
            String str = StringUtils.deleteWhitespace(tf.getText());
            if (StringUtils.isEmpty(str))
            {
                if (inclZeroes)
                {
                    str = "0";
                } else
                {
                    str = "";
                    continue;
                }
            }
            
            if (cnt > 0) sb.append(' ');
            
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
        //log.debug("["+sb.toString()+"]");
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
                latitude    = convertDDDDStrToDDDDBD(str, NORTH_SOUTH[latitudeDir.getSelectedIndex()]);
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
                longitude = convertDDDDStrToDDDDBD(str, EAST_WEST[longitudeDir.getSelectedIndex()]);
                longitudeStr = latitudeTF.getText();
                
            } else
            {
                longitude    = null;
                longitudeStr = null;
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#getLatitudeStr()
     */
    public String getLatitudeStr()
    {
        return getLatitudeStr(true);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#getLongitudeStr()
     */
    public String getLongitudeStr()
    {
        return getLongitudeStr(true);
    }
    
    /**
     * @return
     */
    public String getLatitudeStr(@SuppressWarnings("unused") final boolean inclZeroes)
    {
        return latitudeDD.getText() + LatLonConverter.DEGREES_SYMBOL + " " + NORTH_SOUTH[latitudeDir.getSelectedIndex()];
    }
    
    /**
     * @return
     */
    public String getLongitudeStr(@SuppressWarnings("unused") final boolean inclZeroes)
    {
        return longitudeDD.getText() + LatLonConverter.DEGREES_SYMBOL + " " + EAST_WEST[longitudeDir.getSelectedIndex()];
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
        throw new RuntimeException("Do Not call this");
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#set(java.math.BigDecimal, java.math.BigDecimal, java.lang.String, java.lang.String)
     */
    public void set(final FORMAT srcFormat,
                    final String srcLatitudeStr, 
                    final String srcLongitudeStr)
    {
        if (srcLatitudeStr != null && srcLongitudeStr != null)
        {
            latInfoOrig = adjustLatLonStr(srcLatitudeStr, srcFormat, true, true, LATLON.Latitude);
            lonInfoOrig = adjustLatLonStr(srcLongitudeStr, srcFormat, true, true, LATLON.Longitude);
    
            if (srcFormat != defaultFormat)
            {
                this.latitudeStr  = convert(latInfoOrig.getStrVal(true), latInfoOrig.getFormat(), defaultFormat, LatLonConverter.LATLON.Latitude);
                this.longitudeStr = convert(lonInfoOrig.getStrVal(true), lonInfoOrig.getFormat(), defaultFormat, LatLonConverter.LATLON.Longitude);
            } else
            {
                this.latitudeStr  = srcLatitudeStr;
                this.longitudeStr = srcLongitudeStr;
            }
            
            latInfoCnvrt = adjustLatLonStr(this.latitudeStr, defaultFormat, false, false, LATLON.Latitude);
            lonInfoCnvrt = adjustLatLonStr(this.longitudeStr, defaultFormat, false, false, LATLON.Longitude);
            
            latitudeTF.setText(srcLatitudeStr);
            longitudeTF.setText(srcLongitudeStr);
        } else
        {
            latInfoOrig = null;
            lonInfoOrig = null;
    
            this.latitudeStr  = "";
            this.longitudeStr = "";
            
            latInfoCnvrt = null;
            lonInfoCnvrt = null;
            
            latitudeTF.setText(srcLatitudeStr);
            longitudeTF.setText(srcLongitudeStr);

        }
        setDataIntoUI();
        hasChanged = false;
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
    
    /**
     * @param includeEmptyCheck indicates that empty is invalid
     * @return whether the field is valid
     */
    protected ErrorType validateStateTexFields(final boolean includeEmptyCheck,
                                               final Vector<ValFormattedTextFieldSingle> txtList)
    {
        int completeCnt = 0;
        int cnt         = 0;
        UIValidatable.ErrorType valState = UIValidatable.ErrorType.Valid;
        for (ValFormattedTextFieldSingle vtf : txtList)
        {
            if (vtf.getText().length() == 0)
            {
                if (includeEmptyCheck && cnt == completeCnt)
                {
                    if (valState != UIValidatable.ErrorType.Error)
                    {
                        valState = UIValidatable.ErrorType.Incomplete;
                    }
                } else
                {
                    completeCnt++;
                }
                
            } else if (completeCnt != cnt && cnt > 0)
            {
                //log.debug(" **** Returning* "+completeCnt+"  "+cnt+"  "+UIValidatable.ErrorType.Error);
                return UIValidatable.ErrorType.Error;
                
            } else
            {
                UIValidatable.ErrorType errType = vtf.validateState();
                if (errType == UIValidatable.ErrorType.Valid)
                {
                    completeCnt++;
                    
                } else if (errType.ordinal() > valState.ordinal())
                {
                    valState = errType;
                }
                
            }
            cnt++;
            //log.debug(completeCnt+"  "+cnt);
        }
        
        /*if (completeCnt > 0 && txtList.size() != completeCnt)
        {
            log.debug(" **** Returning* "+completeCnt+"  "+cnt+"  "+UIValidatable.ErrorType.Incomplete);
            return UIValidatable.ErrorType.Incomplete;
        }*/
        //log.debug(" **** Returning  "+completeCnt+"  "+cnt+"  "+valState);
        return valState;
    }
    /**
     * @param includeEmptyCheck
     * @param txtList
     * @param textTF
     * @param formattedStr
     * @return
     */
    protected ErrorType validateState(final boolean                             includeEmptyCheck,
                                      final Vector<ValFormattedTextFieldSingle> txtList,
                                      final JTextField                          textTF,
                                      final String                              formattedStr)
    {
        ErrorType state = validateStateTexFields(includeEmptyCheck, txtList);
        if (hasChanged)
        {
            textTF.setText(state == ErrorType.Valid || state == ErrorType.Incomplete ? formattedStr : "");
        }
        return state;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.latlon.LatLonUIIFace#validateState(boolean)
     */
    public ErrorType validateState(final boolean includeEmptyCheck)
    {
        reason = null;
        
        ErrorType latState = validateState(includeEmptyCheck, latTFs, latitudeTF,  getLatitudeStr(false));
        ErrorType lonState = validateState(includeEmptyCheck, lonTFs, longitudeTF, getLongitudeStr(false));
        
        return latState.ordinal() > lonState.ordinal() ? latState : lonState;
    }
    
    /**
     * @return
     */
    public String getSrcLatitudeStr()
    {
        return latitudeTF.getText();
    }
    
    /**
     * @return
     */
    public String getSrcLongitudeStr()
    {
        return longitudeTF.getText();
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
