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

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rod
 *
 * @code_status Complete
 *
 * May 7, 2008
 *
 */
public class StartUpTask extends edu.ku.brc.af.tasks.StartUpTask
{
    private static final String WELCOME_BTN_PREF = "StartupTask.OnTaskbar";
    private static final String SPECIFY_SPLASH   = "SpecifySplash";
    
    private ToolBarDropDownBtn welcomeBtn;
    private int                indexOfTBB = 0;
    
    /**
     * 
     */
    public StartUpTask()
    {
        super();
        CommandDispatcher.register(PreferencesDlg.PREFERENCES, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.StartUpTask#createSplashPanel()
     */
    @Override
    public JPanel createSplashPanel()
    {
        AppPreferences ap = AppPreferences.getLocalPrefs();
        int width  = ap.getInt("Startup.Image.width", 400);
        int height = ap.getInt("Startup.Image.height", 700);
        
        Image img = null;
        ImageIcon bgImg = IconManager.getIcon(SPECIFY_SPLASH);
        if (bgImg.getIconWidth() > width || bgImg.getIconHeight() > height)
        {
            img = GraphicsUtils.getScaledImage(bgImg, width, height, true);
        } else
        {
            img = bgImg.getImage();
        }
        JPanel splashPanel = new JPanel(new BorderLayout());
        //splashPanel.setBackground(Color.WHITE);
        splashPanel.setOpaque(false);
        splashPanel.add(new JLabel(new ImageIcon(img)), BorderLayout.CENTER);
        return splashPanel;
    }
    
    /**
     * @param title
     * @param task
     * @return
     */
    public static SubPaneIFace createFullImageSplashPanel(final String title, final Taskable task)
    {        
        return new SimpleDescPane(title, task, IconManager.getIcon(SPECIFY_SPLASH));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.StartUpTask#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        toolbarItems = new Vector<ToolBarItemDesc>();
        welcomeBtn = createToolbarButton(getResourceString(STARTUP), "InnerAppIcon", null);
        
        welcomeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                StartUpTask.this.requestContext();
            }
        });
        
        String  discipline = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
        boolean hasWelcome = AppPreferences.getRemote().getBoolean(WELCOME_BTN_PREF+discipline, true);
        if (hasWelcome)
        {
            toolbarItems.add(new ToolBarItemDesc(welcomeBtn));
        }
        return toolbarItems;
    }

    /**
     * @param cmdAction
     */
    protected void prefsChanged(final CommandAction cmdAction)
    {
        AppPreferences remotePrefs = (AppPreferences)cmdAction.getData();
        
        if (remotePrefs == AppPreferences.getRemote())
        {
            String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
            boolean hasWelcome = remotePrefs.getBoolean(WELCOME_BTN_PREF+"."+ds, true);
            
            JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
            if (!hasWelcome)
            {
                indexOfTBB = toolBar.getComponentIndex(welcomeBtn);
                TaskMgr.removeToolbarBtn(welcomeBtn);
                toolBar.validate();
                toolBar.repaint();
                
            } else
            {
                int curInx = toolBar.getComponentIndex(welcomeBtn);
                if (curInx == -1)
                {
                    int inx = indexOfTBB != -1 ? indexOfTBB : 0;
                    TaskMgr.addToolbarBtn(welcomeBtn, inx);
                    toolBar.validate();
                    toolBar.repaint();
                }
                
                welcomeBtn.setIcon(IconManager.getIcon("InnerAppIcon", IconManager.IconSize.Std24));
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
        if (cmdAction.isConsumed())
        {
            return;
        }
            
        if (cmdAction.isType(PreferencesDlg.PREFERENCES))
        {
            prefsChanged(cmdAction);
        }
    }
}
