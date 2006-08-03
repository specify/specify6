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
package edu.ku.brc.specify;


import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.af.prefs.PrefMainPanel;
import edu.ku.brc.helpers.UIHelper;
import edu.ku.brc.ui.UICacheManager;

/**
 * The stand alone part of the FormEditor (this is a prototype at the moment that is used for viewing forms)
 *
 * @code_status Unknown (auto-generated)
 *
 * @author rods
 *
 */
public class PrefTester
{
    private static final Logger log = Logger.getLogger(FormEditor.class);

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
        SpecifyAppPrefs.initialPrefs();

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
        /*
        try
        {
            // First delete everything
            // TODO appPrefs.removeNode(); // maybe add a method on the manager to clear all the prefs
            UICacheManager.getAppPrefs().flush();

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
                AppPrefsIFace sectionNode = appPrefs.node(title);
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

                    AppPrefsIFace prefNode     = sectionNode.node(prefTitle);
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
        */
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
