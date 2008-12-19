/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
        int width  = 300;
        int height = 500;
        
        Image img = null;
        ImageIcon bgImg = IconManager.getIcon("SpecifySplash");
        if (bgImg.getIconWidth() > width || bgImg.getIconHeight() > height)
        {
            img = GraphicsUtils.getScaledImage(IconManager.getIcon("SpecifySplash"), width, height, true);
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
        return new SimpleDescPane(title, task, IconManager.getIcon("SpecifySplash"));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.StartUpTask#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        List<ToolBarItemDesc> items = super.getToolBarItems();
        welcomeBtn = createToolbarButton(getResourceString(STARTUP), "AppIcon", null);
        welcomeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                StartUpTask.this.requestContext();
            }
        });
        
        String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getType();
        boolean hasWelcome = AppPreferences.getRemote().getBoolean(WELCOME_BTN_PREF+ds, true);
        if (hasWelcome)
        {
            items.add(new ToolBarItemDesc(welcomeBtn));
        }
        return items;
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
                
                ImageIcon icon = null;
                String imgBufStr = AppPreferences.getRemote().get("ui.formatting.user_icon_image", null);
                if (imgBufStr != null)
                {
                    icon = GraphicsUtils.uudecodeImage(null, imgBufStr);
                }
                welcomeBtn.setIcon(icon != null ? icon : IconManager.getIcon("AppIcon",IconManager.STD_ICON_SIZE));
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    @SuppressWarnings("unchecked")
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
