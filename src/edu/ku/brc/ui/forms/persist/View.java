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
package edu.ku.brc.ui.forms.persist;

import java.util.List;
import java.util.Vector;

public class View implements Comparable<View>
{
    
    
    //private static final Logger log = Logger.getLogger(View.class);
    
    protected String               viewSetName;
    protected String               name;
    protected String               desc;
    protected String               className;
    protected List<AltView>        altViews       = new Vector<AltView>();
    protected String               resourceLabels = null;
    
    /**
     * Default Constructor
     *
     */
    public View()
    {
        
    }
    
    /**
     * Create View
     */
    public View(final String viewSetName, 
                final String name, 
                final String className, 
                final String desc,
                final String resourceLabels)
    {
        this.viewSetName    = viewSetName;
        this.name           = name;
        this.className      = className;
        this.desc           = desc;
        this.resourceLabels = resourceLabels;
    }
    
    /**
     * Adds an alternative view
     * @param altView the alternate view
     * @return the altView that was passed in
     */
    public AltView addAltView(final AltView altView)
    {
        altViews.add(altView);
        return altView;
    }
    
    /**
     * Find the default AltView and creates it
     * @return the default altView
     */
    public AltView getDefaultAltView()
    {
        for (AltView altView : altViews)
        {
            if (altView.isDefault())
            {
                return altView;
            }
        }
        throw new RuntimeException("No default Alt View in View["+name+"]");
    }

    /**
     * Find the default AltView and creates it
     * @return the default altView
     */
    public AltView getDefaultAltViewWithMode(AltView.CreationMode editMode)
    {
        // First get default AltView and check to see if it's 
        // edit mode matches the desired edit mode
        AltView defAltView = getDefaultAltView();
        if (defAltView.getMode() == editMode)
        {
            return defAltView;
        }
        
        // OK, so we need to use the AltView that is the opposite of the 
        // of the default AltView's edit mode.
        for (AltView av : altViews)
        {
            if (!av.isDefault() && av.getViewDefName().equals(defAltView.getViewDefName()))
            {
                return av;
            }
        }
        throw new RuntimeException("No default AltView in View["+name+"] with the right mode.");
    }

    /**
     * Finds a AltView by name, if the name is null then it returs the default AltView
     * @param name the name of the altView
     * @return the altView
     */
    public AltView getAltView(final String name)
    {
        if (name == null)
        {
            return getDefaultAltView();
        } else
        {
            for (AltView altView : altViews)
            {
                if (altView.getName().equals(name))
                {
                    return altView;
                }
            }
        }
        return null;
    }
    
    public boolean isSpecialViewEdit()
    {
        if (altViews.size() == 2)
        {
            AltView av0 = altViews.get(0);
            AltView av1 = altViews.get(1);
            
            if (av0.getViewDefName().equals(av1.getViewDefName()))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Clean up internal data 
     */
    public void cleanUp()
    {
        altViews.clear();
    }
    
    /**
     * @param obj view obj to be compared to
     * @return 0,1,-1
     */
    public int compareTo(View obj)
    {
        return name.compareTo(obj.getName());
    }
    
    public List<AltView> getAltViews()
    {
        return altViews;
    }

    public String getResourceLabels()
    {
        return resourceLabels;
    }

    public String getDesc()
    {
        return desc;
    }

    public String getName()
    {
        return name;
    }

    public String getClassName()
    {
        return className;
    }

    public String getViewSetName()
    {
        return viewSetName;
    }

    public String toString()
    {
        return this.name;
    }
     
}
