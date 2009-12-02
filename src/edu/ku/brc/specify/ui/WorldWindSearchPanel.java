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
package edu.ku.brc.specify.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.services.mapping.LatLonPoint;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.examples.LineBuilder;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.util.GeometryMath;
import gov.nasa.worldwind.view.OrbitView;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 26, 2009
 *
 */
public class WorldWindSearchPanel extends JPanel
{
    protected static final int MAP_WIDTH  = 500;
    protected static final int MAP_HEIGHT = 500;
    
    protected WorldWindPanel        wwPanel;
    protected Vector<LatLonPoint>   availPoints       = new Vector<LatLonPoint>();
    protected Vector<LatLonPoint>   selectedPoints    = new Vector<LatLonPoint>();
    
    protected Vector<Position>      containedWWPoints = new Vector<Position>();
    protected List<Position>        polygonWWPoints   = new Vector<Position>();
    
    protected JLabel                topLbl;
    protected JList                 dbObjList;
    protected JList                 recSetList;
    protected JComboBox             typeCBX;
    
    protected JButton               searchBtn;
    protected JButton               startBtn;
    protected JButton               endBtn;
    protected JButton               clearBtn;
    
    protected JButton               dwnBtn;
    protected JButton               upBtn;
    
    protected JButton               rsBtn;
    protected JButton               fmBtn;
    
    // Polygon
    protected boolean               isCreatingPolygon = false;
    protected Point                 prevPoint         = null;
    
    // Map Selection
    protected Position              lastClickPos      = null;
    
    protected LineBuilder           lineBuilder;
    protected RenderableLayer       lineLayer;
    protected Polyline              polyline = new Polyline();
    
    /**
     * 
     */
    public WorldWindSearchPanel()
    {
        super();

        createUI();
    }

    /**
     * Creates the UI.
     */
    protected void createUI()
    {
        CellConstraints cc = new CellConstraints();

        lineLayer = new RenderableLayer();
        
        upBtn  = UIHelper.createIconBtn("Green Arrow Up", "", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int inx = recSetList.getSelectedIndex();
                if (inx > -1)
                {
                    DefaultListModel model = (DefaultListModel)recSetList.getModel();
                    Object obj = model.get(inx);
                    model.remove(inx);
                    
                    ((DefaultListModel)dbObjList.getModel()).addElement(obj);
                }
            }
        });
        dwnBtn = UIHelper.createIconBtn("Green Arrow Down", "",  new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int inx = dbObjList.getSelectedIndex();
                if (inx > -1)
                {
                    DefaultListModel model = (DefaultListModel)dbObjList.getModel();
                    Object obj = model.get(inx);
                    model.remove(inx);
                    
                    ((DefaultListModel)recSetList.getModel()).addElement(obj);
                }
            }
        });
        PanelBuilder upDwnPB = new PanelBuilder(new FormLayout("f:p:g,p,10px,p,f:p:g", "p"));
        upDwnPB.add(dwnBtn, cc.xy(2,1));
        upDwnPB.add(upBtn,  cc.xy(4,1));
        
        dbObjList = new JList(new DefaultListModel());
        //wpList.setCellRenderer(new WPListCellRenderer());
        dbObjList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                upBtn.setEnabled(false);
                dwnBtn.setEnabled(dbObjList.getSelectedIndex() != -1);
            }
        });
        
        recSetList = new JList(new DefaultListModel());
        //trkList.setCellRenderer(new TrkListCellRenderer());
        recSetList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                boolean isSel = recSetList.getSelectedIndex() != -1;
                dwnBtn.setEnabled(false);
                upBtn.setEnabled(isSel);
                rsBtn.setEnabled(isSel);
                fmBtn.setEnabled(isSel);
                
            }
        });
        
        rsBtn = UIHelper.createIconBtn("Record_Set", IconManager.IconSize.Std24, "", null);
        fmBtn = UIHelper.createIconBtn("FormEdit", IconManager.IconSize.Std24, "", null);
        PanelBuilder toolsBtnPB = new PanelBuilder(new FormLayout("f:p:g,2px,p,2px,p", "p"));
        toolsBtnPB.add(fmBtn, cc.xy(3, 1));
        toolsBtnPB.add(rsBtn, cc.xy(5, 1));

        PanelBuilder leftPB = new PanelBuilder(new FormLayout("min(p;250px),10px,f:p:g", "p,2px,f:p:g, 4px,p,4px, p,2px,f:p:g, 2px,p"));
        
        int y = 1;
        leftPB.add(topLbl = UIHelper.createLabel("Available Points"), cc.xy(1, y)); y += 2; // I18N
        leftPB.add(UIHelper.createScrollPane(dbObjList, true),        cc.xy(1, y)); y += 2;

        leftPB.add(upDwnPB.getPanel(),                          cc.xy(1, y)); y += 2;
        
        leftPB.add(UIHelper.createLabel("Selected Objects"),    cc.xy(1,y)); y += 2;
        leftPB.add(UIHelper.createScrollPane(recSetList, true), cc.xy(1, y)); y += 2;
        
        leftPB.add(toolsBtnPB.getPanel(),                       cc.xy(1, y)); y += 2;

        wwPanel = new WorldWindPanel();
        wwPanel.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
        wwPanel.getWorld().addSelectListener(new ClickAndGoSelectListener(wwPanel.getWorld(), MarkerLayer.class));
        wwPanel.getWorld().addSelectListener(getWWSelectListener());
        wwPanel.getWorld().getInputHandler().addMouseListener(getWWMouseAdapter());
        
        PanelBuilder rightPB = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));

        lineBuilder = new LineBuilder(wwPanel.getWorld(), lineLayer, polyline);
        rightPB.add(wwPanel, cc.xy(1, 1)); y += 2;
        
        polyline.setAntiAliasHint(Polyline.ANTIALIAS_NICEST);
        polyline.setLineWidth(1.5);
        polyline.setFollowTerrain(true);
         
        startBtn  = UIHelper.createI18NButton("Start");
        endBtn    = UIHelper.createI18NButton("End");
        searchBtn = UIHelper.createI18NButton("SEARCH");
        clearBtn  = UIHelper.createI18NButton("Clear");
        
        typeCBX = UIHelper.createComboBox(new String[] {"Collection Object", "Localities", "Collecting Event", "Taxon"});
        
        PanelBuilder btnPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g,p,f:p:g,p,f:p:g,p,f:p:g,p,f:p:g", "p"));
        btnPB.add(typeCBX,       cc.xy(2, 1));
        btnPB.add(startBtn,      cc.xy(4, 1));
        btnPB.add(endBtn,        cc.xy(6, 1));
        btnPB.add(searchBtn,     cc.xy(8, 1));
        btnPB.add(clearBtn,      cc.xy(10, 1));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,10px,f:p:g", "f:p:g, 4px, p"), this);

        pb.add(leftPB.getPanel(),  cc.xy(1, 1));
        pb.add(rightPB.getPanel(), cc.xy(3, 1));
        pb.add(btnPB.getPanel(),   cc.xyw(1, 3, 3));
        pb.setDefaultDialogBorder();
        
        startBtn.setEnabled(true);
        endBtn.setEnabled(false);
        searchBtn.setEnabled(false);
        clearBtn.setEnabled(false);
        
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doSearch();
            }
        });
        
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                polygonWWPoints.clear();
                isCreatingPolygon = true;
                searchBtn.setEnabled(false);
                endBtn.setEnabled(true);
                startBtn.setEnabled(false);
                lineBuilder.setArmed(true);
            }
        });
        
        endBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                polyline.setClosed(true);
                isCreatingPolygon = false;
                searchBtn.setEnabled(true);
                endBtn.setEnabled(false);
                startBtn.setEnabled(false);
                searchBtn.setEnabled(true);
                clearBtn.setEnabled(true);
                
                //polygonWWPoints.add(polygonWWPoints.get(0));
                //createPolyline();
                
                lineBuilder.setArmed(false);

            }
        });
        
        clearBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doClear(true);
            }
        });
        
        typeCBX.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doPickedNewObjType();
            }
        });
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                typeCBX.setSelectedIndex(0);
            }
        });
    }
    
    /**
     * 
     */
    private void doClear(final boolean doClearPolygon)
    {
        if (doClearPolygon)
        {
            LayerList layers = wwPanel.getWorld().getModel().getLayers();
            int inx = layers.indexOf(lineLayer);
            layers.remove(inx);
        }
        
        wwPanel.getMarkerLayer().clearList();
        wwPanel.getWorld().redrawNow();
        wwPanel.reset();
        
        if (doClearPolygon)
        {
            polyline = new Polyline();
            polyline.setAntiAliasHint(Polyline.ANTIALIAS_NICEST);
            polyline.setLineWidth(1.5);
            polyline.setFollowTerrain(true);
            
            lineLayer   = new RenderableLayer();
            lineBuilder = new LineBuilder(wwPanel.getWorld(), lineLayer, polyline);
            //WorldWindPanel.insertAfterPlacenames(wwPanel.getWorld(), lineLayer);
        }
        
        ((DefaultListModel)dbObjList.getModel()).removeAllElements();
        
        clearBtn.setEnabled(false);
        startBtn.setEnabled(true);
    }
    
    
    /**
     * 
     */
    private void doPickedNewObjType()
    {
        boolean hadItems = dbObjList.getModel().getSize() > 0;
        doClear(false);
        topLbl.setText(typeCBX.getSelectedItem().toString());
        
        if (hadItems)
        {
            searchBtn.setEnabled(true);
            startBtn.setEnabled(false);
        }
    }
    
    /**
     * 
     */
    private void doSearch()
    {
        double topMin = Double.MAX_VALUE;
        double topMax = Double.MIN_VALUE;
        
        double botMin = Double.MAX_VALUE;
        double botMax = -1000.0;
        
        ArrayList<Position> pntList = new ArrayList<Position>();
        for (Position p : polyline.getPositions())
        {
            double lat = p.getLatitude().getDegrees();
            double lon = p.getLongitude().getDegrees();
            
            if (lat <= topMin)
            {
                topMin = lat;
            }
            if (lat >= topMax)
            {
                topMax = lat;
            }
            if (lon <= botMin)
            {
                botMin = lon;
            }
            if (lon >= botMax)
            {
                botMax = lon;
            }
            pntList.add(p);
        }
        StringBuilder sb = new StringBuilder();
        switch (typeCBX.getSelectedIndex())
        {
            case 0: sb.append(doColObjSearchSQL());
                break;
                
            case 1: sb.append(getLocalitySearchSQL());
                break;
                
            case 2: sb.append(doCollEventSearchSQL());
                break;
            
            case 3: sb.append(doTaxonSearchSQL());
                break;
        }
        
        String whereSQL = " Latitude1 >= %10.5f AND Latitude1 <= %10.5f AND Longitude1 >= %10.5f AND Longitude1 <= %10.5f";
        sb.append(String.format(whereSQL, topMin, topMax, botMin, botMax));
        System.err.println(sb.toString());
        
        availPoints.clear();
        DefaultListModel model = (DefaultListModel)dbObjList.getModel();
        model.removeAllElements();
        
        List<LatLonPlacemarkIFace> markers = new Vector<LatLonPlacemarkIFace>();
        
        pntList.add(pntList.get(0));
        Polyline polygon = new Polyline(pntList);
        polygon.setClosed(true);
        
        int index = 0;
        Vector<Object[]> pnts = BasicSQLUtils.query(sb.toString());
        for (Object[] row : pnts)
        {
            double lat = ((BigDecimal)row[1]).doubleValue();
            double lon = ((BigDecimal)row[2]).doubleValue();
            
            Position pos = Position.fromDegrees(lat, lon, 0.0);
            if (GeometryMath.isLocationInside(pos, polygon.getPositions()))
            {
                LatLonPoint llp = new LatLonPoint((Integer)row[0], lat, lon);
                llp.setTitle(row[3] != null ? row[3].toString() : "N/A");
                llp.setIndex(index++);
                availPoints.add(llp);
                markers.add(llp);
                model.addElement(llp);
            }
        }
        
        if (markers.size() > 0)
        {
            wwPanel.placeMarkers(markers, null);
            clearBtn.setEnabled(true);
            searchBtn.setEnabled(false);
            
        } else
        {
            doClear(true);
            clearBtn.setEnabled(true);
            startBtn.setEnabled(false);
        }
    }
    
    /**
     * @return
     */
    private String getLocalitySearchSQL()
    {
        return "SELECT LocalityID, Latitude1, Longitude1, LocalityName FROM locality WHERE "; 
    }
    
    /**
     * @return
     */
    private String doColObjSearchSQL()
    {
        return "SELECT co.CollectionObjectID, l.Latitude1, l.Longitude1, co.CatalogNumber FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " + 
               "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID WHERE ";
    }
    
    /**
     * @return
     */
    private String doCollEventSearchSQL()
    {
        return "SELECT ce.CollectingEventID, l.Latitude1, l.Longitude1, ce.StartDate FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID WHERE ";
    }
    
    /**
     * @return
     */
    private String doTaxonSearchSQL()
    {
        return "SELECT t.TaxonID, l.Latitude1, l.Longitude1, t.FullName " +
                "FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
                "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID " +
                "INNER JOIN determination d ON co.CollectionObjectID = d.CollectionObjectID " +
                "INNER JOIN taxon t ON d.TaxonID = t.TaxonID WHERE d.IsCurrent = TRUE AND ";
    }
    
    /**
     * @return
     */
    private SelectListener getWWSelectListener()
    {
        return new SelectListener() 
        {
            @Override
            public void selected(SelectEvent event)
            {
                if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
                {
                    // This is a left click
                    if (event.hasObjects() && event.getTopPickedObject().hasPosition())
                    {
                        lastClickPos = wwPanel.getWorld().getCurrentPosition();
                        
                        // There is a picked object with a position
                        if (wwPanel.getWorld().getView() instanceof OrbitView)
                        {
                            if (event.getTopObject().getClass().equals(BasicMarker.class))
                            {
                                int inx = wwPanel.getMarkers().indexOf(event.getTopObject());
                                if (inx > -1)
                                {
                                    //resultsTable.setRowSelectionInterval(inx, inx);
                                }
                            } else if (event.getTopObject().getClass().equals(GlobeAnnotation.class))
                            {
                                int inx = wwPanel.getAnnotations().indexOf(event.getTopObject());
                                if (inx > -1)
                                {
                                    //resultsTable.setRowSelectionInterval(inx, inx);
                                }
                            }
                        }
                    }
                }
            }
        };
    }
    
    /**
     * @return
     */
    private MouseAdapter getWWMouseAdapter()
    {
        return new MouseAdapter()
        {
            @Override
            public void mouseClicked(final MouseEvent e)
            {
                super.mouseClicked(e);
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        if (!lineBuilder.isArmed())
                        {
                            Position pos = wwPanel.getWorld().getCurrentPosition();
                            if (!pos.equals(lastClickPos))
                            {
                                
                            } 
                        }
                    }
                });
            }
        };
    }
    
    /**
     * Converts a list of LatLonPoints to a List of WW Positions.
     * @param pnts LatLonPoint list
     * @return list of Positions
     */
    protected List<Position> createPolygonFromPoints(final Vector<LatLonPoint> pnts)
    {
        ArrayList<Position> polygonList = new ArrayList<Position>();
        for (LatLonPoint p : pnts)
        {
            Position pos = Position.fromDegrees(p.getLatitude(), p.getLongitude(), 0.0);
            polygonList.add(pos);
        }
        
        return polygonList;
    }
    
    /**
     * 
     */
    /*protected void createContainedPoints()
    {
        List<LatLonPlacemarkIFace> markers = new Vector<LatLonPlacemarkIFace>();
        for (LatLonPoint p : availPoints)
        {
            Position pos = Position.fromDegrees(p.getLatitude(), p.getLongitude(), 0.0);
            if (GeometryMath.isLocationInside(pos, polyline.getPositions()))
            {
                markers.add(p);
            }
        }
        wwPanel.placeMarkers(markers, null);
    }*/
    
    //------------------------------------------------------------------------------------
    //-- static methods
    //------------------------------------------------------------------------------------
    
    /**
     * @return
     */
    public static CustomDialog getDlg()
    {
        WorldWindSearchPanel panel = new WorldWindSearchPanel();
        
        CustomDialog dlg = new CustomDialog(null, "", true, panel);
        dlg.setVisible(true);
        
        return dlg;
    }
}
