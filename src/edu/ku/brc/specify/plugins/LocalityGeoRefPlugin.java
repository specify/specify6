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

import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.services.biogeomancer.GeoCoordDataIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.plugins.latlon.LatLonUI;
import edu.ku.brc.specify.rstools.BGMRecordSetProcessor;
import edu.ku.brc.specify.rstools.GeoCoordData;
import edu.ku.brc.specify.rstools.GeoLocateRecordSetProcessor;
import edu.ku.brc.specify.rstools.GeoRefRecordSetProcessorBase;
import edu.ku.brc.specify.tasks.PluginsTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.LatLonConverter;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 15, 2008
 *
 */
public class LocalityGeoRefPlugin extends JButton implements GetSetValueIFace, 
                                                             UIPluginable,
                                                             GeoCoordProviderListenerIFace,
                                                             CommandListener
{
    protected final String           PREFERENCES = "Preferences";
    
    protected Locality               locality    = null;
    protected FormViewObj            fvo         = null;    
    protected Object                 dataObj     = null;
    
    protected boolean                isViewMode  = false;
    protected boolean                doGeoLocate = false;
    
    protected String                 title       = null;
    protected String                 llId        = null;
    protected String                 geoId       = null;
    protected String                 locId       = null;
    protected FormViewObj            parent      = null;
    protected Vector<ChangeListener> listeners   = null;
    
    /**
     * Default Constructor.
     */
    public LocalityGeoRefPlugin()
    {
        loadAndPushResourceBundle("specify_plugins");
        
        title = UIRegistry.getResourceString("LocalityGeoRefPlugin");
        
        popResourceBundle();
        
        locality = null;
        
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                sendToGeoRefTool();
            }
        });
        
        AppPreferences remotePrefs = AppPreferences.getRemote();
        String         tool        = remotePrefs.get("georef_tool", "geolocate");
        
        doGeoLocate = tool.equalsIgnoreCase("geolocate");
        
        CommandDispatcher.register(PREFERENCES, this);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#canCarryForward()
     */
    @Override
    public boolean canCarryForward()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getCarryForwardFields()
     */
    @Override
    public String[] getCarryForwardFields()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getTitle()
     */
    @Override
    public String getTitle()
    {
        return title;
    }

    /**
     * Dispatches processing command to the provider.
     */
    protected void sendToGeoRefTool()
    {
        if (locality != null)
        {
            Geography geo = locality.getGeography();
            if (geo == null)
            {
                if (geoId != null)
                {
                    ValComboBoxFromQuery cbx = parent.getCompById(geoId);
                    if (cbx != null)
                    {
                        geo = (Geography)cbx.getValue();
                    }
                } else
                {
                    JOptionPane.showMessageDialog(null, "The LatLonUI is missing the 'geoid' parameter", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            if (geo != null)
            {
                String          country  = GeoRefRecordSetProcessorBase.getNameForRank(geo, 200);
                String          state    = GeoRefRecordSetProcessorBase.getNameForRank(geo, 300);
                String          county   = GeoRefRecordSetProcessorBase.getNameForRank(geo, 400);
                
                int id = locality.getLocalityId() != null ? locality.getLocalityId() : 1;
                
                String locName = locality.getLocalityName();
                if (StringUtils.isEmpty(locName))
                {
                    if (locId != null)
                    {
                        ValTextField txt = parent.getCompById(locId);
                        if (txt != null)
                        {
                            locName = (String)txt.getValue();
                        }
                    } else
                    {
                        JOptionPane.showMessageDialog(null, "The LatLonUI is missing the 'locid' parameter", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                GeoCoordData geoCoordData = new GeoCoordData(id,
                                                             country,
                                                             state,
                                                             county == null ? "" : county,
                                                             locName);
                
                List<GeoCoordDataIFace> items = new Vector<GeoCoordDataIFace>();
                items.add(geoCoordData);
                
                CommandAction command = new CommandAction(PluginsTask.PLUGINS, PluginsTask.EXPORT_LIST);
                command.setData(items);
                command.setProperty("tool", doGeoLocate ? GeoLocateRecordSetProcessor.class : BGMRecordSetProcessor.class);
                command.setProperty("listener", this);
                
                JStatusBar statusBar = UIRegistry.getStatusBar();
                statusBar.setText(UIRegistry.getResourceString(doGeoLocate ? "GEOLOC_PROCESSING" : "BGM_PROCESSING"));
                CommandDispatcher.dispatch(command);
                
            } else
            {
                UIRegistry.displayErrorDlgLocalized(doGeoLocate ? "GEOLOC_REQUIRED" : "BGM_GEO_REQUIRED");
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        throw new NotImplementedException("isNotEmpty not implement!");
    }

    /**
     * 
     */
    protected void adjustUIForTool()
    {
        if (doGeoLocate)
        {
            setIcon(IconManager.getIcon("GEOLocate16"));
            setText("GEOLocate");
            
        } else
        {
            setIcon(IconManager.getIcon("BioGeoMancer32", IconManager.IconSize.Std16));
            setText("BioGeomancer");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        if (dataObj != null && dataObj instanceof Locality)
        {
            return locality;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        dataObj = value;
        
        boolean enable = false;
        if (value != null && value instanceof Locality)
        {
            locality = (Locality)value;
            
        } else
        {
            locality = null;
            dataObj = null;
        }
        
        if (locality != null)
        {
            enable = true;
        }
        setEnabled(enable);
    }

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
    public void initialize(final Properties properties, final boolean isViewModeArg)
    {
        this.isViewMode = isViewModeArg;
        
        setEnabled(!isViewMode);
        
        geoId = properties.getProperty("geoid");
        locId = properties.getProperty("locid");
        llId  = properties.getProperty("llid");

        adjustUIForTool();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setCellName(java.lang.String)
     */
    public void setCellName(final String cellName)
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
     */
    public void addChangeListener(final ChangeListener listener)
    {
        if (this.listeners == null)
        {
            this.listeners = new Vector<ChangeListener>();
        }
        this.listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setParent(edu.ku.brc.af.ui.forms.FormViewObj)
     */
    public void setParent(final FormViewObj parent)
    {
        this.parent = parent;
    }

    /* (non-Javadoc)
     * @see javax.swing.AbstractButton#setEnabled(boolean)
     */
    public void setEnabled(final boolean enable)
    {
        super.setEnabled(enable && locality != null && !isViewMode);// && locality.getLat1() != null && locality.getLong1() != null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace#aboutToDisplayResults()
     */
    public void aboutToDisplayResults()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace#complete(java.util.List, int)
     */
    public void complete(final List<GeoCoordDataIFace> items, final int itemsUpdated)
    {
       if (parent != null && itemsUpdated > 0 && items.size() > 0)
       {
           GeoCoordDataIFace gcData = items.get(0);
           
           BigDecimal lat1 = new BigDecimal(Double.parseDouble(gcData.getLatitude()));
           BigDecimal lon1 = new BigDecimal(Double.parseDouble(gcData.getLongitude()));

           if (llId == null)
           {
               JOptionPane.showMessageDialog(null, "The LatLonUI is missing the 'llid' parameter", "Error", JOptionPane.ERROR_MESSAGE);
               return;
           }
           LatLonUI latLonUI = parent.getCompById(llId);
           if (latLonUI != null)
           {
               latLonUI.resetUI();
               String latStr = LatLonConverter.ensureFormattedString(lat1, null, LatLonConverter.FORMAT.DDDDDD, LatLonConverter.LATLON.Latitude);
               String lonStr = LatLonConverter.ensureFormattedString(lon1, null, LatLonConverter.FORMAT.DDDDDD, LatLonConverter.LATLON.Longitude);
               latLonUI.setLatLon(latStr, lonStr, null, null);
           }
       }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#shutdown()
     */
    public void shutdown()
    {
        parent = null;
        CommandDispatcher.unregister(PREFERENCES, this);
        
        if (listeners != null)
        {
            listeners.clear();
            listeners = null;
        }
        
        locality = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(PREFERENCES))
        {
            if (cmdAction.isAction("Updated"))
            {
                String geoRefTool = cmdAction.getPropertyAsString("georef_tool");
                doGeoLocate = StringUtils.isNotEmpty(geoRefTool) && geoRefTool.equalsIgnoreCase("geolocate");
                adjustUIForTool();
            }
        }
    }
}
