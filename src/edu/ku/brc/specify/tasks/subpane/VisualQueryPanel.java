/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.RecordSetFactory;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.services.mapping.LatLonPoint;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.tasks.BaseTreeTask;
import edu.ku.brc.specify.tasks.DataEntryTask;
import edu.ku.brc.specify.tasks.RecordSetTask;
import edu.ku.brc.specify.ui.ClickAndGoSelectListener;
import edu.ku.brc.specify.ui.WorldWindPanel;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
//ZZZ import gov.nasa.worldwind.examples.LineBuilder;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.MarkerLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Polyline;
//ZZZ import gov.nasa.worldwind.util.GeometryMath;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 12, 2010
 *
 */
public class VisualQueryPanel extends BaseSubPane
{
    protected static final int[] TABLE_IDS = {CollectionObject.getClassTableId(), Locality.getClassTableId(), CollectingEvent.getClassTableId(), Taxon.getClassTableId()};
    
    protected static final int MAP_WIDTH  = 500;
    protected static final int MAP_HEIGHT = 500;
    protected static final String STATUSBAR_NAME = "VisualQueryStatusBar";
    
    protected static DBTableInfo[] TABLE_INFO;
    
    protected WorldWindPanel        wwPanel;
    protected Vector<LatLonPoint>   availPoints       = new Vector<LatLonPoint>();
    protected Vector<LatLonPoint>   selectedPoints    = new Vector<LatLonPoint>();
    
    //protected Vector<Position>      containedWWPoints = new Vector<Position>();
    //protected List<Position>        polygonWWPoints   = new Vector<Position>();
    
    protected HashSet<Integer>      topIdHash         = new HashSet<Integer>();
    protected HashSet<Integer>      botIdHash         = new HashSet<Integer>();
    
    protected JLabel                topLbl;
    protected JList                 dbObjList;
    protected JList                 recSetList;
    protected JComboBox             typeCBX;
    
    protected JButton               searchBtn;
    protected JButton               startBtn;
    protected JButton               endBtn;
    protected JButton               clearAllBtn;
    protected JButton               clearSearchBtn;
    
    protected JButton               dwnBtn;
    protected JButton               upBtn;
    protected JButton               selectAllBtn;
    protected JButton               deselectAllBtn;
    
    protected JButton               rsBtn;
    protected JButton               fmBtn;
    
    // Polygon
    protected boolean               isCreatingPolygon = false;
    protected Point                 prevPoint         = null;
    
    // Map Selection
    protected Position              lastClickPos      = null;
    
 // ZZZ protected LineBuilder           lineBuilder;
    protected RenderableLayer       lineLayer;
    protected Polyline              polyline = new Polyline();
    
    // Search Data
    protected DefaultListModel           model   = null;
    protected List<LatLonPlacemarkIFace> markers = new Vector<LatLonPlacemarkIFace>();
    protected Polyline                   polygon = null;
    protected int                        totalNumRecords = 0;
    
    // For Debugging
    protected StringBuilder              polySB  = new StringBuilder();
    protected StringBuilder              boxSB   = new StringBuilder();
    protected boolean                    doDebug = false;

    /**
     * @param name
     * @param task
     */
    public VisualQueryPanel(String name, Taskable task)
    {
        super(name, task);
        
        TABLE_INFO = new DBTableInfo[TABLE_IDS.length];
        for (int i=0;i<TABLE_IDS.length;i++)
        {
            TABLE_INFO[i] = DBTableIdMgr.getInstance().getInfoById(TABLE_IDS[i]);
        }
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
                moveItems(recSetList, botIdHash, dbObjList, topIdHash);
            }
        });
        
        dwnBtn = UIHelper.createIconBtn("Green Arrow Down", "",  new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                moveItems(dbObjList, topIdHash, recSetList, botIdHash);
                
                boolean hasItems = recSetList.getModel().getSize() > 0;
                rsBtn.setEnabled(hasItems);
                fmBtn.setEnabled(hasItems);

            }
        });
        
        selectAllBtn = UIHelper.createI18NButton("SELECTALL");
        selectAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dbObjList.clearSelection();
                dbObjList.getSelectionModel().setSelectionInterval(0, dbObjList.getModel().getSize());
            }
        });
        deselectAllBtn = UIHelper.createI18NButton("DESELECTALL");
        deselectAllBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dbObjList.clearSelection();
            }
        });
        
        PanelBuilder upDwnPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g,p,10px,p,f:p:g,p,f:p:g", "p"));
        //upDwnPB.add(selectAllBtn,   cc.xy(2,1));
        upDwnPB.add(dwnBtn,         cc.xy(4,1));
        upDwnPB.add(upBtn,          cc.xy(6,1));
        //upDwnPB.add(deselectAllBtn, cc.xy(7,1));
        
        dbObjList = new JList(new DefaultListModel());
        dbObjList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
        recSetList.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //trkList.setCellRenderer(new TrkListCellRenderer());
        recSetList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                boolean isSel = recSetList.getSelectedIndex() != -1;
                dwnBtn.setEnabled(false);
                upBtn.setEnabled(isSel);
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
        //wwPanel.getWorld().addSelectListener(getWWSelectListener());
        //wwPanel.getWorld().getInputHandler().addMouseListener(getWWMouseAdapter());
        
        PanelBuilder rightPB = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));

     // ZZZ lineBuilder = new LineBuilder(wwPanel.getWorld(), lineLayer, polyline);
        rightPB.add(wwPanel, cc.xy(1, 1)); y += 2;
        
        polyline.setAntiAliasHint(Polyline.ANTIALIAS_NICEST);
        polyline.setLineWidth(1.5);
        polyline.setFollowTerrain(true);
         
        startBtn  = UIHelper.createI18NButton("Start");
        endBtn    = UIHelper.createI18NButton("End");
        searchBtn = UIHelper.createI18NButton("SEARCH");
        clearAllBtn  = UIHelper.createI18NButton("Clear All");
        clearSearchBtn  = UIHelper.createI18NButton("Clear Search");
        
        typeCBX = UIHelper.createComboBox(TABLE_INFO);
        
        PanelBuilder btnPB = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g,p,f:p:g,p,f:p:g,p,f:p:g,p,f:p:g,p,f:p:g", "p"));
        btnPB.add(typeCBX,        cc.xy(2, 1));
        btnPB.add(startBtn,       cc.xy(4, 1));
        btnPB.add(endBtn,         cc.xy(6, 1));
        btnPB.add(searchBtn,      cc.xy(8, 1));
        btnPB.add(clearSearchBtn, cc.xy(10, 1));
        btnPB.add(clearAllBtn,    cc.xy(12, 1));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,10px,f:p:g", "f:p:g, 4px, p"), this);

        pb.add(leftPB.getPanel(),  cc.xy(1, 1));
        pb.add(rightPB.getPanel(), cc.xy(3, 1));
        pb.add(btnPB.getPanel(),   cc.xyw(1, 3, 3));
        pb.setDefaultDialogBorder();
        
        startBtn.setEnabled(true);
        endBtn.setEnabled(false);
        searchBtn.setEnabled(false);
        clearAllBtn.setEnabled(false);
        clearSearchBtn.setEnabled(false);
        
        rsBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                RecordSetIFace rs = createRecordSet();
                if (rs != null)
                {
                    CommandDispatcher.dispatch(new CommandAction(RecordSetTask.RECORD_SET, "Save", null, null, rs));
                }
            }
        });
        
        fmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                RecordSetIFace rs = createRecordSet();
                if (rs != null)
                {
                    CommandDispatcher.dispatch(new CommandAction(DataEntryTask.DATA_ENTRY, DataEntryTask.EDIT_DATA, rs));
                }
            }
        });
        
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    doSearch();
                } catch (IOException e1)
                {
                    e1.printStackTrace();
                }
            }
        });
        
        startBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (true)
                {
                    //doSearch();
                    //return;
                }
                /*
                double lat = 37.24517;
                double lon = -100.99083;
                
                double[] p = new double[] {-100.90805562872109,37.24714676134192,
                                            -101.1442623355922,37.15441022126542,
                                            -100.78757107464702,37.1712467680786,
                                            -100.90570697458969,37.24401619379327,
                                            -100.90805562872109,37.24714676134192};
                ArrayList<Position> list = new ArrayList<Position>();
                for (int i=0;i<p.length;i++)
                {
                    Position pos = Position.fromDegrees(p[i+1], p[i], 0);
                    list.add(pos);
                    i++;
                }
                polygon = new Polyline(list);
                polygon.setClosed(true);
                
                Position pos = Position.fromDegrees(lat, lon, 0.0);
                System.out.println("isLocationInside: "+GeometryMath.isLocationInside(pos, polygon.getPositions()));
                */
                
                //polygonWWPoints.clear();
                isCreatingPolygon = true;
                searchBtn.setEnabled(false);
                endBtn.setEnabled(true);
                startBtn.setEnabled(false);
             // ZZZ lineBuilder.setArmed(true);
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
                clearAllBtn.setEnabled(true);
                clearSearchBtn.setEnabled(true);

                //polygonWWPoints.add(polygonWWPoints.get(0));
                //createPolyline();
                
             // ZZZ lineBuilder.setArmed(false);

            }
        });
        
        clearAllBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doClearAll(true);
            }
        });
        
        clearSearchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doClearAll(false);
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
     * @return the RecordSet of chosen items.
     */
    private RecordSetIFace createRecordSet()
    {
        RecordSetIFace   rs    = null;
        DefaultListModel model = (DefaultListModel)recSetList.getModel();
        if (model.getSize() > 0)
        {
            rs = RecordSetFactory.getInstance().createRecordSet();
            rs.setDbTableId(TABLE_IDS[typeCBX.getSelectedIndex()]);
            
            for (int i=0;i<model.getSize();i++)
            {
                LatLonPoint llp = (LatLonPoint)model.get(i);
                rs.addItem(llp.getLocId());
            }
        }
        return rs;
    }
    
    /**
     * Moves selected items from one list to the other.
     * @param srcList
     * @param srcHash
     * @param dstList
     * @param dstHash
     */
    private void moveItems(final JList srcList, final HashSet<Integer> srcHash,
                           final JList dstList, final HashSet<Integer> dstHash)
    {
        int inx = srcList.getSelectedIndex();
        if (inx > -1)
        {
            DefaultListModel srcModel = (DefaultListModel)srcList.getModel();
            DefaultListModel dstModel = (DefaultListModel)dstList.getModel();
            
            int[] indexes = srcList.getSelectedIndices();
            ArrayList<LatLonPoint> llpList = new ArrayList<LatLonPoint>(indexes.length);
            for (int selInx : indexes)
            {
                LatLonPoint llp = (LatLonPoint)srcModel.get(selInx);
                llpList.add(llp);
                
                if (!dstHash.contains(llp.getLocId()))
                {
                    dstModel.addElement(llp);
                    dstHash.add(llp.getLocId());
                }
            }
            
            for (LatLonPoint llp : llpList)
            {
                srcModel.removeElement(llp);
                srcHash.remove(llp.getLocId());
            }
        }
    }
    
    /**
     * 
     */
    private void doClearAll(final boolean doClearPolygon)
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
         // ZZZ lineBuilder = new LineBuilder(wwPanel.getWorld(), lineLayer, polyline);
            //WorldWindPanel.insertAfterPlacenames(wwPanel.getWorld(), lineLayer);
        }
        
        ((DefaultListModel)dbObjList.getModel()).removeAllElements();
        topIdHash.clear();
        
        clearAllBtn.setEnabled(false);
        clearSearchBtn.setEnabled(false);

        startBtn.setEnabled(true);
    }
    
    /**
     * 
     */
    private void doPickedNewObjType()
    {
        boolean hadItems = dbObjList.getModel().getSize() > 0;
        doClearAll(false);
        topLbl.setText(typeCBX.getSelectedItem().toString());
        
        DefaultListModel model = (DefaultListModel)recSetList.getModel();
        model.removeAllElements();
        botIdHash.clear();
        
        rsBtn.setEnabled(false);
        fmBtn.setEnabled(false);

        
        if (hadItems)
        {
            searchBtn.setEnabled(true);
            startBtn.setEnabled(false);
        }
    }
    
    /**
     * @throws IOException 
     * 
     */
    private void doSearch() throws IOException
    {
        final String CNT = "CNT";
            
        UIFieldFormatterIFace fieldFmt = null;
        if (typeCBX.getSelectedIndex() == 0)
        {
            fieldFmt = DBTableIdMgr.getFieldFormatterFor(CollectionObject.class, "catalogNumber");
        }
        
        final StringBuilder pmStr = new StringBuilder();
        final String placeMark = " <Placemark><name>%s - %d / %d</name><Point><coordinates>%8.5f, %8.5f, 5</coordinates></Point></Placemark>\n";
        
        polySB.setLength(0);
        boxSB.setLength(0);
        
        final JStatusBar              statusBar = UIRegistry.getStatusBar();
        final UIFieldFormatterIFace   fldFmt    = fieldFmt;
        SwingWorker<Integer, Integer> worker    = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                // fills pntList from polyline
                // polyline was filled via clicks on WorldWind
                totalNumRecords = BasicSQLUtils.getCountAsInt(buildSQL(true)); 
                
                availPoints.clear();
                model = (DefaultListModel)dbObjList.getModel();
                model.removeAllElements();
                topIdHash.clear();
                
                markers.clear();
                
                polygon = new Polyline(polyline.getPositions());
                polygon.setClosed(true);
                
                for (Position p : polyline.getPositions())
                {
                    polySB.append(String.format("    %8.5f, %8.5f, 20\n", p.longitude.degrees, p.latitude.degrees));
                }
                
                int maxThreshold = 1000;
                int index        = 0;
                Connection conn  = null;        
                Statement  stmt  = null;
                try
                {
                    conn = DBConnection.getInstance().createConnection();
                    stmt = conn.createStatement();

                    int currCnt = 0;
                    ResultSet rs = stmt.executeQuery(buildSQL(false));
                    while (rs.next())
                    {
                        if (currCnt < maxThreshold)
                        {
                            double lat = rs.getBigDecimal(2).doubleValue();
                            double lon = rs.getBigDecimal(3).doubleValue();
                            
                            Position pos = Position.fromDegrees(lat, lon, 0.0);
// ZZZ                            
//                            if (GeometryMath.isLocationInside(pos, polygon.getPositions()))
//                            {
//                                LatLonPoint llp = new LatLonPoint(rs.getInt(1), lat, lon);
//                                String title = rs.getString(4);
//                                if (title != null)
//                                {
//                                    title = (fldFmt != null ? fldFmt.formatToUI(title) :title).toString();
//                                } else
//                                {
//                                    title = "N/A";
//                                }
//                                llp.setTitle(title);
//                                llp.setIndex(index++);
//                                availPoints.add(llp);
//                                markers.add(llp);
//                                topIdHash.add(llp.getLocId());
//                                System.out.println(index+" / "+currCnt+" In:      "+lat+",  "+lon);
//                                pmStr.append(String.format(placeMark, "In: ",index, currCnt, lon, lat));
//                                
//                            } else
//                            {
//                                System.out.println(index+" / "+currCnt+" Tossing: "+lat+",  "+lon);
//                                pmStr.append(String.format(placeMark, "Tossing: ", index, currCnt, lon, lat));
//                            }
                        }
                        currCnt++;
                        if (currCnt % 100 == 0)
                        {
                            firePropertyChange(CNT, 0, currCnt);
                        }
                    }
                    rs.close();
                } 
                catch (SQLException ex)
                {
                    ex.printStackTrace();
                    /*UsageTracker.incrSQLUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseTreeTask.class, ex);
                    log.error("SQLException: " + ex.toString()); //$NON-NLS-1$
                    lo .error(ex.getMessage());*/
                    
                } finally
                {
                    try 
                    {
                        if (stmt != null) stmt.close();
                        if (conn != null) conn.close();
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseTreeTask.class, ex);
                        ex.printStackTrace();
                    }
                }

                return null;
            }

            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#done()
             */
            @Override
            protected void done()
            {
                super.done();
                
                if (doDebug)
                {
                    try
                    {
                        final String template  = FileUtils.readFileToString(new File("template.kml"));
                        final PrintWriter pw   = new PrintWriter(new File("debug.kml"));
    
                        String str = StringUtils.replace(template, "<!-- BOX -->", boxSB.toString());
                        str = StringUtils.replace(str, "<!-- POLYGON -->", polySB.toString());
                        str = StringUtils.replace(str, "<!-- PLACEMARKS -->", pmStr.toString());
                        pw.println(str);
                        pw.flush();
                        pw.close();
                    } catch (IOException ex) {}
                }
                
                UIRegistry.clearSimpleGlassPaneMsg();
                statusBar.setProgressDone(STATUSBAR_NAME);
                
                for (LatLonPlacemarkIFace llp : markers)
                {
                    model.addElement(llp);
                }
                
                if (markers.size() > 0)
                {
                    wwPanel.placeMarkers(markers, null);
                    searchBtn.setEnabled(false);
                    
                } else
                {
                    doClearAll(true);
                    startBtn.setEnabled(false);
                }
                clearAllBtn.setEnabled(true);
                clearSearchBtn.setEnabled(true);
            }
        };
        
        statusBar.setIndeterminate(STATUSBAR_NAME, false);
        statusBar.setProgressRange(STATUSBAR_NAME, 0, 100);
        
        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("MySQLBackupService.BACKINGUP", "XXX"), 24);
        
        worker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (CNT.equals(evt.getPropertyName())) 
                        {
                            int value    = (Integer)evt.getNewValue();
                            int progress = (int)(((double)value / (double)totalNumRecords) * 100.0);
                            glassPane.setProgress(progress);
                            statusBar.setValue(STATUSBAR_NAME, progress);
                        }
                    }
                });
        worker.execute();
    }
    
    /**
     * @param doForCount
     * @return
     */
    private String buildSQL(final boolean doForCount)
    {
        double latMin = Double.MAX_VALUE;
        double latMax = Double.MIN_VALUE;
        
        double lonMin = Double.MAX_VALUE;
        double lonMax = -1000.0;
        
        for (Position p : polyline.getPositions())
        {
            double lat = p.getLatitude().getDegrees();
            double lon = p.getLongitude().getDegrees();
            
            if (lat <= latMin)
            {
                latMin = lat;
            }
            if (lat >= latMax)
            {
                latMax = lat;
            }
            if (lon <= lonMin)
            {
                lonMin = lon;
            }
            if (lon >= lonMax)
            {
                lonMax = lon;
            }
        }
        
        StringBuilder sb = new StringBuilder();
        switch (typeCBX.getSelectedIndex())
        {
            case 0: sb.append(doColObjSearchSQL(doForCount));
                break;
                
            case 1: sb.append(getLocalitySearchSQL(doForCount));
                break;
                
            case 2: sb.append(doCollEventSearchSQL(doForCount));
                break;
            
            case 3: sb.append(doTaxonSearchSQL(doForCount));
                break;
        }
        
        System.err.println(latMin+", "+lonMin+"    "+latMax+", "+lonMax);
        
        boxSB.append(String.format("  %8.5f, %8.5f, 10\n", lonMin, latMin));
        boxSB.append(String.format("  %8.5f, %8.5f, 10\n", lonMin, latMax));
        boxSB.append(String.format("  %8.5f, %8.5f, 10\n", lonMax, latMax));
        boxSB.append(String.format("  %8.5f, %8.5f, 10\n", lonMax, latMin));
        
        String whereSQL = String.format(" Latitude1 >= %10.5f AND Latitude1 <= %10.5f AND Longitude1 >= %10.5f AND Longitude1 <= %10.5f", latMin, latMax, lonMin, lonMax);
        String sql      = String.format(sb.toString(), whereSQL);
        
        //System.err.println(sql);
        
        return sql;
    }
    
    /**
     * @param doForCount
     * @return
     */
    private String getLocalitySearchSQL(final boolean doForCount)
    {
        String fields = "LocalityID, Latitude1, Longitude1, LocalityName";
        return "SELECT " + (doForCount ? "COUNT(*)" : fields) + " FROM locality WHERE %s GROUP BY LocalityID ORDER BY LocalityName"; 
    }
    
    /**
     * @param doForCount
     * @return
     */
    private String doColObjSearchSQL(final boolean doForCount)
    {
        String fields = "co.CollectionObjectID, l.Latitude1, l.Longitude1, co.CatalogNumber";
        return "SELECT " + (doForCount ? "COUNT(*)" : fields) + " FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " + 
               "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID WHERE %s ORDER BY CatalogNumber";
    }
    
    /**
     * @param doForCount
     * @return
     */
    private String doCollEventSearchSQL(final boolean doForCount)
    {
        String fields = "ce.CollectingEventID, l.Latitude1, l.Longitude1, ce.StartDate";
        return "SELECT " + (doForCount ? "COUNT(*)" : fields) + " FROM locality l " +
                "INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID WHERE %s GROUP BY ce.CollectingEventID ORDER BY StartDate";
    }
    
    /**
     * @param doForCount
     * @return
     */
    private String doTaxonSearchSQL(final boolean doForCount)
    {
        String fields = "t.TaxonID, l.Latitude1, l.Longitude1, t.FullName";
        return "SELECT " + (doForCount ? "COUNT(*)" : fields) +
                "FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
                "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID " +
                "INNER JOIN determination d ON co.CollectionObjectID = d.CollectionObjectID " +
                "INNER JOIN taxon t ON d.TaxonID = t.TaxonID WHERE d.IsCurrent = TRUE AND %s GROUP BY t.TaxonID ORDER BY FullName";
    }
    
    /**
     * @return
     */
    /*private SelectListener getWWSelectListener()
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
    }*/
    
    /**
     * @return
     */
    /*private MouseAdapter getWWMouseAdapter()
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
    }*/
    
    /**
     * Converts a list of LatLonPoints to a List of WW Positions.
     * @param pnts LatLonPoint list
     * @return list of Positions
     */
    /*protected List<Position> createPolygonFromPoints(final Vector<LatLonPoint> pnts)
    {
        ArrayList<Position> polygonList = new ArrayList<Position>();
        for (LatLonPoint p : pnts)
        {
            Position pos = Position.fromDegrees(p.getLatitude(), p.getLongitude(), 0.0);
            polygonList.add(pos);
        }
        
        return polygonList;
    }*/
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#shutdown()
     */
    @Override
    public void shutdown()
    {
        super.shutdown();
    }

}
