/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */

package edu.ku.brc.specify.plugins.latlon;

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;
import static edu.ku.brc.util.LatLonConverter.convert;
import static edu.ku.brc.util.LatLonConverter.convertIntToFORMAT;
import static edu.ku.brc.util.LatLonConverter.getFormat;
import static edu.ku.brc.util.LatLonConverter.getFormattedLatLon;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.plugins.UIPluginBase;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.LatLonConverter.FORMAT;
import edu.ku.brc.util.LatLonConverter.LATLON;



/**
 * 
 * Constructs the UI for entering in Lat/Lon data for a Locality Object. This is designed to handle different types of formats 
 * and makes sure the get converted correctly.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 10, 2007
 *
 */
public class LatLonUI extends UIPluginBase implements UIValidatable, ChangeListener
{
    protected final static String[] formatClass             = new String[] {"DDDDPanel", "DDMMMMPanel", "DDMMSSPanel"};
    protected final static String[] formats                 = new String[] {"DDD.DDD", "DD:MM.MM", "DD MM SS"};

    protected final static String[] pointNames              = {"LatLonPoint", "LatLonLineLeft", "LatLonLineRight", "LatLonRectTopLeft", "LatLonRectBottomRight"};
    protected final static String[] typeNames               = {"LatLonPoint", "LatLonLine", "LatLonRect"};
    protected final static String[] typeNamesKeys           = {"Point", "Line", "Rect"};
    protected final static String[] typeToolTipKeys         = {"PointTT", "LineTT", "RectTT"};
    protected final static LatLonUIIFace.LatLonType[] types = {LatLonUIIFace.LatLonType.LLPoint, LatLonUIIFace.LatLonType.LLLine, LatLonUIIFace.LatLonType.LLRect};
    protected final static String[] typeStrs                = {"Point",                          "Line",                          "Rectangle"};
    
    protected String[]              errorMessages;
    
    protected CellConstraints cc = new CellConstraints();
    
    protected String[]                 typeNamesLabels;
    protected Hashtable<LatLonUIIFace.LatLonType, String> typeMapper = new Hashtable<LatLonUIIFace.LatLonType, String>();
    
    
    protected JComboBox                formatSelector;
    protected LatLonUIIFace.LatLonType currentType;
    protected boolean                  stateChangeOK = true;
    
    protected Hashtable<BorderedRadioButton, LatLonUIIFace.LatLonType> selectedTypeHash = new Hashtable<BorderedRadioButton, LatLonUIIFace.LatLonType>();
    
    protected ImageIcon[]        pointImages;
    protected JComponent[]       latLonPanes;
    protected CardLayout         cardLayout = new CardLayout();
    protected JPanel[]           cardSubPanes;
    protected JPanel             cardPanel;
    protected JComponent         currentCardSubPane;
    protected JPanel             botPanel;
    protected JPanel             rightPanel;
    protected Border             panelBorder = BorderFactory.createEtchedBorder();
    protected JLabel             typeLabel   = null;
    protected int                currentInx  = -1;
    protected BorderedRadioButton[] botBtns  = null;
    
    protected Locality           locality;
    protected LatLonUIIFace[]    panels;
    protected String             latLonType;
    
    protected boolean                         hasChanged = false; 
    protected ArrayList<Pair<String, String>> srcLatLon = new ArrayList<Pair<String, String>>(2);
    protected FORMAT                          srcFormat;
    
    // UIValidatable && UIPluginable
    protected UIValidatable.ErrorType valState  = UIValidatable.ErrorType.Valid;
    protected boolean            isRequired = false;
    protected boolean            isChanged  = false;
    protected boolean            isNew      = false;
    protected String             reason     = null;
    
    
    /**
     * Constructs the UI for entering in Lat/Lon data for a Locality Object.
     */
    public LatLonUI()
    {
        loadAndPushResourceBundle("specify_plugins");
        
        srcLatLon.add(new Pair<String, String>());
        srcLatLon.add(new Pair<String, String>());

        typeNamesLabels = new String[typeNamesKeys.length];
        int i = 0;
        for (String key : typeNamesKeys)
        {
            typeNamesLabels[i] = getResourceString(key);
            typeToolTipKeys[i] = getResourceString(typeToolTipKeys[i]);
            i++;
        }
        
        String[] keys = new String[] {"LatLonUI.POINT_ERR", "LatLonUI.POINT_INCMP", "LatLonUI.FIRST_POINT_ERR", 
                                      "LatLonUI.FIRST_POINT_INCMP", "LatLonUI.SEC_POINT_ERR", "LatLonUI.SEC_POINT_INCMP"};
        errorMessages = new String[keys.length];
        i = 0;
        for (String key : keys)
        {
            errorMessages[i++] = getResourceString(key);    
        }
        
        popResourceBundle();
    }
    
    /**
     * Creates the UI.
     * @param localityCEP the locality object (can be null)
     */
    protected void createEditUI()
    {
        PanelBuilder builder = new PanelBuilder(new FormLayout("p", "p, 2px, p"), this);
        
        Color bgColor = getBackground();
        bgColor = new Color(Math.min(bgColor.getRed()+20, 255), 
                            Math.min(bgColor.getGreen()+20, 255), 
                            Math.min(bgColor.getBlue()+20, 255));
        //System.out.println(bgColor);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bgColor), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        
        for (int i=0;i<types.length;i++)
        {
            typeMapper.put(types[i], typeStrs[i]);    
        }
        
        currentType = LatLonUIIFace.LatLonType.LLPoint;
        
        pointImages = new ImageIcon[pointNames.length];
        for (int i=0;i<pointNames.length;i++)
        {
            pointImages[i] = IconManager.getIcon(pointNames[i], IconManager.IconSize.Std16);
        }
        
        cardPanel      = new JPanel(cardLayout);
        formatSelector = createComboBox(formats);
        latLonPanes    = new JComponent[formats.length];
        
        formatSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                swapForm(formatSelector.getSelectedIndex(), currentType);
                
                cardLayout.show(cardPanel, ((JComboBox)ae.getSource()).getSelectedItem().toString());
                
                //stateChanged(null);
            }
        });
         
        Dimension preferredSize = new Dimension(0,0);
        cardSubPanes =  new JPanel[formats.length * 2];
        panels       = new LatLonUIIFace[formats.length * 2];
        int paneInx  = 0;
        for (int i=0;i<formats.length;i++)
        {
            cardSubPanes[i] = new JPanel(new BorderLayout());
            try
            {
                String packageName = "edu.ku.brc.specify.plugins.latlon.";
                LatLonUIIFace latLon1 = Class.forName(packageName+formatClass[i]).asSubclass(LatLonUIIFace.class).newInstance();
                latLon1.setIsRequired(isRequired);
                latLon1.setViewMode(isViewMode);
                latLon1.init();
                latLon1.setChangeListener(this);
                
                JPanel panel1 = (JPanel)latLon1;
                panel1.setBorder(panelBorder);
                panels[paneInx++] = latLon1;
                latLonPanes[i]    = panel1;

                LatLonUIIFace latlon2 = Class.forName(packageName+formatClass[i]).asSubclass(LatLonUIIFace.class).newInstance();
                latlon2.setIsRequired(isRequired);
                latlon2.setViewMode(isViewMode);
                latlon2.init();
                latlon2.setChangeListener(this);

                panels[paneInx++] = latlon2;
                
                JTabbedPane tabbedPane = new JTabbedPane(UIHelper.getOSType() == UIHelper.OSTYPE.MacOSX ? SwingConstants.BOTTOM :  SwingConstants.RIGHT);
                tabbedPane.addTab(null, pointImages[0], (JComponent)panels[paneInx-2]);
                tabbedPane.addTab(null, pointImages[0], (JComponent)panels[paneInx-1]);
                latLonPanes[i] = tabbedPane;

                Dimension size = tabbedPane.getPreferredSize();
                preferredSize.width  = Math.max(preferredSize.width, size.width);
                preferredSize.height = Math.max(preferredSize.height, size.height);
                
                tabbedPane.removeAll();
                cardSubPanes[i].add(panel1, BorderLayout.CENTER);
                cardPanel.add(formats[i], cardSubPanes[i]);
                
                if (locality != null)
                {
                    latLon1.set(locality.getLatitude1(), locality.getLongitude1(), locality.getLat1text(), locality.getLong1text());
                    latlon2.set(locality.getLatitude2(), locality.getLongitude2(), locality.getLat2text(), locality.getLong2text());
                }
                
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        
        // Makes they are all the same size
        for (int i=0;i<formats.length;i++)
        {
            cardSubPanes[i].setPreferredSize(preferredSize);
        }
        
        //final LatLonPanel thisPanel = this;
        
        PanelBuilder botBtnBar = new PanelBuilder(new FormLayout("p:g,p,10px,p,10px,p,p:g", "p"));
        
        BorderedRadioButton.setSelectedBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        BorderedRadioButton.setUnselectedBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        ButtonGroup btnGroup = new ButtonGroup();
        botBtns = new BorderedRadioButton[typeNames.length];
        for (int i=0;i<botBtns.length;i++)
        {
            BorderedRadioButton rb = new BorderedRadioButton(IconManager.getIcon(typeNames[i], IconManager.IconSize.Std16));
            rb.setToolTipText(typeToolTipKeys[i]);
            botBtnBar.add(rb, cc.xy((i*2)+2, 1));
            btnGroup.add(rb);
            botBtns[i] = rb;
            rb.makeSquare();
            selectedTypeHash.put(rb, types[i]);
            
            rb.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ce)
                {
                    stateChanged(null);
                    currentType = selectedTypeHash.get(ce.getSource());
                    swapForm(formatSelector.getSelectedIndex(), currentType);
                }
            });
        }
        botBtns[0].setSelected(true);
        
        if (isViewMode)
        {
            typeLabel   = createLabel(" ");
        }

        PanelBuilder topPane = new PanelBuilder(new FormLayout("l:p, c:p:g", "p"));
        topPane.add(formatSelector,       cc.xy(1, 1));
        topPane.add(isViewMode ? typeLabel : botBtnBar.getPanel(), cc.xy(2, 1));
        
        builder.add(topPane.getPanel(), cc.xy(1, 1));
        builder.add(cardPanel,          cc.xy(1, 3));
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        
        formatSelector.setEnabled(enabled);
        
        for (LatLonUIIFace ll : panels)
        {
            ll.setEnabled(enabled);
        }
        
        if (botBtns != null)
        {
            for (BorderedRadioButton brb : botBtns)
            {
                brb.setEnabled(enabled);
            }
        }
    }
    
    /**
     * @return whether the current panel has a valid Lat and Lon
     */
    public boolean isNotEmpty()
    {
        int curInx = formatSelector.getSelectedIndex() * 2;
        return panels[curInx].getLatitude() != null &&
               panels[curInx].getLongitude() != null;
    }
    
    /**
     * Swaps the the proper form into the panel.
     * @param formInx the index of the format that is being used.
     * @param type the type of point, line or Rect being used
     */
    protected void swapForm(final int                      formInx, 
                            final LatLonUIIFace.LatLonType type)
    {
       if (currentInx != -1 && currentInx != formInx)
       {
            FORMAT[] fmts = FORMAT.values();
            
            FORMAT fromFmt = srcFormat;
            FORMAT toFmt   = fmts[formInx];
            
            LatLonUIIFace prevPanel1 = panels[(currentInx*2)];
            LatLonUIIFace prevPanel2 = panels[(currentInx*2)+1];
            
            LatLonUIIFace nextPanel1 = panels[(formInx*2)];
            LatLonUIIFace nextPanel2 = panels[(formInx*2)+1];
            
            if (prevPanel1.hasChanged() && prevPanel1.validateState(false) == UIValidatable.ErrorType.Valid)
            {
                srcLatLon.get(0).first  = convert(prevPanel1.getLatitudeStr(), fromFmt, toFmt, LATLON.Latitude);
                srcLatLon.get(0).second = convert(prevPanel1.getLatitudeStr(), fromFmt, toFmt, LATLON.Longitude);
                
                srcLatLon.get(1).first  = convert(prevPanel1.getLatitudeStr(), fromFmt, toFmt, LATLON.Latitude);
                srcLatLon.get(1).second = convert(prevPanel1.getLatitudeStr(), fromFmt, toFmt, LATLON.Longitude);
                srcFormat = convertIntToFORMAT(formInx);
                    
                hasChanged = true;
                stateChanged(null);
                prevPanel1.setHasChanged(false);
                
            }
            
            nextPanel1.set(prevPanel1.getLatitude(), 
                           prevPanel1.getLongitude(), 
                           srcLatLon.get(0).first, 
                           srcLatLon.get(0).second);
            
            if (type == LatLonUIIFace.LatLonType.LLPoint)
            {
                nextPanel2.clear();
                
            }
            {
                nextPanel2.set(prevPanel2.getLatitude(), 
                               prevPanel2.getLongitude(), 
                               srcLatLon.get(1).first, 
                               srcLatLon.get(1).second);
            }
        }
        
        // Set the radio button accordingly
        for (BorderedRadioButton rb : selectedTypeHash.keySet())
        {
            if (selectedTypeHash.get(rb).ordinal() == type.ordinal())
            {
                rb.setSelected(true);
                break;
            }
        }
        
        JPanel panel = (JPanel)panels[(formInx*2)];
        
        cardSubPanes[formInx].removeAll();
        
        if (type == LatLonUIIFace.LatLonType.LLPoint)
        {
            cardSubPanes[formInx].add(panel, BorderLayout.CENTER);
            panel.setBorder(panelBorder);

        } else
        {
            JTabbedPane tabbedPane = (JTabbedPane)latLonPanes[formInx];
            cardSubPanes[formInx].add(tabbedPane, BorderLayout.CENTER);
            panel.setBorder(null);
            
            int inx = type == LatLonUIIFace.LatLonType.LLLine ? 1 : 3;
           
            tabbedPane.addTab(null, pointImages[inx++], (JComponent)panels[(formInx*2)]);
            tabbedPane.addTab(null, pointImages[inx], (JComponent)panels[(formInx*2)+1]);
        }

        cardPanel.validate();
        cardPanel.doLayout();
        cardPanel.repaint();
        
        currentInx = formInx;
    }
    
    /**
     * Converts a String to an enum.
     * @param typeStr the string
     * @return the LatLongType enum
     */
    protected static LatLonUIIFace.LatLonType convertLatLongType(final String typeStr)
    {
        if (StringUtils.isEmpty(typeStr) || typeStr.equals("Point"))
        {
            return LatLonUIIFace.LatLonType.LLPoint;
            
        } else if (typeStr.equals("Line"))
        {
            return LatLonUIIFace.LatLonType.LLLine;
            
        } else if (typeStr.equals("Rectangle"))
        {
            return LatLonUIIFace.LatLonType.LLRect;
        }
        // else
        return LatLonUIIFace.LatLonType.LLPoint;
    }
    
    //--------------------------------------------------------
    // GetSetValueIFace Interface
    //--------------------------------------------------------
    
    public void setLatLon(final BigDecimal lat1, 
                          final BigDecimal lon1,
                          final String     latStr1,
                          final String     lonStr1,
                          final BigDecimal lat2, 
                          final BigDecimal lon2,
                          final String     latStr2,
                          final String     lonStr2)
    {
        // NOTE: Every other panel has point 1 or point 2
        int cnt = 1;
        for (LatLonUIIFace panel : panels)
        {
            if (cnt % 2 == 1)
            {
                panel.set(lat1, lon1, latStr1, lonStr1);
            } else
            {
                panel.set(lat2, lon2, latStr2, lonStr2);               
            }
            cnt++;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(final Object value, final String defaultValue)
    {
        super.setValue(value, defaultValue);
        
        if (value == null)
        {
            return;
        }
        
        if (value != null && !(value instanceof Locality))
        {
            throw new RuntimeException("Data ["+value.getClass().getSimpleName()+"] is not of class Locality!");
        }
        
        locality = (Locality)value;
        
        // This figures out if there is a BD value and/or a text value
        // and formats the String if there isn't one
        FORMAT defaultFormat = convertIntToFORMAT(locality.getOriginalLatLongUnit());
        srcLatLon.get(0).first  = getFormattedLatLon(locality.getLatitude1(),  locality.getLat1text(),  defaultFormat);
        srcLatLon.get(0).second = getFormattedLatLon(locality.getLongitude1(), locality.getLong1text(), defaultFormat);
        srcFormat = getFormat(srcLatLon.get(0).first, defaultFormat);
        
        srcLatLon.get(1).first  = getFormattedLatLon(locality.getLatitude2(),  locality.getLat2text(),  defaultFormat);
        srcLatLon.get(1).second = getFormattedLatLon(locality.getLongitude2(), locality.getLong2text(), defaultFormat);
        
        Integer currFormatterIndex = null;
        if (locality != null)
        {
            currFormatterIndex = locality.getOriginalLatLongUnit();
            currentType        = convertLatLongType(locality.getLatLongType());
            
            setLatLon(locality.getLatitude1(), 
                      locality.getLongitude1(), 
                      srcLatLon.get(0).first, 
                      srcLatLon.get(0).second, 
                      locality.getLatitude2(), 
                      locality.getLongitude2(), 
                      srcLatLon.get(1).first, 
                      srcLatLon.get(1).second);
        } else
        {
            currentType = LatLonUIIFace.LatLonType.LLPoint;
            setLatLon(null, null, null, null, null, null, null, null);
        }
        
        stateChangeOK = false;
        int currFormatterInx = currFormatterIndex == null ? 0 : currFormatterIndex;
        formatSelector.setSelectedIndex(currFormatterInx);
        
        swapForm(currFormatterInx, currentType);
        cardLayout.show(cardPanel, formatSelector.getSelectedItem().toString());
        
        if (typeLabel != null)
        {
            typeLabel.setText(typeNamesLabels[currentType.ordinal()]);
        }
        stateChangeOK = true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#getValue()
     */
    @Override
    public Object getValue()
    {
        if (locality != null && currentType != null)
        {
            locality.setLatLongType(typeMapper.get(currentType));
            locality.setOriginalLatLongUnit(formatSelector.getSelectedIndex());
            
            int curInx = formatSelector.getSelectedIndex() * 2;
            
            panels[curInx].getDataFromUI();   // get data for Lat/Long One
            panels[curInx+1].getDataFromUI(); // get data for Lat/Long Two
            
            // Panel One 
            locality.setLatitude1(panels[curInx].getLatitude());
            locality.setLongitude1(panels[curInx].getLongitude());
            
            locality.setLat1text(locality.getLat1() != null ? panels[curInx].getLatitudeStr() : null);
            locality.setLong1text(locality.getLong1() != null ? panels[curInx].getLongitudeStr() : null);   
            
            // Panel Two
            locality.setLatitude2(panels[curInx+1].getLatitude());
            locality.setLongitude2(panels[curInx+1].getLongitude());

            locality.setLat2text(locality.getLat2() != null ? panels[curInx+1].getLatitudeStr() : null);
            locality.setLong2text(locality.getLong2() != null ? panels[curInx+1].getLongitudeStr() : null);   
        }
        return locality;
    }
    
    /**
     * @return
     */
    public Pair<BigDecimal, BigDecimal> getLatLon()
    {
        int curInx = formatSelector.getSelectedIndex() * 2;
        
        panels[curInx].getDataFromUI();   // get data for Lat/Long One
        panels[curInx+1].getDataFromUI(); // get data for Lat/Long Two
        
        Pair<BigDecimal, BigDecimal> latLon = new Pair<BigDecimal, BigDecimal>();
        latLon.first  = panels[curInx].getLatitude();
        latLon.second = panels[curInx].getLongitude();
        return latLon;
    }

    //--------------------------------------------------------
    //-- UIPluginable
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#initialize(java.util.Properties, boolean)
     */
    public void initialize(final Properties propertiesArg, final boolean isViewModeArg)
    {
        super.initialize(propertiesArg, isViewModeArg);
        createEditUI();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#shutdown()
     */
    @Override
    public void shutdown()
    {
        super.shutdown();
        locality = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setViewable(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void setParent(FormViewObj parent)
    {
        
    }

    //--------------------------------------------------------
    // UIValidatable Interface
    //--------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#cleanUp()
     */
    public void cleanUp()
    {
        for (LatLonUIIFace panel : panels)
        {
            panel.cleanUp();
        } 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getState()
     */
    public ErrorType getState()
    {
        return valState;
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
        return isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        return valState != UIValidatable.ErrorType.Valid;
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
        setLatLon(null, null, null, null, null, null, null, null);
        
        valState = isRequired ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
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
    public void setChanged(boolean isChanged)
    {
        this.isChanged = isChanged;
        
        if (isChanged)
        {
            for (PropertyChangeListener l : getPropertyChangeListeners())
            {
                l.propertyChange(new PropertyChangeEvent(LatLonUI.this, "latlon", null, getLatLon()));
            }
        }
        
        for (LatLonUIIFace panel : panels)
        {
            panel.setHasChanged(isChanged);
        } 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setState(edu.ku.brc.ui.forms.validation.UIValidatable.ErrorType)
     */
    public void setState(ErrorType state)
    {
        this.valState = state;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#validateState()
     */
    public ErrorType validateState()
    {
        reason = null;
        // this validates the state
        valState = UIValidatable.ErrorType.Valid;
        
        int startInx = (currentInx * 2);
        for (int i=startInx;i<startInx+2;i++)
        {
            // First check to see if the panel is Valid
            // Empty is Valid.
            boolean isNotPoint  = currentType != LatLonUIIFace.LatLonType.LLPoint;
            if (i == startInx || isNotPoint)
            {
                UIValidatable.ErrorType errType = panels[i].validateState(isNotPoint);
                if (errType.ordinal() > valState.ordinal())
                {
                    //reason   = panels[i].getReason();
                    valState = errType;
                    if (i == startInx)
                    {
                        if (valState == UIValidatable.ErrorType.Incomplete)
                        {
                            reason = isNotPoint ? errorMessages[3] : errorMessages[1];
                        } else
                        {
                            reason = isNotPoint ? errorMessages[2] : errorMessages[0];
                        }
                    } else
                    {
                        reason = valState == UIValidatable.ErrorType.Incomplete ? errorMessages[5] : errorMessages[4];
                    }
                }
            }
        }
        
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getReason()
     */
    public String getReason()
    {
        return reason;
    }

    //--------------------------------------------------------
    // ChangeListener Interface
    //--------------------------------------------------------

    public void stateChanged(ChangeEvent e)
    {
        if (stateChangeOK)
        {
            validateState();
            
            isChanged = true;
            
            notifyChangeListeners(e);
        }
    }

}
