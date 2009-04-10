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
package edu.ku.brc.specify.plugins.latlon;

import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;
import static edu.ku.brc.util.LatLonConverter.convert;
import static edu.ku.brc.util.LatLonConverter.convertIntToFORMAT;
import static edu.ku.brc.util.LatLonConverter.ensureFormattedString;

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
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.plugins.UIPluginBase;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.MacBtnBorder;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
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
    private static final Logger log = Logger.getLogger(UIPluginBase.class);
    
    protected final static String[] formatClass             = new String[] {"DDDDPanel", "DDMMMMPanel", "DDMMSSPanel"};
    protected final static String[] formats                 = new String[] {"DDD.DDD", "DD_MM.MM", "DD_MM_SS"};

    protected final static String[] pointNames              = {"LatLonPoint", "LatLonLineLeft", "LatLonLineRight", "LatLonRectTopLeft", "LatLonRectBottomRight"};
    protected final static String[] typeNames               = {"LatLonPoint", "LatLonLine", "LatLonRect"};
    protected final static String[] typeNamesKeys           = {"Point", "Line", "Rect"};
    protected final static String[] typeToolTipKeys         = {"PointTT", "LineTT", "RectTT"};
    protected final static LatLonUIIFace.LatLonType[] types = {LatLonUIIFace.LatLonType.LLPoint, LatLonUIIFace.LatLonType.LLLine, LatLonUIIFace.LatLonType.LLRect};
    protected final static String[] typeStrs                = {"Point",                          "Line",                          "Rectangle"};
    
    protected String[]              errorMessages;
    
    protected CellConstraints cc = new CellConstraints();
    
    protected String[]                 typeNamesLabels;
    protected String[]                 typeToolTips;
    protected Hashtable<LatLonUIIFace.LatLonType, String> typeMapper = new Hashtable<LatLonUIIFace.LatLonType, String>();
    
    
    protected JComboBox                formatSelector;
    protected LatLonUIIFace.LatLonType currentType;
    protected boolean                  stateChangeOK = true;
    protected String[]                 fieldNames;

    protected Hashtable<JToggleButton, LatLonUIIFace.LatLonType> selectedTypeHash = new Hashtable<JToggleButton, LatLonUIIFace.LatLonType>();
    
    protected ImageIcon[]           pointImages;
    protected JComponent[]          latLonPanes;
    protected CardLayout            cardLayout = new CardLayout();
    protected JPanel[]              cardSubPanes;
    protected JPanel                cardPanel;
    protected JComponent            currentCardSubPane;
    protected JPanel                botPanel;
    protected JPanel                rightPanel;
    protected Border                panelBorder = BorderFactory.createEtchedBorder();
    protected JLabel                typeLabel   = null;
    protected int                   currentInx  = -1;
    protected JToggleButton[]        botBtns  = null;
    
    protected Locality             locality;
    protected DDDDPanel[]          panels;
    protected String               latLonType;
    
    protected boolean              hasChanged = false; 
    protected Pair<String, String> srcLatLon1 = new Pair<String, String>();
    protected Pair<String, String> srcLatLon2 = new Pair<String, String>();
    protected FORMAT               srcFormat;
    protected FORMAT               choosenFormat;
    
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
        super();
        
        loadAndPushResourceBundle("specify_plugins");
        
        title = UIRegistry.getResourceString("LatLonUI");
        
        typeNamesLabels = new String[typeNamesKeys.length];
        typeToolTips    = new String[typeToolTipKeys.length];
        int i = 0;
        for (String key : typeNamesKeys)
        {
            typeNamesLabels[i] = getResourceString(key);
            typeToolTips[i]    = getResourceString(typeToolTipKeys[i]);
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
        
        fieldNames = new String[] {"latitude1",   "longitude1", 
                                   "latitude2",   "longitude2", 
                                   "lat1text",    "long1text", 
                                   "lat2text",    "long2text", 
                                   "latLongType", "originalLatLongUnit", 
                                   "srcLatLongUnit", 
                                   };
    }
    
    /**
     * Creates the UI.
     * @param localityCEP the locality object (can be null)
     */
    protected void createEditUI()
    {
        loadAndPushResourceBundle("specify_plugins");
        
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
        
        String[] formatLabels = new String[formats.length];
        for (int i=0;i<formats.length;i++)
        {
            formatLabels[i] = getResourceString(formats[i]);
        }
        cardPanel      = new JPanel(cardLayout);
        formatSelector = createComboBox(formatLabels);
        latLonPanes    = new JComponent[formatLabels.length];
        
        formatSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                swapForm(formatSelector.getSelectedIndex(), currentType);
                
                cardLayout.show(cardPanel, ((JComboBox)ae.getSource()).getSelectedItem().toString());
                
                //stateChanged(null);
            }
        });
         
        Dimension preferredSize = new Dimension(0,0);
        cardSubPanes = new JPanel[formats.length * 2];
        panels       = new DDDDPanel[formats.length * 2];
        int paneInx  = 0;
        for (int i=0;i<formats.length;i++)
        {
            cardSubPanes[i] = new JPanel(new BorderLayout());
            try
            {
                String packageName = "edu.ku.brc.specify.plugins.latlon.";
                DDDDPanel latLon1 = Class.forName(packageName+formatClass[i]).asSubclass(DDDDPanel.class).newInstance();
                latLon1.setIsRequired(isRequired);
                latLon1.setViewMode(isViewMode);
                latLon1.init();
                latLon1.setChangeListener(this);
                
                JPanel panel1 = latLon1;
                panel1.setBorder(panelBorder);
                panels[paneInx++] = latLon1;
                latLonPanes[i]    = panel1;

                DDDDPanel latlon2 = Class.forName(packageName+formatClass[i]).asSubclass(DDDDPanel.class).newInstance();
                latlon2.setIsRequired(isRequired);
                latlon2.setViewMode(isViewMode);
                latlon2.init();
                latlon2.setChangeListener(this);

                panels[paneInx++] = latlon2;
                
                JTabbedPane tabbedPane = new JTabbedPane(UIHelper.getOSType() == UIHelper.OSTYPE.MacOSX ? SwingConstants.BOTTOM :  SwingConstants.RIGHT);
                tabbedPane.addTab(null, pointImages[0], panels[paneInx-2]);
                tabbedPane.addTab(null, pointImages[0], panels[paneInx-1]);
                latLonPanes[i] = tabbedPane;

                Dimension size = tabbedPane.getPreferredSize();
                preferredSize.width  = Math.max(preferredSize.width, size.width);
                preferredSize.height = Math.max(preferredSize.height, size.height);
                
                tabbedPane.removeAll();
                cardSubPanes[i].add(panel1, BorderLayout.CENTER);
                cardPanel.add(formatLabels[i], cardSubPanes[i]);
                
                /*if (locality != null)
                {
                    latLon1.set(locality.getLatitude1(), locality.getLongitude1(), locality.getLat1text(), locality.getLong1text());
                    latlon2.set(locality.getLatitude2(), locality.getLongitude2(), locality.getLat2text(), locality.getLong2text());
                }*/
                
            } catch (Exception e)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LatLonUI.class, e);
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
        
        ButtonGroup btnGroup = new ButtonGroup();
        botBtns = new JToggleButton[typeNames.length];
        
        if (UIHelper.isMacOS())
        {
            /*for (int i=0;i<botBtns.length;i++)
            {
                ImageIcon selIcon   = IconManager.getIcon(typeNames[i]+"Sel", IconManager.IconSize.Std16);
                ImageIcon unselIcon = IconManager.getIcon(typeNames[i], IconManager.IconSize.Std16);
                
                MacIconRadioButton rb = new MacIconRadioButton(selIcon, unselIcon);
                botBtns[i] = rb;
                rb.setBorder(new MacBtnBorder());
                
                Dimension size = rb.getPreferredSize();
                int max = Math.max(size.width, size.height);
                size.setSize(max, max);
                rb.setPreferredSize(size);
            }*/
            
            BorderedRadioButton.setSelectedBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            BorderedRadioButton.setUnselectedBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            for (int i=0;i<botBtns.length;i++)
            {
                BorderedRadioButton rb = new BorderedRadioButton(IconManager.getIcon(typeNames[i], IconManager.IconSize.Std16));
                botBtns[i] = rb;
                rb.makeSquare();
                rb.setBorder(new MacBtnBorder());
            }
        } else
        {
            BorderedRadioButton.setSelectedBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            BorderedRadioButton.setUnselectedBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            for (int i=0;i<botBtns.length;i++)
            {
                BorderedRadioButton rb = new BorderedRadioButton(IconManager.getIcon(typeNames[i], IconManager.IconSize.Std16));
                botBtns[i] = rb;
                rb.makeSquare();
            }
        }
        
        for (int i=0;i<botBtns.length;i++)
        {
            botBtns[i].setToolTipText(typeToolTips[i]);
            botBtnBar.add(botBtns[i], cc.xy((i*2)+2, 1));
            btnGroup.add(botBtns[i]);
            selectedTypeHash.put(botBtns[i], types[i]);
            
            botBtns[i].addActionListener(new ActionListener() {
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
            typeLabel = createLabel(" ");
        }

        PanelBuilder topPane = new PanelBuilder(new FormLayout("l:p, c:p:g", "p"));
        topPane.add(formatSelector,       cc.xy(1, 1));
        topPane.add(isViewMode ? typeLabel : botBtnBar.getPanel(), cc.xy(2, 1));
        
        builder.add(topPane.getPanel(), cc.xy(1, 1));
        builder.add(cardPanel,          cc.xy(1, 3));
        
        popResourceBundle();
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
            for (JToggleButton brb : botBtns)
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
     * @param formatHasChanged
     * @param prevPanel1
     * @param prevPanel2
     * @param latLon1
     * @param latLon2
     * @param fromFmt
     * @param toFmt
     */
    protected void checkPanel(final boolean              formatHasChanged,
                              final DDDDPanel            prevPanel1, 
                              final DDDDPanel            prevPanel2, 
                              final Pair<String, String> latLon1, 
                              final Pair<String, String> latLon2, 
                              final FORMAT               fromFmt,
                              final FORMAT               toFmt)
    {
        if (formatHasChanged)
        {
            if (prevPanel1.validateState(false) == UIValidatable.ErrorType.Valid)
            {
                srcFormat      = prevPanel1.getDefaultFormat();
                latLon1.first  = convert(latLon1.first, fromFmt, toFmt, LATLON.Latitude);
                latLon1.second = convert(latLon1.second, fromFmt, toFmt, LATLON.Longitude);
                
                if (prevPanel2 != null && latLon2 != null)
                {
                    latLon2.first  = convert(prevPanel2.getLatitudeStr(), fromFmt, toFmt, LATLON.Latitude);
                    latLon2.second = convert(prevPanel2.getLongitudeStr(), fromFmt, toFmt, LATLON.Longitude);
                }
                    
                hasChanged = true;
                stateChanged(null);
                prevPanel1.setHasChanged(false);
            }
        } else 
        {
            if (latLon1.first ==  null && latLon1.second == null)
            {
                latLon1.first  = prevPanel1.getLatitudeStr(false);
                latLon1.second = prevPanel1.getLongitudeStr();
            }
            
            if (prevPanel2 != null && latLon2 != null && latLon2.first ==  null && latLon2.second == null)
            {
                latLon2.first  = prevPanel2.getLatitudeStr(false);
                latLon2.second = prevPanel2.getLongitudeStr();
            }
        }
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
            
            DDDDPanel prevPanel1 = panels[(currentInx*2)];
            DDDDPanel prevPanel2 = panels[(currentInx*2)+1];
            
            DDDDPanel nextPanel1 = panels[(formInx*2)];
            DDDDPanel nextPanel2 = panels[(formInx*2)+1];
            
            boolean srcFormatHasChanged = prevPanel1.getDefaultFormat() != srcFormat; // same for either panel 1 or 2
            if (type == LatLonUIIFace.LatLonType.LLPoint)
            {
                if (prevPanel1.hasChanged())
                {
                    checkPanel(srcFormatHasChanged, prevPanel1, null, srcLatLon1, null, fromFmt, toFmt);
                    
                } else if (srcLatLon1.first == null && srcLatLon1.second == null)
                {
                    srcFormat = nextPanel1.getDefaultFormat();
                }
                
            } else
            {
                if (prevPanel1.hasChanged() && !prevPanel2.hasChanged())
                {
                    checkPanel(srcFormatHasChanged, prevPanel1, prevPanel2, srcLatLon1, srcLatLon2, fromFmt, toFmt);
                    
                } else if (prevPanel2.hasChanged() && !prevPanel1.hasChanged())
                {
                    checkPanel(srcFormatHasChanged, prevPanel2, prevPanel1, srcLatLon2, srcLatLon1, fromFmt, toFmt);
                   
                } else if (prevPanel1.hasChanged() && prevPanel2.hasChanged())
                {
                    checkPanel(srcFormatHasChanged, prevPanel1, null, srcLatLon1, null, fromFmt, toFmt);
                    checkPanel(srcFormatHasChanged, prevPanel2, null, srcLatLon2, null, fromFmt, toFmt);
                    
                } else if (srcLatLon1.first == null && srcLatLon1.second == null &&
                           srcLatLon2.first == null && srcLatLon2.second == null)
                {
                    srcFormat = nextPanel1.getDefaultFormat();
                }
            }
            
            nextPanel1.set(srcFormat,
                           srcLatLon1.first, 
                           srcLatLon1.second);
            
            if (type == LatLonUIIFace.LatLonType.LLPoint)
            {
                nextPanel2.clear();
                
            } else
            {
                nextPanel2.set(srcFormat, 
                               srcLatLon2.first, 
                               srcLatLon2.second);
            }
            hasChanged = true;
            stateChanged(null);
            choosenFormat = toFmt;
        }
        
        // Set the radio button accordingly
        for (JToggleButton rb : selectedTypeHash.keySet())
        {
            if (selectedTypeHash.get(rb).ordinal() == type.ordinal())
            {
                rb.setSelected(true);
                break;
            }
        }
        
        JPanel panel = panels[(formInx*2)];
        
        cardSubPanes[formInx].removeAll();
        
        if (type == LatLonUIIFace.LatLonType.LLPoint)
        {
            cardSubPanes[formInx].add(panel, BorderLayout.CENTER);
            panel.setBorder(panelBorder);

        } else
        {
            JTabbedPane tabbedPane = (JTabbedPane)latLonPanes[formInx];
            tabbedPane.removeAll();
            cardSubPanes[formInx].add(tabbedPane, BorderLayout.CENTER);
            panel.setBorder(null);
            
            int inx = type == LatLonUIIFace.LatLonType.LLLine ? 1 : 3;
           
            tabbedPane.addTab(null, pointImages[inx++], panels[(formInx*2)]);
            tabbedPane.addTab(null, pointImages[inx], panels[(formInx*2)+1]);
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
    
    /**
     * Sets the input panel back to DDDDDD and to point.
     */
    public void resetUI()
    {
        botBtns[0].doClick();
        formatSelector.setSelectedIndex(0);
    }
    
    /**
     * @param latStr1
     * @param lonStr1
     * @param latStr2
     * @param lonStr2
     */
    public void setLatLon(final String latStr1,
                          final String lonStr1,
                          final String latStr2,
                          final String lonStr2)
    {
        // NOTE: Every other panel has point 1 or point 2
        if (currentInx > -1)
        {
            srcLatLon1.first  = latStr1;
            srcLatLon1.second = lonStr1;
            
            srcLatLon2.first  = latStr2;
            srcLatLon2.second = lonStr2;

            panels[currentInx*2].set(srcFormat, latStr1, lonStr1);
            panels[(currentInx*2)+1].set(srcFormat, latStr2, lonStr2);
            
            stateChanged(new ChangeEvent(this));
        }
    }
    
    //--------------------------------------------------------
    // GetSetValueIFace Interface
    //--------------------------------------------------------
    
    
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
        
        for (DDDDPanel p : panels)
        {
            p.setHasChanged(false);
        }
        
        locality = (Locality)value;
        if (locality != null)
        {
            // This figures out if there is a BD value and/or a text value
            // and formats the String if there isn't one
            FORMAT defaultFormat = convertIntToFORMAT(locality.getOriginalLatLongUnit());
            choosenFormat        = defaultFormat;
            
            srcFormat         = convertIntToFORMAT(locality.getSrcLatLongUnit());
            srcLatLon1.first  = ensureFormattedString(locality.getLatitude1(),  locality.getLat1text(),  defaultFormat, LATLON.Latitude);
            srcLatLon1.second = ensureFormattedString(locality.getLongitude1(), locality.getLong1text(), defaultFormat, LATLON.Longitude);
            
            srcLatLon2.first  = ensureFormattedString(locality.getLatitude2(),  locality.getLat2text(),  defaultFormat, LATLON.Latitude);
            srcLatLon2.second = ensureFormattedString(locality.getLongitude2(), locality.getLong2text(), defaultFormat, LATLON.Longitude);
        
            currentInx  = defaultFormat.ordinal();
            currentType = convertLatLongType(locality.getLatLongType()); // Point, Line or Rect
            
            setLatLon(srcLatLon1.first, 
                      srcLatLon1.second, 
                      srcLatLon2.first, 
                      srcLatLon2.second);
        } else
        {
            currentInx  = 0;
            currentType = LatLonUIIFace.LatLonType.LLPoint;
            setLatLon(null, null, null, null);
        }
        
        log.debug("******** Index: "+currentInx);
        
        stateChangeOK = false;
        formatSelector.setSelectedIndex(currentInx);
        
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
            locality.setOriginalLatLongUnit(choosenFormat.ordinal());
            log.debug("choosenFormat "+choosenFormat);
            
            int curInx = formatSelector.getSelectedIndex() * 2;
            DDDDPanel prevPanel1 = panels[curInx];
            DDDDPanel prevPanel2 = panels[curInx+1];
            
            prevPanel1.getDataFromUI();   // get data for Lat/Long One
            prevPanel2.getDataFromUI(); // get data for Lat/Long Two
            
            //boolean srcFormatHasChanged = prevPanel1.getDefaultFormat() != srcFormat; // same for either panel 1 or 2
            //if (srcFormatHasChanged)
            //{
                if (prevPanel1.hasChanged())
                {
                    srcFormat = prevPanel1.getDefaultFormat();
                }
            //}
            
            // Panel One 
            locality.setLatitude1(prevPanel1.getLatitude());
            locality.setLongitude1(prevPanel1.getLongitude());
            
            locality.setLat1text(locality.getLat1() != null ? prevPanel1.getSrcLatitudeStr() : null);
            locality.setLong1text(locality.getLong1() != null ? prevPanel1.getSrcLongitudeStr() : null);   
            
            // Panel Two
            locality.setLatitude2(prevPanel2.getLatitude());
            locality.setLongitude2(prevPanel2.getLongitude());

            locality.setLat2text(locality.getLat2() != null ? prevPanel2.getSrcLatitudeStr() : null);
            locality.setLong2text(locality.getLong2() != null ? prevPanel2.getSrcLongitudeStr() : null);
            
            log.debug("srcFormat "+srcFormat);
            locality.setSrcLatLongUnit((byte)srcFormat.ordinal());
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#canCarryForward()
     */
    @Override
    public boolean canCarryForward()
    {
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#getCarryForwardFields()
     */
    @Override
    public String[] getCarryForwardFields()
    {
        return fieldNames;
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
        setLatLon(null, null, null, null);
        
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
        
        if (!isChanged)
        {
            for (LatLonUIIFace panel : panels)
            {
                panel.setHasChanged(isChanged);
            }
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
