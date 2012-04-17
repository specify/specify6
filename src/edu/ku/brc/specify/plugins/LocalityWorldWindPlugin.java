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

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.util.LatLonConverter.convertIntToFORMAT;
import static edu.ku.brc.util.LatLonConverter.ensureFormattedString;

import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.services.mapping.LatLonPoint;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.ui.WorldWindPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.LatLonConverter.FORMAT;
import edu.ku.brc.util.LatLonConverter.LATLON;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;

/**
 * Implementation of a Google Earth Export plugin for the form system.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Aug 12, 2009
 *
 */
public class LocalityWorldWindPlugin extends LocalityGoogleEarthPlugin implements SelectListener
{
    protected Position       lastClickPos  = null;
    protected WorldWindPanel wwPanel       = new WorldWindPanel();
    protected LatLonPoint    latLonPnt     = null;
    
    protected  CustomDialog  worldWindDlg  = null;

    /**
     * 
     */
    public LocalityWorldWindPlugin()
    {
        super();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.LocalityGoogleEarthPlugin#doButtonAction()
     */
    @Override
    protected void doButtonAction()
    {
        List<LatLonPlacemarkIFace> items = new Vector<LatLonPlacemarkIFace>();
        Pair<BigDecimal, BigDecimal> llPair = latLonPlugin.getLatLon();
        
        if (latLonPlugin != null && llPair != null && llPair.first != null && llPair.second != null)
        {
            Locality geLoc = new Locality();
            geLoc.initialize();
            
            geLoc.setLatitude1(llPair.first);
            geLoc.setLongitude1(llPair.second);
            if (locality != null)
            {
                geLoc.setGeography(locality.getGeography());
            }
            ImageIcon img = imageIcon != null ? imageIcon : IconManager.getIcon("locality", IconManager.IconSize.Std32);
            items.add(new CEPlacemark(geLoc, img));
            
        } else  if (ce != null)
        {
            ImageIcon img = imageIcon != null ? imageIcon : IconManager.getIcon("locality", IconManager.IconSize.Std32);
            items.add(new CEPlacemark(ce, img));
            
        } else if (locality != null)
        {
            List<CollectingEvent> collectingEvents = locality.getCollectingEvents(false);
            if (collectingEvents != null && collectingEvents.size() > 0)
            {
                ImageIcon img = imageIcon != null ? imageIcon : IconManager.getIcon("collectingevent", IconManager.IconSize.Std32);
                for (CollectingEvent colEv : collectingEvents)
                {
                    items.add(new CEPlacemark(colEv, img));
                }
            } else if (locality != null)
            {
                Locality geLoc = new Locality();
                geLoc.initialize();
                
                if (localityNameComp != null && localityNameComp instanceof JTextField)
                {
                    geLoc.setLocalityName(((JTextField)localityNameComp).getText());
                }
                
                ImageIcon img = imageIcon != null ? imageIcon : IconManager.getIcon("locality", IconManager.IconSize.Std32);
                if (geLoc != null && isLatLonOK && latLon != null)
                {
                    geLoc.setLatitude1(latLon.first);
                    geLoc.setLongitude1(latLon.second);
                    geLoc.setGeography(locality.getGeography());
                    
                } else if (locality.getLatitude1() != null && locality.getLongitude1() != null)
                {
                    geLoc.setLatitude1(locality.getLatitude1());
                    geLoc.setLongitude1(locality.getLongitude1());
                    geLoc.setGeography(locality.getGeography());
                }
                items.add(new CEPlacemark(geLoc, img));
            }
        }
        
        lastClickPos  = null;
        
        wwPanel.getWorld().getInputHandler().addMouseListener(new MouseAdapter()
        {
            /* (non-Javadoc)
             * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
             */
            @Override
            public void mouseClicked(final MouseEvent e)
            {
                super.mouseClicked(e);
                
                if (!isViewMode)
                {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            Position pos = wwPanel.getWorld().getCurrentPosition();
                            if (!pos.equals(lastClickPos))
                            {
                                if (latLonPnt == null)
                                {
                                    addUserDefinedMarker();
                                } else 
                                {
                                    repositionUserDefMarker();
                                }
                            }
                        }
                    });
                }
            }
        });
        
        if (items.size() > 0)
        {
            wwPanel.placeMarkers(items, 0);
        }
        
        if (worldWindDlg == null)
        {
            int btns = isViewMode ? CustomDialog.CANCEL_BTN : CustomDialog.OKCANCEL;
            worldWindDlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), "World Wind", true, btns, wwPanel);
            worldWindDlg.setCancelLabel(UIRegistry.getResourceString(isViewMode ? "CLOSE" : "CANCEL"));
            worldWindDlg.createUI();
            worldWindDlg.pack();
            worldWindDlg.setSize(900, 700);
        }
        
        worldWindDlg.setVisible(true);
        
        if (!worldWindDlg.isCancelled() && locality != null && latLonPlugin != null && latLonPnt != null)
        {
            FORMAT defaultFormat = convertIntToFORMAT(locality.getOriginalLatLongUnit());
            if (defaultFormat != null)
            {
                String latStr = ensureFormattedString(new BigDecimal(latLonPnt.getLatitude()),  locality.getLat2text(),  defaultFormat, LATLON.Latitude);
                String lonStr = ensureFormattedString(new BigDecimal(latLonPnt.getLongitude()), locality.getLong2text(), defaultFormat, LATLON.Longitude);
    
                latLonPlugin.setLatLon(latStr, lonStr, null, null);
                
                latLon = new Pair<BigDecimal, BigDecimal>(new BigDecimal(latLonPnt.getLatitude()), new BigDecimal(latLonPnt.getLongitude()));
            }
        }
    }
    
    /**
     * 
     */
    private void addUserDefinedMarker()
    {
        Position pos = wwPanel.getWorld().getCurrentPosition();
        // Create User defined point/marker
        latLonPnt = new LatLonPoint(pos.getLatitude().getDegrees(), pos.getLongitude().getDegrees());
        
        List<LatLonPlacemarkIFace> items = new ArrayList<LatLonPlacemarkIFace>(1);
        wwPanel.placeMarkers(items, null);
    }
    
    /**
     * @return the latLonPnt
     */
    public LatLonPlacemarkIFace getLatLonPnt()
    {
        return latLonPnt;
    }

    /**
     * 
     */
    private void repositionUserDefMarker()
    {
        Position pos = wwPanel.getWorld().getCurrentPosition();
        latLonPnt.setLatitude(pos.getLatitude().getDegrees());
        latLonPnt.setLongitude(pos.getLongitude().getDegrees());
        
        List<LatLonPlacemarkIFace> items = new ArrayList<LatLonPlacemarkIFace>(1);
        items.add(latLonPnt);
        wwPanel.placeMarkers(items, null);
        wwPanel.getWorld().repaint();
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getTitle()
     */
    @Override
    public String getTitle()
    {
        return "WorldWind";
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(final Properties properties, final boolean isViewMode)
    {
        this.isViewMode = isViewMode;
        
        setIcon(IconManager.getIcon("WorldWind", IconManager.STD_ICON_SIZE.Std16));
        setText(getResourceString("WW_DSP_IN_WW"));
        
        watchId = properties.getProperty("watch");
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        hasPoints = true;
        isLatLonOK = true;
        setEnabled(true);
    }
    
    /* (non-Javadoc)
     * @see gov.nasa.worldwind.event.SelectListener#selected(gov.nasa.worldwind.event.SelectEvent)
     */
    @Override
    public void selected(final SelectEvent event)
    {
        if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
        {
            // This is a left click
            if (event.hasObjects() && event.getTopPickedObject().hasPosition())
            {
                lastClickPos = wwPanel.getWorld().getCurrentPosition();
                
                // There is a picked object with a position
                /*if (wwPanel.getWorld().getView() instanceof OrbitView)
                {
                    if (event.getTopObject().getClass().equals(BasicMarker.class))
                    {
                        int inx = wwPanel.getMarkers().indexOf(event.getTopObject());
                    } else if (event.getTopObject().getClass().equals(GlobeAnnotation.class))
                    {
                        int inx = wwPanel.getAnnotations().indexOf(event.getTopObject());
                    }
                }*/
            }
        }
    }
}
