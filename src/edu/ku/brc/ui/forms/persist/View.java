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

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.ui.forms.BusinessRulesIFace;

/**
 * A view is a virtual object that may contain one or more "alternate" views. Typically, there is a 
 * read-only or display view and an "edit" view.
 * 
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class View implements Comparable<View>
{
    protected String               viewSetName;
    protected String               name;
    protected String               desc;
    protected String               objTitle; // The title of a single object
    protected String               className;
    protected String               businessRulesClassName;
    protected List<AltView>        altViews       = new Vector<AltView>();
    protected boolean              useResourceLabels;
    protected String               resourceLabels = null;
    
    protected AltView.CreationMode defaultMode    = AltView.CreationMode.View;
    protected String               selectorName   = null;
    
    // transient data members
    protected BusinessRulesIFace   businessRule = null;
    protected Boolean              isSpecial    = null;
    
   
    /**
     * Constructs a View.
     * @param viewSetName the ViewSet Name
     * @param objTitle the name of a single object of this view
     * @param name the name of the view
     * @param className the class name fr the data object
     * @param businessRulesClassName the fully specified class name of the busniess rules object (implementing BusniessRulesIFace)
     * @param desc a description of the view (for express search indexing)
     * @param resourceLabels (not sure)
     */
    public View(final String viewSetName, 
                final String name, 
                final String objTitle, 
                final String className, 
                final String businessRulesClassName,
                final String desc,
                final boolean useResourceLabels,
                final String resourceLabels)
    {
        this.viewSetName    = viewSetName;
        this.name           = name;
        this.objTitle       = objTitle;
        this.className      = className;
        this.businessRulesClassName = businessRulesClassName;
        this.desc           = desc;
        this.useResourceLabels = useResourceLabels;
        this.resourceLabels = resourceLabels;

    }
    
    /**
     * Adds an alternative view.
     * @param altView the alternate view
     * @return the altView that was passed in
     */
    public AltView addAltView(final AltView altView)
    {
        altViews.add(altView);
        return altView;
    }
    
    /**
     * Find the default AltView and creates it.
     * @param altViewType look for a default view for this type of view
     * @return the default altView
     */
    public AltView getDefaultAltView(final AltView.CreationMode creationMode, final String altViewType)
    {
        
        if (creationMode != null && StringUtils.isNotEmpty(altViewType))
        {
            AltView defAltView = null;
            boolean isForm     = altViewType.equals("form");
            for (AltView altView : altViews)
            {
                ViewDef.ViewType type = altView.getViewDef().getType();
                System.out.println("View.getDefaultAltView ["+type+"]["+altView.getName()+"] mode["+altView.getMode()+"]["+creationMode+"]");
                if (isForm && type == ViewDef.ViewType.form ||
                    !isForm && type != ViewDef.ViewType.form)
                {
                    if (altView.getMode() == creationMode)
                    {
                        return altView;
                    }
                }
                
                if (altView.isDefault())
                {
                    defAltView = altView;
                }
            }
            
            if (defAltView != null)
            {
                return defAltView;
            }
            
        } else
        {
            for (AltView altView : altViews)
            {
                if (altView.isDefault())
                {
                    return altView;
                }
            }
        }

        throw new RuntimeException("No default Alt View in View["+name+"]");
    }
    
    /**
     * Find the default AltView and creates it.
     * @return the default altView
     */
    public AltView getDefaultAltView()
    {
        return getDefaultAltView(null, null);
    }

    /**
     * Find the default AltView for a mode and creates it.
     * @param creationMode the mode to be looked up
     * @param defAltViewType default Alt View Type
     * @return @return the default altView
     */
    public AltView getDefaultAltViewWithMode(final AltView.CreationMode creationMode, final String defAltViewType)
    {
        // First get default AltView and check to see if it's 
        // edit mode matches the desired edit mode
        AltView defAltView = getDefaultAltView(creationMode, defAltViewType);
        if (defAltView.getMode() == creationMode || altViews.size() == 1)
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
     * Finds a AltView by name, if the name is null then it returs the default AltView.
     * @param nameStr the name of the altView
     * @return the altView
     */
    public AltView getAltView(final String nameStr)
    {
        if (nameStr == null)
        {
            return getDefaultAltView();
            
        }
        // else
        for (AltView altView : altViews)
        {
            if (altView.getName().equals(nameStr))
            {
                return altView;
            }
        }
        return null;
    }
    
    /**
     * Returns whether it is a special view, meaning a view with just to AltViews where one is edit and the other is view.
     * @return whether it is a special view, meaning a view with just to AltViews where one is edit and the other is view.
     */
    public boolean isSpecialViewAndEdit()
    {
        // Note: it may still be special even if altView == 3, but then it was agumented with the Grid View
        if (isSpecial == null)
        {
            if (altViews.size() == 2)
            {
                AltView av0 = altViews.get(0);
                AltView av1 = altViews.get(1);
                
                isSpecial = av0.getViewDefName().equals(av1.getViewDefName());
                
            } else
            {
                isSpecial = false;
            }
        }
        return isSpecial;
    }
    
    /**
     * Creates an instance of the BusinessRuleIFace object for processing.
     * @return an instance of the BusinessRuleIFace object for processing.
     */
    public BusinessRulesIFace getBusinessRule()
    {
        if (businessRule == null && StringUtils.isNotEmpty(businessRulesClassName))
        {
            try 
            {
                businessRule =  (BusinessRulesIFace)Class.forName(businessRulesClassName).newInstance();
                 
            } catch (Exception e) 
            {
                
                InternalError error = new InternalError("Can't instantiate BusinessRulesIFace [" + businessRulesClassName + "]");
                error.initCause(e);
                throw error;
            }
        }
        
        return businessRule;

    }

    /**
     * Clean up internal data.
     */
    public void cleanUp()
    {
        altViews.clear();
        businessRule = null;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(View obj)
    {
        return name.compareTo(obj.getName());
    }
    
    public List<AltView> getAltViews()
    {
        return altViews;
    }

    public boolean isUseResourceLabels()
    {
        return useResourceLabels;
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

    public String getObjTitle()
    {
        return StringUtils.isNotEmpty(objTitle) ? objTitle : name;
    }

    public String getClassName()
    {
        return className;
    }

    public String getViewSetName()
    {
        return viewSetName;
    }

    public AltView.CreationMode getDefaultMode()
    {
        return defaultMode;
    }

    public String getSelectorName()
    {
        return selectorName;
    }

    public void setDefaultMode(AltView.CreationMode defaultMode)
    {
        this.defaultMode = defaultMode;
    }

    public void setSelectorName(String selectorName)
    {
        this.selectorName = selectorName;
    }

    @Override
    public String toString()
    {
        return this.name;
    }
     
}
