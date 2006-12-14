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
package edu.ku.brc.stats;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.UICacheManager;

/**
 * This base class implements the Chartable interface
 * that enables derived classes to easily accept and have access to information needed to decorate or describe the chart.
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ChartPanel extends JPanel implements Chartable
{
    // Static Data Members
    //private static final Logger log = Logger.getLogger(ChartPanel.class);

    // Data Members
    protected org.jfree.chart.ChartPanel chartPanel;
    protected String  title       = "";
    protected String  xAxisTitle  = "";
    protected String  yAxisTitle  = "";
    protected boolean isVertical  = true;
    
    protected JProgressBar progressBar;
    protected JLabel       progressLabel;
    private Dimension      maxChartSize = new Dimension(100,100);

    /**
     * @param startUpMsg
     */
    public ChartPanel(final String startUpMsg)
    {
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("max(100px;p):g", "center:p:g, center:p:g"));
        CellConstraints cc         = new CellConstraints();

        builder.add(progressBar,                  cc.xy(1,1));
        builder.add(progressLabel = new JLabel(startUpMsg, JLabel.CENTER), cc.xy(1,2));
        progressBar.setFont(UICacheManager.getFont(JLabel.class));
        progressLabel.setFont(UICacheManager.getFont(JLabel.class));

        PanelBuilder builder2  = new PanelBuilder(new FormLayout("p:g,p,p:g", "p:g,p,p:g"), this);
        builder2.add(builder.getPanel(), cc.xy(2,2));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.Chartable#setTitle(java.lang.String)
     */
    public void setTitle(final String title)
    {
        this.title = title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.Chartable#setXAxis(java.lang.String)
     */
    public void setXAxis(final String title)
    {
        xAxisTitle = title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.Chartable#setYAxis(java.lang.String)
     */
    public void setYAxis(final String title)
    {
       yAxisTitle = title;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.Chartable#setVertical(boolean)
     */
    public void setVertical(boolean isVertical)
    {
        this.isVertical = isVertical;
    }
    
    
    public void setPreferredChartSize(int width, int height)
    {
    	//System.out.println("setPreferredChartSize "+width+"  "+height);
        maxChartSize.setSize(width, height);
    }


    /**
     * The layout manager for laying out NavBoxes in a vertical fashion (only)
     *
     * @author rods
     *
     */
    public class ChartLayoutManager implements LayoutManager, LayoutManager2
    {
    	protected ChartPanel                 parentChartPanel;
    	protected org.jfree.chart.ChartPanel freeChartPanel;
    	protected Dimension                  preferredSize = new Dimension(100,100);
    	
        /**
         * Contructs a layout manager for the ChartPanel
         * @param parentChartPanel the margin around the boxes
         */
        public ChartLayoutManager(ChartPanel parentChartPanel)
        {
        	this.parentChartPanel = parentChartPanel;
        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
         */
        public void addLayoutComponent(String arg0, Component arg1)
        {
            if (arg1 == null || !(arg1 instanceof org.jfree.chart.ChartPanel))
            {
                throw new NullPointerException("Null component in addLayoutComponent");
            }
            freeChartPanel = (org.jfree.chart.ChartPanel)arg1;

        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
         */
        public void removeLayoutComponent(Component arg0)
        {
            if (arg0 == null || !(arg0 instanceof org.jfree.chart.ChartPanel))
            {
                throw new NullPointerException("Null component in addLayoutComponent");
            }
            //freeChartPanel = (org.jfree.chart.ChartPanel)arg0;
        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
         */
        public Dimension preferredLayoutSize(Container arg0)
        {
        	//System.out.println("preferredLayoutSize "+parentChartPanel.maxChartSize);
        	return parentChartPanel.maxChartSize;
            //return new Dimension(preferredSize);
        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
         */
        public Dimension minimumLayoutSize(Container arg0)
        {
        	//System.out.println("minimumLayoutSize "+parentChartPanel.maxChartSize);
        	return parentChartPanel.maxChartSize;
             //return new Dimension(preferredSize);
        }

        /* (non-Javadoc)
         * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
         */
        public void layoutContainer(Container arg0)
        {
            Dimension parentSize =  arg0.getSize();
        	//System.out.println("parentSize "+parentSize);

            //preferredSize.setSize(parentSize.width, parentSize.height-5);
            freeChartPanel.setBounds(0,0,parentSize.width, parentSize.height-5);
        }


        // LayoutManager2
        public void  addLayoutComponent(Component comp, Object constraints)
        {
            if (comp == null || !(comp instanceof org.jfree.chart.ChartPanel))
            {
                throw new NullPointerException("Null component in addLayoutComponent");
            }
            freeChartPanel = (org.jfree.chart.ChartPanel)comp;
        }
        public float   getLayoutAlignmentX(Container target)
        {
            return (float)0.0;
        }
        public float   getLayoutAlignmentY(Container target)
        {
            return (float)0.0;
        }
        public void invalidateLayout(Container target)
        {
            preferredSize.setSize(100, 100);
        }
        public Dimension maximumLayoutSize(Container target)
        {
            return new Dimension(preferredSize);
        }

    }

}
