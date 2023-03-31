/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.tasks.subpane.lm;

import static edu.ku.brc.ui.UIHelper.createI18NButton;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.clearSimpleGlassPaneMsg;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.writeSimpleGlassPaneMsg;
import static edu.ku.brc.ui.UIRegistry.writeTimedSimpleGlassPaneMsg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
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
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polyline;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.BasicMarkerShape;
import org.apache.http.impl.client.HttpClients;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 12, 2011
 *
 */
@SuppressWarnings({"rawtypes","serial"})
public class LifeMapperPane extends BaseSubPane implements ChangeListener
{
    protected static final int GLASS_FONT_SIZE = 14;
    protected static final int MAX_IMAGE_REQUEST_COUNT = 3;
    
    public String URL_FMT;
    public String BG_URL;
    public String BBOX_STR;
    
    protected static final int MAP_WIDTH  = 600;
    protected static final int MAP_HEIGHT = 450;
    protected static final int IMG_WIDTH  = 450;
    protected static final int IMG_HEIGHT = 225;
    
    protected BufferedImage              blueMarble      = null;
    protected int                        blueMarbleTries = 0;
    protected String                     blueMarbleURL;
    protected BufferedImageFetcherIFace  blueMarbleListener;
    protected BufferedImageFetcherIFace  pointsMapImageListener;
    protected BufferedImage              renderImage     = null;
    
    protected WorldWindPanel             wwPanel;
    protected boolean                    doResetWWPanel = true;
    protected JButton                    searchBtn;
    protected int                        currentSize    = 0;
    
    // Search Data
    protected DefaultListModel           model   = null;
    protected List<LatLonPlacemarkIFace> markers = new Vector<LatLonPlacemarkIFace>();
    protected Polyline                   polygon = null;
    protected int                        totalNumRecords = 0;
    
    // My Data UI
    protected JTextField                 myDataTF;
    protected JButton                    searchMyDataBtn;
    protected String                     myDataTaxa = "";
    protected JComponent                 mySepComp;
       
    // Lifemapper Data
    protected JButton                    searchSciNameBtn;
    protected JTextField                 searchText;
    protected String                     occurSet = null;
    protected JList                      list;
    protected DefaultListModel           listModel = new DefaultListModel();
    
    protected ArrayList<OccurrenceSetIFace> occurList     = new ArrayList<OccurrenceSetIFace>();
    protected ImageDisplay                  imgDisplay;
    protected int                           imgRequestCnt = 0;
    protected String                        imgURL        = null;
    
    protected ImageIcon                     markerImg;
    protected ArrayList<LatLonPlacemarkIFace>  points = new ArrayList<LatLonPlacemarkIFace>();

    /**
     * @param name
     * @param task
     */
    public LifeMapperPane(String name, Taskable task)
    {
        super(name, task);
        
        URL_FMT  = getResourceString("LM_URL_FMT");
        BG_URL   = getResourceString("LM_BG_URL");
        BBOX_STR = getResourceString("LM_BBOX_STR");
        
        markerImg = IconManager.getIcon("RedDot6x6");
        createUI();
    }
    
    /**
     * @return
     */
    private int getCurrentSizeSquare()
    {
        int maxHeight = MAP_HEIGHT;
        int maxWidth  = MAP_WIDTH;
        if (SubPaneMgr.getInstance() instanceof JTabbedPane)
        {
            Dimension size;
            JTabbedPane tbPane = (JTabbedPane)SubPaneMgr.getInstance();
            if (tbPane.getTabCount() > 0)
            {
                size = tbPane.getComponentAt(0).getSize();
            } else
            {
                size = tbPane.getSize();
                size.height -= 30;
            }
            int lblHeight = (UIHelper.createLabel(" ").getPreferredSize().height) * 5;
            maxHeight = size.height - lblHeight - IMG_HEIGHT - 30;
            maxWidth  = size.width - 20;
        }
        return Math.min(maxHeight, maxWidth);
    }
    
    /**
     * @return
     */
    public boolean hasSizeChanged()
    {
        int newSize = getCurrentSizeSquare();
        return currentSize != newSize;
    }

    /**
     * Creates the UI.
     */
    @SuppressWarnings("unchecked")
    protected void createUI()
    {
        currentSize = getCurrentSizeSquare();

        searchText       = createTextField(25);
        searchSciNameBtn = createI18NButton("LM_SEARCH");
        list             = new JList(listModel);
        imgDisplay       = new ImageDisplay(IMG_WIDTH, IMG_HEIGHT, false, true);
        
        imgDisplay.setChangeListener(this);
        
        wwPanel = new WorldWindPanel(false);
        wwPanel.setPreferredSize(new Dimension(currentSize, currentSize));
        wwPanel.setZoomInMeters(600000.0);
        
        imgDisplay.setDoShowText(false);
        
        searchMyDataBtn = createI18NButton("LM_SRCH_SP_DATA");
        myDataTF        = UIHelper.createTextField();
        
        CellConstraints cc = new CellConstraints();
        
        PanelBuilder pb1 = new PanelBuilder(new FormLayout("p,2px,f:p:g,2px,p", "p"));
        pb1.add(createI18NFormLabel("LM_SRCH_COL"), cc.xy(1,1));
        pb1.add(searchText,                         cc.xy(3,1));
        pb1.add(searchSciNameBtn,                   cc.xy(5,1));

        PanelBuilder myPB = new PanelBuilder(new FormLayout("f:p:g,p", "p,2px,p,2px,p"));
        mySepComp = myPB.addSeparator(getResourceString("LM_MYDATA_TITLE"), cc.xyw(1,1,2));
        myPB.add(myDataTF,         cc.xyw(1, 3, 2));
        myPB.add(searchMyDataBtn,  cc.xy(2,5));
        
        PanelBuilder pb2 = new PanelBuilder(new FormLayout("MAX(p;300px),2px,f:p:g", "f:p:g,20px,p"));
        pb2.add(createScrollPane(list), cc.xy(1, 1));
        pb2.add(myPB.getPanel(),        cc.xy(1, 3));
        
        PanelBuilder pb3 = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "f:p:g,p,4px,p,f:p:g"));
        pb3.add(createI18NLabel("LM_WRLD_OVRVW", SwingConstants.CENTER), cc.xy(2,2));
        pb3.add(imgDisplay,                           cc.xy(2, 4));
        
        PanelBuilder pb4 = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "f:p:g,p,4px,p,f:p:g"));
        pb4.add(createI18NLabel("LM_INTRACT_VW", SwingConstants.CENTER), cc.xy(2,2));
        pb4.add(wwPanel,            cc.xy(2, 4));
        
        PanelBuilder pb5 = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,p,f:p:g"));
        pb5.add(pb3.getPanel(), cc.xy(1,1));
        pb5.add(pb4.getPanel(), cc.xy(1, 3));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,8px,f:p:g", "p,8px,f:p:g"), this);
        pb.add(pb1.getPanel(),  cc.xyw(1, 1, 3));
        pb.add(pb2.getPanel(),  cc.xy(1, 3));
        pb.add(pb5.getPanel(),  cc.xy(3, 3));
        
        updateMyDataUIState(false);
        
        searchText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchSciNameBtn.doClick();
                }
            }
        });
        
        myDataTF.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchMyDataBtn.doClick();
                }
            }
        });
        
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (list.getSelectedIndex() == -1) {
                        wwPanel.reset();
                        imgDisplay.setImage(blueMarble);

                    } else {
                        SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>() {
                            @Override
                            protected Boolean doInBackground() throws Exception {
                                if (doResetWWPanel) {
                                    wwPanel.reset();
                                }
                                doSearchOccur();
                                return null;
                            }

                            @Override
                            protected void done() {
                                imgDisplay.repaint();
                            }
                        };
                        worker.execute();
                    }
                }
            }
        });
        
        searchMyDataBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        doSearchSpecifyData(myDataTF.getText().trim());
                    }
                });

            }
        });
        
        searchSciNameBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSearchGenusSpecies();
            }
        });
        
        blueMarbleListener = new BufferedImageFetcherIFace()
        {
            @Override
            public void imageFetched(BufferedImage image)
            {
                blueMarble = image;
                imgDisplay.setImage(blueMarble);
            }

            @Override
            public void error()
            {
                blueMarbleTries++;
                if (blueMarbleTries < 5)
                {
                    blueMarbleRetry();
                }
            }
        };


        blueMarbleURL = "";//BG_URL + String.format("WIDTH=%d&HEIGHT=%d", IMG_WIDTH, IMG_HEIGHT);

        pointsMapImageListener = new BufferedImageFetcherIFace()
        {
            @Override
            public void imageFetched(final BufferedImage image)
            {
                if (renderImage == null)
                {
                    renderImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
                }
                Graphics2D g2d = renderImage.createGraphics();
                if (g2d != null)
                {
                    g2d.fillRect(0, 0, IMG_WIDTH, IMG_HEIGHT);
                    if (blueMarble != null)
                    {
                        g2d.drawImage(blueMarble, 0, 0, null);
                    }
                    if (image != null)
                    {
                        g2d.drawImage(image, 0, 0, null);
                    }
                    g2d.dispose();
                    
                    imgDisplay.setImage(renderImage);
                }
            }
            
            @Override
            public void error()
            {
            }
        };
        blueMarbleRetry();
    }
    
    /**
     * 
     */
    private void blueMarbleRetry()
    {
        SwingWorker<BufferedImage, BufferedImage> worker = new SwingWorker<BufferedImage, BufferedImage>()
        {
            @Override
            protected BufferedImage doInBackground() throws Exception
            {
                try {
                    return ImageIO.read(getClass().getResource("/edu/ku/brc/specify/images/bmng_450x225.png"));

                } catch (IOException e) {

                }
                return null;
            }

            @Override
            protected void done()
            {
                try
                {
                    BufferedImage img = get();
                    blueMarbleListener.imageFetched(img);
                    return;

                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
                blueMarbleListener.error();

                super.done();
            }
        };
        worker.execute();

    }
    
    /**
     * @param urlStr
     * @param listener
     */
    public static void getImageFromWeb(final String                    urlStr, 
                                       final BufferedImageFetcherIFace listener)
    {
        //System.out.println(urlStr);
        
        SwingWorker<BufferedImage, BufferedImage> worker = new SwingWorker<BufferedImage, BufferedImage>()
        {
            @Override
            protected BufferedImage doInBackground() throws Exception
            {
                try {
                    URL url = new URL(urlStr);
                    return ImageIO.read(url);
                    
                 } catch (IOException e) {

                 }
                return null;
            }

            @Override
            protected void done()
            {
                try
                {
                    BufferedImage img = get();
                    listener.imageFetched(img);
                    return;
                    
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
                listener.error();
                
                super.done();
            }
        };
        worker.execute();
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
        doSearchGenusSpecies(searchStr, false);
    }
    
    /**
     * 
     */
    public void doSearchGenusSpecies(final String searchStr, final boolean doSetTextField)
    {
        imgDisplay.setImage(blueMarble);
        
        listModel.clear();
        occurList.clear();
        
        if (doSetTextField)
        {
            myDataTF.setText(searchStr);
            searchText.setText(searchStr);
        }
        
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
                @SuppressWarnings("unchecked")
                @Override
                public void itemsFound(final List<OccurrenceSetIFace> items)
                {
                    if (items != null)
                    {
                        for (OccurrenceSetIFace item : items)
                        {
                            listModel.addElement(item.getTitle());
                            occurList.add(item);
                        }
                        
                        if (doSetTextField)
                        {
                            addLocalData(searchStr);
                        }
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
            LatLonPlacemark llp = new LatLonPlacemark(markerImg, rs.getDouble(1), rs.getDouble(2));
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
     * @param genusSpecies
     */
    private void doSearchSpecifyData(final String genusSpecies)
    {
        UsageTracker.incrUsageCount("LM.MyDataSearch");

        myDataTaxa = genusSpecies;
        
        final SimpleGlassPane glassPane = writeSimpleGlassPaneMsg(getLocalizedMessage("LM_SEARCH_SPECIFY"), GLASS_FONT_SIZE);
        glassPane.setTextYPos((int)((double)getSize().height * 0.25));

        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                
                return addLocalData(genusSpecies);
            }

            @Override
            protected void done()
            {
                super.done();
                clearSimpleGlassPaneMsg();
                
                Integer cnt = null;
                try
                {
                    cnt = get();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
                if (cnt == null || cnt == 0)
                {
                    writeTimedSimpleGlassPaneMsg(getResourceString("LM_NO_LOCAL_DATA"), null, Color.RED, GLASS_FONT_SIZE, true, (int)((double)getSize().height * 0.25));
                    UsageTracker.incrUsageCount("LM.NoMyData");
                } else
                {
                    String msg = UIRegistry.getFormattedResStr("LM_MYDATA_FND", cnt, myDataTaxa);
                    writeTimedSimpleGlassPaneMsg(msg, null, null, GLASS_FONT_SIZE, true, (int)((double)getSize().height * 0.25));
                }
            }
        };
        worker.execute();

    }

    /**
     * @param recSet
     */
    public int addLocalData(final String genusSpecies)
    {
        int numFnd = 0;
        
        Connection conn = null;        
        Statement stmt  = null;

        try
        {
            String sql = "SELECT ce.CollectingEventID, l.Latitude1, l.Longitude1 FROM taxon t INNER JOIN determination d ON t.TaxonID = d.TaxonID " +
            "INNER JOIN collectionobject co ON d.CollectionObjectID = co.CollectionObjectID " +
            "INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
            "INNER JOIN locality l ON ce.LocalityID = l.LocalityID WHERE co.CollectionMemberID = COLMEMID AND t.FullName LIKE '" + genusSpecies + "%'";
            
            sql = QueryAdjusterForDomain.getInstance().adjustSQL(sql);
            
            conn   = DBConnection.getInstance().createConnection();
            stmt = conn.createStatement();

            ArrayList<LatLonPlacemarkIFace> coPoints = new ArrayList<LatLonPlacemarkIFace>();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                LatLonPlacemark llp = new LatLonPlacemark(markerImg, rs.getDouble(2), rs.getDouble(3));
                coPoints.add(llp);
            }
            
            numFnd = coPoints.size();
            if (numFnd > 0)
            { 
                BasicMarkerAttributes bmAttrs = new BasicMarkerAttributes(Material.GREEN, BasicMarkerShape.CONE, 1d, 3, 3);
                wwPanel.placeMarkers(coPoints, true, false, 0, bmAttrs, false);
            }
        } 
        catch (SQLException ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try 
            {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception ex) {}
        }
        return numFnd;
    }

    /**
     * @param recSet
     */
    public void addLocalData(final RecordSet recSet)
    {
        UsageTracker.incrUsageCount("LM.RSData");
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
                wwPanel.placeMarkers(coPoints, true, false, 0, bmAttrs, false);
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
        UsageTracker.incrUsageCount("LM.GenSpSearch");
       
        updateMyDataUIState(false);

        final SimpleGlassPane glassPane = writeSimpleGlassPaneMsg(getLocalizedMessage("LifeMapperTask.PROCESSING"), GLASS_FONT_SIZE);
        glassPane.setTextYPos((int) ((double) getSize().height * 0.25));

        SwingWorker<String, String> worker = new SwingWorker<String, String>()
        {
            @Override
            protected String doInBackground() throws Exception
            {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
                httpClient.getParams().setParameter("http.socket.timeout", 15000); 
                
                String genusSpecies = StringUtils.replace(searchStr, " ", "%20");
                String url = "http://svc.lifemapper.org/api/v2/hint/"+genusSpecies + "?limit=1000";
                //System.out.println(url);
                
                HttpGet getMethod = new HttpGet(url);
                try
                {
                    CloseableHttpResponse response = httpClient.execute(getMethod);
                    return EntityUtils.toString(response.getEntity());
                }
                catch (java.net.UnknownHostException uex)
                {
                    //log.error(uex.getMessage());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    UsageTracker.incrUsageCount("LM.GenSpSearchErr");
                }
                return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void done()
            {
                super.done();
                
                ArrayList<OccurrenceSetIFace> items = null;
                
                String errMsgKey = "LifeMapperTask.PROC_ERR";
                String responseString;
                try
                {
                    responseString = get();
                    //System.out.println(responseString);
                    
                    if (responseString != null)
                    {
                        if (responseString.startsWith("[]"))
                        {
                            errMsgKey = "LM_NO_LM_DATA";
                            
//                        } else if (responseString.startsWith("Search too broad"))
//                        {
//                            errMsgKey = "LM_TOO_BROAD";
//
                        } else if (StringUtils.isNotEmpty(responseString) && StringUtils.contains(responseString.toLowerCase(), "{"))
                        {
                            JSONTokener tok = new JSONTokener(responseString);
                            while (tok.more())
                            {
//                                JSONObject obj = (JSONObject)tok.nextValue();
//                                JSONArray pointArray = (JSONArray)tok.nextValue(); //obj.get("columns");
//                                Iterator<Object> iter =  (Iterator<Object>)pointArray.iterator();
//                                while (iter.hasNext())
//                                {
                                    JSONArray arrayObj = (JSONArray)tok.nextValue();
                                    //System.out.println(arrayObj);
                                    Iterator<Object> iterInner =  (Iterator<Object>)arrayObj.iterator();
                                    while (iterInner.hasNext())
                                    {
                                        JSONObject pObj = (JSONObject)iterInner.next();
                                        String binomial   = (String)pObj.get("binomial");
                                        String gnSpName      = (String)pObj.get("name");
                                        Integer numPoints     = (Integer)pObj.get("numPoints");
                                        Integer occurrenceSet = (Integer)pObj.get("occurrenceSet");
                                        
                                        if (StringUtils.isNotEmpty(gnSpName) // &&
//                                            StringUtils.isNotEmpty(numPoints) &&
//                                            StringUtils.isNotEmpty(occurrenceSet)
                                                )
                                        {
                                            try
                                            {
//                                                int numPnts = Integer.parseInt(numPoints);
//                                                int occurId = Integer.parseInt(occurrenceSet);
                                                if (numPoints > 0 && occurrenceSet > 0)
                                                {
                                                    if (items == null)
                                                    {
                                                        items = new ArrayList<OccurrenceSetIFace>();
                                                    }
                                                    items.add(new GenusSpeciesDataItem(String.format("%s (%s)", gnSpName, numPoints.toString()), occurrenceSet.toString(), binomial));
                                                }
                                            } catch (Exception ex)
                                            {
                                                // no op
                                            }
                                            errMsgKey = items == null || items.size() == 0 ? "LM_NO_LOCAL_DATA" : null;
                                        }
                                    }
//                                 }
                            }
                        }
                    }

                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                    
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
                
                if (errMsgKey != null)
                {
                    showErrorMsg(glassPane, errMsgKey);
                    cbListener.noItems();
                    
                } else
                {
                    //System.out.println("Num Genus/Species: "+items.size());
                    clearSimpleGlassPaneMsg();
                    cbListener.itemsFound(items);
                }
            }
        };
        worker.execute();
    }
    
    /**
     * @param glassPane
     * @param key
     */
    protected void showErrorMsg(final SimpleGlassPane glassPane, final String key)
    {
        glassPane.setTextColor(Color.RED);
        glassPane.setText(getLocalizedMessage(key));
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try { Thread.sleep(2000); } catch (Exception ex){}
                clearSimpleGlassPaneMsg();
            }
        });
    }
    
    /**
     * 
     */
    private void doSearchOccur()
    {
        OccurrenceSetIFace occurSetItem = occurList.get(list.getSelectedIndex());
        occurSet = occurSetItem.getOccurrenceId();
        
        if (StringUtils.isNotEmpty(occurSet))
        {
            doSearchOccur(occurSet);
            myDataTaxa = occurSetItem.getTaxa();
            myDataTF.setText(myDataTaxa);
        }
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public void doSearchOccur(final String occurrenceId)
    {
        updateMyDataUIState(false);

        points.clear();

        final SimpleGlassPane glassPane = writeSimpleGlassPaneMsg(getLocalizedMessage("LifeMapperTask.PROCESSING"), GLASS_FONT_SIZE);
        glassPane.setTextYPos((int)((double)getSize().height * 0.25));
        
        // check the website for the info about the latest version
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        httpClient.getParams().setParameter("http.socket.timeout", 15000); 
        
        if (list.getSelectedIndex() < 0)
        {
            return;
        }
        
        UsageTracker.incrUsageCount("LM.OccurSearch");
        
        final String lmURL = String.format("http://svc.lifemapper.org/api/v2/occurrence/%s/json?fillPoints=1", occurrenceId);
        //System.out.println(lmURL);
        
        SwingWorker<String, String> worker = new SwingWorker<String, String>()
        {
            @Override
            protected String doInBackground() throws Exception
            {
                HttpGet  getMethod  = new HttpGet(lmURL);
                try
                {
                    CloseableHttpResponse response = httpClient.execute(getMethod);
                    
                    // get the server response
                    //String responseString = getMethod.getResponseBodyAsString();
                    String responseString = EntityUtils.toString(response.getEntity());
                    if (StringUtils.isNotEmpty(responseString)) {
                        return responseString;
                    }
                    //byte[] bytes = response.getResponseBody();
                    //if (bytes != null && bytes.length > 0)
                   //{
                    //    return new String(bytes, "UTF-8");
                    //}
                    //if (StringUtils.isNotEmpty(responseString))
                    //{
                    //    System.err.println(responseString);
                    //}
                    return null;
                }
                catch (java.net.UnknownHostException uex)
                {
                    //log.error(uex.getMessage());
                }
                catch (java.net.SocketTimeoutException ex)
                {
                    UsageTracker.incrUsageCount("LM.OccurSearchErr");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    UsageTracker.incrUsageCount("LM.OccurSearchErr");
                }

                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                boolean isError    = true;
                boolean parseError = false;

                try
                {
                    String responseString = get();
                    
                    if (StringUtils.isNotEmpty(responseString) && StringUtils.contains(responseString.toLowerCase(), "{"))
                    {
                        // Need to change this to using regex to strip away unwanted chars
                        StringBuilder sb = new StringBuilder();
                        String[] lines = StringUtils.split(responseString, '\n');
                        for (String str : lines)
                        {
                            if (str.indexOf("resname") == -1)
                            {
                                sb.append(str);
                            }
                        }
                        String cleaned = sb.toString();
                        
                        parseError = false;
                        try
                        {
                            JSONTokener tok = new JSONTokener(cleaned);
                            if (tok != null)
                            {
                                while (tok.more())
                                {
                                    JSONObject obj = (JSONObject)tok.nextValue();
                                    if (obj != null)
                                    {
                                        JSONArray pointArray = (JSONArray)obj.get("features");
                                        if (pointArray != null)
                                        {
                                            Iterator<Object> iter =  (Iterator<Object>)pointArray.iterator();
                                            while (iter.hasNext())
                                            {
                                                JSONObject pObj = (JSONObject)iter.next();
                                                if (pObj != null)
                                                {
                                                    String lat  = null;//(String)pObj.get("lat");
                                                    String lon  = null;//(String)pObj.get("lon");
                                                    String geomwkt = (String)pObj.get("geomwkt");
                                                    if (geomwkt != null) {
                                                    	//quel cheapo...
                                                    	geomwkt = geomwkt.replace("POINT", "");
                                                    	geomwkt = geomwkt.replace("(", "");
                                                    	geomwkt = geomwkt.replace(")", "");
                                                    	geomwkt = geomwkt.trim();
                                                    	String[] geocs = geomwkt.split(" ");
                                                    	if (geocs.length == 2) {
                                                    		lon = geocs[0];
                                                    		lat = geocs[1];
                                                    	}
                                                    }
                                                    //System.out.println(lat+"  "+lon);
                                                    if (lat != null && lon != null)
                                                    {
                                                        LatLonPlacemark plcMark = new LatLonPlacemark(markerImg, Double.parseDouble(lat.trim()), Double.parseDouble(lon.trim()));
                                                        points.add(plcMark);
                                                    }
                                                }
                                            }
                                            isError = false;
                                        }
                                    }
                                }
                            }
                        } catch (net.sf.json.JSONException ex)
                        {
                            System.err.println(ex.getLocalizedMessage());
                            parseError = true;
                        }

                        boolean hasPnts = points.size() > 0;
                        updateMyDataUIState(hasPnts && StringUtils.isNotEmpty(myDataTF.getText()));
                        if (hasPnts)
                        {
                            imgDisplay.setImage((Image)null);
                            wwPanel.placeMarkers(points, false, true, 0, null, false);
                        
                            imgRequestCnt = 0;
                            
                            imgURL = makeURL(occurSet);
                            
                            getImageFromWeb(imgURL, pointsMapImageListener);
                            
                        } else
                        {
                            isError = false;
                        }
                        
                    } else
                    {
                        UsageTracker.incrUsageCount("LM.OccurSearchErr");
                    }
                    
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                } catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
                
                if (isError || parseError)
                {
                    showErrorMsg(glassPane, "LifeMapperTask.PROC_ERR");
                } else
                {
                    clearSimpleGlassPaneMsg();
                }
            }
        };
        worker.execute();
    }
    
    /**
     * @param occurrenceSetId
     * @return
     */
    private String makeURL(final String occurrenceSetId)
    {
        String paramStr;
        if (StringUtils.isNotEmpty(occurrenceSetId))
        {
            paramStr = String.format("map=data_%s&layers=bmng,occ_%s", occurrenceSetId, occurrenceSetId);
        } else
        {
            paramStr = "layers=bmng";
        }
        
        return String.format(URL_FMT, paramStr, BBOX_STR, IMG_WIDTH, IMG_HEIGHT);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(ChangeEvent e)
    {
        if (imgDisplay.isInError())
        {
            loadOverviewImage();
        }
    }

    /**
     * @param count
     */
    private void loadOverviewImage()
    {
        if (StringUtils.isNotEmpty(imgURL) && imgRequestCnt < MAX_IMAGE_REQUEST_COUNT)
        {
            imgDisplay.setValue(imgURL, null);
            imgRequestCnt++;
        }
    }
    
    /**
     * @param enable
     */
    private void updateMyDataUIState(final boolean enable)
    {
        myDataTF.setEnabled(enable);
        searchMyDataBtn.setEnabled(enable);
        mySepComp.setEnabled(enable);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#showingPane(boolean)
     */
    @Override
    public void showingPane(boolean show)
    {
        super.showingPane(show);
        wwPanel.setWWPanelVisible(show);
        //wwPanel.getWorld().setVisible(show);
        //wwPanel.setLocation(show ? 0 : -1000, 0);
    }
    
}
