/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.rstools.GoogleEarthExporter;
import edu.ku.brc.specify.rstools.GoogleEarthPlacemarkIFace;
import edu.ku.brc.specify.tasks.ToolsTask;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIPluginable;
import edu.ku.brc.ui.UIRegistry;
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
public class LocalityGoogleEarthPlugin extends JButton implements GetSetValueIFace, UIPluginable
{
    protected CollectionObject colObj    = null;
    protected CollectingEvent  ce;
    protected Locality         locality;
    protected Object           origData  = null;
    protected boolean          hasPoints = false;
    protected ImageIcon        imageIcon = null;
    
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
            if (locality.getCollectingEvents().size() > 0)
            {
                ImageIcon img = imageIcon != null ? imageIcon : IconManager.getIcon("collectingevent", IconManager.IconSize.Std32);
                for (CollectingEvent colEv : locality.getCollectingEvents())
                {
                    items.add(new CEPlacemark(colEv, img));
                }
            } else
            {
                ImageIcon img = imageIcon != null ? imageIcon : IconManager.getIcon("locality", IconManager.IconSize.Std32);
                items.add(new CEPlacemark(locality, img));
            }
        }
        
        JStatusBar statusBar = UIRegistry.getStatusBar();
        if (items.size() > 0)
        {
            CommandAction command = new CommandAction(ToolsTask.TOOLS,ToolsTask.EXPORT_LIST);
            command.setData(items);
            command.setProperty("tool", GoogleEarthExporter.class);
            statusBar.setText(UIRegistry.getResourceString("WB_OPENING_GOOGLE_EARTH"));
            CommandDispatcher.dispatch(command);
            
        } else
        {
            statusBar.setErrorMessage(UIRegistry.getResourceString("GE_NO_POINTS"));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return origData;
    }
    
    protected ImageIcon getDisciplineIcon(final CollectionObject co)
    {
        return IconManager.getIcon(co.getCollection().getCollectionType().getDiscipline(),  IconManager.IconSize.Std32);
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
                imageIcon = getDisciplineIcon(colObj);
                
            } else if (value instanceof CollectingEvent)
            {
                ce       = (CollectingEvent)value;
                locality = ce.getLocality(); // ce can't be null
                
                if (ce.getCollectionObjects().size() == 1)
                {
                    colObj  = ce.getCollectionObjects().iterator().next();
                    imageIcon = getDisciplineIcon(colObj);
                }
                
            } else if (value instanceof Locality)
            {
                locality = (Locality)value;
                if (locality.getCollectingEvents().size() == 1)
                {
                    ce = locality.getCollectingEvents().iterator().next();
                    if (ce.getCollectionObjects().size() == 1)
                    {
                        colObj  = ce.getCollectionObjects().iterator().next();
                        imageIcon = getDisciplineIcon(colObj);
                    }
                }
            }
            hasPoints = locality != null && locality.getLat1() != null && locality.getLong1() != null;
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
     * @see edu.ku.brc.ui.UIPluginable#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#initialize(java.util.Properties, boolean)
     */
    public void initialize(final Properties properties, final boolean isViewMode)
    {
        setIcon(IconManager.getIcon("GoogleEarth16"));
        setText("Display in GoogleEarth");  // I18N
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
        
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.UIPluginable#shutdown()
     */
    public void shutdown()
    {
        
    }
    
    //---------------------------------------------------------------------------------
    //-- Inner Classes
    //---------------------------------------------------------------------------------
    
    class CEPlacemark implements GoogleEarthPlacemarkIFace 
    {
        protected CollectingEvent colEv;
        protected Locality        locality;
        protected String          title;
        protected ImageIcon       iconURL = null;
        
        /**
         * @param ce
         * @param iconURL
         */
        public CEPlacemark(final CollectingEvent ce, final ImageIcon iconURL)
        {
            this.colEv    = ce;
            this.locality = ce.getLocality();
            this.iconURL  = iconURL;
            
            DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
            title = scrDateFormat.format(ce.getStartDate());
        }
        
        /**
         * @param locality
         * @param iconURL
         */
        public CEPlacemark(final Locality locality, final ImageIcon iconURL)
        {
            this.colEv    = null;
            this.locality = locality;
            this.iconURL  = iconURL;
            title         = locality.getLocalityName();
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
            colEv    = null;
            locality = null;
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
            
            sb.append(locality.getLocalityName());
            sb.append("</td></tr>\n");
            
            sb.append("<tr><td align=\"right\">");
            sb.append(localityTI.getFieldByColumnName("latitude1").getTitle());
            sb.append(":</td><td align=\"left\">");
            sb.append(locality.getLat1());
            sb.append("</td></tr>\n");
            
            sb.append("<tr><td align=\"right\">");
            sb.append(localityTI.getFieldByColumnName("longitude1").getTitle());
            sb.append(":</td><td align=\"left\">");
            sb.append(locality.getLong1());
            sb.append("</td></tr>\n");
            sb.append("</table>\n");
            return sb.toString();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getLatLon()
         */
        public Pair<Double, Double> getLatLon()
        {
            return new Pair<Double, Double>(locality.getLat1(), locality.getLong1());
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
