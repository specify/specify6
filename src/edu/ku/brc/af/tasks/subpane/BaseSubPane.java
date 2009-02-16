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

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createProgressBar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.JTiledPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.skin.SkinItem;
import edu.ku.brc.ui.skin.SkinsMgr;

/**
 * Class that implements the SubPanelIFace interface which enables derived classes to participate in the main pane.
 * It also adds the progress indicator and it provide.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
@SuppressWarnings("serial") //$NON-NLS-1$
public class BaseSubPane extends JTiledPanel implements SubPaneIFace, Printable
{
    //private static final Logger log = Logger.getLogger(BaseSubPane.class);

    protected String            name;
    protected Taskable          task;

    protected JProgressBar      progressBar;
    protected JLabel            progressLabel;
    
    protected JPanel			progressBarPanel;
    protected JButton           progressCancelBtn = null;


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
        this(name, task, true, false);
    }
    
    /**
     * @param name
     * @param task
     * @param buildProgressUI
     */
    public BaseSubPane(final String name,
                       final Taskable task,
                       final boolean  buildProgressUI)
    {
    	this(name, task, buildProgressUI, false);
    }
    /**
     * Constructs a base class that implements the SubPanelIFace interface
     * which enables derived classes to participate in the main pane.
     * It also adds the progress indicator and it provide.
     *
     * @param name the name of the subpane
     * @param task the owning task
     * @param buildProgressUI true - build progress UI
     */
    public BaseSubPane(final String name,
                       final Taskable task,
                       final boolean  buildProgressUI,
                       final boolean includeProgressCancelBtn)
    {
        this.name    = name;
        this.task    = task;

        setLayout(new BorderLayout());

        if (buildProgressUI)
        {

            progressBar = createProgressBar();
            progressBar.setIndeterminate(true);
            FormLayout      formLayout = new FormLayout("f:max(100px;p):g", "center:p:g, p, center:p:g"); //$NON-NLS-1$ //$NON-NLS-2$
            PanelBuilder    builder    = new PanelBuilder(formLayout);
            CellConstraints cc         = new CellConstraints();
    
            builder.add(progressBar, cc.xy(1,1));
            builder.add(progressLabel = createLabel("", SwingConstants.CENTER), cc.xy(1,3)); //$NON-NLS-1$
            PanelBuilder    builder2    = includeProgressCancelBtn ?
            		new PanelBuilder(new FormLayout("center:p:g", "center:p:g, p, top:p")): 
            		new PanelBuilder(new FormLayout("center:p:g", "center:p:g")); //$NON-NLS-1$ //$NON-NLS-2$
            		
            builder2.add(builder.getPanel(), cc.xy(1,1));
            if (includeProgressCancelBtn)
            {
            	progressCancelBtn = UIHelper.createButton(UIRegistry.getResourceString("CANCEL"));
            	builder2.add(progressCancelBtn, cc.xy(1, 3));
            }
            progressBarPanel = builder2.getPanel();
            add(progressBarPanel, BorderLayout.CENTER);
        }
        
        SkinItem skinItem = SkinsMgr.getSkinItem("SubPane");
        if (skinItem != null)
        {
            skinItem.setupPanel(this);
        }
    }
    
    //----------------------------------
    // Printable
    //----------------------------------
    
    /* (non-Javadoc)
     * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
     */
    public int print(Graphics g, PageFormat pf, int index) throws PrinterException
    {
        Graphics2D g2 = (Graphics2D) g;
        if (index >= 1)
        {
            return Printable.NO_SUCH_PAGE;
        }
        
        
        //System.out.println(pf.getPaper().getImageableWidth()+", "+pf.getPaper().getImageableHeight());
        double imgWidth  = pf.getImageableWidth();
        double imgHeight = pf.getImageableHeight();
        
        if (true)
        {
            Dimension size        = getSize();
            Image     fullSizeImg = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            Graphics  fsG         = fullSizeImg.getGraphics();
            printAll(fsG);
            fsG.dispose();
            
            int imgW = (int)imgWidth;
            int imgH = (int)imgHeight;
            /*
            int w = size.width;
            int h = size.height;
            
            if (imgW > w || imgH > h)
            {
                double scaleW = 1.0;
                double scaleH = 1.0;
                double scale  = 1.0;

                if (imgW > w)
                {
                    scaleW = (double) w / imgW;
                }
                if (imgH > h)
                {
                    scaleH = (double) h / imgH;
                }
                scale = Math.min(scaleW, scaleH);

                imgW = (int) ((double) imgW * scale);
                imgH = (int) ((double) imgH * scale);
            }*/
            
            Image scaledImg = GraphicsUtils.getScaledImage(new ImageIcon(fullSizeImg), imgW, imgH, true);
            //System.out.println(scaledImg.getWidth(null)+", "+scaledImg.getHeight(null));
            g2.drawImage(scaledImg, 0, 0, null);
        } else
        {
            printAll(g2);
        }
        
        //g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        //g2.drawImage(fullSize, 0, 0, (int)imgWidth, (int)imgHeight, 0, 0, size.width, size.height, null);
        return Printable.PAGE_EXISTS;
    }
    
    protected void printStats(final Printable printable)
    {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(printable);
        if (printJob.printDialog())
        {
            try
            {
                printJob.print();
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BaseSubPane.class, ex);
                throw new RuntimeException(ex);
            }
        }
    }
    
    @SuppressWarnings("cast")
    protected void registerPrintContextMenu()
    {
        if (this instanceof Printable)
        {
            addMouseListener(new MouseAdapter() 
            {
                private void displayMenu(MouseEvent e)
                {
                    if (e.isPopupTrigger())
                    {
                        JPopupMenu menu = new JPopupMenu();
                        JMenuItem printMenu = new JMenuItem(UIRegistry.getResourceString("Print"));
                        menu.add(printMenu);
                        menu.show(e.getComponent(), e.getX(), e.getY());
                        printMenu.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent ev)
                            {
                                printStats(BaseSubPane.this);
                            }
                        });
                        
                    }
                }
                
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    super.mouseClicked(e);
                    displayMenu(e);
                }
                @Override
                public void mousePressed(MouseEvent e)
                {
                    super.mousePressed(e);
                    displayMenu(e);
                }
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    super.mouseReleased(e);
                    displayMenu(e);
                }
                
            });
        }
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
        return task.getIcon(Taskable.StdIcon16);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#getPaneName()
     */
    public String getPaneName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.SubPaneIFace#setPaneName(java.lang.String)
     */
    public void setPaneName(final String name)
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
     * @see edu.ku.brc.af.core.SubPaneIFace#getFirstFocusable()
     */
    public Component getFirstFocusable()
    {
        return null;
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
    
    /**
     * @return true if progressBarPanel should contain a cancel button.
     */
    protected boolean allowProgressCancel()
    {
    	return false;
    }
}
