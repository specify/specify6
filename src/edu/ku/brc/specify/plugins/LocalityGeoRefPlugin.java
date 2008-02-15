/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.services.biogeomancer.GeoCoordDataIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.rstools.BGMRecordSetProcessor;
import edu.ku.brc.specify.rstools.GeoCoordData;
import edu.ku.brc.specify.rstools.GeoLocateRecordSetProcessor;
import edu.ku.brc.specify.rstools.GeoRefRecordSetProcessorBase;
import edu.ku.brc.specify.tasks.ToolsTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIPluginable;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.MultiView;

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
    protected final String     PREFERENCES = "Preferences";
    
    protected Locality         locality   = null;
    protected MultiView        parent     = null;
    
    protected Object           dataObj    = null;
    
    protected boolean          isViewMode = false;
    protected ChangeListener   listener   = null;
    
    protected boolean          doGeoLocate = false;
    
    /**
     * Default Constructor.
     */
    public LocalityGeoRefPlugin()
    {
        locality = null;
        
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                sendToGeoRefTool();
            }
        });
        
        AppPreferences remotePrefs = AppPreferences.getRemote();
        String         tool        = remotePrefs.get("georef_tool", "");
        
        doGeoLocate = tool.equalsIgnoreCase("geolocate");
        
        CommandDispatcher.register(PREFERENCES, this);
    }
    
    /**
     * Dispatches processing command to the provider.
     */
    protected void sendToGeoRefTool()
    {
        if (locality != null)
        {
            Geography geo = locality.getGeography();
            
            if (geo != null)
            {
                String          country  = GeoRefRecordSetProcessorBase.getNameForRank(geo, 200);
                String          state    = GeoRefRecordSetProcessorBase.getNameForRank(geo, 300);
                String          county   = GeoRefRecordSetProcessorBase.getNameForRank(geo, 400);
                
                int id = locality.getLocalityId() != null ? locality.getLocalityId() : 1;
                
                GeoCoordData geoCoordData = new GeoCoordData(id,
                                                             country,
                                                             state,
                                                             county,
                                                             locality.getLocalityName());
                
                List<GeoCoordDataIFace> items = new Vector<GeoCoordDataIFace>();
                items.add(geoCoordData);
    
                
                
                CommandAction command = new CommandAction(ToolsTask.TOOLS, ToolsTask.EXPORT_LIST);
                command.setData(items);
                command.setProperty("tool", doGeoLocate ? GeoLocateRecordSetProcessor.class : BGMRecordSetProcessor.class);
                command.setProperty("listener", this);
                
                JStatusBar statusBar = UIRegistry.getStatusBar();
                statusBar.setText(UIRegistry.getResourceString("WB_OPENING_GOOGLE_EARTH"));
                CommandDispatcher.dispatch(command);
                
            } else
            {
                UIRegistry.displayErrorDlgLocalized("BGM_GEO_REQUIRED");
            }
        }
    }
    
    /**
     * 
     */
    protected void adjustUIForTool()
    {
        if (doGeoLocate)
        {
            setIcon(IconManager.getIcon("BioGeoMancer32", IconManager.IconSize.Std16));
            setText("GeoLocate");
            
        } else
        {
            setIcon(IconManager.getIcon("BioGeoMancer32", IconManager.IconSize.Std16));
            setText("Biogeomancer");
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
     * @see edu.ku.brc.ui.UIPluginable#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#initialize(java.util.Properties, boolean)
     */
    public void initialize(final Properties properties, final boolean isViewModeArg)
    {
        this.isViewMode = isViewModeArg;
        
        Object parentObj = properties.get("parent");
        if (parentObj instanceof MultiView)
        {
            parent = (MultiView)parentObj;
        }
        adjustUIForTool();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setCellName(java.lang.String)
     */
    public void setCellName(final String cellName)
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#setChangeListener(javax.swing.event.ChangeListener)
     */
    public void setChangeListener(final ChangeListener listener)
    {
        this.listener = listener;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.AbstractButton#setEnabled(boolean)
     */
    public void setEnabled(final boolean enable)
    {
        super.setEnabled(enable && !isViewMode && locality != null);
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
           
           locality.setLatitude1(new BigDecimal(Double.parseDouble(gcData.getLatitude())));
           locality.setLongitude1(new BigDecimal(Double.parseDouble(gcData.getLongitude())));
           
           parent.setData(locality);
           
           if (parent.getCurrentValidator() != null)
           {
               parent.getCurrentValidator().setHasChanged(true);
               parent.getCurrentValidator().validateForm();
           }
       }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#shutdown()
     */
    public void shutdown()
    {
        parent = null;
        CommandDispatcher.unregister(PREFERENCES, this);
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
