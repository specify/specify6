/* Filename:    $RCSfile: PreferencesIFace.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/19 19:59:54 $
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
package edu.ku.brc.specify.prefs;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Call Groups no matter of the level must have a unique Name
 * @author rods
 *
 */
public class PreferencesMgr implements PreferencesIFace
{
    private static Log            log      = LogFactory.getLog(PreferencesMgr.class);
    private static PreferencesMgr prefsMgr = new PreferencesMgr();
    
    private Hashtable<String, PrefGroup> groups = new Hashtable<String, PrefGroup>();
    
    private boolean                      changed = false;
    

    /**
     * Default private constructor for singleton
     *
     */
    private PreferencesMgr()
    {
    }
    
    /**
     * the preferencemgr instance
     * @return the preferencemgr instance
     */
    public static PreferencesIFace getInstance()
    {
        return prefsMgr;
    }
    
     /**
     * Loads Prefs from a file
     * @return whether it was loaded
     */
    public boolean load()
    {/*
        try
        {
            List groupsList = new ArrayList();
            File xmlFile    = new File("/home/rods/workspace/Specify/src/preferences.xml");
            Digester digester = DigesterLoader.createDigester(getClass().getResource("preferences_digester.xml"));
            digester.push(groupsList);
            
            
            Object root = digester.parse(xmlFile);

            for (Iterator iter=groupsList.iterator();iter.hasNext();)
            {
                PrefGroup grp = (PrefGroup)iter.next();
                groups.put(grp.getName(), grp);
            }
            
            //changed = true;
            //save();
            return true;
        } catch (Exception ex)
        {
            log.fatal(ex);
        }
        */
        return false;
        
    }
    
    /**
     * Can't use Betwixt
     *
     */
    public void save()
    {
        if (changed)
        {
            try
            {
                Collection<PrefGroup> collection = new ArrayList<PrefGroup>(); 
                for (Enumeration e=groups.elements();e.hasMoreElements();)
                {
                    collection.add((PrefGroup)e.nextElement());
                }
    
                File       file = new File("/home/rods/workspace/Specify/src/preferencesNew.xml");
                FileWriter fw   = new FileWriter(file);
                
                fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    
                BeanWriter beanWriter = new BeanWriter(fw);            
                XMLIntrospector introspector = beanWriter.getXMLIntrospector();
                introspector.getConfiguration().setWrapCollectionsInElement(false);
                beanWriter.getBindingConfiguration().setMapIDs(false);
                
                beanWriter.enablePrettyPrint();
                beanWriter.write("groups", collection);
                
                fw.close();
                
            } catch(Exception ex)
            {
                log.error("error writing prefs", ex);
            }
        }
    }
    
    /**
     * 
     * @param aName name of group
     * @return group by name
     */
    public PrefGroupIFace getGroupByName(String aName)
    {
        for (Enumeration e=groups.elements();e.hasMoreElements();)
        {
            PrefGroupIFace grp = (PrefGroupIFace)e.nextElement();
            if (grp != null && grp.getName().equals(aName))
            {
                return grp;
            }
        }
        return null;
    }
    
    //----------------------------------------------------------------------------
    //-- PreferencesIFace Interface
    //----------------------------------------------------------------------------
    public PrefIFace getPref(String aGroup, String aSubGroup, String aName)
    {
        PrefGroupIFace grp = getGroupByName(aGroup);
        if (grp != null)
        {
            PrefSubGroupIFace subGroup = grp.getSubGroupByName(aSubGroup);
            if (subGroup != null)
            {
                PrefIFace pref = subGroup.getPrefByName(aName);
                if (pref != null)
                {
                    return pref;
                }
                log.error("Couldn't find pref ["+aName+"] for Group["+aGroup+"] Subgroup["+aSubGroup+"]");
            } else
            {
                log.error("Couldn't find Subgroup["+aSubGroup+"] Group["+aGroup+"]");
            }
            
        } else
        {
            log.error("Couldn't find Group["+aGroup+"]");
        }
        return null;
    }
    
    /**
     * 
     */
    public PrefIFace getPrefByPath(String aPath)
    {
        StringTokenizer st = new StringTokenizer(aPath, "/");
        if (st.countTokens() != 3)
        {
            log.error("The path ["+aPath+"] didn't have exactly three segments.");
            return null;
        }
        
        String group    = st.nextToken();
        String subgroup = st.nextToken();
        String pref     = st.nextToken();
        return getPref(group, subgroup, pref);
    }
    
    public void setPrefsChanged()
    {
        changed = true;
    }
    
    public void persist()
    {
        
    }
    
}
