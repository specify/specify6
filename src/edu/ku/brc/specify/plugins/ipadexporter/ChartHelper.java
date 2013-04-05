package edu.ku.brc.specify.plugins.ipadexporter;
import static edu.ku.brc.ui.UIHelper.getInt;
import static edu.ku.brc.ui.UIHelper.getString;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

/* Copyright (C) 2012, University of Kansas Center for Research
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

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Apr 18, 2012
 *
 */
public class ChartHelper
{
    private double numMin = Double.MAX_VALUE;
    private double numMax = Double.MIN_VALUE;
    
    /**
     * 
     */
    public ChartHelper()
    {
        super();
    }

    /**
     * @param list
     * @param title
     * @param xAxisTitle
     * @param yAxisTitle
     * @param isVertical
     * @param width
     * @param height
     */
    public JFreeChart createLineChart(final List<Object> list, 
                                      final String title, 
                                      final String xAxisTitle, 
                                      final String yAxisTitle,
                                      final boolean isVertical,
                                      final int width, 
                                      final int height)
    {
        DefaultCategoryDataset catDataSet = new DefaultCategoryDataset();
        for (int i=0;i<list.size();i++)
        {         
            Object xVal  = list.get(i++);
            Object yVal  = list.get(i);
            
            double xv = getDbl(xVal);
            numMin = Math.min(numMin, xv);
            numMax = Math.max(numMax, xv);
            
            catDataSet.addValue(getDbl(yVal), "X", xVal.toString());
        }

        /*XYDataset dataset = createXYDataSet(list);
        JFreeChart chart = ChartFactory.createXYLineChart( 
                title,      // chart title 
                xAxisTitle, // domain axis label 
                yAxisTitle, // range axis label 
                dataset,    // data 
                isVertical ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL, 
                false,       // include legend 
                true,       // tooltips? 
                false       // URLs? 
            ); 
        
        XYPlot xyplot = chart.getXYPlot();
        NumberAxis numberAxis = (NumberAxis) xyplot.getDomainAxis();
        numberAxis.setRange(numMin, numMax);
        
        //ValueAxis axis = xyplot.getDomainAxis();
        //axis = xyplot.getRangeAxis();
        //((NumberAxis) axis).setTickUnit(new NumberTickUnit(1));
        //axis.setRange(1870,2010);*/
        
        /*
        JFreeChart jfreechart = ChartFactory.createLineChart3D(
                title, 
                xAxisTitle, 
                yAxisTitle, 
                catDataSet, 
                PlotOrientation.VERTICAL, 
                false, 
                true, 
                false);
        jfreechart.setBackgroundPaint(new Color(187, 187, 221));
        CategoryPlot categoryplot = (CategoryPlot)jfreechart.getPlot();
        
        NumberAxis numberaxis = (NumberAxis)categoryplot.getRangeAxis();
        numberaxis.setAutoRangeIncludesZero(false);
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        CategoryItemRenderer renderer = categoryplot.getRenderer(); 
        renderer.setSeriesPaint(0, new Color(95, 158, 160)); 
        renderer.setSeriesOutlinePaint(0, Color.DARK_GRAY); 
        */
        JFreeChart jfreechart = ChartFactory.createLineChart(
                title, 
                xAxisTitle, 
                yAxisTitle, 
                catDataSet, 
                PlotOrientation.VERTICAL, 
                false, 
                true, 
                false);
        
        CategoryPlot categoryplot = (CategoryPlot)jfreechart.getPlot();
        categoryplot.setRangeGridlinePaint(Color.DARK_GRAY);
        categoryplot.setBackgroundPaint(null);
        
        NumberAxis numberaxis = (NumberAxis)categoryplot.getRangeAxis();
        numberaxis.setAutoRangeIncludesZero(false);
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        CategoryItemRenderer renderer = categoryplot.getRenderer(); 
        renderer.setSeriesPaint(0, new Color(95, 158, 160)); 
        renderer.setSeriesOutlinePaint(0, Color.DARK_GRAY);
        
        BasicStroke   lineStroke = new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        renderer.setBaseStroke(lineStroke);
        renderer.setBaseOutlineStroke(lineStroke);
        renderer.setSeriesStroke(0, lineStroke);
        
        //renderer.setOutlineStroke(lineStroke);
        //renderer.setStroke(lineStroke);
        
        return jfreechart;
    }
    
    /**
     * @param list
     * @param title
     * @param xAxisTitle
     * @param yAxisTitle
     * @param isVertical
     * @param width
     * @param height
     */
    public JFreeChart createBarChart(final List<Object> list, 
                               final String title, 
                               final String xAxisTitle, 
                               final String yAxisTitle,
                               final boolean isVertical,
                               final int width, 
                               final int height)
    {
        DefaultCategoryDataset dataset = createDataSet(list);

        JFreeChart chart = ChartFactory.createBarChart3D( 
                title,      // chart title 
                xAxisTitle, // domain axis label 
                yAxisTitle, // range axis label 
                dataset,    // data 
                isVertical ? PlotOrientation.VERTICAL : PlotOrientation.HORIZONTAL, 
                true,       // include legend 
                true,       // tooltips? 
                false       // URLs? 
            ); 
        
        CategoryPlot categoryplot = (CategoryPlot)chart.getPlot();
        categoryplot.setRangeGridlinePaint(Color.DARK_GRAY);
        categoryplot.setBackgroundPaint(null);

        return chart;
    }
    
    ArrayList<Color> colors = new ArrayList<Color>(12);
    int[] rgbs = new int[] {
            248,215,83,
            92,151,70,
            62,117,167,
            112,101,62,
            225,102,42,  
            116,121,111, 
            196,56,79, 
            };
    int[] rgbs0 = new int[] {
            243,192,28,
            61,129,40,
            32,95,154,
            99,82,43,
            220,83,19,
            93,100,90,
            188,28,57,
            };
    /**
     * @param plot
     * @param dataset
     */
    public void setColors(PiePlot plot, DefaultPieDataset dataset)
    {
        if (colors.size() == 0)
        {
            for (int i=0;i<rgbs.length/3;i++)
            {
                int inx = i * 3;
                colors.add(new Color(rgbs[inx],rgbs[inx+1],rgbs[inx+2]));
            }
        }
        List<?> keys = dataset.getKeys();
        for (int i = 0; i < keys.size(); i++)
        {
            int  inx = i % this.colors.size();
            plot.setSectionPaint((Comparable<?>)keys.get(i), this.colors.get(inx));
            
             //System.out.println("Setting section paint " + keys.get(i).toString() + " " + 
             //  Colors.getColors()[aInt].toString());

        }
    }  
    
    /**
     * @param list
     * @param title
     * @param xAxisTitle
     * @param yAxisTitle
     * @param isVertical
     * @param width
     * @param height
     */
    public JFreeChart createPieChart(final List<Object> list, 
                                     final String title, 
                                     final int width, 
                                     final int height,
                                     final boolean do3D)
    {
        DefaultPieDataset dataset = new DefaultPieDataset(); 
        
        for (int i=0;i<list.size();i++)
        {         
            Object descObj = list.get(i++);
            Object valObj  = list.get(i);
            dataset.setValue(getString(descObj), getInt(valObj));
        }
 
        String adjTitle = title;
        JFreeChart chart = do3D ? ChartFactory.createPieChart3D(adjTitle, dataset, false, false, false) :
                                  ChartFactory.createPieChart(adjTitle, dataset, false, false, false);
        chart.setBackgroundPaint(new Color(0, 0, 0, 0)); // transparent black
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 11)); //$NON-NLS-1$
        plot.setBackgroundPaint(new Color(0, 0, 0, 0)); // transparent black
        plot.setOutlinePaint(null);
        
        setColors(plot, dataset);
        
        return chart;
    }
    
    /**
     * @param list
     * @return
     */
    private DefaultCategoryDataset createDataSet(final List<Object> list)
    {
        String cat = ""; //$NON-NLS-1$
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); 
        
        for (int i=0;i<list.size();i++)
        {         
            Object descObj = list.get(i++);
            Object valObj  = list.get(i);
            dataset.addValue(getInt(valObj), getString(descObj), cat);
        }
        return dataset;
    }
    
    private double getDbl(final Object valObj)
    {
        if (valObj instanceof Integer)
        {
            return ((Integer)valObj).doubleValue();
        } else if (valObj instanceof Long)
        {
            return ((Long)valObj).doubleValue();
        } else if (valObj instanceof Double)
        {
            return ((Double)valObj).doubleValue();
        } else if (valObj instanceof Float)
        {
            return ((Float)valObj).doubleValue();
        } else if (valObj instanceof Short)
        {
            return ((Short)valObj).doubleValue();
        } else if (valObj instanceof String)
        {
            return ((Integer)Integer.parseInt((String)valObj)).doubleValue();
        }
        return 0.0;
    }
    
    /**
     * @param chart
     * @param width
     * @param height
     */
    /*private void createPanelWithChart(final JFreeChart chart,
                                 final int width, 
                                 final int height)
    {
        Color bgColor = new Color(255, 255, 255, 0);
        chart.setBackgroundPaint(bgColor);
        
        // create and display a frame... 
        org.jfree.chart.ChartPanel panel = new org.jfree.chart.ChartPanel(chart, true, true, true, true, true); 
        panel.setMaximumSize(new Dimension(width, height));
        panel.setPreferredSize(new Dimension(width, height));
        panel.setBackground(bgColor);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.CENTER);
    }*/
    
    /**
     * @param outFile
     * @param jfreeChart
     * @param width
     * @param height
     */
    public void createImage(final File outFile, 
                            final JFreeChart jfreeChart,
                            final int width,
                            final int height)
    {
        if (jfreeChart != null)
        {
            BufferedImage bufImage = jfreeChart.createBufferedImage(width, height);
            try
            {
                ImageIO.write(bufImage, "PNG", outFile);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
