/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.fielddesc;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 23, 2007
 *
 */
public class LocalizableBaseApp extends JFrame
{
    protected String           appName             = "";
    protected String           appVersion          = "";
    protected String           appBuildVersion     = "";
    
    /**
     * Checks to see if cache has changed before exiting.
     */
    protected void doAbout()
    {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel iconLabel = new JLabel(IconManager.getIcon("SpecifyLargeIcon"));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 8));
        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(new JLabel("<html>"+appName+" " + appVersion + 
                "<br><br>Biodiversity Research Center<br>University of Kansas<br>Lawrence, KS  USA 66045<br><br>" + 
                "www.specifysoftware.org<br>specify@ku.edu<br><br>" + 
                "<p>The Specify Software Project is<br>"+
                "funded by the Biological Databases<br>"+
                "and Informatics Program of the<br>"+
                "U.S. National Science Foundation <br>(Award DBI-0446544)</P><br>" +
                "Build: " + appBuildVersion + 
                "</html>"), BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(6,6,0,6));
        CustomDialog aboutDlg = new CustomDialog(this, getResourceString("About") + " " +appName, true, CustomDialog.OK_BTN, panel);
        aboutDlg.setOkLabel(getResourceString("Close"));
        UIHelper.centerAndShow(aboutDlg);
    }
    
}
