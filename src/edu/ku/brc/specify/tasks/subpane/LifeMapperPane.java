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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
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

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.services.mapping.LatLonPlacemarkIFace;
import edu.ku.brc.specify.ui.WorldWindPanel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ImageDisplay;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;
import edu.ku.brc.util.Pair;
import gov.nasa.worldwind.render.Polyline;

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
    
    protected WorldWindPanel        wwPanel;
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
        wwPanel.setPreferredSize(new Dimension(MAP_WIDTH, MAP_HEIGHT));
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
        pb.add(wwPanel,            cc.xyw(1,5,3));
        
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
        listModel.clear();
        occurList.clear();
        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("LifeMapperTask.PROCESSING"), 24);
        glassPane.setTextYPos((int)((double)getSize().height * 0.25));

        SwingWorker<String, String> worker = new SwingWorker<String, String>()
        {
            @Override
            protected String doInBackground() throws Exception
            {
                HttpClient httpClient = new HttpClient();
                httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
                
                String genusSpecies = StringUtils.replace(searchText.getText(), " ", "%20");
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
                
                try
                {
                    String responseString = get();
                    
                    if (StringUtils.isNotEmpty(responseString))
                    {
                        String[] lines = StringUtils.split(responseString, '\n');
                        for (String line : lines)
                        {
                            String[] fields = StringUtils.split(line, '\t');
                            if (fields != null && fields.length == 3)
                            {
                                listModel.addElement(String.format("%s (%s)", fields[0], fields[2]));
                                occurList.add(fields[1]);
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
                } else
                {
                    UIRegistry.clearSimpleGlassPaneMsg();
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
        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("LifeMapperTask.PROCESSING"), 24);
        glassPane.setTextYPos((int)((double)getSize().height * 0.25));
        
        
        points.clear();
        
        // check the website for the info about the latest version
        final HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        
        if (list.getSelectedIndex() < 0)
        {
            return;
        }
        
        occurSet = occurList.get(list.getSelectedIndex());
        final String url = String.format("http://www.lifemapper.org/services/occurrences/%s/json?fillPoints=true", occurSet);
        //System.out.println(url);
        
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
                            
                            wwPanel.reset();
                            if (points.size() > 0)
                            {
                                wwPanel.placeMarkers(points, 0);
                            }
                            isError = false;
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
}
