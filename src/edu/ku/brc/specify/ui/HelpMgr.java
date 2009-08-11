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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.Hashtable;

import javax.help.BadIDException;
import javax.help.DefaultHelpBroker;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.InvalidHelpSetContextException;
import javax.help.Map;
import javax.help.WindowPresentation;
import javax.swing.AbstractButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.ui.UIRegistry;

/**
 * Help System Wrapper to make it easier to start and load help.
 * 
 * @author timbo
 * 
 * @code_status Beta
 * 
 */
public class HelpMgr
{
    private static final Logger log            = Logger.getLogger(HelpMgr.class);

    protected static HelpSet    hs;
    protected static HelpBroker hb;
    protected static String     helpSystemName;
    protected static String     loadingPage = null;
    protected static String     appDefHelpId = "About";
    
    
    protected static Hashtable<Component, String> compHelpHash = new Hashtable<Component, String>();

    /**
     * Creates a Helpset and HelpBroker.
     * @param helpName the name of the help
     */
    public static void initializeHelp(final String helpName, final Image frameIcon)
    {
        helpSystemName = helpName;
        
        // Find the HelpSet file and create the HelpSet object:
        ClassLoader cl = Specify.class.getClassLoader();
        try
        {
            
            URL hsURL = HelpSet.findHelpSet(cl, helpName);
            hs = new HelpSet(cl, hsURL);

        } catch (Exception ee)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(HelpMgr.class, ee);
            // Say what the exception really is
            log.error(ee);
            return;
        }
        // Create a HelpBroker object:
        hb = hs.createHelpBroker();
        hb.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        if (frameIcon != null)
        {
            // try to change the icon of the help window
            if (hb instanceof DefaultHelpBroker)
            {
                DefaultHelpBroker dhb = (DefaultHelpBroker)hb;
                WindowPresentation pres = dhb.getWindowPresentation();
                pres.createHelpWindow();
                Window window = pres.getHelpWindow();
                if (window instanceof JFrame)
                {
                    ((JFrame)window).setIconImage(frameIcon);
                }
            }
        }
    }

    /**
     * @param appDefHelpId the appDefHelpId to set
     */
    public static void setAppDefHelpId(String appDefHelpId)
    {
        HelpMgr.appDefHelpId = appDefHelpId;
    }

    /**
     * @param loadingPage the loadingPage to set
     */
    public static void setLoadingPage(String loadingPage)
    {
        HelpMgr.loadingPage = loadingPage;
    }

    /**
     * Adds a help menu to the main menu.
     * @param helpMenuName the name of the menu item usuall the Application name.
     */
    public static JMenu createHelpMenuItem(final JMenu helpMenu, final String helpMenuName)
    {
        if (helpMenu != null)
        {
            JMenuItem mainHelpMenuItem = new JMenuItem(helpMenuName);
            mainHelpMenuItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
            helpMenu.add(mainHelpMenuItem);
            registerComponent(mainHelpMenuItem, true);
        }
        return helpMenu;
    }

    /**
     * @return true if HelpBroker is initialized
     */
    public static boolean helpAvailable()
    {
        return (hb != null);
    }

    /**
     * @param id a help context to validate
     * @return true if there is a mapping for id in the help system
     */
    static private boolean isGoodID(final String id)
    {
        try
        {
            Map.ID.create(id, hs);
            return true;
            
        } catch (BadIDException e)
        {
            // XXX REMOVE ME BEFORE RELEASE
            // this used to find help that doesn't have a key word.
            JOptionPane.showMessageDialog(null, "No mapping for '" + id + "'");
//            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(HelpMgr.class, e);
            return false;
        }
    }

    /**
     * @param component a Button, MenuItem, etc that will access help
     * @param idString the help context for the component. if "" then context is determined 'on the
     *            fly' by ContextMgr
     */
    public static void registerComponent(final Component component, final String idString)
    {
        compHelpHash.put(component, idString);
    }

    /**
     * @param component a Button, MenuItem, etc that will access help
     * @param idString the help context for the component. if "" then context is determined 'on the
     *            fly' by ContextMgr
     */
    public static void registerComponent(final AbstractButton component, final String idString)
    {
        if (HelpMgr.helpAvailable())
        {
            //XXX this loop seems a little extreme and strange
        	//Not sure if it is necessary.
        	//Maybe it was added to remove previous help listeners?
        	for (ActionListener l : component.getActionListeners())
            {
                component.removeActionListener(l);    
                //log.warn("removing action listener from help component: " + component.getName());
            }
            if (isGoodID(idString))
            {
            	component.addActionListener( new ActionListener(){

    				/* (non-Javadoc)
    				 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
    				 */
    				@Override
    				public void actionPerformed(ActionEvent arg0)
    				{
    					HelpMgr.displayHelp(HelpMgr.getMapID(idString));
    					
    				}
            		
            	});
            } else
            {
                log.warn("No mapping for '" + idString + "'. Defaulting to '" + getDefaultID() + "'");
            }
            if (!isGoodID(idString))
            {
                component.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent ae)
                    {
                        UIRegistry.displayErrorDlg("No mapping for '" + idString + "'");
                    }
                }); 
            }
            component.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    UsageTracker.incrUsageCount("ShowHelp");
                }
            }); 
        } else
        {
            component.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    JOptionPane.showConfirmDialog(null, getResourceString("HelpSystemNotLocated"),
                            getResourceString("HELP"), JOptionPane.CLOSED_OPTION);
                }
            });
        }
    }

    public static void setHelpID(final Component component, final String idString)
    {
     	compHelpHash.put(component, idString);
    }

    /**
     * @param component a Button, MenuItem, etc that will access help. The help context is
     *            determined 'on the fly' by ContextMgr
     * @param focusListener true if the help accessor to try to determine help context from
     *            component with focus
     */
    public static void registerComponent(final AbstractButton component, final boolean focusListener)
    {
        if (HelpMgr.helpAvailable())
        {
            component.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    HelpMgr.getHelpForContext();
                }
            });
        } else
        {
            component.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    JOptionPane.showConfirmDialog(null, getResourceString("HelpSystemNotLocated"),
                            getResourceString("HELP"), JOptionPane.CLOSED_OPTION);
                }
            });
        }
    }

    /**
     * @return name of current help context
     */
    static private String getCurrentContext()
    {
        if (ContextMgr.getCurrentContext() != null)
        {
            log.debug(ContextMgr.getCurrentContext().getName());
            return ContextMgr.getCurrentContext().getName();
        }
        return null;
    }

    /**
     * @return the default (currently top level) help context
     */
    static private String getDefaultID()
    {
        return appDefHelpId;
    }

    /**
     * @param id help context string id to get mapping for
     * @return Map.ID for id, or map.id for getDefaultID() if no Map.ID for id, or null
     */
    static private Map.ID getMapID(final String id)
    {
        try
        {
            return Map.ID.create(id, hs);
        } catch (BadIDException e)
        {
            try
            {
                //XXX remove before release.
            	UIRegistry.displayErrorDlg("No mapping for '" + id + "'. Defaulting to '" + getDefaultID() + "'");
            	log.warn("No mapping for '" + id + "'. Defaulting to '" + getDefaultID() + "'");
                return Map.ID.create(getDefaultID(), hs);
            } catch (BadIDException e2)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(HelpMgr.class, e2);
                return null;
            }
        }
    }

    /**
     * Displays message about lack of help.
     */
    static private void helpless()
    {
        JOptionPane.showConfirmDialog(null, getResourceString("NoHelpForContext"),
                getResourceString("HELP"), JOptionPane.CLOSED_OPTION);

    }

    /**
     * Displays help for the current application context.
     */
    public static void getHelpForContext()
    {
        String       helpTarget = null;
        
        Component focusComp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusComp != null)
        {
        	helpTarget = compHelpHash.get(focusComp);
        }
        if (helpTarget == null)
        {
            SubPaneIFace subPane    = SubPaneMgr.getInstance().getCurrentSubPane();
            if (subPane != null)
            {
                helpTarget = subPane.getHelpTarget();
                
            } else
            {
                helpTarget = getCurrentContext();
            }            
            
            if (helpTarget == null)
            {
                helpTarget = getDefaultID();
            }
        }
        
        if (helpTarget != null)
        {
            displayHelp(getMapID(helpTarget));
        } else
        {
            helpless();
        }
    }

    /**
     * @param id the help mapping to display in the help window
     */
    protected static void displayHelp(final Map.ID id)
    {
        if (id == null)
        {
            helpless();
        } else
        {
            try
            {
                if (hb.isDisplayed())
                {
                	hb.setDisplayed(false);
                }
                if (loadingPage != null)
                {
                    hb.setCurrentID(getMapID(loadingPage));
                }
                hb.setCurrentID(id);
                Window fWin = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
                ((DefaultHelpBroker )hb).setActivationWindow(fWin);
                if (!hb.isDisplayed())
                {
                    hb.setDisplayed(true);
                }
            } catch (InvalidHelpSetContextException e)
            {
            	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            	edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(HelpMgr.class, e);
                helpless();
            }
        }    	
    }

    /**
     * @return the hb
     */
    public static HelpBroker getHb()
    {
        return hb;
    }

    /**
     * @return the helpSystemName
     */
    public static String getHelpSystemName()
    {
        return helpSystemName;
    }

    /**
     * @return the hs
     */
    public static HelpSet getHs()
    {
        return hs;
    }
}
