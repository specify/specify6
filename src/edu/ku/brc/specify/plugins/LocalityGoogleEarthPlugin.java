/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.plugins;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getStatusBar;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.af.ui.forms.validation.UIValidatable.ErrorType;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.plugins.latlon.LatLonUI;
import edu.ku.brc.specify.rstools.GoogleEarthExporter;
import edu.ku.brc.specify.rstools.GoogleEarthPlacemarkIFace;
import edu.ku.brc.specify.tasks.ToolsTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.util.Pair;

/**
 * Implementation of a Google Earth Export plugin for the form system.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Oct 17, 2007
 *
 */
public class LocalityGoogleEarthPlugin extends JButton implements GetSetValueIFace, 
                                                                  UIPluginable,
                                                                  PropertyChangeListener
{
    protected CollectionObject colObj    = null;
    protected CollectingEvent  ce;
    protected Locality         locality;
    protected Object           origData  = null;
    protected boolean          hasPoints = false;
    protected ImageIcon        imageIcon = null;
    
    protected String           watchId      = null;
    protected LatLonUI         latLonPlugin = null;
    protected Vector<ChangeListener> listeners = null;
    
    /**
     * 
     */
    public LocalityGoogleEarthPlugin()
    {
        locality = null;
        
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                sendToGoogleEarthTool();
            }
        });
    }
    
    /**
     * 
     */
    protected void sendToGoogleEarthTool()
    {
        List<GoogleEarthPlacemarkIFace> items = new Vector<GoogleEarthPlacemarkIFace>();
        if (ce != null)
        {
            ImageIcon img = imageIcon != null ? imageIcon : IconManager.getIcon("locality", IconManager.IconSize.Std32);
            items.add(new CEPlacemark(ce, img));
            
            
        } else if (locality != null)
        {
            List<CollectingEvent> collectingEvents = locality.getCollectingEvents();
            if (collectingEvents.size() > 0)
            {
                ImageIcon img = imageIcon != null ? imageIcon : IconManager.getIcon("collectingevent", IconManager.IconSize.Std32);
                for (CollectingEvent colEv : collectingEvents)
                {
                    items.add(new CEPlacemark(colEv, img));
                }
            } else
            {
                ImageIcon img = imageIcon != null ? imageIcon : IconManager.getIcon("locality", IconManager.IconSize.Std32);
                items.add(new CEPlacemark(locality, img));
            }
        }
        
        JStatusBar statusBar = getStatusBar();
        if (items.size() > 0)
        {
            CommandAction command = new CommandAction(ToolsTask.TOOLS,ToolsTask.EXPORT_LIST);
            command.setData(items);
            command.setProperty("tool", GoogleEarthExporter.class);
            statusBar.setText(getResourceString("WB_OPENING_GOOGLE_EARTH"));
            CommandDispatcher.dispatch(command);
            
        } else
        {
            statusBar.setErrorMessage(getResourceString("GE_NO_POINTS"));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return origData;
    }
    
    protected ImageIcon getDisciplineIcon()
    {
        Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);//co.getCollection().getDiscipline()
        return IconManager.getIcon(discipline.getName(),  IconManager.IconSize.Std32);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(final Object value, final String defaultValue)
    {
        origData = value;
        
        if (value != null)
        {
            if (value instanceof CollectionObject)
            {
                colObj = (CollectionObject)value;
                ce     = colObj.getCollectingEvent();
                if (ce != null)
                {
                    locality = ce.getLocality();
                }
                imageIcon = getDisciplineIcon();
                
            } else if (value instanceof CollectingEvent)
            {
                ce       = (CollectingEvent)value;
                locality = ce.getLocality(); // ce can't be null
                
                if (ce.getCollectionObjects().size() == 1)
                {
                    colObj  = ce.getCollectionObjects().iterator().next();
                    imageIcon = getDisciplineIcon();
                }
                
            } else if (value instanceof Locality)
            {
                locality = (Locality)value;
                List<CollectingEvent> collectingEvents = locality.getCollectingEvents();
                if (collectingEvents.size() == 1)
                {
                    ce = collectingEvents.get(0);
                    if (ce.getCollectionObjects().size() == 1)
                    {
                        colObj  = ce.getCollectionObjects().iterator().next();
                        imageIcon = getDisciplineIcon();
                    }
                }
            }
            hasPoints = locality != null && locality.getLat1() != null && locality.getLong1() != null;
        } else
        {
            hasPoints = false;
        }
        setEnabled(hasPoints);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.AbstractButton#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled && hasPoints);
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
    public void initialize(final Properties properties, final boolean isViewMode)
    {
        setIcon(IconManager.getIcon("GoogleEarth16"));
        setText(getResourceString("GE_DSP_IN_GE"));
        
        watchId = properties.getProperty("watch");
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
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setViewable(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void setParent(FormViewObj parent)
    {
        if (parent != null && StringUtils.isNotEmpty(watchId))
        {
            Component comp = parent.getCompById(watchId);
            if (comp instanceof LatLonUI)
            {
                latLonPlugin = (LatLonUI)comp;
                latLonPlugin.addPropertyChangeListener(this);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#shutdown()
     */
    public void shutdown()
    {
        if (latLonPlugin != null)
        {
            latLonPlugin.removePropertyChangeListener(this);
            latLonPlugin = null;
        }
        
        if (listeners != null)
        {
            listeners.clear();
            listeners = null;
        }
        locality = null;
    }
    
    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals("latlon"))
        {
            Object obj = evt.getNewValue();
            if (obj instanceof Pair<?, ?>)
            {
                Pair<BigDecimal, BigDecimal> latLon = latLonPlugin.getLatLon();
                super.setEnabled(latLon != null && 
                        latLon.first != null && 
                        latLon.second != null && 
                        StringUtils.isNotEmpty(((Locality)latLonPlugin.getValue()).getLocalityName()) &&
                        latLonPlugin.getState() == ErrorType.Valid);
            }
        }
    }
    
    //---------------------------------------------------------------------------------
    //-- Inner Classes
    //---------------------------------------------------------------------------------
    
    class CEPlacemark implements GoogleEarthPlacemarkIFace 
    {
        protected CollectingEvent colEv;
        protected Locality        localityCEP;
        protected String          title;
        protected ImageIcon       iconURL = null;
        
        /**
         * @param ce
         * @param iconURL
         */
        public CEPlacemark(final CollectingEvent ce, final ImageIcon iconURL)
        {
            this.colEv       = ce;
            this.localityCEP = ce.getLocality();
            this.iconURL     = iconURL;
            
            DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
            title = ce.getStartDate() != null ? scrDateFormat.format(ce.getStartDate()) : getResourceString("GE_NO_START_DATE");
        }
        
        /**
         * @param locality
         * @param iconURL
         */
        public CEPlacemark(final Locality locality, final ImageIcon iconURL)
        {
            this.colEv       = null;
            this.localityCEP = locality;
            this.iconURL     = iconURL;
            this.title       = locality.getLocalityName();
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.specify.rstools.GoogleEarthPlacemarkIFace#getIconURL()
         */
        public ImageIcon getImageIcon()
        {
            return iconURL;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#cleanup()
         */
        public void cleanup()
        {
            colEv       = null;
            localityCEP = null;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getHtmlContent()
         */
        public String getHtmlContent()
        {
            DBTableInfo localityTI = DBTableIdMgr.getInstance().getInfoById(Locality.getClassTableId());
            
            StringBuilder sb = new StringBuilder("<table>");
            sb.append("<tr><td align=\"right\">");
            sb.append(localityTI.getFieldByColumnName("localityName").getTitle());
            sb.append(":</td><td align=\"left\">");
            
            sb.append(localityCEP.getLocalityName());
            sb.append("</td></tr>\n");
            
            sb.append("<tr><td align=\"right\">");
            sb.append(localityTI.getFieldByColumnName("latitude1").getTitle());
            sb.append(":</td><td align=\"left\">");
            sb.append(localityCEP.getLat1());
            sb.append("</td></tr>\n");
            
            sb.append("<tr><td align=\"right\">");
            sb.append(localityTI.getFieldByColumnName("longitude1").getTitle());
            sb.append(":</td><td align=\"left\">");
            sb.append(localityCEP.getLong1());
            sb.append("</td></tr>\n");
            sb.append("</table>\n");
            return sb.toString();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getLatLon()
         */
        public Pair<Double, Double> getLatLon()
        {
            return new Pair<Double, Double>(localityCEP.getLat1(), localityCEP.getLong1());
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getTitle()
         */
        public String getTitle()
        {
            return title;
        }
    }
}
