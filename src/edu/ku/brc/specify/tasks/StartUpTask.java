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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.ku.brc.ui.GraphicsUtils;
import edu.ku.brc.ui.IconManager;

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
    /**
     * 
     */
    public StartUpTask()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.StartUpTask#createSplashPanel()
     */
    @Override
    public JPanel createSplashPanel()
    {
        Image img = GraphicsUtils.getScaledImage(IconManager.getIcon("SpecifySplash"), 300, 500, true);
        JPanel splashPanel = new JPanel(new BorderLayout());
        splashPanel.setBackground(Color.WHITE);
        splashPanel.setOpaque(true);
        splashPanel.add(new JLabel(new ImageIcon(img)), BorderLayout.CENTER);
        return splashPanel;
    }


}
