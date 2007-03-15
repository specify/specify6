/*
 * Copyright (C) 2007 The University of Kansas
 * 
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.help;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.help.BadIDException;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.InvalidHelpSetContextException;
import javax.help.Map;
import javax.swing.AbstractButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.specify.Specify;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 */
public class HelpMgr
{
    private static final Logger log            = Logger.getLogger(HelpMgr.class);
    
    protected static HelpSet    hs;
    protected static HelpBroker hb;
    protected static String     helpSystemName = "SpecifyHelp";

    /**
     * Creates a Helpset and HelpBroker.
     */
    public static void initializeHelp()
    {
        // Find the HelpSet file and create the HelpSet object:
        ClassLoader cl = Specify.class.getClassLoader();
        try
        {
            URL hsURL = HelpSet.findHelpSet(cl, helpSystemName);
            hs = new HelpSet(cl, hsURL);
            
        } catch (Exception ee)
        {
            // Say what the exception really is
            log.error(ee);
            return;
        }
        // Create a HelpBroker object:
        hb = hs.createHelpBroker();
    }

    /**
     * Adds a help menu to the main menu
     */
    public static void initializeHelpUI()
    {
        JMenu     help      = new JMenu(getResourceString("Help"));
        JMenuItem menu_help = new JMenuItem("Specify");
        registerComponent(menu_help);
        help.add(menu_help);
        Specify.getSpecify().getMenuBar().add(help);
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
            return false;
        }
    }

    /**
     * @param component a Button, MenuItem, etc that will access help
     * @param idString the help context for the component. if "" then context is determined 'on the fly' by ContextMgr
     */
    public static void registerComponent(final AbstractButton component, final String idString)
    {
        if (idString == "")
        {
            registerComponent(component);
        } else
        {
            if (HelpMgr.helpAvailable())
            {
                component.addActionListener(new CSH.DisplayHelpFromSource(hb));
                if (isGoodID(idString))
                {
                    CSH.setHelpIDString(component, idString);
                } else
                {
                    CSH.setHelpIDString(component, getDefaultID());
                }
            } else
            {
                component.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        JOptionPane.showConfirmDialog(null,
                                getResourceString("HelpSystemNotLocated"),
                                getResourceString("Help"), JOptionPane.CLOSED_OPTION);
                    }
                });
            }
        }
    }

    /**
     * @param component  a Button, MenuItem, etc that will access help. The help context is determined 'on the fly' by ContextMgr
     */
    public static void registerComponent(final AbstractButton component)
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
                            getResourceString("Help"), JOptionPane.CLOSED_OPTION);
                }
            });
        }
    }

    /**
     * @return name of current help context
     */
    static private String getCurrentContext()
    {
        return ContextMgr.getCurrentContext().getName();
    }

    /**
     * @return the default (currently top level) help context
     */
    static private String getDefaultID()
    {
        return "specify";
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
                return Map.ID.create(getDefaultID(), hs);
            } catch (BadIDException e2)
            {
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
                getResourceString("Help"), JOptionPane.CLOSED_OPTION);

    }

    /**
     * Displays help for the current application context.
     */
    public static void getHelpForContext()
    {
        Map.ID id = getMapID(getCurrentContext());
        if (id == null)
        {
            helpless();
        } else
        {
            try
            {
                hb.setCurrentID(id);
                if (!hb.isDisplayed())
                {
                    hb.setDisplayed(true);
                }
            } catch (InvalidHelpSetContextException e)
            {
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
