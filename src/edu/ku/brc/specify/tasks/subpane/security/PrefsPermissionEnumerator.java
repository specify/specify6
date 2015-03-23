/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import com.thoughtworks.xstream.XStream;

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
                Hashtable<String, Hashtable<String, PermissionOptionPersist>> mainHash = BaseTask.readDefaultPermsFromXML("prefsperms.xml");

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
                                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PrefsPermissionEnumerator.class, ex);
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
                                for (PermissionOptionPersist pp : hash.values())
                                {
                                    PermissionIFace defPerm = pp.getDefaultPerms();
                                    securityOption.addDefaultPerm(pp.getUserType(), defPerm);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PrefsPermissionEnumerator.class, ex);
            ex.printStackTrace();
        }
	}
    
    /**
     * 
     */
    protected void createDefaultPrefPerms() 
    {
        prefOptions = new ArrayList<SecurityOptionIFace>();
        try
        {
            XStream xstream = new XStream();
            PermissionOptionPersist.config(xstream);
            Hashtable<String, Hashtable<String, PermissionOptionPersist>> hash = new Hashtable<String, Hashtable<String, PermissionOptionPersist>>();

            Element root = XMLHelper.readDOMFromConfigDir("prefs_init.xml"); //$NON-NLS-1$
            if (root != null)
            {
                List<?> sections = root.selectNodes("/prefs/section/pref"); //$NON-NLS-1$
                for ( Iterator<?> iter = sections.iterator(); iter.hasNext(); )
                {
                    org.dom4j.Element pref = (org.dom4j.Element)iter.next();
                    String name = getAttr(pref, "name", null);
                    if (StringUtils.isNotEmpty(name))
                    {
                        Hashtable<String, PermissionOptionPersist> hashItem = new Hashtable<String, PermissionOptionPersist>();
                        hash.put(name, hashItem);

                        List<?> defPerms = pref.selectNodes("defperm"); //$NON-NLS-1$
                        if (defPerms != null && !defPerms.isEmpty())
                        {
                            for ( Iterator<?> iter2 = defPerms.iterator(); iter2.hasNext(); )
                            {
                                org.dom4j.Element defPref = (org.dom4j.Element)iter2.next();
                                String defPermName = getAttr(defPref, "name", null);
                                String perms       = getAttr(defPref, "perm", null);
                                if (StringUtils.isNotEmpty(defPermName) && StringUtils.isNotEmpty(perms))
                                {
                                    String[] p = StringUtils.split(perms, ',');
                                    if (p.length == 4)
                                    {
                                        boolean[] b = new boolean[4];
                                        for (int i=0;i<b.length;i++)
                                        {
                                            b[i] = Boolean.parseBoolean(p[i]);
                                        }
                                        PermissionOptionPersist securityOption = new PermissionOptionPersist(name, defPermName, b[0], b[1], b[2], b[3]);
                                        hashItem.put(defPermName, securityOption);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            FileUtils.writeStringToFile(new File("prefsperms.xml"), xstream.toXML(hash)); //$NON-NLS-1$
            
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
        //createDefaultPrefPerms();
        
        if (prefOptions == null)
        {
            loadPrefs();
        }
        return prefOptions;
    }
}
