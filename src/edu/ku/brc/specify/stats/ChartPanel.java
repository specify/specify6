/* Filename:    $RCSfile: ChartPanel.java,v $
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
package edu.ku.brc.specify.stats;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This base class implements the Chartable interface
 * that enables derived classes to easily accept and have access to information needed to decorate or describe the chart.
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ChartPanel extends JPanel implements Chartable
{
    // Static Data Members
    //private static Log log = LogFactory.getLog(ChartPanel.class);

    // Data Members
    protected org.jfree.chart.ChartPanel chartPanel;
    protected String  title       = "";
    protected String  xAxisTitle  = "";
    protected String  yAxisTitle  = "";
    protected boolean isVertical  = true;
    
    protected JProgressBar      progressBar;
    protected JLabel            progressLabel;

    /**
     * @param startUpMsg
     */
    public ChartPanel(final String startUpMsg)
    {
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("f:max(100px;p):g", "center:p:g, p, center:p:g"));
        CellConstraints cc         = new CellConstraints();

        builder.add(progressBar,                  cc.xy(1,1));
        builder.add(progressLabel = new JLabel(startUpMsg, JLabel.CENTER), cc.xy(1,3));

        PanelBuilder    builder2    = new PanelBuilder(new FormLayout("p:g,p,p:g", "f:p:g"));
        builder2.add(builder.getPanel(), cc.xy(3,1));

        add(builder2.getPanel(), BorderLayout.CENTER);
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

}
