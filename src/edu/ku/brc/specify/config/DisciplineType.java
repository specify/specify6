/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.config;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.FileInputStream;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.helpers.XMLHelper;


/**
 * This class manages all the available Disciplines.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Mar 2, 2007
 *
 */
public class DisciplineType implements Comparable<DisciplineType>
{
    private static final Logger  log = Logger.getLogger(DisciplineType.class);
    
    public enum STD_DISCIPLINES {fish, herpetology, paleobotany, invertpaleo, vertpaleo, bird, 
                                 mammal, insect, botany, invertebrate, minerals, 
                                 anthropology} // vascplant, fungi, 
            
    // Static SoftReference Data Members
    protected static SoftReference<Vector<DisciplineType>>            disciplineList  = null;
    protected static SoftReference<Hashtable<String, DisciplineType>> disciplineHash  = null;
    
    // Data Members
    protected String  name;
    protected String  title;
    protected int     type;
    protected String  abbrev;
    protected String  folder;
    protected boolean isEmbeddedCollecingEvent;
    
    protected STD_DISCIPLINES disciplineType;
    
    /**
     * @param name
     * @param title
     * @param abbrev
     * @param folder
     * @param type
     * @param isEmbeddedCollecingEvent
     */
    public DisciplineType(final String name, 
                          final String title, 
                          final String abbrev,
                          final String folder,
                          final int type,
                          final boolean isEmbeddedCollecingEvent)
    {
        this.name  = name;
        this.title = title;
        this.type  = type;
        this.abbrev = abbrev;
        this.folder = folder;
        this.isEmbeddedCollecingEvent = isEmbeddedCollecingEvent;
        this.disciplineType = STD_DISCIPLINES.valueOf(name);
    }
    
    public boolean isPaleo()
    {
        return disciplineType == DisciplineType.STD_DISCIPLINES.paleobotany ||
                disciplineType == DisciplineType.STD_DISCIPLINES.vertpaleo ||
                disciplineType == DisciplineType.STD_DISCIPLINES.invertpaleo;
    }

    public String getName()
    {
        return name;
    }

    public String getTitle()
    {
        return title;
    }

    public int getType()
    {
        return type;
    }
    
    /**
     * @return the abbrev
     */
    public String getAbbrev()
    {
        return abbrev;
    }

    /**
     * @return the folder
     */
    public String getFolder()
    {
        return folder;
    }

    /**
     * @return the disciplineType
     */
    public STD_DISCIPLINES getDisciplineType()
    {
        return disciplineType;
    }

    public String toString()
    {
        //return "[" + type + "][" + name + "][" + title + "]";
        return title;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(DisciplineType obj)
    {
        return title.compareTo(obj.title);
    }
    
    /**
     * @return the isEmbeddedCollecingEvent
     */
    public boolean isEmbeddedCollecingEvent()
    {
        return isEmbeddedCollecingEvent;
    }

    /**
     * @param isEmbeddedCollecingEvent the isEmbeddedCollecingEvent to set
     */
    public void setEmbeddedCollecingEvent(boolean isEmbeddedCollecingEvent)
    {
        this.isEmbeddedCollecingEvent = isEmbeddedCollecingEvent;
    }

    //-------------------------------------------------------------------------------
    //-- Static Methods
    //-------------------------------------------------------------------------------
    
    /**
     * Returns a DisciplineType by name.
     * @param name the name of the disciplineType
     * @return a DisciplineType by name.
     */
    public static DisciplineType getDiscipline(final String name)
    {
        DisciplineType disciplineType = getDisciplineHash().get(name);
        if (disciplineType == null)
        {
            for (DisciplineType dt : getDisciplineHash().values())
            {
                if (dt.getTitle().equalsIgnoreCase(name))
                {
                    return dt;
                }
            }
            log.error("Couldn't locate disciplineType["+name+"]");
        }
        return disciplineType;
    }
    

    /**
     * Returns a DisciplineType by title.
     * @param altName the title of the disciplineType
     * @return a DisciplineType by title.
     */
    public static DisciplineType getDiscipline(final DisciplineType.STD_DISCIPLINES dType)
    {
        for (DisciplineType disciplineType : getDisciplineList())
        {
            if (dType == disciplineType.getDisciplineType())
            {
                return disciplineType;
            }
        }
        return null;
    }
    
    /**
     * Returns a DisciplineType by title.
     * @param title the title of the disciplineType
     * @return a DisciplineType by title.
     */
    public static DisciplineType getByTitle(final String title)
    {
        for (DisciplineType disciplineType : getDisciplineList())
        {
            if (title.equals(disciplineType.getTitle()))
            {
                return disciplineType;
            }
        }
        return null;
    }
    
    /**
     * Returns a DisciplineType by name.
     * @param nameStr the name of the disciplineType
     * @return a DisciplineType by title.
     */
    public static DisciplineType getByName(final String nameStr)
    {
        return getDisciplineHash().get(nameStr);
    }
    
    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     */
    public static Vector<DisciplineType> getDisciplineList()
    {
        Vector<DisciplineType> list = null;
        
        if (disciplineList != null)
        {
            list = disciplineList.get();
        }
        
        if (list == null)
        {
            disciplineList = new SoftReference<Vector<DisciplineType>>(loadDisciplineList());
        }
        
        return disciplineList.get();
    }
    
    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     */
    protected static Vector<DisciplineType> loadDisciplineList()
    {
        Vector<DisciplineType> list = new Vector<DisciplineType>();
        try
        {
            Element root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath("disciplines.xml")));
            if (root != null)
            {
                for ( Iterator<?> i = root.elementIterator( "discipline" ); i.hasNext(); )
                {
                    Element disciplineNode = (Element) i.next();

                    String  name      = getAttr(disciplineNode, "name", null);
                    String  title     = getAttr(disciplineNode, "title", null);
                    String  abbrev    = getAttr(disciplineNode, "abbrev", "");
                    String  folder    = getAttr(disciplineNode, "folder", name);
                    int     type      = getAttr(disciplineNode, "type", 0);
                    boolean isEmbedCE = getAttr(disciplineNode, "isembedce", true);
                    DisciplineType disciplineType = new DisciplineType(name, title, abbrev, folder, type, isEmbedCE);
                    list.add(disciplineType);
                }
            } else
            {
                String msg = "The root element for the document was null!";
                log.error(msg);
                throw new ConfigurationException(msg);
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            //edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DisciplineType.class, ex);
            //log.error(ex);
        }
        
        Collections.sort(list);
        return list;
    }

    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     */
    public static Hashtable<String, DisciplineType> getDisciplineHash()
    {
        Hashtable<String, DisciplineType> hash = null;
        
        if (disciplineHash != null)
        {
            hash = disciplineHash.get();
        }
        
        if (hash == null)
        {
            disciplineHash = new SoftReference<Hashtable<String, DisciplineType>>(loadDisciplineHash());
        }
        
        return disciplineHash.get();
    }
    

    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     */
    protected static Hashtable<String, DisciplineType> loadDisciplineHash()
    {
        Hashtable<String, DisciplineType> dispHash = new Hashtable<String, DisciplineType>();
        for (DisciplineType disciplineType : getDisciplineList())
        {
            dispHash.put(disciplineType.getName(), disciplineType);
        }
        return dispHash;
    }
}
