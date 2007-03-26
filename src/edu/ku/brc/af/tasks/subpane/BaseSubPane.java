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

package edu.ku.brc.af.tasks.subpane;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.ui.forms.MultiView;

/**
 * Class that implements the SubPanelIFace interface which enables derived classes to participate in the main pane.
 * It also adds the progress indicator and it provide.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class BaseSubPane extends JPanel implements SubPaneIFace
{
    //private static final Logger log = Logger.getLogger(BaseSubPane.class);

    protected String            name;
    protected Taskable          task;

    protected JProgressBar      progressBar;
    protected JLabel            progressLabel;
    
    protected JPanel			progressBarPanel;


    /**
     * Constructs a base class that implements the SubPanelIFace interface
     * which enables derived classes to participate in the main pane.
     * It also adds the progress indicator and it provide.
     *
     * @param name the name of the subpane
     * @param task the owning task
     */
    public BaseSubPane(final String name,
                       final Taskable task)
    {
        this.name    = name;
        this.task    = task;

        setLayout(new BorderLayout());

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        FormLayout      formLayout = new FormLayout("f:max(100px;p):g", "center:p:g, p, center:p:g");
        PanelBuilder    builder    = new PanelBuilder(formLayout);
        CellConstraints cc         = new CellConstraints();

        builder.add(progressBar, cc.xy(1,1));
        builder.add(progressLabel = new JLabel("", SwingConstants.CENTER), cc.xy(1,3));

        PanelBuilder    builder2    = new PanelBuilder(new FormLayout("center:p:g", "center:p:g"));
        builder2.add(builder.getPanel(), cc.xy(1,1));

        progressBarPanel = builder2.getPanel();
        add(progressBarPanel, BorderLayout.CENTER);
    }
    
    //----------------------------------
    // SubPaneIFace
    //----------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.SubPaneIFace#getTitle()
     */
    public String getTitle()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.SubPaneIFace#getIcon()
     */
    public Icon getIcon()
    {
        return task.getImageIcon();
    }

    /* (non-Javadoc)
     * @see java.awt.Component#getName()
     */
    @Override
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setName(java.lang.String)
     */
    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.SubPaneIFace#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return this;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#getMultiView()
     */
    public MultiView getMultiView()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.SubPaneIFace#getTask()
     */
    public Taskable getTask()
    {
        return task;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#getRecordSet()
     */
    public RecordSetIFace getRecordSet()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#showingPane(boolean)
     */
    public void showingPane(boolean show)
    {
        //log.info("showingPane "+name+"  "+show);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#getHelpTarget()
     */
    public String getHelpTarget()
    {
        return task != null ? task.getName() : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#aboutToShutdown()
     */
    public boolean aboutToShutdown()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#shutdown()
     */
    public void shutdown()
    {
    }
}
