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

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.prefs.AppPrefsCache;
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
    protected CollectionObject colObj = null;
    protected CollectingEvent  ce;
    protected Locality         locality;
    
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
        items.add(new CEPlacemark(ce));
        
        CommandAction command = new CommandAction(ToolsTask.TOOLS,ToolsTask.EXPORT_LIST);
        command.setData(items);
        command.setProperty("tool", GoogleEarthExporter.class);
        JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setText(UIRegistry.getResourceString("WB_OPENING_GOOGLE_EARTH"));
        CommandDispatcher.dispatch(command);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        return colObj != null ? colObj : ce;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        boolean enable = false;
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
                
            } else if (value instanceof CollectingEvent)
            {
                ce       = (CollectingEvent)value;
                locality = ce.getLocality(); // ce can't be null
                
            } else if (value instanceof Locality)
            {
                locality = (Locality)value;
            }
            
            if (locality != null)
            {
                if (locality.getLat1() != null && locality.getLong1() != null)
                {
                    enable = true;
                }
            }
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
        protected String          title;
        
        public CEPlacemark(CollectingEvent ce)
        {
            this.colEv = ce;
            DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
            title = scrDateFormat.format(ce.getStartDate());
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#cleanup()
         */
        public void cleanup()
        {
            colEv = null;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getHtmlContent()
         */
        public String getHtmlContent()
        {
            StringBuilder sb = new StringBuilder("<table>");
            sb.append("<tr><td align=\"center\">");
            sb.append(colEv.getLocality().getLat1());
            sb.append(", ");
            sb.append(colEv.getLocality().getLong1());
            sb.append("</td></tr>\n");
            sb.append("</table>\n");
            return sb.toString();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.exporters.GoogleEarthPlacemarkIFace#getLatLon()
         */
        public Pair<Double, Double> getLatLon()
        {
            return new Pair<Double, Double>(colEv.getLocality().getLat1(), colEv.getLocality().getLong1());
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
