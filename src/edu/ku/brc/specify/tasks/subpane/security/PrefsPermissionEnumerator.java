/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tasks.subpane.security;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import edu.ku.brc.af.auth.specify.permission.BasicSpPermission;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.SpPermission;
import edu.ku.brc.specify.datamodel.SpPrincipal;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIRegistry;

/**
 * This class enumerates task related permissions associated with a principal
 * 
 * @author Ricardo
 *
 */
public class PrefsPermissionEnumerator extends PermissionEnumerator 
{
	public final String permissionBaseName = "Prefs";

	/**
	 * 
	 */
	public PrefsPermissionEnumerator()
	{
	    
	}
	
	@Override
	public List<PermissionEditorRowIFace> getPermissions(final SpPrincipal principal, 
			 final Hashtable<String, SpPermission> existingPerms,
			 final Hashtable<String, SpPermission> overrulingPerms) 
	{
		// iterate through all possible tasks
	    Vector<PermissionEditorRowIFace> perms = new Vector<PermissionEditorRowIFace>();
		
		String type = "Prefs";
		// create a special permission that allows user to see all forms
		perms.add(getStarPermission(permissionBaseName, 
		                            type,
		                            "Prefs: permissions to all preferences", // I18N
				                    "Permissions to view, add, modify and delete data in all prefs", 
				                    existingPerms, 
				                    overrulingPerms));

        try
        {
            Element root = XMLHelper.readDOMFromConfigDir("prefs_init.xml"); //$NON-NLS-1$
            if (root != null)
            {
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
                        
                        // first check if there is a permission with this name
                        String       prefName = permissionBaseName + "." + name;
                        SpPermission perm     = existingPerms.get(prefName);
                        SpPermission oPerm    = (overrulingPerms != null)? overrulingPerms.get(prefName) : null;
                
                        if (perm == null)
                        {
                            perm = new SpPermission();
                            perm.setName(prefName);
                            perm.setActions("");
                            perm.setPermissionClass(BasicSpPermission.class.getCanonicalName());
                        }
                        
                        String desc = "Permissions to view, add, modify and delete data in pref " + title;
                
                        // add newly created permission to the bag that will be returned
                        perms.add(new GeneralPermissionEditorRow(perm, oPerm, type, title, desc, IconManager.getIcon(iconName, IconManager.IconSize.Std20)));
                    }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
		
        Collections.sort(perms);
		return perms;
	}
}
