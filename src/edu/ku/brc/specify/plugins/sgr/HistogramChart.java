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
package edu.ku.brc.specify.plugins.sgr;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.sgr.datamodel.BatchMatchResultSet;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: Apr 21, 2011
 *
 */
public class HistogramChart extends BaseSubPane
{
    final SwingWorker<Void, Void>     chartUpdater;
    final private BatchMatchResultSet resultSet;

    public HistogramChart(String name, Taskable task, BatchMatchResultSet resultSet, 
                         final SGRBatchScenario scenario, float binSize)
    {
        super(name, task);
        this.resultSet = resultSet;
        
        update();
        
        chartUpdater = new SwingWorker<Void, Void>()
        {              
            @Override
            protected Void doInBackground() throws Exception
            {
                Thread.currentThread().setName("Histogram-Updater");
                while (true)
                {
                    try { if (!scenario.isRunning()) return null; }
                    catch (NullPointerException e) { return null; }
                    
                    Thread.sleep(2000);
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            update();
                        }
                    });
                }
            }
        };
        
        chartUpdater.execute();
    }
    
    private void update()
    {
        UIRegistry.loadAndPushResourceBundle("specify_plugins");

        HistogramDataset dataSet = new HistogramDataset();
        double max = resultSet.getMax();

        dataSet.addSeries(getResourceString("SGR_HISTO_DATASERIES"), 
                resultSet.getValues(1.0), 40, 0.0, max);

        JFreeChart chart = ChartFactory.createHistogram(
                getResourceString("SGR_HISTO_TITLE"),
                getResourceString("SGR_HISTO_X_AXIS"),
                getResourceString("SGR_HISTO_Y_AXIS"),
                dataSet,
                PlotOrientation.VERTICAL,
                false,       // include legend
                true,        // tooltips?
                false        // URLs?
        );
  
      UIRegistry.popResourceBundle();

      XYPlot plot = chart.getXYPlot();
      CustomRenderer.setDefaultBarPainter(new StandardXYBarPainter());
      CustomRenderer renderer = new CustomRenderer(dataSet);
      plot.setRenderer(renderer);
      renderer.setMargin(0.1);
      
      ChartPanel chartPanel = new ChartPanel(chart, true, true, true, true, true);
      removeAll();
      add(chartPanel);
      validate();
    }
    
    @SuppressWarnings("serial")
    private static class CustomRenderer extends XYBarRenderer {
        private final HistogramDataset data;
        
        public CustomRenderer(HistogramDataset data)
        {
            this.data = data;
        }
        
        @Override
        public Paint getItemPaint(int row, int col)
        {
            double score = data.getStartXValue(row, col);
            double maxScore = data.getStartXValue(row, data.getItemCount(row) - 1);
            float x = (float) (Math.log(score/maxScore + 1) / Math.log(2));
            float h = 130 + 240*(x-1);
            return Color.getHSBColor(h/360, 0.6f, 0.9f);
        }
    }
    
    @SuppressWarnings("serial")
    private static class ChartBoundingPanel extends JPanel implements ChartMouseListener
    {
        private ChartPanel                    chartPanel;
        private Rectangle                     rect            = null;

        private Rectangle[]                   boundings       = null;
        private CategoryItemEntity[]          currEntities    = null;
        private ImageIcon                     thumb           = IconManager.getIcon("TTV_ToParent");
        private BasicStroke                   lineStroke      = new BasicStroke(2.0f,
                                                                      BasicStroke.CAP_ROUND,
                                                                      BasicStroke.JOIN_ROUND);

        private CategoryItemEntity            currEntity      = null;
        private Integer                       currEntityIndex = null;
        private ArrayList<CategoryItemEntity> entities        = null;

        private int                           maxHeight       = 0;
        private int                           maxY            = 0;
        
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
                    if (currEntityIndex != null)
                    {
                        currEntities[currEntityIndex] = currEntity;
                        currEntityIndex = null;
                    } else
                    {
                        if (currEntity != null)
                        {
                            for (int i = 0; i < currEntities.length; i++)
                            {
                                System.out.println(i + "  " + currEntities[i].hashCode());
                            }

                            Number val2 = currEntity.getDataset().getValue(currEntity.getRowKey(),
                                    currEntity.getColumnKey());
                            for (int i = 0; i < currEntities.length; i++)
                            {
                                Number val1 = currEntities[i].getDataset()
                                        .getValue(currEntities[i].getRowKey(),
                                                currEntities[i].getColumnKey());
                                if (val1.doubleValue() == val2.doubleValue())
                                {
                                    currEntityIndex = i;
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
            int x = rect.x + (int)(rect.getWidth() / 2);
            x = (int) ((double)x * chartPanel.getScaleX());
            rect.x      = x;
            rect.width  = (int)((double)rect.width * chartPanel.getScaleX());
            
            rect.y      = (int)((double)rect.y * chartPanel.getScaleY());
            rect.height = (int)((double)rect.height * chartPanel.getScaleY());
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
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setStroke(lineStroke);
                
                if (boundings == null)
                {
                    boundings = new Rectangle[4];
                    
                    if (entities == null)
                    {
                        entities = new ArrayList<CategoryItemEntity>();
                        EntityCollection entCol = 
                            chartPanel.getChartRenderingInfo().getEntityCollection();

                        for (int i = 0; i < entCol.getEntityCount(); i++)
                        {
                            ChartEntity ce = (ChartEntity) entCol.getEntity(i);
                            if (ce instanceof CategoryItemEntity)
                            {
                                CategoryItemEntity cie = (CategoryItemEntity) ce;
                                Rectangle r = adjustRect(cie.getArea().getBounds());
                                if (r.height > maxHeight)
                                {
                                    maxHeight = (int) cie.getArea().getBounds().getHeight();
                                    maxY = r.y;
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
                    for (int i = 0; i < inxs.length; i++)
                    {
                        currEntities[i] = entities.get(inxs[i]);
                    }
                }
                
                for (Rectangle r : boundings)
                {
                    g.drawImage(thumb.getImage(), r.x - (thumb.getIconWidth() / 2) + 2, r.y
                            + r.height - thumb.getIconHeight(), null);
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

    @Override
    public void shutdown()
    {
        chartUpdater.cancel(true);
        removeAll();
        super.shutdown();
    }

    public BatchMatchResultSet getResultSet()
    {
        return resultSet;
    }
}
