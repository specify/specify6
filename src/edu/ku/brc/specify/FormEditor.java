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


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import edu.ku.brc.specify.helpers.XMLHelper;
import edu.ku.brc.specify.helpers.*;
import edu.ku.brc.specify.ui.*;
import edu.ku.brc.specify.ui.forms.*;
import edu.ku.brc.specify.ui.forms.persist.*;

import org.dom4j.io.SAXReader;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

/**
 * The stand alone part of the FormEditor (this is a prototype at the moment that is used for viewing forms) 
 * 
 * @author rods
 *
 */
public class FormEditor
{
    private static Log log = LogFactory.getLog(FormEditor.class);
    
    protected JPanel contentPane;
    protected JFrame mainFrame;
    
    /**
     * @param formView
     */
    protected void createForm(FormView formView)
    {       
        FormViewable form = ViewFactory.createView(formView);
        
        contentPane.removeAll();
        JComponent comp = form.getUIComponent();
        if (comp != null)
        {
            comp.invalidate();   
            contentPane.add(comp, BorderLayout.CENTER);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() 
                {
                    contentPane.doLayout();
                    UICacheManager.forceTopFrameRepaint();
                    
                    mainFrame.invalidate();
                    mainFrame.doLayout();
                    mainFrame.repaint();
                    
                    // XXX Why???
                    mainFrame.pack();
                    mainFrame.setSize(new Dimension(1024, 764));
                    //frame.pack();
                    UIHelper.centerAndShow(mainFrame);

                }
          });
            
        }

    }
    
    /**
     * 
     */
    protected void selectForm()
    {
        List<ViewSet> viewSets = ViewMgr.getViewSets();
        
        ViewSet viewSet = viewSets.get(0);
        List<FormView> forms = viewSet.getViews();
        ChooseFromListDlg dlg = new ChooseFromListDlg("Choose Form", forms); // XXX I18N
        FormView form = (FormView)dlg.getSelectedObject();
        if (form != null)
        {
            createForm(form);
        }
    }
    
     /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void initialize() 
    {
        // XXX Temporary load of form because now forma er being loaded right now
        try
        {
            ViewMgr.loadViewFile(XMLHelper.getConfigDirPath("fish_forms.xml"));
            
        } catch (Exception ex)
        {
            log.fatal(ex);
            ex.printStackTrace();
        }
       
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
        mainFrame = new JFrame("Specify Form Editor");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        UICacheManager.register(UICacheManager.TOPFRAME, mainFrame);
        
        // Create and set up the content pane.
        contentPane = new JPanel(new BorderLayout());
        contentPane.setOpaque(true); //content panes must be opaque
        mainFrame.setContentPane(contentPane);

        //Display the window.
        //mainFrame.pack();
        //mainFrame.setSize(new Dimension(1024, 764));
        //frame.pack();
        //mainFrame.setVisible(true);
        
        // temp for testing 
        List<ViewSet> viewSets = ViewMgr.getViewSets();
        ViewSet viewSet = viewSets.get(0);
        List<FormView> forms = viewSet.getViews();
        createForm(forms.get(0));
         
    }   
    
    /**
     * @param args
     */
    public static void main(String[] args) 
    {
        // TODO Auto-generated method stub
        FormEditor formEditor = new FormEditor();
        formEditor.initialize();

    }

}
