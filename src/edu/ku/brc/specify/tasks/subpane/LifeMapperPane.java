/* Copyright (C) 2011, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.ui.WorldWindPanel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ImageDisplay;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
import edu.ku.brc.util.Pair;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 12, 2011
 *
 */
public class LifeMapperPane extends BaseSubPane
{
    protected static final int MAP_WIDTH  = 500;
    protected static final int MAP_HEIGHT = 500;
    protected static boolean   IS_OFFLINE = false; 
    
    protected WorldWindPanel        wwPanel;
    protected boolean               doResetWWPanel = true;
    protected JButton               searchBtn;
    
    // Search Data
    protected DefaultListModel           model   = null;
    protected List<LatLonPlacemarkIFace> markers = new Vector<LatLonPlacemarkIFace>();
    protected Polyline                   polygon = null;
    protected int                        totalNumRecords = 0;
    
    // For Debugging
    protected JButton          searchSciNameBtn;
    protected JTextField       searchText;
    protected String           occurSet = null;
    protected JList            list;
    protected DefaultListModel listModel = new DefaultListModel();
    
    protected ArrayList<String> occurList = new ArrayList<String>();
    protected JButton           searchOccurBtn;
    protected ImageDisplay      imgDisplay;
    protected ImageIcon         markerImg;
    protected ArrayList<LatLonPlacemarkIFace>  points = new ArrayList<LatLonPlacemarkIFace>();


    /**
     * @param name
     * @param task
     */
    public LifeMapperPane(String name, Taskable task)
    {
        super(name, task);
        
        markerImg = IconManager.getIcon("RedDot6x6");
        createUI();
    }

    /**
     * Creates the UI.
     */
    protected void createUI()
    {
        searchText       = UIHelper.createTextField(25);
        searchSciNameBtn = UIHelper.createButton("Search");
        list             = new JList(listModel);
        imgDisplay       = new ImageDisplay(450, 225, false, true);
        
        wwPanel = new WorldWindPanel();
        wwPanel.setPreferredSize(new Dimension(MAP_WIDTH, 450));
        wwPanel.setZoomInMeters(3500000.0);
        
        imgDisplay.setDoShowText(false);
        
        searchOccurBtn = UIHelper.createButton("Search");
        
        //searchText.setText("Buteogallus anthracinus anthracinus");
        searchText.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    searchSciNameBtn.doClick();
                }
            }
        });
        
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder pb2 = new PanelBuilder(new FormLayout("MAX(p;300px),2px,f:p:g", "f:p:g"));
        pb2.add(UIHelper.createScrollPane(list), cc.xy(1,1));
        //pb2.add(searchOccurBtn,                  cc.xy(3,1));
        pb2.add(imgDisplay,         cc.xy(3,1));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,2px,p", "p,4px,p,8px,f:p:g"), this);
        pb.add(searchText,         cc.xy(1,1));
        pb.add(searchSciNameBtn,   cc.xy(3,1));
        pb.add(pb2.getPanel(),     cc.xyw(1,3,3));
        
        //JScrollPane sp = new JScrollPane(wwPanel);
        pb.add(wwPanel,                 cc.xyw(1,5,3));
        
        list.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>()
                    {
                        @Override
                        protected Boolean doInBackground() throws Exception
                        {
                            if (doResetWWPanel)
                            {
                                wwPanel.reset();
                            }
                            doSearchOccur();
                            return null;
                        }
                        @Override
                        protected void done()
                        {
                            imgDisplay.repaint();
                        }
                    };
                    worker.execute();
                }
            }
        });
        
        searchOccurBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        doSearchOccur();
                    }
                });
                
            }
        });
        
        searchSciNameBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doSearchGenusSpecies();
            }
        });
    }
    
    /**
     * 
     */
    private void doSearchGenusSpecies()
    {
        doResetWWPanel = true;
        doSearchGenusSpecies(searchText.getText());
    }
    
    /**
     * 
     */
    public void doSearchGenusSpecies(final String searchStr)
    {
        listModel.clear();
        occurList.clear();
        
        if (StringUtils.isNotEmpty(searchStr))
        {
            doSearchGenusSpecies(searchStr, new LMSearchCallbackListener()
            {
                @Override
                public void noItems()
                {
                    
                }
                
                /* (non-Javadoc)
                 * @see edu.ku.brc.specify.tasks.subpane.LifeMapperPane.LMSearchCallbackListener#itemsFound(java.util.List)
                 */
                @Override
                public void itemsFound(List<OccurrenceSetIFace> items)
                {
                    for (OccurrenceSetIFace item : items)
                    {
                        listModel.addElement(item.getTitle());
                        occurList.add(item.getOccurrenceId());
                    }
                    if (occurList.size() == 1)
                    {
                        list.setSelectedIndex(0);
                    }
                }
            });
        }
    }
    
    /**
     * @param pStmt
     * @param ceID
     * @param pmList
     * @throws SQLException
     */
    private void addMarkerFromCE(final PreparedStatement pStmt, 
                                 final int ceID,
                                 final ArrayList<LatLonPlacemarkIFace> pmList) throws SQLException
    {
        pStmt.setInt(1, ceID);
        ResultSet rs = pStmt.executeQuery();
        if  (rs.next())
        {
            LatLonPlacemark llp = new LatLonPlacemark(rs.getDouble(1), rs.getDouble(2));
            pmList.add(llp);
        }
        rs.close();
    }
    
    /**
     * 
     */
    public void resetWWPanel()
    {
        if (wwPanel != null)
        {
            wwPanel.reset();
        }
    }
    
    /**
     * @param doResetWWPanel the doResetWWPanel to set
     */
    public void setDoResetWWPanel(boolean doResetWWPanel)
    {
        this.doResetWWPanel = doResetWWPanel;
    }

    /**
     * @param recSet
     */
    public void addLocalData(final RecordSet recSet)
    {
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        boolean    isEmbedded = collection.getIsEmbeddedCollectingEvent();
        
        ArrayList<LatLonPlacemarkIFace> coPoints = new ArrayList<LatLonPlacemarkIFace>();
        
        Connection conn  = null;        
        PreparedStatement coStmt = null;
        PreparedStatement ceStmt = null;

        try
        {
            conn   = DBConnection.getInstance().createConnection();
            coStmt = conn.prepareStatement("SELECT CollectingEventID FROM collectionobject WHERE CollectionObjectID = ?");
            ceStmt = conn.prepareStatement("SELECT Latitude1, Longitude1 FROM locality l INNER JOIN collectingevent ce ON ce.LocalityID = l.LocalityID " +
            		                       "WHERE CollectingEventID = ? And Latitude1 IS NOT NULL AND Longitude1 IS NOT NULL");

            HashSet<Integer> ceSet = new HashSet<Integer>();
            
            //int currCnt = 0;
            for (RecordSetItemIFace rsi : recSet.getItems())
            {
                coStmt.setInt(1, rsi.getRecordId());
                ResultSet rs = coStmt.executeQuery();
                if  (rs.next())
                {
                    if (isEmbedded)
                    {
                        addMarkerFromCE(ceStmt, rs.getInt(1), coPoints);
                    } else
                    {
                        ceSet.add(rs.getInt(1));
                    }
                }
                rs.close();
                
                if (!isEmbedded)
                {
                    for (Integer id : ceSet)
                    {
                        addMarkerFromCE(ceStmt, id, coPoints);
                    }
                }
            }
            
            if (coPoints.size() > 0)
            { 
                //wwPanel.reset();
                BasicMarkerAttributes bmAttrs = new BasicMarkerAttributes(Material.GREEN, BasicMarkerShape.CONE, 1d, 3, 3);
                wwPanel.placeMarkers(coPoints, 0, bmAttrs, false);
            }
        } 
        catch (SQLException ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try 
            {
                if (ceStmt != null) ceStmt.close();
                if (coStmt != null) coStmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {}
        }
    }
    
    /**
     * @param searchStr
     * @param cbListener
     */
    private void doSearchGenusSpecies(final String searchStr, 
                                      final LMSearchCallbackListener cbListener)
    {    
        if (IS_OFFLINE)
        {
            ArrayList<LifeMapperPane.OccurrenceSetIFace> items = new ArrayList<LifeMapperPane.OccurrenceSetIFace>();
            items.add(new GenusSpeciesDataItem(String.format("%s (%s)", searchStr, "1"), "1"));
            cbListener.itemsFound(items); 
            return;
        }
        
        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("LifeMapperTask.PROCESSING"), 24);
        glassPane.setTextYPos((int)((double)getSize().height * 0.25));

        SwingWorker<String, String> worker = new SwingWorker<String, String>()
        {
            @Override
            protected String doInBackground() throws Exception
            {
                HttpClient httpClient = new HttpClient();
                httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
                httpClient.getParams().setParameter("http.socket.timeout", 15000); 
                
                String genusSpecies = StringUtils.replace(searchStr, " ", "%20");
                String url = "http://www.lifemapper.org/hint/species/"+genusSpecies;
                //System.out.println(url);
                
                PostMethod postMethod = new PostMethod(url);
                try
                {
                    httpClient.executeMethod(postMethod);
                    return postMethod.getResponseBodyAsString();
                }
                catch (java.net.UnknownHostException uex)
                {
                    //log.error(uex.getMessage());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                boolean isError = true;
                ArrayList<OccurrenceSetIFace> items = null;
                
                try
                {
                    String responseString = get();
                    
                    if (StringUtils.isNotEmpty(responseString))
                    {
                        items = new ArrayList<LifeMapperPane.OccurrenceSetIFace>();
                        String[] lines = StringUtils.split(responseString, '\n');
                        for (String line : lines)
                        {
                            String[] fields = StringUtils.split(line, '\t');
                            if (fields != null && fields.length == 3)
                            {
                                items.add(new GenusSpeciesDataItem(String.format("%s (%s)", fields[0], fields[2]), fields[1]));
                            }
                        }
                        isError = false;
                        //System.err.println(responseString);
                    }
                    
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
                
                if (isError)
                {
                    showErrorMsg(glassPane);
                    cbListener.noItems();
                    
                } else
                {
                    UIRegistry.clearSimpleGlassPaneMsg();
                    cbListener.itemsFound(items);
                }
            }
        };
        worker.execute();
    }
    
    /**
     * @param glassPane
     */
    protected void showErrorMsg(final SimpleGlassPane glassPane)
    {
        glassPane.setTextColor(Color.RED);
        glassPane.setText(getLocalizedMessage("LifeMapperTask.PROC_ERR"));
        
        SwingUtilities.invokeLater(new Runnable()
        {
            
            @Override
            public void run()
            {
                try { Thread.sleep(2000); } catch (Exception ex){}
                UIRegistry.clearSimpleGlassPaneMsg();
            }
        });
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    private void doSearchOccur()
    {
        occurSet = occurList.get(list.getSelectedIndex());
        if (StringUtils.isNotEmpty(occurSet))
        {
            doSearchOccur(occurSet);
        }
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public void doSearchOccur(final String occurrenceId)
    {
        points.clear();
        
        if (IS_OFFLINE)
        {
            LatLonPlacemark plcMark = new LatLonPlacemark(-93.0, 38.0);
            points.add(plcMark);
            
            wwPanel.reset();
            if (points.size() > 0)
            {
                wwPanel.placeMarkers(points, 0);
            }
        }

        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("LifeMapperTask.PROCESSING"), 24);
        glassPane.setTextYPos((int)((double)getSize().height * 0.25));
        
        // check the website for the info about the latest version
        final HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        httpClient.getParams().setParameter("http.socket.timeout", 15000); 
        
        if (list.getSelectedIndex() < 0)
        {
            return;
        }
        
        final String url = String.format("http://www.lifemapper.org/services/occurrences/%s/json?fillPoints=true", occurrenceId);
        System.out.println(url);
        
        SwingWorker<String, String> worker = new SwingWorker<String, String>()
        {
            @Override
            protected String doInBackground() throws Exception
            {
                GetMethod  getMethod  = new GetMethod(url);
                
                try
                {
                    httpClient.executeMethod(getMethod);
                    
                    // get the server response
                    String responseString = getMethod.getResponseBodyAsString();
                    if (StringUtils.isNotEmpty(responseString))
                    {
                        //System.err.println(responseString);
                    }
                    return responseString;
                }
                catch (java.net.UnknownHostException uex)
                {
                    //log.error(uex.getMessage());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                boolean isError = true;
                
                try
                {
                    String responseString = get();
                    
                    if (StringUtils.isNotEmpty(responseString))
                    {
                        String bboxStr = "-180.0,-90.0,180.0,90.0";
                        
                        JSONTokener tok = new JSONTokener(responseString);
                        while (tok.more())
                        {
                            JSONObject obj = (JSONObject)tok.nextValue();
                            
                            //JSONArray titleArray = (JSONArray)obj.get("title");
                            //System.out.println(titleArray.get(0));
                            
                            JSONArray pointArray = (JSONArray)obj.get("point");
                            Iterator<Object> iter =  (Iterator<Object>)pointArray.iterator();
                            while (iter.hasNext())
                            {
                                JSONObject pObj = (JSONObject)iter.next();
                                String lat  = (String)pObj.get("latitude");
                                String lon  = (String)pObj.get("longitude");
                                //System.out.println(lat+"  "+lon);
                                
                                LatLonPlacemark plcMark = new LatLonPlacemark(Double.parseDouble(lat.trim()), Double.parseDouble(lon.trim()));
                                points.add(plcMark);
                            }
                            
                            isError = false;
                        }
                        
                        if (points.size() > 0)
                        {
                            wwPanel.placeMarkers(points, 0, null, false);
                        }
                        
                        bboxStr = "-180.0,-90.0,180.0,90.0";
                        String fmtUrl = "http://lifemapper.org/ogc?map=data_%s&layers=bmng,occ_%s&request=GetMap&service=WMS&version=1.1.0&bbox=%s&srs=epsg:4326&format=image/png&width=450&height=225&styles=&color=ff0000";
                        String url = String.format(fmtUrl, occurSet, occurSet, bboxStr);
                        //System.out.println(url);
                        
                        imgDisplay.setValue(url, null);
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                imgDisplay.loadImage();
                            }
                        });
                    } else
                    {
                        // error
                    }
                    
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
                
                if (isError)
                {
                    showErrorMsg(glassPane);
                } else
                {
                    UIRegistry.clearSimpleGlassPaneMsg();
                }
            }
        };
        worker.execute();

    }
    
    //----------------------------------------------------------------------------------
    //--
    //----------------------------------------------------------------------------------
    class LatLonPlacemark implements LatLonPlacemarkIFace
    {
        private Pair<Double, Double> pnt;
        /**
         * 
         */
        public LatLonPlacemark(final double lat, final double lon)
        {
            super();
            pnt = new Pair<Double, Double>(lat, lon);
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#getTitle()
         */
        @Override
        public String getTitle()
        {
            return null;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#getHtmlContent(java.lang.String)
         */
        @Override
        public String getHtmlContent(String textColor)
        {
            return null;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#getLatLon()
         */
        @Override
        public Pair<Double, Double> getLatLon()
        {
            return pnt;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#getAltitude()
         */
        @Override
        public Double getAltitude()
        {
            return null;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#getImageIcon()
         */
        @Override
        public ImageIcon getImageIcon()
        {
            return markerImg;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.mapping.LatLonPlacemarkIFace#cleanup()
         */
        @Override
        public void cleanup()
        {
        }
    }
    
    //----------------------------------------------------------
    class GenusSpeciesDataItem implements OccurrenceSetIFace
    {
        private String title;
        private String occurrenceId;
        
        /**
         * @param title
         * @param genusSpecies
         */
        public GenusSpeciesDataItem(String title, String occurrenceId)
        {
            super();
            this.title = title;
            this.occurrenceId = occurrenceId;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.tasks.subpane.LifeMapperPane.GenusSpeciesItem#getTitle()
         */
        @Override
        public String getTitle()
        {
            return title;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.tasks.subpane.LifeMapperPane.GenusSpeciesItem#getGenusSpecies()
         */
        @Override
        public String getOccurrenceId()
        {
            return occurrenceId;
        }
        
    }
    
    //----------------------------------------------------------------------------------
    //--
    //----------------------------------------------------------------------------------
    public interface LMSearchCallbackListener
    {
        /**
         * 
         */
        public abstract void itemsFound(List<OccurrenceSetIFace> items);
        
        
        /**
         * 
         */
        public abstract void noItems();
    }
    
    //--------------------------------
    public interface OccurrenceSetIFace
    {
        /**
         * @return
         */
        public abstract String getTitle();
        
        /**
         * @return
         */
        public abstract String getOccurrenceId();
    }
    
    //--------------------------------
    public interface GenusSpeciesItemIFace
    {
        /**
         * @return
         */
        public abstract String getTitle();
        
        /**
         * @return
         */
        public abstract String getOccurrenceId();
    }
}
