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
package edu.ku.brc.specify.tools.schemalocale;

import static edu.ku.brc.ui.UIHelper.createLabel;
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
        panel.add(createLabel("<html>"+appName+" " + appVersion + 
                "<br><br>Biodiversity Research Center<br>University of Kansas<br>Lawrence, KS  USA 66045<br><br>" + 
                "www.specifysoftware.org<br>specify@ku.edu<br><br>" + 
                "<p>The Specify Software Project is<br>"+
                "funded by the Biological Databases<br>"+
                "and Informatics Program of the<br>"+
                "U.S. National Science Foundation <br>(Award DBI-0446544)</P><br>" +
                "Build: " + appBuildVersion + 
                "</html>"), BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(6,6,0,6));
        CustomDialog aboutDlg = new CustomDialog(this, getResourceString("ABOUT") + " " +appName, true, CustomDialog.OK_BTN, panel);
        aboutDlg.setOkLabel(getResourceString("CLOSE"));
        UIHelper.centerAndShow(aboutDlg);
    }
    
}
