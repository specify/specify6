/* Filename:    $RCSfile: BaseSubPane.java,v $
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

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.ui.SubPaneIFace;

/**
 * Class that implements the SubPanelIFace interface which enables derived classes to participate in the main pane.
 * It also adds the progress indicator and it provide.
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class BaseSubPane extends JPanel implements SubPaneIFace
{
    //private static Log log = LogFactory.getLog(BaseSubPane.class);

    protected String            name;
    protected Taskable          task;

    protected JProgressBar      progressBar;
    protected JLabel            progressLabel;

    /**
     * Constructsa base class that implements the SubPanelIFace interface
     * which enables derived classes to participate in the main pane.
     * It also adds the progress indicator and it provide
     *
     */
    public BaseSubPane(final String name,
                       final Taskable task)
    {
        this.name = name;
        this.task = task;

        setLayout(new BorderLayout());

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        FormLayout      formLayout = new FormLayout("f:max(100px;p):g", "center:p:g, p, center:p:g");
        PanelBuilder    builder    = new PanelBuilder(formLayout);
        CellConstraints cc         = new CellConstraints();

        builder.add(progressBar,                  cc.xy(1,1));
        builder.add(progressLabel = new JLabel("", JLabel.CENTER), cc.xy(1,3));

        PanelBuilder    builder2    = new PanelBuilder(new FormLayout("center:p:g", "center:p:g"));
        builder2.add(builder.getPanel(), cc.xy(1,1));

        add(builder2.getPanel(), BorderLayout.CENTER);

    }
    

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintChildren(java.awt.Graphics)
     */
    /*public void paintChildren(Graphics g)
    {
        
        ImageIcon imgIcon = IconManager.getImage("BGImage");
        if (imgIcon != null)
        {
            Dimension size = getSize();
            g.drawImage(imgIcon.getImage(), (size.width - imgIcon.getIconWidth())/2, (size.height - imgIcon.getIconHeight())/2, imgIcon.getIconWidth(), imgIcon.getIconHeight(), null);
        }
        super.paintChildren(g);
        
    }*/

    //----------------------------------
    // SubPaneIFace
    //----------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.SubPaneIFace#getTitle()
     */
    public String getTitle()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.SubPaneIFace#getIcon()
     */
    public Icon getIcon()
    {
        return task.getIcon();
    }

    /* (non-Javadoc)
     * @see java.awt.Component#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.SubPaneIFace#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.SubPaneIFace#getTask()
     */
    public Taskable getTask()
    {
        return task;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#showingPane(boolean)
     */
    public void showingPane(boolean show)
    {
        //log.info("showingPane "+name+"  "+show);
    }


}
