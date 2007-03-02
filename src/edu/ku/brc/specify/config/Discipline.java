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

package edu.ku.brc.specify.config;

import static edu.ku.brc.helpers.XMLHelper.getAttr;

import java.io.FileInputStream;
import java.lang.ref.WeakReference;
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
public class Discipline implements Comparable<Discipline>
{
    private static final Logger  log = Logger.getLogger(Discipline.class);
            
    // Static WeakReference Data Members
    protected static WeakReference<Vector<Discipline>>            disciplineList  = null;
    protected static WeakReference<Hashtable<String, Discipline>> disciplineHash  = null;
    
    // Data Members
    protected String name;
    protected String title;
    protected int    type;
    
    public Discipline(String name, String title, int type)
    {
        this.name  = name;
        this.title = title;
        this.type  = type;
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
    
    public String toString()
    {
        //return "[" + type + "][" + name + "][" + title + "]";
        return title;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Discipline obj)
    {
        return name.compareTo(obj.name);
    }
    
    //-------------------------------------------------------------------------------
    //-- Static Methods
    //-------------------------------------------------------------------------------
    

    /**
     * Returns a Discipline by name.
     * @param name the name of the discipline
     * @return a Discipline by name.
     */
    public static Discipline getDiscipline(final String name)
    {
        return getDisciplineHash().get(name);
    }
    

    /**
     * Returns a Discipline by title.
     * @param title the title of the discipline
     * @return a Discipline by title.
     */
    public static Discipline getByTitle(final String title)
    {
        for (Discipline discipline : getDisciplineList())
        {
            if (title.equals(discipline.getTitle()))
            {
                return discipline;
            }
        }
        return null;
    }
    
    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     */
    public static Vector<Discipline> getDisciplineList()
    {
        Vector<Discipline> list = null;
        
        if (disciplineList != null)
        {
            list = disciplineList.get();
        }
        
        if (list == null)
        {
            disciplineList = new WeakReference<Vector<Discipline>>(loadDisciplineList());
        }
        
        return disciplineList.get();
    }
    
    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     */
    protected static Vector<Discipline> loadDisciplineList()
    {
        Vector<Discipline> list = new Vector<Discipline>();
        try
        {
            Element root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath("disciplines.xml")));
            if (root != null)
            {
                for ( Iterator i = root.elementIterator( "discipline" ); i.hasNext(); )
                {
                    Element disciplineNode = (Element) i.next();

                    String name   = getAttr(disciplineNode, "name", null);
                    String title  = getAttr(disciplineNode, "title", null);
                    int    type   = getAttr(disciplineNode, "type", 0);

                    Discipline discipline = new Discipline(name, title, type);
                    list.add(discipline);
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
            log.error(ex);
        }
        
        Collections.sort(list);
        return list;
    }

    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     */
    public static Hashtable<String, Discipline> getDisciplineHash()
    {
        Hashtable<String, Discipline> hash = null;
        
        if (disciplineHash != null)
        {
            hash = disciplineHash.get();
        }
        
        if (hash == null)
        {
            disciplineHash = new WeakReference<Hashtable<String, Discipline>>(loadDisciplineHash());
        }
        
        return disciplineHash.get();
    }
    

    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     */
    protected static Hashtable<String, Discipline> loadDisciplineHash()
    {
        Hashtable<String, Discipline> dispHash = new Hashtable<String, Discipline>();
        for (Discipline discipline : getDisciplineList())
        {
            dispHash.put(discipline.getName(), discipline);
        }
        return dispHash;
    }
}
