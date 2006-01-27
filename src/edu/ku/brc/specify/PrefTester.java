/*
 * Filename:    $RCSfile: FormEditor.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.3 $
 * Date:        $Date: 2005/10/20 12:53:02 $
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
package edu.ku.brc.specify;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.prefs.PrefMainPanel;
import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.forms.ViewMgr;

/**
 * The stand alone part of the FormEditor (this is a prototype at the moment that is used for viewing forms) 
 * 
 * @author rods
 *
 */
public class PrefTester
{
    private static Log log = LogFactory.getLog(FormEditor.class);
    
    protected JPanel contentPane;
    protected JFrame mainFrame;
    
    SimpleDateFormat screenDateFormat = null;

    
     /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void initialize() 
    {
        AppPrefs.initialPrefs();
        
        try 
        { 
            //System.out.println(System.getProperty("os.name"));
            
            if (!System.getProperty("os.name").equals("Mac OS X"))
            {
                UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                PlasticLookAndFeel.setMyCurrentTheme(new DesertBlue());
            }
                
            //UIManager.setLookAndFeel(new PlasticLookAndFeel()); 
            //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } 
        catch (Exception e) 
        { 
            log.error("Can't change L&F: ", e); 
        }    
      

        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        // Create and set up the window.
        mainFrame = new JFrame("Preference Tester");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        UICacheManager.register(UICacheManager.TOPFRAME, mainFrame);
        
        
        //initPrefs();


        // load form definitions for Preferences (Might want to move this to a preference Class
        try
        {
            ViewMgr.loadViewFile(XMLHelper.getConfigDirPath("pref_forms.xml"));
            ViewMgr.loadViewFile(XMLHelper.getConfigDirPath("form.xml"));
            
        } catch (Exception ex)
        {
            log.fatal(ex);
            ex.printStackTrace();
        }
        

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("c:p:g", "c:p:g"));
        CellConstraints cc         = new CellConstraints();
        
        JButton btn = new JButton("Prefs");
        btn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                showPrefs();
            }
        });
        builder.add(btn, cc.xy(1,1));

        
        // Create and set up the content pane.
        contentPane = builder.getPanel();
        //contentPane.setOpaque(true); //content panes must be opaque
        
        mainFrame.setContentPane(contentPane);
         
        SwingUtilities.invokeLater(new Runnable() {
            public void run() 
            {
                contentPane.doLayout();
                UICacheManager.forceTopFrameRepaint();
                
                mainFrame.invalidate();
                mainFrame.doLayout();
                mainFrame.repaint();
                
                // XXX Why???
                //mainFrame.pack();
                mainFrame.setSize(new Dimension(500, 300));
                //mainFrame.setSize(mainFrame.getPreferredSize());
                //frame.pack();
                UIHelper.positionAndShow(mainFrame);
                //mainFrame.pack();
                
            }
      });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() 
            {
        try {Thread.sleep(100);showPrefs();}
        catch (InterruptedException ie) {}
            }
        });

    }   
    protected void showPrefs()
    {
        JDialog dlg = new JDialog();
        dlg.setModal(true);
        PrefMainPanel pane = new PrefMainPanel(dlg);
        dlg.setContentPane(pane);
        dlg.pack();
        dlg.doLayout();
        System.out.println(dlg.getPreferredSize());
        dlg.setPreferredSize(dlg.getPreferredSize());
        dlg.setSize(dlg.getPreferredSize());
        UIHelper.centerAndShow(dlg);       
    }
    
    /**
     * 
     */
    protected void initPrefs()
    {
        try
        {
            // First delete everything
            Preferences appPrefs = UICacheManager.getAppPrefs();
            appPrefs.removeNode();
            appPrefs.flush();
            
            appPrefs = UICacheManager.getAppPrefs();
            
            Element root = XMLHelper.readDOMFromConfigDir("prefsInit.xml");
            if (root == null)
            {
                return; // XXX FIXME
            }
            
            List sections = root.selectNodes("/prefs/section");
            for ( Iterator iter = sections.iterator(); iter.hasNext(); ) 
            {
                org.dom4j.Element section = (org.dom4j.Element)iter.next();
                
                String      title       = section.attributeValue("title");
                Preferences sectionNode = appPrefs.node(title);
                if (!sectionNode.getBoolean("isApp", false))
                {
                    sectionNode.put("title", title);
                    sectionNode.putBoolean("isApp", true);
                }
                
                List prefs = section.selectNodes("pref");
                for ( Iterator iterPrefs = prefs.iterator(); iterPrefs.hasNext(); ) 
                {
                    org.dom4j.Element pref = (org.dom4j.Element)iterPrefs.next();
                    
                    String prefTitle  = pref.attributeValue("title");
                    String iconName   = pref.attributeValue("icon");
                    String panelClass = pref.attributeValue("panelClass");
                    
                    Preferences prefNode     = sectionNode.node(prefTitle);
                    String      prefTitleStr = prefNode.get("title", null);
                    if (prefTitleStr == null)
                    {
                        prefNode.put("title", prefTitle);
                        prefNode.put("panelClass", panelClass);
                        
                        URL url = IconManager.getImagePath(iconName);
                        if (url != null)
                        {
                            prefNode.put("iconPath", url.toString());
                            
                        } else
                        {
                            log.error("Image name["+iconName+"] not found.");
                        }
                    }
                }
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            // XXX FIXME
        }
    }

    
    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        // TODO Auto-generated method stub
        PrefTester prefTester = new PrefTester();
        prefTester.initialize();

    }

}
