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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Mar 17, 2011
 *
 */
@SuppressWarnings("serial")
public class SGRTask extends BaseTask
{
    //private static final Logger log = Logger.getLogger(SGRTask.class);
    public static final int    GLASSPANE_FONT_SIZE   = 20;
    public static final String STATISTICS            = "Statistics"; //$NON-NLS-1$
    
    // Static Data Members
    public static final DataFlavor TOOLS_FLAVOR = new DataFlavor(SGRTask.class, "SGRTask");
    public static final String SGR = "SGR";

    public static final String EXPORT_RS     = "ExportRecordSet";
    public static final String EXPORT_LIST   = "ExportList";
    public static final String EXPORT_JTABLE = "ExportJTable";

    // Data Members
    protected Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    protected ToolBarDropDownBtn      toolBarBtn       = null;
    
    /**
     * A {@link Vector} or the registered export formats/targets.
     */
    protected Vector<NavBoxItemIFace> toolsNavBoxList    = new Vector<NavBoxItemIFace>();
    
    /**
     * Constructor.
     */
    public SGRTask()
    {
        super(SGR, getResourceString("SGR"));
        
        CommandDispatcher.register(SGR, this);
        //CommandDispatcher.register(PreferencesDlg.PREFERENCES, this);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isSingletonPane()
     */
    public boolean isSingletonPane()
    {
        return true;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    @Override
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            // create an instance of each registered exporter
            toolsNavBoxList.clear();

            // if visible, create a nav box button for each exporter
            if (isVisible)
            {
                extendedNavBoxes.clear();
                NavBox navBox = new NavBox(getResourceString("Actions"));
                
                navBox.add(NavBox.createBtnWithTT(getResourceString("Upload"), "Upload", "", IconManager.STD_ICON_SIZE, new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        uploadCollection();
                    }
                }));
                
                navBox.add(NavBox.createBtnWithTT(getResourceString("Process"), "SGR", "", IconManager.STD_ICON_SIZE, new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        processCollection();
                    }
                })); 
                navBoxes.add(navBox);
                
                navBox.add(NavBox.createBtnWithTT(getResourceString(STATISTICS), STATISTICS, "", IconManager.STD_ICON_SIZE, new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        sgrStats();
                    }
                })); 
                navBoxes.add(navBox);
            }
        }
    }
    
    /**
     * 
     */
    private void uploadCollection()
    {
        doWork("Uploading Collection...", "Collection Upload Complete.");
    }
    
    /**
     * 
     */
    private void doWork(final String procMsg, final String finiMsg)
    {
        final String TIME = "time";
        
        final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(procMsg, GLASSPANE_FONT_SIZE);
        
        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
        {
            int progress = 0;
            
            @Override
            protected Integer doInBackground() throws Exception
            {
                try
                {
                    while (progress < 100)
                    {
                        Thread.sleep(200);
                        progress += 3;
                        firePropertyChange(TIME, 0, progress);
                    }
                    
                } catch (Exception ex) {}
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                UIRegistry.clearSimpleGlassPaneMsg();
            }
        };
        worker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (TIME.equals(evt.getPropertyName())) 
                        {
                            int value = (Integer)evt.getNewValue();
                            glassPane.setProgress(value);
                        }
                    }
                });
        worker.execute();
    }
    
    /**
     * 
     */
    private void processCollection()
    {
        doWork("Analyizing Collection...", "Collection Analyzed.");
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    private void sgrStats()
    {
        try
        {
            // create a dataset...
            String cat = "";
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            File histoDataFile = new File("demo_files/sgr_histo.dat");
            if (!histoDataFile.exists())
            {
                return;
            }
            
            int cnt = 0;
            for (String line : (List<String>)FileUtils.readLines(histoDataFile))
            {
                String[] tokens = StringUtils.split(line, ',');
                dataset.addValue(Double.parseDouble(tokens[3]), tokens[0], cat);
                cnt++;
            }
    
            // create the chart...
            JFreeChart chart = ChartFactory.createBarChart3D(
                    "Quality of Matches",      // chart title
                    "Bins",                     // domain axis label
                    "Number of Matches",       // range axis label
                    dataset,    // data
                    PlotOrientation.VERTICAL,
                    true,       // include legend
                    true,       // tooltips?
                    false       // URLs?
                );
            
            final CategoryItemRenderer renderer = new CustomRenderer(cnt);
            chart.getCategoryPlot().setRenderer(renderer);
            
            // create and display a frame...
            ChartPanel chartPanel = new ChartPanel(chart, true, true, true, true, true);
            SimpleDescPane pane = new SimpleDescPane("SGR Stats", this, new ChartBoundingPanel(chartPanel));
            
            addSubPaneToMgr(pane);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#requestContext()
     */
    public void requestContext()
    {
        ContextMgr.requestContext(this);

        if (starterPane == null)
        {
            super.requestContext();
            
        } else
        {
            SubPaneMgr.getInstance().showPane(starterPane);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    public void subPaneRemoved(final SubPaneIFace subPane)
    {
        super.subPaneRemoved(subPane);
        
        if (subPane == starterPane)
        {
            starterPane = null;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        if (starterPane == null)
        {
            starterPane = StartUpTask.createFullImageSplashPanel(title, this);
        }

        return starterPane;
    }


    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getNavBoxes()
     */
    @Override
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        RecordSetTask rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);
        List<NavBoxIFace> nbs = rsTask.getNavBoxes();
        if (nbs != null)
        {
            extendedNavBoxes.addAll(nbs);
        }
        
        return extendedNavBoxes;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        String label    = getResourceString("SGR");
        String hint     = getResourceString("export_hint");
        toolBarBtn      = createToolbarButton(label, iconName, hint);
        
        toolbarItems = new Vector<ToolBarItemDesc>();
        String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
        if (AppPreferences.getRemote().getBoolean("ExportTask.OnTaskbar"+"."+ds, false))
        {
            toolbarItems.add(new ToolBarItemDesc(toolBarBtn));
        }
        return toolbarItems;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        menuItems = new Vector<MenuItemDesc>();
        
        if (AppPreferences.getLocalPrefs().getBoolean("SRG_PLUGIN", false))
        {
            String menuDesc = "Specify.SYSTEM_MENU";
            
            if (permissions == null || permissions.canModify())
            {
                String    menuTitle = "SGRTask.PLUGIN_MENU"; //$NON-NLS-1$
                String    mneu      = "SGRTask.PLUGIN_MNEU"; //$NON-NLS-1$
                String    desc      = "SGRTask.PLUGIN_DESC"; //$NON-NLS-1$
                JMenuItem mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
                mi.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent ae)
                    {
                        SGRTask.this.requestContext();
                    }
                });
                MenuItemDesc miDesc = new MenuItemDesc(mi, menuDesc);
                miDesc.setPosition(MenuItemDesc.Position.Bottom);
                miDesc.setSepPosition(MenuItemDesc.Position.Before);
                menuItems.add(miDesc);
            }
        }
        
        return menuItems;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    @Override
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /**
     * @param cmdAction the command to be processed
     */
    protected void processToolRecordSet(final CommandAction cmdAction)
    {
        /*RecordSetToolsIFace tool = getTool(cmdAction);

        if (tool != null)
        {
            Object data = cmdAction.getData();
            
            if (data instanceof CommandAction && ((CommandAction)data) == cmdAction) // means it was clicked on
            {
                RecordSetTask          rsTask       = (RecordSetTask)TaskMgr.getTask(RecordSetTask.RECORD_SET);
                List<RecordSetIFace>   colObjRSList = rsTask.getRecordSets(CollectionObject.getClassTableId());
                
                // XXX Probably need to also get RSs with Localisties and or CollectingEvents

                data = getRecordSetOfDataObjs(null, CollectionObject.class, "catalogNumber", colObjRSList.size());
            }
            
            processToolDataFromRecordSet(data, cmdAction.getProperties(), tool);
        }*/
    }
    
    /**
     * @param cmdAction the command to be processed
     */
    protected void processToolList(final CommandAction cmdAction)
    {
        /*RecordSetToolsIFace tool = getTool(cmdAction);
        
        if (tool != null)
        {
            //processToolDataFromList(cmdAction.getData(), cmdAction.getProperties(), tool);
        }*/
    }
    
    /**
     * 
     */
    protected void prefsChanged(final CommandAction cmdAction)
    {
        AppPreferences appPrefs = (AppPreferences)cmdAction.getData();
        
        if (appPrefs == AppPreferences.getRemote())
        {
            // Note: The event send with the name of pref from the form
            // not the name that was saved. So we don't need to append the discipline name on the end
            Object value = cmdAction.getProperties().get("Exporttask.OnTaskbar");
            if (value != null && value instanceof Boolean)
            {
                /*
                 * This doesn't work because it isn't added to the Toolbar correctly
                 * */
                JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
                
                Boolean isChecked = (Boolean)value;
                if (isChecked)
                {
                    TaskMgr.addToolbarBtn(toolBarBtn, toolBar.getComponentCount()-1);
                } else
                {
                    TaskMgr.removeToolbarBtn(toolBarBtn);
                }
                toolBar.validate();
                toolBar.repaint();
                 
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return new BasicPermisionPanel(SGR, "ENABLE", null, null, null);
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(SGR))
        {
            if (cmdAction.isAction(EXPORT_RS))
            {
                processToolRecordSet(cmdAction);
            }
            else if (cmdAction.isAction(EXPORT_LIST))
            {
                processToolList(cmdAction);
                
            }
        } else if (cmdAction.isType(PreferencesDlg.PREFERENCES))
        {
            prefsChanged(cmdAction);
        } 
    }
    
    //---------------------------------------------------------------------------------------------------
    //--- 
    //---------------------------------------------------------------------------------------------------
    class ChartBoundingPanel extends JPanel implements ChartMouseListener
    {
        private ChartPanel  chartPanel;
        private Rectangle   rect = null;
        
        private Rectangle[]          boundings     = null;
        private CategoryItemEntity[] currEntities  = null;
        private ImageIcon            thumb         = IconManager.getIcon("TTV_ToParent");
        private BasicStroke          lineStroke    = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        
        private CategoryItemEntity            currEntity         = null;
        private Integer                       currEntityIndex    = null;
        private ArrayList<CategoryItemEntity> entities           = null;
        
        private int                           maxHeight          = 0;
        private int                           maxY               = 0;
        
        /**
         * 
         */
        public ChartBoundingPanel(final ChartPanel chartPanel)
        {
            super(new BorderLayout());
            this.chartPanel = chartPanel;
            
            add(chartPanel, BorderLayout.CENTER);
            
            chartPanel.addChartMouseListener(this);
            
            
            chartPanel.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    //System.out.println("mousePressed        " + currEntityIndex+"  "+e.getPoint());
                    
                    if (currEntityIndex != null)
                    {
                        //System.err.println("Setting Index: "+currEntityIndex+"  to "+currEntity.hashCode());
                        
                        currEntities[currEntityIndex] = currEntity;
                        currEntityIndex    = null;
                        
                    } else
                    {
                        
                        if (currEntity != null)
                        {
                            for (int i=0;i<currEntities.length;i++)
                            {
                                System.out.println(i+"  "+currEntities[i].hashCode());
                            }
                            
                            Number val2 = currEntity.getDataset().getValue(currEntity.getRowKey(), currEntity.getColumnKey());
                            for (int i=0;i<currEntities.length;i++)
                            {
                                Number val1 = currEntities[i].getDataset().getValue(currEntities[i].getRowKey(), currEntities[i].getColumnKey());
                                //System.out.println(i+" "+currEntity+"  "+currEntities[i]);
                                //System.out.println(i+" "+val1+"  "+val2);
                                if (val1.doubleValue() == val2.doubleValue())
                                //if (currEntity.equals(currEntities[i]))
                                {
                                    //System.out.println("FND: "+i+" "+currEntity.hashCode()+"  "+currEntities[i].hashCode());
                                    currEntityIndex    = i;
                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }
        
        protected Rectangle adjustRect(final Rectangle rect)
        {
            //Rectangle2D r2d  = chartPanel.getScreenDataArea();
            //Rectangle2D r2dX = chartPanel.getChartRenderingInfo().getPlotInfo().getPlotArea();
            //int x = (int)r2d.getX() + (int)r2dX.getX() + (int)(rect.getWidth() / 2);
            
            int x = rect.x + (int)(rect.getWidth() / 2);
            x = (int) ((double)x * chartPanel.getScaleX());
            
            //System.out.println(rect);
            
            rect.x      = x;
            rect.width  = (int)((double)rect.width * chartPanel.getScaleX());
            
            rect.y      = (int)((double)rect.y * chartPanel.getScaleY());
            rect.height = (int)((double)rect.height * chartPanel.getScaleY());
            //System.err.println(rect+"\n");
            return rect;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        @Override
        public void paint(Graphics g)
        {
            super.paint(g);
            
            if (rect != null)
            {
                Graphics2D g2d = (Graphics2D)g;
                g2d.setColor(Color.RED);
                
                adjustRect(rect);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setStroke(lineStroke);
                
                if (boundings == null)
                {
                    boundings = new Rectangle[4];
                    
                    if (entities == null)
                    {
                        entities = new ArrayList<CategoryItemEntity>();
                        EntityCollection         entCol   = chartPanel.getChartRenderingInfo().getEntityCollection();
                        for (int i=0;i<entCol.getEntityCount();i++)
                        {
                            ChartEntity ce = (ChartEntity)entCol.getEntity(i);
                            if (ce instanceof CategoryItemEntity)
                            {
                                CategoryItemEntity cie = (CategoryItemEntity)ce;
                                Rectangle r = adjustRect(cie.getArea().getBounds());
                                if (r.height > maxHeight)
                                {
                                    maxHeight =(int)cie.getArea().getBounds().getHeight();
                                    maxY      = r.y;
                                }
                                entities.add(cie);
                            }
                        }
                    }
                    
                    int half = (entities.size() - 1) / 2;
                    boundings[0] = getEntityPoint(entities, 0);
                    boundings[1] = getEntityPoint(entities, half);
                    boundings[2] = getEntityPoint(entities, half+1);
                    boundings[3] = getEntityPoint(entities, entities.size()-1);
                    
                    int[] inxs = new int[] {0, half, half+1, entities.size()-1};
                    currEntities = new CategoryItemEntity[4];
                    for (int i=0;i<inxs.length;i++)
                    {
                        currEntities[i] = entities.get(inxs[i]);
                        //System.out.println("START: "+i+"  "+currEntities[i]);
                    }
                }
                
                for (Rectangle r : boundings)
                {
                    g.drawImage(thumb.getImage(), r.x-(thumb.getIconWidth()/2)+2, r.y+r.height-thumb.getIconHeight(), null);
                }
                
                int x = (int)((double)boundings[0].x * 1);//chartPanel.getScaleX());
                int w = (int)((double)(boundings[1].x - boundings[0].x) * 1);//chartPanel.getScaleX());
                
                //int y = (int)((double)maxY * chartPanel.getScaleY());
                int h = (int)((double)maxHeight * chartPanel.getScaleY());

                g2d.setColor(new Color(255, 255, 255, 64));
                g2d.fillRect(x+2, maxY, w, h);
                
                x = (int)((double)boundings[2].x * 1);//chartPanel.getScaleX());
                w = (int)((double)(boundings[3].x - boundings[2].x) * 1);//chartPanel.getScaleX());
                
                g2d.setColor(new Color(255, 255, 255, 64));
                g2d.fillRect(x+2, maxY, w, h);
                
            }
        }
        
        /**
         * @param entities
         * @param index
         * @return
         */
        private Rectangle getEntityPoint(final ArrayList<CategoryItemEntity> entities, final int index)
        {
            CategoryItemEntity cie = entities.get(index);
            Rectangle          r   = (Rectangle)cie.getArea().getBounds().clone();
            adjustRect(r);
            return r;
        }

        /* (non-Javadoc)
         * @see org.jfree.chart.ChartMouseListener#chartMouseClicked(org.jfree.chart.ChartMouseEvent)
         */
        @Override
        public void chartMouseClicked(ChartMouseEvent ev)
        {
            repaint();
        }

        /* (non-Javadoc)
         * @see org.jfree.chart.ChartMouseListener#chartMouseMoved(org.jfree.chart.ChartMouseEvent)
         */
        @Override
        public void chartMouseMoved(ChartMouseEvent ev)
        {
            currEntity = null;
            ChartEntity ce = ev.getEntity();
            if (ce instanceof CategoryItemEntity)
            {
                CategoryItemEntity cie = (CategoryItemEntity)ce;
                currEntity = cie;
                
                Shape shape = ce.getArea();
                rect = shape.getBounds();
                repaint();
            }
            
            if (currEntityIndex != null && currEntity != null)
            {
                boundings[currEntityIndex] = adjustRect((Rectangle)currEntity.getArea().getBounds().clone());
                //System.out.println("mouseMoved" + boundings[currEntityIndex]);
                repaint();
            }
        }
    }
    
    class CustomRenderer extends BarRenderer3D {

        /** The colors. */
        private Paint[] colors;

        /**
         * Creates a new renderer.
         *
         * @param colors  the colors.
         */
        public CustomRenderer(final int numColorsArg) 
        {
            int numColors = numColorsArg + 1;
            BufferedImage image = new BufferedImage(numColors, 2, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = (Graphics2D)image.getGraphics();
            
            int halfBW = numColors / 2;
            GradientPaint bg = new GradientPaint(new Point(0, 0), Color.RED, new Point(halfBW,0), Color.YELLOW);            
            g2.setPaint(bg);
            
            g2.fillRect(0, 0, halfBW, 2);
            
            // Second Half
            bg = new GradientPaint(new Point(halfBW, 0), Color.YELLOW,
                                   new Point(numColors, 0), Color.GREEN);
            g2.setPaint(bg);
            g2.fillRect(halfBW, 0, halfBW, 2);
            
            colors = new Paint[numColors];
            for (int i=0;i<numColors;i++)
            {
                Color c = new Color(image.getRGB(i, 0));
                //System.out.println(i+" - "+c);
                colors[i] = c;
            }
        }

        /**
         * Returns the paint for an item.  Overrides the default behaviour inherited from
         * AbstractSeriesRenderer.
         *
         * @param row  the series.
         * @param column  the category.
         *
         * @return The item color.
         */
        public Paint getItemPaint(final int row, final int column) 
        {
            return this.colors[row % this.colors.length];
        }
    }
}