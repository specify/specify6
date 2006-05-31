/* Filename:    $RCSfile: StartUpTask.java,v $
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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.core.Taskable;
import edu.ku.brc.specify.plugins.MenuItemDesc;
import edu.ku.brc.specify.plugins.ToolBarItemDesc;
import edu.ku.brc.specify.tasks.subpane.SimpleDescPane;
import edu.ku.brc.specify.tasks.subpane.StatsPane;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.ImageSwirlPanel;
import edu.ku.brc.specify.ui.SubPaneIFace;
import edu.ku.brc.specify.ui.ToolBarDropDownBtn;
import edu.ku.brc.specify.ui.UICacheManager;

/**
 * This task will enable the user to create queries, save them and execute them.
 *
 * @author rods
 *
 */
public class StartUpTask extends BaseTask
{
    public static final String STARTUP = "Startup";

    protected Vector<ToolBarDropDownBtn> tbList = new Vector<ToolBarDropDownBtn>();
    protected SubPaneIFace blankPanel = null;

    // XXX Demo Only
    StatsPane statPane;


    /**
     * Default Constructor
     *
     */
    public StartUpTask()
    {
        super(STARTUP, getResourceString(STARTUP));

        icon = IconManager.getImage(STARTUP, IconManager.IconSize.Std16);
    }

    /**
     * Creates the StartUP Statistics pane and removes the blank pane
     */
    public void createStartUpStatPanel()
    {
        StatsPane statPane = new StatsPane(title, this, "startup_panel.xml", true, null);
        UICacheManager.getSubPaneMgr().removePane(blankPanel);
        UICacheManager.getSubPaneMgr().addPane(statPane);
        blankPanel = null;
    }

    /**
     * @return the blank SubPane or null
     */
    public SubPaneIFace getBlankPane()
    {
        return blankPanel;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        //blankPanel = new SimpleDescPane("", this, "");
        //System.out.println(blankPanel);
        //return blankPanel;

        // XXX Real way
        if (true)
        {
            StatsPane statPane = new StatsPane(title, this, "startup_panel.xml", true, null);
            return statPane;

        } else
        {
            if (true)
            {
                java.io.File  f = new java.io.File(".");
                SimpleDescPane sdp = new SimpleDescPane("", this, "");
                sdp.setBackground(Color.WHITE);
                sdp.setOpaque(true);
                sdp.removeAll();
                //sdp.setLayout(new BorderLayout());
                ImageSwirlPanel isp = new ImageSwirlPanel(f.getAbsolutePath() +  File.separator + "splashfish400.png", 4, 1000);
                isp.setBackground(Color.WHITE);
                isp.setOpaque(true);

                PanelBuilder builder = new PanelBuilder(new FormLayout("C:P:G", "p,p,p"), sdp);
                //PanelBuilder builder = new PanelBuilder(new FormLayout("c:p", "c:p"), sdp);
                CellConstraints cc = new CellConstraints();
                builder.add(isp, cc.xy(1,1));

                builder.add(new JLabel(new ImageIcon(IconManager.getImagePath("specify_splash.gif"))), cc.xy(1,3));

                isp.addMouseListener(new MouseAdapter()
                    {
                    public void mouseClicked(MouseEvent ev)
                    {
                        if (!ev.isShiftDown())
                        {
                            ((ImageSwirlPanel)ev.getSource()).startAnimation();

                        } else
                        {
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run()
                                {
                                    switchToDemoStart();
                                }
                            });
                        }
                    }
                    });
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        createStats();
                    }
                });
                return sdp;

            } else
            {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        createStats();
                    }
                });
            }


            blankPanel = new DemoPane("", this, "", this);
            return blankPanel;

        }


    }
    // XXX Demo only
    protected void createStats()
    {
        statPane = new StatsPane(title, this, "startup_panel.xml", true, null);
    }

    protected void switchToDemoStart()
    {
        UICacheManager.getSubPaneMgr().removePane(UICacheManager.getSubPaneMgr().getCurrentSubPane());
        blankPanel = new DemoPane("", this, "", this);
        UICacheManager.getSubPaneMgr().addPane(blankPanel);
    }


    //-------------------------------------------------------
    // Plugin Interface
    //-------------------------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();

        /*ToolBarDropDownBtn btn = createToolbarButton(name, "queryIt.gif", "search_hint");
        if (tbList.size() == 0)
        {
            tbList.add(btn);
        }
        list.add(new ToolBarItemDesc(btn));
        */
        return list;

    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();

        return list;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.TaskPluginable#getTaskClass()
     */
    public Class getTaskClass()
    {
        return this.getClass();
    }


    // XXX For Demo only
    protected void swapPanes()
    {
        UICacheManager.getSubPaneMgr().addPane(statPane);
        UICacheManager.getSubPaneMgr().removePane(blankPanel);
    }

    class DemoPane extends SimpleDescPane
    {
        String[]    imgNames = {"directorsview.png", "accessions.png"};
        ImageIcon[] imgs     = {null, null};

        protected int currentStep = 0;
        protected JLabel imgLabel;
        protected StartUpTask startUpTask;


        public DemoPane(final String name,
                        final Taskable task,
                        final String desc,
                        final StartUpTask startUpTask)
        {
            super(name, task, desc);
            this.startUpTask = startUpTask;

            java.io.File  f = new java.io.File(".");
            for (int i=0;i<imgNames.length;i++)
            {
                imgs[i] = new ImageIcon(f.getAbsolutePath() +  File.separator + imgNames[i]);

            }
            removeAll();
            imgLabel = new JLabel(imgs[currentStep]);
            add(imgLabel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter()
                    {
                    public void mouseClicked(MouseEvent ev)
                    {
                        if (ev.isShiftDown())
                        {
                            if (currentStep > 0)
                            {
                                currentStep--;
                                imgLabel.setIcon(imgs[currentStep]);
                            }
                        } else
                        {
                            if (currentStep < imgNames.length-1)
                            {
                                currentStep++;
                                imgLabel.setIcon(imgs[currentStep]);

                            } else if (currentStep == imgNames.length-1)
                            {
                                startUpTask.swapPanes();
                            }
                        }
                    }
                    });
        }
    }

}
