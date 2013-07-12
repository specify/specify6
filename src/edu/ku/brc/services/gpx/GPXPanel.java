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
package edu.ku.brc.services.gpx;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.services.gpx.io.GpxType;
import edu.ku.brc.services.gpx.io.RteType;
import edu.ku.brc.services.gpx.io.TrkType;
import edu.ku.brc.services.gpx.io.TrksegType;
import edu.ku.brc.services.gpx.io.WptType;
import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.services.mapping.LatLonPoint;
import edu.ku.brc.specify.ui.WorldWindPanel;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Polyline;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 8, 2009
 *
 */
public class GPXPanel extends JPanel
{
    protected JList          wpList;   // Way Points
    protected JList          trkList;  // Tracks
    protected JList          rteList;  // Routes
    protected WorldWindPanel wwPanel;
    
    protected GPXDataSet     gpxDS;
    protected GpxType        gpxType;
    
    /**
     * 
     */
    public GPXPanel()
    {
        super();
        
        gpxDS   = new GPXDataSet();
        gpxType = gpxDS.load("/Users/rods/workspace/GPX/blue_hills.gpx");
        
        createUI();
    }
    
    /**
     * 
     */
    public GPXPanel(final File file)
    {
        super();
        
        gpxDS   = new GPXDataSet();
        gpxType = gpxDS.load(file);
        
        createUI();
    }
    
    /**
     * 
     */
    protected void createUI()
    {
        wpList = new JList(new Vector<WptType>(gpxType.getWpt()));
        wpList.setCellRenderer(new WPListCellRenderer());
        
        trkList = new JList(new Vector<TrkType>(gpxType.getTrk()));
        trkList.setCellRenderer(new TrkListCellRenderer());
        
        rteList = new JList(new Vector<RteType>(gpxType.getRte()));
        rteList.setCellRenderer(new RteListCellRenderer());
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,10px,f:p:g", "p,2px,f:p:g,10px,p,2px,f:p:g,10px,p,2px,f:p:g"), this);
        
        int y = 1;
        pb.add(UIHelper.createLabel("Way Points"), cc.xy(1,y)); y += 2; // I18N
        pb.add(UIHelper.createScrollPane(wpList),  cc.xy(1, y)); y += 2;
        
        pb.add(UIHelper.createLabel("Tracks"),     cc.xy(1,y)); y += 2;
        pb.add(UIHelper.createScrollPane(trkList), cc.xy(1, y)); y += 2;
        
        pb.add(UIHelper.createLabel("Routes"),     cc.xy(1,y)); y += 2;
        pb.add(UIHelper.createScrollPane(rteList), cc.xy(1, y)); y += 2;
        
        wwPanel = new WorldWindPanel();
        pb.add(wwPanel, cc.xywh(3, 1, 1, 11)); y += 2;
        
        pb.setDefaultDialogBorder();
        
        wpList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    selectedWayPoint();
                }
            }
        });
        
        trkList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    selectedTrack();
                }
            }
        });
        
        rteList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    selectedRoute();
                }
            }
        });
    }
    
    /**
     * 
     */
    protected void selectedWayPoint()
    {
        WptType wpt = (WptType)wpList.getSelectedValue();
        if (wpt != null)
        {
            ArrayList<LatLonPlacemarkIFace> pnts = new ArrayList<LatLonPlacemarkIFace>(gpxType.getWpt().size());
            /*for (WptType wp : gpxType.getWpt())
            {
                pnts.add(new LatLonPoint(wp.getLat().doubleValue(), wp.getLon().doubleValue()));
            }*/
            pnts.add(new LatLonPoint(wpt.getLat().doubleValue(), wpt.getLon().doubleValue(), wpt.getName()));
            wwPanel.placeMarkers(pnts, true, false, 0);
        }
    }
    
    /**
     * 
     */
    protected void selectedTrack()
    {
        TrkType trkType = (TrkType)trkList.getSelectedValue();
        if (trkType != null)
        {
            ArrayList<LatLonPlacemarkIFace> pnts      = new ArrayList<LatLonPlacemarkIFace>(gpxType.getWpt().size());
            ArrayList<Position>         positions = new ArrayList<Position>(gpxType.getWpt().size());
            for (TrksegType trkSeg : trkType.getTrkseg())
            {
                for (WptType wp : trkSeg.getTrkpt())
                {
                    Position pos = Position.fromDegrees(wp.getLat().doubleValue(), wp.getLon().doubleValue(), 0.0);
                    positions.add(pos);
                    pnts.add(new LatLonPoint(wp.getLat().doubleValue(), wp.getLon().doubleValue(), wp.getName()));
                }
            }
            
            wwPanel.reset();
            
            //wwPanel.placeMarkers(pnts, false, 0);
            
            Polyline polyLine = new Polyline(positions);
            polyLine.setFollowTerrain(true);
            polyLine.setNumSubsegments(20);
            polyLine.setLineWidth(1.25d);
            polyLine.setAntiAliasHint(Polyline.ANTIALIAS_NICEST);
            wwPanel.addPolyline(polyLine);
            
            LatLonPlacemarkIFace  p     = pnts.get(0);
            Pair<Double, Double> latLon = p.getLatLon();
            wwPanel.flyTo(LatLon.fromDegrees(latLon.first, latLon.second));
            
        }
    }
    
    protected void selectedRoute()
    {
        
    }
    
    //-----------------------------------------------------------------------
    class WPListCellRenderer extends DefaultListCellRenderer
    {
        public WPListCellRenderer()
        {
            super();
        }

        /* (non-Javadoc)
         * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setText(((WptType)value).getName());
            return lbl;
        }
        
    }
    
    class TrkListCellRenderer extends DefaultListCellRenderer
    {
        public TrkListCellRenderer()
        {
            super();
        }

        /* (non-Javadoc)
         * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setText(((TrkType)value).getName());
            return lbl;
        }
        
    }
    
    class RteListCellRenderer extends DefaultListCellRenderer
    {
        public RteListCellRenderer()
        {
            super();
        }

        /* (non-Javadoc)
         * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setText(((RteType)value).getName());
            return lbl;
        }
        
    }
    
    /**
     * @return the dialog or null if no file was selected
     */
    public static CustomDialog getDlgInstance()
    {
        FileDialog fileDlg = new FileDialog((Frame)UIRegistry.getTopWindow(), "", FileDialog.LOAD);
        
        fileDlg.setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.toLowerCase().endsWith(".gpx");
            }
        });
        fileDlg.setVisible(true);
        
        String fileName = fileDlg.getFile();
        if (fileName != null)
        {
            GPXPanel panel = new GPXPanel(new File(fileDlg.getDirectory() + File.separator + fileName));
            CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), "GPX Points", true, panel);
            dlg.createUI();
            dlg.pack();
            dlg.setSize(950, 700);
            
            return dlg;
        }
        return null;
    }
}
