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
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.af.ui.forms.validation.UIValidatable.ErrorType;
import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.plugins.latlon.LatLonUI;
import edu.ku.brc.specify.rstools.GoogleEarthExporter;
import edu.ku.brc.specify.tasks.PluginsTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
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
    protected CollectionObject             colObj       = null;
    protected CollectingEvent              ce;
    protected Locality                     locality;
    protected Object                       origData     = null;
    protected boolean                      hasPoints    = false;
    protected ImageIcon                    imageIcon    = null;
    protected Component                    localityNameComp = null;
    
    protected String                       watchId      = null;
    protected LatLonUI                     latLonPlugin = null;
    protected Vector<ChangeListener>       listeners    = null;
    protected Pair<BigDecimal, BigDecimal> latLon       = null;
    protected boolean                      isLatLonOK   = false;
    protected boolean                      isViewMode   = true;
    
    /**
     * 
     */
    public LocalityGoogleEarthPlugin()
    {
        locality = null;
        
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0)
            {
                doButtonAction();
            }
        });
    }
    
    /**
     * 
     */
    protected void doButtonAction()
    {
        List<LatLonPlacemarkIFace> items = new Vector<LatLonPlacemarkIFace>();
        if (ce != null)
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
                if (geLoc != null && isLatLonOK)
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
        
        JStatusBar statusBar = getStatusBar();
        if (items.size() > 0)
        {
            CommandAction command = new CommandAction(PluginsTask.PLUGINS,PluginsTask.EXPORT_LIST);
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
    @Override
    public Object getValue()
    {
        return origData;
    }
    
    /**
     * @return the icons for the Discipline
     */
    protected ImageIcon getDisciplineIcon()
    {
        Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);//co.getCollection().getDiscipline()
        return IconManager.getIcon(discipline.getType(),  IconManager.IconSize.Std32);
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
        return "GoogleEarth";
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
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {"localityName", "latitude1", "longitude1", "geography"};
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    @Override
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
                
                String select = "SELECT COUNT(*)";
                String from   = " FROM collectingevent WHERE LocalityID = " + locality.getId();
                int ceCount = BasicSQLUtils.getCountAsInt(select + from);
                if (ceCount == 1)
                {
                    int ceID = BasicSQLUtils.getCountAsInt("SELECT CollectingEventID" + from);
                    // In the next call 'false' means don't load all the COs in the CEs
                    // because we don't know how many COs are attached to the CE
                    // and it could take a long time to load (Hibernate)
                    List<CollectingEvent> collectingEvents = locality.getCollectingEvents(false); 
                    ce = collectingEvents.get(0);
                    
                    from = " FROM collectionobject WHERE CollectingEventID = " + ceID;
                    int coCount = BasicSQLUtils.getCountAsInt(select + from);
                    if (coCount == 1)
                    {
                        // Here, we know there is only one CO, so get the CE (there is only one)
                        // but this time load the the CO (there is only one).
                        collectingEvents = locality.getCollectingEvents(); // load all ColObjs
                        ce               = collectingEvents.get(0);
                        colObj           = ce.getCollectionObjects().iterator().next();
                        imageIcon        = getDisciplineIcon();
                    }
                }
            }
            hasPoints = locality != null && locality.getLat1() != null && locality.getLong1() != null;
        } else
        {
            hasPoints = false;
        }
        
        if (latLon != null && isLatLonOK)
        {
            hasPoints = isLatLonOK;
        }
        setEnabled(hasPoints);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.AbstractButton#setEnabled(boolean)
     */
    @Override
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled && (hasPoints || isLatLonOK));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getUIComponent()
     */
    @Override
    public JComponent getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(final Properties properties, final boolean isViewMode)
    {
        this.isViewMode = isViewMode;
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
    @Override
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
            localityNameComp = parent.getControlByName("localityName");
            
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
    @Override
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
                latLon = latLonPlugin.getLatLon();
                isLatLonOK = latLon != null && latLon.first != null && latLon.second != null && latLonPlugin.getState() == ErrorType.Valid;
                setEnabled(isLatLonOK);
            }
        }
    }
    
    //---------------------------------------------------------------------------------
    //-- Inner Classes
    //---------------------------------------------------------------------------------
    
    class CEPlacemark implements LatLonPlacemarkIFace 
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
        @Override
        public ImageIcon getImageIcon()
        {
            return iconURL;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#cleanup()
         */
        @Override
        public void cleanup()
        {
            colEv       = null;
            localityCEP = null;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.rstools.GoogleEarthPlacemarkIFace#getHtmlContent(java.lang.String)
         */
        @Override
        public String getHtmlContent(final String textColorArg)
        {
            String textColor = UIHelper.fixColorForHTML(textColorArg);
            
            DBTableInfo localityTI = DBTableIdMgr.getInstance().getInfoById(Locality.getClassTableId());
            
            StringBuilder sb = new StringBuilder("<table>");
            sb.append("<tr><td align=\"right\" nowrap=\"true\"><font color=\"");
            sb.append(textColor);
            sb.append("\">");
            
            sb.append(localityTI.getFieldByColumnName("localityName").getTitle());
            sb.append(":</font></td><td align=\"left\"><font color=\"");
            sb.append(textColor);
            sb.append("\">");
            
            sb.append(localityCEP.getLocalityName() == null ? "" : localityCEP.getLocalityName());
            sb.append("</font></td></tr>\n");
            
            sb.append("<tr><td align=\"right\"><font color=\"");
            sb.append(textColor);
            sb.append("\">");
            
            sb.append(localityTI.getFieldByColumnName("latitude1").getTitle());
            sb.append(":</td><td align=\"left\"><font color=\"");
            sb.append(textColor);
            sb.append("\">");
            
            sb.append(localityCEP.getLat1());
            sb.append("</font></td></tr>\n");
            
            sb.append("<tr><td align=\"right\"><font color=\"");
            sb.append(textColor);
            sb.append("\">");
            
            sb.append(localityTI.getFieldByColumnName("longitude1").getTitle());
            sb.append(":</font></td><td align=\"left\"><font color=\"");
            sb.append(textColor);
            sb.append("\">");
            
            sb.append(localityCEP.getLong1());
            sb.append("</font></td></tr>\n");
            sb.append("</table>\n");
            
            return sb.toString();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getLatLon()
         */
        @Override
        public Pair<Double, Double> getLatLon()
        {
            return localityCEP != null ? new Pair<Double, Double>(localityCEP.getLat1(), localityCEP.getLong1()) : null;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getTitle()
         */
        @Override
        public String getTitle()
        {
            return title;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#getAltitude()
         */
        @Override
        public Double getAltitude()
        {
            return null;
        }
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#carryForwardStateChange()
     */
    @Override
    public void carryForwardStateChange()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setNewObj(boolean)
     */
    @Override
    public void setNewObj(boolean isNewObj)
    {
        // no op
    }
}
