/* Filename:    $RCSfile: SimpleDescPane.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.extras.FishBaseInfoGetter;
import edu.ku.brc.specify.extras.FishBaseInfoGetterListener;
import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.tasks.services.LocalityMapper;
import edu.ku.brc.specify.ui.ImageDisplay;
import edu.ku.brc.specify.ui.forms.FormViewObj;
import edu.ku.brc.specify.ui.forms.MultiView;
import edu.ku.brc.specify.ui.forms.ViewMgr;
import edu.ku.brc.specify.ui.forms.persist.AltView;
import edu.ku.brc.specify.ui.forms.persist.View;

/**
 * A default pane for display a simple label telling what it is suppose to do
 * 
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class LocalityMapperSubPane extends BaseSubPane implements LocalityMapper.MapperListener
{
    //private static Log log = LogFactory.getLog(SimpleDescPane.class);
    protected SimpleDateFormat scrDateFormat = PrefsCache.getSimpleDateFormat("ui", "formatting", "scrdateformat");

    protected LocalityMapper                  localityMapper = new LocalityMapper();
    protected JLabel                          imageLabel     = new JLabel("Loading Image...");
    protected MultiView                       multiView;
    
    protected List<Object>                    collectingEvents;
    protected List<Hashtable<String, Object>> valueList   = new ArrayList<Hashtable<String, Object>>();
    protected List<Rectangle>                 markerRects = new ArrayList<Rectangle>();
    protected boolean                         dirty       = false;
    
    protected List<ImageGetter>               imageGetterList = new ArrayList<ImageGetter>();
    protected Hashtable<String, Image>        imageMap        = new Hashtable<String, Image>();
    protected FormViewObj                     formViewObj;
    protected JList                           imageJList;

    /**
     * 
     *
     */
    public LocalityMapperSubPane(final String name, 
                                 final Taskable task,
                                 final List<Object> collectingEvents)
    {
        super(name, task);
        
        setBackground(Color.WHITE);
        
        this.collectingEvents = collectingEvents;
        
        Vector<Locality> localities = new Vector<Locality>();
        Vector<String>   labels     = new Vector<String>();
        for (Object obj : collectingEvents)
        {
        	CollectingEvent collectingEvent = (CollectingEvent)obj;
        	Hashtable<String, Object> map = new Hashtable<String, Object>();
        	
        	Set<CollectionObject> colObjs = collectingEvent.getCollectionObjects();
         	
        	map.put("startDate", collectingEvent.getStartDate());
        	map.put("endDate", collectingEvent.getEndDate());
        	
        	Set<Object> taxonNames = new HashSet<Object>();
        	for (CollectionObject co : colObjs)
        	{
        		for (Determination d : co.getDeterminations())
        		{
        			if (d.getIsCurrent())
        			{
        				//System.out.println(d.getTaxon().getName() + "("+co.getCountAmt()+")");
        				Taxon taxon = d.getTaxon();
        				if (taxon != null)
        				{
        					taxonNames.add(taxon.getName() + (co.getCountAmt() != null ? "("+co.getCountAmt()+")" : ""));
        					if (taxon.getRankId() == 220)
        					{
        						Taxon genus = taxon.getParent();
        						if (genus.getRankId() == 180)
        						{
        							ImageGetter imgGetter = new ImageGetter(imageGetterList, imageMap, genus.getName(), taxon.getName());
        							imageGetterList.add(imgGetter);
        						}
        					}
        				}
        				break;
        			}
        		}
        	}
        	map.put("taxonItems", taxonNames);
        	
        	Locality locality = collectingEvent.getLocality();
        	if (locality != null && locality.getLatitude1() != null && locality.getLongitude1() != null)
        	{
            	map.put("latitude1", locality.getLatitude1());
            	map.put("longitude1", locality.getLongitude1());

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
	        	localities.add(locality);
	        	valueList.add(map);
        	}	
        }
        
        //localityMapper.setPreferredMapWidth(600);
        //localityMapper.setPreferredMapHeight(500);
        
        localityMapper.setPreferredMapWidth(300);
        localityMapper.setPreferredMapHeight(250);
        
        Color arrow = new Color(220,220,220);
        localityMapper.setArrowColor(arrow);
        localityMapper.setDotColor(Color.WHITE);
        localityMapper.setDotSize(4);
        localityMapper.setLabelColor(Color.YELLOW);
        
        int inx = 0;
        for (Locality locality : localities)
        {
            localityMapper.addLocalityAndLabel(locality, labels != null ? labels.get(inx) : null);
            inx++;
        }
        
        //PanelBuilder    builder    = new PanelBuilder(new FormLayout("p:g,p,p:g,p,p:g", "f:p:g"), this);
        //CellConstraints cc         = new CellConstraints();
        //builder.add(imageLabel, cc.xy(2,1));
        
        // XXX DEMO 
        View view = ViewMgr.getView("Main Views", "LocalityMapper");
        multiView = new MultiView(null, view, AltView.CreationMode.View, false, false);
        //builder.add(multiView, cc.xy(4,1));
        multiView.setBackground(Color.WHITE);
        
        formViewObj = (FormViewObj)multiView.getCurrentView();
        formViewObj.getUIComponent().setBackground(Color.WHITE);
         
        imageJList = (JList)formViewObj.getComp("taxonItems");
        imageJList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                	String name = (String)imageJList.getSelectedValue();
                	ImageDisplay imgDisplay = (ImageDisplay)formViewObj.getComp("image");
                	Image img = imageMap.get(name);
                	if (img != null)
                	{
                		imgDisplay.setImage(new ImageIcon(img));
                	} else
                	{
                		imgDisplay.setImage(null);
                	}
                }
            }});
        
        setLayout(new LocalityMapperLayoutManager(this, imageLabel, multiView));
        
        addMouseMotionListener(new MouseMotionListener()
			{
				public void mouseDragged(MouseEvent e)
				{}
				public void mouseMoved(MouseEvent e)
				{
					checkMouseLocation(e.getPoint());
				}
			});

        localityMapper.getMap(this);
    }
    
    protected void setLabel(final Icon imageIcon)
    {
        imageLabel.setText(null);
        imageLabel.setIcon(imageIcon);
        
        dirty = true;
    }

    protected void setLabel(final String msg)
    {
        imageLabel.setIcon(null);
        imageLabel.setText(msg);
    }
    
    /**
     * @param mousePnt
     */
    public void checkMouseLocation(final Point mousePnt)
    {
    	if (dirty)
    	{
    		markerRects.clear();
    		Point pnt = imageLabel.getLocation();
    		//System.out.println("***************** "+pnt+"  "+imageLabel.getBounds()+"  "+imageLabel.getParent()+"  "+this);
            for (Point p : localityMapper.getMarkerLocations())
            {
            	//System.out.println("*** "+p);
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
    			Hashtable<String, Object> map = valueList.get(inx);
    			multiView.setData(map);
    			return;
    		}
    		inx++;
        }
    	//multiView.setData(null);
    	
    }
    
    

 
    //------------------------------------------------------------------------
    //-- Inner Classes
    //------------------------------------------------------------------------
    
    class ImageGetter implements FishBaseInfoGetterListener
    {
    	protected FishBaseInfoGetter getter;
    	protected List<ImageGetter> list;
    	protected Hashtable<String, Image> map;
    	protected String name;
    	
    	public ImageGetter(final List<ImageGetter> list, 
    			           final Hashtable<String, Image> map, 
    			           final String genus,
    			           final String species)
    	{
    		this.list = list;
    		this.map  = map;
    		this.name = species;
    		
    		getter = new FishBaseInfoGetter(this, FishBaseInfoGetter.InfoType.Thumbnail, genus, species);
    		getter.start();
    	}
    	
        public void infoArrived(FishBaseInfoGetter getter)
        {
        	System.out.println("["+name+"]["+getter.getImage()+"]");
        	if (getter.getImage() != null)
        	{
        		map.put(name, getter.getImage());
        	}
        	cleanUp();
        }

        public void infoGetWasInError(FishBaseInfoGetter getter)
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
    	protected final JLabel                label;
    	protected final MultiView             form;

    	protected Dimension                   preferredSize = new Dimension(100,100);
    	
        /**
         * Contructs a layout manager for layting out NavBoxes. It lays out all the NavBoxes vertically
         * and uses the 'ySeparator' as the spacing in between the boxes. It uses borderPadding as a 'margin'
         * aroound all the boxes
         * @param borderPadding the margin around the boxes
         * @param ySeparation the vertical separation inbetween the boxes.
         */
        public LocalityMapperLayoutManager(final LocalityMapperSubPane parent, final JLabel label, final MultiView form)
        {
        	this.parent = parent;
        	this.label  = label;
        	this.form   = form;
        	
        	parent.add(label);
        	parent.add(form);
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
        		preferredSize.setSize(size.width - formSize.width - (3 * gap), size.height - formSize.height - (2*gap));
        		preferredSize.setSize(300, 250); // testing
        		
        		form.setLocation((size.width - formSize.width)-gap, (size.height - formSize.height)/2);
        		System.out.println("1 "+formSize);
        		form.setSize(formSize);
        		form.setVisible(true);
        		
        		label.setLocation(gap, (size.height - preferredSize.height)/2);
        		System.out.println("2 "+preferredSize);
        		label.setSize(preferredSize);
        		
        	} else
        	{
        		preferredSize.setSize(size.width - (2 * gap), size.height - (2*gap));
        		label.setLocation(0,0);
        		label.setSize(preferredSize);
        		form.setVisible(false);
        	}
        }
    }

}
