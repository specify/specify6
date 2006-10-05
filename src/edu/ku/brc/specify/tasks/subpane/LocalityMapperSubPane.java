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

package edu.ku.brc.specify.tasks.subpane;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.jdesktop.animation.timing.TimingTarget;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.extras.FishBaseInfoGetter;
import edu.ku.brc.specify.extras.FishBaseInfoGetterListener;
import edu.ku.brc.specify.tasks.services.KeyholeMarkupGenerator;
import edu.ku.brc.specify.tasks.services.LocalityMapper;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ImageDisplay;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.forms.ControlBarPanel;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.ResultSetController;
import edu.ku.brc.ui.forms.ResultSetControllerListener;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.View;

/**
 * A default pane for display a simple label telling what it is suppose to do
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class LocalityMapperSubPane extends BaseSubPane implements LocalityMapper.MapperListener, ResultSetControllerListener, TimingTarget
{
    //private static final Logger log = Logger.getLogger(SimpleDescPane.class);
    protected DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
    protected static final Cursor handCursor   = new Cursor(Cursor.HAND_CURSOR);
    protected static final Cursor defCursor    = new Cursor(Cursor.DEFAULT_CURSOR);

    protected LocalityMapper                  localityMapper  = new LocalityMapper();
    protected JLabel                          imageLabel      = new JLabel(getResourceString("LoadingImage"));
    protected JLabel                          titleLabel      = new JLabel();
    protected MultiView                       multiView;

    protected List<CollectingEvent>           collectingEvents;
    protected List<Hashtable<String, Object>> valueList       = new ArrayList<Hashtable<String, Object>>();
    protected List<Rectangle>                 markerRects     = new ArrayList<Rectangle>();
    protected boolean                         dirty           = false;

    protected List<ImageGetter>               imageGetterList = new ArrayList<ImageGetter>();
    protected Hashtable<String, Image>        imageMap        = new Hashtable<String, Image>();
    protected FormViewObj                     formViewObj;
    protected JList                           imageJList;

    protected Hashtable<String, String>       imageURLs       = new Hashtable<String, String>();

    protected ResultSetController             recordSetController;
    protected ControlBarPanel                 controlPanel;
    protected JButton                         googleBtn;
    protected KeyholeMarkupGenerator          kmlGen;
    protected List<CollectingEvent>           colEvents;
    protected LocalityMapperSubPane           thisPane;

    /**
     * The incoming List of Collecting Events is already Sorted by StartDate.
     * @param session the DB Session to use
     * @param name the name
     * @param task the owning task
     * @param colEvents sorted list of collecting events
     */
    public LocalityMapperSubPane(final Session session,
                                 final String name,
                                 final Taskable task,
                                 final List<CollectingEvent> colEvents)
    {
        super(session, name, task);
        
        this.colEvents = colEvents;
        this.thisPane  = this;

        progressLabel.setText("Loading Locality Data and Maps...");

        localityMapper.addTimingTarget(this);

        setBackground(Color.WHITE);

        final SwingWorker worker = new SwingWorker()
        {
            public Object construct()
            {
                createUI();
                return null;
            }

            //Runs on the event-dispatching thread.
            public void finished()
            {
                thisPane.removeAll();
                setLayout(new LocalityMapperLayoutManager(thisPane, titleLabel, imageLabel, controlPanel, multiView));

                multiView.setData(valueList.get(0));

                validate();
                doLayout();

            }
        };
        worker.start();
    }

    /**
     *
     */
    protected void createUI()
    {
        kmlGen = new KeyholeMarkupGenerator();
        this.collectingEvents = new ArrayList<CollectingEvent>();

        CollectingEvent startCE = null;
        CollectingEvent endCE   = null;

        Vector<Locality> localities = new Vector<Locality>();
        Vector<String>   labels     = new Vector<String>();
        for (Object obj : colEvents)
        {
        	CollectingEvent collectingEvent = (CollectingEvent)obj;

            Locality locality = collectingEvent.getLocality();
            if (locality == null || locality.getLatitude1() == null || locality.getLongitude1() == null)
            {
                continue;
            }

            collectingEvents.add(collectingEvent);
            kmlGen.addCollectingEvent(collectingEvent, "");

            if (collectingEvents.size() == 1)
            {
                startCE = collectingEvent;
                endCE   = collectingEvent;
            }

            // There may be an End Date that is further out than than the End Date of the last item
            // with the latest Start Date
            if (startCE.getStartDate().compareTo(collectingEvent.getStartDate()) > 1)
            {
                startCE = collectingEvent;
            }
            Calendar leftCal  = endCE.getEndDate() != null ? endCE.getEndDate() : endCE.getStartDate();
            Calendar rightCal = collectingEvent.getEndDate() != null ? collectingEvent.getEndDate() : collectingEvent.getStartDate();
            if (leftCal.compareTo(rightCal) < 0)
            {
                endCE = collectingEvent;
            }
        	Hashtable<String, Object> map = new Hashtable<String, Object>();

        	Set<CollectionObject> colObjs = collectingEvent.getCollectionObjects();

        	map.put("startDate", collectingEvent.getStartDate());
        	map.put("endDate", collectingEvent.getEndDate());

        	Set<Object> taxonNames = new HashSet<Object>();
        	for (CollectionObject co : colObjs)
        	{
        		for (Determination d : co.getDeterminations())
        		{
        			if (d.isCurrent())
        			{
        				//System.out.println(d.getTaxon().getName() + "("+co.getCountAmt()+")");
        				Taxon taxon = d.getTaxon();
        				if (taxon != null)
        				{
        					taxonNames.add(taxon.getName() + (co.getCountAmt() != null ? " ("+co.getCountAmt()+")" : ""));
        					if (taxon.getRankId() == 220)
        					{
        						Taxon genus = taxon.getParent();
        						if (genus.getRankId() == 180)
        						{
        							ImageGetter imgGetter = new ImageGetter(imageGetterList, imageMap, imageURLs, genus.getName(), taxon.getName());
        							imageGetterList.add(imgGetter);
        						}
        					}
        				}
        				break;
        			}
        		}
        	}
        	map.put("taxonItems", taxonNames);

        	map.put("latitude1", locality.getLatitude1());
        	map.put("longitude1", locality.getLongitude1());

            /*
        	Calendar cal = collectingEvent.getStartDate();
        	if (cal != null)
        	{
        		labels.add(scrDateFormat.format(cal.getTime()));

        	} else if (collectingEvent.getVerbatimDate() != null)
        	{
        		labels.add(collectingEvent.getVerbatimDate());

        	} else
        	{
        		labels.add(Integer.toString(collectingEvent.getCollectingEventId()));

        	}
            */
            labels.add(Integer.toString(collectingEvents.size()));
        	localities.add(locality);
        	valueList.add(map);

        }

        // XXX Fix me shouldn't be hard coded here to make it work
        localityMapper.setMaxMapWidth(515);
        localityMapper.setMaxMapHeight(375);

        Color arrow = new Color(220,220,220);
        localityMapper.setArrowColor(arrow);
        localityMapper.setDotColor(Color.WHITE);
        localityMapper.setDotSize(4);
        localityMapper.setLabelColor(Color.RED);

        int inx = 0;
        for (Locality locality : localities)
        {
            localityMapper.addLocalityAndLabel(locality, labels != null ? labels.get(inx) : null);
            inx++;
        }
        localityMapper.setCurrentLoc(localities.get(0));
        localityMapper.setCurrentLocColor(Color.RED);

        // XXX DEMO  (Hard Coded 'null' means everyone would have one which may not be true)
        // "null" ViewSet name means it should use the default
        View view = AppContextMgr.getInstance().getView(null, "LocalityMapper");
        
        // WHERE's the ERROR checking !
        multiView = new MultiView(null, view, AltView.CreationMode.View, MultiView.NO_OPTIONS);
        multiView.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(138,128,128)),
                            BorderFactory.createEmptyBorder(4, 4, 4, 4)));

        multiView.setSession(session);
        
        formViewObj = (FormViewObj)multiView.getCurrentView();
        formViewObj.getUIComponent().setBackground(Color.WHITE);

        imageJList = (JList)formViewObj.getCompById("taxonItems");
        imageJList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                	String nameStr = (String)imageJList.getSelectedValue();
                    if (nameStr != null)
                    {
                        int index = nameStr.indexOf(" (");
                        if (index > -1)
                        {
                            nameStr = nameStr.substring(0, index);
                        }
                    }


                    //System.out.println("Getting["+name+"]");
                    Image img = null;
                    if (StringUtils.isNotEmpty(nameStr))
                    {
                        img = imageMap.get(nameStr); // might return null
                        ImageDisplay imgDisplay = (ImageDisplay)formViewObj.getCompById("image");
                        if (img != null)
                        {
                            imgDisplay.setImage(new ImageIcon(img));
                        } else
                        {
                            imgDisplay.setImage(null);
                        }
                    }


                }
            }});


        String startDateStr = scrDateFormat.format(startCE.getStartDate().getTime());
        String endDateStr   = scrDateFormat.format((endCE.getEndDate() != null ? endCE.getEndDate() : endCE.getStartDate()).getTime());

        Formatter formatter = new Formatter();
        titleLabel.setText(formatter.format(getResourceString("LocalityMapperTitle"), new Object[] {startDateStr, endDateStr}).toString());

        Font font = titleLabel.getFont();
        titleLabel.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()+2));

        recordSetController = new ResultSetController(null, false, false, null, collectingEvents.size());
        recordSetController.addListener(this);
        recordSetController.getPanel().setBackground(Color.WHITE);

        controlPanel = new ControlBarPanel();
        controlPanel.add(recordSetController);
        controlPanel.setBackground(Color.WHITE);

        googleBtn = new JButton(IconManager.getIcon("GoogleEarth", IconManager.IconSize.Std16));
        googleBtn.setMargin(new Insets(1,1,1,1));
        googleBtn.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        googleBtn.setSize(new Dimension(18,18));
        googleBtn.setPreferredSize(new Dimension(18,18));
        googleBtn.setMaximumSize(new Dimension(18,18));
        googleBtn.setFocusable(false);
        googleBtn.setBackground(Color.WHITE);

        controlPanel.addButtons(new JButton[] {googleBtn}, false);

        googleBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                try
                {
                    UICacheManager.displayStatusBarText("Exporting Collecting Events in KML."); // XXX I18N
                    kmlGen.setSpeciesToImageMapper(imageURLs);
                    kmlGen.outputToFile(System.getProperty("user.home")+File.separator+"specify.kml");

                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });

        addMouseMotionListener(new MouseMotionListener()
                {
                    public void mouseDragged(MouseEvent e)
                    {}
                    public void mouseMoved(MouseEvent e)
                    {
                        checkMouseLocation(e.getPoint(), false);
                    }
                });

        addMouseListener(new MouseAdapter()
                {
                    public void mouseClicked(MouseEvent e)
                    {
                        checkMouseLocation(e.getPoint(), true);
                    }

                });

        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                getLocalityMap();
            }
        });

    }

    /**
     * Helper for the above Runnable
     */
    protected void getLocalityMap()
    {
        localityMapper.getMap(this);
    }

    protected void setLabel(final Icon imageIcon)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                imageLabel.setText(null);
                imageLabel.setIcon(imageIcon);
            }
          });


        dirty = true;
    }

    protected void setLabel(final String msg)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                imageLabel.setIcon(null);
                imageLabel.setText(msg);
            }
          });
    }

    /**
     * @param mousePnt
     */
    public void checkMouseLocation(final Point mousePnt, final boolean showInfo)
    {
    	if (dirty)
    	{
    		markerRects.clear();
    		Point pnt = imageLabel.getLocation();
    		//System.out.println("***************** "+pnt+"  "+imageLabel.getBounds()+"  "+imageLabel.getParent()+"  "+this);
            for (Point p : localityMapper.getMarkerLocations())
            {
            	//System.out.println("*** "+(pnt.x+p.x-5)+"  "+(pnt.y+p.y-5));
            	markerRects.add(new Rectangle(pnt.x+p.x-5, pnt.y+p.y-5, 10, 10));
            }
            dirty = false;
    	}

    	int inx = 0;
    	for (Rectangle r : markerRects)
        {
    		//System.out.println(mousePnt.x+" "+mousePnt.y+"  "+r);
    		if (r.contains(mousePnt))
    		{
                if (showInfo)
                {
                    Hashtable<String, Object> map = valueList.get(inx);
                    multiView.setData(map);
                    localityMapper.setCurrentLoc(collectingEvents.get(inx).getLocality());

                } else
                {
                    setCursor(handCursor);
                }
    			return;
    		} else
            {
                setCursor(defCursor);
            }
    		inx++;
        }
    	//multiView.setData(null);

    }

    //------------------------------------------------------------------------
    //-- ResultSetControllerListener
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexChanged(int)
     */
    public void indexChanged(int newIndex)
    {
        Hashtable<String, Object> map = valueList.get(newIndex);
        multiView.setData(map);
        localityMapper.setCurrentLoc(collectingEvents.get(newIndex).getLocality());
        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexAboutToChange(int, int)
     */
    public boolean indexAboutToChange(int oldIndex, int newIndex)
    {
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#newRecordAdded()
     */
    public void newRecordAdded()
    {
        
    }


    //------------------------------------------------------------------------
    //-- Inner Classes
    //------------------------------------------------------------------------



    class ImageGetter implements FishBaseInfoGetterListener
    {
    	protected FishBaseInfoGetter        getter;
    	protected List<ImageGetter>         list;
    	protected Hashtable<String, Image>  map;
        protected Hashtable<String, String> imageURLMap;
        protected String                    genus;
        protected String                    species;

    	public ImageGetter(final List<ImageGetter> list,
                           final Hashtable<String, Image> map,
                           final Hashtable<String, String> imageURLMap,
                           final String genus,
                           final String species)
    	{
    		this.list        = list;
    		this.map         = map;
            this.genus       = genus;
            this.species     = species;
            this.imageURLMap = imageURLMap;

    		getter = new FishBaseInfoGetter(this, FishBaseInfoGetter.InfoType.Thumbnail, genus, species);
    		getter.start();
    	}

        public void infoArrived(FishBaseInfoGetter getterArg)
        {
        	//System.out.println("["+name+"]["+getter.getImage()+"]");
        	if (getterArg.getImage() != null)
        	{
                imageURLMap.put(genus+" "+species, getterArg.getImageURL());
                //System.out.println("["+genus+" "+species+"]["+getter.getImageURL()+"]");
        		map.put(species, getterArg.getImage());
        	}
        	cleanUp();
        }

        public void infoGetWasInError(FishBaseInfoGetter getterArg)
        {
        	cleanUp();
        }

        protected void cleanUp()
        {
        	list.remove(this);
        	list = null;
        	map  = null;
        	getter.setConsumer(null);
        	getter = null;
        }
    }

    //-----------------------------------------------------------------
    // MapperListener Interface
    //-----------------------------------------------------------------

	public void mapReceived(Icon map)
	{
		setLabel(map);
	}

	public void exceptionOccurred(Exception e)
	{
		setLabel("Was unable to get the map.");
	}

    /**
     * The layout manager for laying out NavBoxes in a vertical fashion (only)
     *
     * @author rods
     *
     */
    class LocalityMapperLayoutManager implements LayoutManager
    {
    	protected final int                   gap = 5;
    	protected final LocalityMapperSubPane parent;
        protected final JLabel                titleLbl;
        protected final JLabel                label;
        protected final JPanel                controlBar;
    	protected final MultiView             form;

    	protected Dimension                   preferredSize = new Dimension(100,100);

        /**
         * Contructs a layout manager for layting out NavBoxes. It lays out all the NavBoxes vertically
         * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
         * aroound all the boxes
         * @param borderPadding the margin around the boxes
         * @param ySeparation the vertical separation inbetween the boxes.
         */
        public LocalityMapperLayoutManager(final LocalityMapperSubPane parent,
                                           final JLabel titleLbl,
                                           final JLabel label,
                                           final JPanel controlBar,
                                           final MultiView form)
        {
        	this.parent     = parent;
            this.label      = label;
            this.titleLbl = titleLbl;
            this.controlBar = controlBar;
            this.form       = form;

        	parent.add(label);
            parent.add(form);
            parent.add(titleLbl);
            parent.add(controlBar);
        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
         */
        public void addLayoutComponent(String arg0, Component arg1)
        {
               //throw new NullPointerException("In addLayoutComponent");
        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
         */
        public void removeLayoutComponent(Component arg0)
        {
            //throw new NullPointerException("In removeLayoutComponent");
        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
         */
        public Dimension preferredLayoutSize(Container arg0)
        {
        	//Dimension size     = arg0.getSize();
        	Dimension formSize = form.getPreferredSize();
        	return new Dimension(formSize.width+200, formSize.height+200);
        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
         */
        public Dimension minimumLayoutSize(Container arg0)
        {
        	Dimension size     = arg0.getPreferredSize();
        	Dimension formSize = form.getPreferredSize();

        	int w = size.width - formSize.width - (3 * gap);
        	int h = size.height - formSize.height - (2*gap);
        	return new Dimension(w, h);
        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
         */
        public void layoutContainer(Container arg0)
        {
        	Dimension size      = arg0.getSize();
        	Dimension formSize  = form.getPreferredSize();
        	//Dimension labelSize = label.getPreferredSize();


        	if (size.width > formSize.width && size.height > formSize.height)
        	{
                //preferredSize.setSize(size.width - formSize.width - (3 * gap), size.height - formSize.height - (2*gap));
                preferredSize.setSize(size.width - formSize.width - (3 * gap), formSize.height);
                //preferredSize.setSize(300, 250); // XXX

                int formY = (size.height - formSize.height) / 2;
        		form.setLocation((size.width - formSize.width)-gap, formY);
        		//System.out.println("1 formSize: "+formSize);
        		form.setSize(formSize);
        		form.setVisible(true);

                Dimension compSize = titleLbl.getPreferredSize();
                titleLbl.setBounds((size.width - compSize.width) / 2, (formY - compSize.height) / 2, compSize.width, compSize.height);

                //label.setLocation((size.width - (preferredSize.width + (gap * 2))) / 2, (size.height - preferredSize.height)/2);
                int labelX = gap;
                int labelY = (size.height - preferredSize.height)/2;
                label.setLocation(labelX, labelY);

        		//System.out.println("2 label preferredSize "+preferredSize);
        		label.setSize(preferredSize);
                localityMapper.setMaxMapWidth(preferredSize.width);
                localityMapper.setMaxMapHeight(preferredSize.height);

                // XXXX DEBUG
                //localityMapper.setPreferredMapWidth(300);
                //localityMapper.setPreferredMapHeight(250);


                compSize = controlBar.getPreferredSize();
                controlBar.setBounds(labelX + (preferredSize.width - compSize.width) / 2, labelY + preferredSize.height + gap, compSize.width, compSize.height);

        	} else
        	{
        		preferredSize.setSize(size.width - (2 * gap), size.height - (2*gap));
        		label.setLocation(0,0);
        		label.setSize(preferredSize);
        		form.setVisible(false);
        	}
        }
    }

	public void timingEvent(long arg0, long arg1, float arg2)
	{
		this.repaint();
	}

	public void begin()
	{
	}

	public void end()
	{
	}
}
