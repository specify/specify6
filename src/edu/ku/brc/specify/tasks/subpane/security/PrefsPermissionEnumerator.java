/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import edu.ku.brc.af.auth.SecurityOption;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.core.PermissionIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.tasks.BaseTask;
import edu.ku.brc.specify.tasks.PermissionOptionPersist;
import edu.ku.brc.ui.UIRegistry;

/**
 * This class enumerates Pereferences related permissions associated with a principal.
 * 
 * @author rods
 *
 */
public class PrefsPermissionEnumerator extends PermissionEnumerator 
{
	protected List<SecurityOptionIFace> prefOptions = null;
	
	/**
	 * Constructor.
	 */
	public PrefsPermissionEnumerator()
	{
	    super("Prefs", "ADMININFO_DESC");
    }
    
    /**
     * Loads the Prefs and the Default Permissions from XML.
     */
    protected void loadPrefs() 
    {
        prefOptions = new ArrayList<SecurityOptionIFace>();
        try
        {
            Element root = XMLHelper.readDOMFromConfigDir("prefs_init.xml"); //$NON-NLS-1$
            if (root != null)
            {
                Hashtable<String, Hashtable<String, PermissionOptionPersist>> mainHash = BaseTask.readDefaultPrefsFromXML("prefsperms.xml");

                Node           prefsNode        = root.selectSingleNode("/prefs");
                String         i18NResourceName = getAttr((Element)prefsNode, "i18nresname", (String)null);
                ResourceBundle resBundle        = null;
                
                if (StringUtils.isNotEmpty(i18NResourceName))
                {
                    resBundle = UIRegistry.getResourceBundle(i18NResourceName);
                }

                List<?> sections = root.selectNodes("/prefs/section/pref"); //$NON-NLS-1$
                for ( Iterator<?> iter = sections.iterator(); iter.hasNext(); )
                {
                    org.dom4j.Element pref = (org.dom4j.Element)iter.next();
                    
                    String name     = getAttr(pref, "name", null);
                    String title    = getAttr(pref, "title", null);
                    String iconName = getAttr(pref, "icon", null);
                    
                    if (StringUtils.isNotEmpty(name))
                    {
                        if (resBundle != null)
                        {
                            try 
                            {
                                title = resBundle.getString(title);
                                
                            } catch (MissingResourceException ex)
                            {
                                //log.error("Couldn't find key["+title+"]"); 
                            }
                        }
                    
                        SecurityOption securityOption = new SecurityOption(name, title, "Prefs", iconName);
                        prefOptions.add(securityOption);
                        
                        if (mainHash != null)
                        {
                            Hashtable<String, PermissionOptionPersist> hash = mainHash.get(name);
                            if (hash != null)
                            {
                                for (PermissionOptionPersist tp : hash.values())
                                {
                                    PermissionIFace defPerm = tp.getDefaultPerms();
                                    securityOption.addDefaultPerm(tp.getUserType(), defPerm);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
	}
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.security.PermissionEnumerator#getSecurityOptions()
     */
    @Override
    protected List<SecurityOptionIFace> getSecurityOptions()
    {
        if (prefOptions == null)
        {
            loadPrefs();
        }
        return prefOptions;
    }
}
